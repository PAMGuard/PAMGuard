package detectionPlotFX.layout;

import detectionPlotFX.data.DDDataInfo;
import javafx.geometry.Side;
import pamViewFX.fxNodes.pamAxis.PamAxisPane2;

/**
 * Interface that decouples {@link DetectionPlot} implementations from the concrete 
 * {@link DetectionPlotDisplay}. Plot implementations should interact with this context 
 * rather than holding direct references to the display.
 * <p>
 * This makes it possible to test plots independently of the full display infrastructure,
 * and allows plots to be reused in different display contexts.
 * 
 * @author Jamie Macaulay
 */
public interface DetectionPlotContext {
	
	/**
	 * Request a repaint of the current data unit. This is the preferred way for
	 * plot implementations to trigger a redraw when their settings change.
	 */
	public void requestRedraw();
	
	/**
	 * Request that the scroll bar be reconfigured. Typically called after
	 * axis limits change.
	 */
	public void requestScrollBarSetup();
	
	/**
	 * Set which axis are visible on the display.
	 * @param top - true to show top axis
	 * @param right - true to show right axis
	 * @param bottom - true to show bottom axis
	 * @param left - true to show left axis
	 */
	public void setAxisVisible(boolean top, boolean right, boolean bottom, boolean left);
	
	/**
	 * Get an axis pane by side.
	 * @param side - the side of the axis
	 * @return the axis pane
	 */
	public PamAxisPane2 getAxisPane(Side side);
	
	/**
	 * Get all axis panes.
	 * @return array of all axis panes in order TOP, RIGHT, BOTTOM, LEFT
	 */
	public PamAxisPane2[] getAllAxisPanes();
	
	/**
	 * Check whether the application is in viewer mode.
	 * @return true if in viewer mode
	 */
	public boolean isViewer();
	
	/**
	 * Get the current DDDataInfo for the display. 
	 * @return the current data info, or null if none set
	 */
	public DDDataInfo<?> getCurrentDataInfo();
	
	/**
	 * Get the plot pane (for plots that need direct canvas access, e.g. RawFFTPlot).
	 * Prefer other methods where possible.
	 * @return the plot pane
	 */
	public DDPlotPane getPlotPane();

}
