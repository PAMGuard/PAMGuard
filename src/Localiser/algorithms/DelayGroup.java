package Localiser.algorithms;

import java.util.Arrays;

import Localiser.DelayMeasurementParams;
import PamUtils.complex.ComplexArray;
import Spectrogram.WindowFunction;
import fftFilter.FFTFilter;
import fftFilter.FFTFilterParams;
import fftManager.Complex;
import fftManager.FFTDataArray;
import fftManager.FastFFT;
import signal.Hilbert;

/**
 * Functions for attempting to efficiently prep data for correlation using a variety of
 * pre-processing methods. 
 * Input initially complex spectrum data from waveform. 
 * @author Doug Gillespie
 *
 */
public class DelayGroup {

	private FFTFilter fftFilter;
	private Hilbert hilbert = new Hilbert();
	private FastFFT fastFFT = new FastFFT();
	private Correlations correlations = new Correlations();
	private UpSampler upSampler;
	
	/**
	 * Calculate time delays for a set of raw waveforms
	 * @param waveformInput - the raw waveforms between -1 and 1
	 * @param sampleRate - the sample rate in samples per second
	 * @param delayParams - delay measurement parameters. 
	 * @return time delay class with time delays and errors in seconds. 
	 */
	public TimeDelayData[] getDelays(double[][] waveformInput, float sampleRate, DelayMeasurementParams delayParams) {
		return getDelays(waveformInput,  sampleRate,  delayParams, null); 
	}

	/**
	 * Calculate time delays for a set of raw waveforms
	 * @param waveformInput - the raw waveforms between -1 and 1
	 * @param sampleRate - the sample rate in samples per second
	 * @param delayParams - delay measurement parameters. 
	 * @param maxDelays m- the maximum delay search range in samples. Can be used to restrict searches. Can be null
	 * @return time delay class with time delays and errors in seconds. 
	 */
	public TimeDelayData[] getDelays(double[][] waveformInput, float sampleRate, DelayMeasurementParams delayParams, double[] maxDelays) {
		int nChan = waveformInput.length;
		if (nChan < 2) {
			return null;
		}
		
		//restrict the size of the waveform (usually to reduce echoes). 
		if (delayParams.useRestrictedBins) {
			for (int i=0; i<waveformInput.length; i++) {
				waveformInput[i]=Arrays.copyOf(waveformInput[i], delayParams.restrictedBins); 
			}
		}
		
		//up sample the waveform
		if (delayParams != null && delayParams.getUpSample() > 1) {
			if (upSampler == null) {
				upSampler = new UpSampler(delayParams.getUpSample());
			}
			waveformInput = upSampler.upSample(waveformInput, delayParams.getUpSample());
			if (maxDelays != null) {
				maxDelays = maxDelays.clone();
				for (int i = 0; i < maxDelays.length; i++) {
					maxDelays[i] *= delayParams.getUpSample();
				}
			}
			delayParams = delayParams.upSample(delayParams.getUpSample());
		}
		else {
			delayParams.setUpSample(1);
		}
		
		//filter the waveform.
		FFTDataArray[] specData = getComplexCorrelatorData(waveformInput, sampleRate*delayParams.getUpSample(), null, delayParams);
		int nOutputs = (nChan-1)*nChan/2;
		TimeDelayData[] delays = new TimeDelayData[nOutputs];
		int iOut = 0;
		
		//if max delays is null then it's just the spectrum length
		//added maxDelays.length!=nOutputs because imported clicks (e.g. from Rainbow clicks) can have 
		//a maxDelays length of 0. 
		if (maxDelays==null || maxDelays.length!=nOutputs) {
			maxDelays = new double[waveformInput.length];
			for (int i = 0; i < nOutputs; i++) {
				maxDelays[i]=specData[0].getFftLength(); 
			}
		}
		

		//perform the time delay calculations
		for (int i = 0; i < nChan; i++) {
			for (int j = i+1; j < nChan; j++, iOut++) {	
				TimeDelayData td = correlations.getDelay(specData[i].getFftData(), specData[j].getFftData(), 
						delayParams, sampleRate*delayParams.getUpSample(), specData[i].getFftLength(), maxDelays[iOut]);
				td.scaleDelay(1./ delayParams.getUpSample());
				delays[iOut] = td;
			}
		}
		
		return delays;
	}

	/**
	 * Get the complex data which will go into a cross correlation function, starting with a group of waveforms.  
	 * @param waveformInput Waveform input. 
	 * @param sampleRate audio sample rate (Hz)
	 * @param preallocData preallocated data - can be null and my not be used if the envelope method isn't used. 
	 * @param delayParams Parameters controlling filtering, envelope extraction, etc. 
	 * @return Array of Complex spectra to go into the second stage of a cross correlation function. Will have same FFT length as 
	 * input data, but will only be the first half - fftLength/2 long. 
	 */
	public FFTDataArray[] getComplexCorrelatorData(double[][] waveformInput, float sampleRate, Complex[][] preallocData, DelayMeasurementParams delayParams) {
		int nChan = waveformInput.length;
		// it's possible that different channels may have different length data, so we'll need to 
		// ensure they all have the same - take the maximum. 
		int wavLen = 0;
		for (int i = 0; i < nChan; i++) {
			wavLen = Math.max(wavLen, waveformInput[i].length);
		}
		int fftLength = FastFFT.nextBinaryExp(1, wavLen);
		FFTDataArray[] outData = new FFTDataArray[nChan];
		if (preallocData == null || preallocData.length != nChan) {
			preallocData = new Complex[nChan][]; // won't do much apart from stopping it crash since these will all be null
		}
		for (int iChan = 0; iChan < nChan; iChan++) {
			outData[iChan] = getComplexCorrelatorData(waveformInput[iChan], fftLength, sampleRate, preallocData[iChan], delayParams);
		}
		return outData;
	}

