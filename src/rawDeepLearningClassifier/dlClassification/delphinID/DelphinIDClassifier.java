package rawDeepLearningClassifier.dlClassification.delphinID;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;

import org.jamdev.jdl4pam.transforms.DLTransformsFactory;
import org.jamdev.jdl4pam.transforms.DLTransfromParams;

import PamController.PamControlledUnitSettings;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.DLClassiferModel;
import rawDeepLearningClassifier.dlClassification.StandardClassifierModel;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.dlClassification.genericModel.DLModelWorker;
import rawDeepLearningClassifier.dlClassification.genericModel.GenericPrediction;
import rawDeepLearningClassifier.layoutFX.DLCLassiferModelUI;

/**
 * A classifier based on the delphinID method which uses whistle contours to predict
 * dolphin species. 
 * 
 * @author Jamie Macaulay
 *
 */
public class DelphinIDClassifier extends StandardClassifierModel {
	
	
	private DelphinIDParams delphinIDParams = new DelphinIDParams();
	
	
	private DelphinUI delphinUI;
	
	
	private DelphinIDWorker delphinIDWorker;


	public DelphinIDClassifier(DLControl dlControl) {
		super(dlControl);
	}

	@Override
	public boolean isModelType(URI model) {
		return false;
	}

	@Override
	public String getName() {
		return "delphinID";
	}

	@Override
	public DLCLassiferModelUI getModelUI() {
		if (delphinUI==null) {
			delphinUI = new DelphinUI(this); 
		}
		return delphinUI;
	}

	@Override
	public Serializable getDLModelSettings() {
		return delphinIDParams;
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
		if (delphinIDParams==null) {
			delphinIDParams = new DelphinIDParams(); 
		}

		ArrayList<DLTransfromParams> dlTransformParams = DLClassiferModel.getDLTransformParams(delphinIDParams.dlTransfroms);

		delphinIDParams.dlTransfromParams=dlTransformParams; 

		//System.out.println("SoundSpot have been saved. : " + soundSpotParmas.classNames); 
		return delphinIDParams;

	}


	@Override
	public long getSettingsVersion() {
		return StandardModelParams.serialVersionUID;
	}


	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		DelphinIDParams newParameters = (DelphinIDParams) pamControlledUnitSettings.getSettings();
		if (newParameters!=null) {
			delphinIDParams = (DelphinIDParams) newParameters.clone();
			//System.out.println("SoundSpot have been restored. : " + soundSpotParmas.classNames); 
			if (delphinIDParams.dlTransfromParams!=null) {
				delphinIDParams.dlTransfroms = DLTransformsFactory.makeDLTransforms((ArrayList<DLTransfromParams>) delphinIDParams.dlTransfromParams); 
			}
		}
		else delphinIDParams = new DelphinIDParams();
		return true; 
	}
		


	@Override
	public DLModelWorker<GenericPrediction> getDLWorker() {
		if (delphinIDWorker==null) {
			delphinIDWorker = new DelphinIDWorker();
		}
		return delphinIDWorker;
	}

	@Override
	public DelphinIDParams getDLParams() {
		return delphinIDParams;
	}

	public void setDLParams(DelphinIDParams params) {
		this.delphinIDParams=params;
		
	}
	
}