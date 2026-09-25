package test.difar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import difar.offline.TimeClosure;
import difar.offline.TimeClosure.FileSpan;
import difar.offline.TimeGrid;

/**
 * Tests for finding the files and slots that are rewritten together.
 * Files are named by strings. No PAMGuard runtime is needed.
 */
public class TimeClosureTest {

	private static final long HOUR = 3600000L;
	private static final long MIN = 60000L;
	private static final long H23 = 1362697200000L; // 2013-03-07 23:00:00 UTC

	private final TimeGrid hours = new TimeGrid(HOUR);

	private static FileSpan<String> file(String name, long start, long end) {
		return new FileSpan<>(name, start, end);
	}

	private static List<Long> slots(long... starts) {
		Long[] boxed = new Long[starts.length];
		for (int i = 0; i < starts.length; i++) {
			boxed[i] = starts[i];
		}
		return Arrays.asList(boxed);
	}

	@Test
	public void nothingChangedTakesNothing() {
		TimeClosure<String> c = TimeClosure.close(hours, Collections.emptyList(),
				Arrays.asList(file("a", H23, H23 + HOUR)));
		assertTrue(c.getSlots().isEmpty());
		assertTrue(c.getFiles().isEmpty());
	}

	@Test
	public void changedSlotWithNoFilesStandsAlone() {
		TimeClosure<String> c = TimeClosure.close(hours, slots(H23 + 5 * MIN), Collections.emptyList());
		assertEquals(slots(H23), List.copyOf(c.getSlots()));
		assertTrue(c.getFiles().isEmpty());
	}

	@Test
	public void gridFileInTheSlotIsTaken() {
		TimeClosure<String> c = TimeClosure.close(hours, slots(H23),
				Arrays.asList(file("a", H23, H23 + HOUR), file("b", H23 + HOUR, H23 + 2 * HOUR)));
		assertEquals(slots(H23), List.copyOf(c.getSlots()));
		assertEquals(Arrays.asList("a"), c.getFiles());
	}

	@Test
	public void fileReachingAnotherSlotBringsItIn() {
		// a normal mode file from 23:40 to 00:40
		TimeClosure<String> c = TimeClosure.close(hours, slots(H23),
				Arrays.asList(file("a", H23 + 40 * MIN, H23 + 100 * MIN)));
		assertEquals(slots(H23, H23 + HOUR), List.copyOf(c.getSlots()));
		assertEquals(Arrays.asList("a"), c.getFiles());
	}

	@Test
	public void chainOfOverlapsIsFollowed() {
		// c overlaps only b's second slot, and is listed before b
		TimeClosure<String> c = TimeClosure.close(hours, slots(H23),
				Arrays.asList(
						file("c", H23 + 130 * MIN, H23 + 190 * MIN),
						file("b", H23 + 50 * MIN, H23 + 125 * MIN),
						file("x", H23 + 5 * HOUR, H23 + 6 * HOUR)));
		assertEquals(slots(H23, H23 + HOUR, H23 + 2 * HOUR, H23 + 3 * HOUR), List.copyOf(c.getSlots()));
		assertEquals(Arrays.asList("b", "c"), c.getFiles());
	}

	@Test
	public void fileEndingOnTheBoundaryDoesNotReachOn() {
		TimeClosure<String> c = TimeClosure.close(hours, slots(H23 + HOUR),
				Arrays.asList(file("a", H23 + 30 * MIN, H23 + HOUR)));
		assertEquals(slots(H23 + HOUR), List.copyOf(c.getSlots()));
		assertTrue(c.getFiles().isEmpty());
	}

	@Test
	public void gapBetweenFilesStopsTheChain() {
		// sparse normal mode files: a in the changed slot, b two slots later
		TimeClosure<String> c = TimeClosure.close(hours, slots(H23),
				Arrays.asList(file("a", H23 + 10 * MIN, H23 + 70 * MIN),
						file("b", H23 + 2 * HOUR + 10 * MIN, H23 + 3 * HOUR)));
		assertEquals(slots(H23, H23 + HOUR), List.copyOf(c.getSlots()));
		assertEquals(Arrays.asList("a"), c.getFiles());
	}

	@Test
	public void overlappingViewerFilesAreAllTaken() {
		// the per-window files written before compaction overlap each other
		TimeClosure<String> c = TimeClosure.close(hours, slots(H23),
				Arrays.asList(file("w1", H23 + 52 * MIN, H23 + 172 * MIN),
						file("w2", H23 + 52 * MIN, H23 + 172 * MIN)));
		assertEquals(slots(H23, H23 + HOUR, H23 + 2 * HOUR), List.copyOf(c.getSlots()));
		assertEquals(Arrays.asList("w1", "w2"), c.getFiles());
	}

	@Test
	public void fileWithNoLengthIsTakenByItsSlot() {
		TimeClosure<String> c = TimeClosure.close(hours, slots(H23),
				Arrays.asList(file("a", H23 + 20 * MIN, H23 + 20 * MIN)));
		assertEquals(Arrays.asList("a"), c.getFiles());
	}

	@Test
	public void noFileOutsideTheSetReachesItsSlots() {
		List<FileSpan<String>> all = Arrays.asList(
				file("a", H23 + 40 * MIN, H23 + 100 * MIN),
				file("b", H23 + 110 * MIN, H23 + 130 * MIN),
				file("c", H23 + 3 * HOUR, H23 + 4 * HOUR),
				file("d", H23 - 30 * MIN, H23 + 10 * MIN));
		TimeClosure<String> c = TimeClosure.close(hours, slots(H23 + HOUR), all);
		for (FileSpan<String> f : all) {
			if (c.getFiles().contains(f.getFile())) {
				continue;
			}
			for (long s : hours.slotsSpanned(f.getStartMillis(), f.getEndMillis())) {
				assertTrue(!c.getSlots().contains(s), f.getFile() + " reaches a slot in the set");
			}
		}
	}
}
