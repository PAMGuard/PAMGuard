package difar.demux;

import java.awt.Component;
import java.awt.Window;
import java.util.HashMap;
import java.util.Map;

import difar.DemuxObserver;
import difar.DifarDataUnit;
import difar.DifarParameters;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;
import fftManager.FastFFT;

/**
 * The AAD demultiplexer, done in the frequency domain at the output sample rate
 * ("AAD fast (frequency domain)" in DIFAR settings).
 * <p>
 * {@link AmmcDemux} ("AAD (time domain)") demodulates every sample of a DIFAR clip at the full sample rate,
 * with about nine full-length FFTs and three IIR filters, and then keeps one sample in
 * D. This class takes one FFT of the clip and cuts from it only the bins the output
 * needs, shifting each slice to baseband and inverse transforming it at the output rate:
 * <ul>
 * <li>omni: 0 Hz to the output Nyquist frequency</li>
 * <li>phase pilot: the 15 kHz pilot, +-4 Hz, as an analytic signal</li>
 * <li>directional: the band below the pilot, as wide as the output band, with the
 * pilot's +-4 Hz notched out</li>
 * </ul>
 * The directional channels are the directional band times the pilot's analytic signal,
 * as in AmmcDemux, but formed at the output rate. The output rate is the clip's sample
 * rate divided by the decimation factor, so a classification with a higher sample rate
 * gets a wider band, as with AmmcDemux.
 * <p>
 * Differences from AmmcDemux, all small:
 * <ul>
 * <li>A brick-wall low-pass at the output Nyquist frequency, instead of an 8th-order
 * Butterworth: no phase delay, and a flat response up to the edge.</li>
 * <li>Output sample j is at input sample j*D (AmmcDemux: (j+1)*D - 1, plus the
 * Butterworth's delay).</li>
 * <li>The FFT is of the clip zero-padded to D times a power of two (AmmcDemux's band
 * filters use the clip's own length), so pilot frequencies and bin edges fall slightly
 * differently.</li>
 * </ul>
 * It reproduces one quirk of AmmcDemux on purpose: AmmcDemux's Hilbert transform is
 * unscaled at its power-of-two length but divided by the clip length, which scales the
 * directional channels relative to the omni by nextPow2(n)/n, a factor between 1 and 2
 * that varies with clip length. Bearings do not depend on it; the DIFARgram's shape does.
 * Kept so that the two demultiplexers can be compared clip by clip.
 */
public class AadFastDemux extends DifarDemux {

	/** Frequency pilot, Hz, and the half width searched for it. */
	private static final double FREQ_PILOT = 7500, PILOT_SEARCH = 100;

	/** Half width of the phase pilot's band and notch, Hz. */
	private static final double PILOT_HALF_BAND = 4;

	/** Half width of the directional band about the phase pilot, Hz. */
	private static final double DIRECTIONAL_HALF_BAND = 4000;

	/** FFTs by length. Clip lengths vary, but N is D times a power of two, so few lengths recur. */
	private final Map<Integer, DoubleFFT_1D> ffts = new HashMap<>();

	private DoubleFFT_1D fft(int n) {
		return ffts.computeIfAbsent(n, DoubleFFT_1D::new);
	}

	@Override
	public boolean configDemux(DifarParameters difarParams, double sampleRate) {
		return false;
	}

