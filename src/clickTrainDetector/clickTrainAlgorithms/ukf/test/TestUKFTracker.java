package clickTrainDetector.clickTrainAlgorithms.ukf.test;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import clickTrainDetector.clickTrainAlgorithms.mht.mhtMAT.SimpleClick;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.BearingChi2VarParams.BearingJumpDrctn;
import clickTrainDetector.clickTrainAlgorithms.ukf.AffinityNN;
import clickTrainDetector.clickTrainAlgorithms.ukf.ClickTrack;
import clickTrainDetector.clickTrainAlgorithms.ukf.HungarianAlgorithm;
import clickTrainDetector.clickTrainAlgorithms.ukf.UKFModel;
import clickTrainDetector.clickTrainAlgorithms.ukf.UKFParams;
import clickTrainDetector.clickTrainAlgorithms.ukf.UKFTracker;
import clickTrainDetector.clickTrainAlgorithms.ukf.UnscentedKalmanFilter;

/**
 * Tests for the UKF click train tracker and its components (Hungarian assignment
 * and the unscented Kalman filter). Plain main() program; non-zero exit on
 * failure.
 *
 * @author Jamie Macaulay
 */
public class TestUKFTracker {

	private static final float SR = 500000;

	private static int failures = 0;

	public static void main(String[] args) {

		testHungarian();
		testUKFConverges();
		testCustomAffinity();

		testSingleRegularTrain();
		testFastTrain();
		testTwoInterleavedTrains();
		testBearingSeparatedTrains();
		testBearingJumpCutoff();
		testCoastingGap();
		testTwoStageRecovery();
		testConfirmationRejectsClutter();
		testCrossingTrainsNScan();
		testRejectTooSlow();

		System.out.println("\n==================================");
		if (failures == 0) {
			System.out.println("ALL UKF TRACKER TESTS PASSED");
		} else {
			System.out.println(failures + " UKF TRACKER TEST(S) FAILED");
		}
		System.out.println("==================================");
		System.exit(failures == 0 ? 0 : 1);
	}

	/* ---------------- component tests ---------------- */

	/**
	 * Hungarian assignment on a matrix with a known optimal assignment.
	 */
	private static void testHungarian() {
		// optimal assignment is (0->1, 1->0, 2->2) with cost 1+2+3 = 6.
		double[][] cost = { { 9, 1, 9 }, { 2, 9, 9 }, { 9, 9, 3 } };
		int[] a = HungarianAlgorithm.solve(cost);
		boolean ok = a[0] == 1 && a[1] == 0 && a[2] == 2;
		assertTrue("Hungarian finds the optimal assignment (got [" + a[0] + "," + a[1] + "," + a[2] + "])", ok);
	}

	/**
	 * A UKF tracking a constant 1-D signal should converge to it from noisy
	 * measurements.
	 */
	private static void testUKFConverges() {
		UKFModel model = new UKFModel() {
			public int stateDim() {
				return 1;
			}

			public int measDim() {
				return 1;
			}

			public double[] f(double[] x) {
				return x.clone();
			}

			public double[] h(double[] x) {
				return x.clone();
			}

			public double[][] processNoise() {
				return new double[][] { { 1e-4 } };
			}

			public double[][] measurementNoise() {
				return new double[][] { { 1.0 } };
			}

			public double[] stateResidual(double[] a, double[] b) {
				return new double[] { a[0] - b[0] };
			}

			public double[] measResidual(double[] a, double[] b) {
				return new double[] { a[0] - b[0] };
			}

			public double[] stateMean(double[][] s, double[] w) {
				double m = 0;
				for (int i = 0; i < s.length; i++) {
					m += w[i] * s[i][0];
				}
				return new double[] { m };
			}

			public double[] measMean(double[][] s, double[] w) {
				return stateMean(s, w);
			}
		};

		UnscentedKalmanFilter ukf = new UnscentedKalmanFilter(model, new double[] { 0 }, new double[][] { { 10 } });
		Random r = new Random(0);
		double truth = 5.0;
		for (int i = 0; i < 50; i++) {
			ukf.predict();
			ukf.update(new double[] { truth + r.nextGaussian() * 1.0 });
		}
		double est = ukf.getState()[0];
		assertTrue("UKF converges to the constant (estimate " + String.format("%.2f", est) + ")",
				Math.abs(est - truth) < 0.5);
	}

