package qa.generator;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import Acquisition.AcquisitionControl;
import AirgunDisplay.AirgunControl;
import Array.ArrayManager;
import Array.PamArray;
import Array.SnapshotGeometry;
import GPS.GPSControl;
import PamController.PamController;
import PamController.masterReference.MasterReferencePoint;
import PamController.positionreference.PositionReference;
import PamController.positionreference.PositionReferenceFinder;
import PamDetection.RawDataUnit;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamView.symbol.StandardSymbolManager;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamInstantProcess;
import PamguardMVC.PamObservable;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.toad.GenericTOADSourceParams;
import pamMaths.PamVector;
import propagation.Absorption;
import propagation.LogLawPropagation;
import propagation.PropagationModel;
import propagation.SphericalPropagation;
import qa.ClusterParameters;
import qa.QAControl;
import qa.QADataProcess;
import qa.QANotifyable;
import qa.QASequenceDataBlock;
import qa.QASequenceDataUnit;
import qa.QASoundDataBlock;
import qa.QASoundDataUnit;
import qa.QATestDataBlock;
import qa.QATestDataUnit;
import qa.database.QASequenceLogging;
import qa.database.QASoundLogging;
import qa.database.QATestLogging;
import qa.generator.clusters.QACluster;
import qa.generator.location.QALocationGenerator;
import qa.generator.location.SetDistanceGenerator;
import qa.generator.location.SmartLocationGenerator;
import qa.generator.sequence.SequenceData;
import qa.generator.sequence.SoundSequence;
import qa.generator.sounds.StandardSoundGenerator;
import qa.generator.sounds.RandomToneGenerator;
import qa.generator.testset.QATestSet;
import qa.generator.testset.SimpleTestSet;
import qa.operations.QAOpsDataUnit;
import qa.swing.SequenceOverlayDraw;

public class QAGeneratorProcess extends QADataProcess {

	private QAControl qaControl;

	private PropagationModel spreadingModel = new LogLawPropagation(15.); // was 20 in initial testings ...

	private Absorption absorption = new Absorption();

	private AcquisitionControl daqControl;

	private LinkedList<QATestSet> currentTests = new LinkedList<>();

	private LinkedList<SoundSequence> currentSequences = new LinkedList<>();

	private long currentSample;
	
	private PositionReferenceFinder positionReferenceFinder;
	private Class[] preferedReference = {AirgunControl.class, ArrayManager.class, GPSControl.class}; 

