package pamMaths;

public class Mean {

	/**
	 * Calculate the mean of all data in an array
	 * @param data
	 * @return mean of data
	 */
	public static double getMean(double[] data) {
		return getMean(data, data.length);
	}

	/**
	 * Calculate the mean of n elements in the array
	 * @param data
	 * @param n
	 * @return mean of first n points
	 */
	public static double getMean(double[] data, int n) {
		double a = 0;
		for (int i = 0; i < n; i++) {
			a += data[i];
		}
		return a/n;
	}
}
