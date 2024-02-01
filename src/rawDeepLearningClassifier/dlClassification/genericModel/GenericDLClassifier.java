package rawDeepLearningClassifier.dlClassification.genericModel;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import org.jamdev.jdl4pam.transforms.DLTransformsFactory;
import org.jamdev.jdl4pam.transforms.DLTransfromParams;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.DLClassName;
import rawDeepLearningClassifier.dlClassification.DLClassiferModel;
import rawDeepLearningClassifier.dlClassification.StandardClassifierModel;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.segmenter.SegmenterProcess.GroupedRawData;
import warnings.PamWarning;


/**
 * A generic model - can be load any model but requires manually setting model 
 * *
 * @author Jamie Macaulay
 * 
 *
 */
public class GenericDLClassifier extends StandardClassifierModel {

	
	/**
	 * The DL control. 
	 */
	private DLControl dlControl;

	/**
	 * The generic model parameters. 
	 */
	private GenericModelParams genericModelParams = new GenericModelParams();

	/**
	 * The generic model UI,
	 */
	private GenericModelUI genericModelUI; 

	/**
	 * The generic model worker. 
	 */
	private GenericModelWorker genericModelWorker;



	public GenericDLClassifier(DLControl dlControl) {
		super(dlControl); 
		this.dlControl=dlControl; 

		genericModelUI = new GenericModelUI(this); 

		//the generic model worker...erm...does the work. 
		genericModelWorker = new GenericModelWorker(); 

		//load the previous settings
		PamSettingManager.getInstance().registerSettings(this);

		if (genericModelParams.dlTransfromParams!=null) {
			//important to remkae transforms from params
			genericModelParams.dlTransfroms = DLTransformsFactory.makeDLTransforms((ArrayList<DLTransfromParams>)genericModelParams.dlTransfromParams); 
		}
	}



	@Override
	public int getNumClasses() {
		return genericModelParams.numClasses;
	}

	@Override
	public String getUnitName() {
		return dlControl.getUnitName()+"_generic_model"; 
	}

	@Override
	public String getUnitType() {
		return dlControl.getUnitType()+"_generic_model";
	}


	@Override
	public DLClassName[] getClassNames() {
		return genericModelParams.classNames;
	}


	@Override
	public DLControl getDLControl() {
		return dlControl;
	}


	@Override
	public String getName() {
		return "Generic Model";
	}

	@Override
	public GenericModelUI getModelUI() {
		return genericModelUI;
	}

	@Override
	public Serializable getDLModelSettings() {
		return genericModelParams;
	}

	@Override
	public Serializable getSettingsReference() {
		if (genericModelParams==null) {
			genericModelParams = new GenericModelParams(); 
		}
		
		ArrayList<DLTransfromParams> dlTransformParams = DLClassiferModel.getDLTransformParams(genericModelParams.dlTransfroms);
		
		genericModelParams.dlTransfromParams=dlTransformParams; 
		
		if (genericModelParams.dlTransfromParams!=null) {
			System.out.println("Generic settings have been saved. : " + genericModelParams.dlTransfromParams.size()); 
		}		
		else {
			System.out.println("Generic settings have been saved. : " + null); 

		}
		
		return genericModelParams;
	}
	
	@Override
	public long getSettingsVersion() {
		return GenericModelParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		GenericModelParams newParameters = (GenericModelParams) pamControlledUnitSettings.getSettings();
		if (newParameters!=null) {
			genericModelParams = (GenericModelParams) newParameters.clone();
			if (genericModelParams.dlTransfromParams!=null) {
				System.out.println("Generic settings have been restored. : " + genericModelParams.dlTransfromParams.size()); 
			}
			else {
				System.out.println("Generic settings have been saved. : " + null); 
			}
		}
		else genericModelParams = new GenericModelParams(); 


		return true;
	}

	/**
	 * Get the sound spot parameters. 
	 * @return sound spot parameters. 
	 */
	public GenericModelParams getGenericDLParams() {
		return genericModelParams;
	}

	/**
	 * Get the generic model worker. 
	 * @return the generic model worker. 
	 */
	public GenericModelWorker getGenericDLWorker() {
		return genericModelWorker;
	}


	/**
	 * Set the generic model params. 
	 * @param clone - the params to set. 
	 */
	public void setGenericModelParams(StandardModelParams clone) {
		this.genericModelParams=(GenericModelParams) clone;	
	}

	/**
	 * Send a new result form the thread queue to the process. 
	 * @param modelResult - the model result;
	 * @param groupedRawData - the grouped raw data. 
	 */
	protected void newResult(GenericPrediction modelResult, GroupedRawData groupedRawData) {
		this.dlControl.getDLClassifyProcess().newModelResult(modelResult, groupedRawData);
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



	/**
	 * Check whether a model passes a binary test...
	 * @param modelResult - the model results
	 * @return the model results. 
	 */
	public static boolean isBinaryResult(GenericPrediction modelResult, StandardModelParams genericModelParams) {
		for (int i=0; i<modelResult.getPrediction().length; i++) {
			if (modelResult.getPrediction()[i]>genericModelParams.threshold && genericModelParams.binaryClassification[i]) {
				//				System.out.println("SoundSpotClassifier: prediciton: " + i + " passed threshold with val: " + modelResult.getPrediction()[i]); 
				return true; 
			}
		}
		return  false;
	}


//	@Override
//	public ArrayList<PamWarning> checkSettingsOK() {
//		return checkSettingsOK(genericModelParams, dlControl);
//	}
	


	@Override
	public boolean isModelType(URI uri) {
		return super.isModelExtensions(uri); 
	}

	@Override
	public DLModelWorker<GenericPrediction> getDLWorker() {
		return this.genericModelWorker;
	}



	@Override
	public StandardModelParams getDLParams() {
		return this.genericModelParams;
	}



//	/**
//	 * Get the class name IDs
//	 * @return an array of class name IDs
//	 */
//	private short[] getClassNameIDs() {
//		if (genericModelParams.classNames==null || genericModelParams.classNames.length<=0) return null; 
//		short[] nameIDs = new short[genericModelParams.classNames.length]; 
//		for (int i = 0 ; i<genericModelParams.classNames.length; i++) {
//			nameIDs[i] = genericModelParams.classNames[i].ID; 
//		}
//		return nameIDs; 
//	}
//
//
//
//	/**
//	 * Check whether a model passes a binary test...
//	 * @param modelResult - the model results
//	 * @return the model results. 
//	 */
//	private boolean isBinaryResult(GenericPrediction modelResult) {
//		for (int i=0; i<modelResult.getPrediction().length; i++) {
//			if (modelResult.getPrediction()[i]>genericModelParams.threshold && genericModelParams.binaryClassification[i]) {
//				//				System.out.println("SoundSpotClassifier: prediciton: " + i + " passed threshold with val: " + modelResult.getPrediction()[i]); 
//				return true; 
//			}
//		}
//		return  false;
//	}


}
