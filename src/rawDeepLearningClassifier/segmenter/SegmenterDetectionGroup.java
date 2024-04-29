package rawDeepLearningClassifier.segmenter;

import Localiser.detectionGroupLocaliser.GroupDetection;
import PamguardMVC.PamDataUnit;

/**
 * A group of detection which are within a particular segment. This is used to pass detection groups straight to a classifier.
 * @author Jamie Macaulay
 *
 */
public class SegmenterDetectionGroup extends GroupDetection<PamDataUnit> {

	/**
	 * Constructor for a group of detections within a detection. Note that some
	 * longer detections (e.g. whistles) may have sections outside the segment.
	 * 
	 * @param timeMilliseconds - this is the start of the SEGMENT - Note that the
	 * @param channelBitmap    - channels of all detections
	 * @param startSample      - the stratSample of the SEGMENT.
	 * @param duration         - the duration of the SEGMENT.
	 */
	public SegmenterDetectionGroup(long timeMilliseconds, int channelBitmap, long startSample, double duration) {
		super(timeMilliseconds, channelBitmap, startSample, (long) duration);
		this.setDurationInMilliseconds(duration);
	}
	
	@Override
	public boolean isAllowSubdetectionSharing() {
		//segmetns share sub detections
		return true;
	}


}
