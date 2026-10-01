package test.difar;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import difar.DifarMatchSelector;

/**
 * Which detections on another buoy are close enough in time to be the same
 * call, before any localisation is tried.
 * <p>
 * The timings come from the 2019 voyage data. Buoys 158 and 159.0 were 13.3 km
 * apart, 8.8 s at 1500 m/s, and the marking slack is 6 s, so the gate is
 * 14.8 s either side of the seed.
 */
public class DifarCandidateGateTest {

	private static final long TRAVEL = 8800;
	private static final long SLACK = 6000;

	/** Seed on channel 2 at 19:32:02.020, in milliseconds from 19:32:00. */
	private static final long SEED = 2020;

	/** The ch0 detection that belongs with the seed, 7.9 s later. */
	@Test
	public void theMatchingCallPasses() {
		assertTrue(DifarMatchSelector.couldBeSameCall(SEED, 9939, TRAVEL, SLACK));
	}

	/**
	 * The ch0 detection at 19:31:40.723, 21.3 s earlier. Its clip overlapped the
	 * seed's once widened, so the old gate let it through. The fit then
	 * rejected it on timing.
	 */
	@Test
	public void aCallTooEarlyToBeTheSameFails() {
		assertFalse(DifarMatchSelector.couldBeSameCall(SEED, -19277, TRAVEL, SLACK));
	}

	/** The window is travel time plus slack, inclusive, either side. */
	@Test
	public void theWindowEdgeIsInclusive() {
		long edge = TRAVEL + SLACK;
		assertTrue(DifarMatchSelector.couldBeSameCall(SEED, SEED + edge, TRAVEL, SLACK));
		assertTrue(DifarMatchSelector.couldBeSameCall(SEED, SEED - edge, TRAVEL, SLACK));
		assertFalse(DifarMatchSelector.couldBeSameCall(SEED, SEED + edge + 1, TRAVEL, SLACK));
		assertFalse(DifarMatchSelector.couldBeSameCall(SEED, SEED - edge - 1, TRAVEL, SLACK));
	}

	/** With no buoy positions the travel time is zero, and only the slack applies. */
	@Test
	public void unknownPositionsLeaveOnlyTheSlack() {
		assertTrue(DifarMatchSelector.couldBeSameCall(SEED, SEED + SLACK, 0, SLACK));
		assertFalse(DifarMatchSelector.couldBeSameCall(SEED, SEED + SLACK + 1, 0, SLACK));
	}

	/** With nothing to choose from, no match is chosen. */
	@Test
	public void nothingToChooseFromChoosesNothing() {
		assertNull(DifarMatchSelector.chooseMatch(null));
		assertNull(DifarMatchSelector.chooseMatch(Collections.emptyList()));
	}
}
