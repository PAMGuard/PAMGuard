package clickTrainDetector.clickTrainAlgorithms.adaptive.test;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import PamguardMVC.PamDataUnit;
import clickTrainDetector.clickTrainAlgorithms.adaptive.AdaptiveCTChi2Provider;
import clickTrainDetector.clickTrainAlgorithms.adaptive.AdaptiveCTParams;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTKernel;
import clickTrainDetector.clickTrainAlgorithms.mht.TrackBitSet;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.BearingChi2VarParams.BearingJumpDrctn;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtMAT.SimpleClick;
import clickTrainDetector.clickTrainAlgorithms.mht.test.SimpleClickDataBlock;

/**
 * Synthetic tests for the adaptive predictive-residual click train algorithm.
 * <p>
 * Generates synthetic click trains (regular trains, interleaved trains, trains
 * with gaps, and bearing-separated trains) and runs them through an
 * {@link MHTKernel} wired with an {@link AdaptiveCTChi2Provider}. The detected
 * trains are checked against the known ground truth. Run as a plain main()
 * program (matching the existing test classes in the module); a non-zero exit
 * code indicates a failure.
 *
 * @author Jamie Macaulay
 */
public class TestAdaptiveCTChi2 {

	private static final float SR = 500000;

	private static int failures = 0;

	public static void main(String[] args) {

		testSingleRegularTrain();
		testAcceleratingBuzz();
		testTwoInterleavedTrains();
		testThreeInterleavedTrains();
		testCoastingGap();
		testBearingSeparatedTrains();
		testBearingRapidSweep();
		testBearingJumpCutoff();
		testBearingRotatingTrains();
		testNoisyBearingTrain();
		testCorrelationSeparatedTrains();
		testPeakFreqSeparatedTrains();
		testRejectTooSlow();

		System.out.println("\n==================================");
		if (failures == 0) {
			System.out.println("ALL ADAPTIVE CLICK TRAIN TESTS PASSED");
		} else {
			System.out.println(failures + " ADAPTIVE CLICK TRAIN TEST(S) FAILED");
		}
		System.out.println("==================================");
		System.exit(failures == 0 ? 0 : 1);
	}

	/**
	 * A single regular train with slowly drifting amplitude should be recovered as
	 * one train containing almost all of its clicks. Single channel, no bearing.
	 */
	private static void testSingleRegularTrain() {
		List<SimpleClick> clicks = new ArrayList<>();
		generateTrain(clicks, 0, 1.0, 0.1, 0.003, 120, -0.15, 1.0, null, 30, new Random(1));

		List<Integer> trains = runKernel(clicks, defaultParams(), false, false);

		assertTrue("Single regular train: exactly one train (got " + trains.size() + ")", trains.size() == 1);
		if (trains.size() == 1) {
			assertTrue("Single regular train: recovers most clicks (got " + trains.get(0) + "/30)",
					trains.get(0) >= 25);
		}
	}

	/**
	 * A single train whose ICI shrinks click by click (a terminal buzz) should stay
	 * one train. This is the case the trend-aware ICI predictor is for: a random-walk
	 * ICI model charges a residual at every click of a steadily accelerating train,
	 * whereas the linear-trend predictor sees the acceleration as consistent. Tracked
	 * on ICI alone (amplitude/bearing disabled) so the ICI model is what is under
	 * test.
	 */
	private static void testAcceleratingBuzz() {
		List<SimpleClick> clicks = new ArrayList<>();
		// ICI decreasing linearly from 0.18s down to ~0.03s over 30 clicks.
		generateBuzz(clicks, 0, 1.0, 0.18, 0.03, 0.002, 118, 30, new Random(40));
		AdaptiveCTParams params = defaultParams();
		params.useAmplitude = false;
		params.useBearing = false;
		List<Integer> trains = runKernel(clicks, params, false, false);
		assertTrue("Accelerating buzz stays one train (got " + trains.size() + " sizes " + trains + ")",
				trains.size() == 1 && trains.get(0) >= 25);
	}

