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
public class DeepAcousticsPrediction extends StandardPrediction {

	/**
	 * The result from the Deep Acoustics model. 
	 */
	private DeepAcousticsResult result;
	
	/**
	 * The segment ID associated with this prediction.Usually the UID of the segment. 
	 */
	private long segmentID;
	
	/**
	 * Constructor that takes a DeepAcousticsResult and converts it to a standard prediction.
	 * 
	 * @param result The DeepAcousticsResult containing the predictions.
	 */
	public DeepAcousticsPrediction(DeepAcousticsResult result) {
		super(result.getPredicitions());
		this.setResult(result); 
	}

	/**
	 * Gets the DeepAcousticsResult associated with this prediction.
	 * 
	 * @return the DeepAcousticsResult object
	 */
	public DeepAcousticsResult getResult() {
		return result;
	}

	/**
	 * Sets the DeepAcousticsResult for this prediction.
	 * 
	 * @param result the DeepAcousticsResult to set
	 */
	public void setResult(DeepAcousticsResult result) {
		this.result = result;
	}
	
	/**
	 * Gets the confidence score of the prediction.
	 * 
	 * @return the confidence as a float
	 */
	public float getConfidence() {
		return result.getConfidence();
	}

	/**
	 * Gets the height of the detected bounding box.
	 * 
	 * @return the height as a float
	 */
	public float getHeight() {
		return result.getHeight();
	}

	/**
	 * Gets the Y coordinate of the detected bounding box.
	 * 
	 * @return the Y coordinate as a float
	 */
	public float getY() {
		return result.getY();
	}

	/**
	 * Gets the X coordinate of the detected bounding box.
	 * 
	 * @return the X coordinate as a float
	 */
	public float getX() {
		return result.getX();
	}

	/**
	 * Gets the prediction values from the DeepAcousticsResult.
	 * 
	 * @return an array of prediction floats
	 */
	public float[] getPredicitions() {
		return result.getPredicitions();
	}

	/**
	 * Gets the width of the detected bounding box.
	 * 
	 * @return the width as a float
	 */
	public float getWidth() {
		return result.getWidth();
	}

	/**
	 * Gets the bounding box as an array of doubles.
	 * 
	 * @return the bounding box coordinates
	 */
	public double[] getBoundingBox() {
		return result.getBoundingBox();
	}

	public void setParentSegmentID(long uid) {
		this.segmentID = uid;
		
	}

	public long getParentSegmentID() {
		return segmentID;
	}
}
