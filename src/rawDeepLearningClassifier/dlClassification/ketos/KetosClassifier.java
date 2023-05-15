package rawDeepLearningClassifier.dlClassification.ketos;

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
import rawDeepLearningClassifier.layoutFX.DLCLassiferModelUI;

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
public class KetosClassifier extends StandardClassifierModel {
	
	public static String MODEL_NAME = "Ketos";
	


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
	 * The ketos classifier. 
	 */
	public KetosClassifier(DLControl dlControl) {
		super(dlControl); 
		this.dlControl=dlControl; 
		this.ketosDLParams = new KetosDLParams(); 
		this.ketosUI= new KetosUI(this); 
		//load the previous settings
		PamSettingManager.getInstance().registerSettings(this);
	}


	@Override
	public String getName() {
		return MODEL_NAME;
	}

	@Override
	public DLCLassiferModelUI getModelUI() {
		return this.ketosUI;
	}
	

	@Override
	public DLModelWorker<GenericPrediction> getDLWorker() {
		return getKetosWorker();
	}


	@Override
	public StandardModelParams getDLParams() {
		return ketosDLParams;
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

	@Override
	public Serializable getDLModelSettings() {
		return ketosDLParams;
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
		return dlControl.getUnitName()+"_" + MODEL_NAME; 
	}


	@Override
	public String getUnitType() {
		return dlControl.getUnitType()+"_" + MODEL_NAME;

	}


	@Override
	public Serializable getSettingsReference() {
		if (ketosDLParams==null) {
			ketosDLParams = new KetosDLParams(); 
		}
		
		ArrayList<DLTransfromParams> dlTransformParams = DLClassiferModel.getDLTransformParams(ketosDLParams.dlTransfroms);
		
		ketosDLParams.dlTransfromParams=dlTransformParams; 
		
		System.out.println("KetosParams have been saved. : " + ketosDLParams.dlTransfromParams); 
		
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
			System.out.println("KetosParams have been restored. : " + ketosDLParams.dlTransfromParams); 
			if (ketosDLParams.dlTransfromParams!=null) {
				ketosDLParams.dlTransfroms = DLTransformsFactory.makeDLTransforms((ArrayList<DLTransfromParams>) ketosDLParams.dlTransfromParams); 
			}
		}
		else ketosDLParams = new KetosDLParams(); 
		return true;
	}


	@Override
	public boolean isModelType(URI uri) {
		//Ketos is easy because there are not many files with a .ktpb extension. 
		return super.isModelExtensions(uri);
	}

}
