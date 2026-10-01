package difar.offline;

import java.util.ArrayList;
import java.util.List;

/**
 * A fixed grid of time slots, used to shape binary files written in the
 * viewer.
 * <p>
 * Slots are aligned to the UTC epoch, so a width that divides a day evenly
 * puts slot boundaries on midnight and on round clock times. Every file
 * written in the viewer covers exactly one slot, so its start and end times
 * follow from the slot and never depend on when it was saved.
 * <p>
 * Times are in milliseconds. A slot includes its start and excludes its end.
 * <p>
 * A pure class with no PAMGuard dependencies, so it can be tested alone.
 */
public final class TimeGrid {

	private final long widthMillis;

	/**
	 * @param widthMillis the width of every slot, greater than zero.
	 */
	public TimeGrid(long widthMillis) {
		if (widthMillis <= 0) {
			throw new IllegalArgumentException("Slot width must be greater than zero: " + widthMillis);
		}
		this.widthMillis = widthMillis;
	}

	/**
	 * @return the width of every slot in milliseconds.
	 */
	public long getWidthMillis() {
		return widthMillis;
	}

	/**
	 * @param timeMillis any time.
	 * @return the start of the slot containing it.
	 */
	public long slotStart(long timeMillis) {
		return Math.floorDiv(timeMillis, widthMillis) * widthMillis;
	}

	/**
	 * @param timeMillis any time.
	 * @return the end of the slot containing it, which is the start of the next.
	 */
	public long slotEnd(long timeMillis) {
		return slotStart(timeMillis) + widthMillis;
	}

	/**
	 * The last slot a span of time reaches. The end is excluded, so a span
	 * ending exactly on a boundary does not reach the slot after it. A span
	 * with no length reaches only the slot containing its start.
	 * @param startMillis start of the span.
	 * @param endMillis end of the span.
	 * @return the start of the last slot the span reaches.
	 */
	public long lastSlotStart(long startMillis, long endMillis) {
		long lastInstant = endMillis > startMillis ? endMillis - 1 : startMillis;
		return slotStart(lastInstant);
	}

	/**
	 * Every slot a span of time reaches, in order. The end is excluded, as in
	 * {@link #lastSlotStart(long, long)}.
	 * @param startMillis start of the span.
	 * @param endMillis end of the span.
	 * @return the start of each slot the span reaches.
	 */
	public List<Long> slotsSpanned(long startMillis, long endMillis) {
		List<Long> slots = new ArrayList<>();
		long last = lastSlotStart(startMillis, endMillis);
		for (long s = slotStart(startMillis); s <= last; s += widthMillis) {
			slots.add(s);
		}
		return slots;
	}
}
