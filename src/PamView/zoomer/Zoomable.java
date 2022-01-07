package PamView.zoomer;

import java.awt.event.MouseEvent;

/**
 * To be implemented by any display working with the 
 * Zoomer class. 
 * @author Doug Gillespie
 *
 */
public interface Zoomable {

	/**
	 * Can a zoom area be started ? <p> 
	 * This is called from the zoomer when a zoom area
	 * is about to start to check that there are not reasons
	 * for not starting the area. For instance if some other mouse
	 * event is to take priority over the zoom, e.g. clicking on a 
	 * detection which might initiate some other menu action. 
	 * @param mouseEvent
	 * @return true if zooming can start
	 */
	public boolean canStartZoomArea(MouseEvent mouseEvent);
	
	/**
	 * Called as the zoom shape changes via some sort of 
	 * mouse action. 
	 * @param zoomShape shape that's changing. 
	 */
	public void zoomShapeChanging(ZoomShape zoomShape);
	
	/**
	 * Called when a zoom shape has been completed.
	 * @param zoomShape zoom shape that's completed. 
	 */
	public void zoomPolygonComplete(ZoomShape zoomShape);
	
	/**
	 * 
	 * @return start (left most) value on the x axis scale
	 */
	public double getXStart();
	
	/**
	 * 
	 * @return x scale in pixels per unit
	 */
	public double getXScale();
	
	/**
	 * 
	 * @return range of x axis
	 */
	public double getXRange();
	/**
	 * 
	 * @return start (bottom most) value on the y axis scale
	 */
	public double getYStart();
	
	/**
	 * 
	 * @return y scale in pixels per unit
	 */
	public double getYScale();
	
	/**
	 * 
	 * @return the range of the y axis
	 */
	public double getYRange();
	
	/**
	 * @return the coordinate type - can be pretty much anything so long 
	 * as it is unique for different types of coordinate for that display, 
	 * e.g. the Click bearing time display so that zooming can only take 
	 * place in that coordinate system.
	 */
	public int getCoordinateType();
	
	/**
	 * Zoom to the bounds of a particular shape. 
	 * @param zoomShape shape to zoom to
	 */
	public void zoomToShape(ZoomShape zoomShape);

	/**
	 * @param mouseEvent
	 * @return
	 */
	boolean canClearZoomShape(MouseEvent mouseEvent);
	
}
