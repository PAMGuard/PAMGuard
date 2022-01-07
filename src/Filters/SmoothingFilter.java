package Filters;

/**
 * Smoothing filter which is basically a moving abstract filter
 * but it operates once on a finite amount of data
 * and does not insert any delays (unlike MovingAveragefilter which
 * handles infinite streams, but adds a delay)
 * @author Doug Gillespie
 *
 */
public class SmoothingFilter {

	/**
	 * Smooth data with a moving average filter. 
	 * @param data data to smooth
	 * @param smooth - bins to smooth over. Should be odd, will be incremented if not. 
	 * @return smoothed data
	 */
	public static double[] smoothData(double[] data, int smooth) {
		if (smooth%2 == 0) {
			smooth++;
		}
		int len = data.length;
		int halfN = (smooth-1)/2;
		double[] padded = new double[len+2*halfN];
		for (int i = 0; i < len; i++) {
			padded[i+halfN] = data[i] / smooth;
		}
		double[] smoothData = new double[len];
		smoothData[0] = 0;
		for (int i = 0; i <= halfN; i++) {
			smoothData[0] += padded[i+halfN];
		}
		for (int i = 1; i < len; i++) {
			smoothData[i] = smoothData[i-1] + padded[i+2*halfN] - padded[i-1];
		}
		return smoothData;
	}
		
}
