package userDisplayFX;

import java.io.Serializable;

/**
 * Parameters for a display node. 
 * <p>
 * This holds information on where the node is and crucially, 
 * which tab it belongs to so that the display can be initialised properly. 
 * @author Jamie Macaulay 
 *
 */
public class UserDisplayNodeParams implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;
	
	/**
	 * The name of the tab that the display belongs to. Note that tabs 
	 */
	public String tabName = null; 


	/**
	 * The position of the display in X relative to container. 
	 */
	public double positionX = 0;
	
	/**
	 * The position of the display in X relative to container. 
	 */
	public double positionY = 0; 

	/**
	 * 
	 * The size of X- this is -1 initially (JavaFX default) to indicate no size has been SET. 
	 */
	public double sizeX = -1;
	
	/**
	 * The size of Y
	 */
	public double sizeY= -1; 

}