	/**
	 * A custom affinity network loaded from a JSON file should (a) reproduce the
	 * default network when given the default weights, (b) reject a wrong-shaped
	 * file, and (c) drive the tracker when configured in the params.
	 */
	private static void testCustomAffinity() {
		try {
			// default-gate weights written to a file (built for the current FEATURE_DIM so
			// the test tracks changes to the feature-vector length).
			File good = File.createTempFile("affinity", ".json");
			good.deleteOnExit();
			AffinityNN.defaultGate(UKFTracker.FEATURE_DIM).writeJson(good);

			AffinityNN custom = AffinityNN.fromFile(good, UKFTracker.FEATURE_DIM);
			AffinityNN deflt = AffinityNN.defaultGate(UKFTracker.FEATURE_DIM);
			double[] near = new double[UKFTracker.FEATURE_DIM];
			double[] far = new double[UKFTracker.FEATURE_DIM];
			far[0] = 12;
			boolean matches = Math.abs(custom.affinity(near) - deflt.affinity(near)) < 1e-9
					&& Math.abs(custom.affinity(far) - deflt.affinity(far)) < 1e-9;
			assertTrue("Custom affinity loaded from file reproduces the default network", matches);

			// wrong input dimension should be rejected.
			File bad = File.createTempFile("affinitybad", ".json");
			bad.deleteOnExit();
			try (FileWriter w = new FileWriter(bad)) {
				w.write("{ \"weights\": [ [[-0.5, 0, 0]], [[4.0]] ], \"biases\": [ [3.0], [0.0] ] }");
			}
			boolean threw = false;
			try {
				AffinityNN.fromFile(bad, UKFTracker.FEATURE_DIM);
			} catch (Exception e) {
				threw = true;
			}
			assertTrue("Wrong-shaped affinity network file is rejected", threw);

			// the tracker uses the custom network end to end.
			List<SimpleClick> clicks = new ArrayList<>();
			generateTrain(clicks, 0, 1.0, 0.1, 0.003, 120, -0.15, 1.0, null, 30, new Random(1));
			UKFParams params = defaultParams();
			params.useCustomAffinity = true;
			params.affinityNetworkFile = good.getAbsolutePath();
			List<Integer> trains = runTracker(clicks, params, false);
			assertTrue("Tracker runs with a custom affinity network (got " + trains.size() + " sizes " + trains + ")",
					trains.size() == 1 && trains.get(0) >= 25);
		} catch (Exception e) {
			assertTrue("Custom affinity test threw: " + e, false);
		}
	}

	/* ---------------- tracker scenarios ---------------- */

	private static void testSingleRegularTrain() {
		List<SimpleClick> clicks = new ArrayList<>();
		generateTrain(clicks, 0, 1.0, 0.1, 0.003, 120, -0.15, 1.0, null, 30, new Random(1));
		List<Integer> trains = runTracker(clicks, defaultParams(), false);
		assertTrue("Single regular train: one train (got " + trains.size() + " sizes " + trains + ")",
				trains.size() == 1);
		if (trains.size() == 1) {
			assertTrue("Single regular train: recovers most clicks (" + trains.get(0) + "/30)", trains.get(0) >= 25);
		}
	}

	/**
	 * A fast train whose ICI (0.03s) is below the old default frame window (0.05s)
	 * should still be recovered as one train. With a frame longer than the ICI, two
	 * clicks of the train land in the same frame and one is lost to a spurious new
	 * track each time; a frame shorter than the ICI (0.02s) keeps the train intact.
	 */
	private static void testFastTrain() {
		List<SimpleClick> clicks = new ArrayList<>();
		generateTrain(clicks, 0, 1.0, 0.03, 0.001, 120, -0.05, 1.0, null, 30, new Random(15));
		UKFParams params = defaultParams();
		params.frameDuration = 0.02;
		List<Integer> trains = runTracker(clicks, params, false);
		assertTrue("Fast train recovered as one train (got " + trains.size() + " sizes " + trains + ")",
				trains.size() == 1 && trains.get(0) >= 25);
	}

