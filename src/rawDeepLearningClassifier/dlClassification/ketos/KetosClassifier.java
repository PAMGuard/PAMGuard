package rawDeepLearningClassifier.dlClassification.ketos;

import java.io.Serializable;
import java.util.ArrayList;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.PamCalendar;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.DLClassName;
import rawDeepLearningClassifier.dlClassification.DLClassiferModel;
import rawDeepLearningClassifier.dlClassification.DLTaskThread;
import rawDeepLearningClassifier.dlClassification.PredictionResult;
import rawDeepLearningClassifier.dlClassification.genericModel.DLModelWorker;
import rawDeepLearningClassifier.dlClassification.genericModel.GenericDLClassifier;
import rawDeepLearningClassifier.dlClassification.genericModel.GenericPrediction;
import rawDeepLearningClassifier.layoutFX.DLCLassiferModelUI;
import rawDeepLearningClassifier.segmenter.SegmenterProcess.GroupedRawData;
import warnings.PamWarning;
import warnings.WarningSystem;

/**
 * Classifier which uses deep learning models from Meridian's Ketos framework.
 * <p>
 * Ketos uses TensorFlow models and packages them inside a zipped .ktpb file
 * which contains a JSON file for the transforms and a .pb model. Users can
 * select a .ktpb file - PAMGaurd will decompress it, find the JSON file, set up
 * the transforms and load the model.
 * <p>
 * Details on Meridians framework can be found at https://meridian.cs.dal.ca/2015/04/12/ketos/
 * @author Jamie Macaulay
 *
 */
public class KetosClassifier implements DLClassiferModel, PamSettings {

	/**
	 * Reference to the DL contro.. 
	 */
	private DLControl dlControl;

	/**
	 * Paramters for a Ketos classifier. 
	 */
	private KetosDLParams ketosDLParams;

	/**
	 * The UI components of the Ketos classifier
	 */
	private KetosUI ketosUI; 

	/**
	 * The Ketos worker. this handles the heavy lifting such as loading and running
	 * models. 
	 */
	private KetosWorker ketosWorker; 

	/**
	 * True to force the classifier to use a queue - can be used for simulating real time operation. 
	 */
	private boolean forceQueue = false; 

	/**
	 * Sound spot warning. 
	 */
	PamWarning ketosWarning = new PamWarning("Ketos_Classifier", "",2);

	/**
	 * The Ketos worker thread has a buffer so that Ketos models can be run
	 * in real time without dslowing down the rest of PAMGaurd. 
	 */
	private KetosThread workerThread;


	/**
	 * The ketos classifier. 
	 */
	public KetosClassifier(DLControl dlControl) {
		this.dlControl=dlControl; 
		this.ketosDLParams = new KetosDLParams(); 
		this.ketosUI= new KetosUI(this); 
		//load the previous settings
		PamSettingManager.getInstance().registerSettings(this);
	}


