package rawDeepLearningClassifier.dlClassification.archiveModel;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;

import org.jamdev.jdl4pam.transforms.DLTransformsFactory;
import org.jamdev.jdl4pam.transforms.DLTransfromParams;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.DLClassiferModel;
import rawDeepLearningClassifier.dlClassification.StandardClassifierModel;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.dlClassification.genericModel.DLModelWorker;
import rawDeepLearningClassifier.dlClassification.genericModel.GenericPrediction;
import rawDeepLearningClassifier.dlClassification.ketos.KetosDLParams;
import rawDeepLearningClassifier.dlClassification.ketos.KetosUI;
import rawDeepLearningClassifier.layoutFX.DLCLassiferModelUI;

/**
 * A Tensorflow or PyTorch model zipped with a PAMGuard settings file. This
 * allows anyone to create a model that can be loaded with one click if and only
 * if they have already set up the json metadata properly.
 * 
 * @author Jamie Macaulay
 *
 */
public abstract class ArchiveModelClassifier extends StandardClassifierModel {

	private static final String MODEL_NAME = "Zip Model";
	
	/**
	 * The file extensions
	 */
	private String[] fileExtensions = new String[] {"*.zip"};
		
	/**
	 * Parameters for a Ketos classifier. 
	 */
	private StandardModelParams standardDLParams;

	/**
	 * The UI components of the Ketos classifier
	 */
	private ArchiveModelUI archiveModelUI; 

	/**
	 * The Ketos worker. this handles the heavy lifting such as loading and running
	 * models. 
	 */
	private ArchiveModelWorker archiveModelWorker; 

	public ArchiveModelClassifier(DLControl dlControl) {
		super(dlControl);
		this.standardDLParams = makeParams(); 

		this.archiveModelUI= new ArchiveModelUI(this); 
		
		//load the previous settings
		PamSettingManager.getInstance().registerSettings(this);

	}


	public void setDLParams(StandardModelParams ketosParams) {
		this.standardDLParams=ketosParams;
		
	}
	
	@Override
	public String getName() {
		return MODEL_NAME;
	}

	@Override
	public DLCLassiferModelUI getModelUI() {
		return this.archiveModelUI;
	}
	

	@Override
	public DLModelWorker<GenericPrediction> getDLWorker() {
		return getModelWorker();
	}


	@Override
	public StandardModelParams getDLParams() {
		return standardDLParams;
	}
	
	/**
	 * Get the parameters for the Ketos classifier. 
	 * @param standardDLParams - the Ketos parameters. 
	 */
	public StandardModelParams getKetosParams() {
		return standardDLParams;
	}


	@Override
	public Serializable getDLModelSettings() {
		return standardDLParams;
	}

	/**
	 * Get the KetosWorker. this handles loading and running the Ketos model. 
	 * @return the Ketos worker. 
	 */
	public ArchiveModelWorker getModelWorker() {
		if (archiveModelWorker==null) {
			archiveModelWorker= new ArchiveModelWorker(); 
		}
		return archiveModelWorker;
	}


	@Override
	public String getUnitName() {
		return dlControl.getUnitName()+"_" + getName(); 
	}


	@Override
	public String getUnitType() {
		return dlControl.getUnitType()+"_" + getName();
	}


	@Override
	public Serializable getSettingsReference() {
		if (standardDLParams==null) {
			standardDLParams = makeParams();
		}
		
		ArrayList<DLTransfromParams> dlTransformParams = DLClassiferModel.getDLTransformParams(standardDLParams.dlTransfroms);
		
		standardDLParams.dlTransfromParams=dlTransformParams; 
//		System.out.println("KetosParams have been saved. : " + standardDLParams.dlTransfromParams); 
		return standardDLParams;
	}


	@Override
	public long getSettingsVersion() {
		return StandardModelParams.serialVersionUID;
	}


	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		StandardModelParams newParameters = (StandardModelParams) pamControlledUnitSettings.getSettings();
		if (newParameters != null) {
			standardDLParams = newParameters.clone();
			System.out.println("KetosParams have been restored. : " + standardDLParams.dlTransfromParams);
			if (standardDLParams.dlTransfromParams != null) {
				standardDLParams.dlTransfroms = DLTransformsFactory
						.makeDLTransforms((ArrayList<DLTransfromParams>) standardDLParams.dlTransfromParams);
			}
		} 
		else {
			standardDLParams = makeParams();
		}
		return true;
	}
	
	/**
	 *Create the parameters class for the model. This can be overridden for bespoke parameters. 
	 *classes. 
	 * @return a new parameters class object. 
	 */
	public StandardModelParams makeParams() {
		return new StandardModelParams();
	}

	@Override
	public boolean isModelType(URI uri) {
		return super.isModelExtensions(uri);
	}


	/**
	 * Get the file extensions for the model type. 
	 * @return the file extension. 
	 */
	public String[] getFileExtensions() {
		return fileExtensions;
	}




}
