package pamScrollSystem;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingWorker;

import PamController.AWTScheduler;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamGUIManager;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import PamguardMVC.superdet.SuperDetDataBlock;
import dataMap.DataMapControl;
import dataMap.OfflineDataMap;
import generalDatabase.SQLLogging;
import generalDatabase.SuperDetLogging;
import offlineProcessing.superdet.OfflineSuperDetFilter;
import pamViewFX.pamTask.PamTaskUpdate;

public class ViewerScrollerManager extends AbstractScrollManager implements PamSettings {

	private boolean initialisationComplete;

	private volatile boolean intialiseLoadDone = false;

	private StoredScrollerData oldScrollerData = new StoredScrollerData();

	private boolean currentScrollInitialisation;

	public ViewerScrollerManager() {
		PamSettingManager.getInstance().registerSettings(this);
	}

	/* (non-Javadoc)
	 * @see pamScrollSystem.AbstractScrollManager#addPamScroller(pamScrollSystem.AbstractPamScroller)
	 */
	@Override
	public void addPamScroller(AbstractPamScroller pamScroller) {
		super.addPamScroller(pamScroller);
		if (oldScrollerData != null && initialisationComplete) {
			PamScrollerData oldData = oldScrollerData.findScrollerData(pamScroller);
			if (oldData != null) {
				pamScroller.scrollerData = oldData.clone();
			}
		}
	}


	@Override
	public void moveInnerScroller(AbstractPamScroller scroller, long newValue) {
		//Debug.out.println("Move inner scroller " + newValue); 
		AbstractPamScroller aScroller;
		if (oldScrollerData.coupleAllScrollers) {
			followCoupledScroller(scroller);
		}
		for (int i = 0; i < pamScrollers.size(); i++) {
			aScroller = pamScrollers.get(i);
			if (aScroller == scroller) {
				continue;
			}
			aScroller.anotherScrollerMovedInner(newValue);
		}
	}

	@Override
	public void moveOuterScroller(AbstractPamScroller scroller, long newMin,
			long newMax) {
		//Debug.out.println("Move outer scroller " + newMin + "  " + newMax); 
		AbstractPamScroller aScroller;
		for (int i = 0; i < pamScrollers.size(); i++) {
			aScroller = pamScrollers.get(i);
			if (aScroller == scroller) {
				continue;
			}
			aScroller.anotherScrollerMovedOuter(newMin, newMax);
		}
		if (!currentScrollInitialisation) {
			loadData(false);
		}
	}
	

	private volatile boolean loaderRunning = false;

	private DataLoader dataLoader;

	@Override
	public void reLoad() {
		loadData(false);
	}

	/**
	 * Wait for the data loader to complete. 
	 * 
	 * @param timeOut maximum time to wait for in milliseconds. Enter 0 to wait forever.  
	 * @return true if data loader is not running or stops, false if 
	 * a timeout occurs. 
	 */
//	public boolean waitForLoader(long timeOut) {
//		long now = System.currentTimeMillis();
//		try {
//			while (loaderRunning) {
//				Thread.sleep(10);
//				if (timeOut > 0) {
//					if (System.currentTimeMillis() > now+timeOut) {
//						break;
//					}
//				}	
//			}
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return (!loaderRunning);
//	}
	/**
	 * loads data after a scroller has changed. 
	 * @param immediateLoad 
	 */
	public synchronized void loadData(boolean immediateLoad) {
		//Debug.out.println("Viewer scroll manager: " + immediateLoad); 
		/**
		 * will need to stop this getting called multiple times
		 * when several scroll bars move. 
		 */
		if (loaderRunning) {
			//			return;
			//			if (dataLoader != null) {
			//				return;
			//			}
			//			dataLoader.cancel(true);
		}
		// checks what everyone wants and needs, then loads the
		// appropriate data. 
		/*
		 *  first loop through data blocks, then for
		 *  every data block loop through all scrollers and 
		 *  for srollers which use that data block work out
		 *  the min and max times for data in that block then 
		 *  load the data.  
		 */
		ArrayList<PamDataBlock> dataBlocks = PamController.getInstance().getDataBlocks();
		ArrayList<DataLoadQueData> dataLoadQueue = new ArrayList<DataLoadQueData>();
		//		dataLoadQueu.clear();
		for (int i = 0; i < dataBlocks.size(); i++) {
			checkLoadLimits(dataLoadQueue, dataBlocks.get(i));
		}
		if (dataLoadQueue.size() > 0) {
			loaderRunning = true;
			if (immediateLoad) {
				loadDataQueue(dataLoadQueue);
			}
			else {
				scheduleDataQueue(dataLoadQueue);
			}
		}
	}
	
