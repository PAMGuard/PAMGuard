package pamMaths;

import java.util.Arrays;

import com.sun.glass.ui.Pixels;

import PamUtils.complex.ComplexArray;
import signal.Hilbert;
import fftManager.FastFFT;

public class WignerTransform {

	
	/**
	 * Calculate Wigner transform of real data. 
	 * Will need to take the Hilbert transform and 
	 * then call the complex function. 
	 * @param doubleData double array of Wigner data
	 */
	public static double[][] wignerTransform(double[] doubleData) {
		int originalDataLength, packedDataLength;
		originalDataLength = doubleData.length;
		packedDataLength = FastFFT.nextBinaryExp(originalDataLength);
		doubleData = Arrays.copyOf(doubleData, packedDataLength);
		Hilbert h = new Hilbert();
		ComplexArray hData = h.getHilbertC(doubleData, packedDataLength);
		double[][] tData = transformData2(hData, packedDataLength);
		if (originalDataLength == packedDataLength) {
			return tData;
		}
		return Arrays.copyOf(tData, originalDataLength);
	}
	
	
	/**
	 * Calculate Wigner transform of real data. 
	 * Will need to take the Hilber transform and 
	 * then call the complex function. 
	 * @param doubleData double array of Wigner data
	 * @param wignerUpdate - callback listener which returns every calculated complex line of wigner plot
	 */
	public static double[][] wignerTransform(double[] doubleData, WignerUpdate wignerUpdate) {
		return wignerTransform(doubleData,  wignerUpdate, false);
	}
	
	/**
	 * Calculate Wigner transform of real data. 
	 * Will need to take the Hilber transform and 
	 * then call the complex function. 
	 * @param doubleData double array of Wigner data
	 * @param wignerUpdate - callback listener which returns every calculated complex line of wigner plot
	 * @param dump - true to dump data and save memory. If true function will always return null but data will have been
	 * passed through wignerUpdate listener. 
	 */
	public static double[][] wignerTransform(double[] doubleData, WignerUpdate wignerUpdate, boolean dump) {
		try {
			int originalDataLength, packedDataLength;
			originalDataLength = doubleData.length;
			packedDataLength = FastFFT.nextBinaryExp(originalDataLength);
			doubleData = Arrays.copyOf(doubleData, packedDataLength);
			if (wignerUpdate!=null && wignerUpdate.isCancelled()) return null; 
			Hilbert h = new Hilbert();
			ComplexArray hData = h.getHilbertC(doubleData, packedDataLength);
			if (wignerUpdate!=null && wignerUpdate.isCancelled()) return null; 
			
			double[][] tData = transformData2(hData, packedDataLength, wignerUpdate, dump);
			if (tData==null) return null; 
			if (originalDataLength == packedDataLength) {
				return tData;
			}
			return Arrays.copyOf(tData, originalDataLength);
		}
		catch(Exception e) {
			e.printStackTrace();
			return null; 
		}
	}
	
	/**
	 * Calculate the Wigner transform from data that has already been 
	 * Hilbert transformed. 
	 * @param complexData
	 * @return double array of Wigner data
	 */
	public static double[][] wignerTransform(ComplexArray complexData) {
		int originalDataLength, packedDataLength;
		originalDataLength = complexData.length();
		packedDataLength = FastFFT.nextBinaryExp(originalDataLength);
		double[][] tData = transformData2(complexData, packedDataLength);
		if (originalDataLength == packedDataLength) {
			return tData;
		}
		return Arrays.copyOf(tData, originalDataLength);
	}
	
	/** 
	 * Wigner transform of a complex array, padded if necessary to be 
	 * a power of 2 long. 
	 * This has been largely copied from the Matlab tfrwv library by F. Auger
	 * @param hData
	 * @param fftLength
	 * @return Wigner transform of the data. 
	 */
	private static double[][] transformData2(ComplexArray hData, int N) {
		return transformData2(hData,  N, null, false);
	}
	
	
	/** 
	 * Wigner transform of a complex array, padded if necessary to be 
	 * a power of 2 long. 
	 * This has been largely copied from the Matlab tfrwv library by F. Auger
	 * @param hData - the complex data for input
	 * @param N - the fft length (equivalent) of the Wigner plot
	 * @param wignerUpdate - listener triggered on calculation of every bin. 
	 */
	private static double[][] transformData2(ComplexArray hData, int N,  WignerUpdate wignerUpdate) {
		return transformData2(hData,  N, wignerUpdate, false);
	}
	
	
	

