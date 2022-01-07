package gpl.whiten;

import java.util.Random;

/**
 * Class to conduct an infinite sort of data. That is a finite amount of data are sorted, but
 * as PAMGuard processes, one sample is added in and one taken out at each iteration. Therefore 
 * after the initial sorting of the data, it's relatively fast to just find the position of 
 * each new data point.   
 * @author Doug Gillespie
 *
 */
public class InfiniteSort {

	/**
	 * index 0 contains the oldest data, and index nPoints-1 the latest. 
	 */
	private double[] data;
	
	/**
	 * Knowing a position in the order, this tells us what the 
	 * data position is in the data array. 
	 */
	private int[] sortInd;

	/**
	 * Number of points in the sorted data. 
	 */
	private int nPoints;
	
	private int totalCalls = 0;

	private int debugStop = -1;
	
	private int autoInitialise = -1;
	
	private int currentIndex;
	
	public InfiniteSort(int nPoints) {
		this.nPoints = nPoints;
		data = new double[nPoints];
		sortInd = new int[nPoints];
		// initialise sort indexes.
		for (int i = 0; i < nPoints; i++) {
			sortInd[i] = i;
			// set all data high, so that if we ask for a mean when the array is only 
			// partially full, we know all the unused values will be at the end. 
			data[i] = Double.MAX_VALUE;
		}
		currentIndex = nPoints-1; // only used in the faster version of the sort. 
	}
	
	/**
	 * Add data to the sorted list. The oldest data point will 
	 * drop off the list and the correct index of the new data point
	 * will be found. Data are always left in place (unsorted) in the data
	 * list and modifications are only to the sorted index data.  
	 * @param d New data value. 
	 * @return the value of the data removed from the list (i.e. the oldest value)
	 */
	public double addData(double d) {
		totalCalls++;
		if (Double.isFinite(d) == false) {
			return Double.NaN;
		}
//		if (totalCalls == debugStop) {
//			System.out.println("Debug Stop");
//		}
		double rVal;
		rVal = addAndSortFast(d);
		
		if (totalCalls == autoInitialise) {
			// fill the entire sort vector with a repeat of the 
			// initial rows. Looping through existing data until 
			// the entire data array has been filled. 
			int r1 = 0;//nPoints-totalCalls;
			int nToFill = nPoints-autoInitialise;
			for (int i = 0; i < nToFill; i++) {
				addData(data[r1]);
				if (++r1 == totalCalls) {
					r1 = 0;
				}
			}
		}
		
		return rVal;
	}
	
	/**
	 * Add the data into the array and sort it. 
	 * @param d data
	 * @return removed value. 
	 */
	private double addAndSort(double d) { 
		int firstInd = -1;
		for (int i = 0, j = 1; j < nPoints; i++, j++) {
			data[i] = data[j]; // shift the data along one
		}
		for (int i = 0; i < nPoints; i++) {
			sortInd[i]--;
			if (sortInd[i] < 0) {
				firstInd = i; // index of the one we just lost. 
			}
		}
		double removedData = data[nPoints-1];
		data[nPoints-1] = d;
		sortInd[firstInd] = nPoints-1;
		/*
		 *  now we know that the new point is as position k. and that
		 *  it's the only item which may not be in order. So all we have to do 
		 *  is a swap sort in both directions. If one direction has changes, then 
		 *  it's not even necessary to search the other direction. 
		 */
		int nSwaps = 0;
		for (int i = firstInd; i < nPoints-1; i++) {
			if (d > data[sortInd[i+1]]) {
				swap(sortInd, i, i+1);
				nSwaps++;
			}
			else {
				break;
			}
		}
		if (nSwaps == 0) {
			/**
			 * Only search downwards if there wasn't 
			 * movement in the upward direction. 
			 */
			for (int i = firstInd; i>=1; i--) {
				if (d < data[sortInd[i-1]]) {
					swap(sortInd, i, i-1);
				}
				else {
					break;
				}
			}
		}
		// diagnostic printout
//		System.out.printf("Sorted Data: %3.0f", data[sortInd[0]]);
//		for (int i = 1; i < nPoints; i++) {
////			System.out.printf(", %3.0f", data[sortInd[i]]);
//			if (data[sortInd[i]] < data[sortInd[i-1]]) {
//				System.out.printf("(Sort Error call %d !!!!, args)", totalCalls);
//			}
//		}
//		System.out.printf("\n");
		return removedData;
	}
	