	private static void testTwoInterleavedTrains() {
		List<SimpleClick> clicks = new ArrayList<>();
		generateTrain(clicks, 0, 1.00, 0.10, 0.003, 122, -0.10, 1.0, null, 25, new Random(2));
		generateTrain(clicks, 100, 1.04, 0.13, 0.003, 108, 0.10, 1.0, null, 25, new Random(3));
		sortByTime(clicks);
		List<Integer> trains = runTracker(clicks, defaultParams(), false);
		assertTrue("Two interleaved trains: two trains (got " + trains.size() + " sizes " + trains + ")",
				trains.size() == 2);
	}

	private static void testBearingSeparatedTrains() {
		List<SimpleClick> clicks = new ArrayList<>();
		generateTrain(clicks, 0, 1.00, 0.10, 0.003, 118, 0.0, 1.0, 30.0, 25, new Random(6));
		generateTrain(clicks, 100, 1.05, 0.10, 0.003, 118, 0.0, 1.0, 120.0, 25, new Random(7));
		sortByTime(clicks);
		List<Integer> trains = runTracker(clicks, defaultParams(), true);
		assertTrue("Bearing-separated trains: two trains (got " + trains.size() + " sizes " + trains + ")",
				trains.size() == 2);
	}

	/**
	 * A train that steps sharply in bearing part-way through. With a permissive
	 * bearing gate (large floor) the step alone does not break the train, which
	 * isolates the hard maximum-bearing-jump cutoff: enabling it in the POSITIVE
	 * direction rejects the +40&deg; step and splits the train, while the NEGATIVE
	 * direction ignores a positive jump and keeps the train whole (proving the
	 * direction control works). The cutoff is exercised in both the greedy and the
	 * N-scan association paths.
	 */
	private static void testBearingJumpCutoff() {
		// a train that steps in bearing part-way through by an amount the tracker's
		// default bearing tolerance still accepts as one train - so any split is driven
		// by the maximum-bearing-jump cutoff, not the underlying association gate.
		double stepDeg = 12.0;
		List<SimpleClick> clicks = new ArrayList<>();
		double t = 1.0;
		double ici = 0.1;
		int uid = 0;
		for (int i = 0; i < 15; i++) {
			clicks.add(new SimpleClick(uid++, Double.valueOf(t), Double.valueOf(118.0), Double.valueOf(30.0), SR));
			t += ici;
		}
		for (int i = 0; i < 15; i++) {
			clicks.add(new SimpleClick(uid++, Double.valueOf(t), Double.valueOf(118.0), Double.valueOf(30.0 + stepDeg),
					SR));
			t += ici;
		}

		// no cutoff -> the modest step is tolerated, one train.
		List<Integer> noneTrains = runTracker(clicks, defaultParams(), true);
		assertTrue("Bearing jump cutoff off: one train through the step (got " + noneTrains.size() + " sizes "
				+ noneTrains + ")", noneTrains.size() == 1 && noneTrains.get(0) >= 28);

		// POSITIVE cutoff below the step -> the +step is rejected, splitting the train.
		UKFParams pos = defaultParams();
		pos.bearingJumpEnable = true;
		pos.maxBearingJumpDeg = 6.0;
		pos.bearingJumpDrctn = BearingJumpDrctn.POSITIVE;
		List<Integer> posTrains = runTracker(clicks, pos, true);
		assertTrue("Bearing jump cutoff (POSITIVE 6 deg) splits the train at the +" + (int) stepDeg + " deg step (got "
				+ posTrains.size() + " sizes " + posTrains + ")", posTrains.size() == 2);

		// same cutoff in the N-scan association path.
		UKFParams posN = defaultParams();
		posN.bearingJumpEnable = true;
		posN.maxBearingJumpDeg = 6.0;
		posN.bearingJumpDrctn = BearingJumpDrctn.POSITIVE;
		posN.useMultiHypothesis = true;
		posN.nScanDepth = 6;
		posN.beamWidth = 4;
		posN.nScanKBest = 4;
		List<Integer> posNTrains = runTracker(clicks, posN, true);
		assertTrue("Bearing jump cutoff applies in N-scan mode too (got " + posNTrains.size() + " sizes " + posNTrains
				+ ")", posNTrains.size() >= 2);

		// NEGATIVE direction does not police a positive jump -> the train stays whole.
		UKFParams neg = defaultParams();
		neg.bearingJumpEnable = true;
		neg.maxBearingJumpDeg = 6.0;
		neg.bearingJumpDrctn = BearingJumpDrctn.NEGATIVE;
		List<Integer> negTrains = runTracker(clicks, neg, true);
		assertTrue("Bearing jump cutoff (NEGATIVE) ignores a positive jump: one train (got " + negTrains.size()
				+ " sizes " + negTrains + ")", negTrains.size() == 1 && negTrains.get(0) >= 28);
	}

