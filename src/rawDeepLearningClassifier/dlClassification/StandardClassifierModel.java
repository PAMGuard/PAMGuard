package rawDeepLearningClassifier.dlClassification;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.FileUtils;

import PamController.PamSettings;
import PamDetection.RawDataUnit;
import PamUtils.PamArrayUtils;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;
import javafx.stage.FileChooser.ExtensionFilter;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.DLStatus;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.dlClassification.genericModel.DLModelWorker;
import rawDeepLearningClassifier.dlClassification.genericModel.GenericDLClassifier;
import rawDeepLearningClassifier.dlClassification.genericModel.StandardPrediction;
import rawDeepLearningClassifier.layoutFX.DLSettingsPane;
import rawDeepLearningClassifier.segmenter.GroupedRawData;
import warnings.PamWarning;
import warnings.WarningSystem;

/**
 * A useful abstract class for standard models which are a file or URL that is loaded, have a UI and 
 * utilise PAMSettings to save settings state. These models only accept raw sound data segments. 
 */
public abstract class StandardClassifierModel implements DLClassiferModel, PamSettings {
	
	
	protected DLControl dlControl;
	
	/**
	 * True to force the classifier to use a queue - used for simulating real time operation. 
	 */
	private boolean forceQueue = false; 
	
	/**
	 * The  worker thread has a buffer so that Standard models can be run
	 * in real time without slowing down the rest of PAMGaurd. 
	 */
	private TaskThread workerThread;
	
	/**
	 * Makes a binary decision on whether a prediction result should go on
	 * to be part of a data unit. 
	 */
	private SimpleDLDecision simpleDLDecision = new SimpleDLDecision();


	public StandardClassifierModel(DLControl dlControl) {
		this.dlControl=dlControl; 
	}
	
	/**
	 * Sound spot warning. 
	 */
	PamWarning dlClassifierWarning = new PamWarning(getName(), "",2);
	
	
	@Override
	@SuppressWarnings("rawtypes")
	public ArrayList<? extends PredictionResult> runModel( ArrayList<? extends PamDataUnit> groupedRawData) {
		if (getDLWorker().isModelNull()) return null; 

		//		System.out.println("SoundSpotClassifier: PamCalendar.isSoundFile(): " 
		//		+ PamCalendar.isSoundFile() + "   " + (PamCalendar.isSoundFile() && !forceQueue));
		

		
		/**
		 * If a sound file is being analysed then classifier can go as slow as it wants. if used in real time
		 * then there is a buffer with a maximum queue size. 
		 */
		if ((PamCalendar.isSoundFile() && !forceQueue) || dlControl.isViewer()) {
			//run the model 
			@SuppressWarnings("unchecked")
			ArrayList<StandardPrediction> modelResult = (ArrayList<StandardPrediction>) getDLWorker().runModel(groupedRawData, 
					groupedRawData.get(0).getParentDataBlock().getSampleRate(), 0); 
			
			if (modelResult==null) {
				dlClassifierWarning.setWarningMessage(getName() + " deep learning model returned null");
				WarningSystem.getWarningSystem().addWarning(dlClassifierWarning);
				return null;
			}
			
			for (int i =0; i<modelResult.size(); i++) {
				modelResult.get(i).setClassNameID(GenericDLClassifier.getClassNameIDs(getDLParams())); 
				modelResult.get(i).setBinaryClassification(isDecision(modelResult.get(i), getDLParams())); 
				modelResult.get(i).setTimeMillis(groupedRawData.get(i).getTimeMilliseconds());

			}

			return modelResult; //returns to the classifier. 
		}
		else {
			//REAL TIME
			//add to a buffer if in real time. 
			if (workerThread.getQueue().size()>DLModelWorker.MAX_QUEUE_SIZE) {
				//we are not doing well - clear the buffer
				workerThread.getQueue().clear();
			}
			workerThread.getQueue().add(groupedRawData);
		}
		return null;
	}
	
	
	@Override
	public void prepModel() {
//		System.out.println("STANDARD CLASSIFIER MODEL PREP MODEL! !!!: " +  getDLParams().modelPath);
//		StandardModelParams oldParams = getDLParams().clone();
		
		//just incase group detections has been enabled
		getDLControl().setGroupDetections(false);
		
		getDLWorker().prepModel(getDLParams(), dlControl);


		if (getDLWorker().isModelNull()) {
			dlClassifierWarning.setWarningMessage("There is no loaded " + getName() + " classifier model. " + getName() + " disabled.");
			WarningSystem.getWarningSystem().addWarning(dlClassifierWarning);
			return;
		}


		if ((!PamCalendar.isSoundFile() || forceQueue) && !dlControl.isViewer()) {
			//for real time only
			if (workerThread!=null) {
				workerThread.stopTaskThread();
			}

			workerThread = new TaskThread(getDLWorker());
			workerThread.setPriority(Thread.MAX_PRIORITY);
			workerThread.start();
		}
	}
	
	
	/**
	 * Get the sound spot worker. 
	 * @return the sound spot worker. 
	 */
	public abstract  DLModelWorker<? extends PredictionResult> getDLWorker(); 
	
