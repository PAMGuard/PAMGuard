package videoRangePanel;

import java.awt.Point;

/**
 * Main pane to hold image and controls. 
 * @author Jamie Macaulay 
 *
 */
public interface VRPane {

	/**
	 * Refresh the image 
	 */
	public void repaint();

	/*
	 *Get the width of an image in pixels  
	 */
	public int getImageWidth();

	/*
	 *Get the height of an image in pixels  
	 */
	public int getImageHeight();
	
	/**
	 * Calculate the location on an image given screen co-ordinates. 
	 * @param point - location on image 
	 * @return the location on the screen. 
	 */
	public Point imageToScreen(Point point);

	/**
	 * Calculate the screen location given the locations on the image  
	 * @param point - location on screen
	 * @return the location on the image in pixels 
	 */
	public Point screenToImage(Point point);

	/**
	 * Called whenever a new image is loaded. 
	 */
	public void newImage();

}
