package Filters;

//import org.apache.commons.math.distribution.HypergeometricDistributionImpl;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

/**
 * Calculates a Chebychev window for use in FOR filter design. 
 * @author Doug Gillespie
 *
 */
public class ChebyWindow {

	/**
	 * Calculate a Chebychev window function.
	 * <p>
	 * When used in a filter, the filter will generally have an odd number of taps. This function
	 * will return an even number of window values. You should use the 0th to n-2th values, which will 
	 * be symmetrical about a central value of 1.0. 
	 * @param nPoint number of points
	 * @param gamma Gamma - typically 3, get 20dB * gamma attenuation in filter stop band 
	 * @return window function
	 */
	public static double[] getWindow(int nPoint, double gamma) {
		DoubleFFT_1D fft = new DoubleFFT_1D(nPoint);
		double winData[] = new double[nPoint];
		double alpha = Math.cosh(acosh(Math.pow(10., gamma))/nPoint);
		double A[] = new double[nPoint];
		double w[] = new double[nPoint*2];
		double wr;
		
		for (int m = 0; m < nPoint; m++){
			A[m] = Math.abs(alpha*Math.cos(Math.PI*m/nPoint));
			if (A[m] > 1) {
				wr = Math.pow(-1, m)*Math.cosh(nPoint*acosh(A[m]));
			}
			else {
				wr = Math.pow(-1, m)*Math.cos(nPoint*Math.acos(A[m]));
			}
			w[m*2] = wr;
		}
		fft.complexInverse(w, true);
		for (int i = 0; i < nPoint; i++) {
//			winData[i] = Math.sqrt(Math.pow(w[i*2],2)+Math.pow(w[i*2+1],2));
			winData[i] = w[i*2];
		}
		double fst = winData[0]/2.;
		for (int i = 0; i < nPoint-1; i++) {
			winData[i] = winData[i+1];
		}
		winData[nPoint-1] = fst;
		double maxVal = 0;
		for (int i = 0; i < nPoint-1; i++) {
			maxVal = Math.max(maxVal, winData[i]);
		}
		for (int i = 0; i < nPoint-1; i++) {
			winData[i] /= maxVal;
		}
		
		
		
		
		
		return winData;
	}
	
	/**
	 * Calculate acosh function. 
	 * Returns NaN for input values < 1
	 * @param x input
	 * @return cosh of values >= 1 or NaN
	 */
	public static double acosh(double x) {
		if (x < 1) {
			return Double.NaN;
		}
		return Math.log(x+Math.sqrt(x*x-1));
	}
}
