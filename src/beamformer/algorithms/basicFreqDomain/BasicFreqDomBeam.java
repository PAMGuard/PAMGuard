/*
 *  PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation
 * of marine mammals (cetaceans).
 *
 * Copyright (C) 2006
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */



package beamformer.algorithms.basicFreqDomain;

import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import beamformer.algorithms.BeamInformation;
import fftManager.Complex;
import fftManager.FFTDataUnit;
import pamMaths.PamVector;

/**
 * @author mo55
 *
 */
public class BasicFreqDomBeam extends BeamInformation {

	/**
	 * A reference to the beam former creating this beam
	 */
	BasicFreqDomBeamFormer beamformer;
	
	/**
	 * Channel map describing the channels (hydrophones) used in this beam
	 */
	private int channelMap;

	/**
	 * Sequence map containing the sequence number for this beam
	 */
	private int sequenceMap=0;

	/**
	 * A PamVector object describing the beam (calculated from of heading and slant angle).
	 */
	private PamVector beamVec;
	
	/**
	 * An array of PamVector objects, of size numElementsInBeam.  Each PamVector describes the location of the hydrophone element.
	 */
	private PamVector[] elementLocs;
	
	/**
	 * A list of the channels the correspond to the elementLocs array.  In other words, the channel held in index 0 must correspond to
	 * the hydrophone location vector in elementLocs[0].  
	 */
	private int[] channelList;
	
	/**
	 * An array of weights, of size numElementsInBeam.
	 */
	private double[] weights;

	/**
	 * A 2-index array with the frequency range to analyse.  Index 0 = min freq, index 1 = max freq
	 */
	private double[] freqBins;
	
	/**
	 * The index number in the FFT data unit that corresponds to the minimum frequency to analyse
	 */
	protected int startIdx;
	
	/**
	 * The index number in the FFT data unit that corresponds to the maximum frequency to analyse
	 */
	private int endIdx;

	/**
	 * An array of ComplexArray objects, of size numFreqBins. Each ComplexArray object holds a double array of complex numbers, with one complex
	 * number for each hydrophone element.
	 */
	protected ComplexArray[] steeringVecs;
	
	/**
	 * The speed of sound in the water
	 */
	private double speedOfSound;
	
	/**
	 * The order of channels in the incoming FFT data units array
	 */
	protected int[] chanOrder = null;

	/**
	 * Main Constructor.
	 * 
	 * @param beamformer The BasicFreqDomBeamFormer creating this beam
	 * @param channelMap a bitmap indicating which channels are part of this group
	 * @param sequenceNum the sequence number of this beam
	 * @param beamVec A PamVector describing this beam direction
	 * @param elementLocs an array of PamVectors giving the hydrophone element X,Y,Z locations, one PamVector for each element in this group
	 * @param channelList a list of channels in this beam.  Note that the order of the list MUST MATCH the order of the locations in elementLocs
	 * @param weights an array of double values, one for each element in this group.
	 * @param freqBins an array of frequencies to calculate the steering vector over
	 * @param speedOfSound the speed of sound in the water
	 */
	public BasicFreqDomBeam(BasicFreqDomBeamFormer beamformer, int channelMap, int sequenceNum, PamVector beamVec, PamVector[] elementLocs,
			int[] channelList, double[] weights, double[] freqBins, double speedOfSound) {
		this.beamformer = beamformer;
		this.channelMap = channelMap;
		this.sequenceMap = PamUtils.SetBit(0, sequenceNum, true);
		this.beamVec = beamVec;
		this.elementLocs = elementLocs;
		this.channelList = channelList;
		this.freqBins = freqBins;
		this.speedOfSound = speedOfSound;
		this.weights = weights;
		
		// create the steering vector
		calcSteeringVec();
	}

	
//	/**
//	 * Alternate Constructor using heading and slant angles instead of a PamVector
//	 * 
//	 * @param beamformer The BasicFreqDomBeamFormer creating this beam
//	 * @param heading degrees from heading
//	 * @param slant the slant angle, in degrees, where 0deg is horizontal and 90deg is up
//	 * @param elementLocs an array of PamVectors giving the hydrophone element X,Y,Z locations, one PamVector for each element in this group
//	 * @param weights an array of double values, one for each element in this group
//	 * @param freqBins an array of frequencies to calculate the steering vector over
//	 * @param speedOfSound the speed of sound in the water
//	 */
//	public BasicFreqDomBeam(BasicFreqDomBeamFormer beamformer, int channelMap, int sequenceMap, double heading, double slant, 
//			PamVector[] elementLocs, int[] channelList, double[] weights, double[] freqBins, double speedOfSound) {
//		this(beamformer, channelMap, sequenceMap, PamVector.fromHeadAndSlant(heading, slant), elementLocs, channelList, weights, freqBins, speedOfSound);
//	}
//