	/**
	 * Two interleaved trains with different ICIs and amplitudes should be separated
	 * into two trains. Single channel, no bearing.
	 */
	private static void testTwoInterleavedTrains() {
		List<SimpleClick> clicks = new ArrayList<>();
		generateTrain(clicks, 0, 1.00, 0.10, 0.003, 122, -0.10, 1.0, null, 25, new Random(2));
		generateTrain(clicks, 100, 1.04, 0.13, 0.003, 108, 0.10, 1.0, null, 25, new Random(3));
		sortByTime(clicks);

		List<Integer> trains = runKernel(clicks, defaultParams(), false, false);

		assertTrue("Two interleaved trains: two trains detected (got " + trains.size() + ")", trains.size() == 2);
		assertTrue("Two interleaved trains: both substantial (sizes " + trains + ")",
				trains.size() == 2 && min(trains) >= 15);
	}

	/**
	 * Three interleaved trains with distinct ICIs and amplitudes (the classic hard
	 * case) should be separated into three trains. Single channel, no bearing.
	 */
	private static void testThreeInterleavedTrains() {
		List<SimpleClick> clicks = new ArrayList<>();
		generateTrain(clicks, 0, 1.00, 0.09, 0.003, 125, -0.08, 1.0, null, 25, new Random(10));
		generateTrain(clicks, 100, 1.03, 0.12, 0.003, 112, 0.05, 1.0, null, 25, new Random(11));
		generateTrain(clicks, 200, 1.06, 0.16, 0.003, 100, 0.08, 1.0, null, 25, new Random(12));
		sortByTime(clicks);

		List<Integer> trains = runKernel(clicks, defaultParams(), false, false);

		assertTrue("Three interleaved trains: three trains detected (got " + trains.size() + " sizes " + trains + ")",
				trains.size() == 3);
		assertTrue("Three interleaved trains: each substantial (sizes " + trains + ")",
				trains.size() == 3 && min(trains) >= 12);
	}

	/**
	 * A single train with a short gap (a few missed clicks) should be bridged by
	 * coasting into one train rather than fragmenting.
	 */
	private static void testCoastingGap() {
		List<SimpleClick> clicks = new ArrayList<>();
		generateTrain(clicks, 0, 1.0, 0.1, 0.003, 120, -0.1, 1.0, null, 15, new Random(4));
		// gap of 2 missed clicks (3 ICIs, within maxCoast), then continue the train.
		generateTrain(clicks, 50, 1.0 + 15 * 0.1 + 2 * 0.1, 0.1, 0.003, 118.5, -0.1, 1.0, null, 15, new Random(5));
		sortByTime(clicks);

		List<Integer> trains = runKernel(clicks, defaultParams(), false, false);

		assertTrue("Coasting gap: bridged into a single train (got " + trains.size() + " trains " + trains + ")",
				trains.size() == 1);
	}

	/**
	 * Two trains with the same ICI and amplitude but well-separated bearings should
	 * be separated when bearing information is available.
	 */
	private static void testBearingSeparatedTrains() {
		List<SimpleClick> clicks = new ArrayList<>();
		generateTrain(clicks, 0, 1.00, 0.10, 0.003, 118, 0.0, 1.0, 30.0, 25, new Random(6));
		generateTrain(clicks, 100, 1.05, 0.10, 0.003, 118, 0.0, 1.0, 120.0, 25, new Random(7));
		sortByTime(clicks);

		List<Integer> trains = runKernel(clicks, defaultParams(), true, false);

		assertTrue("Bearing-separated trains: two trains detected with bearing (got " + trains.size() + ")",
				trains.size() == 2);
	}

