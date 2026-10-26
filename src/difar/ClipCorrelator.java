package difar;

import java.util.ArrayList;
import java.util.List;

import Localiser.algorithms.Correlations;
import PamUtils.complex.ComplexArray;
import fftManager.Complex;
import fftManager.FastFFT;

/**
 * Measures the time delay between the same call in two DIFAR clips, by cross
 * correlating their spectrograms.
 * <p>
 * Each clip's spectrogram is cut to a band of frequency bins, converted to log
 * power, and normalised within each bin to zero mean and unit variance over
 * time. A steady noise line therefore does not dominate, and the shape of the
 * call does. The two spectrograms are correlated bin by bin, the results summed
 * over the band, and the sum scaled so that identical spectrograms give 1.
 * Spectrograms are used rather than waveforms because the waveform of a call
 * received on sonobuoys kilometres apart is changed by multipath, while its
 * shape in the spectrogram survives.
 * <p>
 * The delay is the arrival time on the second clip minus that on the first.
 * Candidate delays are the local maxima of the correlation, interpolated
 * between frames, within the largest delay possible between the two sonobuoys,
 * and where the clips overlap enough to compare. A maximum at the edge of the
 * allowed range is not a peak, and is not returned.
 * <p>
 * In noise the main peak can split into two or three maxima a few frames
 * apart. The correlation is therefore smoothed with a short triangular window
 * before the peaks are found, and a peak closer than a minimum separation to a
 * higher one is dropped. Both are settings: smoothing trades a little
 * resolution for stability, and the separation should be shorter than the
 * delay between multipath arrivals that are to be kept apart.
 * <p>
 * The correlation uses the same route as {@link Correlations#getDelay}: a real
 * FFT of each series, one multiplied by the conjugate of the other, and a real
 * inverse FFT. {@link Correlations#getCorrelation} is not used, since it gives
 * inaccurate results.
 * <p>
 * The class works on arrays only and keeps no PAMGuard state, so it can be
 * tested alone. One instance is not safe to share between threads, because the
 * FFT keeps working storage.
 */
public class ClipCorrelator {

	/** One candidate delay. */
	public static final class Peak {

		private final double delaySeconds;
		private final double height;

		Peak(double delaySeconds, double height) {
			this.delaySeconds = delaySeconds;
			this.height = height;
		}

		/** @return arrival time on the second clip minus that on the first, in seconds. */
		public double getDelaySeconds() {
			return delaySeconds;
		}

		/** @return the height of the correlation peak, at most 1 for identical clips. */
		public double getHeight() {
			return height;
		}

		@Override
		public String toString() {
			return String.format("%.3f s (%.2f)", delaySeconds, height);
		}
	}

	/** Added to power before taking the log, so silent bins give a finite value. */
	private static final double POWER_FLOOR = 1e-20;

	private final double threshold;
	private final int maxPeaks;
	private final double minOverlapFraction;
	private final double smoothingSeconds;
	private final double minSeparationSeconds;

	private final FastFFT fastFFT = new FastFFT();
	private final Correlations correlations = new Correlations();

	/**
	 * @param threshold smallest peak height kept, between 0 and 1.
	 * @param maxPeaks most peaks returned, highest first.
	 * @param minOverlapFraction shortest overlap of the two clips at which a
	 * delay is considered, as a fraction of the shorter clip.
	 * @param smoothingSeconds half width of the triangular window the
	 * correlation is smoothed with, in seconds; 0 for none.
	 * @param minSeparationSeconds closest two peaks may be, in seconds. Of two
	 * closer peaks, the lower is dropped.
	 */
	public ClipCorrelator(double threshold, int maxPeaks, double minOverlapFraction,
			double smoothingSeconds, double minSeparationSeconds) {
		this.threshold = threshold;
		this.maxPeaks = maxPeaks;
		this.minOverlapFraction = minOverlapFraction;
		this.smoothingSeconds = smoothingSeconds;
		this.minSeparationSeconds = minSeparationSeconds;
	}

