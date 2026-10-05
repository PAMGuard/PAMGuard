package difar.display;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * Sets how far DIFAR's scrolling panels move for one step of the mouse wheel or
 * one click of a scroll bar arrow. Swing's default for a panel is one pixel,
 * which makes a strip of clips crawl. Kept in DIFAR rather than changing the
 * core clip display.
 */
public class ScrollSteps {

	/**
	 * Pixels per scroll step. The wheel moves three steps per notch on most
	 * systems, so a notch moves about 90 pixels. Swing pixels are scaled with
	 * the screen's display scaling, so a step looks the same size on a 4K
	 * screen at 150% as on an ordinary screen at 100%.
	 */
	public static final int STEP_PIXELS = 30;

	private ScrollSteps() {
	}

	/**
	 * Set the scroll steps of the scroll pane that holds a component.
	 * @param view a component inside a scroll pane
	 */
	public static void setFor(JComponent view) {
		if (view == null) {
			return;
		}
		set((JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, view));
	}

	/**
	 * Set the scroll steps of a scroll pane, vertical and horizontal.
	 * @param scrollPane the scroll pane
	 */
	public static void set(JScrollPane scrollPane) {
		if (scrollPane == null) {
			return;
		}
		set(scrollPane, STEP_PIXELS);
	}

	/**
	 * Set the scroll steps of a scroll pane, with its own vertical step. For a
	 * table, pass the row height, so a step moves one row. A table otherwise
	 * steps only to the next row's edge, which can be a few pixels.
	 * @param scrollPane the scroll pane
	 * @param verticalStep pixels per vertical step
	 */
	public static void set(JScrollPane scrollPane, int verticalStep) {
		if (scrollPane == null) {
			return;
		}
		scrollPane.getVerticalScrollBar().setUnitIncrement(Math.max(verticalStep, 1));
		scrollPane.getHorizontalScrollBar().setUnitIncrement(STEP_PIXELS);
	}
}
