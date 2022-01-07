package pamMaths;

import java.util.Arrays;

/**
 * Calculate the mean and standard deviation of 
 * an double array. 
 * @author Doug Gillespie
 *
 */
public class STD {

	private double[] data;
	
	private double mean, std, median;


	/**
	 * flag indicating whether or not the median has already been calculated
	 */
	private boolean medianAlreadyCalc = false;
	
	
	/**
	 * Calculate the mean and standard deviation using
	 * new data
	 * @param data data array
	 * @return standard deviation
	 */
	public double getSTD(double[] data) {
		calculate(data);
		return std;
	}
	
	/**
	 * Get the standard deviation calculated in a previous
	 * call to getSTD(double[] data) or getMean(double[] data)
	 * @return standard deviation
	 */
	public double getSTD() {
		return std;
	}

	/**
	 * Calculate the mean and standard deviation using
	 * new data
	 * @param data data array
	 * @return mean value
	 */
	public double getMean(double[] data) {
		calculate(data);
		return mean;
	}
	
	/**
	 * Get the mean value calculated in a previous
	 * call to getSTD(double[] data) or getMean(double[] data)
	 * @return mean
	 */
	public double getMean() {
		return mean;
	}
	
	private void calculate(double[] data) {
		medianAlreadyCalc=false;	// reset the median flag
		this.data = data;
		int n = data.length;
		if (n == 0) {
			std = Double.NaN;
			mean = Double.NaN;
			return;
		}
		mean = 0;
		for (int i = 0; i < n; i++) {
			mean += data[i];
		}
		mean /= n;
		double d;
		std = 0;
		for (int i = 0; i < n; i++) {
			d = data[i]-mean;
			std += (d*d);
		}
		std /= (n-1);
//		std /= n;
		std = Math.sqrt(std);
	}

	/**
	 * Get the data previously set with calls to STD and Mean funcs
	 * @return data array
	 */
	public double[] getData() {
		return data;
	}

	/**
	 * Get the skew as per page 612 or numerical recipes. 
	 * @return the skew of the data
	 */
	public double getSkew() {
		double mean = getMean();
		double sig = getSTD();
		return getSkew(mean, sig);
	}
	
	public double getSkew(double mean, double sig) {
		if (sig == 0) {
			return 0.;
		}
		double c = 0;
		if (mean == 0 && sig == 0) {
			return 0;
		}
		for (int i = 0; i < data.length; i++) {
			c += Math.pow(Math.abs((data[i]-mean)/sig), 3);
		}
		return c/data.length;
	}
	
	/**
	 * Get the nth moment of the distribution.
	 * @param i moment (skew = 3rd, kurtosis = 4th, etc)
	 * @return moment
	 */
//	public double getMoment(int i) {
//		return 0;
//	}
	
	
	/**
	 * Get the median of the dataset
	 * 
	 * MO 2015/05/17
	 * 
	 * @return the median value
	 */
	public double getMedian() {
		
		// if we've already calculated the median, return the value...
		if (medianAlreadyCalc) {
			return median;
			
		// if we haven't calculated it yet, do so now
		} else {
			
			// make a copy so we don't sort the original array
			double[] newData = data.clone();
			
			// sort the new array and find the midpoint
			int n = newData.length;
			Arrays.sort(newData);
			if (newData.length % 2 == 0) {	// if we have an even number of data points
			    median = ((double)newData[n/2] + (double)newData[n/2 - 1])/2;
			} else {						// if we have an odd number of data points
			    median = (double) newData[n/2];
			}
			medianAlreadyCalc=true;
			return median;
		}
	}	

	
	/**
	 * Get the median of the dataset passed to this method.  Note that the dataset passed
	 * will replace any dataset that is currently loaded into this object, and mean and stddev
	 * will be recalculated.  The median flag will also be reset, requiring a recalculation the
	 * next time it's requested.
	 * 
	 * MO 2015/05/17
	 * 
	 * @param data the data array to test
	 * @return the median value
	 */
	public double getMedian(double[] data) {

		// recalculate mean and stddev based on this dataset, and reset the median flag
		calculate(data);
		
		// calculate median
		return getMedian();		
	}
	
	
	/**
	 * Get the variance of the dataset already loaded in this class
	 * 
	 * MO 2015/05/17
	 * 
	 * @return the variance of the data
	 */
	public double getVariance() {
		return std*std;
	}
	
	
	/**
	 * Get the variance of the dataset passed to this method.  Note that the dataset passed
	 * will replace any dataset that is currently loaded into this object, and mean and stddev
	 * will be recalculated.  The median flag will also be reset, requiring a recalculation the
	 * next time it's requested.
	 * 
	 * MO 2015/05/17
	 * 
	 * @param data	the data array to test
	 * @return the variance
	 */
	public double getVariance(double[] data) {

		// recalculate mean and stddev based on this dataset, and reset the median flag
		calculate(data);
		
		// return the variance
		return getVariance();
    }
}
