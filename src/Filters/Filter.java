package Filters;

public interface Filter {

	/**
	 * Calculates the poles and zeros for the filter and sets up any memory
	 * buffers required during real time operation.
	 */
	public abstract void prepareFilter();

	/**
	 * Runs the filter on an array of data
	 * <p>
	 * New values overwrite the old values in the array.
	 * 
	 * @param inputData
	 */
	public abstract void runFilter(double[] inputData);

	/**
	 * Runs the filter on an array of data
	 * <p>
	 * New values write into the output data array.
	 * 
	 * @param inputData
	 */
	public abstract void runFilter(double[] inputData, double[] outputData);

	/**
	 * Runs the filter on a single data value
	 * 
	 * @param aData
	 * @return New data value
	 */
	public abstract double runFilter(double aData);
	
	/**
	 * Gets the delay of the filter - rarely used, but can be important
	 * for some processing tasks. For an IIRF filter, this would be 
	 * half the number of poles, for a moving average or median filter it
	 * would be half the filter length.  
	 * @return filter delay in samples. 
	 */
	public abstract int getFilterDelay();

}