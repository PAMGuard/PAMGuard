package dataPlotsFX.data;

import dataPlotsFX.projector.TDProjectorFX;
import javafx.scene.canvas.GraphicsContext;

/**
 * A plugin is used to display extra data for a TDDataInfo. 
 * <p>
 * This is usually used to add some data annotations or different plotting options 
 * for a particular data block where a bespoke TDDataInfoFX is not necessary. It is the 
 * responsibility of the module to add the plugin to the the TDDisplay.
 * <p>
 * Example: The click train detector module does not need to do a large amount of drawing
 * as super detections are already coloured appropriately. However some of the algorithms add 
 * additional draweing to the display and thus a plugin is more appropriate. 
 * @author Jamie Macaulay
 *
 */
public interface TDDataInfoPlugin {
	
	/**
	 * Get the data info class to add the plugin to. The plugin will be added to all these.
	 * @return the data info class. 
	 */
	public Class<?> getDataInfoClass();

	/**
	 * Paint the on the display
	 * @param plotNumber - the plot number
	 * @param g - the graphics context
	 * @param scrollStart - the start of the display
	 * @param tdProjector - the projector
	 * @param dataInfo - the data info. The plugin is added to all dataInfos of type getDataInfoClass();
	 * @return true if successfully painted. 
	 */
	public boolean repaint(int plotNumber, GraphicsContext g, long scrollStart, 
			TDProjectorFX tdProjector, TDDataInfoFX dataInfo); 
	
}
