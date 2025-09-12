package whistleClassifier;

import java.io.File;
import java.util.LinkedList;
import java.util.ListIterator;

import Acquisition.AcquisitionProcess;
import Acquisition.FileInputSystem;
import PamController.PamController;
import PamUtils.FileParts;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import classifier.Classifier;
import whistleClassifier.logging.WhistleClassifierLogging;
import whistleClassifier.training.FileTrainingStore;
import whistleClassifier.training.TrainingDataSet;
import whistleClassifier.training.TrainingDataStore;
import whistlesAndMoans.AbstractWhistleDataBlock;
import whistlesAndMoans.AbstractWhistleDataUnit;

/**
 * Whistle Classifier process <p>
 * Receives whistles from either whistle detector. 
 * Fragments them and either stores the fragments for 
 * later training or builds up information on many fragments
 * and then classifies when it's got enough. 
 * <p>
 * Keeps a list or recent whistle times so that it can clear the
 * store if the whistle rate drops too low. 
 * 
 * @author Doug
 *
 */
public class WhistleClassifierProcess extends PamProcess {

	protected WhistleClassifierControl whistleClassifierControl;

	//	private PamDataBlock<ShapeDataUnit> whistleDataBlock;

	private AbstractWhistleDataBlock whistleSourceData;

	//	private Random random = new Random();

	//	private WhistleDetector whistleDetector;

	private HistoFragmentStore fragmentStore;

	protected WhistleClassificationDataBlock whistleClasificationDataBlock;

	private WhistleClassifierLogging whistleClassifierLogging;

	private TrainingDataStore trainingDataStore;

	private TrainingDataSet trainingDataSet;

	/**
	 * Need to keep a list of recent whistle times so that the system can be cleared
	 * if there are no longer any whistles. 
	 */
	private LinkedList<Long> whistleTimes = new LinkedList<Long>();

	public WhistleClassifierProcess(WhistleClassifierControl whistleClassifierControl) {

		super(whistleClassifierControl, null);

		this.whistleClassifierControl  = whistleClassifierControl;

		whistleClasificationDataBlock = new WhistleClassificationDataBlock(whistleClassifierControl, this, 3);

		addOutputDataBlock(getWhistleClasificationDataBlock());

		fragmentStore = new HistoFragmentStore(this.getSampleRate());

		//		fragmentClassifier = new LinearFragmentClassifier(whistleClassifierControl, fragmentStore);


	}

	/**
	 * Called after settings read in so that 
	 * correct table can be created.
	 */
	public void setupLogging() {
		whistleClassifierLogging = new WhistleClassifierLogging(this, getWhistleClasificationDataBlock());
		getWhistleClasificationDataBlock().SetLogging(whistleClassifierLogging);

	}

	/**
	 * Called whenever settings / species list changes to ensure that 
	 * database table columns match the species list in use. 
	 */
	public void checkLoggingTables() {
		if (whistleClassifierLogging == null) {
			return;
		}
		whistleClassifierLogging.checkLoggingTables();
	}

	@Override
	public void setSampleRate(float sampleRate, boolean notify) {
		super.setSampleRate(sampleRate, notify);
		fragmentStore.setSampleRate(sampleRate);
	}

	/**
	 * Flag to say that it's in the process of collecting training data. 
	 * @return true if it's in the process of collecting training data. 
	 */
	public boolean isTraining() {
		return (getWhistleClassifierControl().getWhistleClassificationParameters().operationMode == 
			WhistleClassificationParameters.COLLECT_TRAINING_DATA);
	}

	@Override
	public void pamStart() {
		fragmentStore.prepareStore();
		clearFragmentStore(0);
		if (isTraining()) {
			prepareTrainingStore();
		}
	}

	public void pamHasStopped() {

		if (isTraining()) {
			writeTrainingStoreData();
		}
		closeTrainingStore();

	}

	@Override
	public void pamStop() {

	}

	public void prepareTrainingStore() {
		// need to work out what on earth the 
		// current file name is. 
		File soundFile = null;
		try {
			AcquisitionProcess sourceProcess = (AcquisitionProcess) getSourceProcess();
			Acquisition.DaqSystem daqSystem = sourceProcess.getAcquisitionControl().findDaqSystem(null);
			if (daqSystem != null & !daqSystem.isRealTime()) {
				// assume it's a file name.
				Acquisition.FileInputSystem fileSystem = (FileInputSystem) daqSystem;
				soundFile = fileSystem.getCurrentFile();
			}
		}
		catch (Exception ex) {
			soundFile = null;
		}
		if (soundFile == null) {
			return;
		}
		FileParts fileParts = new FileParts(soundFile);
		String nameBit = fileParts.getFileName();
		prepareTrainingStore(nameBit, fileParts.getLastFolderPart());
	}

