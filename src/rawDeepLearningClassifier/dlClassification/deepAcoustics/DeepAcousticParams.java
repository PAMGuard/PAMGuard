package rawDeepLearningClassifier.dlClassification.deepAcoustics;

import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.dlClassification.yolo.YoloDetectionParams;

/**
 * Parameters for the Deep Acoustics model.
 * This class extends StandardModelParams to include specific parameters for the Deep Acoustics model.
 *
 * @author Jamie Macaulay
 */
public class DeepAcousticParams extends StandardModelParams implements YoloDetectionParams {

	private static final long serialVersionUID = 2L;


	public double minConfidence = 0.4; //minimum confidence for a detection to be considered valid

	/**
	 * True to merge overlapping boxes.
	 */
	public boolean mergeOverlap = true;


	/**
	 * Minimum overlap required to merge two boxes. For example, if 0.05 then at least 5% overlap in area is required to merge two boxes.
	 */
	public float minMergeOverlap = 0.05f; //minimum overlap for merging boxes, a value between 0.0 (any overlap) and 1.0 (full overlap). For 5% use 0.05


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
}