	/**
	 * Find candidate delays between the same call in two clips.
	 * @param specA power spectrogram of the first clip, [frame][bin].
	 * @param specB power spectrogram of the second clip, with the same FFT
	 * length and hop as the first.
	 * @param frameSeconds the hop between frames, in seconds.
	 * @param offsetSeconds start of the second clip minus start of the first,
	 * in seconds.
	 * @param binRange first bin used, and one past the last.
	 * @param maxDelaySeconds largest delay possible between the two sonobuoys,
	 * in seconds, including any allowance for timing error.
	 * @return candidate delays, highest peak first. Empty if there are none,
	 * never null.
	 */
	public List<Peak> correlate(double[][] specA, double[][] specB, double frameSeconds,
			double offsetSeconds, int[] binRange, double maxDelaySeconds) {
		List<Peak> peaks = new ArrayList<>();
		if (specA == null || specB == null || specA.length < 3 || specB.length < 3
				|| binRange == null || binRange[1] <= binRange[0] || frameSeconds <= 0) {
			return peaks;
		}
		int nA = specA.length;
		int nB = specB.length;
		int nBins = binRange[1] - binRange[0];
		double[][] a = normalisedBins(specA, binRange);
		double[][] b = normalisedBins(specB, binRange);
		if (a == null || b == null) {
			return peaks;
		}

		// shift k: frame t of the second clip lines up with frame t - k of the first
		int fftLength = FastFFT.nextBinaryExp(nA + nB);
		double[] sum = new double[fftLength];
		for (int i = 0; i < nBins; i++) {
			ComplexArray fa = fastFFT.rfft(a[i], fftLength);
			ComplexArray fb = fastFFT.rfft(b[i], fftLength);
			double[] xc = fastFFT.realInverse(fb.conjTimes(fa));
			for (int j = 0; j < fftLength; j++) {
				sum[j] += xc[j];
			}
		}
		// each normalised series has a sum of squares equal to its length
		double scale = nBins * Math.sqrt((double) nA * nB);

		// the shifts allowed by the largest delay and by the overlap
		int minOverlap = (int) Math.ceil(minOverlapFraction * Math.min(nA, nB));
		int kLow = Math.max(-(nA - 1), (int) Math.ceil((-maxDelaySeconds - offsetSeconds) / frameSeconds));
		int kHigh = Math.min(nB - 1, (int) Math.floor((maxDelaySeconds - offsetSeconds) / frameSeconds));
		while (kLow <= kHigh && overlap(kLow, nA, nB) < minOverlap) {
			kLow++;
		}
		while (kHigh >= kLow && overlap(kHigh, nA, nB) < minOverlap) {
			kHigh--;
		}
		if (kHigh - kLow < 2) {
			return peaks;
		}
		double[] function = new double[kHigh - kLow + 1];
		for (int k = kLow; k <= kHigh; k++) {
			function[k - kLow] = sum[(k + fftLength) % fftLength] / scale;
		}

		function = smooth(function, (int) Math.round(smoothingSeconds / frameSeconds));
		double[][] maxima = correlations.getInterpolatedMaxima(function);
		if (maxima == null) {
			return peaks;
		}
		List<Peak> found = new ArrayList<>();
		for (int i = 0; i < maxima[0].length; i++) {
			if (maxima[1][i] >= threshold) {
				double shift = kLow + maxima[0][i];
				found.add(new Peak(offsetSeconds + shift * frameSeconds, maxima[1][i]));
			}
		}
		found.sort((p, q) -> Double.compare(q.getHeight(), p.getHeight()));
		for (Peak candidate : found) {
			if (peaks.size() == maxPeaks) {
				break;
			}
			boolean separate = true;
			for (Peak kept : peaks) {
				if (Math.abs(kept.getDelaySeconds() - candidate.getDelaySeconds()) < minSeparationSeconds) {
					separate = false;
					break;
				}
			}
			if (separate) {
				peaks.add(candidate);
			}
		}
		return peaks;
	}

