package gpl.whiten;

import java.util.Arrays;

public class WhitenVector {
	
	private double mu;

	/**
	 * Static function for instant whitening of a single row of data
	 * using a scale whitening factor of 1.0. 
	 * @param data Data array. 
	 * @return whitened data
	 */
	public double[] whitenVector(double[] data) {
		return whitenVector(data, 1.0);
	}
	/**
	 * Static function for instant whitening of a single row of data. 
	 * @param data Data array. 
	 * @param whitenFac Whitening factor (default to 1). 
	 * @return whitened data
	 */
	public double[] whitenVector(double[] data, double whitenFac) {
		double[] sortedData = data.clone(); // need to check this is  ahard clone ! 
		Arrays.sort(sortedData);
		// now find the central 50%
		int nHalf = sortedData.length/2;
		double minDiff = sortedData[nHalf]-sortedData[0];
		int minInd = 0;
		double diff;
		for (int i = 1, j = nHalf+1; i < nHalf; i++, j++) {
			if ((diff = sortedData[j]-sortedData[i]) < minDiff) {
				minDiff = diff;
				minInd = i;
			}
		}
		int endInd = minInd + nHalf;
		double tot = 0.;
		for (int i = minInd; i < endInd; i++) {
			tot += sortedData[i];
		}
		mu = tot / nHalf;
		double[] whiteData = new double[data.length];
		for (int i = 0; i < data.length; i++) {
			whiteData[i] = data[i] - whitenFac * mu;
		}
		return whiteData;
	}

	/**
	 * @return the latest smoothing mean calculated in the last call to whitenVector(...)
	 */
	public double getMu() {
		return mu;
	}
	
}
