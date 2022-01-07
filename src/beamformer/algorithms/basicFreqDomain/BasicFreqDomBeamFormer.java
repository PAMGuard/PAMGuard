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

import Acquisition.AcquisitionProcess;
import Array.ArrayManager;
import Array.PamArray;
import Localiser.algorithms.PeakPosition;
import Localiser.algorithms.PeakSearch;
import PamController.PamController;import PamDetection.LocContents;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import PamView.dialog.warn.WarnOnce;
import beamformer.BeamFormerBaseProcess;
import beamformer.algorithms.BeamFormerAlgorithm;
import beamformer.algorithms.BeamInformation;
import beamformer.continuous.BeamFormerDataBlock;
import beamformer.continuous.BeamFormerDataUnit;
import beamformer.continuous.BeamOGramDataBlock;
import beamformer.continuous.BeamOGramDataUnit;
import beamformer.loc.BeamFormerLocalisation;
import beamformer.plot.BeamOGramLocalisation;
import fftManager.FFTDataUnit;
import pamMaths.PamVector;

/**
 * A basic frequency-domain beamformer
 * 
 * @author mo55
 *
 */
public class BasicFreqDomBeamFormer implements BeamFormerAlgorithm {

	/**
	 * Link back to the Beamformer process running the show
	 */
	private BeamFormerBaseProcess beamProcess;

	/**
	 * The provider creating this algorithm
	 */
	private BasicFreqDomBeamProvider basicFreqDomBeamProvider;

	/**
	 * The parameters to use
	 */
	BasicFreqDomParams basicFreqDomParams;

	/** 
	 * Output datablock, for all beams in all groups.
	 */
	private BeamFormerDataBlock beamformerOutput;
	
	/**
	 * Output datablock for beamogram
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
	
	/**
	 * Object array containing individual beam information.
	 */
	private BasicFreqDomBeam[] individBeams;
	
	/**
	 * Object array containing beam information related to the beamogram (1 beam / look direction).  The first
	 * array is the secondary angle (aka slant, for a linear horizontal array), the angle relative to perpendicular
	 * to the primary array axis.  The second array is the main angle, in the direction of the primary array axis
	 */
	private BasicFreqDomBeam[][] beamogramBeams;

	/**
	 * The sequence number to start with when creating beams
	 */
	private int firstBeamNum;
	
	/**
	 * The sequence number to use when creating a beamogram
	 */
	private int beamogramNum;
	
	/**
	 * A sequence map for the beams.  This is similar to a channelmap, in that each bit is set for a certain beam.
	 */
	private int sequenceMap;

	/**
	 * A sequence map for the beamogram.  This is similar to a channelmap, but there is only one bit set.
	 */
	private int beamogramSeqMap;
	
	private BeamFormerLocalisation[] beamLocalisations;

