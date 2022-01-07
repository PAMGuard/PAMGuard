package detectionPlotFX.layout;

import detectionPlotFX.projector.DetectionPlotProjector;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

/**
 * An interface to satisfy basic plots. Note that this does not have to be used in a DetectionDisplay
 * @author Jamie Macaulay
 *
 * @param <D> - the type of data that is input for plotting
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
	 * @return a pane with controls for changing settings in a node. 
	 */
	public Pane getSettingsPane();


}
