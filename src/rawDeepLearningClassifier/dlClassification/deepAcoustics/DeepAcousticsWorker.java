package rawDeepLearningClassifier.dlClassification.deepAcoustics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.jamdev.jdl4pam.ArchiveModel;
import org.jamdev.jdl4pam.transforms.jsonfile.DLTransformsParser;
import org.json.JSONObject;

import PamguardMVC.PamDataUnit;
import ai.djl.MalformedModelException;
import ai.djl.engine.EngineException;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.DLStatus;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.dlClassification.archiveModel.ArchiveModelWorker;
import rawDeepLearningClassifier.dlClassification.archiveModel.SimpleArchiveModel;
import rawDeepLearningClassifier.dlClassification.delphinID.DelphinIDParams;
import rawDeepLearningClassifier.dlClassification.genericModel.StandardPrediction;

public class DeepAcousticsWorker extends ArchiveModelWorker {


	@Override
	public DLStatus prepModel(StandardModelParams dlParams, DLControl dlControl) {
		//most of the model prep is done in the parent class. 
		DLStatus status = super.prepModel(dlParams, dlControl);

		//need to get info on anchor boxes
		//now have to read the whsitle2image transform to get correct parameters for that. 
		String jsonString  = DLTransformsParser.readJSONString(new File(this.getModel().getAudioReprFile()));

		JSONObject jsonObject = new JSONObject(jsonString); 
		
		//we want to get info on the network from here.,

		return status;
	}



	@Override
	public synchronized ArrayList<StandardPrediction> runModel(ArrayList<? extends PamDataUnit> dataUnits, float sampleRate, int iChan) {

		//need to override as the input is 4D array float[][][][]


		return null;

	}

	@Override
	public ArchiveModel loadModel(String currentPath2) throws MalformedModelException, IOException, EngineException {
		//Override here to return a deep acoutics model. 
		return new DeepAcousticsModel(new File(currentPath2)); 
	}



}
