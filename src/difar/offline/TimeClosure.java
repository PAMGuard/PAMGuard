package difar.offline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * Finds every binary file, and every time slot, that must be rewritten
 * together when some slots have changed.
 * <p>
 * A file overlapping a changed slot must be rewritten, because its clips
 * will move into files that follow the grid. That file may also reach other
 * slots, and those slots may overlap more files. So the set grows until no
 * file outside it overlaps a slot inside it. Afterwards no file outside the
 * set reaches any slot in it, so the new files cannot overlap any old file
 * that is kept.
 * <p>
 * Files are described only by their start and end times. Nothing here
 * assumes files follow the grid, so files written in normal mode, with
 * uneven starts and gaps between them, are handled the same way.
 * <p>
 * A pure class with no PAMGuard dependencies, so it can be tested alone.
 * @param <F> whatever identifies a file to the caller.
 */
public final class TimeClosure<F> {

	/**
	 * A file and the times it covers. The end is excluded, as in
	 * {@link TimeGrid}.
	 * @param <F> whatever identifies a file to the caller.
	 */
	public static final class FileSpan<F> {

		private final F file;
		private final long startMillis, endMillis;

		public FileSpan(F file, long startMillis, long endMillis) {
			this.file = file;
			this.startMillis = startMillis;
			this.endMillis = endMillis;
		}

		public F getFile() {
			return file;
		}

		public long getStartMillis() {
			return startMillis;
		}

		public long getEndMillis() {
			return endMillis;
		}
	}

	private final NavigableSet<Long> slots;
	private final List<F> files;

	private TimeClosure(NavigableSet<Long> slots, List<F> files) {
		this.slots = Collections.unmodifiableNavigableSet(slots);
		this.files = Collections.unmodifiableList(files);
	}

	/**
	 * Close a set of changed slots over the files that overlap them.
	 * @param grid the grid the new files will follow.
	 * @param changedTimes times in the slots that changed. Each is moved to
	 * the start of its slot, so any time within a slot will do.
	 * @param files every file that might overlap. Files that do not are ignored.
	 * @return the slots to rewrite and the files to replace.
	 */
	public static <F> TimeClosure<F> close(TimeGrid grid, Collection<Long> changedTimes,
			Collection<FileSpan<F>> files) {
		NavigableSet<Long> slots = new TreeSet<>();
		for (Long t : changedTimes) {
			slots.add(grid.slotStart(t));
		}
		List<FileSpan<F>> waiting = new ArrayList<>(files);
		List<F> taken = new ArrayList<>();
		boolean grew = !slots.isEmpty();
		while (grew) {
			grew = false;
			for (int i = 0; i < waiting.size(); i++) {
				FileSpan<F> span = waiting.get(i);
				long first = grid.slotStart(span.getStartMillis());
				long last = grid.lastSlotStart(span.getStartMillis(), span.getEndMillis());
				if (slots.subSet(first, true, last, true).isEmpty()) {
					continue;
				}
				slots.addAll(grid.slotsSpanned(span.getStartMillis(), span.getEndMillis()));
				taken.add(span.getFile());
				waiting.remove(i--);
				grew = true;
			}
		}
		return new TimeClosure<>(slots, taken);
	}

	/**
	 * @return the start of every slot to rewrite, in order.
	 */
	public NavigableSet<Long> getSlots() {
		return slots;
	}

	/**
	 * @return the files to replace, in the order they were found.
	 */
	public List<F> getFiles() {
		return files;
	}
}