	/**
	 * Load the data queue immediately in the current thread. 
	 * @param dataLoadQueue
	 */
	private void loadDataQueue(ArrayList<DataLoadQueData> dataLoadQueue) {
		int n = dataLoadQueue.size();
		for (int i = 0; i < n; i++) {
			loadDataQueueItem(dataLoadQueue.get(i), i, null);
		}
		loadSubformData(dataLoadQueue, null);
		loadDone();
	}

	/**
	 * Load the data from a single object in the queue. 
	 * Generally called from within the worker thread. 
	 * Note that any data units with a starting load time of <=0 are simply not loaded.  This is to prevent potential issues
	 * later with algorithms not expecting a negative date; however, this also means that PAMGuard cannot process pre-1970 data.
	 * @param dataLoadQueData
	 * @param i 
	 */
	private void loadDataQueueItem(DataLoadQueData dataLoadQueData, int queuePosition, ViewLoadObserver loadObserver) {
		PamDataBlock dataBlock = dataLoadQueData.getPamDataBlock();
//		if (dataBlock instanceof OfflineEventDataBlock) {
//			System.out.println("in loadDataQueueItem" + dataBlock);
//		}
		
		// 2019-11-12 add a check of the counts to this 'if' statement as well.  If the data start time is negative
		// (which can indicate that this is a 'special' data block and we should be loading all of it rather than just
		// what is within the scroller time range) and we haven't loaded anything yet, go ahead and load.  But if there
		// are data units, it means the info has already been loaded so don't bother loading again.
		// 2020-01-28 this fixed the problem with DetectionGroupLocaliser data not getting loaded, but Streamer data is still broken.
		// Correct this by changing the default min time from Long.MIN_VALUE to 1 in AbstractScrollManager.getSpecialLoadTimes.  This
		// affects DetectionGroupLocaliser, StreamerData, AcquisitionProcess and IMUProcess (anything that uses the AbstractScrollManager.SpecialDataBlockInfo
		// class without explicitly specifying time limits
		if ((dataLoadQueData.getDataStart() <= 0 || dataLoadQueData.getDataStart() == Long.MAX_VALUE)
			&& dataBlock.getUnitsCount() > 0) {
			return;
		}
		dataLoadQueData.getPamDataBlock().loadViewerData(new OfflineDataLoadInfo(dataLoadQueData.getDataStart(),
				dataLoadQueData.getDataEnd()), loadObserver);
	}
	/**
	 * Schedule the data queue to be loaded in a separate thread. 
	 * @param dataLoadQueue
	 */
	private void scheduleDataQueue(ArrayList<DataLoadQueData> dataLoadQueue) {
		AWTScheduler.getInstance().scheduleTask(dataLoader = new DataLoader(dataLoadQueue));
	}
	
	/**
	 * Go through the list of loaded data blocks and see if there are any that have a subform
	 * 
	 * @param dataLoadQueue
	 * @param dataLoader 
	 */
	private void loadSubformData(ArrayList<DataLoadQueData> dataLoadQueue, DataLoader dataLoader) {
		for (int i=0; i<dataLoadQueue.size(); i++) {
			PamDataBlock dataBlock = dataLoadQueue.get(i).getPamDataBlock();
			if (!(dataBlock instanceof SuperDetDataBlock)) {
				continue;
			}
//			System.out.println("Load subform data for datablock " + i + ": " + dataBlock.getLongDataName());
			if (dataLoader != null) {
				LoadQueueProgressData lpd = new LoadQueueProgressData("Database", 
						dataBlock.getDataName(), 
						dataLoadQueue.size(), i, LoadQueueProgressData.STATE_LINKINGSUBTABLE, 0, 0, 0, 0);
				dataLoader.pubPublish(lpd);
			}
			((SuperDetDataBlock) dataBlock).reattachSubdetections(dataLoader);
//			System.out.println("Completed subform data for datablock " + i + ": " + dataBlock.getLongDataName());
		}
	}

	
	private void loadDone() {
		for (int i = 0; i < pamScrollers.size(); i++) {
			pamScrollers.get(i).notifyRangeChange();
		}
		PamController.getInstance().notifyModelChanged(PamControllerInterface.OFFLINE_DATA_LOADED);
	}

