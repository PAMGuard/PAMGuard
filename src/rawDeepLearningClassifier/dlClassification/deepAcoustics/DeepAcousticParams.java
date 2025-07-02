package rawDeepLearningClassifier.dlClassification.deepAcoustics;

import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;

/**
 * Parameters for the Deep Acoustics model.
 * This class extends StandardModelParams to include specific parameters for the Deep Acoustics model.
 * Parameters for the Deep Acoustics classifier.
 * 
 * 
 * @author Jamie Macaulay
 */
public class DeepAcousticParams extends StandardModelParams {

	private static final long serialVersionUID = 1L; 	

	
	public double minConfidence = 0.5; //minimum confidence for a detection to be considered valid
	
	/**
	 * True to merge overlapping boxes. 
	 */
	public boolean mergeOverlap = true;


}
