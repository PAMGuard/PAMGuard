package annotation.calcs.measurement;

import java.util.EnumMap;

import Spectrogram.WindowFunction;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

/**
 * Calculates Standard sound measurements ({@link SoundMeasurement}) from a clip of sound and
 * the frequency limits of an annotation. The clip should cover exactly the time span of
 * the annotation.
 * <p>
 * The spectrogram is made from frames of fftLength samples, hop samples apart, and the
 * power spectral density of each bin is |X|<sup>2</sup> / (fs &Sigma;w<sup>2</sup>),
 * doubled for all but the DC and Nyquist bins so it's one sided. Only bins whose centre
 * frequency lies within the annotation's frequency limits are used (or the single bin
 * nearest the middle of the limits if none do).
 * <p>
 * This class has no PAMGuard data dependencies so it can be tested on its own.
 *
 * @author Jamie Macaulay
 */
public class SoundMeasurementCalculator {

	/**
	 * Smallest FFT that will be used for very short annotations.
	 */
	public static final int MIN_FFT_LENGTH = 16;

	private int fftLength;

	private int fftHop;

	private int windowType;

	/**
	 * @param fftLength  FFT length in samples. Reduced for clips shorter than this.
	 * @param fftHop     hop between frames in samples.
	 * @param windowType window function type (see {@link WindowFunction}).
	 */
	public SoundMeasurementCalculator(int fftLength, int fftHop, int windowType) {
		this.fftLength = fftLength;
		this.fftHop = fftHop;
		this.windowType = windowType;
	}

	/**
	 * Make all the measurements.
	 *
	 * @param wav        sound samples covering the annotation.
	 * @param sampleRate sample rate in Hz.
	 * @param fLow       lower frequency limit of the annotation in Hz.
	 * @param fHigh      upper frequency limit of the annotation in Hz.
	 * @return the measurements, or null if there are too few samples to measure.
	 */
	public EnumMap<SoundMeasurement, Double> calculate(double[] wav, double sampleRate, double fLow, double fHigh) {
		if (wav == null || wav.length < 2 || sampleRate <= 0) {
			return null;
		}
		if (fHigh < fLow) {
			double tmp = fLow; fLow = fHigh; fHigh = tmp;
		}
		EnumMap<SoundMeasurement, Double> m = new EnumMap<>(SoundMeasurement.class);

		calcWaveform(wav, sampleRate, fLow, fHigh, m);
		calcSpectrogram(wav, sampleRate, fLow, fHigh, m);

		m.put(SoundMeasurement.DELTA_TIME, wav.length / sampleRate);
		m.put(SoundMeasurement.DELTA_FREQUENCY, fHigh - fLow);

		return m;
	}

	/**
	 * Measurements based on waveform values.
	 */
	private void calcWaveform(double[] wav, double sampleRate, double fLow, double fHigh,
			EnumMap<SoundMeasurement, Double> m) {
		double max = wav[0], min = wav[0], sum2 = 0;
		int iMax = 0, iMin = 0;
		for (int i = 0; i < wav.length; i++) {
			double v = wav[i];
			sum2 += v * v;
			if (v > max) {
				max = v;
				iMax = i;
			}
			if (v < min) {
				min = v;
				iMin = i;
			}
		}
		m.put(SoundMeasurement.MAX_AMPLITUDE, max);
		m.put(SoundMeasurement.MIN_AMPLITUDE, min);
		m.put(SoundMeasurement.MAX_TIME, iMax / sampleRate);
		m.put(SoundMeasurement.MIN_TIME, iMin / sampleRate);
		// the first sample with the peak amplitude, whichever sign it has.
		double peak = Math.max(Math.abs(max), Math.abs(min));
		int iPeak = Math.abs(max) >= Math.abs(min) ? iMax : iMin;
		if (Math.abs(max) == Math.abs(min)) {
			iPeak = Math.min(iMax, iMin);
		}
		m.put(SoundMeasurement.PEAK_AMPLITUDE, peak);
		m.put(SoundMeasurement.PEAK_TIME, iPeak / sampleRate);
		m.put(SoundMeasurement.RMS_AMPLITUDE, Math.sqrt(sum2 / wav.length));
		m.put(SoundMeasurement.FILTERED_RMS_AMPLITUDE, filteredRMS(wav, sampleRate, fLow, fHigh));
	}

