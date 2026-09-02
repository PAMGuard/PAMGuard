package annotationMark.spectrogram;

import PamguardMVC.PamDataUnit;

/**
 * Small shared holder describing the annotation box that is currently being
 * interactively moved or resized on a time display, together with the live
 * measurement text (RMS, peak-to-peak, etc.) to draw inside it while the drag is
 * in progress.
 * <p>
 * It is written by the interaction handler
 * ({@link SpectrogramAnnotationModule}'s {@code DisplayObserver}) and read by the
 * drawing code ({@code annotationMark.fx.MarkDataPlotInfo}), which live in
 * different packages. Only one annotation box can be dragged at once, so a single
 * static state is sufficient.
 *
 * @author Jamie Macaulay
 */
public class AnnotationHandleState {

	private static volatile PamDataUnit editingUnit;
	private static volatile String[] editingText;

	/**
	 * Mark a data unit as being actively moved / resized and set the live
	 * measurement text to display inside it.
	 *
	 * @param unit the data unit being edited.
	 * @param text the lines of measurement text to draw (may be null).
	 */
	public static void setEditing(PamDataUnit unit, String[] text) {
		editingUnit = unit;
		editingText = text;
	}

	/**
	 * Clear the editing state once a drag has finished, so the live text is
	 * removed.
	 */
	public static void clearEditing() {
		editingUnit = null;
		editingText = null;
	}

	/**
	 * @param unit a data unit.
	 * @return true if the given data unit is the one currently being edited.
	 */
	public static boolean isEditing(PamDataUnit unit) {
		return editingUnit != null && editingUnit == unit;
	}

	/**
	 * @return the live measurement text for the unit currently being edited, or
	 *         null.
	 */
	public static String[] getEditingText() {
		return editingText;
	}
}
