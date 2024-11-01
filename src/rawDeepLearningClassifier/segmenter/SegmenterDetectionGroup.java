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
	 * The duration of the segment in millis. 
	 */
	private double segDuration;
	
	/**
	 * The start time of the segment in millis.
	 */
	private long segMillis;

	private double timeS;

	/**
	 * Constructor for a group of detections within a detection. Note that some
	 * longer detections (e.g. whistles) may have sections outside the segment.
	 * 
	 * @param timeMilliseconds - this is the start of the SEGMENT - Note that the
	 * @param channelBitmap    - channels of all detections
	 * @param startSample      - the stratSample of the SEGMENT.
	 * @param duration         - the duration of the SEGMENT in milliseconds.
	 */
	public SegmenterDetectionGroup(long timeMilliseconds, int channelBitmap, long startSample, double duration) {
		super(timeMilliseconds, channelBitmap, startSample, (long) duration);
		this.setDurationInMilliseconds(duration);
		this.segMillis = timeMilliseconds;
		this.segDuration = duration;
	}
	
	@Override
	public boolean isAllowSubdetectionSharing() {
		//segments share sub detections
		return true;
	}
	
	/**
	 * Get the segment start milliseconds. Note that this can be quite different to
	 * the getTimeMillis() because getTimeMillis() returns the time millis of the
	 * first data unit. For a long data unit, like a whistle, the end can be in the
	 * segment but the start is significantly before the segment starts
	 * 
	 * @return the segment start in millis.
	 */
	public long getSegmentStartMillis() {
		return segMillis;
	}
	
	/**
	 * Get the segment duration in milliseconds. 
	 * @return the segment duration in millis. 
	 */
	public double getSegmentDuration() {
		return segDuration;
	}

	public long getSegmentEndMillis() {
		return (long) (segMillis+segDuration);
	}

	public void setStartSecond(double timeS) {
		this.timeS = timeS;
	}

	public double getStartSecond() {
		return timeS;
	}

	public float getSampleRate() {
		return super.getParentDataBlock().getSampleRate();
	}


}
