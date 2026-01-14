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

	private static final int OFFLINE_FILE_QUEUE_SIZE = 2; //don't let the deep elarning get very far behind at all with a sound file.


	protected DLControl dlControl;

	/**
	 * True to force the classifier to use a queue - used for simulating real time operation. 
	 */
	private boolean forceQueue = false; 

	/**
	 * The  worker thread has a buffer so that Standard models can be run
	 * in real time without slowing down the rest of PAMGaurd. 
	 */
	private StandardDLTaskThread workerThread;

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

	private DLStatus status = DLStatus.NO_MODEL_LOADED;


	@Override
	@SuppressWarnings("rawtypes")
	public ArrayList<ArrayList<? extends PredictionResult>> runModel( ArrayList<? extends PamDataUnit> groupedRawData) {
		if (getDLWorker().isModelNull()) return null; 

		//		System.out.println("SoundSpotClassifier: PamCalendar.isSoundFile(): " 
		//		+ PamCalendar.isSoundFile() + "   " + (PamCalendar.isSoundFile() && !forceQueue));



		/**
		 * If a sound file is being analysed then classifier can go as slow as it wants. If used in real time
		 * then there is a buffer with a maximum queue size. 
		 */
		if (dlControl.isViewer() || dlControl.isGroupDetections()) {
			
//			System.out.println("----StandardClassifierModel: Running model in viewer or group detections mode.");
			//run the model 
			@SuppressWarnings("unchecked")
			List<StandardPrediction> modelResult = (List<StandardPrediction>) getDLWorker().runModel(groupedRawData, 
					groupedRawData.get(0).getParentDataBlock().getSampleRate(), 0); 

			if (modelResult==null) {
				dlClassifierWarning.setWarningMessage(getName() + " deep learning model returned null");
				WarningSystem.getWarningSystem().addWarning(dlClassifierWarning);
				return null;
			}

			//add time stamps and class names to the model results.
			ArrayList<ArrayList<? extends PredictionResult>> modelResults = processModelResults(groupedRawData, modelResult);


			return modelResults; //returns to the classifier. 
		}
		else {
			//REAL TIME - when using a sound card. 
			//add to a buffer if in real time. 
			if (workerThread.getQueue().size()>getMaxQueueSize()) {
				System.out.println(getName() + " deep learning model queue size exceeded: " + workerThread.getQueue().size());

				if (PamCalendar.isSoundFile() && !forceQueue) {
					//we are analysing a sound file so just wait until the queue has space. Note that we
					//could just put sound data in real time model into the viewer if satement above but
					//this results in a very "jerky" spectrogram - putting on a thread means the processing can take place 
					//without stopping the spectrogram updating every xx seconds if the user sets the processing speed to be
					//slower than the model takes to run. 
					int count =0 ; 
					while (workerThread.getQueue().size()>getMaxQueueSize()) {
						try {
							Thread.sleep(100);
							count++;
							if (count%50==0) {
								System.out.println(getName() + " deep learning model waiting for queue to clear...");
							}
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				else {
					//we are not doing well - clear the buffer
					workerThread.getQueue().clear();
					dlClassifierWarning.setWarningMessage(getName() + " deep learning model queue overloaded - results are being dropped");
				}
			}
			workerThread.getQueue().add(groupedRawData);
		}
		return null;
		
		
//		
//		/**
//		 * If a sound file is being analysed then classifier can go as slow as it wants. if used in real time
//		 * then there is a buffer with a maximum queue size. 
//		 */
//		if ((PamCalendar.isSoundFile() && !forceQueue) || dlControl.isViewer()) {
//			//run the model 
//			@SuppressWarnings("unchecked")
//			List<StandardPrediction> modelResult = (List<StandardPrediction>) getDLWorker().runModel(groupedRawData, 
//					groupedRawData.get(0).getParentDataBlock().getSampleRate(), 0); 
//			
//			if (modelResult==null) {
//				dlClassifierWarning.setWarningMessage(getName() + " deep learning model returned null");
//				WarningSystem.getWarningSystem().addWarning(dlClassifierWarning);
//				return null;
//			}
//			
//			//add time stamps and class names to the model results.
//			ArrayList<ArrayList<? extends PredictionResult>> modelResults = processModelResults(groupedRawData, modelResult);
//			
//	
//			return modelResults; //returns to the classifier. 
//		}
//		else {
//			//REAL TIME - when using a sound card. 
//			//add to a buffer if in real time. 
//			if (workerThread.getQueue().size()>DLModelWorker.MAX_QUEUE_SIZE) {
//				//we are not doing well - clear the buffer
//				workerThread.getQueue().clear();
//			}
//			workerThread.getQueue().add(groupedRawData);
//		}
//		return null;
	}
	
	
	/**
	 * Get the maximum queue size which depends on whether we are analysing a sound file or real time data.
	 * @return the maximum queue size. 
	 */
	private int getMaxQueueSize() {
		if (PamCalendar.isSoundFile() && !forceQueue) {
			return OFFLINE_FILE_QUEUE_SIZE;
		}
		else {
			return DLModelWorker.MAX_QUEUE_SIZE; 
		}
	}

	/**
	 * Process the model results - for example to add class names and time stamps.
	 * @param groupedRawData - the grouped raw data used for input data into the model
	 * @param modelResult - the model results. 
	 */
	protected ArrayList<ArrayList<? extends PredictionResult>> processModelResults(ArrayList<? extends PamDataUnit> groupedRawData, List<StandardPrediction> modelResult) {
		for (int i =0; i<modelResult.size(); i++) {
			modelResult.get(i).setClassNameID(GenericDLClassifier.getClassNameIDs(getDLParams())); 
			modelResult.get(i).setBinaryClassification(isDecision(modelResult.get(i), getDLParams())); 
			modelResult.get(i).setTimeMillis(groupedRawData.get(i).getTimeMilliseconds());
			modelResult.get(i).setStartSample(groupedRawData.get(i).getStartSample());
			modelResult.get(i).setDuratioSamples(groupedRawData.get(i).getSampleDurationAsInt());
			//System.out.println("Frequency limits: " + groupedRawData.get(i).getFrequency()[0] + "  " + groupedRawData.get(i).getFrequency()[1]);
			modelResult.get(i).setFreqLimits(new double[] {groupedRawData.get(i).getFrequency()[0], groupedRawData.get(i).getFrequency()[1]});
		}

		//now convert the model results to a list of lists to satisfy the interface - note this assumes a one to one mapping of model results to input data units.
		ArrayList<ArrayList<? extends PredictionResult>> modelResults = new ArrayList<>();
		ArrayList<StandardPrediction> aList;
		for (StandardPrediction aModelResult:modelResult) {
			aList = new ArrayList<>();
			aList.add(aModelResult);
			modelResults.add(aList);
		}

		return modelResults;
	}


	@Override
	public void prepModel() {
		//		System.out.println("STANDARD CLASSIFIER MODEL PREP MODEL! !!!: " +  getDLParams().modelPath);
		//		StandardModelParams oldParams = getDLParams().clone();

		//just incase group detections has been enabled
		getDLControl().setGroupDetections(false);

		
		try {
			getDLWorker().prepModel(getDLParams(), dlControl);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		if (getDLWorker().isModelNull()) {
			dlClassifierWarning.setWarningMessage("There is no loaded " + getName() + " classifier model. " + getName() + " disabled.");
			WarningSystem.getWarningSystem().addWarning(dlClassifierWarning);
			return;
		}


		if (!dlControl.isViewer()) {
			//for real time only
			if (workerThread!=null) {
				workerThread.stopTaskThread();
			}

			workerThread = new StandardDLTaskThread(getDLWorker());
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
		return status;
	}

	@Override
	public DLStatus setModel(URI uri) {
		//will change the params if we do not clone. 
		StandardModelParams.setModel(uri, this.getDLParams()); 
		status = this.getDLWorker().prepModel(getDLParams(), dlControl);

		//		System.out.println("----MODEL STATUS: " + status); 

		status  = checkDLStatus(status);

		return status; 
	}

	/**
	 * The model status is returned by the prep model function but there may be other issues which override the returned status. 
	 * This function chekcs those issues and returns a different status if necessary.
	 * @param status2 - the current model status. 
	 * @return the current model status. 
	 */
	private DLStatus checkDLStatus(DLStatus status2) {


		if (getDLWorker().isModelNull() && !status2.isError()) {
			return DLStatus.MODEL_LOAD_FAIL;
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
		return status2;
	}



	/**
	 * The task thread. 
	 * @author Jamie Macaulay 
	 *
	 */
	public class StandardDLTaskThread extends DLTaskThread {

		public StandardDLTaskThread(DLModelWorker dlWorker) {
			super(dlWorker);
		}

		@Override
		public void newDLResult(ArrayList<StandardPrediction> modelResult,
				ArrayList<? extends PamDataUnit> groupedRawData) {

			if (modelResult == null) {
				dlClassifierWarning.setWarningMessage(getName() + " deep learning model returned null");
				WarningSystem.getWarningSystem().addWarning(dlClassifierWarning);
				return;
			}

			// add time stamps and class names to the model results.

			ArrayList<ArrayList<? extends PredictionResult>> modelResults = processModelResults(groupedRawData,
					modelResult);
			
			//when this thread has been used the runModel function the the DLClassifyProcess returns null so we have to
			//send ensure the results are sent to the correct functions here. 

			if (dlControl.isGroupDetections()) {

				for (int i = 0; i < groupedRawData.size(); i++) {
					if (modelResults != null && modelResults.get(i) != null) {
						// detection groups never have more than one result
						dlControl.getDLClassifyProcess().newDetectionGroupResult(groupedRawData.get(i),
								modelResults.get(i).get(0));
					}
				}
				
			} else {
				// process the raw model results
				dlControl.getDLClassifyProcess().newRawModelResults(modelResults,
						(ArrayList<GroupedRawData>) groupedRawData);
			}

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


	//	/**
	//	 * Send a new result form the thread queue to the process. 
	//	 * @param modelResult - the model result;
	//	 * @param groupedRawData - the grouped raw data. 
	//	 */
	//	protected void newResult(StandardPrediction modelResult, PamDataUnit groupedRawData) {
	//		if (groupedRawData instanceof GroupedRawData) {
	//			this.dlControl.getDLClassifyProcess().newRawModelResult(modelResult, (GroupedRawData) groupedRawData);
	//		}
	//	}
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
