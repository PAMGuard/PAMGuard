package RightWhaleEdgeDetector;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import Localiser.algorithms.Correlations;
import Localiser.algorithms.timeDelayLocalisers.bearingLoc.BearingLocaliser;
import Localiser.algorithms.timeDelayLocalisers.bearingLoc.BearingLocaliserSelector;
import annotation.calcs.snr.SNRAnnotationType;
import autecPhones.AutecGraphics;
import fftManager.Complex;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import networkTransfer.receive.BuoyStatusDataUnit;
import spectrogramNoiseReduction.kernelSmoothing.KernelSmoothing;
import whistlesAndMoans.WhistleBearingInfo;
import PamController.PamController;
import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamDetection.LocalisationInfo;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import PamView.symbol.StandardSymbolManager;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import PamguardMVC.debug.Debug;
import RightWhaleEdgeDetector.graphics.RWESymbolManager;

public class RWEProcess extends PamProcess {

	private RWEChannelProcess[] rweChannelProcesses;
	private RWEControl rweControl;
	private FFTDataBlock sourceDataBlock;
	private RWEDataBlock rweDataBlock;
		
	public RWEDataBlock getRweDataBlock() {
		return rweDataBlock;
	}

	private Hashtable<Integer, BearingLocaliser> bearingLocalisers;
	private StandardSymbolManager symbolManager;
	/**
	 * Already has Kernel smoothing applied to input data. 
	 */
	private boolean isPreSmoothed;
	private KernelSmoothing kernelSmoothing;
	
	public RWEProcess(RWEControl rweControl) {
		super(rweControl, null);
		this.rweControl = rweControl;
		rweDataBlock = new RWEDataBlock(rweControl, rweControl.getUnitName(), this, 0);
		addOutputDataBlock(rweDataBlock);
		rweDataBlock.setLocalisationContents(LocContents.HAS_BEARING);
		rweDataBlock.setDatagramProvider(new RWEDatagramProvider());
		rweDataBlock.setOverlayDraw(new RWEOverlayGraphics(this, rweDataBlock));
		StandardSymbolManager symbolManager = symbolManager = new RWESymbolManager(rweDataBlock, RWEOverlayGraphics.defaultSymbol, true);
		symbolManager.addSymbolOption(StandardSymbolManager.HAS_LINE_AND_LENGTH);
		rweDataBlock.setPamSymbolManager(symbolManager);
		symbolManager.addSymbolOption(StandardSymbolManager.HAS_LINE | StandardSymbolManager.HAS_LINE_LENGTH);
		
		rweDataBlock.setBinaryDataSource(new RWEBinaryDataSource(this, rweDataBlock));
		rweDataBlock.setCanClipGenerate(true);
		rweDataBlock.SetLogging(new RWESQLLogging(rweControl, rweDataBlock));
		rweDataBlock.addDataAnnotationType(new SNRAnnotationType());
	}
	
	@Override
	public void pamStart() {
		setupProcesses();
	}
	@Override
	public void pamStop() {
	}
	
	@Override
	public void newData(PamObservable o, PamDataUnit pamDataUnit) {
		FFTDataUnit fftDataUnit = (FFTDataUnit) pamDataUnit;
//		int chan = PamUtils.getSingleChannel(fftDataUnit.getChannelBitmap());
		int chan = PamUtils.getSingleChannel(fftDataUnit.getSequenceBitmap());
		if (rweChannelProcesses[chan] == null) {
			return;
		}
		else {
			rweChannelProcesses[chan].newData(fftDataUnit);
		}
	}


