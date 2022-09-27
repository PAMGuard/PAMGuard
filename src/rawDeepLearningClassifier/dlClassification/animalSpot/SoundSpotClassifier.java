package rawDeepLearningClassifier.dlClassification.animalSpot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.PamCalendar;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.DLClassName;
import rawDeepLearningClassifier.dlClassification.DLClassiferModel;
import rawDeepLearningClassifier.dlClassification.DLTaskThread;
import rawDeepLearningClassifier.dlClassification.genericModel.DLModelWorker;
import rawDeepLearningClassifier.dlClassification.genericModel.GenericDLClassifier;
import rawDeepLearningClassifier.dlClassification.genericModel.GenericPrediction;
import rawDeepLearningClassifier.layoutFX.DLCLassiferModelUI;
import rawDeepLearningClassifier.layoutFX.RawDLSettingsPane;
import rawDeepLearningClassifier.segmenter.SegmenterProcess.GroupedRawData;
import warnings.PamWarning;
import warnings.WarningSystem;

/**
 * A deep learning classifier running models wihc have been created using the
 * AnimalSpot framework .
 * <p>
 * AnimalSpot uses Pytorch models which contains a JSON string for the
 * transforms Users can select a .py file - PAMGaurd will open it, find the
 * embedded JSON string and set up the transforms.
 * <p>
 * <Previous comments on why to use native Java rather than calling python> This
 * method has numerous advantages, it greatly simplifies the setup, which
 * requires only that the pytorch TorchScript library is installed and that the
 * library location is added to the virtual machine arguments e.g.
 * -Djava.library.path=/Users/au671271/libtorch/lib
 * <p>
 * It also means that np python code is called which greatly increases speed.
 * 
 * @author JamieMacaulay
 *
 */
public class SoundSpotClassifier implements DLClassiferModel, PamSettings {

	/**
	 * Reference to the control.
	 */
	private DLControl dlControl;

	/**
	 * The user interface for sound spot. 
	 */
	private SoundSpotUI soundSpotUI; 

	/**
	 * Sound spot parameters. 
	 */
	private StandardModelParams soundSpotParmas;


	/**
	 * The deep learning model worker.
	 */
	private SoundSpotWorker soundSpotWorker;

	/**
	 * True to force the classifier to use a queue - used for simulating real time operation. 
	 */
	private boolean forceQueue = false; 

	/**
	 * Sound spot warning. 
	 */
	PamWarning soundSpotWarning = new PamWarning("AnimalSpot_Classifier", "",2);

	/**
	 * Runs the deep leanring on a different thread with a data buffer. For real time only. 
	 */
	private DLTaskThread workerThread; 

	public SoundSpotClassifier(DLControl dlControl) {
		this.dlControl=dlControl; 
		this.soundSpotParmas = new StandardModelParams(); 
		this.soundSpotUI= new SoundSpotUI(this); 
		//load the previous settings
		PamSettingManager.getInstance().registerSettings(this);
		
		System.out.println("LOADED CLASS NAMES: currParams.classNames: " + soundSpotParmas.classNames); 

	}


	@Override
	public ArrayList<SoundSpotResult> runModel(ArrayList<GroupedRawData> groupedRawData) {

		//		System.out.println("SoundSpotClassifier: PamCalendar.isSoundFile(): " 
		//		+ PamCalendar.isSoundFile() + "   " + (PamCalendar.isSoundFile() && !forceQueue));
		/**
		 * If a sound file is being analysed then SoundSpot can go as slow as it wants. if used in real time
		 * then there is a buffer with a maximum queue size. 
		 */
		if ((PamCalendar.isSoundFile() && !forceQueue) || dlControl.isViewer()) {
			//run the model 
			ArrayList<SoundSpotResult> modelResult = getSoundSpotWorker().runModel(groupedRawData, 
					groupedRawData.get(0).getParentDataBlock().getSampleRate(), 0); 

			for (int i =0; i<modelResult.size(); i++) {
				modelResult.get(i).setClassNameID(getClassNameIDs()); 
				modelResult.get(i).setBinaryClassification(isBinaryResult(modelResult.get(i))); 
			}

			return modelResult; //returns to the classifier. 
		}
		else {
			//add to a buffer if in real time. 
			if (workerThread.getQueue().size()>DLModelWorker.MAX_QUEUE_SIZE) {
				//we are not doing well - clear the buffer
				workerThread.getQueue().clear();
			}
			workerThread.getQueue().add(groupedRawData);
		}
		return null;
	}