	/**
	 * A single train whose bearing sweeps rapidly but smoothly (as for a close,
	 * fast-moving animal) should stay one train - the second-difference bearing
	 * model must not penalise a fast but consistent bearing change.
	 */
	private static void testBearingRapidSweep() {
		List<SimpleClick> clicks = new ArrayList<>();
		generateTrainBearing(clicks, 0, 1.0, 0.1, 0.003, 118, 20.0, 8.0, 1.0, 30, new Random(30));
		List<Integer> trains = runKernel(clicks, defaultParams(), true, false);
		assertTrue("Rapid smooth bearing sweep stays one train (got " + trains.size() + " sizes " + trains + ")",
				trains.size() == 1 && trains.get(0) >= 25);
	}

	/**
	 * A single train that steps sharply in bearing part-way through. With a
	 * permissive bearing floor the step alone does not fragment the train, which
	 * isolates the hard maximum-bearing-jump cutoff: enabling it in the POSITIVE
	 * direction marks the train junk at the +50&deg; step and splits it in two, while
	 * the NEGATIVE direction ignores a positive jump and keeps the train whole
	 * (proving the direction control works).
	 */
	private static void testBearingJumpCutoff() {
		// a train that steps in bearing part-way through by an amount the adaptive
		// scorer's default bearing tolerance still accepts as one train - so any split
		// is driven by the maximum-bearing-jump cutoff, not the self-calibrated scale.
		double stepDeg = 15.0;
		List<SimpleClick> clicks = new ArrayList<>();
		generateTrain(clicks, 0, 1.0, 0.1, 0.003, 118, 0.0, 1.0, 30.0, 15, new Random(60));
		generateTrain(clicks, 100, 1.0 + 15 * 0.1, 0.1, 0.003, 118, 0.0, 1.0, 30.0 + stepDeg, 15, new Random(61));
		sortByTime(clicks);

		// no cutoff -> the modest step is tolerated, one train.
		List<Integer> noneTrains = runKernel(clicks, defaultParams(), true, false);
		assertTrue("Adaptive bearing jump cutoff off: one train through the step (got " + noneTrains.size() + " sizes "
				+ noneTrains + ")", noneTrains.size() == 1 && noneTrains.get(0) >= 28);

		// POSITIVE cutoff below the step -> the +step is rejected, splitting the train.
		AdaptiveCTParams pos = defaultParams();
		pos.bearingJumpEnable = true;
		pos.maxBearingJumpDeg = 8.0;
		pos.bearingJumpDrctn = BearingJumpDrctn.POSITIVE;
		List<Integer> posTrains = runKernel(clicks, pos, true, false);
		assertTrue("Adaptive bearing jump cutoff (POSITIVE 8 deg) splits at the +" + (int) stepDeg + " deg step (got "
				+ posTrains.size() + " sizes " + posTrains + ")", posTrains.size() == 2);

		// NEGATIVE direction does not police a positive jump -> the train stays whole.
		AdaptiveCTParams neg = defaultParams();
		neg.bearingJumpEnable = true;
		neg.maxBearingJumpDeg = 8.0;
		neg.bearingJumpDrctn = BearingJumpDrctn.NEGATIVE;
		List<Integer> negTrains = runKernel(clicks, neg, true, false);
		assertTrue("Adaptive bearing jump cutoff (NEGATIVE) ignores a positive jump: one train (got " + negTrains.size()
				+ " sizes " + negTrains + ")", negTrains.size() == 1 && negTrains.get(0) >= 28);
	}

	/**
	 * Two interleaved trains that are both rotating in bearing but well separated
	 * should not hop between each other.
	 */
	private static void testBearingRotatingTrains() {
		List<SimpleClick> clicks = new ArrayList<>();
		generateTrainBearing(clicks, 0, 1.00, 0.10, 0.003, 118, 30.0, 2.0, 1.0, 25, new Random(31));
		generateTrainBearing(clicks, 100, 1.05, 0.10, 0.003, 118, 120.0, 2.0, 1.0, 25, new Random(32));
		sortByTime(clicks);
		List<Integer> trains = runKernel(clicks, defaultParams(), true, false);
		assertTrue("Rotating well-separated bearing trains stay separate (got " + trains.size() + " sizes " + trains
				+ ")", trains.size() == 2);
	}