	/**
	 * Add the data into the array and sort it. <p>
	 * Attempts to run faster than addAndSort by leaving 
	 * original data in a circular rather than a shifting buffer
	 * and by using a binary division search for the new data position
	 * which should work if we already know that the rest of the data are
	 * in order. 
	 * <p>Runs about 60% faster than addAndSort(...)
	 * @param d data
	 * @return removed value. 
	 */
	private double addAndSortFast(double d) { 
		if (++currentIndex == nPoints) currentIndex = 0;
		double removedData = data[currentIndex];
		int firstInd = -1;
		for (int i = 0; i < nPoints; i++) {
			if (sortInd[i] == currentIndex) {
				firstInd = i; // sorted index of the data we're replacing. 
			}
		}
		data[currentIndex] = Double.NEGATIVE_INFINITY;
		sortInd[firstInd] = currentIndex;
		/*
		 *  now we know that the new point is as position firstInd. and that
		 *  it's the only item which may not be in order. So all we have to do 
		 *  is a swap sort in both directions. If one direction has changes, then 
		 *  it's not even necessary to search the other direction. 
		 */
//		if (totalCalls >= 22) {
//			System.out.printf("stop %d, removed %3.1f\n", totalCalls, removedData);
//		}
		int sortPos = -1;
		if (firstInd < nPoints - 1 && d > data[sortInd[firstInd+1]]) {
			// sort upwards.
			sortPos = binarySearch(d, firstInd, firstInd, nPoints-1);
//			System.out.printf("Sort %3.1f up from %d to %d, ", d, firstInd, sortPos);
			/**
			 * Now all the values between firstInd and sortPos need 
			 * to shift down 1.
			 */
			for (int i = firstInd; i < sortPos; i++) {
				sortInd[i] = sortInd[i+1];
			}
			sortInd[sortPos] = currentIndex;
		}
		else {
			// sort downwards.
			sortPos = binarySearch(d, -1, 0, firstInd-1)+1;
//			System.out.printf("Sort %3.1f down from %d to %d, ", d, firstInd, sortPos);
			/**
			 * Now all the values between firstInd and sortPos need 
			 * to shift up 1.
			 */
			for (int i = firstInd; i > sortPos; i--) {
				sortInd[i] = sortInd[i-1];
			}
			sortInd[sortPos] = currentIndex;
		}
		data[currentIndex] = d;
		
		// diagnostic printout
//		System.out.printf("Sorted Data: %3.1f", data[sortInd[0]]);
//		for (int i = 1; i < nPoints; i++) {
//			System.out.printf(", %3.1f", data[sortInd[i]]);
//			if (data[sortInd[i]] < data[sortInd[i-1]]) {
//				System.out.printf("(Sort Error call %d !!!!, args)", totalCalls);
//			}
//		}
//		System.out.printf("\n");
		return removedData;
	}

	/**
	 * Recursive search to find the index of d between 
	 * indexed ind1 and ind2 using some kind of binary division. 
	 * @param d data
	 * @param ind1 first search index
	 * @param ind2 last search index (inclusive)
	 * @return index of d between ind1 and ind2 inclusive. 
	 */
	private int binarySearch(double d, int currentInd, int ind1, int ind2) {
		if (ind2-ind1 <= 5) {
			// down to the last few entries so just get the index
			// by going through the list...
			int k = currentInd;
			for (int i = ind1; i <= ind2; i++) {
				if (d >= data[sortInd[i]]) {
					k = i;
				}
				else {
					break;
				}
			}
			return k;
		}
		int mid = (ind1 + ind2) / 2;
		if (d >= data[sortInd[mid]]) {
			return binarySearch(d, currentInd, mid, ind2);
		}
		else {
			return binarySearch(d, currentInd, ind1, mid);
		}
	}

	/**
	 * Swap a pair of sort indexes into the data 
	 * @param sortInd list of sort indexes
	 * @param ind1 first index to swap 
	 * @param ind2 second index to swap
	 */
	private void swap(int[] sortInd, int ind1, int ind2) {
		int val = sortInd[ind1];
		sortInd[ind1] = sortInd[ind2];
		sortInd[ind2] = val;
	}
	
