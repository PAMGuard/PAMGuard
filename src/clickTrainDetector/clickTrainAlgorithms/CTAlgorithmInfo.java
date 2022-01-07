package clickTrainDetector.clickTrainAlgorithms;

/**
 * Extra results or information for each CTDataUnit from the click train algorithm. 
 * . 
 * @author Jamie Macaulay
 *
 */
public interface CTAlgorithmInfo {

	/**
	 * Get an HTML formatted string for an extra chi^2 calculation info. 
	 * @return string showing info. 
	 */
	public String getInfoString(); 
	
	/**
	 * Logging class for saving algorithm info data. 
	 * @return logging info. 
	 */
	public CTAlgorithmInfoLogging getCTAlgorithmLogging();

	/**
	 * Get the type of algorithm used. 
	 * @return the type of click train algorithm used. 
	 */
	public String getAlgorithmType(); 
	
}
