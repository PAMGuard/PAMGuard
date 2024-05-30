package Array.layoutFX;

import Array.Streamer;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;


/**
 * Property class for a Streamer. Create property bindings for certain Streamer values which allows 
 * for much easier integration into UI components in JavaFX. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class StreamerProperty {
	
	/**
	 * The simple name property. 
	 */
	private SimpleStringProperty name = new SimpleStringProperty(); 

	private SimpleStringProperty reference = new SimpleStringProperty(); 

	private SimpleStringProperty origin = new SimpleStringProperty(); 

	/**
	 * Get the x, y and z. 
	 */
	private SimpleDoubleProperty x = new SimpleDoubleProperty(); 
	
	private SimpleDoubleProperty y = new SimpleDoubleProperty(); 

	private SimpleDoubleProperty z = new SimpleDoubleProperty(); 


	private Streamer streamer;

	private SimpleIntegerProperty streamerIDProperty = new SimpleIntegerProperty();

	public StreamerProperty(Streamer streamer) {
		setStreamer( streamer); 
	}
	
	public void setStreamer(Streamer streamer) {
		this.streamer = streamer; 
		name.setValue(streamer.getStreamerName());
		x.setValue(streamer.getX());
		y.setValue(streamer.getY());
		z.setValue(streamer.getZ());
		streamerIDProperty.setValue(streamer.getStreamerIndex());
		reference.setValue(streamer.getHydrophoneLocator() != null ? streamer.getHydrophoneLocator().getName() : "null");
		origin.setValue(streamer.getHydrophoneOrigin() != null ? streamer.getHydrophoneOrigin().getName() : "null");

	}
	
	public SimpleStringProperty getName() {
		return name;
	}

	public void setName(SimpleStringProperty name) {
		this.name = name;
	}

	public SimpleDoubleProperty getX() {
		return x;
	}

	public void setX(SimpleDoubleProperty x) {
		this.x = x;
	}

	public SimpleDoubleProperty getY() {
		return y;
	}

	public void setY(SimpleDoubleProperty y) {
		this.y = y;
	}

	public SimpleDoubleProperty getZ() {
		return z;
	}

	public void setZ(SimpleDoubleProperty z) {
		this.z = z;
	}


	public Streamer getStreamer() {
		return streamer;
	}

	/**
	 * Get the index property of the streamer. 
	 * @return the streamer index. 
	 */
	public SimpleIntegerProperty getID() {
		return streamerIDProperty;
	}

	/**
	 * Get the reference property. 
	 * @return the reference property. 
	 */
	public SimpleStringProperty getHydrophineLocator() {
		return reference;
	}


	/**
	 * Get the origin string property. 
	 * @return the origin string property. 
	 */
	public SimpleStringProperty getHydrophoneOrigin() {
		return origin;
	}



}
