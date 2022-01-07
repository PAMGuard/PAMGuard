package Array.sensors;

/**
 * Types of value which might occur in streamer data for height, head, pitch and roll
 * where values can be fixed, default or from a sensor 
 * default not applicable to depth, 0 for pitch and roll, GPS head for heading 
 * @author dg50
 *
 */
public enum ArrayParameterType {

	FIXED, DEFAULT, SENSOR;
	
}
