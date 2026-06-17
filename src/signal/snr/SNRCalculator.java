package signal.snr;

import java.util.Arrays;

import PamUtils.complex.ComplexArray;
import fftManager.FastFFT;
import pamMaths.STD;
import signal.Hilbert;

/**
 * General functions for calculating SNR from as wide a variety of signals as possible, 
 * with no real a-priori knowledge as to where the signal is, though we assume that there
 * is a signal present and that there is enough data in the passed waveform snippet that
 * a reasonable measure of noise is also obtained. <br>
 * For short snips, signal is searched for as the peak in the waveform envelope, for longer
 * snips, it's searched for in a spectrogram of the data, summed over the frequency 
 * range of interest (generally what's relevant to a bearing calculator).  
 */
public class SNRCalculator {

	private int fftLength = 512;
	
	private double threshold = 2; // 3dB threshold as default

	private FastFFT fastFFT = new FastFFT();
	
	private double sampleRate = 1;
	
	private double[] frequencyRange = null;
	
	private STD std = new STD();
	
	private int smooth = 1;
	
	private Hilbert hilbert = new Hilbert();
	
	public SNRCalculator() {
	}

	/**
	 * @param fftLength
	 * @param threshold
	 */
	public SNRCalculator(int fftLength, double threshold) {
		super();
		this.fftLength = fftLength;
		this.threshold = threshold;
	}
	
	/**
	 * @return the fftLength
	 */
	public int getFftLength() {
		return fftLength;
	}

	/**
	 * @param fftLength the fftLength to set
	 */
	public void setFftLength(int fftLength) {
		this.fftLength = fftLength;
	}

	/**
	 * @return the threshold
	 */
	public double getThreshold() {
		return threshold;
	}

	/**
	 * @param threshold the threshold to set
	 */
	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	/**
	 * @return the sampleRate
	 */
	public double getSampleRate() {
		return sampleRate;
	}

	/**
	 * @param sampleRate the sampleRate to set
	 */
	public void setSampleRate(double sampleRate) {
		this.sampleRate = sampleRate;
	}

	/**
	 * @return the frequencyRange
	 */
	public double[] getFrequencyRange() {
		return frequencyRange;
	}

	/**
	 * @param frequencyRange the frequencyRange to set
	 */
	public void setFrequencyRange(double[] frequencyRange) {
		this.frequencyRange = frequencyRange;
	}

	/**
	 * @return the smooth
	 */
	public int getSmooth() {
		return smooth;
	}

	/**
	 * @param smooth the smooth to set
	 */
	public void setSmooth(int smooth) {
		this.smooth = smooth;
	}

	/**
	 * Multi-channel version.<br>
	 * Calculate the SNR and other parameters required for bearing 
	 * error (TDOA error) estimation, including the fundamental frequency
	 * and the time-bandwidth product. 
	 * @param wave
	 * @return array of SNR measurements, one for each channel
	 */
	public SNRData[] calculateSNR(double[][] waves) {
		if (waves == null) {
			return null;
		}
		SNRData[] snr = new SNRData[waves.length];
		for (int i = 0; i < waves.length; i++) {
			snr[i] = calculateSNR(waves[i]);
		}
		return snr;
	}
	
	/**
	 * Calculate the SNR and other parameters required for bearing 
	 * error (TDOA error) estimation, including the fundamental frequency
	 * and the time-bandwidth product. 
	 * @param wave
	 * @return SNR measurement. 
	 */
	public SNRData calculateSNR(double[] wave) {
		if (wave == null) {
			return null;
		}

		double[][] spectrogram = getSpectrogram(wave);
		double[] powerSpec = getPowerSpectrum(spectrogram);
		double[] tSeries;
		double tScale;
		/**
		 * Get the call duration from the hilbert transform of the wave
		 */
		if (spectrogram.length < 5) {
			tSeries = getEnvelope(wave);
			tScale = 1./sampleRate;
		}
		else {
			/**
			 * Get the call duration from spectrogram
			 */
			tSeries = getSpectralPower(spectrogram);
			tScale = fftLength/2/sampleRate;
		}
		double fScale = sampleRate/fftLength;
		
		powerSpec = smoothData(powerSpec);
		tSeries = smoothData(tSeries);
		
		PeakData fPeak = findPeak(powerSpec);
		PeakData tPeak = findPeak(tSeries);
		
		double fPeakWidth = (fPeak.binEnd-fPeak.binStart+ 1) * fScale;
		double tPeakWidth = (tPeak.binEnd-tPeak.binStart + 1) * tScale;
		
//		double snr = tPeak.peakEnergy / (tPeak.median*tSeries.length);
		double snr = tPeak.peakEnergy / (tPeak.binEnd-tPeak.binStart + 1) / tPeak.median;
		double f0 = fPeak.peakBin * fScale;		
		
		return new SNRData(snr, f0, tPeakWidth, fPeakWidth, frequencyRange);
	}
	