	/**
	 * Check the required load limits for a data block which 
	 * may be being used my multiple scrollers. 
	 * 
	 * @param pamDataBlock data block to check
	 */
	private void checkLoadLimits(ArrayList<DataLoadQueData> dataLoadQueue, PamDataBlock pamDataBlock) {
		long minTime = Long.MAX_VALUE;
		long maxTime = Long.MIN_VALUE;
		AbstractPamScroller aScroller;
		boolean used = false;

		for (int i = 0; i < this.pamScrollers.size(); i++) {
			aScroller = pamScrollers.get(i);
			if (aScroller.isDataBlockUsed(pamDataBlock) || aScroller.isInSpecialList(pamDataBlock)) {
				long[] loadTimes = aScroller.getSpecialLoadTimes(pamDataBlock, aScroller.getMinimumMillis(), aScroller.getMaximumMillis()); 
				/*
				 * Needed to change the logic here, since was going wrong if one scroller had a shorter load time. 
				 * Now a straight forward 3-way maximum. 
				 */
				if (loadTimes != null) {
					minTime = Math.min(minTime, loadTimes[0]);
					maxTime = Math.max(maxTime, loadTimes[1]);
				}
				minTime = Math.min(minTime, aScroller.getMinimumMillis());
				maxTime = Math.max(maxTime, aScroller.getMaximumMillis());
				
				used = true;
				
			}
		}
		if (pamDataBlock.getDataName().contains("Gemini Target") ) {
			System.out.println("Gemini Tracks");
		}
		if (used) {
//			dataLoadQueue.add(new DataLoadQueData(pamDataBlock, minTime, maxTime, hasSubFormData(pamDataBlock)));
			addToDataQueue(dataLoadQueue, pamDataBlock, minTime, maxTime, hasSubFormData(pamDataBlock));
			/*
			 * Also need to do this for any super detection datablock(s), which may or may not already 
			 * be in the dataloadQueue, and may or may not be associated with this scroller. Also note that a superblock 
			 * may already be in the load que, in which case AND the times. 
			 */
			ArrayList<SuperDetDataBlock> superBlocks = OfflineSuperDetFilter.findPossibleSuperDetections(pamDataBlock);
			for (PamDataBlock aBlock : superBlocks) {
				addToDataQueue(dataLoadQueue, pamDataBlock, minTime, maxTime, true);
//				DataLoadQueData exItem = findLoadQueueData(dataLoadQueue, aBlock);
//				if (exItem != null) {
//					// expand an existing item as necessary
//					exItem.setDataStart(Math.min(exItem.getDataStart(), minTime));
//					exItem.setDataEnd(Math.max(exItem.getDataEnd(), maxTime));
//				}
//				else {
//					// or add a new item. 
//					dataLoadQueue.add(new DataLoadQueData(aBlock, minTime, maxTime, true));
//				}
			}
		}
	}
	
	/**
	 * Add to data queue, being aware that the block may already be in the queue if it was included from a sub detections super
	 * detection list. 
	 * @param dataLoadQueue
	 * @param pamDataBlock
	 * @param minTime
	 * @param maxTime
	 * @param hasSubData
	 */
	private void addToDataQueue(ArrayList<DataLoadQueData> dataLoadQueue, PamDataBlock pamDataBlock, long minTime, long maxTime, boolean hasSubData) {
		DataLoadQueData exItem = findLoadQueueData(dataLoadQueue, pamDataBlock);
		if (exItem != null) {
			// expand an existing item as necessary
			exItem.setDataStart(Math.min(exItem.getDataStart(), minTime));
			exItem.setDataEnd(Math.max(exItem.getDataEnd(), maxTime));
			exItem.setHasSubTable(true);
		}
		else {
			// or add a new item. 
			dataLoadQueue.add(new DataLoadQueData(pamDataBlock, minTime, maxTime, true));
		}
	}
	