	private boolean keepFrequencyInfo = false;

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
	 * @param basicFreqDomBeamProvider
	 * @param beamFormerProcess
	 * @param channelMap
	 * @param firstBeamNum
	 * @param basicFreqDomParams
	 */
	public BasicFreqDomBeamFormer(BasicFreqDomBeamProvider basicFreqDomBeamProvider,
			BeamFormerBaseProcess beamFormerProcess, 
			BasicFreqDomParams basicFreqDomParams, 
			int firstBeamNum, 
			int beamogramNum
			) {
		super();
		this.basicFreqDomBeamProvider = basicFreqDomBeamProvider;
		this.beamProcess = beamFormerProcess;
		this.basicFreqDomParams = basicFreqDomParams;
		this.firstBeamNum = firstBeamNum;
		this.beamogramNum = beamogramNum;
		beamformerOutput = beamFormerProcess.getBeamFormerOutput();
		beamOGramOutput = beamFormerProcess.getBeamOGramOutput();
		
		// create the vector of beam directions for individual beams
		if (basicFreqDomParams.getNumBeams()>0) {
			int locContents = LocContents.HAS_BEARING | LocContents.HAS_AMBIGUITY | LocContents.HAS_BEARINGERROR;
			thisHasBeams = true;
			beamDirs = new PamVector[basicFreqDomParams.getNumBeams()];
			int[] headings = basicFreqDomParams.getHeadings();
			int[] slants = basicFreqDomParams.getSlants();
			beamLocalisations = new BeamFormerLocalisation[basicFreqDomParams.getNumBeams()];
			for (int i=0; i<basicFreqDomParams.getNumBeams(); i++) {
				beamDirs[i]=PamVector.fromHeadAndSlant(headings[i], slants[i]);
				double beamErr = 180.;
				if (basicFreqDomParams.getNumBeams() > 1) {
					if (i == 0 || i == basicFreqDomParams.getNumBeams()-1) {
						beamErr = Math.abs(headings[1]-headings[0]);
					}
					else {
						beamErr = Math.abs(headings[i+1]-headings[i-1]) / 2.;
					}
				}
				beamLocalisations[i] = null;//new BeamFormerLocalisation(null, locContents, basicFreqDomParams.getChannelMap(), Math.toRadians(headings[i]), Math.toRadians(beamErr));
			}
		}
		
		// get the parameters for the beamogram
		if (basicFreqDomParams.getNumBeamogram()>0) {
			thisHasABeamOGram = true;
		}
	}

	/**
	 * Prepare the beams for data.
	 */
	@Override
	public void prepare() {
		
		// get a list of all channels in this channel map, and the corresponding element locations
		int[] channelList = PamUtils.getChannelArray(basicFreqDomParams.getChannelMap());
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
		int[] boAngles = basicFreqDomParams.getBeamOGramAngles();
		int[] slantAngles = basicFreqDomParams.getBeamOGramSlants();
		if (slantAngles == null) {
			slantAngles = new int[]{0, 0, 1};
		}
		if (thisHasABeamOGram && boAngles != null && boAngles.length >= 2) {
			int nMainBeams = (int) ((basicFreqDomParams.getBeamOGramAngles()[1]-basicFreqDomParams.getBeamOGramAngles()[0])/basicFreqDomParams.getBeamOGramAngles()[2]+1);
			int nSecBeams = (int) ((slantAngles[1]-slantAngles[0])/slantAngles[2]+1);
			beamogramBeams = new BasicFreqDomBeam[nSecBeams][nMainBeams];
			beamogramSeqMap = PamUtils.SetBit(0, beamogramNum, true);
//			int arrayIdx = 0;
//			if (beamDirs != null) {
//				arrayIdx = beamDirs.length;
//			}
			PamVector beamDir = new PamVector();
			double[] beamogramWeights = basicFreqDomParams.getBeamogramWeights();
			beamogramWeights = checkBeamogramWeights(beamogramWeights, elementLocs.length);
			for (int j=0; j<nSecBeams; j++) {
				for (int i=0; i<nMainBeams; i++) {
					beamDir=PamVector.fromHeadAndSlant(boAngles[0]+i*boAngles[2],
							slantAngles[0]+j*slantAngles[2]);
//					System.out.printf("beam %d,%d %s\n", i, j, beamDir.toString());
					beamogramBeams[j][i] = new BasicFreqDomBeam(this, 
							basicFreqDomParams.getChannelMap(), 
							beamogramNum, 
							beamDir, 
							elementLocs, 
							channelList, 
							beamogramWeights, 
							basicFreqDomParams.getBeamOGramFreqRange(), 
							currentArray.getSpeedOfSound());
				}
			}		
			double[] freqBins = basicFreqDomParams.getBeamOGramFreqRange();
			beamOStartBin = beamProcess.frequencyToBin(freqBins[0]);
			beamOEndBin = beamProcess.frequencyToBin(freqBins[1]);
				
		// Create the individual beams.  Loop through the look vectors created in the constructor.  Give each beam a unique seequence number
		} 
		if (thisHasBeams) {
			individBeams = new BasicFreqDomBeam[beamDirs.length];
			double[][] beamWeights = basicFreqDomParams.getWeights();
			beamWeights = checkBeamWeights(beamWeights, beamDirs.length, elementLocs.length);
			for (int i=0; i<beamDirs.length; i++) {
				sequenceMap = PamUtils.SetBit(0, firstBeamNum+i, true);
				individBeams[i] = new BasicFreqDomBeam(this, 
						basicFreqDomParams.getChannelMap(), 
						firstBeamNum+i, 
						beamDirs[i], 
						elementLocs, 
						channelList, 
						basicFreqDomParams.getWeights()[i], 
						basicFreqDomParams.getFreqRange()[i], 
						currentArray.getSpeedOfSound());
			}
		}
	}
	