	/**
	 * Get the spectrogram of the data. This will 
	 * probably be 2d, except for very short wave.
	 * @param wave
	 * @return
	 */
	double[][] getSpectrogram(double[] wave) {
		int fftHop = fftLength/2;
		int n = wave.length / fftHop - 1;
		n = Math.max(n, 1);
		double[][] specGram = new double[n][];
		int s1 = 0;
		int s2 = fftLength;
		for (int i = 0; i < n; i++) {
			s2 = Math.min(wave.length, s2);
			double[] fDat = Arrays.copyOfRange(wave, s1, s2);
			// should probably window this too ?
			ComplexArray compData = fastFFT.rfft(fDat, fftLength);
			specGram[i] = compData.magsq();
			s1 += fftHop;
			s2 += fftHop;
		}
		return specGram;
	}
	
	/**
	 * Get the power spectrum. 
	 * @param spectrogram
	 * @return
	 */
	double[] getPowerSpectrum(double[][] spectrogram) {
		int nT = spectrogram.length;
		int nF = spectrogram[0].length;
		double[] ps = Arrays.copyOf(spectrogram[0], nF);

		for (int i = 1; i < nT; i++) {
			double[] s = spectrogram[i];
			for (int j = 0; j < nF; j++) {
				ps[j] += s[j];
			}
		}

		return ps;
	}
	
	/**
	 * Get the frequency bins for any summing or searching. 
	 * @return
	 */
	private int[] getFreqBins() {
		int nF = fftLength/2;
		int[] fB = {0, nF};
		if (frequencyRange != null) {
			for (int i = 0; i < frequencyRange.length; i++) {
				fB[i] = Math.max(0,  Math.min(nF, (int) Math.round(frequencyRange[0]*fftLength/sampleRate)));
			}
		}
		return fB;
	}
	
	/**
	 * Get the power as a function of time from a spectrogram. 
	 * Add the power spectrum in time within frequency limits if given 
	 * @param spectrogram
	 * @return
	 */
	double[] getSpectralPower(double[][] spectrogram) {
		int nT = spectrogram.length;
		int[] fB = getFreqBins();
		
		double[] pow = new double[nT];
		for (int i = 0; i < nT; i++) {
			double[] s = spectrogram[i];
			for (int j = fB[0]; j < fB[1]; j++) {
				pow[i] += s[j];
			}
		}
		return pow;
	}
	
	/**
	 * Use a Hilbert transform to get the waveform envelope squared. 
	 * @param wave
	 * @return
	 */
	double[] getEnvelope(double[] wave) {
		ComplexArray c = hilbert.getHilbertC(wave, fftLength);
		return c.magsq();
	}
	
	/**
	 * Smooth data with a moving average filter into
	 * a new array. Signal will be time shifted by smooth/2 bins. 
	 * @param data
	 * @return
	 */
	private double[] smoothData(double[] data) {
		if (smooth <= 1) {
			return data;
		}
		double[] out = Arrays.copyOf(data, data.length);
		double[] buf = Arrays.copyOf(data, smooth-1);
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < buf.length; j++) {
				out[i] += buf[j];
			}
			// cycle the buffer
			for (int j = 0; j < buf.length-1; j++) {
				buf[j] = buf[j+1];
			}
			buf[buf.length-1] = data[i];
			
		}
		return out;
	}

	/**
	 * Find a peak in any data array according to 
	 * threshold. If the difference between the median and the
	 * peak is less than the threshold, then use the half way point 
	 * between the median and peak. 
	 * @param data
	 * @return
	 */
	private PeakData findPeak(double[] data) {
		double median = std.getMedian(data);
		// get the maximum and it's position in the data. 
		double max = data[0];
		int maxBin = 0;
		for (int i = 1; i < data.length; i++) {
			if (data[i]>max) {
				max = data[i];
				maxBin = i;
			}
		}
		// set a threshold for deciding what's signal. 
		double sigLev;
		if (threshold < 1) {
			sigLev = max * threshold;
		}
		else {
			sigLev = max / threshold;
		}
		double medLev = (median + max) / 2.;
		sigLev = Math.max(sigLev, medLev); // this is the threshold
		/*
		 *  work forward and backward to see where the first and last
		 *  bins >= sigLev are
		 */
		double energy = max;
		int b1, b2;
		for (b2 = maxBin+1; b2 < data.length; b2++) {
			if (data[b2] < sigLev) {
				b2--;
				break;
			}
			energy += data[b2];
		}
		for (b1 = maxBin-1; b1 >= 0; b1--) {
			if (data[b1] < sigLev) {
				b1++;
				break;
			}
			energy = data[b1];
		}
		return new PeakData(maxBin, b1,b2,energy,median);
	}
	
	private class PeakData {
		
		int peakBin;
		int binStart, binEnd;
		double peakEnergy;
		private double median;

		/**
		 * @param binStart
		 * @param binEnd
		 */
		public PeakData(int peakBin, int binStart, int binEnd, double peakEnergy, double median) {
			super();
			this.peakBin = peakBin;
			this.binStart = binStart;
			this.binEnd = binEnd;
			this.peakEnergy = peakEnergy;
			this.median = median;
		}
	}
	
	
	
	

}
