/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
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
package whistleDetector;

import java.util.ArrayList;

import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import PamView.symbol.StandardSymbolManager;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;

/**
 * @author Doug Gillespie
 * 
 * Detects whistle peaks from FFT data
 * 
 */
abstract public class PeakDetector extends PamProcess {

	double[] spectrumAverage;

	boolean[] overThreshold;

	protected double[] nrData;
	
//	double[] 

	double[] freqGrad;

	protected int searchBin0;
	
	protected int searchBin1; 
	
	WhistleDetector whistleDetector;

	FFTDataBlock fftDataSource;

	PamDataBlock<PeakDataUnit> peakDataBlock;
	
	WhistleControl whistleControl;
	
	/**
	 * a bitmap of the channels (or sequences) in this group
	 */
	private int groupChannels;
	
	/**
	 * the lowest channel/sequence in the groupChannels object
	 */
	private int detectionChannel;

	int slicesAnalysed;

	// constatns for background updating
	protected double detectionThreshold;

	protected double bgndUpdate0;

	protected double bgndUpdate0_1;

	protected double bgndUpdate1;

	protected double bgndUpdate1_1;

	protected final static int WARMUP_SLICES = 100;

	enum PeakStatus {
		PEAK_ON, PEAK_OFF, PEAK_ENDING
	}

	public PeakDetector(WhistleControl whistleControl,
			WhistleDetector whistleDetector, FFTDataBlock fftDataSource, int groupChannels) {

		super(whistleControl, null);

		this.whistleControl = whistleControl;
		this.fftDataSource = fftDataSource;
		this.whistleDetector = whistleDetector;
		setGroupChannels(groupChannels);

		peakDataBlock = new PamDataBlock<PeakDataUnit>(
				PeakDataUnit.class, "Spectral Peak Blocks Channel " + detectionChannel,
				whistleDetector, groupChannels);

		peakDataBlock.setOverlayDraw(new PeakGraphics(whistleControl, whistleDetector));
		peakDataBlock.setPamSymbolManager(new StandardSymbolManager(peakDataBlock, PeakGraphics.defaultSymbol, true));
		whistleDetector.addOutputDataBlock(peakDataBlock);
	}

	@Override
	public void prepareProcess() {

		super.prepareProcess();
		
		if (whistleDetector == null) {
			return;
		}

		spectrumAverage = new double[whistleDetector.fftLength / 2]; // only
																		// need
																		// half
																		// of
																		// the
																		// fft !
		overThreshold = new boolean[whistleDetector.fftLength / 2]; // I hope
																	// this
																	// initialises
																	// to false
		nrData = new double[whistleDetector.fftLength / 2];
		freqGrad = new double[whistleDetector.fftLength / 2];

		slicesAnalysed = 0;

		detectionThreshold = Math.pow(10.,
				whistleControl.whistleParameters.detectionThreshold / 10.);
		bgndUpdate0 = whistleDetector.fftHop / getSampleRate()
				/ whistleControl.whistleParameters.peakTimeConstant[0];
		bgndUpdate0_1 = 1. - bgndUpdate0;
		bgndUpdate1 = whistleDetector.fftHop / getSampleRate()
				/ whistleControl.whistleParameters.peakTimeConstant[1];
		
		bgndUpdate1_1 = 1. - bgndUpdate1;
		
		searchBin0 = whistleDetector.hzToBins(whistleControl.whistleParameters.getSearchStartHz());
		searchBin1 = whistleDetector.hzToBins(whistleControl.whistleParameters.getSearchEndHz(getSampleRate()));
	}

	@Override
	public void setSampleRate(float sampleRate, boolean notify) {
		super.setSampleRate(sampleRate, notify);
		prepareProcess();
	}

	/*
	 * Methods from PamProcess and PamObserver which must be implemented
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see PamModel.PamObserver#update(PamModel.PamObservable,
	 *      java.lang.Object)
	 */
	@Override
	public void newData(PamObservable obs, PamDataUnit newUnit) {
		// Should get called each time there is a new DataUnit from an FFT
		// Process
		FFTDataUnit fftDataUnit = (FFTDataUnit) newUnit;
//		if ((fftDataUnit.getChannelBitmap() & (1 << detectionChannel)) == 0) {
		if ((fftDataUnit.getSequenceBitmap() & (1 << detectionChannel)) == 0) {
			return;
		}
		searchForPeaks(obs, fftDataUnit);
	}