	/**
	 * Calculates a steering vector over all hydrophones and frequency bins.<p>
	 * A phase delay of the form e<sup>j&#969t</sup> is calculated for each frequency bin, where &#969=the angular
	 * frequency (=2*&#960*center-of-freq-bin) and t is the time delay based on the location of the hydrophone.<p>
	 * t is calculated as the location vector of the hydrophone / speed of sound (=locVec/c).<p>
	 * The wave number k is defined as &#969/c.  Substituting for t gives the phase delay in the form
	 * e<sup>j*k*locVec</sup>, which can also be expressed through Euler's formula as cos(k*locVec)+j*sin(k*locVec).
	 * The cos/sin values are saved to the ComplexArray steeringVecs[freqBinIdx].
	 * 
	 * @param elementNumber the hydrophone number
	 * @return a ComplexArray[] vector of length numFreqBins.  Each ComplexArray in the vector contains a double[] vector, which
	 * holds the complex steering value for each hydrophone at that frequency bin
	 */
	protected void calcSteeringVec() {
		/*
		 * Modified DG 15/8 to calc SV's for all frequencies. 
		 */
		
		// Calculate the number of frequency bins in the required freq range, based on the fft source params.  The center value
		// of any frequency bin is (i+0.5)*hzPerBin, where i=index number and hzPerBin = Fs / nfft
		double hzPerBin = beamformer.getBeamProcess().getFftDataSource().getSampleRate() / beamformer.getBeamProcess().getFftDataSource().getFftLength();
//		startIdx = (int) (freqBins[0]/hzPerBin-0.5);
//		endIdx = Math.min((int) (freqBins[1]/hzPerBin+0.5), beamformer.getBeamProcess().getFftDataSource().getFftLength()/2-1);
		startIdx = beamformer.getBeamProcess().frequencyToBin(freqBins[0]);
		endIdx = beamformer.getBeamProcess().frequencyToBin(freqBins[1]);

		int nFreq = beamformer.getBeamProcess().getFftDataSource().getFftLength()/2;
		// Initialise the return variable
		steeringVecs = new ComplexArray[nFreq];
		
		// loop through the data, first over the element locations and then over the frequency bins, calculating the phase delay for each
		// and storing in a double vector in real,imaginary pairs.
		for (int i=0; i < nFreq; i++) {
			
			// Initialise a new steeringArray vector here
			double[] steeringArray = new double[elementLocs.length*2];

			// loop through all frequency bins
			for (int j=0, realIdx=0, imgIdx=1; j<elementLocs.length; j++, realIdx+=2, imgIdx+=2) {

				// calculate the frequency for this iteration and the corresponding wavenumber vector
				double k = 2*Math.PI*((i+0.5)*hzPerBin)/speedOfSound;
				PamVector kVec = beamVec.times(k);

				// calculate the phase delay by getting the dot product of the location and wavenumber vectors
				if (elementLocs[j] == null) {
					continue;
				}
				double phaseDelay = kVec.dotProd(elementLocs[j]);
				
				// put the real and imaginary components into the double array after multiplying by the element weight
				steeringArray[realIdx] = weights[j]*Math.cos(phaseDelay);
				steeringArray[imgIdx] = weights[j]*Math.sin(phaseDelay);

			}		
			
			// create a ComplexArray object and add it to the steering vector
			steeringVecs[i]=new ComplexArray(steeringArray); 
		}		
		
		// if we are running this method it means something has changed in the beamformer parameters.  If that's the case, clear
		// the channel order list so that it gets recalculated next time the user starts processing data
		this.clearChannelOrderList();
	}
	
	/**
	 * Process a set of FFTDataUnit objects using the preset frequency bin range
	 * @param fftDataUnits the group of FFTDataUnits to analyse
	 * @return complex array of summed beamformed data. Note that the size of the array is the number of frequency bins in the full
	 * frequency range, as specified by the user in the parameters GUI
	 */
	public ComplexArray process(FFTDataUnit[] fftDataUnits) {
		return process(fftDataUnits, startIdx, endIdx);
	}
	
