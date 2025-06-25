package rawDeepLearningClassifier.dlClassification.deepAcoustics;

import java.util.ArrayList;

import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.PredictionResult;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.dlClassification.archiveModel.ArchiveModelClassifier;
import rawDeepLearningClassifier.dlClassification.archiveModel.ArchiveModelWorker;
import rawDeepLearningClassifier.dlClassification.genericModel.DLModelWorker;
import rawDeepLearningClassifier.dlClassification.genericModel.GenericDLClassifier;
import rawDeepLearningClassifier.dlClassification.genericModel.StandardPrediction;
import warnings.WarningSystem;


/**
 * A classifier based on the Deep Acoustics method which uses object detection models within spectrograms to predict
 * dolphin whistle detections and then classify to species.. 
 * 
 * @author Jamie Macaulay
 *
 */
public class DeepAcousticsClassifier extends ArchiveModelClassifier {
	
	public static String MODEL_NAME = "DeepAcoustics";


	public DeepAcousticsClassifier(DLControl dlControl) {
		super(dlControl);
		archiveModelUI = new DeepAcousticsUI(this);
	}
	

	@Override
	public String getName() {
		//important because this is used to identify model from JSON file
		return MODEL_NAME;
	}
	
	@Override
	public StandardModelParams makeParams() {
		return new DeepAcousticParams();
	}
	

	/**
	 * Get the KetosWorker. this handles loading and running the Ketos model. 
	 * @return the Ketos worker. 
	 */
	public ArchiveModelWorker getModelWorker() {
		if (archiveModelWorker==null) {
			archiveModelWorker= new DeepAcousticsWorker(); 
		}
		return archiveModelWorker;
	}


}
