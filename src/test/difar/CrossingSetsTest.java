package test.difar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import difar.crossings.CrossingSets;
import difar.crossings.CrossingSets.Standing;

/**
 * Tests for which DIFAR crossings stand, under the rule that a clip belongs
 * to one crossing. Clips are named by letters, and crossings and old records
 * by strings. No PAMGuard runtime is needed.
 */
public class CrossingSetsTest {

	private static Set<String> clips(String... names) {
		return new HashSet<>(Arrays.asList(names));
	}

	/** An old record: the clips it holds, and when the clip holding it was saved. */
	private static final class Record {
		final String name;
		final Set<String> clips;
		final long saved;

		Record(String name, long saved, String... clips) {
			this.name = name;
			this.saved = saved;
			this.clips = clips(clips);
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private static List<Standing<Record, String>> replay(Record... records) {
		return CrossingSets.replay(Arrays.asList(records), r -> r.clips, r -> r.saved, 2);
	}

	private static List<Record> records(List<Standing<Record, String>> standing) {
		List<Record> out = new ArrayList<>();
		standing.forEach(s -> out.add(s.getRecord()));
		return out;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Set<String>> existing(Object... nameThenClips) {
		Map<String, Set<String>> map = new LinkedHashMap<>();
		for (int i = 0; i < nameThenClips.length; i += 2) {
			map.put((String) nameThenClips[i], (Set<String>) nameThenClips[i + 1]);
		}
		return map;
	}

	// remainders

	@Test
	public void grownCrossingLeavesNothing() {
		Map<String, Set<String>> hit = CrossingSets.remainders(clips("A", "B", "C"), existing("AB", clips("A", "B")));
		assertEquals(clips(), hit.get("AB"));
	}

	@Test
	public void sameClipsLeaveNothing() {
		Map<String, Set<String>> hit = CrossingSets.remainders(clips("A", "B"), existing("AB", clips("A", "B")));
		assertEquals(clips(), hit.get("AB"));
	}

	@Test
	public void sharedClipIsTaken() {
		Map<String, Set<String>> hit = CrossingSets.remainders(clips("C", "D"), existing("ABC", clips("A", "B", "C")));
		assertEquals(clips("A", "B"), hit.get("ABC"));
	}

	@Test
	public void smallerNewCrossingStillTakesItsClips() {
		// re-matching C with only A takes both from A-B-C
		Map<String, Set<String>> hit = CrossingSets.remainders(clips("A", "C"), existing("ABC", clips("A", "B", "C")));
		assertEquals(clips("B"), hit.get("ABC"));
	}

	@Test
	public void crossingSharingNothingIsUntouched() {
		Map<String, Set<String>> hit = CrossingSets.remainders(clips("A", "B"),
				existing("DE", clips("D", "E"), "BX", clips("B", "X")));
		assertEquals(Arrays.asList("BX"), new ArrayList<>(hit.keySet()));
	}

	// replay

	@Test
	public void pilotTriangulationBecomesOneCrossing() {
		// three clips of one call hold crossings with 1, 2 and 3 clips
		Record r1 = new Record("r1", 100, "A");
		Record r2 = new Record("r2", 200, "B", "A");
		Record r3 = new Record("r3", 300, "C", "A", "B");
		List<Standing<Record, String>> out = replay(r1, r2, r3);
		assertEquals(Arrays.asList(r3), records(out));
		assertFalse(out.get(0).isTrimmed());
	}

	@Test
	public void separateTriangulationsBothStand() {
		Record r1 = new Record("r1", 100, "A", "B");
		Record r2 = new Record("r2", 200, "C", "D");
		assertEquals(Arrays.asList(r1, r2), records(replay(r1, r2)));
	}

	@Test
	public void laterRecordTakesASharedClip() {
		// A-B then B-C: B goes to the later one, and A alone is no crossing
		Record r1 = new Record("r1", 100, "A", "B");
		Record r2 = new Record("r2", 200, "B", "C");
		assertEquals(Arrays.asList(r2), records(replay(r1, r2)));
	}

	@Test
	public void trimmedCrossingStandsAndIsFlagged() {
		Record r1 = new Record("r1", 100, "A", "B", "C");
		Record r2 = new Record("r2", 200, "C", "D");
		List<Standing<Record, String>> out = replay(r1, r2);
		assertEquals(Arrays.asList(r1, r2), records(out));
		assertEquals(clips("A", "B"), out.get(0).getClips());
		assertTrue(out.get(0).isTrimmed());
		assertFalse(out.get(1).isTrimmed());
	}

	@Test
	public void recordsAreReplayedInSaveOrder() {
		Record r1 = new Record("r1", 100, "A", "B");
		Record r2 = new Record("r2", 200, "B", "C");
		assertEquals(Arrays.asList(r2), records(replay(r2, r1)));
	}

	@Test
	public void identicalRecordsKeepTheLastSaved() {
		Record older = new Record("older", 100, "A", "B");
		Record newer = new Record("newer", 200, "B", "A");
		assertEquals(Arrays.asList(newer), records(replay(older, newer)));
	}

	@Test
	public void identicalRecordsSavedTogetherKeepTheLastGiven() {
		Record first = new Record("first", 100, "A", "B");
		Record second = new Record("second", 100, "A", "B");
		assertEquals(Arrays.asList(second), records(replay(first, second)));
	}

	@Test
	public void oneClipRecordIsDroppedAndClaimsNothing() {
		Record pair = new Record("pair", 100, "A", "B");
		Record alone = new Record("alone", 200, "A");
		assertEquals(Arrays.asList(pair), records(replay(pair, alone)));
	}

	@Test
	public void nothingInNothingOut() {
		assertTrue(CrossingSets.replay(Collections.<Record>emptyList(), r -> r.clips, r -> r.saved, 2).isEmpty());
	}
}
