package difar.offline;

import java.util.function.LongPredicate;

/**
 * Picks the time used to name a new binary file written in the viewer.
 * <p>
 * Binary file names carry their time to the second, so two files started in
 * the same second would share a name and the second would overwrite the
 * first. In normal mode that cannot happen, because each file is named from
 * the moment it opens. In the viewer a file is named from the start of the
 * loaded data, and the same data can be loaded and saved to more than once.
 * <p>
 * A pure class with no PAMGuard dependencies, so it can be tested alone.
 */
public final class ViewerFileTimes {

	/** File names change once a second. */
	public static final long STEP_MILLIS = 1000;

	private ViewerFileTimes() {
	}

	/**
	 * The first time, at or after the one wanted, whose file name is free.
	 * Times step forward a second at a time.
	 * @param wantedMillis the time the file would ideally be named from.
	 * @param taken true for a time whose file name is already in use.
	 * @param maxSteps how many seconds to try before giving up.
	 * @return a time whose name is free, or null if none was found.
	 */
	public static Long firstFreeTime(long wantedMillis, LongPredicate taken, int maxSteps) {
		for (int i = 0; i <= maxSteps; i++) {
			long t = wantedMillis + i * STEP_MILLIS;
			if (!taken.test(t)) {
				return t;
			}
		}
		return null;
	}
}