	/**
	 * Check whether a model passes a binary test...
	 * @param modelResult - the model results
	 * @return the model results. 
	 */
	private boolean isBinaryResult(GenericPrediction modelResult) {
		for (int i=0; i<modelResult.getPrediction().length; i++) {
			if (modelResult.getPrediction()[i]>soundSpotParmas.threshold && soundSpotParmas.binaryClassification[i]) {
				//System.out.println("SoundSpotClassifier: prediciton: " + i + " passed threshold with val: " + modelResult.getPrediction()[i]); 
				return true; 
			}
		}
		return  false;
	}

	/**
	 * Get the sound spot worker. 
	 * @return the sound spot worker. 
	 */
	SoundSpotWorker getSoundSpotWorker() {
		if (soundSpotWorker==null) {
			soundSpotWorker = new SoundSpotWorker(); 
		}
		return soundSpotWorker; 
	}


	/**
	 * The task thread. 
	 * @author Jamie Macaulay 
	 *
	 */
	public class TaskThread extends DLTaskThread {

		TaskThread(DLModelWorker soundSpotWorker) {
			super(soundSpotWorker);
		}

		@Override
		public void newResult(GenericPrediction soundSpotResult, GroupedRawData groupedRawData) {
			soundSpotResult.setClassNameID(getClassNameIDs()); 
			soundSpotResult.setBinaryClassification(isBinaryResult(soundSpotResult)); 
			newModelResult(soundSpotResult, groupedRawData);
		}

	}


	//	public class TaskThread extends Thread {
	//
	//		private AtomicBoolean run = new AtomicBoolean(true);
	//
	//		TaskThread() {
	//			super("TaskThread");
	//			if (soundSpotWorker == null) {
	//				//create the daemons etc...
	//				soundSpotWorker = new SoundSpotWorker(); 	
	//			}
	//		}
	//
	//		public void stopTaskThread() {
	//			run.set(false);  
	//			//Clean up daemon.
	//			if (soundSpotWorker!=null) {
	//				soundSpotWorker.closeModel();
	//			}
	//			soundSpotWorker = null; 
	//		}
	//
	//		public void run() {
	//			while (run.get()) {
	//				//				System.out.println("ORCASPOT THREAD while: " + "The queue size is " + queue.size()); 
	//				try {
	//					if (queue.size()>0) {
	//						//						System.out.println("ORCASPOT THREAD: " + "The queue size is " + queue.size()); 
	//						ArrayList<GroupedRawData> groupedRawData = queue.remove(0);
	//
	//						ArrayList<SoundSpotResult> modelResult = getSoundSpotWorker().runModel(groupedRawData, 
	//								groupedRawData.get(0).getParentDataBlock().getSampleRate(), 0); //TODO channel?
	//
	//						for (int i =0; i<modelResult.size(); i++) {
	//							modelResult.get(i).setClassNameID(getClassNameIDs()); 
	//							modelResult.get(i).setBinaryClassification(isBinaryResult(modelResult.get(i))); 
	//							newResult(modelResult.get(i), groupedRawData.get(i));
	//						}
	//
	//					}
	//					else {
	//						//						System.out.println("ORCASPOT THREAD SLEEP: "); ; 
	//						Thread.sleep(10);
	//						//						System.out.println("ORCASPOT THREAD DONE: "); ; 
	//					}
	//
	//				} catch (Exception e) {
	//					e.printStackTrace();
	//				}
	//			}
	//		}
	//
	//	}

	/**
	 * Get the class name IDs
	 * @return an array of class name IDs
	 */
	private short[] getClassNameIDs() {
		if (soundSpotParmas.classNames==null || soundSpotParmas.classNames.length<=0) return null; 
		short[] nameIDs = new short[soundSpotParmas.classNames.length]; 
		for (int i = 0 ; i<soundSpotParmas.classNames.length; i++) {
			nameIDs[i] = soundSpotParmas.classNames[i].ID; 
		}
		return nameIDs; 
	}

