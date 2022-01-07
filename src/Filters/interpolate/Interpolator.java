package Filters.interpolate;

/**
 * Interface for an interpolator. These are a bit like filters in that they have some history and therefore some
 * delay. Designed for use with the decimator, when the decimation factor is non integer.  
 * @author dg50
 *
 */
public interface Interpolator {

	/**
	 * Get the memory or delay in samples. 
	 * @return average delay in samples. 
	 */
	double getSampleDelay();
	
	/**
	 * Set a new array of input data. The interpolator will probably hold 
	 * some history from previous data in a buffer. 
	 * @param inputArray array of input data.
	 */
	void setInputData(double[] inputArray);
	
	/**
	 * Get an output value for a specific point. 0 refers to the exact position of the 
	 * first input sample, though there may be buffered data allowing the extraction of 
	 * data from slightly earlier than that.  
	 * @param arrayPosition picking position. Must be >= 0 and <= length of input data-1 
	 * @return value picked from the input array and any history buffer
	 */
	double getOutputValue(double arrayPosition);
	
}
