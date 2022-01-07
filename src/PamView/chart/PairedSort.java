package PamView.chart;

import PamguardMVC.debug.Debug;

/**
 * functions for sorting paired arrays, i.e. order the first array and
 * then move second array around so that it still matches the first array. 
 * @author dg50
 *
 */
public class PairedSort {
	

//    public static void main(String[] args) { 
//    	double[] testArray = {-1., -3, -5, 5, -6, 10, 20};
//    	int[] sortInds = getSortIndexes(testArray);
//    	
//    }
	
	/**
	 * sort a 2d array by the content of it's first element
	 * @param array d2 array to sort. 
	 * @return a new array. Old content not overwritten
	 */
	public static double[][] sortPairedArray(double[][] array) {
		int[] inds = getSortIndexes(array[0]);
		int nCol = array[0].length;
		double[][] newArray = new double[nCol][inds.length];
		for (int i = 0; i < inds.length; i++) {
			for (int j = 0; i < nCol; i++) {
				newArray[j][i] = array[j][inds[i]];
			}
		}
		return newArray;
	}

	public static int[] getSortIndexes(double[] array) {
		int[] inds = new int[array.length];
		for (int i = 1; i < inds.length; i++) {
			inds[i] = i;
		}
		/* 
		 * now do a bubble sort of the data in array, but never move
		 * anything in array, just swap round the indexes, 
		 */
		int lastSwap = array.length-1;
		while (lastSwap > 0) {
			int last = lastSwap;
			for (int iA = 0, iB = 1; iB < last; iA++, iB++) {
				if (array[inds[iA]] > array[inds[iB]]) {
					lastSwap = iA;
					swap(inds, iA, iB);
				}
			}
		}
		return inds;
	}
	
	/**
	 * Swap values in the array at the two indexes.
	 * @param indexes
	 * @param ind1
	 * @param ind2
	 */
	private static void swap(int[] indexes, int ind1, int ind2) {
		int v1 = indexes[ind1];;
		indexes[ind1] = indexes[ind2];
		indexes[ind2] = v1;
	}
}
