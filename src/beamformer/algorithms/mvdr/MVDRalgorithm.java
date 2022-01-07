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



package beamformer.algorithms.mvdr;

import org.apache.commons.math3.complex.Complex; // NOTE: references to Complex are for this class, not the fftManager.Complex class
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.linear.ArrayFieldVector;
import org.apache.commons.math3.linear.FieldDecompositionSolver;
import org.apache.commons.math3.linear.FieldLUDecomposition;
import org.apache.commons.math3.linear.FieldMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.SingularMatrixException;

import Acquisition.AcquisitionProcess;
import Array.ArrayManager;
import Array.PamArray;
import Localiser.algorithms.PeakPosition;
import Localiser.algorithms.PeakSearch;
import PamController.PamController;
import PamDetection.LocContents;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import PamView.dialog.warn.WarnOnce;
import beamformer.BeamAlgorithmParams;
import beamformer.BeamFormerBaseProcess;
import beamformer.algorithms.BeamFormerAlgorithm;
import beamformer.algorithms.BeamInformation;
import beamformer.continuous.BeamFormerDataBlock;
import beamformer.continuous.BeamFormerDataUnit;
import beamformer.continuous.BeamFormerProcess;
import beamformer.continuous.BeamOGramDataBlock;
import beamformer.continuous.BeamOGramDataUnit;
import beamformer.loc.BeamFormerLocalisation;
import beamformer.plot.BeamOGramLocalisation;
import fftManager.FFTDataUnit;
import pamMaths.PamVector;

/**
 * The algorithm class for the Minimum Variance Distortionless Response beamformer.  
 * 
 * @author mo55
 *
 */
public class MVDRalgorithm implements BeamFormerAlgorithm {

	/**
	 * The provider creating this algorithm
	 */
	private MVDRProvider mvdrProvider;
	
	/**
	 * Link back to the Beamformer process running the show
	 */
	private BeamFormerBaseProcess beamProcess;
	
	/**
	 * The parameters to use
	 */
	private MVDRParams mvdrParams;
	
	/**
	 * The sequence number to start with when creating beams
	 */
	private int firstBeamNum;
	
	/** 
	 * Output datablock, for all beams in all groups.
	 */
	private BeamFormerDataBlock beamformerOutput;

	/**
	 * The sequence number to use when creating a beamogram
	 */
	private int beamogramNum;

	/** 
	 * Output datablock, for all beams in all groups.
	 */
	private BeamOGramDataBlock beamOGramOutput;

	/**
	 * boolean indicating whether we are processing individual beams (true) or not (false)
	 */
	private boolean thisHasBeams=false;
	
	/**
	 * boolean indicating whether this is a BeamOGram (true) or not (false)
	 */
	private boolean thisHasABeamOGram=false;
	
	/**
	 * An array of PamVector objects describing the desired beam directions.  This is only used for individual beam
	 * analysis, not BeamOGrams.  It is initialised as empty, so that any calls to .length() will return 0 instead of a
	 * NullPointerException.
	 */
	private PamVector[] beamDirs = new PamVector[0];
	
	private BeamFormerLocalisation[] beamLocalisations;

	/**
	 * Object array containing beam information related to the beamogram (1 beam / look direction).  The first
	 * array is the secondary angle (aka slant, for a linear horizontal array), the angle relative to perpendicular
	 * to the primary array axis.  The second array is the main angle, in the direction of the primary array axis
	 */
	private MVDRBeam[][] beamogramBeams;

	/**
	 * A sequence map for the beamogram.  This is similar to a channelmap, but there is only one bit set.
	 */
	private int beamogramSeqMap;

	/**
	 * Object array containing individual beam information.
	 */
	private MVDRBeam[] individBeams;

	/**
	 * A sequence map for the beams.  This is similar to a channelmap, in that each bit is set for a certain beam.
	 */
	private int sequenceMap;

	/**
	 * The order of channels in the incoming FFT data units array
	 */
	protected int[] chanOrder = null;


	/**
	 * Keep frequency info in the beamOGram.
	 */
	private boolean keepFrequencyInfo = false;
	
	/**
	 * The inverse of the CSDM.  Each array index holds the CSDM for an fft bin.
	 */
	private FieldMatrix<Complex>[] Rinv;
	
	/**
	 * List of all channels in this group (derived from the channel map found in the parameters object)
	 */
	int[] channelList;