	public QAGeneratorProcess(QAControl qaControl) {
		super(qaControl, true);
		this.qaControl = qaControl;

		getTestsDataBlock().setNaturalLifetime(3600*24);
		getSequenceDataBlock().setNaturalLifetime(3600);
		getSequenceDataBlock().setOverlayDraw(new SequenceOverlayDraw());
		getSoundsDataBlock().setNaturalLifetime(3600);
		
		positionReferenceFinder = new PositionReferenceFinder(preferedReference, true);
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#setupProcess()
	 */
	@Override
	public void setupProcess() {
		// for now, just subscribe to the first available
		// acquisition block. 
		daqControl = (AcquisitionControl) PamController.getInstance().findControlledUnit(AcquisitionControl.class, null);
		if (daqControl == null) {
			setParentDataBlock(null);
		}
		else {
			setParentDataBlock(daqControl.getRawDataBlock());
		}

		super.setupProcess();
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#prepareProcess()
	 */
	@Override
	public void prepareProcess() {

		super.prepareProcess();
		currentSample = 0;

//		currentTests.clear();
//		currentSequences.clear();
	}

	@Override
	public void pamStart() {
		currentSequences.clear();
	}

	@Override
	public void pamStop() {
		getQaLogging().flushBuffer();
	}
	
	public QATestDataUnit addTestSet(String testType, QACluster cluster, QALocationGenerator locationGenerator) {
		QATestSet testSet = new SimpleTestSet(cluster.getName(), locationGenerator, cluster, getSampleRate(), currentSample);
		long now = absSamplesToMilliseconds(currentSample + (long)getSampleRate());
		QATestDataUnit newTest = new QATestDataUnit(now, testType, testSet);
		QAOpsDataUnit opsUnit = findOpsDataUnit(now);
		newTest.setQaOpsDataUnit(opsUnit);
		getTestsDataBlock().addPamData(newTest);
		synchronized (currentTests) {
			currentTests.add(testSet);
		}
		qaControl.tellNotifyables(QANotifyable.TEST_START, testSet);
		return newTest;
	}
	
	public void addTestSet(QATestDataUnit testDataUnit) {
		if (testDataUnit.getParentDataBlock() == null) {
			getTestsDataBlock().addPamData(testDataUnit);
		}
		synchronized (currentTests) {
			currentTests.add(testDataUnit.getQaTestSet());
		}
		qaControl.tellNotifyables(QANotifyable.TEST_START, testDataUnit.getQaTestSet());
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#newData(PamguardMVC.PamObservable, PamguardMVC.PamDataUnit)
	 */
	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		RawDataUnit rawDataUnit = (RawDataUnit) arg;
		PamDataBlock dataBlock = (PamDataBlock) o;
		int firstChan = 1<< PamUtils.getLowestChannel(dataBlock.getChannelMap());
		if (firstChan == rawDataUnit.getChannelBitmap()) {
			// data arrive channel by channel, this
			// only needs to be called once for all channels. 
			generateSounds(currentSample = rawDataUnit.getStartSample() + rawDataUnit.getSampleDuration());
		}
		// but this needs calling for each channel
		addGeneratedSounds(rawDataUnit);
	}

	/**
	 * Generate sounds up to lastSample from all current sequences in all test sets. 
	 * @param lastSample limit of how far in time to generate sounds. 
	 */
	private void generateSounds(long lastSample) {

		synchronized (currentTests) {
			if (currentTests.isEmpty()) {
				return;
			}
			LatLong currentLocation = getReferenceLocation(lastSample);
			/*
			 * First see if any tests need to start a new sequence
			 */
			Iterator<QATestSet> testIt = currentTests.iterator();
			while (testIt.hasNext()) {
				QATestSet testSet = testIt.next();
				SoundSequence newSequence = testSet.getNextSequence(lastSample, currentLocation);
				if (newSequence != null) {
					long tm = absSamplesToMilliseconds(newSequence.getStartSample());
					/**
					 * Create the data unit. the SoundSequence will hold a reference to the 
					 * data unit. only add it to the datablock when the sequence is complete. 
					 */
					QASequenceDataUnit seqDataUnit = new QASequenceDataUnit(tm, newSequence);
					seqDataUnit.setQaOpsDataUnit(findOpsDataUnit(tm));
					seqDataUnit.setDistanceToAirgun(positionReferenceFinder.distanceToAirgun(newSequence.getSourceLocation(), tm));
					Double hydDist = findNearestHydrophone(newSequence.getSourceLocation(), tm);
					seqDataUnit.setDistanceToHydrophone(hydDist);

					getSequenceDataBlock().addPamData(seqDataUnit);
					testSet.getTestDataUnit().addSubDetection(seqDataUnit);
					getTestsDataBlock().updatePamData(testSet.getTestDataUnit(), tm);
					
					qaControl.tellNotifyables(QANotifyable.SEQUENCE_START, seqDataUnit);
					
					currentSequences.add(newSequence);
				}
			}
			/**
			 * Then loop through all current sequences and see if any need sounds generated.
			 */
			Iterator<SoundSequence> sequenceIt = currentSequences.iterator();
			while (sequenceIt.hasNext()) {
				SoundSequence aSequence = sequenceIt.next();
				while (true) {
					SequenceData sequenceData = aSequence.getNext(lastSample);
					if (sequenceData == null) {
						break;
					}
					generateSound(aSequence, sequenceData);
				}
				// remove any completed sequences
				if (aSequence.isFinished(lastSample)) {
					sequenceIt.remove();
					// add to the datablock so it gets written to the database. 
					getSequenceDataBlock().updatePamData(aSequence.getSequenceDataUnit(), PamCalendar.getTimeInMillis());
					qaControl.tellNotifyables(QANotifyable.SEQUENCE_END, aSequence.getSequenceDataUnit());
				}
			}
			/*
			 * Loop through the tests again to close any complete ones. This has to be 
			 * done after sequences have been saved (just above) so that the last sequence
			 * already has a parent datablock, or it all goes horribly wrong 
			 */
			testIt = currentTests.iterator();
			int removed = 0;
			while (testIt.hasNext()) {
				QATestSet testSet = testIt.next();
				// remove any completed tests
				if (testSet.isFinsihed(lastSample)) {
					long endTime = absSamplesToMilliseconds(lastSample);
					testSet.setEndTime(endTime);
					if (QATestSet.STATUS_CANCELLED.equals(testSet.getStatus()) == false) {
						testSet.setStatus(QATestSet.STATUS_COMPLETE);
//						removeTest(testSet);
					}
					getTestsDataBlock().updatePamData(testSet.getTestDataUnit(), endTime);
					testIt.remove();
					removed++;
				}
			}
			if (removed > 0) {
				qaControl.tellNotifyables(QANotifyable.TEST_END);
			}
		}
	}
	
	/**
	 * Get the 2D distance to the nearest hydrophone for the given location. 
	 * @param sourceLocation location
	 * @param tm time
	 * @return horizontal distance. 
	 */
	private Double findNearestHydrophone(LatLong sourceLocation, long tm) {
		int hydrophoneMap = getParentDataBlock().getHydrophoneMap();
		
		SnapshotGeometry geometry = ArrayManager.getArrayManager().getSnapshotGeometry(hydrophoneMap, tm);
		if (geometry == null) {
			return null;
		}
		PamVector[] geomVecs = geometry.getGeometry();
		int nHyd = geomVecs.length;
		double minDist = Double.MAX_VALUE;
		for (int i = 0; i < nHyd; i++) {
			LatLong absPhonePos = geometry.getReferenceGPS().addDistanceMeters(geomVecs[i]);
			double d = absPhonePos.distanceToMetres(sourceLocation);
			minDist = Math.min(minDist, d);
		}
		
		return minDist;
	}

	/**
	 * Generate a single sound. Only called when it really IS time 
	 * to generate. 
	 * @param aSequence sound test set (should know about the sound to be generated)
	 * @param sequenceData (gives the time and amplitude data for the sound)
	 */
	private void generateSound(SoundSequence aSequence, SequenceData sequenceData) {
		/*
		 * Call back into the testset to do the actual work so that it can override behaviour
		 * and generally have freedom to do whatever the programmer wants. 
		 */
		QASoundGenerator qaSoundGenerator = aSequence.getQaTestSet().getQaCluster().getSoundGenerator();
		long timeMillis = absSamplesToMilliseconds(sequenceData.getStartSample());
		double amplitudeDB = sequenceData.getAmplitude();
		/**
		 * Work out where everything is and what the propagation delays and
		 * attenuations are... Prop model may return multiple values if there
		 * are reflections / multipath. 
		 */
		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
		if (getParentDataBlock() == null) {
			return;
		}
		int channelMap = getParentDataBlock().getChannelMap();
		int phoneMap = getParentDataBlock().getHydrophoneMap();
		SnapshotGeometry geometry = ArrayManager.getArrayManager().getSnapshotGeometry(phoneMap, timeMillis);
		PamVector[] geomVecs = geometry.getGeometry();
		//get the absolute start sample for the sound at it's source...
		long sourceSample = sequenceData.getStartSample();
		/**
		 * For each phone now need to get it's abs lat long, then feed into the prop model. 
		 */
		int nChan = PamUtils.getNumChannels(channelMap);
		/*
		 * Can use doubles for start sample, even after 1 year at 500kHz 
		 * sample rate the double accuracy is .002 samples which is fine.
		 */
		int nPath = spreadingModel.getNumPaths();
		double[][] channelDelay = new double[nPath][nChan];
		double[][] channelGain = new double[nPath][nChan];
		double[][] channelRanges = new double[nPath][nChan];
		double[] delays, gains;
		double[] highestGain = new double[nPath];

		/*
		 * Transpose during calculation, so it's each to take each path in turn for sound generation
		 * Also work out actual amplitudes at this point while we've got access to the acquisition
		 * calibration data. 
		 */
		double c = ArrayManager.getArrayManager().getCurrentArray().getSpeedOfSound();
		for (int i = 0; i < nChan; i++) {
			int chanInd = PamUtils.getNthChannel(i, channelMap);
			double dbFullScale = daqControl.getDaqProcess().rawAmplitude2dB(1, chanInd, false);
			double pip = Math.pow(10, -dbFullScale/20.);
			LatLong absPhonePos = geometry.getReferenceGPS().addDistanceMeters(geomVecs[chanInd]);
			spreadingModel.setLocations(absPhonePos, aSequence.getSourceLocation(), array.getSpeedOfSound());
			delays = spreadingModel.getDelays();
			gains = spreadingModel.getGains();
			for (int j = 0; j < delays.length; j ++) {
				channelRanges[j][i] = delays[j] * c;
				channelDelay[j][i] = delays[j] * (double) getSampleRate();
				channelGain[j][i] = gains[j] * pip * Math.pow(10., amplitudeDB/20.);
				highestGain[j] = Math.max(highestGain[j], gains[j]);
			}
		}
		for (int i = 0; i < channelDelay.length; i++) {
			QASound sound = qaSoundGenerator.generateSound(sourceSample, sampleRate, channelDelay[i], channelGain[i]);
			double[][] wav = sound.getWave();
			for (int w = 0; w < wav.length; w++) {
				wav[w] = absorption.fftAbsorption(wav[w], getSampleRate(), -aSequence.getSourceLocation().getHeight(), channelRanges[i][w]);
			}
			/*
			 * Work out the highest received level based on the waveform and the gain. 
			 * For now, just do this for channel 0 - will need to refine this for more widely 
			 * distributed arrays. 
			 */
			double rl = getReceivedLevel(wav[0]);
			
			long arrivalStartMillis, arrivalEndMillis;
			QASoundDataUnit qaDataUnit = new QASoundDataUnit(aSequence, sequenceData, timeMillis, channelMap, sourceSample, sound, rl);
			qaDataUnit.setDistanceToAirgun(positionReferenceFinder.distanceToAirgun(aSequence.getSourceLocation(), timeMillis));
			qaDataUnit.setDistanceToHydrophone(findNearestHydrophone(aSequence.getSourceLocation(), timeMillis));
			arrivalStartMillis = absSamplesToMilliseconds(qaDataUnit.getFirstSample());
			arrivalEndMillis = absSamplesToMilliseconds(qaDataUnit.getLastSample());
			qaDataUnit.setArrivalMillis(arrivalStartMillis, arrivalEndMillis);
			qaDataUnit.setMultiPath(i);
			getSoundsDataBlock().addPamData(qaDataUnit);
//			System.out.printf("Adding sound %s to sequence %s / %s\n", qaDataUnit, aSequence, 
//					aSequence.getSequenceDataUnit());
			aSequence.getSequenceDataUnit().addSubDetection(qaDataUnit);
			aSequence.getSequenceDataUnit().setLastUpdateTime(qaDataUnit.getTimeMilliseconds());
		}
	}

	/**
	 * work out the RL of the sound. 
	 * Assuming there is VERY little padding in the sound, can 
	 * just add up all non-zero bins (chances of a bin being zero
	 * if doubpe precis is V small)
	 * @param ds
	 * @return
	 */
	private double getReceivedLevel(double[] ds) {
		int lastSamp = ds.length-1;
		while (lastSamp > 0 && ds[lastSamp] == 0) {
			lastSamp--;
		}
		int firstSamp = 0;
		while (firstSamp < lastSamp && ds[firstSamp] == 0) {
			firstSamp++;
		}
		if (firstSamp > lastSamp) {
			return -9999;
		}
		double rms = 0;
		for (int i = firstSamp; i <= lastSamp; i++) {
			rms += Math.pow(ds[i], 2);
		}
		rms = Math.sqrt(rms/(lastSamp-firstSamp+1));
		return daqControl.getDaqProcess().rawAmplitude2dB(rms, 0, false);
//		return 0;
	}

	/**
	 * work through the list of generated sounds and add them to 
	 * the raw data unit. 
	 * @param rawDataUnit Raw data unit. 
	 */
	private void addGeneratedSounds(RawDataUnit rawDataUnit) {
		synchronized (getSoundsDataBlock().getSynchLock()) {
			ListIterator<QASoundDataUnit> it = getSoundsDataBlock().getListIterator(0);
			while (it.hasNext()) {
				QASoundDataUnit du = it.next();
				if (du.getState() == QASoundDataUnit.SOUND_COMPLETE) {
//					it.remove(); // remove complete sounds. Don't or they won't be availeble for matching. 
				}
				else {
					addGeneratedSound(rawDataUnit, du);
				}
			}
		}
	}


	/**
	 * Adds a generated sound to the raw data stream. 
	 * @param rawDataUnit Raw data unit
	 * @param du QA Data Unit
	 */
	private void addGeneratedSound(RawDataUnit rawDataUnit, QASoundDataUnit du) {
		QASound sound = du.getStandardSound();
		long r1 = rawDataUnit.getStartSample();
		long r2 = rawDataUnit.getLastSample();
		int duState = du.getState();
		if (duState == QASoundDataUnit.SOUND_COMPLETE) {
			return;
		}
		else if (duState == QASoundDataUnit.SOUND_NOT_STARTED) {
			if (du.getFirstSample() <= r2) {
				du.setState(QASoundDataUnit.SOUND_GENERATING);
			}
			else {
				return;
			}
		}
		else if (du.getLastSample() < r1) {
			du.setState(QASoundDataUnit.SOUND_COMPLETE);
			return;
		}
		int iChan = PamUtils.getSingleChannel(rawDataUnit.getChannelBitmap());
		long[] firstSamples = sound.getFirstSamples();
		double[] rawWav = rawDataUnit.getRawData();
		double[][] genWav = sound.getWave();
		int startOffset = (int) (firstSamples[iChan] - r1); //s1-r1
		int rawFirst = (int) Math.max(startOffset, 0); // looks correct
		int endOffset = (int) (firstSamples[iChan] + genWav[iChan].length - r1);
		long rawLast = Math.min(rawDataUnit.getSampleDuration(), endOffset);
		int g0 = Math.max(0, -startOffset);
		for (int i = rawFirst, j = g0; i < rawLast; i++, j++) {
			//			try {
			rawWav[i] += genWav[iChan][j];
			//			}
			//			catch (IndexOutOfBoundsException e) {
			//				System.out.printf("r1 %d, L %d, s1 %d, LG %d, i=%d, j=%d\n", r1, r2-r1, firstSamples[iChan], 
			//						genWav[iChan].length, i, j);
			//			}
		}
		rawDataUnit.setRawData(rawDataUnit.getRawData(), true);
	}

	//	/**
	//	 * Test generate of a new sound once per seconds. 
	//	 * @param startSample
	//	 */
	//	private void testGenerate(long startSample) {
	//		GeneratorDataUnit genDataUnit = generatorDataBlock.getFirstUnit();
	//		long now = this.absSamplesToMilliseconds(startSample);
	//		if (genDataUnit == null) {
	//			LatLong latLong = ArrayManager.getArrayManager().getCurrentArray().getHydrophoneLocator().getReferenceLatLong(now);
	////			latLong = latLong.addDistanceMeters(100, 00, -0);
	////			QASoundGenerator standardGen = new StandardSoundGenerator(latLong);
	//			QASoundGenerator standardGen = new WhistleGenerator(latLong);
	//			genDataUnit = new GeneratorDataUnit(now, standardGen);
	//			generatorDataBlock.addPamData(genDataUnit);
	//		}
	//		ListIterator<GeneratorDataUnit> genIt = generatorDataBlock.getListIterator(0);
	//		while (genIt.hasNext()) {
	//			GeneratorDataUnit generatorDU = genIt.next();
	//			generatorDU.getSoundGenerator().setLatLong(generatorDU.getSoundGenerator().getLatLong().addDistanceMeters(100,0,0));
	//			generateSound(generatorDU.getSoundGenerator(), now);
	//		}
	//	}

	/**
	 * @return the spreadingModel
	 */
	public PropagationModel getSpreadingModel() {
		return spreadingModel;
	}

	/**
	 * @param spreadingModel the spreadingModel to set
	 */
	public void setSpreadingModel(PropagationModel spreadingModel) {
		this.spreadingModel = spreadingModel;
	}

	/**
	 * Get a reference position for the tests. This will ideally be the centre of a mitigation 
	 * zone, but if there isn't one, use the centre of the hydrophone array. 
	 * @param sampleNumber Sample number
	 * @return a reference position for test sound generation. 
	 */
	private LatLong getReferenceLocation(long sampleNumber) {
		/*
		 * First try to find an airgun module
		 */

		/*
		 * If that didn't work, just use the centre of the array. 
		 */
		long now = absSamplesToMilliseconds(sampleNumber);
//		LatLong latLong = ArrayManager.getArrayManager().getCurrentArray().getHydrophoneLocator().getReferenceLatLong(now);
//		if (latLong != null) {
//			return latLong;
//		}
//		/**
//		 * Otherwise return the system master reference point. 
//		 */
//		LatLong masterPoint = MasterReferencePoint.getLatLong();
//		return masterPoint;
		PositionReference ref = positionReferenceFinder.findPreferredReference();
		if (ref == null) {
			return null;
		}
		return ref.getReferencePosition(now);
	}

	/**
	 * Called to cancel a test. 
	 * @param dataUnit
	 */
	public void cancelTest(QATestDataUnit dataUnit) {
		if (dataUnit == null) {
			return;
		}
		QATestSet testSet = dataUnit.getQaTestSet();
		if (QATestSet.STATUS_ACTIVE.equals(testSet) == false) {
			testSet.cancelTest();
			// no need to update block since it will come up as finished, so will get saved then...
//			getTestsDataBlock().updatePamData(dataUnit, PamCalendar.getTimeInMillis());
		}
//		removeTest(testSet);
//		qaControl.tellNotifyables(QANotifyable.TEST_END, testSet);
	}
	
//	/**
//	 * Called when a test is completed to remove from the current list. 
//	 * @param testSet
//	 */
//	public void removeTest(QATestSet testSet) {
//		synchronized (currentTests) {
//			currentTests.remove(testSet);
//		}
//	}

	/**
	 * @return the currentSample
	 */
	public long getCurrentSample() {
		return currentSample;
	}

	/**
	 * 
	 * @return Get the number of currently active sequences. 
	 */
	public int getNumCurrentSequences() {
		return currentSequences.size();
	}

	/**
	 * called when range factor has changed - need to update all random tests. 
	 */
	public void notifyNewRangeFactor() {
		synchronized (currentTests) {
			for (QATestSet aTest:currentTests) {
				/*
				 * Find cluster params, find nom range, make new limits
				 * this will then affect the next generated distance. 
				 */
				ClusterParameters clusterParams = qaControl.getQaParameters().getClusterParameters(aTest.getQaCluster());
				double[] rangeLimits = qaControl.getRangeLimits(clusterParams.monitorRange);
				aTest.getLocationGenerator().setRangeLimits(rangeLimits);
			}
		}
	}

}
