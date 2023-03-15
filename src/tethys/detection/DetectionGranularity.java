package tethys.detection;

/**
 * Class to help define what the granularity of exported detections 
 * documents should be. The entire document will be in memory, so it
 * may be necessary to add many detections documents into the database 
 * for a single Deployment. 
 * @author dg50
 *
 */
public class DetectionGranularity {

	public enum GRANULARITY {NONE, BINARYFILE, TIME};
	
	/**
	 * Type of granularity. Are data all in one lump, split by binary file or by time. 
	 */
	GRANULARITY granularity = GRANULARITY.NONE;
	
	/**
	 * Granularity interval in seconds. Output system will try to round these
	 * to something with sensible boundaries. 
	 * This field is only needed when using the GRANULARITY.TIME option.  
	 */
	public long granularityIntervalSeconds;

	public DetectionGranularity(GRANULARITY granularity, long granularityIntervalSeconds) {
		super();
		this.granularity = granularity;
		this.granularityIntervalSeconds = granularityIntervalSeconds;
	}
	
	
}
