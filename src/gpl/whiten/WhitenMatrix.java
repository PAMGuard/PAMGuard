package gpl.whiten;

import java.util.Arrays;
import java.util.Collections;

public class WhitenMatrix {

	private int nTimeBins, nRows;
	private double whitenFac = 1.;
	private InfiniteSortGroup sortGroup;
	
	/**
	 * Create a standard whitener for real data. 
	 * @param nRows number of data rows (number of frequency bins)
	 * @param nTimeBins number of time bins (history lenght of whitener)
	 * @param whitenFac whitening factor (default to 1)
	 */
	public WhitenMatrix(int nRows, int nTimeBins, double whitenFac) {
			super();
			this.nRows = nRows;
			this.nTimeBins = nTimeBins;
			this.whitenFac = whitenFac;
			sortGroup = new InfiniteSortGroup(nRows, nTimeBins);
	}
	
	/**
	 * Add data to the whiteners sort group. 
	 * @param dataCol array of data. 
	 */
	public void addData(double[] dataCol) {
		sortGroup.addData(dataCol);
	}
	/**
	 * Whitens a column of data points. Does not add to the sorted data, call
	 * addData before or after if you need to do that. 
	 * @param dataCol column of data. 
	 * @return whitened data. 
	 */
	public double[] whitenData(double[] dataCol) {
//		sortGroup.addData(dataCol);
		double[] centralMean = sortGroup.getCentralMean();
		double[] whiteData = new double[nRows];
		for (int i = 0; i < nRows; i++) {
			whiteData[i] = dataCol[i] - whitenFac*centralMean[i];
		}
		return whiteData;
	}
	
	/**
	 * find the central mean of all data in a 2D array. 
	 * @param array
	 * @return
	 */
	public static double quickCentralMean(double[][] array) {
		if (array == null || array.length == 0 || array[0].length == 0) {
			return 0;
		}
		int nx = array.length;
		int ny = array[0].length;
		double[] toSort = new double[nx*ny];
		for (int ix = 0, i = 0; ix < nx; ix++) {
			for (int iy = 0; iy < ny; iy++, i++) {
				toSort[i] = array[ix][iy];
			}
		}
		Arrays.sort(toSort);
		int n = toSort.length/2;
		double minDiff = toSort[toSort.length-1] - toSort[0];
		int minStart = 0;
		for (int i = 0, j = n; i < n; i++, j++) {
			if (toSort[j]-toSort[i] < minDiff) {
				minDiff = toSort[j]-toSort[i];
				minStart = i;
			}
		}
		double mu = 0.;
		for (int i = 0, j = minStart; i < n; i++, j++) {
			mu += toSort[j];
//			System.out.printf("%4.2f,",toSort[j]);
		}
		return mu/n;
		
	}
}
