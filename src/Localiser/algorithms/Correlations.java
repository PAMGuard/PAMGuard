/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package Localiser.algorithms;

import java.util.Arrays;

import pamMaths.PamVector;
import Array.ArrayManager;
import Array.PamArray;
import Localiser.DelayMeasurementParams;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import fftFilter.FFTFilterParams;
import fftManager.Complex;
import fftManager.FastFFT;

/**
 * @author Doug Gillespie
 *         <p>
 *         Various functions to do with cross correlating two or more signals
 *         <p>
 *         As with the FastFFT class, these functions are no longer static so 
 *         that Allocation of FFT internal storage is done separately for each detector. This 
 *         avoids it having to be continually reallocated if fft's of different lenghts are taken. 
 *         <p>
 *         Also calculates a delay correction factor based on a parabolic interpolation around the 
 *         maximum value. For compatibility with previous versions, the returned results is still 
 *         the integer solution. If you require greater accuracy, you should get the interpolated
 *         correction and add this to the main integer result for double precision accuracy. 
 *         
 */
public class Correlations {

	private FastFFT fastFFT = new FastFFT();
	private double correlationValue;
	private double[] lastCorrelationData;
	private double[] lastPeak;

	/**
	 * 
	 */
	public Correlations() {
		super();
		// TODO Auto-generated constructor stub
	}
	/**
	 * Measure the time delay between pulses on two channels. 
	 * @param f1 waveform on channel 1
	 * @param f2 waveform on channel 2
	 * @param delayMeasurementParams Measurement parameters. 
	 * @param fftLength FFT length to use (data will be packed or truncated as necessary)
	 * @return time delay in samples. 
	 */
	public TimeDelayData getDelay(double[] s1, double[] s2, DelayMeasurementParams delayMeasurementParams, double sampleRate, int fftLength) {
		return getDelay(s1, s2, delayMeasurementParams, sampleRate, fftLength, fftLength/2);
	}
	/**
	 * Measure the time delay between pulses on two channels. 
	 * @param f1 waveform on channel 1
	 * @param f2 waveform on channel 2
	 * @param delayMeasurementParams Measurement parameters. 
	 * @param fftLength FFT length to use (data will be packed or truncated as necessary)
	 * @param maxDelaySamples Maximum possible delay in samples
	 * @return time delay in samples. 
	 */
	public TimeDelayData getDelay(double[] s1, double[] s2, DelayMeasurementParams delayMeasurementParams, double sampleRate, int fftLength, double maxDelaySamples) {
		double[][] fftdata = null;
		ComplexArray[] fftOutData = null;

		double[][] inputSound = new double[2][];
		inputSound[0] = s1;
		inputSound[1] = s2;

		int soundLen = Math.max(s1.length, s2.length);
		if (fftLength == 0) {
			fftLength = FastFFT.nextBinaryExp(soundLen);
		}

//		int log2FFTLen = 1;
//		int dum = 2;
//		while (dum < fftLength) {
//			dum *= 2;
//			log2FFTLen++;
//		}
		if (fftOutData == null)
			fftOutData = new ComplexArray[2];
		if (fftdata == null)
			fftdata = new double[2][];
		int nS;
		for (int i = 0; i < 2; i++) {
			if (fftdata[i] == null || (fftdata[i].length != fftLength)) {
				fftdata[i] = new double[fftLength];
			}
			nS = Math.min(fftLength, inputSound[i].length);
			for (int iS = 0; iS < nS; iS++) {
				fftdata[i][iS] = inputSound[i][iS];
			}
			for (int iS = nS; iS < fftLength; iS++) {
				fftdata[i][iS] = 0.;
			}
			fftOutData[i] = fastFFT.rfft(fftdata[i], fftLength);
		}
		return getDelay(fftOutData[0], fftOutData[1], delayMeasurementParams, sampleRate, fftLength, maxDelaySamples);
	}
	
//	/**
//	 * Measure the time delay between pulses on two channels. Inputs in this case are the 
//	 * spectrum data (most of the cross correlation is done in the frequency domain)
//	 * @param f1 complex spectrum on channel 1
//	 * @param f2 complex spectrum on channel 2
//	 * @param delayMeasurementParams Measurement parameters. 
//	 * @param fftLength FFT length to use (data will be packed or truncated as necessary)
//	 * @return time delay in samples. 
//	 */
//	public double getDelay(Complex[] f1, Complex[] f2, DelayMeasurementParams delayMeasurementParams, int fftLength) {
//		return getDelay(f1, f2, delayMeasurementParams, fftLength, fftLength/2);
//	}
	
