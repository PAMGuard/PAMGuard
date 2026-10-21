package test.difar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import PamguardMVC.PamDataUnit;
import difar.DifarMatchSelector;
import difar.SonobuoyHistory;
import difar.SonobuoyRecord;

/**
 * What changing a buoy's calibration does to a localisation.
 * <p>
 * Each detection keeps the magnetic angle it measured, and reads its buoy
 * position and compass correction from the sonobuoy history, as DIFAR does. So
 * calibrating a buoy afterwards moves its bearing, and the same detections give
 * a different answer. This is the scenario behind reworking triangulations when
 * a buoy changes.
 */
public class DifarCalibrationChangeTest {

	private static final double[] BUOY_A = {0, 0};
	private static final double[] BUOY_B = {15000, 0};
	private static final double[] BUOY_C = {7500, -12000};

	private static final double[] WHALE = {5000, 8000};

	/** Compass corrections of the three buoys, in degrees. */
	private static final double HEADING_A = 93.3;
	private static final double HEADING_B = 97.8;
	private static final double HEADING_C = 98.1;

	private static final double TIMING_ERROR_S = 2.0;
	private static final double MAX_BEARING_DEG = 20;
	private static final double MAX_DELAY_S = 6;

	/** Well before any detection, so every record is in force. */
	private static final long DEPLOYED = 0L;

	private static SonobuoyHistory history(double headingA, double headingB, double headingC) {
		SonobuoyHistory history = new SonobuoyHistory();
		history.setRecords(Arrays.asList(
				DifarTestScenario.buoyRecord(0, BUOY_A, headingA, DEPLOYED),
				DifarTestScenario.buoyRecord(1, BUOY_B, headingB, DEPLOYED),
				DifarTestScenario.buoyRecord(2, BUOY_C, headingC, DEPLOYED)));
		return history;
	}

	/** One call on all three buoys, read through the history. */
	private static DifarTestScenario callOnThreeBuoys(SonobuoyHistory history) {
		DifarTestScenario scenario = new DifarTestScenario();
		scenario.addCalibratedDetection(history, 0, BUOY_A, WHALE, 0, 0);
		scenario.addCalibratedDetection(history, 1, BUOY_B, WHALE, 0, 0);
		scenario.addCalibratedDetection(history, 2, BUOY_C, WHALE, 0, 0);
		return scenario;
	}

	private static DifarMatchSelector.Match localise(DifarTestScenario scenario) {
		List<PamDataUnit> units = scenario.getUnits();
		DifarMatchSelector selector = new DifarMatchSelector(null,
				TIMING_ERROR_S, MAX_BEARING_DEG, MAX_DELAY_S);
		List<List<PamDataUnit>> candidates = new ArrayList<>();
		candidates.add(Arrays.asList(units.get(1)));
		candidates.add(Arrays.asList(units.get(2)));
		return selector.select(units.get(0), candidates);
	}

	/** With the buoys calibrated as they were, the call is found where it was. */
	@Test
	public void theRightCalibrationFindsTheWhale() {
		DifarMatchSelector.Match match = localise(callOnThreeBuoys(history(HEADING_A, HEADING_B, HEADING_C)));

		assertTrue(match != null, "a correct geometry should give a match");
		double error = DifarTestScenario.distanceFrom(match.getResult().getLatLong(), WHALE);
		assertTrue(error < 500, "found " + error + " m from the whale");
	}

	/**
	 * One buoy left uncalibrated, which is what a heading of zero means, throws
	 * its bearing out by the calibration it should have had. That buoy drops
	 * out of the match, and the two that are still calibrated are used instead.
	 * This is why a saved triangulation cannot stand after a calibration
	 * changes: it may no longer be made of the same buoys.
	 */
	@Test
	public void losingOneCalibrationDropsThatBuoyFromTheMatch() {
		SonobuoyHistory history = history(HEADING_A, HEADING_B, HEADING_C);
		DifarTestScenario scenario = callOnThreeBuoys(history);
		List<PamDataUnit> units = scenario.getUnits();
		DifarMatchSelector.Match before = localise(scenario);
		assertTrue(before.getUnits().containsAll(units), "all three buoys should be used at first");

		history.setRecords(Arrays.asList(
				DifarTestScenario.buoyRecord(0, BUOY_A, HEADING_A, DEPLOYED),
				DifarTestScenario.buoyRecord(1, BUOY_B, 0, DEPLOYED),
				DifarTestScenario.buoyRecord(2, BUOY_C, HEADING_C, DEPLOYED)));

		DifarMatchSelector.Match after = localise(scenario);
		assertFalse(after.getUnits().contains(units.get(1)),
				"the buoy that lost its calibration should drop out of the match");
		assertTrue(after.getUnits().contains(units.get(2)),
				"the buoys still calibrated should be used");
		double error = DifarTestScenario.distanceFrom(after.getResult().getLatLong(), WHALE);
		assertTrue(error < 500, "the two good buoys still find the whale, " + error + " m out");
	}

