package PamController;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import PamguardMVC.PamRawDataBlock;
import dataMap.filemaps.OfflineFileServer;

/**
 * Generic data store for offline files. Works with OfflineFileServer and subclasses thereof. 
 * Was OfflineRawDataStore when just used for wav files. 
 * @author dg50
 *
 */
public interface OfflineFileDataStore extends OfflineDataStore {
	/**
	 * 
	 * @return The offline file server which will do the actual work
	 */
	public OfflineFileServer getOfflineFileServer() ;

	public PamDataBlock getRawDataBlock();
	
	public PamProcess getParentProcess();
	
	public String getUnitName();
	
}
