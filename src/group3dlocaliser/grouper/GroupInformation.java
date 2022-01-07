package group3dlocaliser.grouper;

/**
 * Information about a channel group used by the DetectionGrouper
 * @author dg50
 *
 */
public class GroupInformation {

	public long lastSample;
	public int channelMap;
	
	/**
	 * @param channelMap
	 * @param lastSample
	 */
	public GroupInformation(int channelMap, long lastSample) {
		super();
		this.channelMap = channelMap;
		this.lastSample = lastSample;
	}
	

}
