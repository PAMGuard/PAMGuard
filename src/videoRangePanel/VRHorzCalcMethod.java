package videoRangePanel;

import PamController.SettingsPane;
import videoRangePanel.layoutAWT.RangeDialogPanel;

/**
 * Method for measuring the distance to an animal based on the pitch and height of an observer.
 * @author Doug Gillespie, Jamie Macaulay
 *
 */
public abstract class VRHorzCalcMethod {
	
	/**
	 * The earth's radius in meters. 
	 */
	protected static final double earthRadius = 6356766;
	
	/**
	 * The gravitational field strength in (m/s)^2
	 */
	protected static final double gravity = 9.80665;
	
	/**
	 * Converts a height and an angle below the horizon to a distance in metres. 
	 * @param height platform height (metres)
	 * @param angle angle below the horizon (Radians)
	 * @return distance in metres. 
	 */
	public abstract double getRange(double height, double angle);
	
	/**
	 * Converts a range into an angle below the horizon. 
	 * <p>
	 * Or returns -1 if the range is beyond the horizon.
	 * @param height platform height (metres)
	 * @param range range to object. 
	 * @return angle in Radians. 
	 */
	public abstract double getAngle(double height, double range);
	
	abstract void configure();
		
	/**
	 * Get the swing based pane for the method
	 * @return pane for changing settings
	 */
	public abstract RangeDialogPanel dialogPanel();
	
	/**
	 * Get the FX based settings pane for the method
	 * @return pane for changing settings. 
	 */
	public abstract SettingsPane<?> getRangeMethodPane();

	
	abstract String getName();
	
	/**
	 * Calculate the horizon dip angle from the horizontal
	 * @param height Platform height
	 * @return dip angle in radians. 
	 */
	protected double getHorizonAngle(double height) {
		return Math.acos(earthRadius / (earthRadius + height));
	}
	
	/**
	 * Calculate the distance to the horizon from a given height. 
	 * @param height
	 * @return distnace to horizon in metres. 
	 */
	abstract public double getHorizonDistance(double height);

	/**
	 * Get the range from the pitch of the camera and camera height. 
	 * Note that the range is the distance from the camera to the animal. 
	 * @param height- height of the camera
	 * @param psi- the angle of the camera in radians. 
	 * 0-camera pointing downwards (i.e same direction as g). 90 degrees= camera point perpendicular to direction of g; 
	 * @return the rane in meters
	 */
	abstract public double rangeFromPsi(double height, double psi);

	/**
	 * Get the pitch of the camera from the height and range to target. 
	 * @param height-height of the camera
	 * @param range-distance to the target from the camera. 
	 * @return psi- the angle of the camera in radians. 0-camera pointing downwards (i.e same direction as g). 90 degrees= camera point perpendicular to direction of g; 
	 */
	abstract public double psiFromRange(double height, double range);

	
}
