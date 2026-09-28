package test.difar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import PamguardMVC.PamDataUnit;
import difar.DifarMatchSelector;

/**
 * Choosing which detection on another buoy is the same call.
 * <p>
 * Each test sets up one detection on the first buoy and several on the others,
 * only one of which is the same whale. The selector has to find it by
 * localising each combination rather than by anything about the calls
 * themselves.
 */
public class DifarMatchSelectorTest {

	private static final double[] BUOY_A = {0, 0};
	private static final double[] BUOY_B = {15000, 0};
	private static final double[] BUOY_C = {7500, -12000};

	private static final double[] WHALE = {5000, 8000};

	/** Timing error, and the two limits, as the defaults have them. */
	private static final double TIMING_ERROR_S = 2.0;
	private static final double MAX_BEARING_DEG = 20;
	private static final double MAX_DELAY_S = 6;

	/**
	 * The right call is on the second buoy alongside two other whales. The
	 * overlap in time and frequency cannot tell them apart, so the geometry
	 * has to.
	 * <p>
	 * The whale at 3000, 6000 is a hard distractor. Matching it leaves a
	 * timing residual of only 1.2 s, so it passes the limits and the selector
	 * has to prefer the better fit rather than simply reject it.
	 */
	@Test
	public void picksTheRightCallFromSeveralCandidates() {
		DifarTestScenario scenario = new DifarTestScenario();
		scenario.addDetection(BUOY_A, WHALE, 0, 0);
		PamDataUnit seed = scenario.getLastUnit();

		scenario.addDetection(BUOY_B, new double[] {3000, 6000}, 0, 0);
		PamDataUnit wrongOne = scenario.getLastUnit();
		scenario.addDetection(BUOY_B, WHALE, 0, 0);
		PamDataUnit rightOne = scenario.getLastUnit();
		scenario.addDetection(BUOY_B, new double[] {11000, 20000}, 0, 0);
		PamDataUnit wrongTwo = scenario.getLastUnit();

		DifarMatchSelector.Match match = select(seed,
				Arrays.asList(Arrays.asList(wrongOne, rightOne, wrongTwo)));
		assertNotNull(match, "the correct call should have been found");
		assertEquals(2, match.getUnits().size());
		assertTrue(match.getUnits().contains(rightOne), "the correct call should be in the match");
	}

	/**
	 * With no consistent candidate, nothing should be matched. These two
	 * whales leave timing residuals of 8.8 s and 7.3 s, both outside the limit.
	 * Not every wrong match is this obvious: see DifarWrongMatchTest for
	 * geometries that leave no usable trace at all.
	 */
	@Test
	public void rejectsWhenNoCandidateFits() {
		DifarTestScenario scenario = new DifarTestScenario();
		scenario.addDetection(BUOY_A, WHALE, 0, 0);
		PamDataUnit seed = scenario.getLastUnit();

		scenario.addDetection(BUOY_B, new double[] {11000, 20000}, 0, 0);
		PamDataUnit wrongOne = scenario.getLastUnit();
		scenario.addDetection(BUOY_B, new double[] {9000, 18000}, 0, 0);
		PamDataUnit wrongTwo = scenario.getLastUnit();

		assertNull(select(seed, Arrays.asList(Arrays.asList(wrongOne, wrongTwo))),
				"neither candidate is the same call, so there should be no match");
	}

	/** A third buoy that agrees should be taken, not left out. */
	@Test
	public void prefersATripletWhenTheThirdBuoyAgrees() {
		DifarTestScenario scenario = new DifarTestScenario();
		scenario.addDetection(BUOY_A, WHALE, 0, 0);
		PamDataUnit seed = scenario.getLastUnit();
		scenario.addDetection(BUOY_B, WHALE, 0, 0);
		PamDataUnit onB = scenario.getLastUnit();
		scenario.addDetection(BUOY_C, WHALE, 0, 0);
		PamDataUnit onC = scenario.getLastUnit();

		DifarMatchSelector.Match match = select(seed,
				Arrays.asList(Arrays.asList(onB), Arrays.asList(onC)));
		assertNotNull(match);
		assertEquals(3, match.getUnits().size(), "all three buoys heard the same call");
	}

	/**
	 * A third buoy that disagrees should be left out, keeping the good pair.
	 * The whale on the third buoy leaves a timing residual of 11.4 s when
	 * added to the group.
	 */
	@Test
	public void dropsAThirdBuoyThatDisagrees() {
		DifarTestScenario scenario = new DifarTestScenario();
		scenario.addDetection(BUOY_A, WHALE, 0, 0);
		PamDataUnit seed = scenario.getLastUnit();
		scenario.addDetection(BUOY_B, WHALE, 0, 0);
		PamDataUnit onB = scenario.getLastUnit();
		scenario.addDetection(BUOY_C, new double[] {2000, 25000}, 0, 0);
		PamDataUnit otherWhaleOnC = scenario.getLastUnit();

		DifarMatchSelector.Match match = select(seed,
				Arrays.asList(Arrays.asList(onB), Arrays.asList(otherWhaleOnC)));
		assertNotNull(match, "the good pair should still be matched");
		assertEquals(2, match.getUnits().size(), "the third buoy heard a different whale");
		assertTrue(match.getUnits().contains(onB));
	}