	@Override
	public synchronized DifarResult processClip(double[] difarClip, double sampleRate,
			int decimationFactor, DemuxObserver observer, DifarDataUnit difarDataUnit) {
		int n = difarClip.length;
		int d = Math.max(1, decimationFactor);
		int nOutput = n / d;
		DifarResult result = new DifarResult(nOutput);
		if (nOutput == 0) {
			return result;
		}

		// N = D * M, M a power of two, so that output sample j falls on input sample j*D
		int m = Math.max(2, Integer.highestOneBit(Math.max(1, (n + d - 1) / d)));
		if (m * (long) d < n) {
			m *= 2;
		}
		int bigN = d * m;
		double[] x = new double[bigN];
		System.arraycopy(difarClip, 0, x, 0, n);
		fft(bigN).realForward(x);
		double df = sampleRate / bigN;

		// frequency pilot: the largest bin within 100 Hz of 7.5 kHz; the phase pilot is twice it
		int lo = (int) Math.floor((FREQ_PILOT - PILOT_SEARCH) / df);
		int hi = (int) Math.ceil((FREQ_PILOT + PILOT_SEARCH) / df);
		int k75 = lo;
		double best = -1;
		for (int k = lo; k <= hi; k++) {
			double p = power(x, k, bigN);
			if (p > best) {
				best = p;
				k75 = k;
			}
		}
		int kp = 2 * k75;

		int half = m / 2 - 1;                       // bins strictly below the output Nyquist
		int hp = (int) Math.floor(PILOT_HALF_BAND / df);
		int hd = (int) Math.min(half, Math.floor(DIRECTIONAL_HALF_BAND / df));
		if (kp + Math.max(hd, hp) >= bigN / 2 || kp - hd <= 0) {
			return result;                          // sample rate too low for a 15 kHz pilot
		}

		// omni: low-pass, a real signal
		double[] zo = new double[2 * m];
		for (int k = 0; k <= half; k++) {
			put(zo, k, re(x, k, bigN), im(x, k, bigN));
			if (k > 0) {
				put(zo, m - k, re(x, k, bigN), -im(x, k, bigN));
			}
		}
		// phase pilot, shifted to baseband: bins kp-hp .. kp+hp
		double[] zp = new double[2 * m];
		double pilotPower = 0;
		for (int j = -hp; j <= hp; j++) {
			int k = kp + j;
			put(zp, Math.floorMod(j, m), re(x, k, bigN), im(x, k, bigN));
			pilotPower += power(x, k, bigN);
		}
		// directional band below the pilot, shifted to baseband, conjugated, pilot notched out
		double[] zd = new double[2 * m];
		for (int j = -hd; j <= hd; j++) {
			if (Math.abs(j) <= hp) {
				continue;
			}
			int k = kp - j;
			put(zd, Math.floorMod(j, m), re(x, k, bigN), -im(x, k, bigN));
		}
		DoubleFFT_1D small = fft(m);
		small.complexInverse(zo, false);
		small.complexInverse(zp, false);
		small.complexInverse(zd, false);

		/*
		 * AmmcDemux divides by ppMag = rms(pilot) / sqrt(2), and by its Hilbert scaling.
		 * The pilot's rms over the clip, from its bins (Parseval): sum of squares over N
		 * samples is 2/N times the sum of |X|^2 over the band.
		 */
		double ppRms = Math.sqrt(2 * pilotPower / bigN / n);
		double ppMag = ppRms / Math.sqrt(2);
		double scale = (double) FastFFT.nextBinaryExp(n) / n;
		double gain = ppMag > 0 ? -scale / ppMag : 0;   // a silent channel gives zeros, not NaN

		for (int j = 0; j < nOutput; j++) {
			int i = 2 * j;
			result.om[j] = zo[i] / bigN;
			double ar = 2 * zp[i] / bigN, ai = 2 * zp[i + 1] / bigN;
			double dr = zd[i] / bigN, di = zd[i + 1] / bigN;
			result.ew[j] = gain * (ar * dr - ai * di);
			result.ns[j] = gain * (ar * di + ai * dr);
			result.lock_15[j] = true; // no lock detection, as AmmcDemux
			result.lock_75[j] = true;
		}
		return result;
	}

	/** Real part of bin k of a JTransforms realForward array of even length n. */
	private static double re(double[] a, int k, int n) {
		if (k == 0) {
			return a[0];
		}
		if (k == n / 2) {
			return a[1];
		}
		return a[2 * k];
	}

	/** Imaginary part of bin k of a JTransforms realForward array of even length n. */
	private static double im(double[] a, int k, int n) {
		return (k == 0 || k == n / 2) ? 0 : a[2 * k + 1];
	}

	private static double power(double[] a, int k, int n) {
		double r = re(a, k, n), i = im(a, k, n);
		return r * r + i * i;
	}

	private static void put(double[] z, int k, double r, double i) {
		z[2 * k] = r;
		z[2 * k + 1] = i;
	}

	@Override
	public boolean hasOptions() {
		return false;
	}

	@Override
	public boolean showOptions(Window window, DifarParameters difarParams) {
		return false;
	}

	@Override
	public Component getDisplayComponent() {
		return null;
	}
}