	/**
	 * A single train with consistently noisy bearings (e.g. poor bearing
	 * measurements on a narrowband species). Bearing is a strong separation cue, so
	 * with it enabled garbage bearings will fragment the train - the correct
	 * workflow for such data is to disable the bearing feature, after which the
	 * train forms cleanly on ICI and amplitude. This test verifies that fallback.
	 */
	private static void testNoisyBearingTrain() {
		List<SimpleClick> clicks = new ArrayList<>();
		// bearing is essentially random (+/- ~60 degrees) but ICI and amplitude are clean.
		generateTrainBearing(clicks, 0, 1.0, 0.1, 0.003, 118, 90.0, 0.0, 60.0, 30, new Random(33));
		AdaptiveCTParams params = defaultParams();
		params.useBearing = false; // garbage bearings -> disable the bearing feature
		List<Integer> trains = runKernel(clicks, params, true, false);
		assertTrue("Noisy-bearing train forms with bearing disabled (got " + trains.size() + " sizes " + trains + ")",
				trains.size() == 1 && trains.get(0) >= 25);
	}

	/**
	 * Two interleaved trains with identical ICI and amplitude but different
	 * waveforms should be separated when the waveform correlation feature is
	 * enabled.
	 */
	private static void testCorrelationSeparatedTrains() {
		double[] waveformA = clickWaveform(40000, 64); // 40 kHz click
		double[] waveformB = clickWaveform(90000, 64); // 90 kHz click

		List<SimpleClick> clicks = new ArrayList<>();
		generateTrainWaveform(clicks, 0, 1.00, 0.10, 0.003, 118, waveformA, 25, new Random(20));
		generateTrainWaveform(clicks, 100, 1.05, 0.10, 0.003, 118, waveformB, 25, new Random(21));
		sortByTime(clicks);

		AdaptiveCTParams params = defaultParams();
		params.useCorrelation = true;

		List<Integer> trains = runKernel(clicks, params, false, true);

		assertTrue("Correlation-separated trains: two trains detected with waveform correlation (got " + trains.size()
				+ " sizes " + trains + ")", trains.size() == 2);
	}

	/**
	 * Two interleaved trains with identical ICI and amplitude but clicks of
	 * different peak frequency should be separated when the peak-frequency feature is
	 * enabled (with waveform correlation off, so only peak frequency distinguishes
	 * them).
	 */
	private static void testPeakFreqSeparatedTrains() {
		double[] waveformA = clickWaveform(40000, 64); // 40 kHz click
		double[] waveformB = clickWaveform(120000, 64); // 120 kHz click

		List<SimpleClick> clicks = new ArrayList<>();
		generateTrainWaveform(clicks, 0, 1.00, 0.10, 0.003, 118, waveformA, 25, new Random(30));
		generateTrainWaveform(clicks, 100, 1.05, 0.10, 0.003, 118, waveformB, 25, new Random(31));
		sortByTime(clicks);

		AdaptiveCTParams params = defaultParams();
		params.useCorrelation = false;
		params.usePeakFreq = true;

		List<Integer> trains = runKernel(clicks, params, false, true);

		assertTrue("Peak-frequency-separated trains: two trains detected with peak frequency (got " + trains.size()
				+ " sizes " + trains + ")", trains.size() == 2);
	}

	/**
	 * A train whose ICI exceeds the maximum allowed ICI should be rejected.
	 */
	private static void testRejectTooSlow() {
		List<SimpleClick> clicks = new ArrayList<>();
		// ICI of 0.6s, above the default maxICI of 0.4s.
		generateTrain(clicks, 0, 1.0, 0.6, 0.005, 120, 0.0, 1.0, null, 20, new Random(8));

		List<Integer> trains = runKernel(clicks, defaultParams(), false, false);

		assertTrue("Too-slow train rejected (got " + trains.size() + " trains " + trains + ")", trains.isEmpty());
	}