	/**
	 * Measure the time delay between pulses on two channels. Inputs in this case are the 
	 * spectrum data (most of the cross correlation is done in the frequency domain)
	 * @param f1 complex spectrum on channel 1
	 * @param f2 complex spectrum on channel 2
	 * @param delayMeasurementParams Measurement parameters. 
	 * @param fftLength FFT length to use (data will be packed or truncated as necessary)
	 * @return time delay in samples. 
	 */
	public TimeDelayData getDelay(ComplexArray f1, ComplexArray f2, DelayMeasurementParams delayMeasurementParams, double sampleRate, int fftLength) {
		return getDelay(f1, f2, delayMeasurementParams, sampleRate, fftLength, fftLength/2);
	}
	

	/**
	 * Measure the time delay between pulses on two channels. Inputs in this case are the 
	 * spectrum data (most of the cross correlation is done in the frequency domain)
	 * @param complexArray complex spectrum on channel 1
	 * @param complexArray2 complex spectrum on channel 2
	 * @param delayMeasurementParams Measurement parameters. 
	 * @param fftLength FFT length to use (data will be packed or truncated as necessary)
	 * @param maxDelaySamples Maximum feasible delay between channels. 
	 * @return time delay in samples. 
	 */
	public TimeDelayData getDelay(ComplexArray complexArray, ComplexArray complexArray2,
			DelayMeasurementParams delayMeasurementParams, double sampleRate, int fftLength, double maxDelaySamples) {
		/**
		 * See comment above - needs a lot of work to fully incorporate all delay options. 
		 */
		int[] binLims = {0, complexArray.length()};
		if (delayMeasurementParams != null) {
			FFTFilterParams filterParams = delayMeasurementParams.delayFilterParams;
			if (filterParams != null) {
				fftLength = complexArray.length()*2;
				int b1 = (int) Math.round(filterParams.lowPassFreq*fftLength/sampleRate);
				int b2 = (int) Math.round(filterParams.highPassFreq*fftLength/sampleRate);
				switch(filterParams.filterBand) {
				case BANDPASS:
					binLims[0] = Math.max(0, (int)Math.floor(Math.min(b1, b2))); 
					binLims[1] = Math.min(fftLength/2, (int)Math.ceil(Math.max(b1, b2))); 
					break;
				case BANDSTOP:
					binLims[0] = Math.max(0, (int)Math.floor(Math.max(b1, b2))); 
					binLims[1] = Math.min(fftLength/2, (int)Math.ceil(Math.min(b1, b2))); 
					break;
				case HIGHPASS:
					binLims[0] = Math.max(0, b2);
					break;
				case LOWPASS:
					binLims[1] = Math.min(fftLength/2, b1);
					break;
				default:
					break;
				
				}
			}
		}
		return getDelay(complexArray, complexArray2, fftLength, maxDelaySamples, binLims);
	}

