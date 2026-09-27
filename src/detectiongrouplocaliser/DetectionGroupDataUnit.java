package detectiongrouplocaliser;

import java.util.List;

import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.AcousticDetectionGroup;

/**
 * A group of acoustic detections, put together manually by the user or by a
 * detection process. Almost all functionality is in the base classes
 * {@link PamguardMVC.superdet.DetectionGroup} and {@link AcousticDetectionGroup}.
 */
public class DetectionGroupDataUnit extends AcousticDetectionGroup<PamDataUnit> {

	public DetectionGroupDataUnit(long timeMilliseconds, List<PamDataUnit> list) {
		super(timeMilliseconds);
		addSubDetections(list);
	}

}
