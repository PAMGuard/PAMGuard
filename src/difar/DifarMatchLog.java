package difar;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import PamguardMVC.PamDataUnit;

/**
 * Keeps the candidate matches worked out for recent detections, so that a
 * display can show why a call was matched to one detection and not another.
 * <p>
 * Matching is repeated whenever a detection is processed, so this holds only
 * the most recent result for each one, and only for the last few hundred
 * detections. Nothing here is saved. Reopening a dataset and clicking a clip
 * recalculates it.
 */
public class DifarMatchLog {

	/** Detections to remember. Enough for a display, small enough to ignore. */
	private static final int MAX_ENTRIES = 500;

	private final Map<PamDataUnit, List<DifarMatchSelector.Match>> entries =
			new LinkedHashMap<PamDataUnit, List<DifarMatchSelector.Match>>(16, 0.75f, true) {

		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Map.Entry<PamDataUnit, List<DifarMatchSelector.Match>> eldest) {
			return size() > MAX_ENTRIES;
		}
	};

	/**
	 * Record the candidates worked out for a detection, replacing anything
	 * held for it already.
	 * @param unit the detection that was matched.
	 * @param matches every candidate group, as ranked by the selector.
	 */
	public synchronized void put(PamDataUnit unit, List<DifarMatchSelector.Match> matches) {
		if (unit == null) {
			return;
		}
		entries.put(unit, matches == null ? new ArrayList<>() : new ArrayList<>(matches));
	}

	/**
	 * @param unit the detection of interest.
	 * @return the candidates worked out for it, best first, or an empty list if
	 * it has not been matched recently.
	 */
	public synchronized List<DifarMatchSelector.Match> get(PamDataUnit unit) {
		List<DifarMatchSelector.Match> matches = entries.get(unit);
		return matches == null ? new ArrayList<>() : new ArrayList<>(matches);
	}

	/** Forget everything, for example when a new dataset is loaded. */
	public synchronized void clear() {
		entries.clear();
	}
}