	public void prepareTrainingStore(String nameBit, String wavFolder) {
		//		
		//		int pt = nameBit.indexOf('.');
		//		if (pt >= 0) {
		//			nameBit = nameBit.substring(0, pt);
		//		}
		String storeName = 
			String.format("%s\\%s%s",
					getWhistleClassifierControl().getWhistleClassificationParameters().trainingDataFolder, nameBit,
					WhistleClassifierControl.trainingFileEnd);
		//		System.out.println("Open training store " + storeName);
		trainingDataStore = new FileTrainingStore();
		trainingDataStore.openStore(storeName);
		String trainingSpecies = getWhistleClassifierControl().getWhistleClassificationParameters().trainingSpecies;
		if (getWhistleClassifierControl().getWhistleClassificationParameters().wavFolderNameAsSpecies &&
				wavFolder != null) {
			trainingSpecies = wavFolder;
			getWhistleClassifierControl().getWhistleClassificationParameters().trainingSpecies = trainingSpecies;
		}

		trainingDataSet = new TrainingDataSet(whistleSourceData.getUnitClass(),
				trainingSpecies, whistleSourceData.getSampleRate(), 
				whistleSourceData.getFftLength(), whistleSourceData.getFftHop());
	}

	public void writeTrainingStoreData() {
		if (trainingDataStore == null || trainingDataSet == null) {
			return;
		}
		trainingDataStore.writeData(trainingDataSet);
	}

	public void closeTrainingStore() {
		if (trainingDataStore == null) {
			return;
		}
		trainingDataStore.closeStore();
		trainingDataStore = null;
	}
	protected void findSourceData()
	{

		whistleSourceData = (AbstractWhistleDataBlock) PamController.getInstance().
		getDataBlock(AbstractWhistleDataUnit.class, getWhistleClassifierControl().getWhistleClassificationParameters().dataSource);
		if (whistleSourceData == null) {
			return;
		}

		setParentDataBlock(whistleSourceData);

		getWhistleClasificationDataBlock().setChannelMap(whistleSourceData.getChannelMap());
		getWhistleClasificationDataBlock().setSequenceMap(whistleSourceData.getSequenceMapObject());


	}

