package test.difar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import difar.offline.ViewerFileTimes;

/**
 * Tests for the rule that names binary files written in the viewer.
 * No PAMGuard runtime is needed.
 */
public class ViewerFileTimesTest {

	private static final long T0 = 1362698941000L; // 2013-03-07 23:29:01 UTC

	@Test
	public void freeNameIsUsedAsWanted() {
		assertEquals(T0, ViewerFileTimes.firstFreeTime(T0, t -> false, 10));
	}

	@Test
	public void takenNameStepsOnASecond() {
		Set<Long> taken = new HashSet<>();
		taken.add(T0);
		assertEquals(T0 + 1000, ViewerFileTimes.firstFreeTime(T0, taken::contains, 10));
	}

	@Test
	public void severalTakenNamesAreAllSkipped() {
		Set<Long> taken = new HashSet<>();
		for (int i = 0; i < 3; i++) {
			taken.add(T0 + i * 1000L);
		}
		assertEquals(T0 + 3000, ViewerFileTimes.firstFreeTime(T0, taken::contains, 10));
	}

	@Test
	public void givesUpAfterTheLastStep() {
		assertNull(ViewerFileTimes.firstFreeTime(T0, t -> true, 5));
	}

	@Test
	public void lastStepIsStillTried() {
		assertEquals(T0 + 5000, ViewerFileTimes.firstFreeTime(T0, t -> t < T0 + 5000, 5));
	}
}