	/**
	 * Measure the time delay between pulses on two channels. Inputs in this case are the 
	 * spectrum data (most of the cross correlation is done in the frequency domain)
	 * @param f1 complex spectrum on channel 1
	 * @param f2 complex spectrum on channel 2
	 * @param delayMeasurementParams Measurement parameters. 
	 * @param fftLength FFT length to use (data will be packed or truncated as necessary)
	 * @param maxDelay Maximum feasible delay between channels. 
	 * @return time delay in samples. 
	 * @deprecated use ComplexArray for everything. This is now never called. 
	 */
	public double getDelay(Complex[] f1, Complex[] f2, DelayMeasurementParams delayMeasurementParams, int fftLength, double maxDelay) {
		/*
		 * Everything ultimately ends up here and we can assume that the complex data are from a waveform and not 
		 * an envelope at this point. We may therefore have to both filter the data and take a hilbert transform 
		 * in order to satisfy new options in delayMeasurementParams. It's possible that f1 and f2 are references back
		 * into the original spectra which we don't want to mess with. 
		 * If it's complex wave data and we want to do the envelope, then we'll also need to Hilbert transform and 
		 * then call right back into the correlations call which uses the waveform data, which will end up in a second call back into here
		 * once it's done yet another FFT of the analytic waveform (or it's derivative). What a lot of options !  
		 */
		
		
		return getDelay(f1, f2, fftLength, maxDelay);
	}
	/**
	 * Measure the time delay between pulses on two channels. Inputs in this case are the 
	 * spectrum data (most of the cross correlation is done in the frequency domain)<p>
	 * This was the public function called from the click detector, but now that we've added
	 * DelayMEasurementParams to the main call, this is to be called privately only once the 
	 * filtering and envelope manipulations specified in the params have all been dealt with. 
	 * @param f1 complex spectrum on channel 1
	 * @param f2 complex spectrum on channel 2
	 * @param fftLength FFT length to use (data will be packed or truncated as necessary)
	 * @param maxDelay Maximum feasible delay between channels. 
	 * @return time delay in samples. 
	 * @deprecated use ComplexArray for everything. This is now never called. 
	 */
	private double getDelay(Complex[] f1, Complex[] f2, int fftLength, double maxDelay) {
		int delay = 0;
		Complex[] corrData = null;

		// now make up a new complex array which consists of one chan * the
		// complex conjugate of the other
		// and at the same time, fill in the other half of it.
		// also normalise it. 
		double scale1=0, scale2=0;
		if (corrData == null || corrData.length != fftLength) {
			corrData = Complex.allocateComplexArray(fftLength);
		}
		for (int i = 0; i < fftLength / 2; i++) {
			corrData[i].assign(f1[i].times(f2[i].conj()));
			corrData[fftLength - i - 1] = f1[i].conj().times(f2[i]);
			scale1 += f1[i].magsq();
			scale2 += f2[i].magsq();
		}
		// now take the inverse FFT ...
		fastFFT.ifft(corrData, FastFFT.log2(fftLength));
		double scale = Math.sqrt(scale1*scale2)*2; // double it for negative freq energy not incl in original sums. 
		
		double[] newPeak = getInterpolatedPeak(corrData, scale, maxDelay);
		correlationValue = newPeak[1];
		return newPeak[0];
	}
	
	
	/**
	 * Measure the time delay between pulses on two channels. Inputs in this case are the 
	 * spectrum data (most of the cross correlation is done in the frequency domain)<p>
	 * This was the public function called from the click detector, but now that we've added
	 * DelayMEasurementParams to the main call, this is to be called privately only once the 
	 * filtering and envelope manipulations specified in the params have all been dealt with. 
	 * @param f1 complex spectrum on channel 1
	 * @param f2 complex spectrum on channel 2
	 * @param fftLength FFT length to use (data will be packed or truncated as necessary)
	 * @param maxDelay Maximum feasible delay between channels. 
	 * @param binRnage - the bin range to use. 
	 * @return time delay in samples. 
	 */
	public TimeDelayData getDelay(ComplexArray f1, ComplexArray f2, int fftLength, double maxDelay, int[] binRange) {
		int delay = 0;
		if (binRange == null) {
			binRange = new int[2];
			binRange[1] = fftLength/2;
		}
		// now make up a new complex array which consists of one chan * the
		// complex conjugate of the other
		// and at the same time, fill in the other half of it.
		// also normalise it.
		ComplexArray corrData = f1.conjTimes(f2, binRange).fillConjugateHalf();
		double scale1=0, scale2=0;
		for (int i = binRange[0]; i < binRange[1]; i++) {
			scale1 += f1.magsq(i);
			scale2 += f2.magsq(i);
		}
		// now take the inverse FFT ...
		fastFFT.ifft(corrData, fftLength);
		double scale = Math.sqrt(scale1*scale2)*2; // double it for negative freq energy not incl in original sums. 
		
		double[] newPeak = getInterpolatedPeak(corrData, scale, maxDelay);
		correlationValue = newPeak[1];
		return new TimeDelayData(newPeak[0], newPeak[1]);
	}
	/**
	 * Get the value of the last correlation calculated by 
	 * any of the functions in the class.  
	 * @return the correlationValue
	 */
	public double getCorrelationValue() {
		return correlationValue;
	}
	/**
	 * Generate a default timing error which is 1/sqrt(12) times
	 * the sample interval
	 * @param sampleRate sample rate
	 * @return typical timing error
	 */
	public static synchronized double defaultTimingError(float sampleRate) {
		return 1./sampleRate/Math.sqrt(12.);
	}