	/**
	 * RMS amplitude within the frequency limits. Equivalent to an ideal band pass filter,
	 * worked out from the spectrum of the whole clip using Parseval's theorem.
	 */
	private double filteredRMS(double[] wav, double sampleRate, double fLow, double fHigh) {
		int n = wav.length;
		int nFFT = Integer.highestOneBit(n);
		if (nFFT < n) {
			nFFT <<= 1;
		}
		double[] x = new double[nFFT];
		System.arraycopy(wav, 0, x, 0, n);
		new DoubleFFT_1D(nFFT).realForward(x);
		double binWidth = sampleRate / nFFT;
		double energy = 0;
		for (int k = 0; k <= nFFT / 2; k++) {
			double f = k * binWidth;
			if (f < fLow || f > fHigh) {
				continue;
			}
			double magSq = magSq(x, k, nFFT);
			// bins other than DC and Nyquist appear twice in the full spectrum.
			energy += (k == 0 || k == nFFT / 2) ? magSq : 2 * magSq;
		}
		// Parseval: sum(x^2) = sum(|X|^2) / nFFT
		return Math.sqrt(energy / nFFT / n);
	}

	/**
	 * Measurements based on spectrogram values, including the robust signal measurements.
	 */
	private void calcSpectrogram(double[] wav, double sampleRate, double fLow, double fHigh,
			EnumMap<SoundMeasurement, Double> m) {
		int fftLen = fftLength;
		int hop = fftHop;
		if (wav.length < fftLen) {
			// clip too short for a whole frame - use the biggest FFT which fits.
			fftLen = Math.max(MIN_FFT_LENGTH, Integer.highestOneBit(wav.length));
			hop = Math.max(1, fftLen / 2);
		}
		if (hop <= 0) {
			hop = fftLen / 2;
		}
		int nFrames = wav.length < fftLen ? 1 : 1 + (wav.length - fftLen) / hop;
		m.put(SoundMeasurement.LENGTH, (double) nFrames);

		double binWidth = sampleRate / fftLen;
		int b0 = (int) Math.ceil(fLow / binWidth);
		int b1 = (int) Math.floor(fHigh / binWidth);
		b0 = Math.max(0, b0);
		b1 = Math.min(fftLen / 2, b1);
		if (b1 < b0) {
			// band narrower than one bin - use the bin nearest the middle.
			int mid = (int) Math.round((fLow + fHigh) / 2 / binWidth);
			mid = Math.max(0, Math.min(fftLen / 2, mid));
			b0 = b1 = mid;
		}
		int nBins = b1 - b0 + 1;

		double[] window = WindowFunction.getWindowFunc(windowType, fftLen);
		double winSum2 = 0;
		for (double w : window) {
			winSum2 += w * w;
		}
		double psdScale = 1. / (sampleRate * winSum2);

		// power spectral density for each frame and bin within the annotation.
		double[][] psd = new double[nFrames][nBins];
		DoubleFFT_1D fft = new DoubleFFT_1D(fftLen);
		double[] frame = new double[fftLen];
		for (int t = 0; t < nFrames; t++) {
			int start = t * hop;
			for (int i = 0; i < fftLen; i++) {
				int j = start + i;
				frame[i] = j < wav.length ? wav[j] * window[i] : 0;
			}
			fft.realForward(frame);
			for (int b = b0; b <= b1; b++) {
				double p = magSq(frame, b, fftLen) * psdScale;
				if (b != 0 && b != fftLen / 2) {
					p *= 2;
				}
				psd[t][b - b0] = p;
			}
		}

		// totals over time (per bin) and frequency (per frame)
		double[] binEnergy = new double[nBins];
		double[] frameEnergy = new double[nFrames];
		double total = 0, peak = -1;
		int tPeak = 0, bPeak = 0;
		double avgEntropy = 0;
		for (int t = 0; t < nFrames; t++) {
			for (int b = 0; b < nBins; b++) {
				double p = psd[t][b];
				binEnergy[b] += p;
				frameEnergy[t] += p;
				total += p;
				// strict > keeps the first time and then the lowest frequency of the peak.
				if (p > peak) {
					peak = p;
					tPeak = t;
					bPeak = b;
				}
			}
			avgEntropy += entropy(psd[t], frameEnergy[t]);
		}
		avgEntropy /= nFrames;

		m.put(SoundMeasurement.AVERAGE_POWER, dB(total / (nFrames * nBins)));
		m.put(SoundMeasurement.ENERGY, dB(total * binWidth));
		m.put(SoundMeasurement.PEAK_POWER, dB(peak));
		m.put(SoundMeasurement.PEAK_FREQUENCY, (b0 + bPeak) * binWidth);
		m.put(SoundMeasurement.PEAK_POWER_TIME, frameTime(tPeak, hop, fftLen, sampleRate));
		// the selection spectrum is the average of the frames, so the frame count cancels.
		m.put(SoundMeasurement.DELTA_POWER, dB(binEnergy[nBins - 1] / nFrames) - dB(binEnergy[0] / nFrames));
		m.put(SoundMeasurement.AGGREGATE_ENTROPY, entropy(binEnergy, total));
		m.put(SoundMeasurement.AVERAGE_ENTROPY, avgEntropy);

		// robust frequency measurements
		double f5 = (b0 + quantileIndex(binEnergy, total, 0.05)) * binWidth;
		double f25 = (b0 + quantileIndex(binEnergy, total, 0.25)) * binWidth;
		double f50 = (b0 + quantileIndex(binEnergy, total, 0.5)) * binWidth;
		double f75 = (b0 + quantileIndex(binEnergy, total, 0.75)) * binWidth;
		double f95 = (b0 + quantileIndex(binEnergy, total, 0.95)) * binWidth;
		m.put(SoundMeasurement.FREQ_5, f5);
		m.put(SoundMeasurement.FREQ_25, f25);
		m.put(SoundMeasurement.CENTER_FREQUENCY, f50);
		m.put(SoundMeasurement.FREQ_75, f75);
		m.put(SoundMeasurement.FREQ_95, f95);
		m.put(SoundMeasurement.IQR_BANDWIDTH, f75 - f25);
		m.put(SoundMeasurement.BANDWIDTH_90, f95 - f5);

		// robust time measurements
		double t5 = frameTime(quantileIndex(frameEnergy, total, 0.05), hop, fftLen, sampleRate);
		double t25 = frameTime(quantileIndex(frameEnergy, total, 0.25), hop, fftLen, sampleRate);
		double t50 = frameTime(quantileIndex(frameEnergy, total, 0.5), hop, fftLen, sampleRate);
		double t75 = frameTime(quantileIndex(frameEnergy, total, 0.75), hop, fftLen, sampleRate);
		double t95 = frameTime(quantileIndex(frameEnergy, total, 0.95), hop, fftLen, sampleRate);
		m.put(SoundMeasurement.TIME_5, t5);
		m.put(SoundMeasurement.TIME_25, t25);
		m.put(SoundMeasurement.CENTER_TIME, t50);
		m.put(SoundMeasurement.TIME_75, t75);
		m.put(SoundMeasurement.TIME_95, t95);
		m.put(SoundMeasurement.IQR_DURATION, t75 - t25);
		m.put(SoundMeasurement.DURATION_90, t95 - t5);
	}

