package analoginput;

/**
 * Descriptions of a module that will use analog data. 
 * Gets used in dialog construction
 * @author dg50
 *
 */
public interface AnalogSensorUser {

	/**
	 * Get a list of useful names for the channels. 
	 * @return List of channel names
	 */
	public SensorChannelInfo[] getChannelNames();
	
	/**
	 * 
	 * @return A name for the data user
	 */
	public String getUserName();
	
	
}
