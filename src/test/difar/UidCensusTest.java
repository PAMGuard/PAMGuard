package test.difar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import difar.offline.UidCensus;

/**
 * Tests for counting shared and repeated clip UIDs. No PAMGuard runtime is
 * needed.
 */
public class UidCensusTest {

	private static final long T0 = 1548720000000L; // 2019-01-29 00:00:00 UTC
	private static final int CH0 = 1, CH1 = 2;

	@Test
	void uniqueUIDsAreUnique() {
		UidCensus census = new UidCensus();
		census.add(1, CH0, T0);
		census.add(2, CH1, T0);
		census.add(3, CH0, T0 + 1000);
		assertTrue(census.isUnique());
		assertEquals(3, census.getStored());
		assertEquals(3, census.getClips());
		assertEquals(3, census.getUIDs());
		assertEquals(0, census.getExactDuplicates());
		assertEquals(0, census.getClipsSharingUIDs());
	}

	@Test
	void aRestartedCountSharesUIDs() {
		UidCensus census = new UidCensus();
		// before a crash
		census.add(1, CH0, T0);
		census.add(2, CH1, T0 + 1000);
		// after it, the count starts again
		census.add(1, CH0, T0 + 3600000);
		census.add(2, CH0, T0 + 3601000);
		census.add(3, CH1, T0 + 3602000);
		assertFalse(census.isUnique());
		assertEquals(2, census.getSharedUIDs());
		assertEquals(4, census.getClipsSharingUIDs());
		assertEquals(3, census.getUIDs());
		assertEquals(0, census.getExactDuplicates());
	}

	@Test
	void aClipStoredTwiceIsNotShared() {
		UidCensus census = new UidCensus();
		census.add(300, CH1, T0);
		census.add(300, CH1, T0);
		census.add(301, CH1, T0 + 1000);
		assertTrue(census.isUnique());
		assertEquals(3, census.getStored());
		assertEquals(1, census.getExactDuplicates());
		assertEquals(2, census.getClips());
	}

	@Test
	void sameUIDAndTimeOnAnotherChannelIsShared() {
		UidCensus census = new UidCensus();
		census.add(7, CH0, T0);
		census.add(7, CH1, T0);
		assertFalse(census.isUnique());
		assertEquals(0, census.getExactDuplicates());
		assertEquals(2, census.getClipsSharingUIDs());
	}

	@Test
	void aSecondCopyOfASharedClipCountsOnce() {
		UidCensus census = new UidCensus();
		census.add(300, CH1, T0);
		census.add(300, CH1, T0);
		census.add(300, CH0, T0 + 3600000);
		assertEquals(1, census.getExactDuplicates());
		assertEquals(1, census.getSharedUIDs());
		assertEquals(2, census.getClipsSharingUIDs());
	}
}