	public void setupProcesses() {
		
		PamDataBlock db = PamController.getInstance().getDataBlock(FFTDataUnit.class, 
				rweControl.rweParameters.dataSourceName);
		if (db != getSourceDataBlock()) {
			setParentDataBlock(db, true);
			sourceDataBlock = (FFTDataBlock) db;
		}
		if (sourceDataBlock == null) {
			return;
		}
		/**
		 * work out if the fft source has already run the Guassian smoothing or not
		 */
		isPreSmoothed = rweControl.hasKernelSmoothing(sourceDataBlock);
		if (isPreSmoothed) {
			kernelSmoothing = null;
		}
		else {
			kernelSmoothing = new KernelSmoothing();
			kernelSmoothing.initialise(sourceDataBlock.getChannelMap());
		}
//		sourceDataBlock.findAnnotation(null);
//		rweDataBlock.setChannelMap(rweControl.rweParameters.channelMap);
		rweDataBlock.sortOutputMaps(sourceDataBlock.getChannelMap(), sourceDataBlock.getSequenceMapObject(), rweControl.rweParameters.channelMap);
		if (db == null) {
			return;
		}
		rweDataBlock.setFftLength(sourceDataBlock.getFftLength());
		rweDataBlock.setFftHop(sourceDataBlock.getFftHop());
		rweChannelProcesses = new RWEChannelProcess[PamConstants.MAX_CHANNELS];
		int channelMap = rweControl.rweParameters.channelMap;
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			if ((channelMap & (1<<i)) != 0) {
				rweChannelProcesses[i] = new RWEChannelProcess(this, i);
			}
		}
//		checkBearingLocaliser(sourceDataBlock.getChannelMap());
	}
	
	/**
	 * Class to do absolutely all the processing - peak detection, region detection, etc
	 * for a single channel of the detector. 
	 * @author Doug Gillespie
	 *
	 */
	class RWEChannelProcess {
		private RWEProcess rweProcess;
		private int iChannel;
		int searchBin1, searchBin2;
		double[] backgroundData;
		double[] magData;
		boolean[] oTh;
		boolean first = true;
		double updateConstant[] = new double[2];
		double threshold;
		private List<RWESound> growingSounds;
		private int maxGap;
		private int maxFrequencyGap;
		private int numOT; // number of bins in last analysed slice over threshold
		private int minSoundType;
		private RWClassifier classifier = new RWStandardClassifier();
		
		private int[] soundCounts = new int[RWStandardClassifier.MAXSOUNDTYPE+1];
		
		public RWEChannelProcess(RWEProcess rweProcess, int iChannel) {
			this.rweProcess = rweProcess;
			this.iChannel = iChannel;
			searchBin1 = (int) (rweControl.rweParameters.startFreq * 
					sourceDataBlock.getFftLength() / getSampleRate());
			searchBin2 = (int) (rweControl.rweParameters.endFreq * 
					sourceDataBlock.getFftLength() / getSampleRate());
			searchBin1 = Math.max(0, searchBin1);
			searchBin2 = Math.min(searchBin2, sourceDataBlock.getFftLength()/2-1);
			backgroundData = new double[searchBin2+1];
			magData = new double[searchBin2+1];
			oTh = new boolean[searchBin2+1];
			updateConstant[0] = rweControl.rweParameters.backgroundConst[0];
			updateConstant[1] = rweControl.rweParameters.backgroundConst[1];
			threshold = rweControl.rweParameters.threshold;
			growingSounds = new LinkedList<RWESound>();
			maxGap = rweControl.rweParameters.maxSoundGap;
			maxFrequencyGap = rweControl.rweParameters.maxFrequencyGap;
			minSoundType = rweControl.rweParameters.minSoundType;
			classifier.setSoundData(getSampleRate(), sourceDataBlock.getFftLength(),
					sourceDataBlock.getFftHop());
//			System.out.println("Create right whale channel process " + iChannel);
		}
		
		
		public void newData(FFTDataUnit fftDataUnit) {
			if (!isPreSmoothed) {
				FFTDataUnit newFFTUnit = new FFTDataUnit(fftDataUnit.getTimeMilliseconds(), fftDataUnit.getChannelBitmap(), 
						fftDataUnit.getStartSample(), fftDataUnit.getSampleDuration(), null, fftDataUnit.getFftSlice());
				newFFTUnit.setSequenceBitmap(fftDataUnit.getSequenceBitmapObject());
				newFFTUnit.setFftData(fftDataUnit.getFftData().clone());
				kernelSmoothing.runNoiseReduction(newFFTUnit);
				fftDataUnit = newFFTUnit;
			}
			
			RWEDetectionPeak[] newPeaks = findPeaks(fftDataUnit.getFftData());
			RWESound[] newSounds = findSounds(fftDataUnit.getTimeMilliseconds(), newPeaks);
			if (newSounds == null) {
				return;
			}
//			System.out.printf("FFT data at %s sample %d\n", PamCalendar.formatTime(fftDataUnit.getTimeMilliseconds(), 3), fftDataUnit.getStartSample());
			int nSounds = newSounds.length;
			int soundType;
			RWESound aSound;
			RWEDataUnit rweDataUnit;
			long duration;
			double f[] = new double[2];
			for (int i = 0; i < nSounds; i++) {
				aSound = newSounds[i];
				soundType = classifier.getSoundClass(aSound);
				aSound.soundType = soundType;
				if (soundType >= minSoundType) {
					// output the sound the the output data block. 
//					System.out.println(String.format("Detected sound type %d on channel %d", 
//							soundType, this.iChannel));
					duration = sourceDataBlock.getFftHop() * aSound.duration;
					rweDataUnit = new RWEDataUnit(RWEProcess.this, aSound.timeMilliseconds, 
							1<<iChannel, fftDataUnit.getStartSample()-duration, duration, aSound);
					rweDataUnit.sortOutputMaps(sourceDataBlock.getChannelMap(), sourceDataBlock.getSequenceMapObject(), 1<<iChannel);
					f[0] = aSound.minFreq * getSampleRate()/sourceDataBlock.getFftLength();
					f[1] = aSound.maxFreq * getSampleRate()/sourceDataBlock.getFftLength();
					rweDataUnit.setFrequency(f);
					rweDataBlock.addPamData(rweDataUnit);
					soundCounts[rweDataUnit.rweSound.soundType]++;
//					System.out.printf("RW %d at %s\n", rweDataUnit.rweSound.soundType, PamCalendar.formatTime(rweDataUnit.getTimeMilliseconds()));
				}
			}
		}
		
		/**
		 * Clear sound counts
		 */
		private void clearSoundCounts() {
			Arrays.fill(soundCounts, 0);
		}
		
		private RWEDetectionPeak[] findPeaks(ComplexArray complexArray) {
			RWEDetectionPeak[] detectedPeaks = null;
			int nPeaks = 0;
			int nOver = 0;
			double onDown = rweControl.rweParameters.downThreshold ? 1 : 0;
			for (int i = searchBin1; i <= searchBin2; i++) {
				magData[i] = complexArray.magsq(i);
			}
			if (first) {
				first = false;
				for (int i = searchBin1; i <= searchBin2; i++)  {
					backgroundData[i] = magData[i] * 10;
				}
			}
			for (int i = searchBin1; i <= searchBin2; i++)  {
				if (oTh[i] = (magData[i]/threshold) > backgroundData[i]) {
			      // now update the background slowly (over threshold)
			    	backgroundData[i] += (magData[i]-backgroundData[i])/updateConstant[1];
			    	nOver++;
			    }
			    else
			    {
			      // now update the background more quickly (below threshold)
			    	backgroundData[i] += (magData[i]-backgroundData[i])/updateConstant[0];
			    }
				if (Double.isNaN(backgroundData[i]) || Double.isInfinite(backgroundData[i])) {
					System.out.println(String.format("Bad bg data slice %d = %3.5f", i, backgroundData[i]));
					backgroundData[i] = magData[i] * 10.;
				}
			}
			numOT = nOver;
			if (nOver == 0) {
				return null;
			}
			boolean peakOn = false;
			RWEDetectionPeak newPeak = new RWEDetectionPeak(0);
			for (int i = searchBin1; i <= searchBin2; i++)  {
				if (!peakOn) {
					if(oTh[i]) { // start a peak.
						newPeak.bin1 = newPeak.bin2 = newPeak.peakBin = i;
						newPeak.signal = newPeak.maxAmp = magData[i];
						newPeak.noise = backgroundData[i];
						peakOn = true;
					}
					// do nothing it it's still off !
				}
				else {
					// if downThreshold == false then onDown is 0, so second threshold is always on since magData always > 0
					if (oTh[i] && magData[i] > newPeak.maxAmp*onDown/threshold) { // continue peak
						newPeak.bin2 = i;
						if (magData[i] > newPeak.maxAmp) {
							newPeak.maxAmp = magData[i];
							newPeak.peakBin = i;
							newPeak.signal += magData[i];
							newPeak.noise += backgroundData[i];
						}
					}
					else { // end peak
						if (detectedPeaks == null) {
							detectedPeaks = new RWEDetectionPeak[1];
						}
						else {
							detectedPeaks = Arrays.copyOf(detectedPeaks, nPeaks+1);
						}
						detectedPeaks[nPeaks++] = newPeak;
						newPeak = new RWEDetectionPeak(0);
						peakOn = false;
						// now trim off the start of the peak if it's too wide. 
						if (onDown > 0) {
							for (int ip = newPeak.bin1; ip < newPeak.peakBin; ip++) {
								if (magData[ip] < newPeak.maxAmp/threshold) {
									newPeak.bin1 = ip+1;
									newPeak.signal -= magData[ip];
									newPeak.noise -= backgroundData[ip];
								}
							}
						}
					}
				}
			}
			return detectedPeaks;
		}

		/**
		 * From the detected peaks, so some region connecting 
		 * and make some sounds. 
		 * @param newPeaks array of new peaks
		 * @return array of completed sounds. 
		 */
		private RWESound[] findSounds(long timeMilliseconds, RWEDetectionPeak[] newPeaks) {
			RWESound[] rweSounds = null;
			int nSounds = 0;
			/*
			 * First see if any old sounds have had their day and send them 
			 * back ...
			 */
			RWESound aSound;
			Iterator<RWESound> sI = growingSounds.iterator();
			while (sI.hasNext()) {
				aSound = sI.next();
				if (aSound.deadCount++ > maxGap) {
					sI.remove();
					aSound.completeSound();
					if (rweSounds == null) {
						rweSounds = new RWESound[1];
					}
					else {
						rweSounds = Arrays.copyOf(rweSounds, nSounds+1);
					}
					rweSounds[nSounds++] = aSound;
				}
			}
			/*
			 *  now go through any sounds left in the list and grow them 
			 *  with the new peaks. 
			 */
			int lastSlice, newSlice = 0;
			int nPeaks = 0;
			if (newPeaks != null) {
				nPeaks = newPeaks.length;
				sI = growingSounds.iterator();
				while (sI.hasNext()) {
					aSound = sI.next();
					if (aSound.peakStolen) continue;
					lastSlice = aSound.sliceCount-1;
					for (int iP = 0; iP < nPeaks; iP++) {
						if (aSound.lowFreq[lastSlice] > newPeaks[iP].bin2+maxFrequencyGap) continue;
						if (aSound.highFreq[lastSlice] < newPeaks[iP].bin1-maxFrequencyGap) continue;
						if (newPeaks[iP].rweSound != null) {
					        // the peak has already been added to another sound.
					        // see which sound is the longest and if this sound is longer, steal the
					        // peak, otherwise, let the other sound keep it
					        if (newPeaks[iP].rweSound.sliceCount >= aSound.sliceCount) continue;
					        // otherwise, shorten that other sound.  
					        newPeaks[iP].rweSound.peakStolen = true;
						}
						newPeaks[iP].rweSound = aSound;
						// if the deadCount is == 0 then a peak has already been added to this
						// sound from this time partition, otherwise we need a new one
						if (aSound.deadCount == 0) {
							aSound.highFreq[newSlice] = newPeaks[iP].bin2;
					        if (newPeaks[iP].maxAmp > aSound.peakAmp[newSlice]) {
					          aSound.peakFreq[newSlice] = newPeaks[iP].peakBin;   
					          aSound.peakAmp[newSlice] = newPeaks[iP].maxAmp;
					        }
					        aSound.signal += newPeaks[iP].signal;
					        aSound.noise += newPeaks[iP].noise;
						}
						else {
							aSound.addPeak(aSound.sliceList[lastSlice] + aSound.deadCount, newPeaks[iP], numOT);
					        newSlice = lastSlice + 1;    
						}
					}	
				}
				/*
				 * Now go through the peaks one more time and any peak which hasn't been 
				 * allocated to a sound is used to create a new sound. 
				 * (line 146 in fprwfindsounds.c)
				 */
				for (int iP = 0; iP < nPeaks; iP++) {
					if (newPeaks[iP].rweSound != null) {
						continue;
					}
					aSound = new RWESound(timeMilliseconds, newPeaks[iP], numOT);
					growingSounds.add(aSound);
				}
			}
			
			return rweSounds;
		}
				
	}
	
