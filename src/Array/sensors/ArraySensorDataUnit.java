package Array.sensors;


/**
 * this is nominally an add on to data units, but we do also add it to GpsData so 
 * that GpsData and StremerDataUnits can both be passed to array graphics. 
 * @author dg50
 *
 */
public interface ArraySensorDataUnit {

	/**
	 * Get a value for a specified field for a specified streamer. 
	 * @param streamer streamer index
	 * @param fieldtype Depth, heading, pitch or roll. 
	 * @return Value in metres or degrees (not radians)
	 */
	public Double getField(int streamer, ArraySensorFieldType fieldtype);
	
	/**
	 * Get the time in milliseconds
	 * @return time in milliseconds. 
	 */
	public long getTimeMilliseconds();
	
}