	private static void testCoastingGap() {
		List<SimpleClick> clicks = new ArrayList<>();
		generateTrain(clicks, 0, 1.0, 0.1, 0.003, 120, -0.1, 1.0, null, 15, new Random(4));
		// gap of 2 missed clicks then continue.
		generateTrain(clicks, 50, 1.0 + 15 * 0.1 + 2 * 0.1, 0.1, 0.003, 118.5, -0.1, 1.0, null, 15, new Random(5));
		sortByTime(clicks);
		List<Integer> trains = runTracker(clicks, defaultParams(), false);
		assertTrue("Coasting gap: one bridged train (got " + trains.size() + " sizes " + trains + ")",
				trains.size() == 1);
	}

	private static void testTwoStageRecovery() {
		// a single train where every other click is quiet (below the confidence
		// threshold); the two-stage association should recover the quiet clicks.
		List<SimpleClick> clicks = new ArrayList<>();
		Random r = new Random(9);
		double t = 1.0;
		for (int i = 0; i < 24; i++) {
			double amp = (i % 2 == 0) ? 120 : 90; // loud / quiet alternating
			clicks.add(new SimpleClick(i, t, amp, SR));
			t += 0.1 + r.nextGaussian() * 0.003;
		}
		UKFParams params = defaultParams();
		params.confidenceAmplitude = 100; // quiet (90) clicks are low-confidence
		// track on ICI only: amplitude is the confidence signal here, so using it as a
		// tracking gate would itself reject the quiet clicks the second stage recovers.
		params.useAmplitude = false;
		List<Integer> trains = runTracker(clicks, params, false);
		assertTrue("Two-stage recovery: one train recovering quiet clicks (got " + trains.size() + " sizes " + trains
				+ ")", trains.size() == 1 && trains.get(0) >= 20);
	}

	/**
	 * A clean train sprinkled with isolated high-amplitude clutter clicks should keep
	 * its clicks: because confirmed tracks are associated before tentative ones, the
	 * established train wins every contested click and stays intact, rather than
	 * having clicks stolen by the short tentative tracks the clutter spawns. (Clutter
	 * clicks that happen to fall a train-like interval apart may still form their own
	 * short track - that is a false-positive/minimum-length question, not a
	 * fragmentation of the real train, so we assert only that the real train survives
	 * essentially whole.)
	 */
	private static void testConfirmationRejectsClutter() {
		List<SimpleClick> clicks = new ArrayList<>();
		generateTrain(clicks, 0, 1.0, 0.1, 0.003, 118, 0.0, 1.0, null, 30, new Random(16));
		// scatter 6 loud clutter clicks across the same time span.
		Random r = new Random(17);
		double span = 0.1 * 30;
		for (int i = 0; i < 6; i++) {
			double t = 1.0 + r.nextDouble() * span;
			clicks.add(new SimpleClick(1000 + i, t, 119 + r.nextGaussian(), SR));
		}
		sortByTime(clicks);
		List<Integer> trains = runTracker(clicks, defaultParams(), false);
		int big = 0;
		int largest = 0;
		for (int s : trains) {
			if (s >= 25) {
				big++;
			}
			largest = Math.max(largest, s);
		}
		assertTrue("Established train survives clutter intact (got " + trains.size() + " sizes " + trains + ")",
				big == 1 && largest >= 28);
	}

