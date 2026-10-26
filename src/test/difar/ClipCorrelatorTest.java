package test.difar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import difar.ClipCorrelator;
import difar.ClipCorrelator.Peak;

/**
 * Tests for measuring delays between DIFAR clips by spectrogram correlation.
 * <p>
 * Clips are synthetic: a right whale style upcall, a sweep from 100 to 200 Hz
 * over 1 s, in white noise, sampled at 2 kHz. Spectrograms are made by
 * ClipCorrelator.spectrogram, as the DIFAR module makes them: Hann window,
 * power, FFT length 256, hop 32 (16 ms). No PAMGuard runtime is needed.
 */
public class ClipCorrelatorTest {

	private static final double FS = 2000;
	private static final int FFT = 256;
	private static final int HOP = 32;
	private static final double FRAME = HOP / FS;
	/** The upcall band, 47 to 357 Hz, as bins. */
	private static final int[] BAND = {6, 46};
	/**
	 * Tolerance on a delay: three frames, 48 ms. Noise moves the peak by up to
	 * a few frames; the clip start times it replaces are good to about 1 s.
	 */
	private static final double TOL = 3 * FRAME;

	/** Smoothing half width and minimum peak separation, as the defaults will be. */
	private static final double SMOOTH = 0.064, SEPARATION = 0.15;

	private final ClipCorrelator best = new ClipCorrelator(0.05, 1, 0.5, SMOOTH, SEPARATION);

	/** Add an upcall starting at a time, with an amplitude. */
	private static void addUpcall(double[] wave, double start, double amplitude) {
		int i0 = (int) Math.round(start * FS);
		int n = (int) FS;
		double phase = 0;
		for (int i = 0; i < n && i0 + i < wave.length; i++) {
			double t = i / FS;
			double f = 100 + 100 * t;
			phase += 2 * Math.PI * f / FS;
			double taper = Math.sin(Math.PI * t); // smooth onset and end
			if (i0 + i >= 0) {
				wave[i0 + i] += amplitude * taper * Math.sin(phase);
			}
		}
	}

	private static double[] noise(double seconds, double sd, long seed) {
		Random r = new Random(seed);
		double[] w = new double[(int) (seconds * FS)];
		for (int i = 0; i < w.length; i++) {
			w[i] = sd * r.nextGaussian();
		}
		return w;
	}

	private static double[][] spectrogram(double[] wave) {
		return ClipCorrelator.spectrogram(wave, FFT, HOP);
	}

	/** A 2 s clip with an upcall at a time within it. */
	private static double[][] clip(double callAt, double amplitude, long seed) {
		double[] w = noise(2.5, 1, seed);
		addUpcall(w, callAt, amplitude);
		return spectrogram(w);
	}

	@Test
	public void findsDelayBetweenClipsStartingAtDifferentTimes() {
		// second clip starts 3 s later; the call is 0.3 s into the first and 0.8 s into the second
		List<Peak> p = best.correlate(clip(0.3, 3, 1), clip(0.8, 3, 2), FRAME, 3.0, BAND, 20);
		assertEquals(1, p.size());
		assertEquals(3.5, p.get(0).getDelaySeconds(), TOL);
		assertTrue(p.get(0).getHeight() > 0.1, "peak " + p.get(0));
	}

	@Test
	public void findsNegativeDelay() {
		// the second clip starts 2 s earlier, and the call arrives there 1.6 s earlier
		List<Peak> p = best.correlate(clip(0.6, 3, 3), clip(1.0, 3, 4), FRAME, -2.0, BAND, 20);
		assertEquals(1, p.size());
		assertEquals(-1.6, p.get(0).getDelaySeconds(), TOL);
	}

	@Test
	public void findsCallInLowSignalToNoise() {
		// peak amplitude 1 in noise of standard deviation 1, over several noise samples
		for (long seed = 20; seed < 30; seed++) {
			List<Peak> p = best.correlate(clip(0.4, 1, seed), clip(0.9, 1, seed + 100), FRAME, 0, BAND, 20);
			assertEquals(1, p.size(), "seed " + seed);
			assertEquals(0.5, p.get(0).getDelaySeconds(), TOL, "seed " + seed);
		}
	}

