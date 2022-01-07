package videoRangePanel.importTideData;

import PamUtils.LatLong;
import PamguardMVC.PamDataUnit;
import videoRangePanel.VRControl;

/**
 * Stores data on the tide at a specific location at a specific time. 
 * @author Jamie Macaulay.
 *
 */
public class TideDataUnit extends PamDataUnit {

	/**
	 * the height of the tide above mean sea level in METERS
	 */
	private double level;

	/**
	 * the speed of the tide in METERS PER SECOND

	 */
	private double speed;
	/**
	 * the angle of the tide in RADIANS
	 */
	private double angle;

	/**
	 * location of this measurment
	 */
	private LatLong location;

	public TideDataUnit( long timeMillis) {
		super(timeMillis);
	}
	
	/**
	 * 
	 * @param timeMi llis - time
	 * @param level - the height of the tide above mean sea level in METERS
	 * @param speed- the speed of the tide in METERS PER SECOND
	 * @param angle- the angle of the tide in RADIANS
	 * @params location- location of this measurment
	 */
	public TideDataUnit( long timeMillis, double level, double speed, double angle, LatLong location ) {
		super(timeMillis);
		this.level=level;
		this.speed=speed;
		this.angle=angle;
		this.location=location; 
	}
	
	@Override 
	public int getChannelBitmap(){
		//tide has no channel!
		return 0; 
	}
	
	public TideDataUnit() {
		super(0);
	}

	/**
	 * @return level of the tide above mean sea level in meters
	 */
	public double getLevel() {
		return level;
	}

	/**
	 * @return the speed of the tide in meters per second
	 */
	public double getSpeed() {
		return speed;
	}

	/**
	 * 
	 * @return the direction of the tide in RADIANS
	 */
	public double getAngle() {
		return angle;
	}

	/**
	 * Location of the tidal measurment. 
	 * @return
	 */
	public LatLong getLocation() {
		return location;
	}

	public void setLevel(double level) {
		this.level = level;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}

	public void setLocation(LatLong location) {
		this.location = location;
	}

}
