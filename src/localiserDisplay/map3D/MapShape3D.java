package localiserDisplay.map3D;

import javafx.scene.Group;

/**
 * A layer which is added to a 3D map. 
 * @author Jamie Macaulay 
 *
 */
public interface MapShape3D {
	
	/**
	 * Get the name of the shape type e.g. Bathymetry
	 * @return the name of the shape type. 
	 */
	public String getShapeType();
	
	
	/**
	 * Get the shape to be added to the 3D graph. 
	 * @return the shape to add the Map. 
	 */
	public Group getShape(); 
	
	

}