	/**
	 * Find an existing entry for a data block in the load queue
	 * @param loadQueue
	 * @param dataBlock
	 * @return
	 */
	private DataLoadQueData findLoadQueueData(ArrayList<DataLoadQueData> loadQueue, PamDataBlock dataBlock) {
		for (DataLoadQueData dlqd : loadQueue) {
			if (dlqd.getPamDataBlock() == dataBlock) {
				return dlqd;
			}
		}
		return null;
	}
	

	/**
	 * 
	 * @param dataBlock
	 * @return true if a datablock has subform data. 
	 */
	private boolean hasSubFormData(PamDataBlock dataBlock) {
		SQLLogging logging = dataBlock.getLogging();
		if (logging == null) {
			return false;
		}
		return (logging instanceof SuperDetLogging);
	}
	
	
	class DataLoader extends SwingWorker<Integer, LoadQueueProgressData> implements ViewLoadObserver{

		private ArrayList<DataLoadQueData> dataLoadQueue;

		private LoadingDataDialog loadingDataDialog;

		private volatile boolean emergencyStop = false;
		
		private volatile String currentDataName;
		
		private volatile String storeName;

		public DataLoader(ArrayList<DataLoadQueData> dataLoadQueue) {
			super();
//			System.out.printf("Makeing dataloader with %d items, inicomplete = %s\n",
//					dataLoadQueue.size(),
//					new Boolean(PamController.getInstance().isInitializationComplete()));
			this.dataLoadQueue = dataLoadQueue;
		}

		@Override
		protected Integer doInBackground() throws Exception {
			try {
				int nDone = 0;
				/**
				 * First load all the data which has subtable detections
				 */
				for (int i = 0; i < dataLoadQueue.size(); i++) {
					DataLoadQueData queueItem = dataLoadQueue.get(i);
					if (!queueItem.isHasSubTable()) {
						continue;
					}
					OfflineDataMap priMap = dataLoadQueue.get(i).getPamDataBlock().getPrimaryDataMap();
					if (priMap != null) {
						storeName = priMap.getDataMapName();
					}
					else {
						storeName = null;
					}
					currentDataName = dataLoadQueue.get(i).getPamDataBlock().getDataName();
					LoadQueueProgressData lpd = new LoadQueueProgressData(storeName, 
							currentDataName, 
							dataLoadQueue.size(), nDone++, 0, 0, 0, 0, 0);
					publish(lpd);
					loadDataQueueItem(queueItem, i, this);
					/**
					 * Now go through every item which was loaded so far and 
					 * then check to see that any required sub detections are
					 * about to be loaded. 
					 */
//					ArrayList subTableData = queueItem.getPamDataBlock().getSubtableData();
					/*
					 * Need to 
					 * 1. Get a list of datablock, 
					 * 2. Find if they are in the rest of the queueItem list
					 * 3. If so, then check load limits are the same
					 * 4. If not, then add to the list before proceeding below. 
					 * All very complicated !!!!!!!!
					 */
				}
				
				/**
				 *
				 * Then load all the data that doesn't have sub table detections. 
				 */
				for (int i = 0; i < dataLoadQueue.size(); i++) {
					DataLoadQueData queueItem = dataLoadQueue.get(i);
					if (queueItem.isHasSubTable()) {
						continue;
					}
					LoadQueueProgressData lpd = new LoadQueueProgressData(storeName, 
							dataLoadQueue.get(i).getPamDataBlock().getDataName(), 
							dataLoadQueue.size(), nDone++, 0, 0, 0, 0, 0);
					publish(lpd);
					loadDataQueueItem(queueItem, i, this);
				}
				loadSubformData(dataLoadQueue, this);
				// need to set this here so that waitForLoader() can execute in the AWT thread. 
				loaderRunning = false;
			}
			catch (Exception e){
				System.out.println("Error in Viewer Scroller data loader " + e.getMessage());
				e.printStackTrace();
			}
			LoadQueueProgressData lpd = new LoadQueueProgressData(storeName, 
					"Data Load Complete, updating displays", 
					dataLoadQueue.size(), dataLoadQueue.size(), 0, 0, 0, 0, 0);
			publish(lpd);
			PamController.getInstance().notifyModelChanged(PamControllerInterface.DATA_LOAD_COMPLETE);
			return null;
		}
		
