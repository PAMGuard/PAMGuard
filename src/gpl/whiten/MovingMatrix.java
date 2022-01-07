package gpl.whiten;

/**
 * A matrix for moving average calculations, initially designed to 
 * replicate some functionality in SIO GPL_Quiet function.
 * <p>
 * Will store squared data in the matrix to save on later squaring ...
 * @author dg50
 *
 */
public class MovingMatrix {

	private int nTime, nFreq;
	
	private double[] sumTime2;
	
	private double[] sumFreq2;
	
	private double[][] data;
	
	private int iTime = -1;
	
	private int autoInitialise = 25;
	
	private int totalCalls = 0;

	public MovingMatrix(int nTime, int nFreq) {
		super();
		this.nTime = nTime;
		this.nFreq = nFreq;
		iTime = nTime-1;
		data = new double[nTime][nFreq];
		sumFreq2 = new double[nFreq];
		sumTime2 = new double[nTime];
	}
	
	/**
	 * Add a new row of data to the matrix. 
	 * @param newSlice
	 */
	public void addData(double[] newSlice) {
		addSquares(newSlice);
//		if (++totalCalls == autoInitialise) {
//			int nCurrent = iTime+1;
//			int i = 0;
//			double[] magData = new double[nFreq];
//			while (iTime < nTime-1) {
//				for (int j = 0; j < nFreq; j++) {
//					magData[j] = Math.sqrt(data[i][j]);
//				}
//				addSquares(magData);
//				if (++i == nCurrent) {
//					i = 0;
//				}
//			}
//		}
	}
	
	
	private void addSquares(double[] newSlice) {
		/*
		 * 1. Take off existing data from the current row from the sumFreq2
		 * 2. replace row
		 * 3. Add back data to sumFreq2
		 * 4. Work out the values for the new sumTime2 for that iTime
		 * 5. Increment iTime
		 */
		if (++iTime >= nTime) iTime = 0;
		double val;
		sumTime2[iTime] = 0;
		for (int i = 0; i < nFreq; i++) {
			sumFreq2[i] -= data[iTime][i];
			val = newSlice[i]*newSlice[i];
			if (Double.isFinite(val)) {
				sumFreq2[i] += val;
				data[iTime][i] = val;
				sumTime2[iTime] += val;
			}
			else {
				// this stops a value being taken out twice if one couldn't be put in 
				// because it was infinite. 
				data[iTime][i] = 0;
			}
		}
	}

	/**
	 * @return the nTime
	 */
	public int getnTime() {
		return nTime;
	}

	/**
	 * @return the nFreq
	 */
	public int getnFreq() {
		return nFreq;
	}

	/**
	 * @return the sumTime2
	 */
	public double[] getSumTime2() {
		return sumTime2;
	}

	/**
	 * Get the energy summed over time in each frequency bin
	 * @return the sumFreq2
	 */
	public double[] getSumFreq2() {
		return sumFreq2;
	}

	/**
	 * Get the index of the last time bin to be filled. 
	 * @return the iTime
	 */
	public int getiTime() {
		return iTime;
	}

	/**
	 * Get the index of a time bin which is offset beins earlier than the
	 * current bin. 
	 * @param offset positive number saying how far back in time to go. 0 == last entry. 
	 * @return bin number based on current bin index and the offset. 
	 */
	public int getTimeIndex(int offset) {
		int ind = iTime-offset;
		while (ind < 0) {
			ind += nTime;
		}
		return ind;
	}
	
	
}
