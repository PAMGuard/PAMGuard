package dataPlotsFX.overlaymark;

import detectiongrouplocaliser.DetectionGroupSummary;

/**
 * Listener for detection group. 
 * 
 * @author Jamie Macaulay
 *
 */
public interface DetectionGroupListener {
	
	/**
	 * A new detection group has been selected. 
	 * @param detectionGroup - the detection group. 
	 */
	public void newSelectedGroup(DetectionGroupSummary detectionGroup);

}
