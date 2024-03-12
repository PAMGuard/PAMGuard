package PamController;

/**
 * Functions for a data input store. There is a fair bit of overlap for this and 
 * OfflineDataStore, but the OfflineDataStore is really about stuff that can provide
 * data offline which needs mapping. This is specifically about data which will be input
 * during 'normal operation, i.e. sound acquisition and Tritech sonar data 
 * (a plugin, but coming down the tracks at us all). 
 * @author dg50
 * @see OfflineDataStore
 * @See DataOutputStore
 *
 */
public interface DataInputStore {
	
	/**
	 * Get information about the input store (e.g. start times of all files). 
	 * @param detail
	 * @return information about data input. 
	 */
	public InputStoreInfo getStoreInfo(boolean detail);

	/**
	 * Set an analysis start time. This might get called just before
	 * processing starts, in which case 
	 * @param startTime
	 * @return ok if no problems. 
	 */
	public boolean setAnalysisStartTime(long startTime);

	/**
	 * Very specific command handler for batch status which will only work 
	 * with the acquisition folderinputSystem or the tritech file processing. 
	 * @return
	 */
	public String getBatchStatus();

}
