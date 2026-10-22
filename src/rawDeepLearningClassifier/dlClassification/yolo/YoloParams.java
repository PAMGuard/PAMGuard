package rawDeepLearningClassifier.dlClassification.yolo;

import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;

/**
 * Parameters for a YOLO object detection classifier.
 *
 * @author Jamie Macaulay
 */
public class YoloParams extends StandardModelParams implements YoloDetectionParams {

	private static final long serialVersionUID = 1L;

	/**
	 * The minimum confidence for a detection. The default is the same as MATLAB's
	 * yolov4ObjectDetector.
	 */
	public double minConfidence = 0.5;

	/**
	 * True to merge overlapping boxes.
	 */
	public boolean mergeOverlap = false;

	/**
	 * Minimum overlap required to merge two boxes, a value between 0.0 (any
	 * overlap) and 1.0 (full overlap).
	 */
	public float minMergeOverlap = 0.05f;

	@Override
	public double getMinConfidence() {
		return minConfidence;
	}

	@Override
	public void setMinConfidence(double minConfidence) {
		this.minConfidence = minConfidence;
	}

	@Override
	public boolean isMergeOverlap() {
		return mergeOverlap;
	}

	@Override
	public void setMergeOverlap(boolean mergeOverlap) {
		this.mergeOverlap = mergeOverlap;
	}

	@Override
	public float getMinMergeOverlap() {
		return minMergeOverlap;
	}

	@Override
	public void setMinMergeOverlap(float minMergeOverlap) {
		this.minMergeOverlap = minMergeOverlap;
	}

	@Override
	public YoloParams clone() {
		return (YoloParams) super.clone();
	}

}
