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
	 * systems, so a notch moves a little under one clip.
	 */
	public static final int STEP_PIXELS = 40;

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
		scrollPane.getVerticalScrollBar().setUnitIncrement(STEP_PIXELS);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(STEP_PIXELS);
	}
}
