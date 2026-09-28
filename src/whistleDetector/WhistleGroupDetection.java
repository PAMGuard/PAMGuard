package whistleDetector;

import PamguardMVC.superdet.AcousticDetectionGroup;

/**
 * Whistle group detection is initially designed to take just one whistle from each 
 * of many hydrophone pairs. Once in there, they shouldhoweer localise in just the same
 * way as the clicks if they have good bearing information. 
 * 
 * @author Doug
 *
 */
public class WhistleGroupDetection extends AcousticDetectionGroup<ShapeDataUnit> {

	public WhistleGroupDetection(ShapeDataUnit firstShape) {
		super(firstShape);
	}

}