	/**
	 * The index in the fft data unit to start processing at.  Used only for the beamogram, and can be changed dynamically
	 * during processing
	 */
	private int beamOStartBin;

	/**
	 * The index in the fft data unit to end processing at.  Used only for the beamogram, and can be changed dynamically
	 * during processing
	 */
	private int beamOEndBin;

	/**
	 * Number of threads to process new data on
	 */
	int nBeamOThreads = 2;
	
	/**
	 * List of the beamformer threads
	 */
	private Thread[] beamThreads = new Thread[nBeamOThreads];
	
	private PeakSearch peakSearch = new PeakSearch(true);

	/**
	 * @param beamogramNum 
	 * @param firstSeqNum 
	 * @param mvdrParams 
	 * @param beamFormerProcess 
	 * @param mvdrProvider 
	 * 
	 */
	public MVDRalgorithm(MVDRProvider mvdrProvider, BeamFormerBaseProcess beamFormerProcess, MVDRParams mvdrParams, int firstSeqNum, int beamogramNum) {
		super();
		this.mvdrProvider = mvdrProvider;
		this.beamProcess = beamFormerProcess;
		this.mvdrParams = mvdrParams;
		this.firstBeamNum = firstSeqNum;
		this.beamogramNum = beamogramNum;
		this.beamformerOutput = beamFormerProcess.getBeamFormerOutput();
		this.beamOGramOutput = beamFormerProcess.getBeamOGramOutput();
		
		// create the vector of beam directions for individual beams
		if (mvdrParams.getNumBeams()>0) {
			int locContents = LocContents.HAS_BEARING | LocContents.HAS_AMBIGUITY | LocContents.HAS_BEARINGERROR;
			thisHasBeams = true;
			beamDirs = new PamVector[mvdrParams.getNumBeams()];
			int[] headings = mvdrParams.getHeadings();
			int[] slants = mvdrParams.getSlants();
			beamLocalisations = new BeamFormerLocalisation[mvdrParams.getNumBeams()];
			for (int i=0; i<mvdrParams.getNumBeams(); i++) {
				beamDirs[i]=PamVector.fromHeadAndSlant(headings[i], slants[i]);
				double beamErr = 180.;
				if (mvdrParams.getNumBeams() > 1) {
					if (i == 0 || i == mvdrParams.getNumBeams()-1) {
						beamErr = Math.abs(headings[1]-headings[0]);
					}
					else {
						beamErr = Math.abs(headings[i+1]-headings[i-1]) / 2.;
					}
				}
//				beamLocalisations[i] = new BeamFormerLocalisation(null, 
//						locContents, 
//						mvdrParams.getChannelMap(), 
//						Math.toRadians(headings[i]), 
//						Math.toRadians(beamErr));
			}
		}
		
		// get the parameters for the beamogram
		if (mvdrParams.getNumBeamogram()>0) {
			thisHasABeamOGram = true;
		}
	}

