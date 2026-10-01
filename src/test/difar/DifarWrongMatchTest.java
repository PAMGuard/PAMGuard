package test.difar;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import difar.DIFARTargetMotionInformation;
import difar.DifarLocalisationResiduals;
import difar.targetmotion.Simplex2D;
import difar.targetmotion.TargetMotionResult;

/**
 * Detections of two different animals, wrongly treated as one call.
 * <p>
 * This is the case the residual limits exist to catch. Two bearings always
 * cross somewhere, so a wrong match still produces a position, and it can look
 * reasonable on a map. The arrival times are what give it away: the crossing
 * point implies a delay between the buoys that the detections do not show.
 * <p>
 * Both animals call at the same moment here, which is the hardest case. A wrong
 * match between calls at different times is easier to reject, since the timing
 * error adds to the one from geometry.
 * <p>
 * Each test states the residual a geometry produces, rather than whether it
 * passes a particular limit. The residual belongs to the geometry and does not
 * change. Which of them a limit catches is a separate question, and one the
 * summary test below prints rather than asserts, since the limits are settings
 * an operator can change.
 */
public class DifarWrongMatchTest {

	private static final double[] BUOY_A = {0, 0};
	private static final double[] BUOY_B = {15000, 0};

	/** Whales at different ranges, in a similar direction from both buoys. */
	private static final double[][] DIFFERENT_RANGES = {{3000, 6000}, {9000, 18000}};

	/** One whale close to its own buoy, the other far from its own. */
	private static final double[][] UNEQUAL_RANGES = {{4000, 12000}, {13000, 5000}};

	/** One whale beyond the other, along a similar line. */
	private static final double[][] WHALE_BEYOND_WHALE = {{5000, 8000}, {11000, 20000}};

	/** Whales on opposite sides of the line between the buoys. */
	private static final double[][] OPPOSITE_SIDES = {{5000, 8000}, {10000, -8000}};

	/** Each whale the same distance from its own buoy. */
	private static final double[][] EQUAL_RANGES = {{5000, 8000}, {10000, 8000}};

	/** Whales in similar directions from both buoys. */
	private static final double[][] SIMILAR_DIRECTIONS = {{2000, 10000}, {12000, 9000}};

	/** The same whale on both buoys. */
	private static final double[][] CORRECT_MATCH = {{5000, 8000}, {5000, 8000}};

	@Test
	public void differentRangesGiveALargeTimingResidual() {
		assertTimingResidualAbove("different ranges", DIFFERENT_RANGES, 5.0);
	}

	@Test
	public void whaleBeyondWhaleGivesALargeTimingResidual() {
		assertTimingResidualAbove("whale beyond whale", WHALE_BEYOND_WHALE, 5.0);
	}

	@Test
	public void oppositeSidesGiveALargeResidual() {
		DifarLocalisationResiduals residuals = residualsForMatch(OPPOSITE_SIDES);
		print("opposite sides", residuals);
		assertTrue(residuals.getMaxTimeDelayErrorSeconds() > 5.0
				|| residuals.getMaxBearingErrorDegrees() > 20.0,
				"whales on opposite sides cannot both be explained by one position");
	}

	/**
	 * A wrong match that sits in between. Its timing residual is a few seconds,
	 * so a three second limit catches it and a six second limit does not. It is
	 * the case that decides what a limit costs.
	 */
	@Test
	public void unequalRangesGiveAModerateTimingResidual() {
		DifarLocalisationResiduals residuals = residualsForMatch(UNEQUAL_RANGES);
		print("unequal ranges to own buoy", residuals);
		double timing = residuals.getMaxTimeDelayErrorSeconds();
		assertTrue(timing > 3.0 && timing < 6.0,
				"expected a residual of a few seconds, got " + timing);
	}

	/**
	 * A wrong match that cannot be detected. Each whale is the same distance
	 * from its own buoy, so the measured delay is zero, and the bearings cross
	 * where the predicted delay is also zero. The measurements are exactly
	 * consistent with one call at the crossing point.
	 * <p>
	 * Nothing in the data separates this from a true detection. Rejecting it
	 * would need something else, such as the calls themselves not matching.
	 */
	@Test
	public void equalRangesCannotBeDetected() {
		DifarLocalisationResiduals residuals = residualsForMatch(EQUAL_RANGES);
		print("equal ranges", residuals);
		assertTrue(residuals.getMaxTimeDelayErrorSeconds() < 0.1,
				"this wrong match leaves no trace in the measurements");
	}