		public void pubPublish(LoadQueueProgressData loadQueueProgressData) {
			publish(loadQueueProgressData);
		}

		@Override
		protected void done() {
			super.done();
			loaderRunning = false;
			if (PamGUIManager.isSwing()) {
				if (loadingDataDialog != null) {
					loadingDataDialog.closeLater();
					//				loadingDataDialog.setVisible(false);
				}
			}
			loadDone();
			PamController.getInstance().notifyTaskProgress(new LoadQueueProgressData(PamTaskUpdate.STATUS_DONE));

		}

		@Override
		protected void process(List<LoadQueueProgressData> chunks) {
			if (PamGUIManager.isSwing()) {
				if (loadingDataDialog == null) {
					loadingDataDialog = LoadingDataDialog.showDialog(PamController.getMainFrame());
				}
				if (loadingDataDialog != null) {
					for (LoadQueueProgressData lpd:chunks) {
						loadingDataDialog.setData(lpd);
					}
					emergencyStop = loadingDataDialog.shouldStop();
				}
			}
			else {
				for (LoadQueueProgressData lpd:chunks) {
					PamController.getInstance().notifyTaskProgress(lpd);
				}
				//TODO- how to return an emergency stop form seperate GUI?
			}
		}

		@Override
		public void sayProgress(int state, long loadStart, long loadEnd,
				long lastTime, int nLoaded) {
			LoadQueueProgressData lpd = new LoadQueueProgressData(storeName, 
						currentDataName, 0, 0, state, loadStart, loadEnd, lastTime, nLoaded);
				publish(lpd);
			
		}

		/* (non-Javadoc)
		 * @see pamScrollSystem.ViewLoadObserver#cancelLoad()
		 */
		@Override
		public boolean cancelLoad() {
			return emergencyStop ;
		}


	}

	private DataMapControl findDataMapControl() {
		return DataMapControl.getDataMapControl();
	}

	@Override
	public long checkMaximumTime(long requestedTime) {
		DataMapControl dmc = findDataMapControl();
		if (dmc == null) {
			return 0;
		}
		return Math.min(dmc.getLastTime(), requestedTime);
	}

	@Override
	public long checkMinimumTime(long requestedTime) {
		DataMapControl dmc = findDataMapControl();
		if (dmc == null) {
			return 0;
		}
		return Math.max(dmc.getFirstTime(), requestedTime);
	}

	@Override
	public void notifyModelChanged(int changeType) {
		switch (changeType) {
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			initialisationComplete = true;
			break;
		case PamControllerInterface.INITIALIZE_LOADDATA:
		case PamControllerInterface.CHANGED_OFFLINE_DATASTORE:
		case PamControllerInterface.ADD_CONTROLLEDUNIT:
		case PamControllerInterface.REMOVE_CONTROLLEDUNIT:
			if (initialisationComplete && intialiseLoadDone == false) {
				/*
				 *  changed 20240114 so that this is only called once. Stops it 
				 *  from resetting every time the datamap is updated e.g. if the 
				 *  acquisition file map changes when user selects correct folder.  
				 */
				intialiseLoadDone = true; // move earlier to avoid risk of recursion. 
				initialiseScrollers();
			}
			break;
		}	
	}