	/**
	 * Get the sound spot worker. 
	 * @return the sound spot worker. 
	 */
	public abstract  StandardModelParams getDLParams(); 
	
	
	public DLControl getDLControl() {
		return dlControl;
	}

	
	@Override
	public int getNumClasses() {
		return this.getDLParams().numClasses;
	}

	@Override
	public DLClassName[] getClassNames() {
		return getDLParams().classNames;
	}
	
	@Override
	public DLStatus getModelStatus() {
		if (getDLWorker().isModelNull()) {
			return DLStatus.MODEL_LOAD_FAILED;
		}

		File file = new File(getDLParams().modelPath);
		if (getDLParams().modelPath == null || !file.isFile()) {
			return DLStatus.NO_MODEL_LOADED;
		}

		// if continuous data is selected and all classes are false then this is a
		// potential mistake...
		if (dlControl.getSegmenter().getParentDataBlock()!=null) {
			if (dlControl.getSegmenter().getParentDataBlock().getUnitClass() == RawDataUnit.class
					&& (getDLParams().binaryClassification==null || PamArrayUtils.isAllFalse(getDLParams().binaryClassification))){
				return DLStatus.NO_BINARY_CLASSIFICATION;
				//			warnings.add(new PamWarning("Generic classifier",
				//					"There are no prediction classes selected for classification. "
				//							+ "Predicitons for each segment will be saved but there will be no detections generated",
				//					1));
			}
		}
		return DLStatus.MODEL_LOAD_SUCCESS;
	}
	
	@Override
	public DLStatus setModel(URI uri) {
		//will change the params if we do not clone. 
		StandardModelParams.setModel(uri, this.getDLParams()); 
		this.getDLWorker().prepModel(getDLParams(), dlControl);
		return getModelStatus();
	}
	
	

	/**
	 * The task thread. 
	 * @author Jamie Macaulay 
	 *
	 */
	public class TaskThread extends DLTaskThread {

		public TaskThread(DLModelWorker soundSpotWorker) {
			super(soundSpotWorker);
		}

		@Override
		public void newDLResult(StandardPrediction soundSpotResult, PamDataUnit groupedRawData) {
			soundSpotResult.setClassNameID(getClassNameIDs(getDLParams())); 
			soundSpotResult.setBinaryClassification(isDecision(soundSpotResult, getDLParams())); 
			newResult(soundSpotResult, groupedRawData);
		}

	}
	
	/**
	 * Make a decision on whether a result passed a decision 
	 * @param modelResult - the model result.
	 * @param modelParmas - the model parameters. 
	 * @return true if a threshold has been met. 
	 */
	public boolean isDecision(StandardPrediction modelResult, StandardModelParams modelParmas) {
		simpleDLDecision.setParams(modelParmas);
		return simpleDLDecision.isBinaryResult(modelResult);
	}

	
	
