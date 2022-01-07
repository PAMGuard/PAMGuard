package PamView.zoomer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import PamView.paneloverlay.overlaymark.OverlayMark;
import PamView.paneloverlay.overlaymark.OverlayMarker;

/**
 * Interface to support a variety of zoomable shapes.
 * <p> I currently can't think of anything but a 
 * rectangle and an arbitrary polygon, but you never know !
 * @author Doug Gillespie
 *
 */
public abstract class ZoomShape {

	private int coordinateType;
	
	private Zoomer zoomer;
	
	private boolean closed = false;

	protected Color outlineColor=Color.LIGHT_GRAY;

	protected Color fillColor=new Color(50,50,50,50); 
	
	/**
	 * @param zoomer
	 * @param coordinateType
	 */
	public ZoomShape(Zoomer zoomer, int coordinateType) {
		super();
		this.zoomer = zoomer;
		this.coordinateType = coordinateType;
	}
	
	/**
	 * 
	 * @return the outer bounds of the zoom shape in pixels. 
	 */
	public abstract Rectangle getBounds(Component component);
	
	/**
	 * Draw the shape using the given graphics handle on the
	 * given component.  
	 * @param g graphics
	 * @param component component to draw on
	 * @param beforeOther 
	 * @return outer bounds of drawn region (should be same as getBounds());
	 */
	public abstract Rectangle drawShape(Graphics g, Component component, boolean beforeOther);
	

	/**
	 * New point to add to the shape - or update old point if it's a
	 * rectangle. 
	 * @param x x coordinate in display units such as time or bearing, NOT PIXELS
	 * @param y y coordinate in display units such as time or bearing, NOT PIXELS
	 */
	public abstract void newPoint(double x, double y);
	
	/**
	 * Shape is complete for whatever reason. 
	 */
	public void closeShape() {
		closed = true;
	}

	/**
	 * @return the closed
	 */
	public boolean isClosed() {
		return closed;
	}
	
	/**
	 * 
	 * @return true if the object should no longer be displayed after zooming
	 * <p>Basically true for rectangles, false for polygons. 
	 */
	public abstract boolean removeOnZoom();
	
	/**
	 * Shape contains the point pt. 
	 * <p> Note that these are in screen pixel coordinates. 
	 * @param Component component
	 * @param pt point
	 * @return true if the point is within the shape. 
	 */
	public abstract boolean containsPoint(Component component, Point pt);
	
	/**
	 * 
	 * @return the lowest x value
	 */
	public abstract double getXStart();
	
	/**
	 * 
	 * @return the difference between the minimum and maximum
	 * x values. 
	 */
	public abstract double getXLength(); 
	
	/**
	 * 
	 * @return the lowest y value
	 */
	public abstract double getYStart();
	
	/**
	 * 
	 * @return the difference between the minimum and maximum
	 * y values. 
	 */
	public abstract double getYLength(); 

	/**
	 * Get the type of coordinate (only needs to be specific
	 * within a particular display, not more generally. 
	 * @return coordinate type. 
	 */
	public int getCoordinateType() {
		return coordinateType;
	}

	public Zoomer getZoomer() {
		return zoomer;
	}
	
	public void setZoomShapeOutline(Color outlineColor){
		this.outlineColor=outlineColor;
	}
	
	public void setZoomShapeFill(Color fillColor){
		this.fillColor=fillColor;
	}
	
	/**
	 * Convert the obsolete zoom shapes into newer overlay marks. 
	 * @param overlayMarker
	 * @return
	 */
	public abstract OverlayMark zoomShapeToOverlayMark(OverlayMarker overlayMarker);
}