	public boolean checkSort() {
		int nUsed = Math.min(nPoints,  totalCalls);
		for (int i = 1; i < nUsed; i++) {
			if (data[sortInd[i]] < data[sortInd[i-1]]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @return The median value from the sorted data. 
	 */
	public double getMedian() {
		int nUsed = Math.min(nPoints,  totalCalls);
		return data[sortInd[nUsed/2]];
	}

	/**
	 * Get a mean value about the central tendency of the data. This 
	 * function first searches for the 50% of bins of data which are 
	 * the most similar, then takes the mean of the data within that 
	 * range.  
	 * @return Mean value about the central tendency. 
	 */
	public double getCentralMean() {
		/**
		 * It's possible that this will be called before the array is completely full, in 
		 * which case, we should only sort parts of it. 
		 */
		int nUsed = Math.min(nPoints,  totalCalls);
		return getCentralMean(nUsed, nUsed/2);
	}

	/**
	 * Get a mean value about the central tendency of the data. This 
	 * function first searches for the nMean bins of data which are 
	 * the most similar, then takes the mean of the data within that 
	 * range.  
	 * @param nMean Number of bins to calculate over. Must be <= nPoints 
	 * @return Mean value about the central tendency. 
	 */
	public double getCentralMean(int usedPoints, int nMean) {
		int nSearch = nMean;
		double minDiff = data[sortInd[nPoints-1]] - data[sortInd[0]];
		int minInd = 0;
		double diff;
		for (int i = 0, j = nMean; i < nSearch; i++, j++) {
			if ((diff = data[sortInd[j]]-data[sortInd[i]]) < minDiff) {
				minDiff = diff;
				minInd = i;
			}
		}
		double tot = 0;
		int endInd =  Math.min(nMean+minInd, usedPoints-1);
		int n = 0;
		double[] usedDat = new double[endInd-minInd+1];
		for (int i = minInd; i <= endInd; i++) {
			tot += data[sortInd[i]];
			usedDat[n] = data[sortInd[i]];
			n++;
		}
		return tot/n;
	}

	/**
	 * Array of data values. Note that these remain unsorted. The 
	 * oldest data will be at index 0 and the latest at index (nPoints-1)
	 * @return the data
	 */
	public double[] getData() {
		return data;
	}
	
	/**
	 * Get a single value at a specific sorted position. 
	 * @param sortPosition sort position
	 * @return data value for that index. 
	 */
	public double getSortedData(int sortPosition) {
		return data[sortInd[sortPosition]];
	}

	/**
	 * Sort order of the data. Data themselves are left in their original 
	 * unsorted order, so these indexes are used to get data in the 
	 * correct order, i.e. data[sortInd[0]] will be the data having the 
	 * lowest value and data[sortind[nPoints-1]] will be the data having
	 * the highest value. 
	 * @return the sortIndexes
	 */
	public int[] getSortInd() {
		return sortInd;
	}

	/**
	 * The number of points in the sort 
	 * @return the nPoints
	 */
	public int getnPoints() {
		return nPoints;
	}

	public void setStop(int i) {
		debugStop  = i;
	}

	/**
	 * @return the autoInitialise
	 */
	public int getAutoInitialise() {
		return autoInitialise;
	}

	/**
	 * @param autoInitialise the autoInitialise to set
	 */
	public void setAutoInitialise(int autoInitialise) {
		this.autoInitialise = autoInitialise;
	}

	public static void main(String[] args) {
			double[] td = { 1., 8, 7, 7, 4, 7, 3, 5, 2, 5};
			Random random = new Random(10);
			InfiniteSort is = new InfiniteSort(100);
			td = new double[100000];
			for (int i = 0; i < td.length; i++) {
				td[i] = random.nextGaussian()*200;
			}
			long nn = System.currentTimeMillis();
			for (int i = 0; i < 100000; i++) {
//				System.out.printf("%3.1f: ", r);
				is.addData(td[i]);
			}
			double cm = is.getCentralMean();
			long n2 = System.currentTimeMillis();
			System.out.printf("Test took %d millis", n2-nn);
	//		is.addData(50);
		}

	/**
	 * @return the totalCalls
	 */
	public int getTotalCalls() {
		return totalCalls;
	}

}
