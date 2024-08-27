package signal;

import java.util.Arrays;

import PamUtils.complex.ComplexArray;
import fftManager.FastFFT;

/**
 * Functions to calculate the Hilbert transform of data.
 *  
 * @author Doug Gillespie
 *
 */
public class Hilbert {

	private FastFFT fastFFT = new FastFFT();
//	private Complex[] storedFFTArray = null;
//	private Complex[] fullFFTArray = null;
	private double[] storedData1221 = null;
	
	

	/**
	 * Calculate the Hilbert Transform of a sample of data. 
	 * and return as a complex array of the magnitude
	 * 
	 * @param signal signal waveform
	 * @return Complex Hilbert Transform of the data. 
	 */
	public synchronized ComplexArray getHilbertC(double[] signal) {
		int fftLength = FastFFT.nextBinaryExp(signal.length);
		ComplexArray fullFFTData = getHilbertC(signal, fftLength);
		if (fullFFTData.length() == fftLength) {
			return fullFFTData;
		}
		else
			return fullFFTData.copyOf(signal.length);
//			return Arrays.copyOf(fullFFTData, signal.length);
	}
	
	/**
	 * Calculate the Hilbert Transform of a sample of data. 
	 * and return as a complex array of the magnitude
	 * 
	 * @param signal signal waveform
	 * @param signal length (will truncate or stretch to this)
	 * @return Complex Hilbert Transform of the data. 
	 */
	public synchronized ComplexArray getHilbertC(double[] signal, int fftLength) {	

//		int logFFTLen = FastFFT.log2(fftLength);
		
		if (fftLength == 0) {
			return null;
		}

//		makeStorageArrays(fftLength);
		
		ComplexArray fftArray = fastFFT.rfft(signal, fftLength);

		
		return getHilbertC(fftArray, fftLength);


	}

//	/**
//	 * Compute a Hilbert transform from a Complex spectrum of real data. 
//	 * for some parts of PAMguard for which the complex spectrum is already available, 
//	 * this may be a bit quicker. 
//	 * @param fftData Complex FFT data. Only the first half (fftLength/2 long) is needed. 
//	 * @param fftLength FFT Length
//	 * @return Complex Hilbert Transform of the data. 
//	 */
//	public synchronized ComplexArray getHilbertC(ComplexArray fftData, int fftLength) {
//
//		makeStorageArrays(fftLength);
//		double[] data121 = get1221Data(fftLength);
//		int i = 0;
//		for (; i < fftLength/2; i++) {
//			fullFFTArray[i] = fftData[i].times(data121[i]);
//			fullFFTArray[fftLength-i-1].assign(0,0);
//		}
//
//		int logFFTLen = FastFFT.log2(fftLength);
//
//		fastFFT.ifft(fullFFTArray,logFFTLen);
//
//		return fullFFTArray;
//	}
	
	/**
	 * Compute a Hilbert transform from a Complex spectrum of real data. 
	 * for some parts of PAMguard for which the complex spectrum is already available, 
	 * this may be a bit quicker. 
	 * @param fftData Complex FFT data. Only the first half (fftLength/2 long) is needed. 
	 * @param fftLength FFT Length
	 * @return Complex Hilbert Transform of the data. 
	 */
	public synchronized ComplexArray getHilbertC(ComplexArray fftData, int fftLength) {

//		makeStorageArrays(fftLength);
		double[] data121 = get1221Data(fftLength);
		ComplexArray fullArray = new ComplexArray(fftLength);
		double[] faData = fullArray.getData();
		double[] fftDoubles = fftData.getData();
		int i = 0;
		for (int j = 0; j < fftLength/2; j++) {
			faData[i] = fftDoubles[i]*data121[j];
			i++;
			faData[i] = fftDoubles[i]*data121[j];
			i++;
		}
//		for (; i < fftLength/2; i++) {
//			fullFFTArray[i] = fftData.times(i, data121[i]);
//			fullFFTArray[fftLength-i-1].assign(0,0);
//		}

		fastFFT.ifft(fullArray, fftLength);

		return fullArray;
	}
	
