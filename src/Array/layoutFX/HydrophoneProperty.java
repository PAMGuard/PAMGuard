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
	
	SimpleDoubleProperty xErr = new SimpleDoubleProperty(); 
	
	SimpleDoubleProperty yErr = new SimpleDoubleProperty(); 

	SimpleDoubleProperty zErr = new SimpleDoubleProperty(); 

	SimpleIntegerProperty id = new SimpleIntegerProperty();

	private Hydrophone hydrophone; 

	public HydrophoneProperty(Hydrophone hydrophone) {
		setHydrophone(hydrophone);
	}
	
	public void setHydrophone(Hydrophone hydrophone) {
		this.hydrophone = hydrophone;
		
		
		x .set(hydrophone.getX());
		y .set(hydrophone.getY());
		z .set(hydrophone.getZ());
		
		xErr .set(hydrophone.getdX());
		yErr .set(hydrophone.getdY());
		zErr .set(hydrophone.getdZ());
		
		id.set(hydrophone.getID());
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

	public Hydrophone getHydrophone() {
		//incase table data changes. 
		this.hydrophone.setID(this.id.get());
		this.hydrophone.setX(x.get());
		this.hydrophone.setY(y.get());
		this.hydrophone.setZ(z.get());
		this.hydrophone.setdX(xErr.get());
		this.hydrophone.setdY(yErr.get());
		this.hydrophone.setdZ(xErr.get());

		return hydrophone;
	}

	/**
	 * The x-coordinate property.
	 * @return the x coordintae property. 
	 */
	public SimpleDoubleProperty getXErr() {
		return xErr;
	}
	
	/**
	 * The y-coordinate property.
	 * @return the y coordintae property. 
	 */
	public SimpleDoubleProperty getYErr() {
		return yErr;
	}
	
	/**
	 * The z-coordinate property.
	 * @return the z coordintae property. 
	 */
	public SimpleDoubleProperty getZErr() {
		return zErr;
	}

}