	/** Putting the calibration back gives the same answer as before. */
	@Test
	public void restoringTheCalibrationRestoresTheAnswer() {
		SonobuoyHistory history = history(HEADING_A, HEADING_B, HEADING_C);
		DifarTestScenario scenario = callOnThreeBuoys(history);
		double before = DifarTestScenario.distanceFrom(localise(scenario).getResult().getLatLong(), WHALE);

		history.setRecords(Arrays.asList(
				DifarTestScenario.buoyRecord(0, BUOY_A, HEADING_A, DEPLOYED),
				DifarTestScenario.buoyRecord(1, BUOY_B, HEADING_B + 25, DEPLOYED),
				DifarTestScenario.buoyRecord(2, BUOY_C, HEADING_C, DEPLOYED)));
		history.setRecords(Arrays.asList(
				DifarTestScenario.buoyRecord(0, BUOY_A, HEADING_A, DEPLOYED),
				DifarTestScenario.buoyRecord(1, BUOY_B, HEADING_B, DEPLOYED),
				DifarTestScenario.buoyRecord(2, BUOY_C, HEADING_C, DEPLOYED)));

		double after = DifarTestScenario.distanceFrom(localise(scenario).getResult().getLatLong(), WHALE);
		assertEquals(before, after, 1e-6, "the same buoys should give the same answer");
	}

	/**
	 * A small calibration error moves the answer rather than breaking it, which
	 * is why the triangulation has to be worked out again rather than left
	 * alone.
	 */
	@Test
	public void aSmallCalibrationErrorMovesTheAnswer() {
		SonobuoyHistory history = history(HEADING_A, HEADING_B, HEADING_C);
		DifarTestScenario scenario = callOnThreeBuoys(history);
		double before = DifarTestScenario.distanceFrom(localise(scenario).getResult().getLatLong(), WHALE);

		history.setRecords(Arrays.asList(
				DifarTestScenario.buoyRecord(0, BUOY_A, HEADING_A + 5, DEPLOYED),
				DifarTestScenario.buoyRecord(1, BUOY_B, HEADING_B, DEPLOYED),
				DifarTestScenario.buoyRecord(2, BUOY_C, HEADING_C, DEPLOYED)));

		DifarMatchSelector.Match match = localise(scenario);
		assertTrue(match != null, "five degrees is within the limits, so it still fits");
		double after = DifarTestScenario.distanceFrom(match.getResult().getLatLong(), WHALE);
		assertTrue(after > before + 100,
				"the answer should move: was " + before + " m out, now " + after + " m");
	}

	/** Moving a buoy moves the answer too, not only recalibrating it. */
	@Test
	public void movingABuoyMovesTheAnswer() {
		SonobuoyHistory history = history(HEADING_A, HEADING_B, HEADING_C);
		DifarTestScenario scenario = callOnThreeBuoys(history);
		double before = DifarTestScenario.distanceFrom(localise(scenario).getResult().getLatLong(), WHALE);

		history.setRecords(Arrays.asList(
				DifarTestScenario.buoyRecord(0, new double[] {2000, 1000}, HEADING_A, DEPLOYED),
				DifarTestScenario.buoyRecord(1, BUOY_B, HEADING_B, DEPLOYED),
				DifarTestScenario.buoyRecord(2, BUOY_C, HEADING_C, DEPLOYED)));

		DifarMatchSelector.Match match = localise(scenario);
		double after = match == null ? Double.MAX_VALUE
				: DifarTestScenario.distanceFrom(match.getResult().getLatLong(), WHALE);
		assertTrue(after > before + 100,
				"the answer should move: was " + before + " m out, now " + after + " m");
	}

	/** A buoy with no record has no bearing to give. */
	@Test
	public void aDetectionWithNoBuoyRecordCannotBeUsed() {
		SonobuoyHistory history = history(HEADING_A, HEADING_B, HEADING_C);
		DifarTestScenario scenario = callOnThreeBuoys(history);

		history.setRecords(new ArrayList<SonobuoyRecord>());

		assertFalse(scenario.getUnits().isEmpty());
		assertEquals(null, scenario.getUnits().get(0).getOriginLatLong(false));
	}
}