	/* ----------------------------------------------------------------- */

	private static AdaptiveCTParams defaultParams() {
		AdaptiveCTParams params = new AdaptiveCTParams();
		params.maxICI = 0.4;
		params.sensitivity = 0.5;
		params.detectionProb = 0.9;
		params.useCorrelation = false;
		params.mhtKernel.nPruneback = 4;
		params.mhtKernel.nPruneBackStart = 5;
		params.mhtKernel.maxCoast = 4;
		params.mhtKernel.nHold = 50;
		return params;
	}

	/**
	 * Run a set of clicks through a kernel wired with the adaptive scorer and return
	 * the sizes of the detected trains (3 or more clicks, non-junk).
	 */
	private static List<Integer> runKernel(List<SimpleClick> clicks, AdaptiveCTParams params, boolean bearing,
			boolean waveform) {
		SimpleClickDataBlock block = new SimpleClickDataBlock();
		for (SimpleClick click : clicks) {
			block.addPamData(click);
		}

		AdaptiveCTChi2Provider provider = new AdaptiveCTChi2Provider(params, bearing, waveform);
		MHTKernel<PamDataUnit> kernel = new MHTKernel<>(provider);
		kernel.setMHTParams(params.mhtKernel);

		ListIterator<SimpleClick> it = block.getListIterator(0);
		while (it.hasNext()) {
			kernel.addDetection(it.next());
		}
		kernel.confirmRemainingTracks();

		List<Integer> trainSizes = new ArrayList<>();
		ArrayList<PamDataUnit> dataUnits = kernel.getDataUnits();
		int kcount = kernel.getKCount();
		int nTracks = kernel.getNConfrimedTracks();
		for (int i = 0; i < nTracks; i++) {
			@SuppressWarnings("rawtypes")
			TrackBitSet tbs = kernel.getConfirmedTrack(i);
			if (tbs == null || tbs.flag == TrackBitSet.JUNK_TRACK) {
				continue;
			}
			int size = 0;
			for (int k = 0; k < kcount; k++) {
				if (tbs.trackBitSet.get(k)) {
					size++;
				}
			}
			if (size >= 3) {
				trainSizes.add(size);
			}
		}
		return trainSizes;
	}

	/**
	 * Generate a synthetic click train and append it to the list.
	 *
	 * @param clicks    - list to append to.
	 * @param uidStart  - starting UID for clicks in this train.
	 * @param t0        - start time in seconds.
	 * @param ici       - mean inter-click interval in seconds.
	 * @param iciJitter - standard deviation of the ICI jitter in seconds.
	 * @param amp       - starting amplitude in dB.
	 * @param ampDrift  - amplitude change per click in dB.
	 * @param ampJitter - amplitude jitter standard deviation in dB.
	 * @param bearing   - bearing in degrees, or null for no bearing.
	 * @param n         - number of clicks.
	 * @param random    - random source.
	 */
	private static void generateTrain(List<SimpleClick> clicks, int uidStart, double t0, double ici, double iciJitter,
			double amp, double ampDrift, double ampJitter, Double bearing, int n, Random random) {
		double t = t0;
		for (int i = 0; i < n; i++) {
			double a = amp + i * ampDrift + random.nextGaussian() * ampJitter;
			double bear = bearing == null ? 0 : bearing + random.nextGaussian() * 1.5;
			if (bearing == null) {
				clicks.add(new SimpleClick(uidStart + i, t, a, SR));
			} else {
				clicks.add(new SimpleClick(uidStart + i, Double.valueOf(t), Double.valueOf(a), Double.valueOf(bear), SR));
			}
			t += ici + random.nextGaussian() * iciJitter;
		}
	}

