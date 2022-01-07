package whistlesAndMoans;

/**
 * 
 * Median Filter for a finite amount of data. Copied from 
 * Paul Whites C code in medfilt_prw_c.c
 * @author Doug Gillespie
 *
 */
public class MedianFilter {

	int[] indices;
	double[] xsrt;
	double[] x;
	
	/**
	 * Median Filter for a finite amount of data. Copied from 
	 * Paul Whites C code in medfilt_prw_c.c
	 * @param inputData array of input data
	 * @param outputData output of filter 
	 * @param N width of median filter. 
	 */
	public void medianFilter(double[] inputData, double[] outputData, int N) {
		
		int arg, index, k, kmin, imin;
		double xNew;
		
		int L = inputData.length;
		if (outputData == null || outputData.length != L) {
			outputData = new double[L];
		}
		if (N%2 == 0) {
			N++;
		}
		int lZ=(N-1)/2;
		
		int lIndices = N+2;
		int lXsrt = N+2;
		int lX = 2*lZ+L+1;
		// recreate arrays - saves setting all to zero (default in java)
		indices = new int[lIndices];
		xsrt = new double[lXsrt];
		x = new double[lX];
		
		// put the data into the middle of a bigger array
		for (k = 0; k < L; k++) {
			x[k+lZ] = inputData[k];
		}
		// don't pad with zeros. Pad with the beginning and end of the real data
		// so that the fist and last bins get something sensible.
		for (k = 0; k < lZ; k++) {
			x[k] = inputData[k];
			x[lX-k-1] = inputData[L-k-1];
		}
		
		// define the initial indices
		for (k = 0; k < N; k++) {
			indices[k] = k;
			xsrt[k]=x[k];
		}
		
		bubble(xsrt, N, indices);
		
		// first median output
		outputData[0] = xsrt[lZ];
		
		// looping over remaining samples
		for (int n = 1; n < L; n++){
			// New Sample
			arg = n+N-1;
			xNew = x[arg];
			// find first value in the sorted list bigger than the new sample
			for (k = 0; k < N; k++) {
				if (xsrt[k] > xNew) {
					break;
				}
			}
			index = k;
	        // Insert new value at the correct location
	        
	        // First - shift sorted elements up one
	        for (k=N; k>=index; k--) {
	            xsrt[k+1]=xsrt[k];
	            indices[k+1]=indices[k];
	        }
	        
	        // Second - insert new value
	        xsrt[index]=xNew;
	        indices[index]=arg;
	        
	        // Find oldest value in sorted list
	        kmin=0;
	        imin=indices[0];
	        for (k=1; k<=N ; k++)
	            if (imin>indices[k]) {
	                kmin=k;
	                imin=indices[k];
	            }
	        
	        // Delete oldest value - shift values down overwritting old value
	        for (k=kmin+1; k<=N; k++) {
	            xsrt[k-1]=xsrt[k];
	            indices[k-1]=indices[k];
	        }
	        
	        // Output median
	        outputData[n]=xsrt[lZ];
		}
	}
	
	/**
	 * Bubble sort. Data are sorted in descending order. Function
	 * also returns a set of ordered indices in descending order
	 * The indices need to be initialised to a correct order 
	 * before anything starts !
	 * @param x input data
	 * @param L number of points
	 * @param y sorted output indices
	 */
	private void bubble(double[] x, int L, int[] y)
	{
	    int n;
	    boolean flag=true;
	    	    
	    while (flag) {
	        flag=false;
	        for (n=1; n<L; n++) {
	            if (x[n]<x[n-1]) {
	                swap(x, n-1, n);
	                swap(y, n-1, n);
	                flag=true;
	            }
	        }
	    }
	}
	
	void swap(int[] array, int ind1, int ind2) {
		int dum = array[ind1];
		array[ind1] = array[ind2];
		array[ind2] = dum;
	}
	
	void swap(double[] array, int ind1, int ind2) {
		double dum = array[ind1];
		array[ind1] = array[ind2];
		array[ind2] = dum;
	}
}
