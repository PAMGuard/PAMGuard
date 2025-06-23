package rawDeepLearningClassifier.dlClassification.deepAcoustics;

import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;

/**
 * Parameters for the Deep Acoustics model.
 * This class extends StandardModelParams to include specific parameters for the Deep Acoustics model.
 * 
 * @author Jamie Macaulay
 */
public class DeepAcousticParams extends StandardModelParams {

	private static final long serialVersionUID = 1L; 	
	
	/**
	 * The minimum confidence threshold for a detection to be considered valid.
	 * This is used to filter out low-confidence detections.
	 */
	public double minConfidence = 0.5; // Minimum confidence for a detection to be considered valid

}