	/**
	 * Calculate a parabolic fit correction based on three bin heights
	 * <br>The answer should always be between -0.5 and +0.5
	 * @param y1 first bin
	 * @param y2 second bin
	 * @param y3 third bin
	 * @return correction = 0.5*(y3-y1)/(2*y2-y1-y3);
	 */
	public double parabolicCorrection(double y1, double y2, double y3) {
		// need to be wary in case all three values are identical ...
		double bottom = 2*y2-y1-y3;
		if (bottom == 0.) {
			return 0.;
		}
		return 0.5*(y3-y1)/bottom;
	}
	/**
	 * Calculate a parabolic maximum height based on three bin heights
	 * @param y1 first bin
	 * @param y2 second bin
	 * @param y3 third bin
	 * @return height of parabola through those points. 
	 */
	public double parabolicHeight(double y1, double y2, double y3) {
		double t = parabolicCorrection(y1, y2, y3);
		double a = (y1+y3-2.*y2)/2.; 
		double b = (y3-y1) / 2.;
		double c = y2;
		return a*t*t + b*t + c;
	}
	
	/**
	 * 
	 * @param t time to measure height at. 
	 * @param correlationFunction correlation function. 
	 * @return height of parabola through those points. 
	 */
	public double parabolicHeight(double time,  double[] correlationFunction) {
		int ncp = correlationFunction.length;
		if (time <= 1) {
			return linearInterp(time, correlationFunction[0], correlationFunction[1]);
		}
		else if (time >= ncp-2) {
			return linearInterp(time-(ncp-1), correlationFunction[ncp-2], correlationFunction[ncp-1]);
		}
		int midBin = (int) Math.round(time);
		double y2 = correlationFunction[midBin];
		double y1 = correlationFunction[midBin-1];
		double y3 = correlationFunction[midBin+1];
		
		double a = (y1+y3-2.*y2)/2.; 
		double b = (y3-y1) / 2.;
		double c = y2;
		double t = time-midBin;
		return a*t*t + b*t + c;
	}

	/**
	 * Linear interpolation between two points 
	 * @param t distance from y1 to y2 on scale 0 - 1
	 * @param y1 first y value
	 * @param y2 second y value
	 * @return y2*t + y1*(1-t)
	 */
	private double linearInterp(double t, double y1, double y2) {
		return y2*t + y1*(1-t);
	}