	/* (non-Javadoc)
	 * @see beamformer.algorithms.BeamFormerAlgorithm#prepare()
	 */
	@Override
	public void prepare() {
		
		// get a list of all channels in this channel map, and the corresponding element locations
		channelList = PamUtils.getChannelArray(mvdrParams.getChannelMap());
		AcquisitionProcess sourceProcess = null;
		try {
//			sourceProcess = (AcquisitionProcess) beamProcess.getSourceProcess();
			sourceProcess = (AcquisitionProcess) beamProcess.getFftDataSource().getSourceProcess();
		}
		catch (ClassCastException e) {
			String title = "Error finding Acquisition module";
			String msg = "There was an error trying to find the Acquisition module.  " +
					"The beamformer needs this information in order to run.  There will be no output until " +
					"a valid Acquisition module is added and the Pamguard run is restarted.";
			String help = null;
			int ans = WarnOnce.showWarning(PamController.getInstance().getGuiFrameManager().getFrame(0), title, msg, WarnOnce.WARNING_MESSAGE, help, e);
			sourceProcess=null;
			e.printStackTrace();
			return;
		}
		if (sourceProcess==null) {
			String title = "Error finding Acquisition module";
			String msg = "There was an error trying to find the Acquisition module.  " +
					"The beamformer needs this information in order to run.  There will be no output until " +
					"a valid Acquisition module is added and the Pamguard run is restarted.";
			String help = null;
			int ans = WarnOnce.showWarning(PamController.getInstance().getGuiFrameManager().getFrame(0), title, msg, WarnOnce.WARNING_MESSAGE, help, null);
			return;
		}
		
		ArrayManager arrayManager = ArrayManager.getArrayManager();
		PamArray currentArray = arrayManager.getCurrentArray();
		PamVector[] elementLocs = new PamVector[channelList.length];
		long now = PamCalendar.getTimeInMillis();
		int hydrophoneMap = 0;
		for (int i=0; i<channelList.length; i++) {
			int hydrophone = sourceProcess.getAcquisitionControl().getChannelHydrophone(channelList[i]);
			elementLocs[i] = currentArray.getAbsHydrophoneVector(hydrophone, now);
			hydrophoneMap |= 1<<hydrophone;
		}
		/*
		 * Do some normalisation of those vectors. Start by making everything relative
		 * to the average position. 
		 */
		PamVector arrayCenter = PamVector.mean(elementLocs);
		for (int i = 0; i < channelList.length; i++) {
			elementLocs[i] = elementLocs[i].sub(arrayCenter);
		}
		/*
		 * Now get the principle axis vector of the array. 
		 */
		int arrayShape = arrayManager.getArrayType(hydrophoneMap); // 2 for  aline array. 
		PamVector[] arrayAxis = arrayManager.getArrayDirection(hydrophoneMap); // will be single vector for a line array. 
		
		// Create the beams for the beamogram.  Step through the look angles (based on the beamOGramAngles variable) and create a vector for each.  Use
		// the same sequence number, since when processing all beams will be added together anyway.  For the element weights and frequency range, use
		// the last index in the weights and freqRange variables (the last index = number of individual beams)
		int[] boAngles = mvdrParams.getBeamOGramAngles();
		if (thisHasABeamOGram && boAngles != null && boAngles.length >= 2) {
			int nMainBeams = (int) ((mvdrParams.getBeamOGramAngles()[1]-mvdrParams.getBeamOGramAngles()[0])/mvdrParams.getBeamOGramAngles()[2]+1);
			int nSecBeams = (int) ((mvdrParams.getBeamOGramSlants()[1]-mvdrParams.getBeamOGramSlants()[0])/mvdrParams.getBeamOGramSlants()[2]+1);
			beamogramBeams = new MVDRBeam[nSecBeams][nMainBeams];
			beamogramSeqMap = PamUtils.SetBit(0, beamogramNum, true);
//			int arrayIdx = 0;
//			if (beamDirs != null) {
//				arrayIdx = beamDirs.length;
//			}
			PamVector beamDir = new PamVector();
			for (int j=0; j<nSecBeams; j++) {
				for (int i=0; i<nMainBeams; i++) {
					beamDir=PamVector.fromHeadAndSlant(mvdrParams.getBeamOGramAngles()[0]+i*mvdrParams.getBeamOGramAngles()[2],
							mvdrParams.getBeamOGramSlants()[0]+j*mvdrParams.getBeamOGramSlants()[2]);
					beamogramBeams[j][i] = new MVDRBeam(this, 
							mvdrParams.getChannelMap(), 
							beamogramNum, 
							beamDir, 
							elementLocs, 
							mvdrParams.getBeamOGramFreqRange(), 
							currentArray.getSpeedOfSound());
				}
			}
			double[] freqBins = mvdrParams.getBeamOGramFreqRange();
			beamOStartBin = beamProcess.frequencyToBin(freqBins[0]);
			beamOEndBin = beamProcess.frequencyToBin(freqBins[1]);
			
		// Create the individual beams.  Loop through the look vectors created in the constructor.  Give each beam a unique seequence number
		} 
		if (thisHasBeams) {
			individBeams = new MVDRBeam[beamDirs.length];
			for (int i=0; i<beamDirs.length; i++) {
				sequenceMap = PamUtils.SetBit(0, firstBeamNum+i, true);
				individBeams[i] = new MVDRBeam(this, 
						mvdrParams.getChannelMap(), 
						firstBeamNum+i, 
						beamDirs[i], 
						elementLocs, 
						mvdrParams.getFreqRange()[i], 
						currentArray.getSpeedOfSound());
			}
		}
		
		// reset the channel order array
		this.clearChannelOrderList();
	}