	/** 
	 * Wigner transform of a complex array, padded if necessary to be 
	 * a power of 2 long. 
	 * This has been largely copied from the Matlab tfrwv library by F. Auger
	 * 
	 * @param hData - the complex data for input
	 * @param N - the fft length (equivalent) of the Wigner plot
	 * @param wignerUpdate - listener triggered on calculation of every bin. 
	 * @param dumpData - if true then data is not saved into into an array and the function returns null. 
	 * The data is passed to the wignerUpdate listener. 
	 * @return Wigner transform of the data. 
	 */
	private static double[][] transformData2(ComplexArray hData, int N, WignerUpdate wignerUpdate, boolean dumpData) {
		ComplexArray[] tfr;
		
//		System.out.println("Start");
		if (!dumpData) {
			//if data is to be saved create a complex array
			tfr = new ComplexArray[N];
			for (int i = 0; i < N; i++) {
				tfr[i] = new ComplexArray(N);
				//			Complex.allocateComplexArray(N, N);
			}
		}
		else {
			//if data is dumped only need one complex array. 
			tfr = new ComplexArray[1];
			tfr[0] = new ComplexArray(N);
		}
		
//		System.out.println("Start 1: " + N);

		int xrow, xcol, taumax;
		int tau, indices;
		FastFFT fft = new FastFFT();
		ComplexArray hConj = hData.conj();
			
		xrow = N;
		xcol = 1;
		int iColN=0; 
		for (int iCol = 0; iCol < N; iCol++) {
			
//			System.out.println("////////////////////////////"); 

			
			taumax = min(iCol, xrow-iCol-1, Math.round(N)/2 - 1);
			for (tau = -taumax; tau <= taumax; tau++) {
				indices = (N+tau)%N ;
				//set a number in the complex array.
				tfr[iColN].set(indices, hData.times(iCol+tau, 0.5).times(hConj.get(iCol-tau))); 
//				System.out.println("Index: " + indices + " get real: "+tfr[iColN].getReal(indices));
			}
			
			tau = Math.round(N/2);
			if (iCol < xrow-tau && iCol >= tau) {
				tfr[iColN].set(tau, hData.get(iCol+tau).times(hConj.get(iCol-tau)).plus(
						hData.get(iCol-tau).times(hConj.get(iCol+tau))).times(.5));
			}
			
			
			//call cancel function before potentially large FFT calculation. 
			if (wignerUpdate!=null && wignerUpdate.isCancelled()) return null; 
			//04/08/2017 - moved inside this loop. This was so callback could be used 
			//and data dumped to save memory ofr larger wigner plots. 
			fft.fft(tfr[iColN]); 
			
			//callback for when multi threaded
			if (wignerUpdate!=null){
				//cancel the calculation
				if (wignerUpdate.isCancelled()) {
//					System.out.println("The wigner update has been CANCELLED");
					return null; 
				}

				//call the update function to send data to external callback
				wignerUpdate.wignerUpdated(tfr[iColN], iCol);
//				System.out.println("/////////TESTTEST////////////"); 
//				for (int i=0; i< tfr[iColN].length(); i++) {
//					System.out.println("Index: " + i + " get real: "+tfr[iColN].getReal(i));
//				}
			}
			
			if (!dumpData) iColN++; //only update the index if not dumping data 
			else tfr[iColN]=new ComplexArray(N); 

		}
		
		//04/08/2017 - moved to inside above loop
//		for (int i = 0; i < N; i++) {
//			fft.fft(tfr[i]);
//		}
		
		
		//now dump the data into an array 
		if (!dumpData) {
			double[][] d = new double[N][N];
			for (int i = 0; i < N; i++) {
				d[i] = new double[N];
			}

			for (int i = 0; i < N; i ++) {
				for (int j = 0; j < N; j++) {
					d[i][j] = tfr[i].getReal(j);
				}
			}
			return d; 
		}
		else return null; //have dumped data so nothing to shown. 

	}
	

	 public static double getMaxValue(double[][] array) {
		 double max = array[0][0];
		 for (int i = 0; i < array.length; i++) {
			 for (int j = 0; j < array[i].length; j++) {
				 max = Math.max(max, array[i][j]);
			 }
		 }
		 return max;
	 }
	 public static double getMinValue(double[][] array) {
		 double min = array[0][0];
		 for (int i = 0; i < array.length; i++) {
			 for (int j = 0; j < array[i].length; j++) {
				 min = Math.min(min, array[i][j]);
			 }
		 }
		 return min;
	 }
	
	private static int min(int a, int b, int c) {
		return Math.min(a, Math.min(b, c));
	}
	
	/**
	 * 
	 * Called whenever a wigner calculation is updated. Can be used to put wigner plot on a new thread. 
	 * @author Jamie Macaulay
	 *
	 */
	public interface  WignerUpdate{
		
		/**
		 * Called whenever a line in the wigner plot us updated.
		 * @param tfr, int line
		 */
		public void wignerUpdated(ComplexArray tfr, int line);
		
		/**
		 * Check whether to cancel the wigner calculation
		 * @return true to cancel. 
		 */
		public boolean isCancelled();
		
	}
}