	/**
	 * @param obs
	 *            PamObservable - always a PamDataBlock
	 * @param newDataUnit
	 *            The latest PamDataUnit added to the PamDataBlock
	 * 
	 * Takes a new block of FFT data and creates a series of peaks based on
	 * parts of the sectrum being above some threshold. Rather than add
	 * individual peaks to the output data block, the peaks are stored in a new
	 * ArrayList and the array list is added to the output block.
	 * 
	 * This reduces the amount of traffic to the output blocks listeners and
	 * will make it easier for the process linking the peaks to navigate between
	 * successive peak slices.
	 */
	public void searchForPeaks(PamObservable obs, FFTDataUnit newDataUnit) {

		slicesAnalysed++;

		ComplexArray newFftData = newDataUnit.getFftData();
		Enum peakOn = PeakStatus.PEAK_OFF;
		double magsq;
		double newval;
		int nOver = 0;
		WhistlePeak newPeak = new WhistlePeak(newDataUnit.getStartSample(),
				newDataUnit.getFftSlice(), newDataUnit.getTimeMilliseconds());
		ArrayList<WhistlePeak> peakList = new ArrayList<WhistlePeak>();
		/*
		 * May as well initialise these here everytime
		 */

		if (slicesAnalysed <= WARMUP_SLICES) {
			// just average the slice data into the background measure to get
			// started
			for (int i = 0; i < whistleDetector.fftLength / 2; i++) {
				spectrumAverage[i] += (newFftData.magsq(i) / WARMUP_SLICES);
			}
			return;
		}
		searchBin0 = Math.max(searchBin0, 1);
		searchBin1 = Math.min(searchBin1, whistleDetector.fftLength/2 - 2);
		// otherwise, update the background according to whether each bin is
		// over threshold or not.
		// and at the same time see if it's over threshold
		for (int i = 0; i < whistleDetector.fftLength / 2; i++) {
			magsq = newFftData.magsq(i);
			newval = magsq / spectrumAverage[i];
			if ((overThreshold[i] = newval > detectionThreshold)) {
				spectrumAverage[i] *= bgndUpdate1_1;
				spectrumAverage[i] += magsq * bgndUpdate1;
				nOver++;
			} else {
				spectrumAverage[i] *= bgndUpdate0_1;
				spectrumAverage[i] += magsq * bgndUpdate0;
			}
//			nrData[i] = magsq - spectrumAverage[i]; to 20/10/08
			nrData[i] = newval;
		}
		

		int downCount = 0;
		for (int iFreq = 1; iFreq < whistleDetector.fftLength / 2 - 1; iFreq++) {
			freqGrad[iFreq] = nrData[iFreq + 1] / nrData[iFreq - 1];
			// pOTh[iFreq] = freqGrad[iFreq] > detectionThreshold;
		}

		/*
		 * now look for sections which rise and fall Initially put the
		 * information in a temporary structure and only add it to the main
		 * peaks list if if passes certain criteria later on
		 */

		for (int iFreq = searchBin0; iFreq <= searchBin1; iFreq++) {
//		for (int iFreq = Math.max(1, searchBin0); iFreq < whistleDetector.fftLength / 2 - 1; iFreq++) {
			if (peakOn == PeakStatus.PEAK_OFF) // look for a start
			{
				if (freqGrad[iFreq] > 1. // change from > 0 20/10/08
						&& freqGrad[iFreq] > freqGrad[iFreq - 1]
						&& freqGrad[iFreq] > freqGrad[iFreq + 1]) // peak
																	// start
				{
					// System.out.println("New peak start " + iFreq);
					newPeak.MinFreq = newPeak.MaxFreq = newPeak.PeakFreq = iFreq;
					newPeak.Signal = newPeak.MaxAmp = spectrumAverage[iFreq];
					newPeak.Noise = spectrumAverage[iFreq];
					newPeak.whistleShape = null;
					newPeak.VetoPeak = false;
					newPeak.timeOffset = 0;
					newPeak.Ok = overThreshold[iFreq];
					peakOn = PeakStatus.PEAK_ON;
					downCount = 0;
				}
			} else if (peakOn == PeakStatus.PEAK_ON) {
				/*
				 * end the peak when the amplitude is below the peak amplitude
				 * AND either - it's below threshold or - the gradient is
				 * positive or - the rate of change of gradient is positive and
				 * above some threshold
				 */
				// if (++downCount > 0 && pData[iFreq] < newPeak.MaxAmp &&
				// (!pOTh[iFreq] || freqGrad[iFreq+1] > 1 ||
				// (freqGrad[iFreq+1]>freqGrad[iFreq] &&
				// freqGrad[iFreq]>1./freqGradThresh)))
				if ((freqGrad[iFreq] < 1. && 
						freqGrad[iFreq] < freqGrad[iFreq - 1] && 
						freqGrad[iFreq] < freqGrad[iFreq + 1])) // peak
																											// end
				{
					newPeak.MaxFreq = iFreq;
					peakOn = PeakStatus.PEAK_ENDING;
				} else // peak continuation
				{
					if ((newval = newFftData.magsq(iFreq)) > newPeak.MaxAmp) {
						newPeak.MaxAmp = newval;
						newPeak.PeakFreq = iFreq;
					}
					newPeak.Signal += newval;
					newPeak.Noise += spectrumAverage[iFreq];
					newPeak.Ok |= overThreshold[iFreq];
					// if (pOTh[iFreq] & freqGrad[iFreq] > freqGrad[iFreq-1] &&
					// freqGrad[iFreq] > .75)
					// downCount = 0;
				}
			}
			if (peakOn == PeakStatus.PEAK_ENDING) {
				if (!newPeak.Ok) {
					peakOn = PeakStatus.PEAK_OFF;
					continue;
				}
				// see if the peak start criteria have been met again
				// if (freqGrad[iFreq-1] > 1. && freqGrad[iFreq+1] > 1. &&
				// freqGrad[iFreq] >= freqGrad[iFreq-1] && freqGrad[iFreq] >
				// freqGrad[iFreq+1]) // peak start
				// if (freqGrad[iFreq] > 1. && overThreshold[iFreq]) // peak
				// start
				// {
				// downCount = 0;
				// peakOn = PeakStatus.PEAK_ON;
				// }
				else {
					// search out from the maximum in each direction and take
					// the half heigth width.
					if (++downCount > 0) {
						peakOn = PeakStatus.PEAK_OFF;
						// if it reached above threshold and some local average
						// ...
						if (true) {
							// System.out.println("Min " + newPeak.MinFreq + ";
							// Max " + newPeak.MaxFreq);
							if (newPeak.Ok)
//									&& newPeak.MaxAmp
//											/ ((newFftData[newPeak.MinFreq - 1]
//													.magsq() + newFftData[newPeak.MaxFreq + 1]
//													.magsq()) / 2.) > 1)

							{
								peakList.add(newPeak);
								// then immediately generate a new Peak so data
								// in the old one is not overridden.
								newPeak = new WhistlePeak(
										newDataUnit.getStartSample(), newDataUnit.getFftSlice(), 
										newDataUnit.getTimeMilliseconds());
							}
						}
					} else
						downCount++;
				}
			}
		}
		// always create an output unit - even if the array list is empty.
		if (whistleDetector.fftLength / 2 < nOver * 4)
			peakList.clear();

//		PamDataUnit newPeakUnit = getOutputDataBlock(0).getNewUnit(newDataUnit.getStartSample(),
//				newDataUnit.getDuration(), 1 << whistleControl.whistleParameters.detectionChannel);
		PeakDataUnit peakDataUnit = new PeakDataUnit(absSamplesToMilliseconds(newDataUnit.getStartSample()),
				1 << detectionChannel, newDataUnit.getStartSample(), 
				whistleDetector.fftLength, peakList, newDataUnit.getFftSlice());
		peakDataUnit.sortOutputMaps(newDataUnit.getChannelBitmap(), newDataUnit.getSequenceBitmapObject(), 1 << detectionChannel);

		peakDataBlock.addPamData(peakDataUnit);
	}

//	public long FirstRequiredTime(PamObservable obs, Object obj) {
//		return -1;
//	}
	
	public double[] getEqualisationConstants() {
		return spectrumAverage;
	}

	@Override
	public void pamStart() {

	}

	@Override
	public void pamStop() {

	}

	public PamDataBlock<PeakDataUnit> getPeakDataBlock() {
		return peakDataBlock;
	}

	public int getGroupChannels() {
		return groupChannels;
	}

	public void setGroupChannels(int groupChannels) {
		this.groupChannels = groupChannels;
		detectionChannel = PamUtils.getLowestChannel(groupChannels);
		if (peakDataBlock != null) {
//			peakDataBlock.setChannelMap(groupChannels);
			peakDataBlock.sortOutputMaps(fftDataSource.getChannelMap(), fftDataSource.getSequenceMapObject(), groupChannels);
		}
	}

	public int getDetectionChannel() {
		return detectionChannel;
	}
	
	abstract public String getPeakDetectorName();
}
