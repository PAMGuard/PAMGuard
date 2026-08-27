package Localiser.detectionGroupLocaliser;

import PamDetection.PamDetection;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.AcousticDetectionGroup;

/**
 * Legacy base class for groups of localised detections.
 * <p>
 * All functionality has moved up into {@link PamguardMVC.superdet.DetectionGroup} and
 * {@link AcousticDetectionGroup} which are the modern base classes for any
 * group of detections. This class remains only so that existing subclasses
 * (whistle groups, the old target motion code, etc.) continue to work; new
 * code should extend {@link AcousticDetectionGroup} directly.
 *
 * @author Doug Gillespie
 */
@Deprecated
public class GroupDetection<T extends PamDataUnit> extends AcousticDetectionGroup<T> implements PamDetection {

	public GroupDetection(T firstDetection) {
		super(firstDetection);
	}

	/**
	 * Note that if using this constructor, the sequence map will have to be set explicitly by the calling class
	 * 
	 * @param timeMilliseconds
	 * @param channelBitmap
	 * @param startSample
	 * @param duration
	 */
	public GroupDetection(long timeMilliseconds, int channelBitmap, long startSample, long duration) {
		super(timeMilliseconds, channelBitmap, startSample, duration);
		makeLocalisation();
	}

}
