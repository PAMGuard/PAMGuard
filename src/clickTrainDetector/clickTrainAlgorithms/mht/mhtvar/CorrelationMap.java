package clickTrainDetector.clickTrainAlgorithms.mht.mhtvar;

import java.util.HashMap;

/**
 * Holds the UID of a click and the correlation value. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class CorrelationMap extends HashMap<Long, CorrelationValue>{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The UID of the master data unit the correlation map belongs to. 
	 */
	private long dataUnitUID;
	
	public CorrelationMap(long unitUID) {
		this.dataUnitUID = unitUID; 
	}
	
	/**
	 * Get the master unit UID. All correlations are with respect ot the master unit. 
	 * @return the master unit UID. 
	 */
	public long getDataUnitUID() {
		return dataUnitUID;
	}

	/**
	 * The master unit UID. 
	 * @param dataUnitUID - the unit UID. 
	 */
	public void setDataUnitUID(long dataUnitUID) {
		this.dataUnitUID = dataUnitUID;
	}


	

}
