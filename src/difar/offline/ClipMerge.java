package difar.offline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.ToLongFunction;

/**
 * Merges the clips read from binary files with the clips in memory, and
 * groups them into the time slots of the files that will replace them.
 * <p>
 * Clips are matched by UID. A clip in memory replaces a clip from a file
 * with the same UID, since memory holds the latest version. A deleted UID is
 * dropped wherever it is found, so a rewrite never restores it. A clip
 * belongs to the slot containing its start time, so it may end after its
 * file does, as clips can at file boundaries in normal mode.
 * <p>
 * Only the slots being rewritten are filled. A clip in memory from another
 * slot is already in a file that is kept, so it is left out. A clip read
 * from a file but lying outside every slot being rewritten would be lost
 * when that file is replaced, so it is returned as a stray, and the caller
 * should not replace any files.
 * <p>
 * A pure class with no PAMGuard dependencies, so it can be tested alone.
 * @param <C> the clip type.
 */
public final class ClipMerge<C> {

	private final NavigableMap<Long, List<C>> bySlot;
	private final List<C> strays;

	private ClipMerge(NavigableMap<Long, List<C>> bySlot, List<C> strays) {
		this.bySlot = Collections.unmodifiableNavigableMap(bySlot);
		this.strays = Collections.unmodifiableList(strays);
	}

	/**
	 * @param grid the grid the new files follow.
	 * @param slots the start of each slot being rewritten.
	 * @param fromFiles clips read from the files being replaced.
	 * @param inMemory clips held in memory, from any slot.
	 * @param deletedUIDs UIDs of clips deleted since they were saved.
	 * @param uid gives a clip's UID, which must be greater than zero.
	 * @param time gives a clip's start time in milliseconds.
	 * @return the clips for each slot, and any strays.
	 */
	public static <C> ClipMerge<C> merge(TimeGrid grid, NavigableSet<Long> slots,
			Collection<C> fromFiles, Collection<C> inMemory, Set<Long> deletedUIDs,
			ToLongFunction<C> uid, ToLongFunction<C> time) {
		Map<Long, C> fileClips = new LinkedHashMap<>();
		for (C clip : fromFiles) {
			// a second copy of a UID from another file is dropped
			fileClips.putIfAbsent(checkedUID(clip, uid), clip);
		}
		Map<Long, C> merged = new LinkedHashMap<>(fileClips);
		for (C clip : inMemory) {
			long id = checkedUID(clip, uid);
			if (slots.contains(grid.slotStart(time.applyAsLong(clip)))) {
				merged.put(id, clip);
			}
		}
		NavigableMap<Long, List<C>> bySlot = new TreeMap<>();
		List<C> strays = new ArrayList<>();
		for (Map.Entry<Long, C> entry : merged.entrySet()) {
			if (deletedUIDs.contains(entry.getKey())) {
				continue;
			}
			C clip = entry.getValue();
			long slot = grid.slotStart(time.applyAsLong(clip));
			if (slots.contains(slot)) {
				bySlot.computeIfAbsent(slot, s -> new ArrayList<>()).add(clip);
			}
			else {
				strays.add(clip);
			}
		}
		Comparator<C> order = Comparator.comparingLong(time).thenComparingLong(uid);
		for (List<C> clips : bySlot.values()) {
			clips.sort(order);
		}
		strays.sort(order);
		return new ClipMerge<>(bySlot, strays);
	}

	private static <C> long checkedUID(C clip, ToLongFunction<C> uid) {
		long id = uid.applyAsLong(clip);
		if (id <= 0) {
			throw new IllegalArgumentException("Clip has no UID, so it cannot be merged: " + clip);
		}
		return id;
	}

	/**
	 * @return the clips for each slot that has any, keyed by slot start and
	 * sorted by time, then UID. Slots with no clips are absent.
	 */
	public NavigableMap<Long, List<C>> getClipsBySlot() {
		return bySlot;
	}

	/**
	 * @return clips from files that lie outside every slot being rewritten.
	 * If there are any, no files should be replaced.
	 */
	public List<C> getStrays() {
		return strays;
	}
}