	/** An empty candidate list for a buoy is normal, not an error. */
	@Test
	public void handlesBuoysWithNoCandidates() {
		DifarTestScenario scenario = new DifarTestScenario();
		scenario.addDetection(BUOY_A, WHALE, 0, 0);
		PamDataUnit seed = scenario.getLastUnit();
		scenario.addDetection(BUOY_B, WHALE, 0, 0);
		PamDataUnit onB = scenario.getLastUnit();

		DifarMatchSelector.Match match = select(seed,
				Arrays.asList(Arrays.asList(onB), new ArrayList<PamDataUnit>()));
		assertNotNull(match);
		assertEquals(2, match.getUnits().size());
	}

	/** With nothing to match to, there is no match. */
	@Test
	public void noCandidatesGivesNoMatch() {
		DifarTestScenario scenario = new DifarTestScenario();
		scenario.addDetection(BUOY_A, WHALE, 0, 0);
		assertNull(select(scenario.getLastUnit(), new ArrayList<List<PamDataUnit>>()));
	}


	/**
	 * Every group that was tried should be reported, in the order they were
	 * preferred, with a reason against each one that lost. This is what a
	 * display needs to show why a call was not matched.
	 */
	@Test
	public void reportsEveryCandidateConsidered() {
		DifarTestScenario scenario = new DifarTestScenario();
		scenario.addDetection(BUOY_A, WHALE, 0, 0);
		PamDataUnit seed = scenario.getLastUnit();
		scenario.addDetection(BUOY_B, WHALE, 0, 0);
		PamDataUnit rightOne = scenario.getLastUnit();
		scenario.addDetection(BUOY_B, new double[] {11000, 20000}, 0, 0);
		PamDataUnit wrongOne = scenario.getLastUnit();

		DifarMatchSelector selector = new DifarMatchSelector(null,
				TIMING_ERROR_S, MAX_BEARING_DEG, MAX_DELAY_S);
		List<DifarMatchSelector.Match> all =
				selector.selectAll(seed, Arrays.asList(Arrays.asList(rightOne, wrongOne)));

		assertEquals(2, all.size(), "both candidates should be reported");
		assertTrue(all.get(0).isAccepted(), "the correct call should rank first");
		assertTrue(all.get(0).getUnits().contains(rightOne));
		assertNull(all.get(0).getRejectReason(), "an accepted match has no reason against it");

		DifarMatchSelector.Match rejected = all.get(1);
		assertTrue(!rejected.isAccepted());
		assertTrue(rejected.getUnits().contains(wrongOne));
		assertNotNull(rejected.getRejectReason());
		assertTrue(rejected.getRejectReason().contains("timing"),
				"this one fails on timing: " + rejected.getRejectReason());
		System.out.println("rejected because: " + rejected.getRejectReason());
	}

	/** A group that fails on bearings should say so, not blame the timing. */
	@Test
	public void reasonNamesTheMeasurementThatFailed() {
		DifarTestScenario scenario = new DifarTestScenario();
		scenario.addDetection(BUOY_A, WHALE, 0, 0);
		PamDataUnit seed = scenario.getLastUnit();
		scenario.addDetection(BUOY_B, new double[] {10000, -8000}, 0, 0);
		PamDataUnit oppositeSide = scenario.getLastUnit();

		DifarMatchSelector selector = new DifarMatchSelector(null,
				TIMING_ERROR_S, MAX_BEARING_DEG, MAX_DELAY_S);
		List<DifarMatchSelector.Match> all =
				selector.selectAll(seed, Arrays.asList(Arrays.asList(oppositeSide)));
		assertEquals(1, all.size());
		assertTrue(!all.get(0).isAccepted());
		assertTrue(all.get(0).getRejectReason().contains("bearing"),
				"whales on opposite sides fail on bearings first: " + all.get(0).getRejectReason());
		System.out.println("rejected because: " + all.get(0).getRejectReason());
	}

	private DifarMatchSelector.Match select(PamDataUnit seed, List<List<PamDataUnit>> candidates) {
		DifarMatchSelector selector = new DifarMatchSelector(null,
				TIMING_ERROR_S, MAX_BEARING_DEG, MAX_DELAY_S);
		return selector.select(seed, candidates);
	}
}
