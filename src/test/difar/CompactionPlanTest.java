package test.difar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import difar.offline.CompactionPlan;
import difar.offline.TimeClosure.FileSpan;
import difar.offline.TimeGrid;

/**
 * Tests for planning a rewrite, including clips that lie outside their own
 * file's times. Files are named by strings and hold clips in a map, which
 * stands in for reading them. No PAMGuard runtime is needed.
 */
public class CompactionPlanTest {

	private static final long HOUR = 3600000L;
	private static final long MIN = 60000L;
	private static final long H23 = 1362697200000L; // 2013-03-07 23:00:00 UTC

	private final TimeGrid hours = new TimeGrid(HOUR);

	private static final class Clip {
		final long uid, time;

		Clip(long uid, long time) {
			this.uid = uid;
			this.time = time;
		}

		@Override
		public String toString() {
			return "uid " + uid;
		}
	}

	/** Files, their spans and their clips, with a count of reads. */
	private static final class Disk {
		final List<FileSpan<String>> spans = new ArrayList<>();
		final Map<String, List<Clip>> clips = new HashMap<>();
		final Map<String, Integer> reads = new HashMap<>();

		Disk add(String name, long start, long end, Clip... in) {
			spans.add(new FileSpan<>(name, start, end));
			clips.put(name, Arrays.asList(in));
			return this;
		}

		List<Clip> read(String name) {
			reads.merge(name, 1, Integer::sum);
			return clips.get(name);
		}
	}

	private CompactionPlan<String, Clip> plan(Disk disk, List<Long> changed, List<Clip> memory, Set<Long> deleted) {
		return CompactionPlan.plan(hours, changed, disk.spans, disk::read, memory, deleted,
				c -> c.uid, c -> c.time);
	}

	private static List<Long> times(Long... t) {
		return Arrays.asList(t);
	}

	@Test
	public void simpleCaseNeedsOneRound() {
		Clip a = new Clip(1, H23 + MIN);
		Clip b = new Clip(2, H23 + 2 * MIN);
		Disk disk = new Disk().add("f", H23, H23 + HOUR, a);
		CompactionPlan<String, Clip> p = plan(disk, times(H23), Arrays.asList(b), Collections.emptySet());
		assertEquals(Arrays.asList("f"), p.getFiles());
		assertEquals(Arrays.asList(a, b), p.getClipsBySlot().get(H23));
		assertTrue(p.getRelocated().isEmpty());
	}

	@Test
	public void clipBeforeItsFileBringsItsSlotIn() {
		Clip early = new Clip(1, H23 - 10 * MIN); // header says 23:00, clip is 22:50
		Clip ok = new Clip(2, H23 + MIN);
		Disk disk = new Disk().add("f", H23, H23 + HOUR, early, ok);
		CompactionPlan<String, Clip> p = plan(disk, times(H23), Collections.emptyList(), Collections.emptySet());
		assertEquals(Arrays.asList(early), p.getClipsBySlot().get(H23 - HOUR));
		assertEquals(Arrays.asList(ok), p.getClipsBySlot().get(H23));
		assertEquals(Arrays.asList(early), p.getRelocated());
	}

	@Test
	public void relocatedSlotBringsInTheFileAlreadyThere() {
		Clip early = new Clip(1, H23 - 10 * MIN);
		Clip neighbour = new Clip(2, H23 - 30 * MIN);
		Disk disk = new Disk()
				.add("f", H23, H23 + HOUR, early)
				.add("g", H23 - HOUR, H23, neighbour);
		CompactionPlan<String, Clip> p = plan(disk, times(H23), Collections.emptyList(), Collections.emptySet());
		assertEquals(Arrays.asList("f", "g"), p.getFiles());
		assertEquals(Arrays.asList(neighbour, early), p.getClipsBySlot().get(H23 - HOUR));
	}

	@Test
	public void eachFileIsReadOnce() {
		Clip early = new Clip(1, H23 - 10 * MIN);
		Clip neighbour = new Clip(2, H23 - 30 * MIN);
		Disk disk = new Disk()
				.add("f", H23, H23 + HOUR, early)
				.add("g", H23 - HOUR, H23, neighbour);
		plan(disk, times(H23), Collections.emptyList(), Collections.emptySet());
		assertEquals(1, disk.reads.get("f"));
		assertEquals(1, disk.reads.get("g"));
	}

	@Test
	public void deletedClipOutsideItsFileDoesNotGrowThePlan() {
		Clip early = new Clip(1, H23 - 10 * MIN);
		Disk disk = new Disk().add("f", H23, H23 + HOUR, early);
		CompactionPlan<String, Clip> p = plan(disk, times(H23), Collections.emptyList(),
				new HashSet<>(Arrays.asList(1L)));
		assertEquals(1, p.getSlots().size());
		assertTrue(p.getClipsBySlot().isEmpty());
	}

	@Test
	public void untouchedFilesAreNotRead() {
		Disk disk = new Disk()
				.add("f", H23, H23 + HOUR, new Clip(1, H23 + MIN))
				.add("far", H23 + 5 * HOUR, H23 + 6 * HOUR, new Clip(2, H23 + 5 * HOUR));
		plan(disk, times(H23), Collections.emptyList(), Collections.emptySet());
		assertTrue(!disk.reads.containsKey("far"));
	}

	@Test
	public void noClipReadIsLeftOut() {
		Clip early = new Clip(1, H23 - 10 * MIN);
		Clip late = new Clip(2, H23 + 3 * HOUR);
		Clip ok = new Clip(3, H23 + MIN);
		Disk disk = new Disk().add("f", H23, H23 + HOUR, early, late, ok);
		CompactionPlan<String, Clip> p = plan(disk, times(H23), Collections.emptyList(), Collections.emptySet());
		Set<Clip> planned = new HashSet<>();
		p.getClipsBySlot().values().forEach(planned::addAll);
		assertEquals(new HashSet<>(Arrays.asList(early, late, ok)), planned);
	}
}
