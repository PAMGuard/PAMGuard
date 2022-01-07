package pamViewFX.fxNodes.pamScrollers.acousticScroller;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import dataPlotsFX.scrollingPlot2D.Plot2DColours;

/**
 * Handles the graphics for an acopustic scroller.  
 * @author Jamie Macaulay
 *
 */
public interface AcousticScrollerGraphics {
	
	/**
	 * 	Flag for a sample rate chnage. 
	 */
	public static final int SAMPLE_RATE_CHANGE = 0;

	/**
	 * The data block represented in the graphics.
	 * @return the data block. 
	 */
	public PamDataBlock getDataBlock(); 
	
	/**
	 * Add new data units. These will be used to create graphics
	 * @param rawData . A dataunit. Can be any type of data unit.
	 */
	public void addNewData(PamDataUnit rawData); 
	
	/**
	 * Repaint the canvas with the graphics.
	 */
	public void repaint();
	
	/**
	 * Name of the graphics.
	 * @return the graphics
	 */
	public String getName();

	/**
	 *Clear all data in the store. 
	 */
	public void clearStore();
	
	/**
	 * True if the data block requires offline loading of data. 
	 */
	public boolean orderOfflineData();
	
	/**
	 * Set colours for the datagram. 
	 * @param specColors
	 */
	public void setColors(Plot2DColours specColors);
	
	/**
	 * Set colours for the datagram. 
	 * @param specColors
	 */
	public Plot2DColours getColors();

	/**
	 * Notifies of updates form the acoustic scroller. 
	 * @param flag - the update flag.
	 */
	public void notifyUpdate(int flag); 

}
