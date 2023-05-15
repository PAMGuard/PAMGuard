package PamController;

import PamController.fileprocessing.StoreStatus;

/**
 * Functions for a data output store. there is a fair bit of overlap for this and 
 * OfflineDataStore, but the OfflineDataStore is really about stuff that can provide
 * data offline which needs mapping. This is specifically about data which will be stored
 * during 'normal operation, i.e. binary and database modules. 
 * @author dg50
 * @see OfflineDataStore
 * @See DataInputStore
 *
 */
public interface DataOutputStore extends OfflineDataStore {

	/**
	 * Get the store status, i.e. does it exist, does it contain data, if so over what date range, 
	 * etc. 
	 * @param getDetail
	 * @return
	 */
	public StoreStatus getStoreStatus(boolean getDetail);
	
	/**
	 * Delete all data from a given time, in all data streams. 
	 * @param timeMillis time to delete from (anything >= this time)
	 * @return true if it seems to have worked OK. False if any errors (e.g. database or file system error). 
	 */
	public boolean deleteDataFrom(long timeMillis);
	
}
