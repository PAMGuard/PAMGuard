package test.difar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import PamUtils.LatLong;
import difar.DIFARTargetMotionInformation;
import difar.DifarLocalisationResiduals;
import difar.targetmotion.Simplex2D;
import difar.targetmotion.TargetMotionResult;

/**
 * Tests for DIFAR localisation from bearings and arrival time differences.
 * <p>
 * Buoys and sources are placed on a flat local grid in metres. Each buoy
 * reports the true bearing to the source, and a detection time set by the
 * travel time from the source. Bearing and timing errors can be added to check
 * that the fit notices them.
 * <p>
 * The tests drive the real DIFARTargetMotionInformation and Simplex2D. Only the
 * data units are fakes, so no PamController and no audio are needed.
 */
public class DifarSimplex2DTest {

	/** Timing error of one detection, in seconds. */
	private static final double TIMING_SD_S = 1.0;

	/** Allowed position error for noise-free measurements, in metres. */
	private static final double POSITION_TOL_M = 50.0;

	private static final double[][] TWO_BUOYS = {{0, 0}, {15000, 0}};
	private static final double[][] THREE_BUOYS = {{0, 0}, {15000, 0}, {7500, -12000}};
	private static final double[] SOURCE = {5000, 8000};

	@Test
	public void pairFindsSource() {
		TargetMotionResult result = localise(TWO_BUOYS, SOURCE, new double[] {0, 0}, 0);
		assertNearSource(result, SOURCE);
		assertTrue(result.getChi2() < 0.1, "chi2 for exact measurements should be small: " + result.getChi2());
	}

	@Test
	public void tripletFindsSource() {
		TargetMotionResult result = localise(THREE_BUOYS, SOURCE, new double[] {0, 0, 0}, 0);
		assertNearSource(result, SOURCE);
		assertTrue(result.getChi2() < 0.1, "chi2 for exact measurements should be small: " + result.getChi2());
	}

	@Test
	public void tripletWithBadBearingHasLargerChi2() {
		TargetMotionResult exact = localise(THREE_BUOYS, SOURCE, new double[] {0, 0, 0}, 0);
		TargetMotionResult biased = localise(THREE_BUOYS, SOURCE, new double[] {0, 0, 15}, 0);
		assertTrue(biased.getChi2() > exact.getChi2() + 1.0,
				"a 15 degree bearing error should raise chi2: " + biased.getChi2());
	}

	/**
	 * Delays between buoys should match the difference in travel time from the
	 * source, ordered by PamUtils.indexM1() and indexM2().
	 */
	@Test
	public void timeDelaysMatchGeometry() {
		DIFARTargetMotionInformation tmi = buildInfo(THREE_BUOYS, SOURCE, new double[] {0, 0, 0}, 0);
		ArrayList<ArrayList<Double>> delays = tmi.getTimeDelays();
		assertNotNull(delays);
		assertEquals(1, delays.size(), "all buoys share a clock, so there is one row");
		assertEquals(3, delays.get(0).size(), "three buoys give three pairs");
		int[] m1 = {0, 0, 1};
		int[] m2 = {1, 2, 2};
		for (int j = 0; j < 3; j++) {
			double expected = travelTime(THREE_BUOYS[m2[j]], SOURCE) - travelTime(THREE_BUOYS[m1[j]], SOURCE);
			assertEquals(expected, delays.get(0).get(j), 0.01,
					String.format("delay between buoys %d and %d", m1[j], m2[j]));
		}
		ArrayList<ArrayList<Double>> errors = tmi.getTimeDelayErrors();
		assertEquals(TIMING_SD_S * Math.sqrt(2.), errors.get(0).get(0), 1e-9);
	}

	/**
	 * Two bearings always cross, so a pair could never fail a bearing-only fit.
	 * With delays included, a pair whose timing disagrees with its bearings
	 * should now fail.
	 */
	@Test
	public void pairWithBadDelayIsRejected() {
		TargetMotionResult good = localise(TWO_BUOYS, SOURCE, new double[] {0, 0}, 0);
		TargetMotionResult bad = localise(TWO_BUOYS, SOURCE, new double[] {0, 0}, 8.0);
		assertEquals(1, good.getnDegreesFreedom(),
				"two bearings and one delay, fitting two coordinates, leaves one degree of freedom");
		assertTrue(good.getChi2() < 0.1, "consistent measurements should give a small chi2: " + good.getChi2());
		assertTrue(bad.getChi2() > 10.0, "an 8 second timing error should give a large chi2: " + bad.getChi2());
	}