	/**
	 * Get the correlation function of waves 1 and 2 and normalise if requested. 
	 * @param wave1 - the first wave.
	 * @param wave2 - the second wave.
	 * @param normalise - true to normalise.
	 * @return the correlation function. 
	 */
	public double[] getCorrelation(double[] wave1, double[] wave2, boolean normalise) {
		int maxLength = Math.max(wave1.length,  wave2.length);
		int fftLength = FastFFT.nextBinaryExp(maxLength);

		wave1 = Arrays.copyOf(wave1, fftLength);
		wave2 = Arrays.copyOf(wave2, fftLength);

		Complex[] fftData1 = Complex.allocateComplexArray(fftLength);
		Complex[] fftData2 = Complex.allocateComplexArray(fftLength);
		Complex[] corrData = Complex.allocateComplexArray(fftLength);
		FastFFT fft = new FastFFT();
		int logFFTLen = FastFFT.log2(fftLength);
		fftData1 = fft.rfft(wave1, fftData1, logFFTLen);
		fftData2 = fft.rfft(wave2, fftData2, logFFTLen);
		for (int i = 0; i < fftLength/2; i++) {
			corrData[i].assign(fftData1[i].times(fftData2[i].conj()));
			corrData[fftLength-i-1].assign(fftData1[i].conj().times(fftData2[i]));
		}
		fft.ifft(corrData, logFFTLen);
		int corrLength = maxLength;
		if (corrLength%2 > 0) {
			corrLength++;
		}
		double[] corrFunction = new double[corrLength];
		for (int i = 0; i < corrLength/2; i++) {
			corrFunction[i+corrLength/2] = corrData[i].real;
			corrFunction[corrLength/2-i-1] = corrData[fftLength-1-i].real;
		}
		if (normalise) {
			double corrScale0 = 0;
			double corrScale1 = 0;
			for (int i = 0; i < maxLength; i++) {
				corrScale0 += wave1[i]*wave1[i];
				corrScale1 += wave2[i]*wave2[i];
			}
			double corrScale = Math.sqrt(corrScale0*corrScale1)*fftLength;
			for (int i =0; i < corrLength; i++) {
				corrFunction[i] /= corrScale;
			}
		}
		return corrFunction;
	}

	
	/**
	 * Get the peak position and the peak height from a complex array
	 * returned by the ifft function. 
	 * @param complexData Complex data from the ifft during cross correlation calculation
	 * @param scale Scaling factor (sqrt(sum^2 of each channel))
	 * @param maxDelay max searchable delay
	 * @return two element array giving the peak pos and it's height. 
	 */
	public double[] getInterpolatedPeak(ComplexArray complexData, double scale, double maxDelay) {
		int fftLength = complexData.length();
		int maxCorrLen = (int) (Math.ceil(maxDelay) + 2);
		maxCorrLen = Math.min(fftLength/2, maxCorrLen);
		double[] linCorr = new double[maxCorrLen*2]; 
		for (int i = 0; i < maxCorrLen; i++) {
			linCorr[i+maxCorrLen] = complexData.getReal(i);
			linCorr[maxCorrLen-1-i] = complexData.getReal(fftLength-1-i);
		}
		double[] parabolicPeak = getInterpolatedPeak(linCorr);
		parabolicPeak[1] /= scale;
		parabolicPeak[0] -= maxCorrLen;
		parabolicPeak[0] = -parabolicPeak[0];
		parabolicPeak[0] = Math.max(-maxDelay, Math.min(maxDelay, parabolicPeak[0]));
		lastPeak = parabolicPeak;
		return parabolicPeak;
	}
	
