package rawDeepLearningClassifier.dlClassification.delphinID;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;

import PamController.PamControlledUnitSettings;
import PamController.PamSettings;
import PamguardMVC.PamDataUnit;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.DLStatus;
import rawDeepLearningClassifier.dlClassification.DLClassName;
import rawDeepLearningClassifier.dlClassification.DLClassiferModel;
import rawDeepLearningClassifier.dlClassification.PredictionResult;
import rawDeepLearningClassifier.dlClassification.StandardClassifierModel;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.dlClassification.archiveModel.ArchiveModelClassifier;
import rawDeepLearningClassifier.dlClassification.archiveModel.ArchiveModelWorker;
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

	public DelphinIDClassifier(DLControl dlControl) {
		super(dlControl);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean isModelType(URI model) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DLCLassiferModelUI getModelUI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Serializable getDLModelSettings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUnitType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Serializable getSettingsReference() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getSettingsVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getUnitName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DLModelWorker<GenericPrediction> getDLWorker() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StandardModelParams getDLParams() {
		// TODO Auto-generated method stub
		return null;
	}
	
}