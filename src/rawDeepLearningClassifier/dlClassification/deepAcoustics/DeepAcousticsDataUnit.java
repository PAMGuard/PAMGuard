package rawDeepLearningClassifier.dlClassification.deepAcoustics;

import org.jamdev.jdl4pam.deepAcoustics.DeepAcousticsResult;

import rawDeepLearningClassifier.dlClassification.genericModel.StandardPrediction;

/**
 * A data unit for the Deep Acoustics method which uses object detection models within spectrograms to predict
 * dolphin whistle detections and then classify to species.
 * 
 * This class is used to wrap the results from a DeepAcousticsResult into a format that can be used by the standard prediction system.
 * 
 * @author Jamie Macaulay
 *
 */
public class DeepAcousticsDataUnit extends StandardPrediction {

	/**
	 * The result from the Deep Acoustics model. 
	 */
	private DeepAcousticsResult result;

	public DeepAcousticsDataUnit(DeepAcousticsResult result) {
		super(result.getPredicitions());
		this.result = result; 
	}


}
