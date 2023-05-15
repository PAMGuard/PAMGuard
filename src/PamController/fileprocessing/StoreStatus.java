package PamController.fileprocessing;

import java.io.File;

import PamController.OfflineDataStore;

/**
 * Class to carry information about an OfflineDataStore. Used when restarting offline
 * processing to help work out if we should overwrite, start again, etc.  
 * @author dg50
 *
 */
abstract public class StoreStatus {
	
	public static final int STATUS_MISSING = 1;
	
	public static final int STATUS_EMPTY = 2;
	
	public static final int STATUS_HASDATA = 3;

	private OfflineDataStore offlineDataStore;
	
	/**
	 * Time of first data, may be null if detail not asked for or if 
	 * hasData is false. 
	 */
	private Long firstDataTime; 
	
	/**
	 * Time of last data, may be null if detail not asked for or if 
	 * hasData is false. 
	 */
	private Long lastDataTime;
	
	/**
	 * General status flag. 
	 */
	private int storeStatus;

	public StoreStatus(OfflineDataStore offlineDataStore) {
		this.offlineDataStore = offlineDataStore;
	}
	
	/**
	 * Get the amount of free space for this storage. 
	 * @return free space in bytes. 
	 */
	public abstract long getFreeSpace();

	public long getFreeSpace(String currDir) {
		if (currDir == null) {
			return 0;
		}
		File dirFile = new File(currDir);
		long space = 0;
		try {
			space = dirFile.getUsableSpace();
		}
		catch (SecurityException e) {
			System.out.printf("Security exception getting space for %s: \n%s\n", currDir, e.getMessage());
		}
		return space;
	}

	/**
	 * @return the firstDataTime
	 */
	public Long getFirstDataTime() {
		return firstDataTime;
	}

	/**
	 * @param firstDataTime the firstDataTime to set
	 */
	public void setFirstDataTime(Long firstDataTime) {
		this.firstDataTime = firstDataTime;
	}

	/**
	 * @return the lastDataTime
	 */
	public Long getLastDataTime() {
		return lastDataTime;
	}

	/**
	 * @param lastDataTime the lastDataTime to set
	 */
	public void setLastDataTime(Long lastDataTime) {
		this.lastDataTime = lastDataTime;
	}

	/**
	 * @return the storeStatus
	 */
	public int getStoreStatus() {
		return storeStatus;
	}

	/**
	 * @param storeStatus the storeStatus to set
	 */
	public void setStoreStatus(int storeStatus) {
		this.storeStatus = storeStatus;
	}

	/**
	 * @return the offlineDataStore
	 */
	public OfflineDataStore getOfflineDataStore() {
		return offlineDataStore;
	}
	

}
