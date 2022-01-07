package videoRangePanel.layoutFX;

import java.io.File;
import java.util.ArrayList;

import javafx.scene.Node;
import javafx.scene.layout.Region;

/**
 * Pane which holds some type of visual media e.g. a video or photograph. 
 * @author Jamie Macaulay 
 *
 */
public interface VRImage {
	
	/**
	 * Get the control pane for the particular media type. This sits at the bottom of the screen and allows users
	 * to manipulate the media e.g. might contain play and pause controls. 
	 * @return the control pane
	 */
	public Region getControlPane(); 
	
	/**
	 * Get the image edit pane. This contains image/media editing controls. E.g. brightness or contrast controls might 
	 * go here. 
	 * @return the image edit pane
	 */
	public Region getImageEditPane(); 
	
	
	/**
	 * Set the current media. 
	 * @param currentFile - the current file for the media. 
	 * @return - true if loaded properly, otherwise false. 
	 */
	public boolean setMedia(File currentFile);

	/**
	 * The main node which holds the media. 
	 * @return node which holds the media view. 
	 */
	public Node getNode();
	
	/**
	 * Get readable metadata for the image/media
	 * @return a list of human readable metadata strings. 
	 */
	public ArrayList<String> getMetaData();
	
	/**
	 * Get the height of the current image
	 * @return the height of the current image
	 */
	public int getImageHeight();
	
	/**
	 * Get the width of the current image
	 * @return the width of the current image.
	 */
	public int getImageWidth();


}
