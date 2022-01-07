package Array.sensors;

/**
 * Label for a datablock that has sensor data. If a datablock implements this, 
 * then its data units MUST implement ArraySensorDataUnit. 
 * @author dg50
 *
 */
public interface ArraySensorDataBlock {

	/**
	 * Ask if this data block has data of a specified field type. Not a guarantee that
	 * individual data units within the datablock will actually have the data, but they
	 * should have at least tried
	 * @param fieldType
	 * @return
	 */
	public boolean hasSensorField(ArraySensorFieldType fieldType);
	
	/**
	 * 
	 * @return a bitmap of streamers that may have information in the data within this data block. 
	 */
	public int getStreamerBitmap();
	
	/**
	 * Get a count sensor groups. This may be different to the streamer map bit count, e.g. some 
	 * systems may say to apply a single sensor set to multiple streamers, others may have one 
	 * group of sensors for each streamer, etc.  
	 * @return count of sensor groups. 
	 */
	public int getNumSensorGroups();
	
}