	/**
	 * Send a new result form the thread queue to the process. 
	 * @param modelResult - the model result;
	 * @param groupedRawData - the grouped raw data. 
	 */
	private void newModelResult(GenericPrediction modelResult, GroupedRawData groupedRawData) {
		this.dlControl.getDLClassifyProcess().newModelResult(modelResult, groupedRawData);
	}


	@Override
	public void prepModel() {
		//System.out.println("PrepModel! !!!");
		StandardModelParams oldParams = soundSpotParmas.clone();
		getSoundSpotWorker().prepModel(soundSpotParmas, dlControl);


		if (getSoundSpotWorker().getModel()==null) {
			soundSpotWarning.setWarningMessage("There is no loaded classifier model. AnimalSpot disabled.");
			WarningSystem.getWarningSystem().addWarning(soundSpotWarning);
		}


		if ((!PamCalendar.isSoundFile() || forceQueue) && !dlControl.isViewer()) {
			//for real time only
			if (workerThread!=null) {
				workerThread.stopTaskThread();
			}
			
			workerThread = new TaskThread(getSoundSpotWorker());
			workerThread.setPriority(Thread.MAX_PRIORITY);
			workerThread.start();
		}
	}

	@Override
	public void closeModel() {


	}

	@Override
	public String getName() {
		return "AnimalSpot";
	}

	@Override
	public DLCLassiferModelUI getModelUI() {
		return soundSpotUI;
	}

	@Override
	public Serializable getDLModelSettings() {
		return soundSpotParmas;
	}

	@Override
	public String getUnitName() {
		return dlControl.getUnitName()+"_SoundSpot"; 
	}

	@Override
	public String getUnitType() {
		return dlControl.getUnitType()+"_SoundSpot";
	}

	@Override
	public Serializable getSettingsReference() {
		if (soundSpotParmas==null) {
			soundSpotParmas = new StandardModelParams(); 
		}
		//System.out.println("SoundSpot have been saved. : " + soundSpotParmas.classNames); 
		return soundSpotParmas;

	}

	@Override
	public long getSettingsVersion() {
		return StandardModelParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		StandardModelParams newParameters = (StandardModelParams) pamControlledUnitSettings.getSettings();
		if (newParameters!=null) {
			soundSpotParmas = newParameters.clone();
		//System.out.println("SoundSpot have been restored. : " + soundSpotParmas.classNames); 
		}
		else soundSpotParmas = new StandardModelParams(); 
		return true;
	}

	/**
	 * Get the sound spot parameters. 
	 * @return sound spot parameters. 
	 */
	public StandardModelParams getSoundSpotParams() {
		return soundSpotParmas;
	}

	/**
	 * Set the sound spot parameters. 
	 * @param the params to set 
	 */
	public void setSoundSpotParams(StandardModelParams soundSpotParmas) {
		this.soundSpotParmas=soundSpotParmas; 

	}

	/**
	 * Get raw settings pane
	 * @return the setting pane. 
	 */
	public RawDLSettingsPane getRawSettingsPane() {
		return this.dlControl.getSettingsPane();
	}

	/**
	 * Get the number of samples for microseconds. Based on the sample rate of the parent data block. 
	 */
	public double millis2Samples(double millis) {
		//System.out.println("Samplerate: " + this.dlControl.getSegmenter().getSampleRate() ); 
		return millis*this.dlControl.getSegmenter().getSampleRate()/1000.0;
	}

	@Override
	public int getNumClasses() {
		return this.soundSpotParmas.numClasses;
	}

	@Override
	public DLClassName[] getClassNames() {
		return soundSpotParmas.classNames;
	}


	public DLControl getDLControl() {
		return dlControl;
	}


	@Override
	public boolean checkModelOK() {
		return getSoundSpotWorker().getModel()!=null;
	}


	@Override
	public ArrayList<PamWarning> checkSettingsOK() {
		return GenericDLClassifier.checkSettingsOK(soundSpotParmas, dlControl); 
	}


}
