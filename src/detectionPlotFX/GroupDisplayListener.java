package detectionPlotFX;

import PamguardMVC.PamDataUnit;

/**
 * Listeners for changing selected detection in the GroupDetectionDisplay. 
 * 
 * @author Jamie Macaulay
 *
 */
@SuppressWarnings("rawtypes")
public interface GroupDisplayListener {

	/** 
	 * A new data unit has been selected 
	 * @param oldDataUnit - the old data unit. 
	 * @param newDataUnit - the new selected data unit.
	 */
	public void newDataUnitSlected(PamDataUnit oldDataUnit, PamDataUnit newDataUnit);
}