	//	public double[] getInterpolatedPeak(ComplexArray complexData, double scale, double minDelay, double maxDelay) {
//		/**
//		 * Normally, minDelay will be -ve and equal to maxDelay. However, in some special 
//		 * circumstances connected with matching / timing things that are far apart then
//		 * minDelay and maxDelay will no longer have that symetry - mainly when we're dealing
//		 * with timing measurements between signals from different data units on widely spaced
//		 * hydrophones. - This is utter rubish ! When teh hydrophones are widely spaced the cahnces 
//		 * of being that close to the 'edge' of allowable values is small anyway, so can just
//		 * do the full range - am commenting out this entire function !
//		 */
//		int fftLength = complexData.length();
//		int minCorrLen = (int) (Math.floor(minDelay) - 2);
//		int maxCorrLen = (int) (Math.ceil(maxDelay) + 2);
//		maxCorrLen = Math.min(fftLength/2, maxCorrLen);
//		double[] linCorr = new double[maxCorrLen-minCorrLen]; 
//		for (int i = minCorrLen, j = 0; i < 0; i++,j++) {
//			linCorr[j] = complexData.getReal(i)
//		}
//		for (int i = 0; i < maxCorrLen; i++) {
//			linCorr[i+maxCorrLen] = complexData.getReal(i);
//			linCorr[maxCorrLen-1-i] = complexData.getReal(fftLength-1-i);
//		}
//		double[] parabolicPeak = getInterpolatedPeak(linCorr);
//		parabolicPeak[1] /= scale;
//		parabolicPeak[0] += minCorrLen;
//		parabolicPeak[0] = -parabolicPeak[0];
//		parabolicPeak[0] = Math.max(-maxDelay, Math.min(maxDelay, parabolicPeak[0]));
//		lastPeak = parabolicPeak;
//		return parabolicPeak;
//	}
	/**
	 * Get the peak position and the peak height from a complex array
	 * returned by the ifft function. 
	 * @param complexData Complex data from the ifft during cross correlation calculation
	 * @param scale Scaling factor (sqrt(sum^2 of each channel))
	 * @param maxDelay max searchable delay
	 * @return two element array giving the peak pos and it's height. 
	 */
	public double[] getInterpolatedPeak(Complex[] complexData, double scale, double maxDelay) {
		int fftLength = complexData.length;
		int maxCorrLen = (int) (Math.ceil(maxDelay) + 2);
		maxCorrLen = Math.min(fftLength/2, maxCorrLen);
		double[] linCorr = new double[maxCorrLen*2]; 
		for (int i = 0; i < maxCorrLen; i++) {
			linCorr[i+maxCorrLen] = complexData[i].real;
			linCorr[maxCorrLen-1-i] = complexData[fftLength-1-i].real;
		}
		double[] parabolicPeak = getInterpolatedPeak(linCorr);
		parabolicPeak[1] /= scale;
		parabolicPeak[0] -= maxCorrLen;
		parabolicPeak[0] = -parabolicPeak[0];
		parabolicPeak[0] = Math.max(-maxDelay, Math.min(maxDelay, parabolicPeak[0]));
		return parabolicPeak;
	}
	
	/**
	 * Find the peak of a correlation function, interpolating about each peak
	 * using a wee parabola. 
	 * <p>Note that first and last bins are not used in the search. 
	 * @param corrFunction correlation function
	 * @return peak position and peak height. 
	 */
	public double[] getInterpolatedPeak(double[] corrFunction) {
		lastCorrelationData = corrFunction;
		int n = corrFunction.length;
		double currPeak;
		double[] retData = new double[2];
		for (int i = 1; i < n-1; i++) {
			if (corrFunction[i] > corrFunction[i-1] && corrFunction[i] > corrFunction[i+1]) {
				currPeak = parabolicHeight(corrFunction[i-1], corrFunction[i], corrFunction[i+1]);
				if (currPeak > retData[1]) {
					retData[0] = i + parabolicCorrection(corrFunction[i-1], corrFunction[i], corrFunction[i+1]);
					retData[1] = currPeak;
				}
			}
		}
		return retData;
	}
//	
//	public double[] getNonInterpolatedPeak(ComplexArray complexData, double scale, double maxDelay) {
//		int fftLength = complexData.length();
//		int maxCorrLen = (int) (Math.ceil(maxDelay) + 2);
//		maxCorrLen = Math.min(fftLength/2, maxCorrLen);
//		double[] linCorr = new double[maxCorrLen*2]; 
//		for (int i = 0; i < maxCorrLen; i++) {
//			linCorr[i+maxCorrLen] = complexData.getReal(i);
//			linCorr[maxCorrLen-1-i] = complexData.getReal(fftLength-1-i);
//		}
//		double[] parabolicPeak = getNonInterpolatedPeak(linCorr);
//		parabolicPeak[1] /= scale;
//		parabolicPeak[0] -= maxCorrLen;
//		parabolicPeak[0] = -parabolicPeak[0];
//		parabolicPeak[0] = Math.max(-maxDelay, Math.min(maxDelay, parabolicPeak[0]));
//	
//		return parabolicPeak;
//	}
//	public double[] getNonInterpolatedPeak(double[] corrFunction) {
//		lastCorrelationData = corrFunction;
//		int n = corrFunction.length;
//		double currPeak;
//		double[] retData = new double[2];
//		for (int i = 0; i < n; i++) {
//			if (corrFunction[i] > retData[1]) {
//				retData[0] = i;
//				retData[1] = corrFunction[i];
//			}
//		}
//		return retData;
//	}
//	
	/**
	 * Get interpolated maxima around every peak in the correlation function. 
	 * @param corrFunction correlation function. 
	 * @return 2d array of times and interpolated values. 
	 */
	public double[][] getInterpolatedMaxima(double[] corrFunction) {
		if (corrFunction == null || corrFunction.length < 3) {
			return null;
		}
		double[] times = new double[0];
		double[] amplitudes = new double[0];
		for (int i = 1; i < corrFunction.length - 1; i++) {
			if (corrFunction[i] > corrFunction[i-1] && corrFunction[i] > corrFunction[i+1]) {
				times = Arrays.copyOf(times, times.length+1);
				amplitudes = Arrays.copyOf(amplitudes, amplitudes.length+1);
				times[times.length-1] = i + parabolicCorrection(corrFunction[i-1], corrFunction[i], corrFunction[i+1]);
				amplitudes[amplitudes.length-1] = parabolicHeight(corrFunction[i-1], corrFunction[i], corrFunction[i+1]);
			}
		}
		double[][] ans = new double[2][];
		ans[0] = times;
		ans[1] = amplitudes;
		return ans;
	}

