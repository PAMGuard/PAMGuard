package test.difar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import PamguardMVC.PamDataUnit;
import difar.DIFARTargetMotionInformation;
import difar.DifarLocalisationResiduals;
import difar.targetmotion.Simplex2D;
import difar.targetmotion.TargetMotionResult;
import pamMaths.PamVector;

/**
 * Localisation with arrival times measured by correlation, in place of
 * coarse detection times.
 * <p>
 * Detection times here are off by up to 0.7 s, as clip start times from a
 * detector with a 1 s hop are. The arrival times given as measured by
 * correlation are the true ones. The tests check that they are used, that
 * they are weighted by the correlation error, that a wrong match is caught by
 * the correlated residual, and that a detection without a correlated arrival
 * falls back to its detection time.
 */
public class DifarCorrelatedDelaysTest {

	private static final double[] BUOY_A = {0, 0};
	private static final double[] BUOY_B = {15000, 0};
	private static final double[] BUOY_C = {7000, 12000};
	private static final double[] WHALE = {6000, 5000};

	/** Errors added to the detection times, in seconds. */
	private static final double[] CLIP_ERRORS = {0.6, -0.7, 0.4};

	private static final double CORRELATION_ERROR = 0.05;

	/** Three detections of one whale, with coarse detection times. */
	private DifarTestScenario threeBuoys() {
		return new DifarTestScenario()
				.addDetection(BUOY_A, WHALE, 0, CLIP_ERRORS[0])
				.addDetection(BUOY_B, WHALE, 0, CLIP_ERRORS[1])
				.addDetection(BUOY_C, WHALE, 0, CLIP_ERRORS[2]);
	}

	/** True arrival times: each detection time less the error added to it. */
	private static Map<PamDataUnit, Double> trueArrivals(List<PamDataUnit> units, double[] errors) {
		Map<PamDataUnit, Double> arrivals = new HashMap<>();
		for (int i = 0; i < units.size(); i++) {
			arrivals.put(units.get(i), units.get(i).getTimeMilliseconds() - errors[i] * 1000.);
		}
		return arrivals;
	}

	private static TargetMotionResult fit(DIFARTargetMotionInformation info) {
		Simplex2D simplex = new Simplex2D();
		simplex.setStartPoint(info.getMeanPosition());
		TargetMotionResult[] results = simplex.runModel(info);
		if (results == null || results.length != 1 || results[0] == null || results[0].getLatLong() == null) {
			throw new AssertionError("optimisation failed");
		}
		return results[0];
	}

	/** @return distance from the fitted position to the whale, in metres. */
	private static double missMetres(DIFARTargetMotionInformation info, TargetMotionResult result) {
		PamVector p = info.latLongToMetres(result.getLatLong());
		return Math.hypot(p.getElement(0) - (WHALE[0] - BUOY_A[0]), p.getElement(1) - (WHALE[1] - BUOY_A[1]));
	}

	@Test
	public void correlatedArrivalsReplaceDetectionTimes() {
		DifarTestScenario scenario = threeBuoys();
		DIFARTargetMotionInformation info = scenario.build(2.0);
		info.setArrivalTimes(trueArrivals(scenario.getUnits(), CLIP_ERRORS), CORRELATION_ERROR);
		ArrayList<Double> delays = info.getTimeDelays().get(0);
		// buoy A to B: true travel times, without the clip errors
		double expected = DifarTestScenario.travelTime(BUOY_B, WHALE) - DifarTestScenario.travelTime(BUOY_A, WHALE);
		assertEquals(expected, delays.get(0), 0.002);
		for (double error : info.getTimeDelayErrors().get(0)) {
			assertEquals(CORRELATION_ERROR, error, 1e-9);
		}
		for (boolean correlated : info.getCorrelatedDelays()) {
			assertTrue(correlated);
		}
	}

	@Test
	public void correlatedArrivalsLocateTheWhale() {
		DifarTestScenario scenario = threeBuoys();
		DIFARTargetMotionInformation coarse = scenario.build(2.0);
		double coarseMiss = missMetres(coarse, fit(coarse));

		DIFARTargetMotionInformation fine = scenario.build(2.0);
		fine.setArrivalTimes(trueArrivals(scenario.getUnits(), CLIP_ERRORS), CORRELATION_ERROR);
		TargetMotionResult result = fit(fine);
		double fineMiss = missMetres(fine, result);
		System.out.printf("miss with detection times %.0f m, with correlated arrivals %.0f m%n", coarseMiss, fineMiss);
		assertTrue(fineMiss < 50, "correlated arrivals should put the fit on the whale, missed by " + fineMiss);

		DifarLocalisationResiduals residuals = DifarLocalisationResiduals.calculate(fine, result.getLatLong());
		assertTrue(residuals.getMaxCorrelatedDelayErrorSeconds() < 0.02);
		assertTrue(Double.isNaN(residuals.getMaxDetectionDelayErrorSeconds()), "every delay was correlated");
	}

	/**
	 * Two whales, each heard on its own buoy, wrongly matched. With detection
	 * times good to a second, the residual hides inside the timing error; with
	 * correlated arrivals it stands out.
	 */
	@Test
	public void wrongMatchShowsInTheCorrelatedResidual() {
		double[] whaleA = {4000, 12000};
		double[] whaleB = {13000, 5000};
		DifarTestScenario scenario = new DifarTestScenario()
				.addDetection(BUOY_A, whaleA, 0, 0)
				.addDetection(BUOY_B, whaleB, 0, 0);
		DIFARTargetMotionInformation info = scenario.build(2.0);
		info.setArrivalTimes(trueArrivals(scenario.getUnits(), new double[] {0, 0}), CORRELATION_ERROR);
		TargetMotionResult result = fit(info);
		DifarLocalisationResiduals residuals = DifarLocalisationResiduals.calculate(info, result.getLatLong());
		System.out.printf("wrong match: bearing %.1f deg, correlated timing %.2f s%n",
				residuals.getMaxBearingErrorDegrees(), residuals.getMaxCorrelatedDelayErrorSeconds());
		assertTrue(residuals.getMaxCorrelatedDelayErrorSeconds() > 0.5
				|| residuals.getMaxBearingErrorDegrees() > 20,
				"a wrong match should fail the correlated timing or the bearing limit");
	}

	@Test
	public void detectionWithoutArrivalFallsBackToItsTime() {
		DifarTestScenario scenario = threeBuoys();
		List<PamDataUnit> units = scenario.getUnits();
		Map<PamDataUnit, Double> arrivals = trueArrivals(units, CLIP_ERRORS);
		arrivals.remove(units.get(2));
		DIFARTargetMotionInformation info = scenario.build(2.0);
		info.setArrivalTimes(arrivals, CORRELATION_ERROR);
		// delays in PamUtils index order: 0-1, 0-2, 1-2
		List<Boolean> correlated = info.getCorrelatedDelays();
		assertTrue(correlated.get(0));
		assertFalse(correlated.get(1));
		assertFalse(correlated.get(2));
		// a delay to the uncorrelated detection uses its detection time
		double expected = (units.get(2).getTimeMilliseconds() - arrivals.get(units.get(0))) / 1000.;
		assertEquals(expected, info.getTimeDelays().get(0).get(1), 1e-6);
		// and carries mostly the detection timing error
		assertTrue(info.getTimeDelayErrors().get(0).get(1) > 1.9);
	}
}
