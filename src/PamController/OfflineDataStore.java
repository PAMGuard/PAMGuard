package PamController;

import java.awt.Window;

import PamguardMVC.PamDataBlock;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import dataGram.DatagramManager;
import dataMap.OfflineDataMapPoint;
import pamScrollSystem.ViewLoadObserver;

/**
 * Interface implemented by PamControlledUnits which 
 * are capable of reloading and restoring data when operating in 
 * Viewer mode.
 * @author Doug Gillespie
 *
 */
public interface OfflineDataStore {

	/**
	 * Create a basic map of the data including first and 
	 * last times and some kind of data/unit time count 
	 * plus ideally some kind of start and stop time list
	 * of where there are gaps in the data. 
	 */
	public void createOfflineDataMap(Window parentFrame);
	
	/**
	 * Get the data source name
	 * @return data source name
	 */
	public String getDataSourceName();
	
	/**
	 * Get the data location. This may be a specific file, or might be a folder
	 * if data are in many files, a URI, etc. 
	 * @return store locations
	 */
	public String getDataLocation();
	
	/**
	 * Load data for a given datablock between two time limits. 
	 * @param dataBlock datablock owner of the data
	 * @param dataStart start time in milliseconds
	 * @param dataEnd end time in milliseconds
	 * @param loadObserver 
	 * @return true if load successful. 
	 */
	public boolean loadData(PamDataBlock dataBlock, OfflineDataLoadInfo offlineDataLoadInfo, ViewLoadObserver loadObserver);
	
	/**
	 * Save data previously loaded from the store during 
	 * offline viewing. 
	 * @param dataBlock datablock owner of the data
	 * @return true if saved or save not needed. False if an error prevents saving. 
	 */
	public boolean saveData(PamDataBlock dataBlock);

	/**
	 * Moved this function over from binary data store. 
	 * Many storage systems may not be able to do this, but some might !
	 * @param dataBlock
	 * @param dmp
	 * @return 
	 */
	public boolean rewriteIndexFile(PamDataBlock dataBlock, OfflineDataMapPoint dmp);

	/**
	 * @return the datagramManager
	 */
	public DatagramManager getDatagramManager();
	
}