	/**
	 * Generate a synthetic click train whose clicks all share the same waveform
	 * shape (with a little per-click amplitude noise on the waveform), and append it
	 * to the list. Used to test the waveform-correlation feature.
	 */
	private static void generateTrainWaveform(List<SimpleClick> clicks, int uidStart, double t0, double ici,
			double iciJitter, double amp, double[] waveform, int n, Random random) {
		double t = t0;
		for (int i = 0; i < n; i++) {
			double a = amp + random.nextGaussian();
			double[] wf = new double[waveform.length];
			for (int j = 0; j < waveform.length; j++) {
				wf[j] = waveform[j] * (1 + 0.02 * random.nextGaussian());
			}
			clicks.add(new SimpleClick(uidStart + i, Double.valueOf(t), Double.valueOf(a), null, wf, SR));
			t += ici + random.nextGaussian() * iciJitter;
		}
	}

	/**
	 * Generate a "buzz" train whose ICI decreases linearly from {@code ici0} to
	 * {@code ici1} over {@code n} clicks (with jitter), and append it to the list.
	 * Single channel, no bearing.
	 *
	 * @param clicks    - list to append to.
	 * @param uidStart  - starting UID for clicks in this train.
	 * @param t0        - start time in seconds.
	 * @param ici0      - initial inter-click interval in seconds.
	 * @param ici1      - final inter-click interval in seconds.
	 * @param iciJitter - standard deviation of the ICI jitter in seconds.
	 * @param amp       - amplitude in dB.
	 * @param n         - number of clicks.
	 * @param random    - random source.
	 */
	private static void generateBuzz(List<SimpleClick> clicks, int uidStart, double t0, double ici0, double ici1,
			double iciJitter, double amp, int n, Random random) {
		double t = t0;
		for (int i = 0; i < n; i++) {
			double a = amp + random.nextGaussian();
			clicks.add(new SimpleClick(uidStart + i, t, a, SR));
			double frac = n > 1 ? (double) i / (n - 1) : 0;
			double ici = ici0 + (ici1 - ici0) * frac;
			t += ici + random.nextGaussian() * iciJitter;
		}
	}

	/**
	 * Build a simple synthetic click waveform: a damped sinusoid at the given
	 * frequency.
	 */
	private static double[] clickWaveform(double frequency, int nSamples) {
		double[] wf = new double[nSamples];
		for (int i = 0; i < nSamples; i++) {
			double env = Math.exp(-3.0 * i / nSamples);
			wf[i] = env * Math.sin(2 * Math.PI * frequency * i / SR);
		}
		return wf;
	}

	/**
	 * Generate a train whose bearing changes linearly (a smooth sweep) with a given
	 * per-click rate and jitter, both in degrees.
	 */
	private static void generateTrainBearing(List<SimpleClick> clicks, int uidStart, double t0, double ici,
			double iciJitter, double amp, double bearingStartDeg, double bearingRateDeg, double bearingJitterDeg,
			int n, Random random) {
		double t = t0;
		for (int i = 0; i < n; i++) {
			double a = amp + random.nextGaussian();
			double bearing = bearingStartDeg + i * bearingRateDeg + random.nextGaussian() * bearingJitterDeg;
			clicks.add(new SimpleClick(uidStart + i, Double.valueOf(t), Double.valueOf(a), Double.valueOf(bearing), SR));
			t += ici + random.nextGaussian() * iciJitter;
		}
	}

	private static void sortByTime(List<SimpleClick> clicks) {
		clicks.sort((a, b) -> Double.compare(a.timeSeconds, b.timeSeconds));
	}

	private static int min(List<Integer> values) {
		int m = Integer.MAX_VALUE;
		for (int v : values) {
			m = Math.min(m, v);
		}
		return m;
	}

	private static void assertTrue(String message, boolean condition) {
		if (condition) {
			System.out.println("[PASS] " + message);
		} else {
			System.out.println("[FAIL] " + message);
			failures++;
		}
	}

}
