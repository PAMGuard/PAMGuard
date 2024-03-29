package rawDeepLearningClassifier.segmenter;

import Localiser.detectionGroupLocaliser.GroupDetection;
import PamguardMVC.PamDataUnit;

/**
 * A group of detection which are within a particular segment. This is used to pass detection groups straight to
 * 
 *  
 *  * @author Jamie Macaulay
 *
 */
public class SegmenterDetectionGroup extends GroupDetection<PamDataUnit> {

	public SegmenterDetectionGroup(long timeMilliseconds, int channelBitmap, long startSample, long duration) {
		super(timeMilliseconds, channelBitmap, startSample, duration);
		// TODO Auto-generated constructor stub
	}

}
