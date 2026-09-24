package rawDeepLearningClassifier.dlClassification.yolo;

/**
 * Parameters which are specific to YOLO object detection models. This is an
 * interface so that existing parameter classes (e.g. DeepAcousticParams) can
 * be used without breaking the serialisation of saved settings.
 *
 * @author Jamie Macaulay
 */
public interface YoloDetectionParams {

	/**
	 * Get the minimum confidence for a detection.
	 *
	 * @return the minimum confidence (objectness multiplied by the maximum class
	 *         probability) between 0 and 1.
	 */
	public double getMinConfidence();

	/**
	 * Set the minimum confidence for a detection.
	 *
	 * @param minConfidence - the minimum confidence between 0 and 1.
	 */
	public void setMinConfidence(double minConfidence);

	/**
	 * Check whether overlapping detections are merged.
	 *
	 * @return true to merge overlapping detections.
	 */
	public boolean isMergeOverlap();

	/**
	 * Set whether overlapping detections are merged.
	 *
	 * @param mergeOverlap - true to merge overlapping detections.
	 */
	public void setMergeOverlap(boolean mergeOverlap);

	/**
	 * Get the minimum overlap (intersection over union) required to merge two
	 * detections.
	 *
	 * @return the minimum overlap between 0 and 1.
	 */
	public float getMinMergeOverlap();

	/**
	 * Set the minimum overlap (intersection over union) required to merge two
	 * detections.
	 *
	 * @param minMergeOverlap - the minimum overlap between 0 and 1.
	 */
	public void setMinMergeOverlap(float minMergeOverlap);

}
