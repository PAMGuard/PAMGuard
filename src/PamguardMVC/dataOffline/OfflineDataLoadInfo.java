package PamguardMVC.dataOffline;

import PamguardMVC.LoadObserver;
import PamguardMVC.PamObserver;

/**
 * Contains information required to load data from a file or other source into a datablock. 
 * @author Jamie Macaulay
 *
 */
public class OfflineDataLoadInfo implements Cloneable {
		
	/**
	 * If cancelled the thread will SIMPLY STOP
	 */
	public static int  PRIORITY_CANCEL_DESTROY=-1; 
	

	/**
	 * If cancelled the thread will automatically restart
	 */
	public static int  PRIORITY_CANCEL_RESTART=-1; 
	
	
	
	/**
	 * A current data observer. Each loaded data unit is past to the observer.
	 * THIS VALUE WILL CHANGE IN UPSTREAM PROCESSES. 
	 */
	private PamObserver currentDataObserver;
	
	/**
	 * This is the first observer set in the chain i.e. the original observer. This is required incase 
	 * the loading process needs to restart
	 */
	private PamObserver startObserver;
	

	/**
	 * This is the last observer e.g. Aquisition data block. 
	 */
	private PamObserver endObserver;

	/**
	 * Returns load progress information. 
	 */
	private LoadObserver loadObserver;
	
	/**
	 * The start time to load data from. This is the original requested data load time. 
	 * It will not change during a data load. 
	 */
	private long startMillis;
	
	/**
	 * The end time to load data to. 
	 */
	private long endMillis;
	
	/**
	 *Number of layers of datablock which should hang on to loaded data rather than delete it immediately. 
	 *THIS VALUE WILL CHANGE IN UPSTREAM PROCESSES. 
	 */
	private int loadKeepLayers;
	
	
	/**
	 *Number of layers of datablock which should hang on to loaded data rather than delete it immediately. T
	 *his is the initial value keeplayers was set to incase a reload of data is needed
	 */
	private int originalKeepLayers;

	/**
	 * Interupt data options. e.g. OFFLINE_DATA_INTERRUPT, OFFLINE_DATA_CANCEL, OFFLINE_DATA_WAIT
	 */
	private int interrupt;

	/**
	 *  Allow repeated loads of exactly the same data. 
	 */
	private boolean allowRepeats; 
	
	/**
	 * The priority of the load. Priority minimum is zero. Negative numbers are reserved for priority flags. 
	 * This is only used if threads are waiting in a que. 
	 */
	private int priority=PRIORITY_CANCEL_DESTROY; 


	/**
	 * 
	 * Information on the last load. This may be null if a load with this data has not been attempted and cancelled. 
	 * Any datablock specific loading info that might be needed. E.g. the sound aquisition module might keep a 
	 * byte number and file loclasation here. 
	 */
	private LoadPositionInfo lastLoadInfo;
	
	/**
	 * Changed by threads to cancel an order. 
	 */
	public volatile boolean cancel = false;

	
	/**
	 * Simple constructor for 
	 * @param currentStart
	 * @param currentEnd
	 */
	public OfflineDataLoadInfo(long currentStart, long currentEnd) {
		this.currentDataObserver = null;
		this.startObserver=null;
		this.endObserver = null;
		this.startMillis = currentStart;
		this.endMillis = currentEnd;
		this.loadKeepLayers = 0;
		originalKeepLayers=0; 
		this.allowRepeats = false;
	}
	
	public OfflineDataLoadInfo(PamObserver dataObserver, PamObserver endObserver, long startMillis, long endMillis, 
			int loadKeepLayers, boolean allowRepeats) {
		this.currentDataObserver = dataObserver;
		this.startObserver=dataObserver;
		this.endObserver = endObserver;
		this.startMillis = startMillis;
		this.endMillis = endMillis;
		this.loadKeepLayers = loadKeepLayers;
		this.originalKeepLayers = loadKeepLayers;
		this.allowRepeats = allowRepeats;
	}
	