	/**
	 * Called once at the start, and possibly after 
	 * any changes to the database or binary store. 
	 * Initialises scroll bars and calls for a data load. 
	 */
	private void initialiseScrollers() {
		DataMapControl dmc = findDataMapControl();
		if (dmc == null) {
			return;
		}
		currentScrollInitialisation = true;
		AbstractPamScroller aScroller;
		PamScrollerData oldData;
		for (int i = 0; i < pamScrollers.size(); i++) {
			aScroller = pamScrollers.get(i);
			if (oldScrollerData != null) {
				oldData = oldScrollerData.findScrollerData(aScroller);
			}
			else {
				oldData = null;
			}
			if (oldData == null) {
				aScroller.setRangeMillis(dmc.getFirstTime(), 
						dmc.getFirstTime() + aScroller.getDefaultLoadtime(), true);
			}
			else {
				aScroller.scrollerData = oldData.clone();
				aScroller.rangesChanged(0);
			}
		}


		currentScrollInitialisation = false;
		loadData(false);
	}

	@Override
	public Serializable getSettingsReference() {
		StoredScrollerData sd = new StoredScrollerData();
		for (int i = 0; i < pamScrollers.size(); i++) {
			sd.addScrollerData(pamScrollers.get(i));
		}
		return sd;
	}

	@Override
	public long getSettingsVersion() {
		return StoredScrollerData.serialVersionUID;
	}

	@Override
	public String getUnitName() {
		return "Viewer Scroll Manager";
	}

	@Override
	public String getUnitType() {
		return "Viewer Scroll Manager";
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		oldScrollerData = (StoredScrollerData) pamControlledUnitSettings.getSettings();
		return true;
	}

	@Override
	public void centreDataAt(PamDataBlock dataBlock, long menuMouseTime) {
		// centre all scroll bars as close to the above as is possible.
		AbstractPamScroller aScroller;
		for (int i = 0; i < pamScrollers.size(); i++) {
			aScroller = pamScrollers.get(i);
			centreScrollerAt(aScroller, dataBlock, menuMouseTime);
		}
		loadData(false);
	}
	
	/**
	 * Moves outer scroller to centre at given time. 
	 * @param aScroller
	 * @param dataBlock
	 * @param menuMouseTime
	 */
	private void centreScrollerAt(AbstractPamScroller aScroller, PamDataBlock dataBlock, long menuMouseTime) {
		long scrollRange, newMax, newMin;
		scrollRange = aScroller.getMaximumMillis() - aScroller.getMinimumMillis();
		newMin = checkMinimumTime(menuMouseTime - scrollRange / 2);
		newMax = checkMaximumTime(newMin + scrollRange);
		newMin = menuMouseTime - scrollRange/2;
//		newMin = checkGapPos(dataBlock, newMin, newMax);
		newMax = newMin + scrollRange;
//		System.out.printf("Centering scoller at %s, range %s to %s\n", PamCalendar.formatDBDateTime(menuMouseTime),
//				PamCalendar.formatDBDateTime(newMin), PamCalendar.formatDBDateTime(newMax));
		aScroller.setRangeMillis(newMin, newMax, false);
		aScroller.setValueMillis(menuMouseTime);
	}
	
	@Override
	public void scrollToTime(PamDataBlock dataBlock, long menuMouseTime) {
		// centre all scroll bars as close to the above as is possible.
		AbstractPamScroller aScroller;
		boolean moved = false;
		for (int i = 0; i < pamScrollers.size(); i++) {
			aScroller = pamScrollers.get(i);
			long scrollerMin = aScroller.getMinimumMillis();
			long scrollerMax = aScroller.getMaximumMillis();
			if (menuMouseTime < scrollerMin || menuMouseTime > scrollerMax) {
				centreScrollerAt(aScroller, dataBlock, menuMouseTime);
				moved = true;
			}
			aScroller.setValueMillis(menuMouseTime);
		}
		if (moved) {
			loadData(false);
		}
	}

	@Override
	public void startDataAt(PamDataBlock dataBlock, long menuMouseTime, boolean immediateLoad) {
		AbstractPamScroller aScroller;
		long scrollRange, newMax, newMin;
		for (int i = 0; i < pamScrollers.size(); i++) {
			aScroller = pamScrollers.get(i);
			scrollRange = aScroller.getMaximumMillis() - aScroller.getMinimumMillis();
			newMin = checkMinimumTime(menuMouseTime);
			newMax = checkMaximumTime(newMin + scrollRange);
			newMin = newMax-scrollRange;
			newMin = checkGapPos(dataBlock, newMin, newMax);
			newMax = newMin + scrollRange;
			aScroller.setRangeMillis(newMin, newMax, false);
		}
		loadData(immediateLoad);
	}