	/**
	 * Calculate the Hilbert Transform of a sample of data. 
	 * and return as a real array of the magnitude
	 * 
	 * @param signal signal waveform
	 * @return magnitude of analytic waveform. 
	 */
	public synchronized double[] getHilbert(double[] signal) {
		int dataLen = signal.length;
		return getHilbert(signal, dataLen);
	}
	
	/**
	 * Calculate the Hilbert Transform of a sample of data
	 * to length dataLen. If data are shorter than dataLen they
	 * will be zero padded. If longer, they will be truncated. 
	 * @param signal signal waveform
	 * @param dataLen maximum data length
	 * @return magnitude of analytic waveform. 
	 */
	public synchronized double[] getHilbert(double[] signal, int dataLen) {

		int fftLength = FastFFT.nextBinaryExp(dataLen);
		
		if (dataLen < fftLength) {
			signal = Arrays.copyOf(signal, fftLength);
		}
		
		ComplexArray hData = getHilbertC(signal, fftLength);

		double[] newData = new double[dataLen];

		for (int i = 0; i < dataLen; i++) {
			newData[i] = hData.mag(i) / fftLength;
		}

		return newData;
	}

//	/**
//	 * Compute the Hilbert transform from a Complex spectrum of real data. 
//	 * @param fftData Complex spectrum of real data. Only the first half of the 
//	 * spectrum is required. Length of data will either be fftLenght/2 or fftLength
//	 * but fftLength must match the original fftLength used with these data. 
//	 * @param fftLength FFT length used to calculate the spectrum. 
//	 * @param dataLen length of output data (can be < fftLength if data had to be padded)
//	 * @return magnitude of Hilbert Transform. 
//	 */
//	public synchronized double[] getHilbert(ComplexArray fftData, int fftLength, int dataLen) {
//	
//		ComplexArray hData = getHilbertC(fftData, fftLength);
//
//		double[] newData = new double[dataLen];
//
//		for (int i = 0; i < dataLen; i++) {
//			newData[i] = hData.mag(i) / fftLength;
//		}
//
//		return newData;
//	}
	/**
	 * Compute the Hilbert transform from a Complex spectrum of real data. 
	 * @param fftData Complex spectrum of real data. Only the first half of the 
	 * spectrum is required. Length of data will either be fftLenght/2 or fftLength
	 * but fftLength must match the original fftLength used with these data. 
	 * @param fftLength FFT length used to calculate the spectrum. 
	 * @param dataLen length of output data (can be < fftLength if data had to be padded)
	 * @return magnitude of Hilbert Transform. 
	 */
	public synchronized double[] getHilbert(ComplexArray fftData, int fftLength, int dataLen) {
	
		ComplexArray hData = getHilbertC(fftData, fftLength);

		double[] newData = new double[dataLen];

		for (int i = 0; i < dataLen; i++) {
			newData[i] = hData.mag(i) / fftLength;
		}

		return newData;
	}

	/**
	 * Get an array of 12222100000 to use as a multiplier 
	 * in the Hilbert transform
	 * @param length of array
	 * @return array of coefficients. 
	 */
	private double[] get1221Data(int length) {

		if (storedData1221 != null && storedData1221.length == length) {
			return storedData1221;
		}
		storedData1221 = new double[length];
		int halfLength = length / 2;
		for (int i = 1; i < halfLength; i++) {
			storedData1221[i] = 2;
		}
		storedData1221[0] = storedData1221[halfLength] = 1;

		return storedData1221;
	}

//	/**
//	 * Make sure storage arrays are correctly allocated.
//	 * @param fftLength
//	 */
//	private void makeStorageArrays(int fftLength) {
//		if (storedFFTArray == null || storedFFTArray.length != fftLength / 2) {
//			storedFFTArray = Complex.allocateComplexArray(fftLength / 2);
//		}
//		if (fullFFTArray == null || fullFFTArray.length != fftLength) {
//			fullFFTArray = Complex.allocateComplexArray(fftLength);
//		}
//	}
}