	/**
	 * 
	 * Get the complex data which will go into a cross correlation function, starting with a waveform. 
	 * @param waveformInput Waveform input. 
	 * @param sampleRate audio sample rate (Hz)
	 * @param preallocData preallocated data - can be null and my not be used if the envelope method isn't used. 
	 * @param delayParams Parameters controlling filtering, envelope extraction, etc. 
	 * @return Complex spectrum to go into the second stage of a cross correlation function. Will have same FFT length as 
	 * input data, but will only be the first half - fftLength/2 long. 
	 */
	public FFTDataArray getComplexCorrelatorData(double[] waveformInput, int fftLength, float sampleRate, Complex[] preallocData, DelayMeasurementParams delayParams) {
		ComplexArray fftData = fastFFT.rfft(waveformInput, fftLength);
		return getComplexCorrelatorData(fftData, sampleRate, waveformInput.length, fftLength, preallocData, delayParams);
	}

	/**
	 * Get the complex data which will go into a cross correlation function, starting with a complex spectrum. <p>
	 * This can be used to save a certain amount of processing time since it minimises the number of fft and ifft's 
	 * that need doing to a particular waveform. 
	 * @param spectrogramInput Complex spectrum data. Only need the first half, so it can be fftLength or fftLength/2 long. 
	 * @param sampleRate audio sample rate (Hz)
	 * @param signalLength signal length (can be < fftLength)
	 * @param fftLength FFT length used to calculate the spectrum. 
	 * @param preallocData preallocated data - can be null and my not be used if the envelope method isn't used. 
	 * @param delayParams Parameters controlling filtering, envelope extraction, etc. 
	 * @return Complex spectrum to go into the second stage of a cross correlation function. Will have same FFT length as 
	 * input data, but will only be the first half - fftLength/2 long. 
	 */
	public FFTDataArray getComplexCorrelatorData(ComplexArray spectrogramInput, float sampleRate, int signalLength, int fftLength, 
			Complex[] preallocData, DelayMeasurementParams delayParams) {
		if (delayParams.filterBearings) {
			FFTFilter fftFilter = getFFTFilter(delayParams.delayFilterParams, sampleRate);
			fftFilter.runFilter(spectrogramInput, fftLength);
		}
		if (delayParams.envelopeBearings) {
			// get the waveform envelope.
			double[] envelope = hilbert.getHilbert(spectrogramInput, fftLength, signalLength);
			if (delayParams.useLeadingEdge) {
				extractLeadingEdge(envelope, delayParams.leadingEdgeSearchRegion);
			}
			// now need to do another FFT on that in order to return a complex spectrum. 
//			int m = FastFFT.log2(fftLength);
			ComplexArray envelopeSpectrum = fastFFT.rfft(envelope, fftLength);
			return new FFTDataArray(envelopeSpectrum, fftLength, WindowFunction.RECTANGULAR);
		}
		else {
			return new FFTDataArray(spectrogramInput, fftLength, WindowFunction.RECTANGULAR);
		}
	}

	/**
	 * Differentiate the envelope and find a single peak close to where the leading edge of
	 * the envelope should be. 
	 * @param envelope envelope function (modified in place)
	 * @param sRegion search region for a single peak to extract. 
	 */
	private void extractLeadingEdge(double[] envelope, int[] sRegion) {
		// differentiate it.
		for (int i = 1, j = 0; i < envelope.length; i++, j++) {
			envelope[j] = envelope[i]-envelope[j];
		}
		
		if (sRegion == null || sRegion.length != 2) return;
		
		// find the peak pos between these points. 
		for (int i = 0; i < 2; i++) {
			// check length is within array bounds. 
			sRegion[i] = Math.max(0, Math.min(envelope.length-1, sRegion[i]));
		}
		
		double maxVal = envelope[sRegion[0]];
		int maxPos = sRegion[0];
		for (int i = sRegion[0]; i <= sRegion[1]; i++) {
			if (envelope[i] > maxVal) {
				maxVal = envelope[i];
				maxPos = i;
			}
		}
		// now find the regions either side of a single peak and zero them entirely. 
		int iPos = maxPos;
		while (envelope[iPos] > 0 && iPos > 0) {
			iPos--;
		}
		for (; iPos >= 0; iPos--) {
			envelope[iPos] = 0;
		}
		iPos = maxPos;
		while (iPos < envelope.length) {
			if (envelope[iPos] > 0 ) {
				iPos++;
			}
			else {
				break;
			}
		}
		for (; iPos < envelope.length; iPos++) {
			envelope[iPos] = 0;
		}
	}


	private FFTFilter getFFTFilter(FFTFilterParams fftFilterParams, float sampleRate) {
		if (fftFilter == null) {
			fftFilter = new FFTFilter(fftFilterParams, sampleRate);
		}
		else {
			fftFilter.setParams(fftFilterParams, sampleRate);
		}
		return fftFilter;
	}

	/**
	 * @return the correlations
	 */
	public Correlations getCorrelations() {
		return correlations;
	}

}