	@Override
	public JPopupMenu getStandardOptionsMenu(AbstractPamScroller pamScroller) {
		JPopupMenu menu = new JPopupMenu();
		JCheckBoxMenuItem cbItem = new JCheckBoxMenuItem("Couple all scrollers");
		cbItem.setSelected(oldScrollerData.coupleAllScrollers);
		cbItem.addActionListener(new CoupleScrollersMenuItem(pamScroller, oldScrollerData.coupleAllScrollers));
		menu.add(cbItem);
		return menu;
	}

	class CoupleScrollersMenuItem implements ActionListener {
		private boolean currentState;
		private AbstractPamScroller pamScroller;

		public CoupleScrollersMenuItem(AbstractPamScroller pamScroller, boolean currentState) {
			super();
			this.currentState = currentState;
			this.pamScroller = pamScroller;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			setCoupleAllScrollers(pamScroller, !currentState);
		}
	}

	private void setCoupleAllScrollers(AbstractPamScroller pamScroller, boolean newState) {
		oldScrollerData.coupleAllScrollers = newState;
		if (newState) {
			followCoupledScroller(pamScroller);
		}
	}

	private void followCoupledScroller(AbstractPamScroller pamScroller) {
		AbstractPamScroller aScroller;
		long value = pamScroller.getValueMillis();
		for (int i = 0; i < pamScrollers.size(); i++) {
			aScroller = pamScrollers.get(i);
			if (aScroller == pamScroller) {
				continue;
			}
			/*
			 * Can end up with some horrible feedback here if two 
			 * coupled scrollers bring in rounding errors and start 
			 * to oscillate each other. 
			 */
			long currentVal = aScroller.getValueMillis();
			long change = Math.abs(value-currentVal);
			long range = aScroller.getMaximumMillis() - aScroller.getMinimumMillis();
			if (range == 0) {
				aScroller.setValueMillis(aScroller.getMinimumMillis());
				continue;
			}
			double fracChange = (double) change / (double) range;
			if (fracChange <= .0001) {
				continue;
			}
			
			aScroller.setValueMillis(value);
		}
	}

