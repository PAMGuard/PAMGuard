package PamUtils;

/**
 * Class to sort indexes, leaving original data intact. 
 * Copied shamelessly from the code Paul White provided 
 * for the median filter in the Whistle and Moan detector. 
 * @author Doug Gillespie
 *
 */
public class BubbleSort {
	
	/**
	 * Bubble sort. Sorts both the data
	 * and also an array of indices.
	 * Data and indices arrays must be the same length.
	 * indices array need not be initialised. 
	 * @param data input data
	 * @param indices sorted output indices
	 */
	public static void sortAcending(double[] data, int[] indices)
	{
	    int i;
	    int n = data.length;
	    for (i = 0; i < n; i++) {
	    	indices[i] = i;
	    }
	    int newn; 
	    while (n > 1) {
	    	newn = 0;
	    	for (i = 0; i < n-1; i++) {
	    		if (data[i] > data[i+1]) {
	    			swap(data, i, i+1);
	    			swap(indices, i, i+1);
	    			newn = i+1;
	    		}
	    	}
	    	n = newn;
	    }
	    
	}

	/**
	 * Bubble sort. Sorts both the data
	 * and also an array of indices.
	 * Data and indices arrays must be the same length.
	 * indices array need not be initialised. 
	 * @param data input data
	 * @param indices sorted output indices
	 */
	public static void sortAcending(int[] data, int[] indices)
	{
	    int i;
	    int n = data.length;
	    for (i = 0; i < n; i++) {
	    	indices[i] = i;
	    }
	    int newn; 
	    while (n > 1) {
	    	newn = 0;
	    	for (i = 0; i < n-1; i++) {
	    		if (data[i] > data[i+1]) {
	    			swap(data, i, i+1);
	    			swap(indices, i, i+1);
	    			newn = i+1;
	    		}
	    	}
	    	n = newn;
	    }
	}

	/**
	 * Bubble sort. Sorts both the data
	 * and also an array of indices.
	 * Data and indices arrays must be the same length.
	 * indices array need not be initialised. 
	 * @param data input data
	 * @param indices sorted output indices
	 */
	public static void sortAcending(long[] data, int[] indices)
	{
	    int i;
	    int n = data.length;
	    for (i = 0; i < n; i++) {
	    	indices[i] = i;
	    }
	    int newn; 
	    while (n > 1) {
	    	newn = 0;
	    	for (i = 0; i < n-1; i++) {
	    		if (data[i] > data[i+1]) {
	    			swap(data, i, i+1);
	    			swap(indices, i, i+1);
	    			newn = i+1;
	    		}
	    	}
	    	n = newn;
	    }
	}
	
	/**
	 * Swap two elements in an array 
	 * @param array
	 * @param ind1
	 * @param ind2
	 */
	private static void swap(int[] array, int ind1, int ind2) {
		int dum = array[ind1];
		array[ind1] = array[ind2];
		array[ind2] = dum;
	}
	
	/**
	 * Swap two elements in an array
	 * @param array
	 * @param ind1
	 * @param ind2
	 */
	private static void swap(double[] array, int ind1, int ind2) {
		double dum = array[ind1];
		array[ind1] = array[ind2];
		array[ind2] = dum;
	}
	/**
	 * Swap two elements in an array
	 * @param array
	 * @param ind1
	 * @param ind2
	 */
	private static void swap(long[] array, int ind1, int ind2) {
		long dum = array[ind1];
		array[ind1] = array[ind2];
		array[ind2] = dum;
	}
}
