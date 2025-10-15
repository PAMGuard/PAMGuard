package fftFilter;

import java.util.Arrays;

import Filters.Filter;
import PamUtils.PamArrayUtils;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;
import fftManager.Complex;

public class FFTFilter implements Filter {
	
	private DoubleFFT_1D doubleFFT_1D;
	
	private FFTFilterParams fftFilterParams;
	
	private float sampleRate;

	private int currentFFTLength;
	
	private boolean removeMean;
	
	public FFTFilter(FFTFilterParams fftFilterParams, float sampleRate) {
		setParams(fftFilterParams, sampleRate);
	}
	
	/**
	 * Set parameters for the filter.
	 * @param fftFilterParams the filter parameters. 
	 * @param sampleRate - the sample rate.
	 */
	public void setParams(FFTFilterParams fftFilterParams, float sampleRate) {
		this.fftFilterParams = fftFilterParams.clone();
		this.sampleRate = sampleRate;
	}

	@Override
	public int getFilterDelay() {
		return 0;
	}

	@Override
	public void prepareFilter() {
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public synchronized void runFilter(double[] inputData, double[] outputData) {

		int len = inputData.length;
		int fftLen = 1<<PamUtils.log2(len);
		if (doubleFFT_1D == null || currentFFTLength != fftLen) {
			currentFFTLength = fftLen;
			doubleFFT_1D = new DoubleFFT_1D(fftLen);
		}
		
		double mean = 0;
		if (removeMean) {
			mean = PamArrayUtils.mean(inputData);
			inputData = Arrays.copyOf(inputData, inputData.length);
			PamArrayUtils.add(inputData, -mean);
		}
		
		double[] complexData = Arrays.copyOf(inputData, fftLen*2);
//		doubleFFT_1D.realForward(fftData);
		doubleFFT_1D.realForwardFull(complexData);
		// now make a second array of twice that length so as to make the 
		// full complex conjugate data prior to filtering and the inverse transform...
		/*
		 * Do the filtering on the first half of the FFT only.
		 * N.B. the array is real and imaginary pairs, so will have to 
		 * operate on 2* the number of elements and double a lot of bin numbers. 
		 */
		int bin1, bin2, j;
		switch(fftFilterParams.filterBand) {
		case HIGHPASS:
			bin1 = getFFTBin(fftFilterParams.highPassFreq, fftLen, sampleRate);
			j = fftLen*2-1;
			for (int i = 0; i < bin1*2; i++, j--) {
				complexData[i] = 0;
				complexData[j] = 0;
			}
			break;
		case LOWPASS:
			bin1 = getFFTBin(fftFilterParams.lowPassFreq, fftLen, sampleRate);
			j = fftLen*2-1-bin1*2;
			for (int i = bin1*2; i < fftLen; i++, j--) {
				complexData[i] = 0;
				complexData[j] = 0;
			}
			break;
		case BANDPASS:
			bin1 = getFFTBin(Math.min(fftFilterParams.highPassFreq, fftFilterParams.lowPassFreq), fftLen, sampleRate);
			j = fftLen*2-1;
			for (int i = 0; i < bin1*2; i++,j--) {
				complexData[i] = 0;
				complexData[j] = 0;
			}
			bin1 = getFFTBin(Math.max(fftFilterParams.highPassFreq, fftFilterParams.lowPassFreq), fftLen, sampleRate);
			j = fftLen*2-1-bin1*2;
			for (int i = bin1*2; i < fftLen; i++,j--) {
				complexData[i] = 0;
				complexData[j] = 0;
			}
			break;
		case BANDSTOP:
			bin1 = getFFTBin(Math.min(fftFilterParams.highPassFreq, fftFilterParams.lowPassFreq), fftLen, sampleRate);
			bin2 = getFFTBin(Math.max(fftFilterParams.highPassFreq, fftFilterParams.lowPassFreq), fftLen, sampleRate);
			j = fftLen*2-1-bin1*2;
			for (int i = bin1*2; i < bin2*2; i++,j--) {
				complexData[i] = 0;
				complexData[j] = 0;
			}
		}
		/*
		 * Then do the inverse transform which will return another 
		 * complex result !
		 */
		doubleFFT_1D.complexInverse(complexData, true);
		
		/**
		 * And copy that into the output data. 
		 */
		j = 0;
		for (int i = 0; i < len; i++, j+=2) {
			outputData[i] = complexData[j];
		}
		if (mean != 0) {
			switch(fftFilterParams.filterBand) {
			case BANDSTOP:
			case LOWPASS:
				PamArrayUtils.add(outputData, mean);
			}
		}
	}
	
	/**
	 * Filter complex FFT data. Note that the data may be full or 
	 * just half of the FTF length. 
	 * @param complexData Complex Array will be FFTLength of FFTLength / 2 long. 
	 * @param fftLength FFT Length
	 */
	public synchronized void runFilter(Complex[] complexData, int fftLength) {
		if (complexData.length == fftLength) {
			runFullFilter(complexData, fftLength);
		}
		else {
			runHalfFilter(complexData, fftLength);
		}
	}
	
	public synchronized void runFilter(ComplexArray complexData, int fftLength) {
		if (complexData.length() == fftLength) {
			runFullFilter(complexData, fftLength);
		}
		else {
			runHalfFilter(complexData, fftLength);
		}
	}
	
	private void runHalfFilter(Complex[] complexData, int fftLen) {

		int bin1, bin2;
		switch(fftFilterParams.filterBand) {
		case HIGHPASS:
			bin1 = getFFTBin(fftFilterParams.highPassFreq, fftLen, sampleRate);
			for (int i = 0; i < bin1; i++) {
				complexData[i].assign(0, 0);
			}
			break;
		case LOWPASS:
			bin1 = getFFTBin(fftFilterParams.lowPassFreq, fftLen, sampleRate);
			for (int i = bin1; i < fftLen/2; i++) {
				complexData[i].assign(0, 0);
			}
			break;
		case BANDPASS:
			bin1 = getFFTBin(Math.min(fftFilterParams.highPassFreq, fftFilterParams.lowPassFreq), fftLen, sampleRate);
			for (int i = 0; i < bin1; i++) {
				complexData[i].assign(0, 0);
			}
			bin1 = getFFTBin(Math.max(fftFilterParams.highPassFreq, fftFilterParams.lowPassFreq), fftLen, sampleRate);
			for (int i = bin1; i < fftLen/2; i++) {
				complexData[i].assign(0, 0);
			}
			break;
		case BANDSTOP:
			bin1 = getFFTBin(Math.min(fftFilterParams.highPassFreq, fftFilterParams.lowPassFreq), fftLen, sampleRate);
			bin2 = getFFTBin(Math.max(fftFilterParams.highPassFreq, fftFilterParams.lowPassFreq), fftLen, sampleRate);
			for (int i = bin1; i < bin2; i++) {
				complexData[i].assign(0, 0);
			}
		}
	}

	private void runHalfFilter(ComplexArray complexData, int fftLen) {

		int bin1, bin2;
		switch(fftFilterParams.filterBand) {
		case HIGHPASS:
			bin1 = getFFTBin(fftFilterParams.highPassFreq, fftLen, sampleRate);
			for (int i = 0; i < bin1; i++) {
				complexData.set(i, 0, 0);
			}
			break;
		case LOWPASS:
			bin1 = getFFTBin(fftFilterParams.lowPassFreq, fftLen, sampleRate);
			for (int i = bin1; i < fftLen/2; i++) {
				complexData.set(i, 0, 0);
			}
			break;
		case BANDPASS:
			bin1 = getFFTBin(Math.min(fftFilterParams.highPassFreq, fftFilterParams.lowPassFreq), fftLen, sampleRate);
			for (int i = 0; i < bin1; i++) {
				complexData.set(i, 0, 0);
			}
			bin1 = getFFTBin(Math.max(fftFilterParams.highPassFreq, fftFilterParams.lowPassFreq), fftLen, sampleRate);
			for (int i = bin1; i < fftLen/2; i++) {
				complexData.set(i, 0, 0);
			}
			break;
		case BANDSTOP:
			bin1 = getFFTBin(Math.min(fftFilterParams.highPassFreq, fftFilterParams.lowPassFreq), fftLen, sampleRate);
			bin2 = getFFTBin(Math.max(fftFilterParams.highPassFreq, fftFilterParams.lowPassFreq), fftLen, sampleRate);
			for (int i = bin1; i < bin2; i++) {
				complexData.set(i, 0, 0);
			}
		}
	}
	
	private void runFullFilter(Complex[] complexData, int fftLen) {

		int bin1, bin2, j;
		switch(fftFilterParams.filterBand) {
		case HIGHPASS:
			bin1 = getFFTBin(fftFilterParams.highPassFreq, fftLen, sampleRate);
			j = fftLen-1;
			for (int i = 0; i < bin1; i++, j--) {
				complexData[i].assign(0, 0);
				complexData[j].assign(0, 0);
			}
			break;
		case LOWPASS:
			bin1 = getFFTBin(fftFilterParams.lowPassFreq, fftLen, sampleRate);
			j = fftLen-1-bin1;
			for (int i = bin1; i < fftLen/2; i++, j--) {
				complexData[i].assign(0, 0);
				complexData[j].assign(0, 0);
			}
			break;
		case BANDPASS:
			bin1 = getFFTBin(Math.min(fftFilterParams.highPassFreq, fftFilterParams.lowPassFreq), fftLen, sampleRate);
			j = fftLen-1;
			for (int i = 0; i < bin1; i++,j--) {
				complexData[i].assign(0, 0);
				complexData[j].assign(0, 0);
			}
			bin1 = getFFTBin(Math.max(fftFilterParams.highPassFreq, fftFilterParams.lowPassFreq), fftLen, sampleRate);
			j = fftLen-1-bin1;
			for (int i = bin1; i < fftLen; i++,j--) {
				complexData[i].assign(0, 0);
				complexData[j].assign(0, 0);
			}
			break;
		case BANDSTOP:
			bin1 = getFFTBin(Math.min(fftFilterParams.highPassFreq, fftFilterParams.lowPassFreq), fftLen, sampleRate);
			bin2 = getFFTBin(Math.max(fftFilterParams.highPassFreq, fftFilterParams.lowPassFreq), fftLen, sampleRate);
			j = fftLen-1-bin1;
			for (int i = bin1; i < bin2; i++,j--) {
				complexData[i].assign(0, 0);
				complexData[j].assign(0, 0);
			}
		}
	}

	private void runFullFilter(ComplexArray complexData, int fftLen) {

		int bin1, bin2, j;
		switch(fftFilterParams.filterBand) {
		case HIGHPASS:
			bin1 = getFFTBin(fftFilterParams.highPassFreq, fftLen, sampleRate);
			j = fftLen-1;
			for (int i = 0; i < bin1; i++, j--) {
				complexData.set(i, 0, 0);
				complexData.set(j, 0, 0);
			}
			break;
		case LOWPASS:
			bin1 = getFFTBin(fftFilterParams.lowPassFreq, fftLen, sampleRate);
			j = fftLen-1-bin1;
			for (int i = bin1; i < fftLen/2; i++, j--) {
				complexData.set(i, 0, 0);
				complexData.set(j, 0, 0);
			}
			break;
		case BANDPASS:
			bin1 = getFFTBin(Math.min(fftFilterParams.highPassFreq, fftFilterParams.lowPassFreq), fftLen, sampleRate);
			j = fftLen-1;
			for (int i = 0; i < bin1; i++,j--) {
				complexData.set(i, 0, 0);
				complexData.set(j, 0, 0);
			}
			bin1 = getFFTBin(Math.max(fftFilterParams.highPassFreq, fftFilterParams.lowPassFreq), fftLen, sampleRate);
			j = fftLen-1-bin1;
			for (int i = bin1; i < fftLen; i++,j--) {
				complexData.set(i, 0, 0);
				complexData.set(j, 0, 0);
			}
			break;
		case BANDSTOP:
			bin1 = getFFTBin(Math.min(fftFilterParams.highPassFreq, fftFilterParams.lowPassFreq), fftLen, sampleRate);
			bin2 = getFFTBin(Math.max(fftFilterParams.highPassFreq, fftFilterParams.lowPassFreq), fftLen, sampleRate);
			j = fftLen-1-bin1;
			for (int i = bin1; i < bin2; i++,j--) {
				complexData.set(i, 0, 0);
				complexData.set(j, 0, 0);
			}
		}
	}

	@Override
	public void runFilter(double[] inputData) {
		runFilter(inputData, inputData);
	}
	
	private int getFFTBin(double freq, int fftLen, float sampleRate) {
		int bin = (int) Math.round(freq * fftLen / sampleRate);
		return Math.min(Math.max(0, bin), fftLen/2-1);
	}

	@Override
	public double runFilter(double aData) {
		// can't do this with fft filtering, since data need to be in blocks. 
		return Double.NaN;
	}

	/**
	 * @return the removeMean
	 */
	public boolean isRemoveMean() {
		return removeMean;
	}

	/**
	 * @param removeMean the removeMean to set
	 */
	public void setRemoveMean(boolean removeMean) {
		this.removeMean = removeMean;
	}

}