	@Override
	public long checkGapPos(AbstractPamScroller scroller,
			long oldMin, long oldMax, long newMin, long newMax, int direction) {
		int nDataBlocks = scroller.getNumUsedDataBlocks();
		if (nDataBlocks == 0) {
			return newMin;
		}
		int oldMinGap = 0, oldMaxGap = 0, newMinGap = 0, newMaxGap = 0;
		PamDataBlock dataBlock;
		long range = oldMax-oldMin;

		/*
		 * Get a series of flags for all four positions indicating
		 * exactly where the data are / want to be relative to 
		 * gaps in the datamap. 
		 * These are defined in OfflineDataMap	 
		 * <p>NO_DATA
		 * <p>BEFORE_FIRST
		 * <p>AFTER_LAST
		 * <p>POINT_START
		 * <p>POINT_END
		 * <P>IN_GAP
		 */
		for (int i = 0; i < nDataBlocks; i++) {
			dataBlock = scroller.getUsedDataBlock(i);
			oldMinGap |= isInGap(dataBlock, oldMin);
			oldMaxGap |= isInGap(dataBlock, oldMax);
			newMinGap |= isInGap(dataBlock, newMin);
			newMaxGap |= isInGap(dataBlock, newMax);
		}

		/**
		 * Can now think about what to do based on gap status
		 * of the four times.
		 */
		if ((newMinGap & OfflineDataMap.IN_DATA) > 0 && (newMaxGap & OfflineDataMap.IN_DATA) > 0) {
			return newMin;
		}
		long newStart;
		if (direction > 0) { // going forward. 
			/*
			 *  if the old start was in a gap, move so the new start is at the edge of
			 *  the next data.  
			 */
			if ((oldMinGap == OfflineDataMap.IN_GAP)) {
				newStart = getNextDataStart(scroller, newMin);
				if (newStart != Long.MAX_VALUE) {
					return newStart;
				}
			}
			/*
			 * If the old end was on the end of a point, then jump so that the new start
			 * is at the start of the next map point AFTER the old end.
			 */
			if ((oldMaxGap & OfflineDataMap.POINT_END) != 0) {
				newStart = getNextDataStart(scroller, oldMax);
				if (newStart != Long.MAX_VALUE) {
					return newStart;
				}
			}

			/*
			 * If the old end wasn't in gap and the new end is in a gap, then align the
			 * data so that the end is on the end of the data.  
			 */
			if ((oldMaxGap & OfflineDataMap.IN_DATA) != 0 &&
					(newMaxGap == OfflineDataMap.IN_GAP)) {
				newStart = getPrevDataEnd(scroller, newMax);
				if (newStart != Long.MAX_VALUE) {
					return newStart - range;
				}
			}			
		}
		else if (direction < 0) { // going backwards. 
			/*
			 *  if the old end was in a gap, move so the new end is at the edge of
			 *  the previous data.  
			 */
			if ((oldMaxGap == OfflineDataMap.IN_GAP)) {
				newStart = getPrevDataEnd(scroller, oldMax);
				if (newStart != Long.MIN_VALUE) {
					return newStart-range;
				}
			}
			/*
			 * If the old start was on the start of a point, then jump so that the new end
			 * is at the end of the previous map point BEFORE the old start.
			 */
			if ((oldMinGap & OfflineDataMap.POINT_START) != 0) {
				newStart = getPrevDataEnd(scroller, oldMin);
				if (newStart != Long.MIN_VALUE) {
					return newStart-range;
				}
			}

			/*
			 * If the old start wasn't in gap and the new start is in a gap, then align the
			 * data so that the start is on the start of the data.  
			 */
			if ((oldMinGap & OfflineDataMap.IN_DATA) != 0 &&
					(newMinGap == OfflineDataMap.IN_GAP)) {
				newStart = getNextDataStart(scroller, newMin);
				if (newStart != Long.MAX_VALUE) {
					return newStart;
				}
			}			
		}
		return newMin;
	}

	/**
	 * Very similar to the function checking gap pos for a whole scroller, 
	 * but only does it for one datablock. 
	 * @param dataBlock data block
	 * @param newMin new min time
	 * @param newMax new max time
	 * @return adjusted new min time
	 */
	private long checkGapPos(PamDataBlock dataBlock, long newMin, long newMax) {
		int newMinGap = 0, newMaxGap = 0;
		newMinGap |= isInGap(dataBlock, newMin);
		newMaxGap |= isInGap(dataBlock, newMax);
		long newT;
		if ((newMinGap & OfflineDataMap.IN_DATA) > 0 && (newMaxGap & OfflineDataMap.IN_DATA) > 0) {
			return newMin;
		}
		else if (newMinGap == OfflineDataMap.IN_GAP) {
			/*
			 * Move forward
			 */
			newT = dataBlock.getNextDataStart(newMin);
			if (newT > 0xFF) {
				return newT;
			}
		}
		else if (newMinGap == OfflineDataMap.IN_DATA && newMaxGap == OfflineDataMap.IN_GAP) {
			/**
			 * Move back a bit, so end of data aligns. 
			 */
			newT = dataBlock.getPrevDataEnd(newMax);
			if (newT > 0xFF) {
				return newT - (newMax-newMin);
			}

		}
		return newMin;
	}

	public long getNextDataStart(AbstractPamScroller scroller, long timeMillis) {
		long time = Long.MAX_VALUE;
		long t;
		for (int i = 0; i < scroller.getNumUsedDataBlocks(); i++) {
			t = scroller.getUsedDataBlock(i).getNextDataStart(timeMillis);
			if (t > 0xFF) {
				time = Math.min(time, t);
			}
		}
		return time;
	}

	public long getPrevDataEnd(AbstractPamScroller scroller, long timeMillis) {
		long time = Long.MIN_VALUE;
		long t;
		for (int i = 0; i < scroller.getNumUsedDataBlocks(); i++) {
			t = scroller.getUsedDataBlock(i).getPrevDataEnd(timeMillis);
			if (t > 0xFF) {
				time = Math.max(time, t);
			}
		}
		return time;
	}


}
