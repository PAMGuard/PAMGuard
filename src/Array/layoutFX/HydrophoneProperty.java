package Array.layoutFX;

import Array.Hydrophone;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;


/**
 * Property class for a hydrophone object.
 * 
 * @author Jamie Macaulay
 *
 */
public class HydrophoneProperty {
	
	SimpleDoubleProperty x = new SimpleDoubleProperty(); 
	
	SimpleDoubleProperty y = new SimpleDoubleProperty(); 

	SimpleDoubleProperty z = new SimpleDoubleProperty(); 

	SimpleIntegerProperty id = new SimpleIntegerProperty(); 

	public HydrophoneProperty(Hydrophone hydrophone) {
		hydrophone.getCoordinateError(0);
	}

	/**
	 * The x-coordinate property.
	 * @return the x coordintae property. 
	 */
	public SimpleDoubleProperty getX() {
		return x;
	}
	
	/**
	 * The y-coordinate property.
	 * @return the y coordintae property. 
	 */
	public SimpleDoubleProperty getY() {
		return y;
	}
	
	/**
	 * The z-coordinate property.
	 * @return the z coordintae property. 
	 */
	public SimpleDoubleProperty getZ() {
		return z;
	}

	public SimpleIntegerProperty getID() {
		return id;
	}

}
