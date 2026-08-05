package detectionPlotFX.layout;

import PamguardMVC.PamDataUnit;
import detectionPlotFX.projector.DetectionPlotProjector;
import javafx.geometry.Side;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import pamViewFX.fxNodes.pamAxis.PamAxisPane2;

/**
 * Abstract base class for {@link DetectionPlot} implementations that provides common 
 * infrastructure. This eliminates the need for every plot type to independently hold  
 * a reference to {@code DetectionPlotDisplay} and duplicate the {@code reDrawLastUnit()} 
 * pattern.
 * <p>
 * Subclasses interact with the display through the {@link DetectionPlotContext} interface
 * rather than coupling directly to {@link DetectionPlotDisplay}. This allows plots to be
 * tested or reused in different display contexts.
 * <p>
 * Migrating an existing plot to use this base class is straightforward:
 * <ol>
 *   <li>Change {@code implements DetectionPlot<D>} to {@code extends AbstractDetectionPlot<D>}</li>
 *   <li>Replace the {@code DetectionPlotDisplay} field and constructor param with a call to {@code super(context)}</li>
 *   <li>Replace {@code detectionPlotDisplay.drawCurrentUnit()} with {@code requestRedraw()}</li>
 *   <li>Replace {@code detectionPlotDisplay.setAxisVisible(...)} with {@code getContext().setAxisVisible(...)}</li>
 *   <li>Remove the {@code reDrawLastUnit()} method (inherited from this class)</li>
 * </ol>
 * 
 * @param <D> the type of data unit this plot can render
 * @author Jamie Macaulay
 */
public abstract class AbstractDetectionPlot<D extends PamDataUnit> implements DetectionPlot<D> {

	/**
	 * The context through which this plot interacts with the display.
	 */
	private DetectionPlotContext context;

	/**
	 * Constructor.
	 * @param context - the display context. Can be a {@link DetectionPlotDisplay}.
	 */
	public AbstractDetectionPlot(DetectionPlotContext context) {
		this.context = context;
	}

	/**
	 * Get the display context for this plot.
	 * @return the context
	 */
	public DetectionPlotContext getContext() {
		return context;
	}

	/**
	 * Convenience method: request a repaint of the current data unit.
	 * Replaces the pattern {@code detectionPlotDisplay.drawCurrentUnit()}.
	 */
	public void requestRedraw() {
		if (context != null) {
			context.requestRedraw();
		}
	}

	/**
	 * Convenience method: request that the scroll bar be reconfigured.
	 * Replaces the pattern {@code detectionPlotDisplay.setupScrollBar()}.
	 */
	public void requestScrollBarSetup() {
		if (context != null) {
			context.requestScrollBarSetup();
		}
	}

	/**
	 * Repaint the current data unit. This is a compatibility alias for 
	 * {@link #requestRedraw()} — existing subclasses that override 
	 * {@code reDrawLastUnit()} will continue to work.
	 */
	public void reDrawLastUnit() {
		requestRedraw();
	}

	/**
	 * Get an axis pane from the display context.
	 * @param side - the side
	 * @return the axis pane
	 */
	protected PamAxisPane2 getAxisPane(Side side) {
		return context.getAxisPane(side);
	}

	/**
	 * Default implementation returns null (no settings pane).
	 * Override to provide a settings pane for this plot type.
	 */
	@Override
	public Pane getSettingsPane() {
		return null;
	}
}