	public OfflineDataLoadInfo(PamObserver dataObserver, LoadObserver loadObserver, long startMillis, long endMillis, 
			int loadKeepLayers, int interrupt, boolean allowRepeats) {
		this.currentDataObserver = dataObserver;
		this.startObserver=dataObserver;
		this.endObserver = null;
		this.loadObserver = loadObserver;
		this.startMillis = startMillis;
		this.endMillis = endMillis;
		this.loadKeepLayers = loadKeepLayers;
		this.originalKeepLayers = loadKeepLayers;
		this.interrupt=interrupt;
		this.allowRepeats = allowRepeats;
	}

	public OfflineDataLoadInfo() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Resets the load data for another data load. 
	 */
	public void reset(){
		this.loadKeepLayers=this.originalKeepLayers;
		this.cancel=false;
		this.currentDataObserver=this.startObserver;
	}

//	/**
//	 * The current state of the the thread. 
//	 */
//	public int currentState = STATE_RUN;
//	
//	/**
//	 * The state the thread should be in. 
//	 */
//	public int stateCommand = STATE_RUN;
	

	/**
	 * Get the current start millis. This is either the original requested start time or, if a data load has been 
	 * partially completed and lastLoadInfo is not null this will return the latest load time 
	 * @return the startMillis
	 */
	public long getStartMillis() {
		if (this.lastLoadInfo!=null) return lastLoadInfo.lastLoadMillis(); 
		else return startMillis;
	}
	
	/**
	 * Get the original start of the data load request. This will be the same as {@link getStartMillis()}
	 * @return the start of the data load in millis datenum
	 */
	public long getOriginalStartMillis() {
		return startMillis;
	}


	/**
	 * @param startMillis the startMillis to set
	 */
	public void setStartMillis(long startMillis) {
		this.startMillis = startMillis;
	}
	
	/**
	 * @return the endMillis
	 */
	public long getEndMillis() {
		return endMillis;
	}

	/**
	 * @param endMillis the endMillis to set
	 */
	public void setEndMillis(long endMillis) {
		this.endMillis = endMillis;
	}
	
	/**
	 * @return the dataObserver
	 */
	public PamObserver getCurrentObserver() {
		return currentDataObserver;
	}

	/**
	 * @param dataObserver the dataObserver to set
	 */
	public void setCurrentObserver(PamObserver dataObserver) {
		this.currentDataObserver = dataObserver;
	}
	
	/**
	 * @return the endObserver
	 */
	public PamObserver getEndObserver() {
		return endObserver;
	}

	/**
	 * @param endObserver the endObserver to set
	 */
	public void setEndObserver(PamObserver endObserver) {
		this.endObserver = endObserver;
	}
	
	/**
	 * @return the loadObserver
	 */
	public LoadObserver getLoadObserver() {
		return loadObserver;
	}

	/**
	 * @param loadObserver the loadObserver to set
	 */
	public void setLoadObserver(LoadObserver loadObserver) {
		this.loadObserver = loadObserver;
	}
	
	/**
	 * @return the loadKeepLayers
	 */
	public int getLoadKeepLayers() {
		return loadKeepLayers;
	}

	/**
	 * @param loadKeepLayers the loadKeepLayers to set
	 */
	public void setLoadKeepLayers(int loadKeepLayers) {
		this.loadKeepLayers = loadKeepLayers;
	}
	
	/**
	 * @return the lastLoadInfo
	 */
	public LoadPositionInfo getLastLoadInfo() {
		return lastLoadInfo;
	}

	/**
	 * @param lastLoadInfo the lastLoadInfo to set
	 */
	public void setLastLoadInfo(LoadPositionInfo lastLoadInfo) {
		this.lastLoadInfo = lastLoadInfo;
	}

	/**
	 * 
	 * @return alow repeats
	 */
	public boolean getAllowRepeats() {
		 return this.allowRepeats;
	}
	
	/**
	 * @return the interrupt
	 */
	public int getInterrupt() {
		return interrupt;
	}

	/**
	 * @param interrupt the interrupt to set
	 */
	public void setInterrupt(int interrupt) {
		this.interrupt = interrupt;
	}
	
	
	/**
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * @param priority the priority to set
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}

	@Override
	public OfflineDataLoadInfo clone() {
		try {
			return (OfflineDataLoadInfo) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}



}