	/**
	 * Two trains that cross in bearing (ambiguous at the crossing) with distinct ICIs.
	 * The single-frame greedy tracker swaps the two trains' identities at the crossing
	 * (mixing their clicks); the N-scan multi-hypothesis mode defers the decision and
	 * lets later ICI/bearing evidence pick the globally consistent assignment, making
	 * far fewer association errors. We measure errors as UID "impurity" (clicks in a
	 * saved train that do not belong to its majority true source) and assert N-scan
	 * makes strictly fewer than the greedy tracker while still recovering the trains.
	 * N-scan tends to break a track at the ambiguous crossing rather than merge across
	 * it, so it may return more (but purer) trains - hence we do not require exactly
	 * two.
	 */
	private static void testCrossingTrainsNScan() {
		List<SimpleClick> clicks = new ArrayList<>();
		// Two trains cleanly separated in bearing at the ends (30 vs 150 deg) that sweep
		// through each other, coinciding at ~90 deg mid-run. Their ICIs stay distinct
		// (0.09 vs 0.13 s) throughout, so the correct association is recoverable through
		// the bearing crossing - but a greedy tracker can swap identities at the moment
		// the bearings coincide.
		generateTrainBearingSweep(clicks, 0, 1.00, 0.090, 0.002, 118, 30.0, 5.0, 1.0, 25, new Random(50));
		generateTrainBearingSweep(clicks, 1000, 1.00, 0.130, 0.002, 118, 150.0, -5.0, 1.0, 25, new Random(51));
		sortByTime(clicks);

		UKFParams gnn = defaultParams();
		int gnnImpurity = impurity(runTrackerTrains(clicks, gnn, true));

		UKFParams nscan = defaultParams();
		nscan.useMultiHypothesis = true;
		nscan.nScanDepth = 6;
		nscan.beamWidth = 4;
		nscan.nScanKBest = 4;
		List<List<Long>> nscanTrains = runTrackerTrains(clicks, nscan, true);
		int nscanImpurity = impurity(nscanTrains);

		System.out.println("    [info] crossing trains: GNN impurity=" + gnnImpurity + ", N-scan impurity="
				+ nscanImpurity + " (" + nscanTrains.size() + " N-scan trains)");
		assertTrue("N-scan makes fewer crossing association errors than greedy (GNN=" + gnnImpurity + ", N-scan="
				+ nscanImpurity + ")",
				nscanImpurity < gnnImpurity && nscanImpurity <= 6 && nscanTrains.size() >= 2);
	}

	private static void testRejectTooSlow() {
		List<SimpleClick> clicks = new ArrayList<>();
		generateTrain(clicks, 0, 1.0, 0.6, 0.005, 120, 0.0, 1.0, null, 20, new Random(8));
		List<Integer> trains = runTracker(clicks, defaultParams(), false);
		assertTrue("Too-slow train rejected (got " + trains.size() + " sizes " + trains + ")", trains.isEmpty());
	}

	/* ---------------- helpers ---------------- */

	private static UKFParams defaultParams() {
		UKFParams params = new UKFParams();
		params.maxICI = 0.4;
		params.frameDuration = 0.05;
		params.maxCoast = 4;
		params.minTrackLength = 3;
		params.useAmplitude = true;
		params.useBearing = true;
		params.confidenceAmplitude = 0.0; // 0 = two-stage split off
		// the scenario tests below (and the crossing test's baseline) exercise the greedy
		// single-frame path; the N-scan path is turned on explicitly where it is tested,
		// so pin this off regardless of the production default.
		params.nScanDepth = 0;
		return params;
	}

