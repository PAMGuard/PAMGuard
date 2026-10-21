package difar.crossings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToLongFunction;

/**
 * Rules for which DIFAR crossings stand, given the clips each one contains.
 * <p>
 * A clip is one acoustic event, so it belongs to at most one crossing. A new
 * crossing claims its clips: any crossing sharing a clip with it loses that
 * clip. A crossing left with enough clips stands but must be recalculated;
 * one left with too few is removed. Growth is the case where nothing is left.
 * <p>
 * A crossing is described here only by the set of clips in it. The caller
 * chooses how a clip is named: by UID for crossings made now, or by channel
 * and time for crossings read from old files, which name partners that way.
 * <p>
 * A pure class with no PAMGuard dependencies, so it can be tested alone.
 */
public final class CrossingSets {

	private CrossingSets() {
	}

	/**
	 * The existing crossings a new crossing takes clips from, each with the
	 * clips it keeps. Crossings sharing no clip with the new one are absent.
	 * An empty set means the crossing keeps nothing.
	 * @param newClips the clips in the new crossing.
	 * @param existing each existing crossing and its clips.
	 * @return each affected crossing and the clips it keeps, in the order given.
	 */
	public static <C, K> Map<C, Set<K>> remainders(Set<K> newClips, Map<C, ? extends Set<K>> existing) {
		Map<C, Set<K>> affected = new LinkedHashMap<>();
		for (Map.Entry<C, ? extends Set<K>> entry : existing.entrySet()) {
			if (Collections.disjoint(newClips, entry.getValue())) {
				continue;
			}
			Set<K> kept = new HashSet<>(entry.getValue());
			kept.removeAll(newClips);
			affected.put(entry.getKey(), kept);
		}
		return affected;
	}

	/** A crossing left standing after old records are replayed. */
	public static final class Standing<R, K> {

		private final R record;
		private final Set<K> clips;
		private final boolean trimmed;

		Standing(R record, Set<K> clips, boolean trimmed) {
			this.record = record;
			this.clips = Collections.unmodifiableSet(clips);
			this.trimmed = trimmed;
		}

		/** @return the old record this crossing comes from. */
		public R getRecord() {
			return record;
		}

		/** @return the clips it holds now. */
		public Set<K> getClips() {
			return clips;
		}

		/**
		 * @return true if later records took clips from it, so its stored
		 * location no longer matches its clips and must be recalculated.
		 */
		public boolean isTrimmed() {
			return trimmed;
		}
	}

	/**
	 * Turn the crossings stored in old files into crossings that follow the
	 * rule that a clip belongs to one crossing.
	 * <p>
	 * Old files stored, on each clip, the crossing as it stood when that clip
	 * was saved. Replaying those records in the order they were saved, each
	 * claiming its clips as a new crossing does now, leaves the operator's
	 * latest decision standing. So a triangulation stored as records of one,
	 * two and three clips becomes one crossing of three.
	 * <p>
	 * Records with fewer clips than the minimum claim nothing and are
	 * dropped: a crossing needs at least two bearings. Records saved at the
	 * same time are replayed in the order given.
	 * @param records the records read from old clips, in any order.
	 * @param clips gives the clips in a record, including the clip that held it.
	 * @param savedTime gives the time the clip holding the record was saved.
	 * @param minClips the fewest clips a crossing may have.
	 * @return the crossings left standing, in the order their records were saved.
	 */
	public static <R, K> List<Standing<R, K>> replay(Collection<R> records, Function<R, ? extends Set<K>> clips,
			ToLongFunction<R> savedTime, int minClips) {
		List<R> ordered = new ArrayList<>(records);
		ordered.sort(Comparator.comparingLong(savedTime)); // stable, so ties keep the order given
		Map<R, Set<K>> standing = new LinkedHashMap<>();
		Set<R> trimmed = new HashSet<>();
		for (R record : ordered) {
			Set<K> mine = new HashSet<>(clips.apply(record));
			if (mine.size() < minClips) {
				continue;
			}
			for (Map.Entry<R, Set<K>> hit : remainders(mine, standing).entrySet()) {
				if (hit.getValue().size() < minClips) {
					standing.remove(hit.getKey());
					trimmed.remove(hit.getKey());
				}
				else {
					standing.put(hit.getKey(), hit.getValue());
					trimmed.add(hit.getKey());
				}
			}
			standing.put(record, mine);
		}
		List<Standing<R, K>> result = new ArrayList<>();
		for (Map.Entry<R, Set<K>> entry : standing.entrySet()) {
			result.add(new Standing<>(entry.getKey(), entry.getValue(), trimmed.contains(entry.getKey())));
		}
		return result;
	}
}