	/**
	 * Check the beamogram weights.  If they are undefined or there is an incorrect number based on
	 * the current number of elements, set them all to 1.
	 * 
	 * @param beamWeights
	 * @param nElements
	 * @return
	 */
	private double[] checkBeamogramWeights(double[] beamWeights, int nElements) {
		if (beamWeights == null || beamWeights.length < nElements) {
			beamWeights = new double[nElements];
			for (int i = 0; i < nElements; i++) {
					beamWeights[i] = 1;
			}
		}
		return beamWeights;
	}

	/**
	 * Check the individual beam weights.  If they are undefined or there is an incorrect number based on
	 * the current number of elements, set them all to 1.
	 * 
	 * @param beamWeights
	 * @param numBeams
	 * @param nElements
	 * @return
	 */
	private double[][] checkBeamWeights(double[][] beamWeights, int numBeams, int nElements) {
		if (beamWeights == null || beamWeights.length < numBeams || beamWeights[0].length < nElements) {
			beamWeights = new double[numBeams][nElements];
			for (int i = 0; i < numBeams; i++) {
				for (int j = 0; j < nElements; j++) {
					beamWeights[i][j] = 1;
				}
			}
		}
		return beamWeights;
	}

	/**
	 * Process an array of FFTDataUnits.  The size of the array will equal the number of channels in this beam group
	 */
	@Override
	public void process(FFTDataUnit[] fftDataUnits) {
		// if this is a BeamOGram, we need to loop through all of the beams, averaging the magnitudes of the returned complex numbers and then
		// saving that to a new index in a ComplexArray.  This ComplexArray will then be used to create a new BeamFormerDataUnit.
		if (thisHasABeamOGram && beamogramBeams != null) {
//			ComplexArray aveData = new ComplexArray(beams.length);
			int numAngleBins = beamogramBeams[0].length;//beamProcess.getFftDataSource().getFftLength()/2;
			int numSlantBins = beamogramBeams.length;
			int nFBins = keepFrequencyInfo ? fftDataUnits[0].getFftData().length() : 1;
			double[][][] beamData = new double[nFBins][numSlantBins][numAngleBins];
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
					beamThreads[i] = new Thread(new BeamOThread(fftDataUnits, beamData, i, nBeamOThreads));
					beamThreads[i].start();
				}
				try {
					for (int i = 0; i < nBeamOThreads; i++) {
						beamThreads[i].join();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				
			}
			else {
				for (int j=0; j<beamogramBeams.length; j++) {
					for (int i=0; i<beamogramBeams[0].length; i++) {
						ComplexArray summedData = beamogramBeams[j][i].process(fftDataUnits, beamOStartBin, beamOEndBin);
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
			BeamOGramDataUnit newUnit = new BeamOGramDataUnit(fftDataUnits[0].getTimeMilliseconds(),
					basicFreqDomParams.getChannelMap(),
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
				int[] boga = basicFreqDomParams.getBeamOGramAngles();
				double bestAng = peakPos.getBin() * (double) boga[2] + (double) boga[0];
				newUnit.setLocalisation(new BeamOGramLocalisation(newUnit, basicFreqDomParams.getChannelMap(), bestAng));
			}
			
			beamOGramOutput.addPamData(newUnit);
		}
		
		// if these are individual beams, loop through them one at a time and create a new BeamFormerDataUnit for each
		if (thisHasBeams) {
			int iBeam = 0;
			for (BasicFreqDomBeam beam : individBeams) {
				ComplexArray summedData = beam.process(fftDataUnits);
				BeamFormerDataUnit newUnit = new BeamFormerDataUnit(fftDataUnits[0].getTimeMilliseconds(),
						basicFreqDomParams.getChannelMap(),
						beam.getSequenceMap(), 
						fftDataUnits[0].getStartSample(),
						fftDataUnits[0].getSampleDuration(),
						summedData, 
						fftDataUnits[0].getFftSlice());
				newUnit.setLocalisation(beamLocalisations[iBeam]);
				beamformerOutput.addPamData(newUnit);
				iBeam++;
			}
		}
	}
	
	/**
	 * Multithreading inner class to process new data
	 *
	 */
	private class BeamOThread implements Runnable {

		private FFTDataUnit[] fftDataUnits;
		private double[][][] beamData;
		private int firstBeamIndex;
		private int beamIndexStep;

		public BeamOThread(FFTDataUnit[] fftDataUnits, double[][][] beamData, int firstBeamIndex, int beamIndexStep) {
			super();
			this.fftDataUnits = fftDataUnits;
			this.beamData = beamData;
			this.firstBeamIndex = firstBeamIndex;
			this.beamIndexStep = beamIndexStep;
		}

		@Override
		public void run() {
			for (int j=0; j<beamogramBeams.length; j++) {
				for (int i=firstBeamIndex; i<beamogramBeams[0].length; i+=beamIndexStep) {
					ComplexArray summedData = beamogramBeams[j][i].process(fftDataUnits, beamOStartBin, beamOEndBin);
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
	 * Returns a boolean indicating whether or not this beamformer is creating a beamogram
	 * @return
	 */
	@Override
	public boolean thereIsABeamogram() {
		return (thisHasABeamOGram);
	}
	
	/**
	 * Return the number of beams.  
	 */
	@Override
	public int getNumBeams() {
//		int numBeams = 0;
//		if (thisHasBeams) {
//			numBeams = beamDirs.length;
//		}
		return beamDirs.length;
	}
		
	/**
	 * @see beamformer.algorithms.BeamFormerAlgorithm#getBeamInformation(int)
	 */
	@Override
	public BeamInformation getBeamInformation(int iBeam) {
		return null;
	}
	
	/**
	 * @param beamformerOutput the beamformerOutput to set
	 */
	public void setBeamformerOutput(BeamFormerDataBlock beamformerOutput) {
		this.beamformerOutput = beamformerOutput;
	}


	/**
	 * @return the beamProcess
	 */
	public BeamFormerBaseProcess getBeamProcess() {
		return beamProcess;
	}

	/**
	 * Get the sequence map for the beams created by this beamformer
	 * 
	 * @return the sequenceMap
	 */
	public int getSequenceMap() {
		return sequenceMap;
	}

	/* (non-Javadoc)
	 * @see beamformer.algorithms.BeamFormerAlgorithm#getNumBeamogramAngles()
	 */
	@Override
	public int getNumBeamogramAngles() {
		int[] boAngles = basicFreqDomParams.getBeamOGramAngles();
		if (boAngles == null) return 1;
		int numAngles = (int) ((basicFreqDomParams.getBeamOGramAngles()[1]-basicFreqDomParams.getBeamOGramAngles()[0])/basicFreqDomParams.getBeamOGramAngles()[2]+1);
		return numAngles;
	}

	@Override
	public void setKeepFrequencyInformation(boolean keep) {
		keepFrequencyInfo  = keep;
	}

	@Override
	public void setFrequencyBinRange(int binFrom, int binTo) {
		beamOStartBin = binFrom;
		beamOEndBin = binTo;		
	}


}