	/**
	 * Process an array of FFTDataUnits.  The size of the array will equal the number of channels in this beam group
	 */
	@Override
	public void process(FFTDataUnit[] fftDataUnits) {
		
		// if this is a BeamOGram, we need to loop through all of the beams, averaging the magnitudes of the returned complex numbers and then
		// saving that to a new index in a ComplexArray.  This ComplexArray will then be used to create a new BeamFormerDataUnit.  Note that we
		// store the values into the ComplexArray in reverse order (from last bin to first), so that on the spectrogram display the value related
		// to heading=0deg (straight ahead) is at the top of the display, and the value related to heading=180deg (behind the boat) is at the
		// bottom.
		if (thisHasABeamOGram && beamogramBeams != null) {
			
			// calculate the inverse CSDM for the data over the currently-specified frequency bin range
//			this.prepNewData(fftDataUnits, beamogramBeams[0][0].getStartIdx(), beamogramBeams[0][0].getNumFFTBins()); this one calculates CSDM for entire range
			this.prepNewData(fftDataUnits, beamOStartBin, beamOEndBin-beamOStartBin);
			
			// loop through the angles one beam at a time, processing the data and averaging the results
			int numAnlgeBins = beamogramBeams[0].length;//beamProcess.getFftDataSource().getFftLength()/2;
			int numSlantBins = beamogramBeams.length;
			int nAllFBins = fftDataUnits[0].getFftData().length();
			int nFBinsToKeep = keepFrequencyInfo ? nAllFBins : 1;
			double[][][] beamData = new double[nFBinsToKeep][numSlantBins][numAnlgeBins];
			/*
			 * Keep the order simple here - for the data, not the displays ! So bin 0 in the output 
			 * is bin 0 in angle. Don't reverse it.  DG. 27.07.17
			 * 
			 * Multithreading. Make multiple threads for this next part, looping 
			 * over the second (final) dimension, which is generaly the one having the 
			 * most separate beams. 
			 */
			if (nBeamOThreads > 1) {
				for (int i = 0; i < nBeamOThreads; i++) {
					beamThreads[i] = new Thread(new BeamOThread(fftDataUnits, beamData, i, nBeamOThreads, nAllFBins));
					beamThreads[i].start();
				}
				try {
					for (int i = 0; i < nBeamOThreads; i++) {
						beamThreads[i].join();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				
			}
			else {
				for (int j=0; j<beamogramBeams.length; j++) {
					for (int i=0; i<beamogramBeams[0].length; i++) {
						//					ComplexArray summedData = beamogramBeams[j][i].process(Rinv);
						ComplexArray summedData = beamogramBeams[j][i].process(Rinv, beamOStartBin, beamOEndBin-beamOStartBin,nAllFBins);
						/*
						 * Do a search for NaN values here ...
						 */
						double[] d = summedData.getData();
//						int nNan = 0;
//						for (int r = 1; r < d.length; r++) {
//							if (Double.isNaN(d[r])) {
//								nNan ++;
//							}
//						}
//						if (nNan > 0) {
//							summedData = beamogramBeams[j][i].process(Rinv, beamOStartBin, beamOEndBin-beamOStartBin,nAllFBins);
//						}
						//				double summedMag = 0.;
						//				for (int k=0; k<summedData.length(); k++) {
						//					summedMag += summedData.magsq(k);
						//				}
						//					for (int k=0; k<summedData.length(); k++) {
						for (int k=beamOStartBin; k<beamOEndBin; k++) {
							if (keepFrequencyInfo) {
								beamData[k][j][i] = summedData.magsq(k);
							}
							else {
								beamData[0][j][i] += summedData.magsq(k);
							}
						}
					}
				}
			}
			
			// create a new data unit
			BeamOGramDataUnit newUnit = new BeamOGramDataUnit(fftDataUnits[0].getTimeMilliseconds(),
					mvdrParams.getChannelMap(),
					beamogramSeqMap, 
					fftDataUnits[0].getStartSample(),
					fftDataUnits[0].getSampleDuration(),
					beamData, 
					fftDataUnits[0].getFftSlice());
			
			// now get the best angle and add it as a localisation. 
			double meanLev = 0;
			double[] aveData = newUnit.getAngle1Data(true);
			for (int i = 0; i < aveData.length; i++) {
				meanLev += aveData[i];
			}
			meanLev /= aveData.length;
			PeakPosition peakPos = peakSearch.interpolatedPeakSearch(aveData);
			if (peakPos.getHeight()/meanLev > 2) {
				int[] boga = mvdrParams.getBeamOGramAngles();
				double bestAng = peakPos.getBin() * (double) boga[2] + (double) boga[0];
				newUnit.setLocalisation(new BeamOGramLocalisation(newUnit, mvdrParams.getChannelMap(), bestAng));
			}
			
			beamOGramOutput.addPamData(newUnit);
		}
		
		// if these are individual beams, loop through them one at a time and create a new BeamFormerDataUnit for each
		if (thisHasBeams) {
			int iBeam = 0;
			
			// set the counters and calculate the inverse CSDM for the first beam
			int lastStartIdx = individBeams[0].getStartIdx();
			int lastNumFFTBins = individBeams[0].getNumFFTBins();
			this.prepNewData(fftDataUnits, individBeams[0].getStartIdx(), individBeams[0].getNumFFTBins());
			
			// loop through the beams one at a time, saving each result to the beamformer output data block
			for (MVDRBeam beam : individBeams) {
				
				// if this beam has a different starting index or number of fft bins as the previous, recalculate the inverse CSDM
				if (beam.getStartIdx()!=lastStartIdx || beam.getNumFFTBins()!=lastNumFFTBins) {
					this.prepNewData(fftDataUnits, beam.getStartIdx(), beam.getNumFFTBins());
					lastStartIdx = beam.getStartIdx();
					lastNumFFTBins = beam.getNumFFTBins();
				}
				ComplexArray summedData = beam.process(Rinv);
				BeamFormerDataUnit newUnit = new BeamFormerDataUnit(fftDataUnits[0].getTimeMilliseconds(),
						mvdrParams.getChannelMap(),
						beam.getSequenceMap(), 
						fftDataUnits[0].getStartSample(),
						fftDataUnits[0].getSampleDuration(),
						summedData, 
						fftDataUnits[0].getFftSlice());
//				System.out.println("Beam " + String.valueOf(beam.getSequenceMap()) + " at 150Hz = " +String.valueOf(summedData.getReal(19)) + "+" + String.valueOf(summedData.getImag(19))+"i");
				newUnit.setLocalisation(beamLocalisations[iBeam]);
				beamformerOutput.addPamData(newUnit);
				iBeam++;
			}
		}
	}

	/**
	 * Multithreading inner class to process new data
	 * @author mo55
	 *
	 */
	private class BeamOThread implements Runnable {

		private FFTDataUnit[] fftDataUnits;
		private double[][][] beamData;
		private int firstBeamIndex;
		private int beamIndexStep;
		private int nFBins;

		public BeamOThread(FFTDataUnit[] fftDataUnits, double[][][] beamData, int firstBeamIndex, int beamIndexStep, int nFBins) {
			super();
			this.fftDataUnits = fftDataUnits;
			this.beamData = beamData;
			this.firstBeamIndex = firstBeamIndex;
			this.beamIndexStep = beamIndexStep;
			this.nFBins = nFBins;
		}

		@Override
		public void run() {
			for (int j=0; j<beamogramBeams.length; j++) {
				for (int i=firstBeamIndex; i<beamogramBeams[0].length; i+=beamIndexStep) {
					ComplexArray summedData = beamogramBeams[j][i].process(Rinv, beamOStartBin, beamOEndBin-beamOStartBin,nFBins);
					for (int k=beamOStartBin; k<beamOEndBin; k++) {
						if (keepFrequencyInfo) {
							beamData[k][j][i] = summedData.magsq(k);
						}
						else {
							beamData[0][j][i] += summedData.magsq(k);
						}
					}
					//				aveData.set(j, summedMag/summedData.length(), 0.);
					//				aveData[j] = summedMag/summedData.length();
				}
			}
			
		}
		
	}

	/**
	 * Calculate the inverse CSDM (Rinv field) based on the current fftDataUnits.
	 * 
	 * @param fftDataUnits the new data to process
	 * @param startIdx the fft bin to start the processing at
	 * @param numFFTBins the number of FFT bins to process
	 */
	private void prepNewData(FFTDataUnit[] fftDataUnits, int startIdx, int numFFTBins) {
		Rinv = (FieldMatrix<Complex>[]) new FieldMatrix[numFFTBins]; // cannot create an array with generics, so need to do this awkward cast instead
		
		// if we don't know the order of channels for the incoming FFT data units, determine that now
		if (chanOrder==null) {
			getChannelOrder(fftDataUnits);
		}

		// loop over the FFT bins calculating an inverse CSDM for each one
		for (int fftBin=0; fftBin<numFFTBins; fftBin++) {
				
			// compile the FFT values for each hydrophone in this FFT bin into both a regular vector as well as a
			// conjugate vector, so that later we can calculate the CSDM (outer product) easily.  Make sure to compile them
			// in the order that matches the steering vector order.
			// Note: the Complex objects used here are from the Apache Commons Math library org.apache.commons.math3.complex.Complex,
			// not the fftManager.Complex class
			ArrayFieldVector<Complex> dataVec = new ArrayFieldVector<Complex>(ComplexField.getInstance(), fftDataUnits.length);
			ArrayFieldVector<Complex> dataVecConj = new ArrayFieldVector<Complex>(ComplexField.getInstance(), fftDataUnits.length);
			for (int chan=0; chan<fftDataUnits.length; chan++) {
				dataVec.setEntry(chan, new Complex(fftDataUnits[chanOrder[chan]].getFftData().get(startIdx+fftBin).real, fftDataUnits[chanOrder[chan]].getFftData().get(startIdx+fftBin).imag));
				dataVecConj.setEntry(chan, new Complex(fftDataUnits[chanOrder[chan]].getFftData().get(startIdx+fftBin).real, -1*fftDataUnits[chanOrder[chan]].getFftData().get(startIdx+fftBin).imag));
			}
			
			// calculate the CSDM of the FFT data
			FieldMatrix<Complex> R = dataVec.outerProduct(dataVecConj);
			
			// add diagonal loading to the matrix.  First calculate the trace value (the sum of the diagonal components)
			// and divide that by the square of the number of channels.  Add that to the diagonal elements in the matrix R
			Complex traceVal = R.getTrace();
			Complex noiseVal = traceVal.divide(Math.pow(fftDataUnits.length,2));
			for (int chan=0; chan<fftDataUnits.length; chan++) {
				R.addToEntry(chan, chan, noiseVal);
			}
			
			// divide by the number of channels.  This was done in the Matlab program.  Not really necessary, but it helps to bring
			// the data into a scale closer to the original FFT data.
//			R.scalarMultiply(new Complex(1./fftDataUnits.length));
			
			
			// calculate the inverse matrix for this fft bin
			FieldDecompositionSolver<Complex> solver = new FieldLUDecomposition<Complex>(R).getSolver();
			FieldMatrix<Complex> singleRinv = MatrixUtils.createFieldMatrix(ComplexField.getInstance(), R.getRowDimension(), R.getColumnDimension());
			try {
				singleRinv = solver.getInverse();

			} catch (SingularMatrixException ex) {
			}
			
			// save the inverse CSDM to the field array
			Rinv[fftBin] = singleRinv;
		}
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
	 * clear the list of channel orders
	 */
	public void clearChannelOrderList() {
		chanOrder=null;
	}

	/* (non-Javadoc)
	 * @see beamformer.algorithms.BeamFormerAlgorithm#getNumBeams()
	 */
	@Override
	public int getNumBeams() {
		return beamDirs.length;
	}

	/* (non-Javadoc)
	 * @see beamformer.algorithms.BeamFormerAlgorithm#getNumBeamogramAngles()
	 */
	@Override
	public int getNumBeamogramAngles() {
		int[] boAngles = mvdrParams.getBeamOGramAngles();
		if (boAngles == null) return 1;
		int numAngles = (int) ((mvdrParams.getBeamOGramAngles()[1]-mvdrParams.getBeamOGramAngles()[0])/mvdrParams.getBeamOGramAngles()[2]+1);
		return numAngles;
	}

	/* (non-Javadoc)
	 * @see beamformer.algorithms.BeamFormerAlgorithm#getBeamInformation(int)
	 */
	@Override
	public BeamInformation getBeamInformation(int iBeam) {
		return null;
	}

	/* (non-Javadoc)
	 * @see beamformer.algorithms.BeamFormerAlgorithm#thereIsABeamogram()
	 */
	@Override
	public boolean thereIsABeamogram() {
		return (thisHasABeamOGram);
	}
	
	/**
	 * @return the beamProcess
	 */
	public BeamFormerBaseProcess getBeamProcess() {
		return beamProcess;
	}

	@Override
	public void setKeepFrequencyInformation(boolean keep) {
		keepFrequencyInfo  = true;
	}

	@Override
	public void setFrequencyBinRange(int binFrom, int binTo) {
		beamOStartBin = binFrom;
		beamOEndBin = binTo;		
	}

}