	/**
	 * Process a set of FFTDataUnit objects using a given frequency bin range. 
	 * @param fftDataUnits the group of FFTDataUnits to analyse
	 * @param startBin first bin to process
	 * @param endBin last bin to process (not inclusive, so endBin can equal fftLength/2)
	 * @return complex array of summed beamformed data. Note that the size of the array is the number of frequency bins in the full
	 * frequency range, as specified by the user in the parameters GUI, even if only a portion of that range was actually processed
	 */
	public ComplexArray process(FFTDataUnit[] fftDataUnits, int startBin, int endBin) {
		ComplexArray summedData = new ComplexArray(fftDataUnits[0].getFftData().length()); // make return variable same length as FFTDataUnit, to prevent errors in Spectrogram display
		
		// if we don't know the order of channels for the incoming FFT data units, determine that now
		if (chanOrder==null) {
			getChannelOrder(fftDataUnits);
		}

		// loop over the FFT bins
		for (int fftBin=startBin; fftBin<endBin; fftBin++) {
				
			// loop through the channels, multiplying the data by the corresponding steering vector.  The calc is
			// written as r_H * d, where r_H is the Hermitian (complex-conjugate transpose) replica vector
			// (aka the steering vector) and d is the data vector.  Add the real and imaginary components together,
			// to get a summed value for this fft bin
			double realVal = 0.;
			double imagVal = 0.;
			for (int chan=0; chan<fftDataUnits.length; chan++) {
				
				// this is the calculation written out for clarity.
//				double dataReal = fftDataUnits[chanOrder[chan]].getFftData().getReal(startIdx+fftBin);
//				double dataImag = fftDataUnits[chanOrder[chan]].getFftData().getImag(startIdx+fftBin);
//				double steeringReal = steeringVecs[fftBin].getReal(chan);
//				double steeringImag = steeringVecs[fftBin].getImag(chan);
//				realVal+=steeringReal*dataReal + steeringImag*dataImag;
//				imagVal+=steeringReal*dataImag - steeringImag*dataReal;
				
				// no reason to create all these double objects - just do it in one step
				realVal += steeringVecs[fftBin].getReal(chan) *
						fftDataUnits[chanOrder[chan]].getFftData().getReal(fftBin) +
						steeringVecs[fftBin].getImag(chan) *
						fftDataUnits[chanOrder[chan]].getFftData().getImag(fftBin);
				imagVal += steeringVecs[fftBin].getReal(chan) *
						fftDataUnits[chanOrder[chan]].getFftData().getImag(fftBin) -
						steeringVecs[fftBin].getImag(chan) *
						fftDataUnits[chanOrder[chan]].getFftData().getReal(fftBin);
			}
			
			// save the data for this fft bin
			summedData.set(fftBin, realVal, imagVal);
		}
		
		// return the summedData
		return summedData;
	}

	/**
	 * loop over the number of channels and determine the order of the hydrophones in the FFTDataUnits object.
	 * Create a look up table to match the order of hydrophones in the fftDataUnits array to the order
	 * in the steeringVecs array
	 * 
	 * @param fftDataUnits
	 */
	protected void getChannelOrder(FFTDataUnit[] fftDataUnits) {
		chanOrder = new int[channelList.length];
		for (int i = 0; i < fftDataUnits.length; i++) {
			int chanToMatch = PamUtils.getSingleChannel(fftDataUnits[i].getChannelBitmap());
			for (int j=0; j<channelList.length; j++) {
				if (channelList[j]==chanToMatch) {
					chanOrder[j]=i;
					break;
				}
			}
		}
	}


	/**
	 * @return the channelMap
	 */
	public int getChannelMap() {
		return channelMap;
	}


	/**
	 * @return the sequenceMap
	 */
	public int getSequenceMap() {
		return sequenceMap;
	}


	/**
	 * @param weights the weights to set
	 */
	public void setWeights(double[] weights) {
		this.weights = weights;
		calcSteeringVec();
	}

	/**
	 * clear the list of channel orders
	 */
	public void clearChannelOrderList() {
		chanOrder=null;
	}
	
	
	
	
}
