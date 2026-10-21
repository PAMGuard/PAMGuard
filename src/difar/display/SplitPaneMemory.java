package difar.display;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JSplitPane;

/**
 * Places a split pane's divider at a fraction of the pane, and remembers where
 * the operator leaves it. Fractions, not pixels, so a saved layout suits any
 * screen or window size.
 * <p>
 * A divider can only be placed by fraction once the pane has a size, so the
 * fraction is applied at the pane's first resize. Until then nothing is
 * remembered, so the layout's own early divider moves are not saved. The
 * pane's resize weight is set to the same fraction, so the proportions hold
 * as the window is resized.
 */
public class SplitPaneMemory {

	private SplitPaneMemory() {
	}

	/**
	 * @param pane the split pane
	 * @param defaultFraction where the divider goes when nothing is remembered, 0 to 1
	 * @param remembered gets the remembered fraction, or null if none
	 * @param remember stores the fraction after the operator moves the divider
	 */
	public static void apply(JSplitPane pane, double defaultFraction,
			Supplier<Double> remembered, Consumer<Double> remember) {
		Double saved = remembered.get();
		double fraction = usable(saved) ? saved : defaultFraction;
		pane.setResizeWeight(fraction);
		boolean[] placed = {false};
		pane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				if (!placed[0] && span(pane) > 0) {
					pane.setDividerLocation(fraction);
					placed[0] = true;
				}
			}
		});
		pane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, e -> {
			int span = span(pane);
			if (!placed[0] || span <= 0) {
				return;
			}
			double now = pane.getDividerLocation() / (double) span;
			if (usable(now)) {
				remember.accept(now);
			}
		});
	}

	/**
	 * A fraction worth keeping: a divider dragged to an edge hides a panel,
	 * which is not a layout to restore next time.
	 */
	private static boolean usable(Double fraction) {
		return fraction != null && fraction > 0.05 && fraction < 0.95;
	}

	/** Pixels the divider can move across. */
	private static int span(JSplitPane pane) {
		int size = pane.getOrientation() == JSplitPane.VERTICAL_SPLIT ? pane.getHeight() : pane.getWidth();
		return size - pane.getDividerSize();
	}
}
