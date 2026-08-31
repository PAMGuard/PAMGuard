package PamView.tables;

/**
 * A table cell value which displays as one thing but sorts as another. Useful
 * in tables where a cell shows a formatted string (a date, a duration, a "n of
 * m" count, etc.) but should sort on the underlying number.
 * <p>
 * Table models returning these should also return {@link SortableTableValue}
 * from getColumnClass so that the table row sorter uses the natural
 * (Comparable) ordering rather than a string comparison of the displayed text.
 * Default cell renderers show the displayed text since that's what
 * {@link #toString()} returns.
 *
 * @author Jamie Macaulay
 */
public class SortableTableValue implements Comparable<SortableTableValue> {

	private final String displayValue;

	private final Comparable<Object> sortValue;

	/**
	 * @param displayValue text to show in the table cell.
	 * @param sortValue value to sort on, can be null (nulls sort first).
	 */
	@SuppressWarnings("unchecked")
	public SortableTableValue(String displayValue, Comparable<?> sortValue) {
		this.displayValue = displayValue;
		this.sortValue = (Comparable<Object>) sortValue;
	}

	/**
	 * Convenience constructor for a time in milliseconds.
	 * @param displayValue text to show in the table cell.
	 * @param timeMillis time in milliseconds to sort on.
	 */
	public SortableTableValue(String displayValue, long timeMillis) {
		this(displayValue, Long.valueOf(timeMillis));
	}

	/**
	 * @return the text shown in the table cell.
	 */
	public String getDisplayValue() {
		return displayValue;
	}

	/**
	 * @return the value the cell sorts on.
	 */
	public Comparable<?> getSortValue() {
		return sortValue;
	}

	@Override
	public int compareTo(SortableTableValue other) {
		if (other == null) {
			return 1;
		}
		if (sortValue == null) {
			return other.sortValue == null ? 0 : -1;
		}
		if (other.sortValue == null) {
			return 1;
		}
		try {
			return sortValue.compareTo(other.sortValue);
		}
		catch (ClassCastException e) {
			// different types in the same column: fall back on the displayed text.
			return String.valueOf(displayValue).compareTo(String.valueOf(other.displayValue));
		}
	}

	@Override
	public String toString() {
		return displayValue == null ? "" : displayValue;
	}

}