	/**
	 * Get the class name IDs
	 * @return an array of class name IDs
	 */ 
	public static short[] getClassNameIDs(StandardModelParams standardModelParams) {
		if (standardModelParams.classNames==null || standardModelParams.classNames.length<=0) return null; 
		short[] nameIDs = new short[standardModelParams.classNames.length]; 
		for (int i = 0 ; i<standardModelParams.classNames.length; i++) {
			nameIDs[i] = standardModelParams.classNames[i].ID; 
		}
		return nameIDs; 
	}



//	/**
//	 * Check whether a model passes a binary test...
//	 * @param modelResult - the model results
//	 * @return the model results. 
//	 */
//	public static boolean isBinaryResult(StandardPrediction modelResult, StandardModelParams genericModelParams) {
//		for (int i=0; i<modelResult.getPrediction().length; i++) {
//						//System.out.println("Binary Classification: "  + genericModelParams.binaryClassification.length); 
//
//			if (modelResult.getPrediction()[i]>genericModelParams.threshold && genericModelParams.binaryClassification[i]) {
//				//				System.out.println("SoundSpotClassifier: prediciton: " + i + " passed threshold with val: " + modelResult.getPrediction()[i]); 
//				return true; 
//			}
//		}
//		return  false;
//	}
	
	
	@Override
	public void closeModel() {
		getDLWorker().closeModel();
	}
	
	
	/**
	 * Send a new result form the thread queue to the process. 
	 * @param modelResult - the model result;
	 * @param groupedRawData - the grouped raw data. 
	 */
	protected void newResult(StandardPrediction modelResult, PamDataUnit groupedRawData) {
		if (groupedRawData instanceof GroupedRawData) {
			this.dlControl.getDLClassifyProcess().newRawModelResult(modelResult, (GroupedRawData) groupedRawData);
		}
	}
//	
//	@Override
//	public ArrayList<PamWarning> checkSettingsOK() {
//		return checkSettingsOK(getDLParams(), dlControl); 
//	}
	
	
	/**
	 * Get the number of samples for microseconds. Based on the sample rate of the parent data block. 
	 */
	public double millis2Samples(double millis) {
		//System.out.println("Samplerate: " + this.dlControl.getSegmenter().getSampleRate() ); 
		return millis*this.dlControl.getSegmenter().getSampleRate()/1000.0;
	}
	

	/**
	 * Get raw settings pane
	 * @return the setting pane. 
	 */
	public DLSettingsPane getRawSettingsPane() {
		return this.dlControl.getSettingsPane();
	}
	
	
	public boolean isModelExtensions(URI uri){
//		System.out.println("Check: " + getName() + " extensions"); 
		String url;
		try {
			url = uri.toURL().getPath();

			if (url.contains(".")) {
				String extension = FileUtils.getExtension(url); 
//				System.out.println("Check: " + getName() + "  file extension " + extension); 

				List<ExtensionFilter> pExtensions = this.getModelUI().getModelFileExtensions(); 
				for (ExtensionFilter extF : pExtensions) {
					for (String ext : extF.getExtensions()) {
//						System.out.println(getName() + " Extensions: " + ext + "  " + extension + "  " + ext.equals(extension.substring(2).trim()) + "  " + extension.substring(2).trim()); 
					if (extension.equals(ext.substring(2))) {
						return true; 
					}
					}
				}
				
			}
			return false;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	
	@Deprecated 
	public static ArrayList<PamWarning> checkSettingsOK(StandardModelParams genericModelParams, DLControl dlControl) {

		// TODO - check if model is null.
		// check that classifier is selected if continous files.
		//
		ArrayList<PamWarning> warnings = new ArrayList<PamWarning>();

		File file = new File(genericModelParams.modelPath);
		if (genericModelParams.modelPath == null || !file.isFile()) {
			warnings.add(
					new PamWarning("Generic classifier", "There is no model loaded - the classifier will not run", 2));
			// if no model then this is ionly message needed for the generic classifier.
			return warnings;
		}

		// if continous data is selected and all classes are false then this is a
		// potential mistake...
		if (dlControl.getSettingsPane().getSelectedParentDataBlock().getUnitClass() == RawDataUnit.class
				&& PamArrayUtils.isAllFalse(genericModelParams.binaryClassification)) {
			warnings.add(new PamWarning("Generic classifier",
					"There are no prediction classes selected for classification. "
							+ "Predicitons for each segment will be saved but there will be no detections generated",
					1));
		}

		return warnings;

	}
	
	@Override
	public ArrayList<Class> getAllowedDataTypes(){
		//null means default data types which is anything with raw data. 
		return null;
	}





}