	/**
	 * The smallest index at which the cumulative energy reaches a fraction of the total.
	 * This is how the center, quartile and 5% / 95% frequencies and times are defined.
	 */
	static int quantileIndex(double[] energy, double total, double fraction) {
		double target = total * fraction;
		double cum = 0;
		for (int i = 0; i < energy.length; i++) {
			cum += energy[i];
			if (cum >= target) {
				return i;
			}
		}
		return energy.length - 1;
	}

	/**
	 * Shannon entropy in bits of an energy distribution, treating the fraction of the
	 * energy in each element as a probability.
	 */
	static double entropy(double[] energy, double total) {
		if (total <= 0) {
			return 0;
		}
		double h = 0;
		for (double e : energy) {
			if (e > 0) {
				double p = e / total;
				h -= p * Math.log(p);
			}
		}
		return h / Math.log(2);
	}

	/**
	 * Time of the centre of a spectrogram frame, from the start of the clip.
	 */
	private static double frameTime(int frame, int hop, int fftLen, double sampleRate) {
		return (frame * hop + fftLen / 2.) / sampleRate;
	}

	/**
	 * Magnitude squared of bin k from JTransforms realForward output, which packs the
	 * real Nyquist value into element 1.
	 */
	private static double magSq(double[] x, int k, int n) {
		if (k == 0) {
			return x[0] * x[0];
		}
		if (k == n / 2) {
			return x[1] * x[1];
		}
		return x[2 * k] * x[2 * k] + x[2 * k + 1] * x[2 * k + 1];
	}

	private static double dB(double power) {
		return 10. * Math.log10(power);
	}

	public int getFftLength() {
		return fftLength;
	}

	public int getFftHop() {
		return fftHop;
	}

	public int getWindowType() {
		return windowType;
	}
}
