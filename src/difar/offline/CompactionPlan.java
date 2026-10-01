package difar.offline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.ToLongFunction;

import difar.offline.TimeClosure.FileSpan;

/**
 * Works out which binary files to replace, and which clips go in each new
 * file, when some time slots have changed.
 * <p>
 * The changed slots are closed over the files that overlap them, the clips
 * in those files are read, and they are merged with the clips in memory. A
 * clip read from a file can lie outside every slot found so far, for example
 * if its file's header time is wrong. Its slot is then added, the closure is
 * found again, and any files it newly reaches are read. This repeats until
 * every clip read has a slot, so no clip is lost when the files are
 * replaced. It always stops, since each round adds at least one slot and
 * there are only so many clips.
 * <p>
 * Each file is read once, however many rounds there are.
 * <p>
 * A pure class with no PAMGuard dependencies, so it can be tested alone.
 * @param <F> whatever identifies a file to the caller.
 * @param <C> the clip type.
 */
public final class CompactionPlan<F, C> {

	private final List<F> files;
	private final NavigableSet<Long> slots;
	private final NavigableMap<Long, List<C>> clipsBySlot;
	private final List<C> relocated;

	private CompactionPlan(List<F> files, NavigableSet<Long> slots,
			NavigableMap<Long, List<C>> clipsBySlot, List<C> relocated) {
		this.files = files;
		this.slots = slots;
		this.clipsBySlot = clipsBySlot;
		this.relocated = Collections.unmodifiableList(relocated);
	}

	/**
	 * @param grid the grid the new files follow.
	 * @param changedTimes times in the slots that changed.
	 * @param files every file that might overlap them.
	 * @param readClips reads the clips in one file.
	 * @param inMemory clips held in memory, from any slot.
	 * @param deletedUIDs UIDs of clips deleted since they were saved.
	 * @param uid gives a clip's UID, which must be greater than zero.
	 * @param time gives a clip's start time in milliseconds.
	 * @return the files to replace and the clips for each new file.
	 */
	public static <F, C> CompactionPlan<F, C> plan(TimeGrid grid, Collection<Long> changedTimes,
			Collection<FileSpan<F>> files, Function<F, List<C>> readClips,
			Collection<C> inMemory, Set<Long> deletedUIDs,
			ToLongFunction<C> uid, ToLongFunction<C> time) {
		Set<Long> changed = new TreeSet<>(changedTimes);
		Map<F, List<C>> read = new HashMap<>();
		List<C> relocated = new ArrayList<>();
		while (true) {
			TimeClosure<F> closure = TimeClosure.close(grid, changed, files);
			List<C> fromFiles = new ArrayList<>();
			for (F file : closure.getFiles()) {
				fromFiles.addAll(read.computeIfAbsent(file, readClips));
			}
			ClipMerge<C> merge = ClipMerge.merge(grid, closure.getSlots(), fromFiles,
					inMemory, deletedUIDs, uid, time);
			if (merge.getStrays().isEmpty()) {
				return new CompactionPlan<>(closure.getFiles(), closure.getSlots(),
						merge.getClipsBySlot(), relocated);
			}
			changed.addAll(closure.getSlots());
			for (C stray : merge.getStrays()) {
				changed.add(time.applyAsLong(stray));
				relocated.add(stray);
			}
		}
	}

	/**
	 * @return the files to replace, in the order they were found.
	 */
	public List<F> getFiles() {
		return files;
	}

	/**
	 * @return the start of every slot being rewritten, in order.
	 */
	public NavigableSet<Long> getSlots() {
		return slots;
	}

	/**
	 * @return the clips for each new file, keyed by slot start and sorted by
	 * time, then UID. Slots with no clips get no file, so are absent.
	 */
	public NavigableMap<Long, List<C>> getClipsBySlot() {
		return clipsBySlot;
	}

	/**
	 * @return clips that lay outside their own file's times, in the order
	 * they were found. They are in the plan, in the slots of their start
	 * times. Worth reporting, since each one means a file's times were wrong.
	 */
	public List<C> getRelocated() {
		return relocated;
	}
}