	/**
	 * Get the maximum permissible delays in samples
	 * for the set of hydrophones. These numbers can
	 * be used to limit the range of correlation searches
	 * to valid values. 
	 * @param sampleRate sample rate of the data
	 * @param groupHydrophones bitmap of used hydrophones. 
	 * @return array of maximum time delays in samples. 
	 */
	public double[] getMaxDelays(float sampleRate, int groupHydrophones, long currentMillis) {
		int nHyd = PamUtils.getNumChannels(groupHydrophones);
		int nDelay = nHyd * (nHyd-1) / 2;
		double[] maxDelays = new double[nDelay];
		PamArray currentArray = ArrayManager.getArrayManager().getCurrentArray();
		int iHyd, jHyd;
		PamVector iVec, jVec;
		double dist;
		int iDelay = 0;
		for (int i = 0; i < nHyd; i++) {
			/*
			 * Absolutely essential that this is done off hydrophone numbers not indexes
			 * within a group or it will get the wrong separations. 
			 */
			iHyd = PamUtils.getNthChannel(i, groupHydrophones);
			iVec = currentArray.getAbsHydrophoneVector(iHyd, currentMillis);
			for (int j = i+1; j < nHyd; j++) {
				jHyd = PamUtils.getNthChannel(j, groupHydrophones);
//				jVec = currentArray.getAbsHydrophoneVector(jHyd, currentMillis);
//				dist = iVec.dist(jVec);
//				maxDelays[iDelay++] = dist * sampleRate / currentArray.getSpeedOfSound();
			
				maxDelays[iDelay++] = currentArray.getSeparationInSeconds(iHyd, jHyd, currentMillis) * sampleRate;
			}
		}
		return maxDelays;
	}
	/**
	 * @return the fastFFT
	 */
	public FastFFT getFastFFT() {
		return fastFFT;
	}
	/**
	 * @return the lastCorrelationData
	 */
	public double[] getLastCorrelationData() {
		return lastCorrelationData;
	}
	/**
	 * @param lastCorrelationData the lastCorrelationData to set
	 */
	public void setLastCorrelationData(double[] lastCorrelationData) {
		this.lastCorrelationData = lastCorrelationData;
	}
	/**
	 * @return the lastPeak
	 */
	public double[] getLastPeak() {
		return lastPeak;
	}
	/**
	 * @param lastPeak the lastPeak to set
	 */
	public void setLastPeak(double[] lastPeak) {
		this.lastPeak = lastPeak;
	}
}
