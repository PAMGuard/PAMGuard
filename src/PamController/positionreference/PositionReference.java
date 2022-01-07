package PamController.positionreference;

import GPS.GpsData;

/**
 * Slightly different behaviour to MasterReference. Can be implemented in 
 * a number of modules to get a 'latest' reference position for range
 * measurements, etc. 
 * @author Doug
 *
 */
public interface PositionReference {

	/**
	 * Get position data, with heading information if possible. 
	 * @param timeMillis time for position (will usually be latest)
	 * @return GPS position with heading
	 */
	public GpsData getReferencePosition(long timeMillis);
	
	/**
	 * Name of the reference. 
	 * @return
	 */
	public String getReferenceName();
}
