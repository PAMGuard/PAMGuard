package gpl.whiten;

public interface TimeWhitener {

	/**
	 * Add data to background data store. 
	 * @param specData abs spec data only with bins in range of interest
	 */
	public void addBackground(double[] specData);
	
	/**
	 * Get the current background
	 * @return the mean, median, or whatever, background
	 */
	public double[] getBackground();

	/**
	 * Whiten spectral data 
	 * @param specMean spectral mean values returned from a call to addBackground
	 * @param specData abs spec data only with bins in range of interest
	 * @return double array of background means. 
	 */
	public double[] whitenData(double[] specMean, double[] specData);
	
}