	/**
	 * Smooth a function with a triangular window. Near the ends, only the
	 * part of the window inside the function is used.
	 * @param f the function.
	 * @param half half width of the window, in samples; 0 or less for none.
	 * @return the smoothed function, or f itself if there is no smoothing.
	 */
	private static double[] smooth(double[] f, int half) {
		if (half <= 0) {
			return f;
		}
		double[] out = new double[f.length];
		for (int i = 0; i < f.length; i++) {
			double sum = 0, weight = 0;
			for (int j = -half; j <= half; j++) {
				int k = i + j;
				if (k >= 0 && k < f.length) {
					double w = half + 1 - Math.abs(j);
					sum += w * f[k];
					weight += w;
				}
			}
			out[i] = sum / weight;
		}
		return out;
	}

	/**
	 * Power spectrogram of a waveform, made as the DIFAR clip displays make
	 * theirs: a Hann window, and the squared magnitude of each bin. Frame i
	 * starts at sample i * hop.
	 * @param wave the waveform.
	 * @param fftLength FFT length, a power of two.
	 * @param hop samples between frames.
	 * @return power, [frame][bin] with fftLength / 2 bins, or null if the
	 * waveform is shorter than one FFT.
	 */
	public static double[][] spectrogram(double[] wave, int fftLength, int hop) {
		if (wave == null || hop <= 0 || wave.length < fftLength) {
			return null;
		}
		int nFrames = (wave.length - fftLength) / hop + 1;
		double[][] spec = new double[nFrames][fftLength / 2];
		double[] window = new double[fftLength];
		for (int j = 0; j < fftLength; j++) {
			window[j] = 0.5 - 0.5 * Math.cos(2 * Math.PI * j / (fftLength - 1));
		}
		FastFFT fft = new FastFFT();
		Complex[] out = Complex.allocateComplexArray(fftLength / 2);
		double[] frame = new double[fftLength];
		int m = FastFFT.log2(fftLength);
		for (int i = 0; i < nFrames; i++) {
			for (int j = 0; j < fftLength; j++) {
				frame[j] = wave[i * hop + j] * window[j];
			}
			fft.rfft(frame, out, m);
			for (int j = 0; j < fftLength / 2; j++) {
				spec[i][j] = out[j].magsq();
			}
		}
		return spec;
	}

	/**
	 * @param k shift of the second clip against the first, in frames.
	 * @return how many frames of the two clips overlap at that shift.
	 */
	private static int overlap(int k, int nA, int nB) {
		return Math.max(0, Math.min(nB, nA + k) - Math.max(0, k));
	}

	/**
	 * Cut a spectrogram to a band and normalise each bin over time.
	 * @param spec power spectrogram, [frame][bin].
	 * @param binRange first bin used, and one past the last.
	 * @return [bin][frame] of log power with zero mean and unit variance in
	 * each bin; a bin with no variation is all zero. Null if the band is not
	 * inside the spectrogram.
	 */
	private static double[][] normalisedBins(double[][] spec, int[] binRange) {
		int nFrames = spec.length;
		if (binRange[0] < 0 || binRange[1] > spec[0].length) {
			return null;
		}
		int nBins = binRange[1] - binRange[0];
		double[][] out = new double[nBins][nFrames];
		for (int i = 0; i < nBins; i++) {
			double mean = 0;
			for (int t = 0; t < nFrames; t++) {
				out[i][t] = Math.log10(spec[t][binRange[0] + i] + POWER_FLOOR);
				mean += out[i][t];
			}
			mean /= nFrames;
			double var = 0;
			for (int t = 0; t < nFrames; t++) {
				out[i][t] -= mean;
				var += out[i][t] * out[i][t];
			}
			double sd = Math.sqrt(var / nFrames);
			for (int t = 0; t < nFrames; t++) {
				out[i][t] = sd > 0 ? out[i][t] / sd : 0;
			}
		}
		return out;
	}
}