	/**
	 * Residuals at the true source position should be near zero when the
	 * measurements are exact.
	 */
	@Test
	public void residualsAreSmallForExactMeasurements() {
		DIFARTargetMotionInformation tmi = buildInfo(THREE_BUOYS, SOURCE, new double[] {0, 0, 0}, 0);
		DifarLocalisationResiduals residuals = DifarLocalisationResiduals.calculate(tmi, sourceLatLong());
		assertEquals(0, residuals.getMaxBearingErrorDegrees(), 0.1);
		assertEquals(0, residuals.getMaxTimeDelayErrorSeconds(), 0.01);
		assertTrue(residuals.isWithin(20, 3), "exact measurements should pass any sensible limits");
	}

	/** A bearing error should show up in degrees, and nowhere else. */
	@Test
	public void bearingResidualReportsBearingError() {
		DIFARTargetMotionInformation tmi = buildInfo(THREE_BUOYS, SOURCE, new double[] {0, 0, 15}, 0);
		DifarLocalisationResiduals residuals = DifarLocalisationResiduals.calculate(tmi, sourceLatLong());
		assertEquals(15, residuals.getMaxBearingErrorDegrees(), 0.1);
		assertEquals(0, residuals.getMaxTimeDelayErrorSeconds(), 0.01);
		assertTrue(residuals.isWithin(20, 3), "15 degrees is within a 20 degree limit");
		assertTrue(!residuals.isWithin(10, 3), "15 degrees is outside a 10 degree limit");
	}

	/** A timing error should show up in seconds, and nowhere else. */
	@Test
	public void timeDelayResidualReportsTimingError() {
		DIFARTargetMotionInformation tmi = buildInfo(THREE_BUOYS, SOURCE, new double[] {0, 0, 0}, 8.0);
		DifarLocalisationResiduals residuals = DifarLocalisationResiduals.calculate(tmi, sourceLatLong());
		assertEquals(0, residuals.getMaxBearingErrorDegrees(), 0.1);
		assertEquals(8, residuals.getMaxTimeDelayErrorSeconds(), 0.01);
		assertTrue(!residuals.isWithin(20, 3), "8 seconds is outside a 3 second limit");
	}

	/** The true source position, for checking residuals directly. */
	private LatLong sourceLatLong() {
		return DifarTestScenario.toLatLong(SOURCE);
	}

	/** Travel time from a source to a buoy, in seconds. */
	private double travelTime(double[] buoy, double[] source) {
		return DifarTestScenario.travelTime(buoy, source);
	}

	/**
	 * Build the localisation inputs for a set of buoys.
	 * @param buoys buoy positions in metres, as {x east, y north}
	 * @param source source position in metres
	 * @param biasDeg bearing error added at each buoy, in degrees
	 * @param delayBiasSeconds timing error added at the second buoy, in seconds
	 * @return information ready to pass to the localiser
	 */
	private DIFARTargetMotionInformation buildInfo(double[][] buoys, double[] source,
			double[] biasDeg, double delayBiasSeconds) {
		DifarTestScenario scenario = new DifarTestScenario();
		for (int i = 0; i < buoys.length; i++) {
			scenario.addDetection(buoys[i], source, biasDeg[i], i == 1 ? delayBiasSeconds : 0);
		}
		return scenario.build(TIMING_SD_S);
	}

	/** Build the inputs, then run the real localiser on them. */
	private TargetMotionResult localise(double[][] buoys, double[] source,
			double[] biasDeg, double delayBiasSeconds) {
		DIFARTargetMotionInformation tmi = buildInfo(buoys, source, biasDeg, delayBiasSeconds);
		Simplex2D simplex = new Simplex2D();
		simplex.setStartPoint(tmi.getMeanPosition());
		TargetMotionResult[] results = simplex.runModel(tmi);
		assertNotNull(results);
		assertEquals(1, results.length);
		assertNotNull(results[0], "optimisation failed");
		return results[0];
	}

	/** Check a result lies close to the source. */
	private void assertNearSource(TargetMotionResult result, double[] source) {
		LatLong ll = result.getLatLong();
		assertNotNull(ll);
		double err = DifarTestScenario.distanceFrom(ll, source);
		assertTrue(err < POSITION_TOL_M, String.format(
				"expected (%.0f, %.0f), error %.0f m", source[0], source[1], err));
	}
}
