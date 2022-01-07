package clickTrainDetector.clickTrainAlgorithms.mht.mhtvar;

/**
 * Converts Radians to degrees and vice versa. Degrees are shown in controls and radians 
 * saved in memory. 
 * @author Jamie Macaulay
 *
 */
public class Rad2Deg extends ResultConverter {
	/**
	 * Convert the value to the value to be shown in controls
	 * @param value
	 * @return
	 */
	public double convert2Control(double value) {
		return Math.toDegrees(value); 
	}
	
	/**
	 * Convert the control value to the true value. 
	 * @param value - value form control
	 * @return
	 */
	public double convert2Value(double value) {
		return Math.toRadians(value); 
	}
}