//	private boolean checkBearingLocaliser(int channelMap) {
//		int hydrophoneMap = sourceDataBlock.getChannelListManager().channelIndexesToPhones(channelMap);
//		double timingError = Correlations.defaultTimingError(getSampleRate());
//		boolean ok = (bearingLocaliser != null);
//		if (ok) {
//			ok = bearingLocaliser.getHydrophoneMap() == hydrophoneMap;
//		}
//		if (ok == false) {
//			bearingLocaliser = BearingLocaliserSelector.createBearingLocaliser(hydrophoneMap, timingError); 
//		}
//		return (bearingLocaliser != null);
//	}

	private BearingLocaliser findBearingLocaliser(PamDataUnit rwDataUnit) {
		int chanMap = rwDataUnit.getChannelBitmap();
		int hydrophoneMap = sourceDataBlock.getChannelListManager().channelIndexesToPhones(chanMap);
		return findBearingLocaliser(hydrophoneMap);
	}
	private synchronized BearingLocaliser findBearingLocaliser(int hydrophoneMap) {
		int nPhones = PamUtils.getNumChannels(hydrophoneMap);
		if (nPhones < 2) {
			return null;
		}
		if (bearingLocalisers == null) {
			bearingLocalisers = new Hashtable<Integer, BearingLocaliser>();
		}
		BearingLocaliser bearingLocaliser = bearingLocalisers.get(hydrophoneMap);
		if (bearingLocaliser == null) {
			bearingLocaliser = BearingLocaliserSelector.createBearingLocaliser(hydrophoneMap, Correlations.defaultTimingError(getSampleRate()));
			if (bearingLocaliser != null) {
				bearingLocalisers.put(hydrophoneMap, bearingLocaliser);
			}
		}
		return bearingLocaliser;
	}

	@Override
	public void processNewBuoyData(BuoyStatusDataUnit buoyStatusDataUnit, PamDataUnit dataUnit) {
		/**
		 * Estimate bearings to RW sound using a standard TOAD bearing localiser. 
		 */
		RWEDataUnit rwDataUnit = (RWEDataUnit) dataUnit;
//		double la1 = dataUnit.getOriginLatLong(false).getLatitude();
//		double lo1 = dataUnit.getOriginLatLong(false).getLongitude();
		
		rwDataUnit.getOriginLatLong(true);
		
//		if (dataUnit.getChannelBitmap() > 20) {
//			double la2 = dataUnit.getOriginLatLong(false).getLatitude();
//			double lo2 = dataUnit.getOriginLatLong(false).getLongitude();
//			System.out.printf("Recalculate origin for rwe unit channels %d phones %d, move %6.7f, %6.7f %s,%s\n", 
//					dataUnit.getChannelBitmap(), dataUnit.getHydrophoneBitmap(),
//					(la2-la1)*60*1852, (lo2-lo1)*60*1852, LatLong.formatLatitude(la2), LatLong.formatLongitude(lo2));
//		}
		calculateAngles(rwDataUnit);
//		Debug.out.printf("Right whale TOAD delay[0] = %3.3fms\n", delays[0]*1000.);
	}
	
	protected boolean calculateAngles(RWEDataUnit rwDataUnit) {

		double[] delays = rwDataUnit.getTimeDelaysSeconds();
		if (delays == null || delays.length == 0) {
			return false;
		}
		BearingLocaliser bearingLocaliser = findBearingLocaliser(rwDataUnit);
		if (bearingLocaliser == null) {
			bearingLocaliser = findBearingLocaliser(rwDataUnit);
			return false;
		}
		double[][] angles = bearingLocaliser.localise(delays, rwDataUnit.getTimeMilliseconds());
		WhistleBearingInfo newLoc = new WhistleBearingInfo(rwDataUnit, bearingLocaliser, 
				bearingLocaliser.getHydrophoneMap(), angles);
			newLoc.setArrayAxis(bearingLocaliser.getArrayAxis());
		rwDataUnit.setLocalisation(newLoc);
		return true;
	}

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		// best do this from the binary datasource so that it also happens
		// during offline task data loading. 
//		if (changeType == PamController.OFFLINE_DATA_LOADED) {
//			recalculateAllAngles();
//		}
	}

	public String getModuleSummary(boolean clear) {
		String str = String.format("%d", RWStandardClassifier.MAXSOUNDTYPE+1);
		for (int c = 0; c < rweChannelProcesses.length; c++) {
			if (rweChannelProcesses[c] == null) {
				continue;
			}
			str += String.format("ch%d", c);
			int[] counts = rweChannelProcesses[c].soundCounts;
			for (int i = 0; i < counts.length; i++) {
				str += String.format(",%d", counts[i]);
			}
			if (clear) {
				rweChannelProcesses[c].clearSoundCounts();
			}
		}
		return str;
	}

//	private void recalculateAllAngles() {
//		synchronized(rweDataBlock.getSynchLock()) {
//			ListIterator<RWEDataUnit> it = rweDataBlock.getListIterator(0);
//			while (it.hasNext()) {
//				calculateAngles(it.next());
//			}
//		}
//	}

}
