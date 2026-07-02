package detectionPlotFX.layout;

import detectionPlotFX.projector.DetectionPlotProjector;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

/**
 * Interface for rendering a single detection on a plot canvas.
 * <p>
 * A {@code DetectionPlot} knows how to set up axes and paint data for one particular 
 * visualisation of a data unit (e.g. waveform, spectrum, spectrogram, Wigner plot).
 * Multiple {@code DetectionPlot} implementations are typically registered with a 
 * {@link detectionPlotFX.data.DDDataInfo DDDataInfo}, which lets the user switch between 
 * different plot types for the same data.
 * <p>
 * <b>Architecture overview:</b>
 * <pre>
 *   DetectionPlotDisplay ──implements──▶ DetectionPlotContext
 *          │                                    ▲
 *          ├── DDDataInfo (wraps a data block)   │ (plots interact via context)
 *          │       └── DetectionPlot[]           │
 *          │              └── AbstractDetectionPlot ───uses───┘
 *          └── DDPlotPane (canvas + axes)
 * </pre>
 * <p>
 * <b>Implementing a new plot type:</b>
 * <ol>
 *   <li>Extend {@link AbstractDetectionPlot} rather than implementing this interface directly.</li>
 *   <li>The abstract base class provides a {@link DetectionPlotContext} reference, 
 *       {@code requestRedraw()}, {@code requestScrollBarSetup()}, and a default 
 *       {@code getSettingsPane()} returning null.</li>
 *   <li>Override {@link #setupPlot()}, {@link #setupAxis}, and {@link #paintPlot} at minimum.</li>
 * </ol>
 * <p>
 * This interface does <i>not</i> require a reference to any display — it can be used standalone 
 * for testing or in alternative display contexts.
 *
 * @param <D> the type of data that is input for plotting
 * @author Jamie Macaulay
 */
public interface DetectionPlot<D> {
	
	/**
	 * Indicates that the data unit is being drawn on the plot canvas. 
	 */
	public static final int STANDARD_DRAW = 0; 
	
	/**
	 * Indicates that the data unit is being drawn on a scroll bar. 
	 */
	public static final int SCROLLPANE_DRAW = 1;  
	
	/**
	 * Get the name of the plot e.g. waveform, spectrum etc. 
	 */
	public String getName();
	
	/**
	 * Setup the plot. This may, for example, involve changing axis etc. 
	 * @param detectionPlotDisplay: The detection display. 
	 */
	public void setupPlot();
	
	/**
	 * Setup the axis. This is called before paintPlot. Ensures axis are correct values 
	 * @param data - the data of every channel. e.g. might be a waveform with each point representing a sample.
	 * @param sR - the sample rate. Primarily used to convert bins into time values. 
	 * @param pamAxis - all the axis of the graph. This is in order TOP, RIGHT, BOTTOM, LEFT. 
	 */
	public void setupAxis(D data, double sR, DetectionPlotProjector projector);

	/**
	 * Plot the relevent data from a detection. 
	 * @param data - the data of every channel. e.g. might be a waveform with each point representing a sample.
	 * @param graphicsContext - the graphics handle to draw on. 
	 * @param - the area, on the plot, to draw on. 
	 * @param pamAxis - the PamAxis for the plot, x and y axis (time and some measure of amplitude. )
	 * @param a flag with extra information - e.g. if this is a scroll bar paint or not. 
	 */
	public void paintPlot(D data, GraphicsContext graphicsContext, Rectangle rectangle, DetectionPlotProjector projector, int flag);
	
	/**
	 * Get the settings pane for the particular plot. This sits on the right hand side of the display
	 * inside a hiding pane. 
	 * @return a pane with controls for changing settings in a node. Returns null by default. 
	 */
	default Pane getSettingsPane() {
		return null;
	}


}
