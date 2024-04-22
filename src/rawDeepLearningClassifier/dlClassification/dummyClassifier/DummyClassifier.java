package rawDeepLearningClassifier.dlClassification.dummyClassifier;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;

import PamguardMVC.PamDataUnit;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.DLStatus;
import rawDeepLearningClassifier.dlClassification.DLClassName;
import rawDeepLearningClassifier.dlClassification.DLClassiferModel;
import rawDeepLearningClassifier.dlClassification.PredictionResult;
import rawDeepLearningClassifier.layoutFX.DLCLassiferModelUI;
import rawDeepLearningClassifier.segmenter.GroupedRawData;

/**
 * Classifier which returns a random results. Used for debugging and testing. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class DummyClassifier implements DLClassiferModel{


	@Override
	public void prepModel() {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeModel() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		return "Random Classifier";
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
	public int getNumClasses() {
		return 2;
	}

	@Override
	public DLClassName[] getClassNames() {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<? extends PredictionResult> runModel(ArrayList<? extends PamDataUnit> rawDataUnit) {
		ArrayList<PredictionResult> modelResults = new ArrayList<PredictionResult>(); 

		for (int i=0; i<rawDataUnit.size(); i++) {
			modelResults.add(new DummyModelResult(new float[] {(float) Math.random(), (float) Math.random()}));
		}


		return modelResults;
	}

	@Override
	public DLControl getDLControl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DLStatus getModelStatus() {
		return null;
	}



	@Override
	public boolean isModelType(URI uri) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DLStatus setModel(URI model) {
		// TODO Auto-generated method stub
		return null;
	}

}
