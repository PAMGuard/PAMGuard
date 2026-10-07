package test.difar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import difar.offline.TimeGrid;

/**
 * Tests for the grid of time slots that shapes binary files written in the
 * viewer. Runs at an hour and at fifteen minutes, so nothing assumes an hour.
 * No PAMGuard runtime is needed.
 */
public class TimeGridTest {

	private static final long HOUR = 3600000L;
	private static final long QUARTER = 900000L;
	private static final long H23 = 1362697200000L; // 2013-03-07 23:00:00 UTC
	private static final long T0 = 1362698941000L; // 2013-03-07 23:29:01 UTC

	private final TimeGrid hours = new TimeGrid(HOUR);
	private final TimeGrid quarters = new TimeGrid(QUARTER);

	@Test
	public void slotStartsOnTheClock() {
		assertEquals(H23, hours.slotStart(T0));
		assertEquals(H23 + QUARTER, quarters.slotStart(T0));
	}

	@Test
	public void slotEndIsTheNextStart() {
		assertEquals(H23 + HOUR, hours.slotEnd(T0));
		assertEquals(H23 + 2 * QUARTER, quarters.slotEnd(T0));
	}

	@Test
	public void boundaryBelongsToTheLaterSlot() {
		assertEquals(H23 + HOUR, hours.slotStart(H23 + HOUR));
		assertEquals(H23, hours.slotStart(H23 + HOUR - 1));
	}

	@Test
	public void timesBefore1970FloorDown() {
		assertEquals(-HOUR, hours.slotStart(-1));
	}

	@Test
	public void spanEndingOnABoundaryStopsThere() {
		assertEquals(Arrays.asList(H23), hours.slotsSpanned(H23, H23 + HOUR));
	}

	@Test
	public void spanCrossingABoundaryReachesBothSlots() {
		assertEquals(Arrays.asList(H23, H23 + HOUR), hours.slotsSpanned(T0, H23 + HOUR + 1));
	}

	@Test
	public void spanWithNoLengthReachesItsOwnSlot() {
		assertEquals(Arrays.asList(H23), hours.slotsSpanned(T0, T0));
		assertEquals(Arrays.asList(H23), hours.slotsSpanned(T0, T0 - 5));
	}

	@Test
	public void spanReachesEverySlotBetween() {
		assertEquals(Arrays.asList(H23 + QUARTER, H23 + 2 * QUARTER, H23 + 3 * QUARTER),
				quarters.slotsSpanned(T0, H23 + 3 * QUARTER + 1));
	}

	@Test
	public void widthMustBePositive() {
		assertThrows(IllegalArgumentException.class, () -> new TimeGrid(0));
		assertThrows(IllegalArgumentException.class, () -> new TimeGrid(-HOUR));
	}
}
