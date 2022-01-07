package clickTrainDetector.clickTrainAlgorithms.mht.mhtvar;

/**
 * Converts results to values. e.g. if between degrees and RADIANS.  
 * @author Jamie Macaulay
 *
 */
public class ResultConverter {

	/**
	 * Convert the value to the value to be shown in controls
	 * @param value
	 * @return
	 */
	public double convert2Control(double value) {
		return value; 
	}
	
	/**
	 * Convert the control value to the true value. 
	 * @param value - value form control
	 * @return
	 */
	public double convert2Value(double value) {
		return value; 
	}
	
}