	/**
	 * Run clicks through the tracker and return the sizes of the finished trains
	 * (>= minTrackLength).
	 */
	private static List<Integer> runTracker(List<SimpleClick> clicks, UKFParams params, boolean bearingAvailable) {
		List<Integer> sizes = new ArrayList<>();
		UKFTracker tracker = new UKFTracker(params, bearingAvailable, (ClickTrack track) -> {
			if (track.size() >= params.minTrackLength) {
				sizes.add(track.size());
			}
		});
		for (SimpleClick click : clicks) {
			tracker.addClick(click);
		}
		tracker.finaliseAll();
		return sizes;
	}

	/**
	 * Run clicks through the tracker and return, per saved train (>= minTrackLength),
	 * the list of click UIDs. Used to score association purity against ground truth.
	 */
	private static List<List<Long>> runTrackerTrains(List<SimpleClick> clicks, UKFParams params,
			boolean bearingAvailable) {
		List<List<Long>> trains = new ArrayList<>();
		UKFTracker tracker = new UKFTracker(params, bearingAvailable, (ClickTrack track) -> {
			if (track.size() >= params.minTrackLength) {
				List<Long> uids = new ArrayList<>();
				for (Object click : track.getClicks()) {
					uids.add(((SimpleClick) click).getUID());
				}
				trains.add(uids);
			}
		});
		for (SimpleClick click : clicks) {
			tracker.addClick(click);
		}
		tracker.finaliseAll();
		return trains;
	}

	/**
	 * Total association impurity across all saved trains: for each train, the number
	 * of clicks not belonging to that train's majority true source (UID &lt; 1000 is
	 * train A, otherwise train B). Zero means every train is perfectly pure.
	 */
	private static int impurity(List<List<Long>> trains) {
		int total = 0;
		for (List<Long> train : trains) {
			int a = 0;
			int b = 0;
			for (long uid : train) {
				if (uid < 1000) {
					a++;
				} else {
					b++;
				}
			}
			total += Math.min(a, b);
		}
		return total;
	}

	/**
	 * Generate a train whose bearing changes linearly (a smooth sweep) at a given
	 * per-click rate (degrees), with bearing jitter, and append it to the list.
	 */
	private static void generateTrainBearingSweep(List<SimpleClick> clicks, int uidStart, double t0, double ici,
			double iciJitter, double amp, double bearingStartDeg, double bearingRateDeg, double bearingJitterDeg, int n,
			Random random) {
		double t = t0;
		for (int i = 0; i < n; i++) {
			double a = amp + random.nextGaussian();
			double bearing = bearingStartDeg + i * bearingRateDeg + random.nextGaussian() * bearingJitterDeg;
			clicks.add(new SimpleClick(uidStart + i, Double.valueOf(t), Double.valueOf(a), Double.valueOf(bearing), SR));
			t += ici + random.nextGaussian() * iciJitter;
		}
	}

	private static void generateTrain(List<SimpleClick> clicks, int uidStart, double t0, double ici, double iciJitter,
			double amp, double ampDrift, double ampJitter, Double bearing, int n, Random random) {
		double t = t0;
		for (int i = 0; i < n; i++) {
			double a = amp + i * ampDrift + random.nextGaussian() * ampJitter;
			if (bearing == null) {
				clicks.add(new SimpleClick(uidStart + i, t, a, SR));
			} else {
				double bear = bearing + random.nextGaussian() * 1.5;
				clicks.add(new SimpleClick(uidStart + i, Double.valueOf(t), Double.valueOf(a), Double.valueOf(bear), SR));
			}
			t += ici + random.nextGaussian() * iciJitter;
		}
	}

	private static void sortByTime(List<SimpleClick> clicks) {
		clicks.sort((a, b) -> Double.compare(a.timeSeconds, b.timeSeconds));
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
