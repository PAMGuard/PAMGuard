package Array.layoutFX;

import Array.Streamer;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;


/**
 * Property class for a Streamer. Create property bindings for certain Streamer values which allows 
 * for much easier integration into UI components such as JavaFX. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class StreamerProperty {
	
	private SimpleStringProperty name = new SimpleStringProperty(); 

	private SimpleDoubleProperty x,y,z = new SimpleDoubleProperty(); 

	private Streamer streamer;

	
	public StreamerProperty(Streamer streamer) {
		setStreamer( streamer); 
	}
	
	public void setStreamer(Streamer streamer) {
		this.streamer = streamer; 
		name.setValue(streamer.getStreamerName());
		x.setValue(streamer.getX());
		y.setValue(streamer.getY());
		z.setValue(streamer.getZ());
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

}