	@Override
	public ArrayList<? extends PredictionResult> runModel(ArrayList<GroupedRawData> groupedRawData) {
		if (getKetosWorker().getModel()==null) return null; 

		//		System.out.println("SoundSpotClassifier: PamCalendar.isSoundFile(): " 
		//		+ PamCalendar.isSoundFile() + "   " + (PamCalendar.isSoundFile() && !forceQueue));
		
		/**
		 * If a sound file is being analysed then Ketos can go as slow as it wants. if used in real time
		 * then there is a buffer with a maximum queue size. 
		 */
		if ((PamCalendar.isSoundFile() && !forceQueue) || dlControl.isViewer()) {
			//run the model 
			ArrayList<KetosResult> modelResult = getKetosWorker().runModel(groupedRawData, 
					groupedRawData.get(0).getParentDataBlock().getSampleRate(), 0); 
			
			if (modelResult==null) {
				ketosWarning.setWarningMessage("Generic deep learning model returned null");
				WarningSystem.getWarningSystem().addWarning(ketosWarning);
				return null;
			}
			
			for (int i =0; i<modelResult.size(); i++) {
				modelResult.get(i).setClassNameID(GenericDLClassifier.getClassNameIDs(ketosDLParams)); 
				modelResult.get(i).setBinaryClassification(GenericDLClassifier.isBinaryResult(modelResult.get(i), ketosDLParams)); 
				modelResult.get(i).setTimeMillis(groupedRawData.get(i).getTimeMilliseconds());

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


	@Override
	public void prepModel() {
		//System.out.println("PrepModel! !!!");
		getKetosWorker().prepModel(ketosDLParams, dlControl);
		
		if (!ketosDLParams.useDefaultTransfroms) {
			//set custom transforms in the model. 
			getKetosWorker().setModelTransforms(ketosDLParams.dlTransfroms);
		}

		if (getKetosWorker().getModel()==null) {
			ketosWarning.setWarningMessage("There is no loaded classifier model. Ketos disabled.");
			WarningSystem.getWarningSystem().addWarning(ketosWarning);
		}


		if ((!PamCalendar.isSoundFile() || forceQueue) && !dlControl.isViewer()) {
			//for real time only
			if (workerThread!=null) {
				workerThread.stopTaskThread();
			}

			workerThread = new KetosThread(getKetosWorker());
			workerThread.setPriority(Thread.MAX_PRIORITY);
			workerThread.start();
		}

	}

	/**
	 * The task thread to run Ketos classifier in real time.  
	 * 
	 * @author Jamie Macaulay 
	 *
	 */
	public class KetosThread extends DLTaskThread {

		KetosThread(DLModelWorker soundSpotWorker) {
			super(soundSpotWorker);
		}

		@Override
		public void newResult(GenericPrediction soundSpotResult, GroupedRawData groupedRawData) {
			soundSpotResult.setClassNameID(GenericDLClassifier.getClassNameIDs(ketosDLParams)); 
			soundSpotResult.setBinaryClassification(GenericDLClassifier.isBinaryResult(soundSpotResult, ketosDLParams)); 
			newModelResult(soundSpotResult, groupedRawData);
		}

	}


	@Override
	public void closeModel() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		return "Ketos";
	}

	@Override
	public DLCLassiferModelUI getModelUI() {
		return this.ketosUI;
	}

	@Override
	public Serializable getDLModelSettings() {
		return ketosDLParams;
	}

	@Override
	public int getNumClasses() {
		return ketosDLParams.numClasses;
	}

	@Override
	public DLClassName[] getClassNames() {
		//System.out.println("Ketos Model: " + ketosDLParams.numClasses); 
		return ketosDLParams.classNames;
	}

	@Override
	public DLControl getDLControl() {
		return dlControl;
	}

	@Override
	public boolean checkModelOK() {
		return getKetosWorker().getModel()!=null;
	}

	/**
	 * Get the parameters for the Ketos classifier. 
	 * @param ketosDLParams - the Ketos parameters. 
	 */
	public KetosDLParams getKetosParams() {
		return ketosDLParams;
	}

	/**
	 * Set the Ketos parameters. 
	 * @param ketosDLParams - the parameters to set. 
	 */
	public void setKetosParams(KetosDLParams ketosDLParams) {
		this.ketosDLParams = ketosDLParams; 
	}

	/**
	 * Get the KetosWorker. this handles loading and running the Ketos model. 
	 * @return the Ketos worker. 
	 */
	public KetosWorker getKetosWorker() {
		if (ketosWorker==null) {
			ketosWorker= new KetosWorker(); 
		}
		return ketosWorker;
	}


	@Override
	public String getUnitName() {
		return dlControl.getUnitName()+"_Ketos"; 
	}


	@Override
	public String getUnitType() {
		return dlControl.getUnitType()+"_Ketos";

	}


	@Override
	public Serializable getSettingsReference() {
		if (ketosDLParams==null) {
			ketosDLParams = new KetosDLParams(); 
		}
		//System.out.println("SoundSpot have been saved. : " + soundSpotParmas.modelPath); 
		return ketosDLParams;
	}


	@Override
	public long getSettingsVersion() {
		return KetosDLParams.serialVersionUID;
	}


	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		KetosDLParams newParameters = (KetosDLParams) pamControlledUnitSettings.getSettings();
		if (newParameters!=null) {
			ketosDLParams = newParameters.clone();
			//System.out.println("SoundSpot have been restored. : " + soundSpotParmas.modelPath); 
		}
		else ketosDLParams = new KetosDLParams(); 
		return true;
	}
	
	@Override
	public ArrayList<PamWarning> checkSettingsOK() {
		return GenericDLClassifier.checkSettingsOK(ketosDLParams, dlControl); 
	}
		/**
	 * Send a new result form the thread queue to the process. 
	 * @param modelResult - the model result;
	 * @param groupedRawData - the grouped raw data. 
	 */
	private void newModelResult(GenericPrediction modelResult, GroupedRawData groupedRawData) {
		this.dlControl.getDLClassifyProcess().newModelResult(modelResult, groupedRawData);
	}

}
