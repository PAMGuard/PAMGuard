package test.difar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

import difar.offline.ClipMerge;
import difar.offline.TimeGrid;

/**
 * Tests for merging clips from files with clips in memory, by UID.
 * Clips are a small stand-in class. No PAMGuard runtime is needed.
 */
public class ClipMergeTest {

	private static final long HOUR = 3600000L;
	private static final long MIN = 60000L;
	private static final long H23 = 1362697200000L; // 2013-03-07 23:00:00 UTC

	private final TimeGrid hours = new TimeGrid(HOUR);

	/** A clip with a UID, a time and a label to tell versions apart. */
	private static final class Clip {
		final long uid, time;
		final String label;

		Clip(long uid, long time, String label) {
			this.uid = uid;
			this.time = time;
			this.label = label;
		}

		@Override
		public String toString() {
			return label;
		}
	}

	private static NavigableSet<Long> slots(long... starts) {
		NavigableSet<Long> set = new TreeSet<>();
		for (long s : starts) {
			set.add(s);
		}
		return set;
	}

	private ClipMerge<Clip> merge(NavigableSet<Long> slots, List<Clip> files, List<Clip> memory, Set<Long> deleted) {
		return ClipMerge.merge(hours, slots, files, memory, deleted, c -> c.uid, c -> c.time);
	}

	private static Set<Long> none() {
		return Collections.emptySet();
	}

	@Test
	public void memoryReplacesTheFileVersion() {
		Clip old = new Clip(5, H23 + MIN, "file");
		Clip fresh = new Clip(5, H23 + MIN, "memory");
		ClipMerge<Clip> m = merge(slots(H23), Arrays.asList(old), Arrays.asList(fresh), none());
		assertEquals(1, m.getClipsBySlot().get(H23).size());
		assertSame(fresh, m.getClipsBySlot().get(H23).get(0));
	}

	@Test
	public void deletedUIDIsDroppedFromFilesAndMemory() {
		Clip a = new Clip(1, H23 + MIN, "a");
		Clip b = new Clip(2, H23 + 2 * MIN, "b");
		Clip b2 = new Clip(2, H23 + 2 * MIN, "b in memory");
		ClipMerge<Clip> m = merge(slots(H23), Arrays.asList(a, b), Arrays.asList(b2),
				new HashSet<>(Arrays.asList(2L)));
		assertEquals(Arrays.asList(a), m.getClipsBySlot().get(H23));
	}

	@Test
	public void slotLeftEmptyIsAbsent() {
		Clip a = new Clip(1, H23 + MIN, "a");
		ClipMerge<Clip> m = merge(slots(H23, H23 + HOUR), Arrays.asList(a), Collections.emptyList(), none());
		assertEquals(Arrays.asList(H23), List.copyOf(m.getClipsBySlot().keySet()));
	}

	@Test
	public void clipGoesToTheSlotOfItsStart() {
		Clip late = new Clip(1, H23 + HOUR - 2000, "starts 23:59:58");
		Clip next = new Clip(2, H23 + HOUR, "starts 00:00:00");
		ClipMerge<Clip> m = merge(slots(H23, H23 + HOUR), Arrays.asList(late, next), Collections.emptyList(), none());
		assertEquals(Arrays.asList(late), m.getClipsBySlot().get(H23));
		assertEquals(Arrays.asList(next), m.getClipsBySlot().get(H23 + HOUR));
	}

	@Test
	public void clipsAreSortedByTimeThenUID() {
		Clip c = new Clip(3, H23 + 5 * MIN, "c");
		Clip a = new Clip(9, H23 + MIN, "a");
		Clip b = new Clip(4, H23 + MIN, "b");
		ClipMerge<Clip> m = merge(slots(H23), Arrays.asList(c, a), Arrays.asList(b), none());
		assertEquals(Arrays.asList(b, a, c), m.getClipsBySlot().get(H23));
	}

	@Test
	public void memoryOutsideTheSlotsIsLeftOut() {
		Clip away = new Clip(1, H23 + 5 * HOUR, "in a kept file");
		ClipMerge<Clip> m = merge(slots(H23), Collections.emptyList(), Arrays.asList(away), none());
		assertTrue(m.getClipsBySlot().isEmpty());
		assertTrue(m.getStrays().isEmpty());
	}

	@Test
	public void fileClipOutsideTheSlotsIsAStray() {
		Clip stray = new Clip(1, H23 - 10 * MIN, "before its file");
		Clip ok = new Clip(2, H23 + MIN, "ok");
		ClipMerge<Clip> m = merge(slots(H23), Arrays.asList(stray, ok), Collections.emptyList(), none());
		assertEquals(Arrays.asList(stray), m.getStrays());
		assertEquals(Arrays.asList(ok), m.getClipsBySlot().get(H23));
	}

	@Test
	public void deletedStrayIsNotAStray() {
		Clip stray = new Clip(1, H23 - 10 * MIN, "deleted");
		ClipMerge<Clip> m = merge(slots(H23), Arrays.asList(stray), Collections.emptyList(),
				new HashSet<>(Arrays.asList(1L)));
		assertTrue(m.getStrays().isEmpty());
	}

	@Test
	public void secondFileCopyOfAUIDIsDropped() {
		Clip first = new Clip(7, H23 + MIN, "first");
		Clip second = new Clip(7, H23 + MIN, "second");
		ClipMerge<Clip> m = merge(slots(H23), Arrays.asList(first, second), Collections.emptyList(), none());
		assertEquals(Arrays.asList(first), m.getClipsBySlot().get(H23));
	}

	@Test
	public void newClipInMemoryIsAdded() {
		Clip saved = new Clip(1, H23 + MIN, "saved");
		Clip added = new Clip(2, H23 + 3 * MIN, "added");
		ClipMerge<Clip> m = merge(slots(H23), Arrays.asList(saved), Arrays.asList(added), none());
		assertEquals(Arrays.asList(saved, added), m.getClipsBySlot().get(H23));
	}

	@Test
	public void clipWithoutUIDIsRefused() {
		Clip bad = new Clip(0, H23, "no uid");
		assertThrows(IllegalArgumentException.class,
				() -> merge(slots(H23), Collections.emptyList(), Arrays.asList(bad), none()));
	}
}