	@Override
	public void masterClockUpdate(long timeMilliseconds, long sampleNumber) {
		super.masterClockUpdate(timeMilliseconds, sampleNumber);
		runTimingFunctions(timeMilliseconds);
	}

	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		if (o == whistleSourceData) {
			newWhistleData((AbstractWhistleDataUnit) arg);
		}
	}

	private long lastTimingCall = 0;
	private long lastStoreClearTime = 0;
	public void runTimingFunctions(long timeMilliseconds) {
		if (timeMilliseconds - lastTimingCall > 1000) {
			lastTimingCall = timeMilliseconds;
			checkNeedForClear(timeMilliseconds);
		}
	}

	private void checkNeedForClear(long timeMilliseconds) {
		long whileBack = timeMilliseconds - 
		getWhistleClassifierControl().getWhistleClassificationParameters().lowWhistleClearTime * 1000;
		if (lastStoreClearTime == 0) {
			lastStoreClearTime = timeMilliseconds; // happens on first call 1
		}
		if (whileBack < lastStoreClearTime) {
			return;
		}
		synchronized (whistleTimes) {

			ListIterator<Long> it = whistleTimes.listIterator();
			long wt;
			while (it.hasNext()) {
				wt = it.next();
				if (wt < whileBack) {
					it.remove();
				}
				else {
					break;
				}
			}
			int n = whistleTimes.size();
			if (n < getWhistleClassifierControl().getWhistleClassificationParameters().lowWhistleNumber) {
				if (getWhistleClassifierControl().getWhistleClassificationParameters().alwaysClassify) {
					runClassification(timeMilliseconds);
				}
				clearFragmentStore(timeMilliseconds);
//				System.out.println("Clear Whistle Classifier store due to low whistle rate");
			}
		}
	}


	private void newWhistleData(AbstractWhistleDataUnit shapeDataUnit) {
		FragmentClassifierParams fragClassParams = getWhistleClassifierControl().getWhistleClassificationParameters().
				fragmentClassifierParams;
		if (fragClassParams == null) {
			return;
		}
		if (!isTraining() && shapeDataUnit.getSliceCount() <
				fragClassParams.minimumContourLength) {
			return;
		}
		int nFrags = fragmentWhistle(shapeDataUnit);

		if (nFrags == 0) {
			return;
		}
		synchronized (whistleTimes) {
			whistleTimes.add(shapeDataUnit.getTimeMilliseconds());
		}
	}

	/**
	 * @param abstractWhistle
	 * @return the number of fragments extracted and used by the fragmenter.
	 */
	private int fragmentWhistle(AbstractWhistleDataUnit abstractWhistle) {

		if (isTraining() && trainingDataSet != null) {
			trainingDataSet.addContour(abstractWhistle);
			return 0;
		}
		
		
		WhistleFragment[] fragments = getWhistleClassifierControl().getWhistleFragmenter().
		getFragments(abstractWhistle);

		if (fragments == null) {
			return 0;
		}
		int nUsed = 0;
		for (int f = 0; f < fragments.length; f++){
			if (processFragment(fragments[f], abstractWhistle.getTimeMilliseconds())) {
				nUsed++;
			}
		}
		return nUsed;
	}

	private boolean processFragment(WhistleFragment fragment, long time) {

		
		/*
		 * The fragment will have worked out it's main parameters when it was calculated
		 * now need to summarise these over many many fragments in a series of histograms
		 * 
		 * However, it's possible that the fragment is outside teh set frequency limits, 
		 * in which case, don't use it !
		 */
		double[] f = fragment.getFreqsHz();
		double[] fLims = getWhistleClassifierControl().getWhistleClassificationParameters().fragmentClassifierParams.getFrequencyRange();
		for (int i = 0; i < f.length; i++) {
			if (f[i] < fLims[0]) {
				return false;
			}
			if (f[i] > fLims[1]) {
				return false;
			}
		}

		fragmentStore.addFragemnt(fragment, time);

		if (isTraining()) {
			return false;
		}

		FragmentClassifierParams fp = getWhistleClassifierControl().getWhistleClassificationParameters().fragmentClassifierParams;
		if (fp == null) {
			return false;
		}
		if (fragmentStore.getFragmentCount() >= fp.sectionLength) {
			runClassification(time);
			clearFragmentStore(time);
		}
		return true;
	}
	
	/**
	 * Called when reprocessing offline. 
	 */
	public void resetClassifier() {
		clearFragmentStore(0);
		lastTimingCall = 0;
	}

	public void clearFragmentStore(long clearTime) {
		fragmentStore.clearStore();
		whistleTimes.clear();
		lastStoreClearTime = clearTime;
	}

	/**
	 * Called once enough fragments have accumulated. 
	 * Run the classification model. 
	 * @return true if model ran without errors. 
	 */
	public boolean runClassification(long timeMillis) {

		double[] params = fragmentStore.getParameterArray();
		if (params == null) {
			return false;
		}
		Classifier classifier = getWhistleClassifierControl().getFragmentClassifier();
		int species = classifier.runClassification(params);
		String[] speciesList = getWhistleClassifierControl().getWhistleClassificationParameters().fragmentClassifierParams.getSpeciesList();
		String bestSpecies;
		if (species < 0 || species >= speciesList.length) {
			bestSpecies = "??";
		}
		else {
			bestSpecies = speciesList[species];
		}
		double[] speciesProbabilities = classifier.getProbabilities1();

		/** 
		 * Up to March 2025, this was outputting the end time of the classification as the time. It would 
		 * be better if we recorded the start time of the classification (i.e. first fragment) and correctly stored
		 * the duration / end time.  
		 */
		long startMillis = fragmentStore.getStartTimeMillis();
		long endMillis = fragmentStore.getEndTimeMillis();
		WhistleClassificationDataUnit newData = new WhistleClassificationDataUnit(startMillis, getWhistleClasificationDataBlock().getChannelMap(),
				this.absMillisecondsToSamples(startMillis), 0);
		newData.setDurationInMilliseconds(endMillis-startMillis);
		// set the frequency range
		double[] fR = {fragmentStore.getLowFreq(), fragmentStore.getHighFreq()};
		newData.setFrequency(fR);
		
		//		newData.setSpeciesLikelyhoods(fudge);
		newData.setSequenceBitmap(getWhistleClasificationDataBlock().getSequenceMapObject());
		newData.setSpeciesProbabilities(speciesProbabilities);
		newData.setSpecies(bestSpecies);
		newData.setNFragments((int)fragmentStore.getFragmentCount());
		getWhistleClasificationDataBlock().addPamData(newData);


		getWhistleClassifierControl().updateClassification(true);

		return true;
	}

	public FragmentStore getFragmentStore() {
		return fragmentStore;
	}

	/**
	 * @return the whistleClassifierControl
	 */
	public WhistleClassifierControl getWhistleClassifierControl() {
		return whistleClassifierControl;
	}

	/**
	 * @return the whistleClasificationDataBlock
	 */
	public WhistleClassificationDataBlock getWhistleClasificationDataBlock() {
		return whistleClasificationDataBlock;
	}



}