	@Test
	public void multipathGivesSecondPeak() {
		// the second clip holds the call and a weaker copy 0.3 s later
		double[] w = noise(2.5, 0.5, 8);
		addUpcall(w, 0.5, 3);
		addUpcall(w, 0.8, 1.8);
		ClipCorrelator two = new ClipCorrelator(0.03, 2, 0.5, SMOOTH, SEPARATION);
		List<Peak> p = two.correlate(clip(0.5, 3, 7), spectrogram(w), FRAME, 0, BAND, 20);
		assertEquals(2, p.size(), "peaks " + p);
		assertEquals(0.0, p.get(0).getDelaySeconds(), TOL);
		assertEquals(0.3, p.get(1).getDelaySeconds(), TOL);
		assertTrue(p.get(1).getHeight() < p.get(0).getHeight());
	}

	@Test
	public void noiseAloneGivesNoPeakAboveThreshold() {
		ClipCorrelator strict = new ClipCorrelator(0.3, 1, 0.5, SMOOTH, SEPARATION);
		List<Peak> p = strict.correlate(spectrogram(noise(2.5, 1, 9)), spectrogram(noise(2.5, 1, 10)),
				FRAME, 0, BAND, 20);
		assertTrue(p.isEmpty(), "peaks " + p);
	}

	@Test
	public void delayBeyondTheLimitIsNotReturned() {
		// true delay 1.0 s, but the sonobuoys can be at most 0.5 s apart
		List<Peak> p = best.correlate(clip(0.3, 3, 11), clip(1.3, 3, 12), FRAME, 0, BAND, 0.5);
		for (Peak peak : p) {
			assertTrue(Math.abs(peak.getDelaySeconds()) <= 0.5, "peak " + peak);
			assertTrue(Math.abs(peak.getDelaySeconds() - 1.0) > 0.2, "peak " + peak);
		}
	}

	@Test
	public void steadyToneDoesNotHideTheCall() {
		// a strong constant tone at 150 Hz, inside the band, on both clips
		double[] wa = noise(2.5, 1, 13);
		double[] wb = noise(2.5, 1, 14);
		for (int i = 0; i < wa.length; i++) {
			double tone = 20 * Math.sin(2 * Math.PI * 150 * i / FS);
			wa[i] += tone;
			wb[i] += tone;
		}
		addUpcall(wa, 0.2, 3);
		addUpcall(wb, 0.9, 3);
		List<Peak> p = best.correlate(spectrogram(wa), spectrogram(wb), FRAME, 0, BAND, 20);
		assertEquals(1, p.size());
		assertEquals(0.7, p.get(0).getDelaySeconds(), TOL);
	}

	@Test
	public void soundOutsideTheBandIsIgnored() {
		// a loud sweep at 600 to 800 Hz on the second clip only, at a different time
		double[] wb = noise(2.5, 1, 16);
		addUpcall(wb, 1.0, 3);
		double phase = 0;
		for (int i = (int) (0.1 * FS); i < (int) (0.9 * FS); i++) {
			phase += 2 * Math.PI * (600 + 250 * (i / FS)) / FS;
			wb[i] += 30 * Math.sin(phase);
		}
		List<Peak> p = best.correlate(clip(0.4, 3, 15), spectrogram(wb), FRAME, 0, BAND, 20);
		assertEquals(1, p.size());
		assertEquals(0.6, p.get(0).getDelaySeconds(), TOL);
	}

	@Test
	public void splitPeaksNearTheTrueDelayAreReturnedOnce() {
		// in noise, the main peak can split; only one of its parts may be returned
		ClipCorrelator three = new ClipCorrelator(0.0, 3, 0.5, SMOOTH, SEPARATION);
		for (long seed = 40; seed < 50; seed++) {
			List<Peak> p = three.correlate(clip(0.3, 1, seed), clip(0.8, 1, seed + 100), FRAME, 0, BAND, 20);
			int near = 0;
			for (Peak peak : p) {
				if (Math.abs(peak.getDelaySeconds() - 0.5) < 0.12) {
					near++;
				}
			}
			assertEquals(1, near, "seed " + seed + ", peaks " + p);
		}
	}

	@Test
	public void emptyInputGivesNoPeaks() {
		assertTrue(best.correlate(null, clip(0.5, 3, 17), FRAME, 0, BAND, 20).isEmpty());
		assertTrue(best.correlate(clip(0.5, 3, 18), clip(0.5, 3, 19), FRAME, 0, new int[] {10, 10}, 20).isEmpty());
	}
}