	/**
	 * Whales in similar directions from both buoys. The residual is about a
	 * second, which is smaller than the timing error of a clip marked by hand,
	 * so no usable limit would catch it.
	 */
	@Test
	public void similarDirectionsGiveASmallResidual() {
		DifarLocalisationResiduals residuals = residualsForMatch(SIMILAR_DIRECTIONS);
		print("similar directions", residuals);
		assertTrue(residuals.getMaxTimeDelayErrorSeconds() < 2.0,
				"recorded so that a change in this behaviour is noticed");
	}

	/**
	 * A correct match, for comparison. Without it the tests above would pass
	 * even if every fit were nonsense.
	 */
	@Test
	public void correctMatchHasNoResidual() {
		DifarLocalisationResiduals residuals = residualsForMatch(CORRECT_MATCH);
		print("correct match", residuals);
		assertTrue(residuals.getMaxTimeDelayErrorSeconds() < 0.1,
				"a correct match should agree with its own measurements");
		assertTrue(residuals.getMaxBearingErrorDegrees() < 0.1,
				"a correct match should agree with its own bearings");
	}

	/**
	 * Print which wrong matches each candidate limit would catch. This asserts
	 * nothing, since the limits are settings rather than facts, but it is the
	 * evidence for choosing one.
	 */
	@Test
	public void summariseWhatEachLimitCatches() {
		double[][][] cases = {DIFFERENT_RANGES, WHALE_BEYOND_WHALE, OPPOSITE_SIDES,
				UNEQUAL_RANGES, SIMILAR_DIRECTIONS, EQUAL_RANGES, CORRECT_MATCH};
		String[] names = {"different ranges", "whale beyond whale", "opposite sides",
				"unequal ranges", "similar directions", "equal ranges", "correct match"};
		System.out.printf("%-22s %8s %8s %8s %8s%n", "case", "bearing", "timing", "3 s", "6 s");
		for (int i = 0; i < cases.length; i++) {
			DifarLocalisationResiduals r = residualsForMatch(cases[i]);
			System.out.printf("%-22s %7.1f %8.2f %8s %8s%n", names[i],
					r.getMaxBearingErrorDegrees(), r.getMaxTimeDelayErrorSeconds(),
					r.isWithin(20, 3) ? "kept" : "rejected",
					r.isWithin(20, 6) ? "kept" : "rejected");
		}
	}

	/** Check a geometry produces at least the timing residual expected of it. */
	private void assertTimingResidualAbove(String label, double[][] whales, double seconds) {
		DifarLocalisationResiduals residuals = residualsForMatch(whales);
		print(label, residuals);
		assertTrue(residuals.getMaxTimeDelayErrorSeconds() > seconds,
				String.format("%s should give a timing residual above %.0f s, got %.2f s",
						label, seconds, residuals.getMaxTimeDelayErrorSeconds()));
	}

	private void print(String label, DifarLocalisationResiduals residuals) {
		System.out.printf("%-28s bearing %6.1f deg, timing %6.2f s%n", label + ":",
				residuals.getMaxBearingErrorDegrees(), residuals.getMaxTimeDelayErrorSeconds());
	}

	/**
	 * Two whales calling at the same moment, each heard on its own buoy, then
	 * localised as though the detections were one call.
	 * @param whales the source heard on each buoy, in buoy order. The same
	 * position twice gives a correct match.
	 * @return how far the fitted position sits from the measurements.
	 */
	private DifarLocalisationResiduals residualsForMatch(double[][] whales) {
		DIFARTargetMotionInformation info = new DifarTestScenario()
				.addDetection(BUOY_A, whales[0], 0, 0)
				.addDetection(BUOY_B, whales[1], 0, 0)
				.build(2.0);
		Simplex2D simplex = new Simplex2D();
		simplex.setStartPoint(info.getMeanPosition());
		TargetMotionResult[] results = simplex.runModel(info);
		if (results == null || results.length != 1 || results[0] == null
				|| results[0].getLatLong() == null) {
			throw new AssertionError("optimisation failed");
		}
		return DifarLocalisationResiduals.calculate(info, results[0].getLatLong());
	}
}
