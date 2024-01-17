package PamController;

/**
 * Interface to define modules which can be considered as sensors of some sort. 
 * e.g. depth and orientation modules and the SoundTrap clickdetecotr
 * @author dg50
 *
 */
public interface PamSensor {

	public String getUnitName();
	
	public String getUnitType();
	
	public String getSensorDescription();
	
	public String getSensorId();
	
	
}
