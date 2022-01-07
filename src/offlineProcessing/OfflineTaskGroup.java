package offlineProcessing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.swing.SwingWorker;

import binaryFileStorage.DataUnitFileInformation;
import dataMap.OfflineDataMap;
import dataMap.OfflineDataMapPoint;
import generalDatabase.DBControlUnit;
import offlineProcessing.superdet.OfflineSuperDetFilter;
import pamScrollSystem.DataTimeLimits;
import pamScrollSystem.ViewLoadObserver;
import PamController.OfflineDataStore;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.CPUMonitor;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import PamguardMVC.debug.Debug;
import PamguardMVC.superdet.SuperDetDataBlock;

/**
 * 
 * @author Doug Gillespie
 * 
 * Handles a series of offline tasks which all use a 
 * common data block so that data can be loaded, a whole
 * series of tasks completed and the data then saved in 
 * a single operation. 
 * <p>
 * This will be the primary interface to OfflineTasks - even
 * if there is only one task it will be in a group of one !
 *  
 *
 */
public class OfflineTaskGroup implements PamSettings {

	/**
	 * Summary list of all data blocks required by all tasks in 
	 * the list. 
	 */
	private ArrayList<RequiredDataBlockInfo> requiredDataBlocks = new ArrayList<RequiredDataBlockInfo>();

	/**
	 * Summary list of all data blocks affected by the list. 
	 */
	private ArrayList<PamDataBlock> affectedDataBlocks = new ArrayList<PamDataBlock>();

	private PamControlledUnit pamControlledUnit;

	private String settingsName;
	
	private TaskGroupParams taskGroupParams = new TaskGroupParams();
	
	private DataTimeLimits dataTimeLimits;

	/**
	 * PamControlledunit required in constructor since some bookkeeping will
	 * be goign on in the background which will need the unit type and name. 
	 * @param pamControlledUnit host controlled unit. 
	 * @param settingsName  Name to be used in PamSettings for storing some basic information 
	 * (which tasks are selected)
	 */
	public OfflineTaskGroup(PamControlledUnit pamControlledUnit, String settingsName) {
		super();
		this.pamControlledUnit = pamControlledUnit;
		pamControlledUnit.addOfflineTaskGroup(this);
		this.settingsName = settingsName;
		PamSettingManager.getInstance().registerSettings(this);
	}

	/**
	 * Setup summary lists of required and affected datablocks
	 * based on which tasks are actually going to run .
	 */
	public void setSummaryLists() {
		requiredDataBlocks.clear();
		affectedDataBlocks.clear();
		OfflineTask aTask;
		PamDataBlock aBlock;
		RequiredDataBlockInfo blockInfo;
		for (int iTask = 0; iTask < getNTasks(); iTask++) {
			aTask = getTask(iTask);
			for (int i = 0; i < aTask.getNumRequiredDataBlocks(); i++) {
				blockInfo = aTask.getRequiredDataBlock(i);
				if (hasRequiredDataBlock(blockInfo) == false) {
					requiredDataBlocks.add(blockInfo);
				}
			}
			for (int i = 0; i < aTask.getNumAffectedDataBlocks(); i++) {
				aBlock = aTask.getAffectedDataBlock(i);
				if (affectedDataBlocks.indexOf(aBlock) < 0) {
					affectedDataBlocks.add(aBlock);
				}
			}
		}
	}
	
	/**
	 * Check to see if a datablock is already in the required list and if so, 
	 * merge the pre and post load times.  
	 * @param dataBlock data block to check
	 * @return true if already listed. 
	 */
	private boolean hasRequiredDataBlock(RequiredDataBlockInfo dataBlockInfo) {
		if (requiredDataBlocks == null) {
			return false;
		}
		for (RequiredDataBlockInfo dbinfo : requiredDataBlocks) {
			if (dbinfo.getPamDataBlock() == dataBlockInfo.getPamDataBlock()) {
				dbinfo.setPreLoadTime(Math.max(dbinfo.getPreLoadTime(), dataBlockInfo.getPreLoadTime()));;
				dbinfo.setPostLoadTime(Math.max(dbinfo.getPostLoadTime(), dataBlockInfo.getPostLoadTime()));;
				return true;
			}
		}
		return false;
	}

	/**
	 * A task monitor which will receive progress updates 
	 * as the tasks complete. 
	 */
	private TaskMonitor taskMonitor;

	/**
	 * Data block used by ALL tasks in the group.
	 */
	private PamDataBlock primaryDataBlock;

	private ArrayList<OfflineTask> offlineTasks = new ArrayList<OfflineTask>();

	private TaskGroupWorker worker;

	private OfflineSuperDetFilter superDetectionFilter;

	/**
	 * Run all the tasks. 
	 * @param offlineClassifierParams 
	 * @return
	 */
	public boolean runTasks() {
		setSummaryLists();
		worker = new TaskGroupWorker();
		worker.execute();
		return true;
	}

	public void killTasks() {
		if (worker == null) {
			return;
		}
		worker.killWorker();
	}

	/**
	 * Add a list of offline tasks. 
	 * Checks that tasks are not already included in the task list 
	 * and doesn't add them if they already exist. 
	 * @param tasks list of tasks. 
	 * @return number added. 
	 */
	public int addTasks(List<OfflineTask> tasks) {
		int n = 0;
		if (tasks == null) {
			return 0;
		}
		for (OfflineTask task : tasks) {
			if (haveTask(task) == false) { 
				addTask(task);
				n++;
			}
		}
		
		return n;
	}
	
	/**
	 * See if a task already exists. 
	 * @param task offline tasks. 
	 * @return true if it exists
	 */
	public boolean haveTask(OfflineTask task ) {
		return offlineTasks.contains(task);
	}
	
	/**
	 * See if we already have a task of the same class. 
	 * @param task task to check
	 * @return true if a task of the same class already exists. 
	 */
	public boolean haveTaskClass(OfflineTask task) {
		if (task == null) return false;
		for (OfflineTask exTask : offlineTasks) {
			if (exTask.getClass() == task.getClass()) {
				return true;
			}
		}
		return false;
	}
	/**
	 * 
	 * @param task task to add to the group
	 */
	public boolean addTask(OfflineTask task) {
		offlineTasks.add(task);
		task.setOfflineTaskGroup(this);
		task.setDoRun(taskGroupParams.getTaskSelection(offlineTasks.size()-1));
		if (primaryDataBlock == null) {
			primaryDataBlock = task.getDataBlock();
		}
		else if (primaryDataBlock != task.getDataBlock()) {
			System.out.println(String.format("Error - cannot combine tasks with data from %s and %s",
					primaryDataBlock.getDataName(), task.getDataBlock().getDataName()));
		}
		return true;
	}
	/**
	 * 
	 * @return the number of tasks in the group
	 */
	public int getNTasks() {
		return offlineTasks.size();
	}

	/**
	 * 
	 * @param iTask the task number
	 * @return the task. 
	 */
	public OfflineTask getTask(int iTask) {
		return offlineTasks.get(iTask);
	}
	/**
	 * @return the processTime
	 */
	public int getProcessTime() {
		return taskGroupParams.dataChoice;
	}

	/**
	 * @return the primaryDataBlock
	 * 
	 */
	public PamDataBlock getPrimaryDataBlock() {
		return primaryDataBlock;
	}

	/**
	 * @param primaryDataBlock the primaryDataBlock to set
	 */
	public void setPrimaryDataBlock(PamDataBlock primaryDataBlock) {
		this.primaryDataBlock = primaryDataBlock;
	}

	/**
	 * @return the taskMonitor
	 */
	public TaskMonitor getTaskMonitor() {
		return taskMonitor;
	}

	/**
	 * @param taskMonitor the taskMonitor to set
	 */
	public void setTaskMonitor(TaskMonitor taskMonitor) {
		this.taskMonitor = taskMonitor;
	}

	/**
	 * Swing worker to do the actual work. 
	 * @author Doug Gillespie
	 *
	 */
	class TaskGroupWorker extends SwingWorker<Integer, TaskMonitorData> implements ViewLoadObserver {

		volatile boolean instantKill = false;

		private int completionStatus = TaskMonitor.TASK_IDLE;
		
		private CPUMonitor cpuMonitor = new CPUMonitor();

		public void killWorker() {
			instantKill = true;
		}
	
		/**
		 * Check whether the thread has been stopped. 
		 * @return true if the thread has been stopped. 
		 */
		public boolean isInstantKill() {
			return instantKill; 
		}

		@Override
		protected Integer doInBackground() {
			completionStatus = TaskMonitor.TASK_RUNNING;
			try {
				prepareTasks();
				switch (taskGroupParams.dataChoice) {
				case TaskGroupParams.PROCESS_LOADED:
					processLoadedData();
					break;
				case TaskGroupParams.PROCESS_ALL:
					processAllData(0, Long.MAX_VALUE);
					break;
				case TaskGroupParams.PROCESS_NEW:
					processAllData(taskGroupParams.lastDataTime, Long.MAX_VALUE);
					break;
				case TaskGroupParams.PROCESS_SPECIFICPERIOD:
					processAllData(taskGroupParams.startRedoDataTime, taskGroupParams.endRedoDataTime);
					break;
				case TaskGroupParams.PROCESS_TME_CHUNKS:
					processAllData(taskGroupParams.timeChunks);
					break;
				}
				if (instantKill) {
					completionStatus = TaskMonitor.TASK_INTERRRUPTED;
				}
				else {
					completionStatus = TaskMonitor.TASK_COMPLETE;
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				completionStatus = TaskMonitor.TASK_CRASHED;
			}
			completeTasks();
			return null;
		}
		
		
		/**
		 * Process all data for a list of time chunks. This is robust to the list
		 * not being in chronological order. 
		 * @param timeChunks - the time chunks.
		 */
		private void processAllData(ArrayList<long[]> timeChunks){
			long startTime = Long.MAX_VALUE;
			long endTime = -Long.MAX_VALUE;
			for (int i=0; i<timeChunks.size(); i++) {
				if (timeChunks.get(i)[0]<startTime) {
					startTime=timeChunks.get(i)[0];
				}
				if (timeChunks.get(i)[1]>endTime) {
					endTime=timeChunks.get(i)[1];
				}
			}
			processAllData(startTime,  endTime);
		}
		

		/**
		 * Process data between two times
		 * @param startTime - the start time in millis
		 * @param endTime - the end time in millis. 
		 */
		private void processAllData(long startTime, long endTime) {
			
			//System.out.println("TaskGroupParams.dataChoice" +  taskGroupParams.dataChoice);
			
			long currentStart = primaryDataBlock.getCurrentViewDataStart();
			long currentEnd = primaryDataBlock.getCurrentViewDataEnd();

			//			synchronized(primaryDataBlock) {
			OfflineDataMap dataMap = primaryDataBlock.getPrimaryDataMap();
			int nMapPoints = dataMap.getNumMapPoints(startTime, endTime);
			int iMapPoint = 0;
			publish(new TaskMonitorData(TaskMonitor.TASK_RUNNING, nMapPoints));
			publish(new TaskMonitorData(0, 0.0));
			OfflineDataStore dataSource = dataMap.getOfflineDataSource();
			Iterator<OfflineDataMapPoint> mapIterator = dataMap.getListIterator();
			OfflineDataMapPoint mapPoint;
//			System.out.println("NUMBER OF MAP POINTS: " + mapIterator.hasNext()
//			+ "Start time: " + PamCalendar.formatDateTime(startTime) +  "End time: " + PamCalendar.formatDateTime(endTime));
			boolean reallyDoIt = false;
			while (mapIterator.hasNext()) {
				mapPoint = mapIterator.next();
				reallyDoIt = true;
				if (mapPoint.getEndTime() < startTime || mapPoint.getStartTime() > endTime ) {
					//System.out.println("HELLOOOOO: " + (mapPoint.getEndTime()-mapPoint.getStartTime())/1000.+  "s"); 
					continue; // will whip through early part of list without increasing the counters
				}
				if (shouldProcess(mapPoint) == false) {
					Debug.out.printf("Skipping map point %s since no matching data\n", mapPoint.toString());
					reallyDoIt = false;;
				}
				publish(new TaskMonitorData(mapPoint.getName()));
				primaryDataBlock.clearAll();
				
				if (reallyDoIt) {
					Runtime.getRuntime().gc(); //garbage collection

					primaryDataBlock.loadViewerData(new OfflineDataLoadInfo(mapPoint.getStartTime(), mapPoint.getEndTime()), null);

					primaryDataBlock.sortData();

					//					if (procDataEnd - procDataStart < maxSecondaryLoad) {
					loadSecondaryData(mapPoint.getStartTime(), mapPoint.getEndTime());
					//					}
					for (OfflineTask aTask: offlineTasks) {
						if (aTask.isDoRun() == false) {
							continue;
						}
						aTask.newDataLoad(mapPoint.getStartTime(), mapPoint.getEndTime(), mapPoint);
					}
					ArrayList<PamDataBlock> dataBlocks = PamController.getInstance().getDetectorDataBlocks();
					for (PamDataBlock dataBlock:dataBlocks) {
						// may no longer be needed depending on how reloading data goes.  
						if (dataBlock instanceof SuperDetDataBlock) {
							((SuperDetDataBlock) dataBlock).reattachSubdetections(null);
						}
					}
					
					if (superDetectionFilter != null) {
						superDetectionFilter.checkSubDetectionLinks();
					}
					processData(iMapPoint, mapPoint, mapPoint.getStartTime(), mapPoint.getEndTime());
				}
				iMapPoint++;
				publish(new TaskMonitorData(iMapPoint+1, 0.0));
				if (instantKill) {
					break;
				}
			}
			//			}
			publish(new TaskMonitorData(TaskMonitor.TASK_IDLE));
			publish(new TaskMonitorData(TaskMonitor.TASK_COMPLETE));
			primaryDataBlock.loadViewerData(new OfflineDataLoadInfo(currentStart, currentEnd), null);
		}

		/**
		 * See if it's worth loading this map point. This will currently always
		 * return true unless there is a superDetectionFilter, in which case it will 
		 * attempt to work out if there are any sub detections that might want
		 * processing. 
		 * @param mapPoint
		 * @return
		 */
		private boolean shouldProcess(OfflineDataMapPoint mapPoint) {
			if (superDetectionFilter == null) {
				return true;
			}
			else {
				return superDetectionFilter.shouldProcess(mapPoint);
			}
		}

		private void processLoadedData() {
			publish(new TaskMonitorData(TaskMonitor.TASK_RUNNING, 1));
			if (dataTimeLimits == null) {
				processData(0, null, 0, Long.MAX_VALUE);
			}
			else {
				processData(0, null, dataTimeLimits.getMinimumMillis(), dataTimeLimits.getMaximumMillis());
			}
			publish(new TaskMonitorData(TaskMonitor.TASK_IDLE));
			publish(new TaskMonitorData(TaskMonitor.TASK_COMPLETE));
		}

		/**
		 * Called once at start of all processing. 
		 */
		private void prepareTasks() {
			int nTasks = getNTasks();
			OfflineTask aTask;
			for (int iTask = 0; iTask < nTasks; iTask++) {
				aTask = getTask(iTask);
				if (aTask.canRun() == false) {
					continue;
				}
				if (aTask.isDoRun()) {
					if (taskGroupParams.deleteOld) {
						aTask.deleteOldData(taskGroupParams);
					}
					//added this here so that tasks with different
					//parent data blocks can be run (albiet not at the same time)
					aTask.prepareTask();
				}
			}			
		}

		/**
		 * Called to process data currently in memory. i.e. get's called 
		 * once when processing loaded data, multiple times when pocessing all data. 
		 * @param globalProgress
		 * @param mapPoint
		 * @param processStartTime
		 * @param processEndTime
		 */
		private void processData(int globalProgress, OfflineDataMapPoint mapPoint, long processStartTime, long processEndTime) {
			int nDatas = primaryDataBlock.getUnitsCount();
			int nSay = Math.max(1, nDatas / 100);
//			int nDone = 0;
			int nTasks = getNTasks();
			PamDataUnit dataUnit;
			OfflineTask aTask;
			boolean unitChanged;
			DataUnitFileInformation fileInfo;
			/**
			 * Make sure that any data from required data blocks is loaded. First check the 
			 * start and end times of the primary data units we actually WANT to process
			 * Also get a count of found data - may be able to leave without having to do anything at all
			 */
			ListIterator<PamDataUnit> it = primaryDataBlock.getListIterator(0);
			long procDataStart = Long.MAX_VALUE;
			long procDataEnd = 0;
			int nToProcess = 0;
			while (it.hasNext()) {
				dataUnit = it.next();
				/**
				 * Make sure we only process data units within the current time interval. 
				 */
				if (dataUnit.getTimeMilliseconds() < processStartTime) {
					continue;
				}
				if (dataUnit.getTimeMilliseconds() > processEndTime) {
					break;
				}
//				if (shouldProcess(dataUnit) == false) {
//					continue;
//				}
				procDataStart = Math.min(procDataStart, dataUnit.getTimeMilliseconds());
				procDataEnd = Math.max(procDataEnd, dataUnit.getEndTimeInMilliseconds());
				// do this one too - just to make sure in case end time returns zero. 
				procDataEnd = Math.max(procDataEnd, dataUnit.getTimeMilliseconds());
				nToProcess++; // increase toprocess counter
			}
			if (nToProcess == 0) {
				return;
			}
			PamDataBlock aDataBlock;
			RequiredDataBlockInfo blockInfo;
			/* 
			 * if the data interval is < 1 hour, then load it all now
			 * otherwise we'll do it on a data unit basis. 
			 */
////			long maxSecondaryLoad = 1800L*1000L;
////			if (procDataEnd - procDataStart < maxSecondaryLoad) {
//				loadSecondaryData(procDataStart, procDataEnd);
////			}
			// remember the end time of the data so we can use the "new data" selection flag. 
			taskGroupParams.lastDataTime = Math.min(primaryDataBlock.getCurrentViewDataEnd(),processEndTime);
			//			synchronized(primaryDataBlock) {
			/*
			 * Call newDataLoaded for each task before getting on with processing individual data units. 
			 */

			/**
			 * Now process the data
			 */
			it = primaryDataBlock.getListIterator(0);
			unitChanged = false;
			int totalUnits = 0;
			int unitsChanged = 0;
			boolean doTasks = false;
			while (it.hasNext()) {
				dataUnit = it.next();
				totalUnits++;
				doTasks = true;
				/**
				 * Make sure we only process data units within the current time interval. 
				 */
				if (dataUnit.getTimeMilliseconds() < processStartTime) {
					continue;
				}
				if (dataUnit.getTimeMilliseconds() > processEndTime) {
					break;
				}
				
				if (shouldProcess(dataUnit) == false) {
					doTasks = false;
				}
				
				if (doTasks) {
					/*
					 *  load the secondary datablock data. this can be called even if
					 *  it was called earlier on since it wont' reload if data are already
					 *  in memory.  
					 */
//					loadSecondaryData(dataUnit.getTimeMilliseconds(), dataUnit.getEndTimeInMilliseconds());

					for (int iTask = 0; iTask < nTasks; iTask++) {
						aTask = getTask(iTask);
						if (aTask.isDoRun() == false ||  !isInTimeChunk(dataUnit, taskGroupParams.timeChunks)) {
							continue;
						}
						cpuMonitor.start();
						unitChanged |= aTask.processDataUnit(dataUnit);
						cpuMonitor.stop();
					}
					if (unitChanged) {
						fileInfo = dataUnit.getDataUnitFileInformation();
						if (fileInfo != null) {
							fileInfo.setNeedsUpdate(true);
						}
						dataUnit.updateDataUnit(System.currentTimeMillis());
					}
					dataUnit.freeData();
				}
				if (instantKill) {
					break;
				}
				unitsChanged++;
				if (totalUnits%nSay == 0) {
					publish(new TaskMonitorData(globalProgress+1, (double) totalUnits / (double) nDatas));
				}
			}
			for (int iTask = 0; iTask < nTasks; iTask++) {
				aTask = getTask(iTask);
				if (aTask.isDoRun() == false) {
					continue;
				}
				aTask.loadedDataComplete();
			}
			//			}
			for (int i = 0; i < affectedDataBlocks.size(); i++) {
				//System.out.println("SAVE VIEWER DATA FOR: " + affectedDataBlocks.get(i) );
				aDataBlock = affectedDataBlocks.get(i);
				aDataBlock.saveViewerData();
			}
			publish(new TaskMonitorData(globalProgress+1, (double) totalUnits / (double) nDatas));
			Debug.out.printf("Processd %d out of %d data units at " + mapPoint + "\n", unitsChanged, totalUnits);
			commitDatabase();
		}
		
		private boolean shouldProcess(PamDataUnit dataUnit) {
			if (superDetectionFilter != null) {
				boolean yes = superDetectionFilter.checkSubDetection(dataUnit);
//				Debug.out.printf("Super det filter says %s\n", yes);
				return yes;
			}
			return true;
		}
		
		/**
		 * Check whether a data unit is within a list of time chunks within params. Returns true if the 
		 * PROCESS_TME_CHUNKS option is not the current data analysis choice. 
		 * @param dataUnit - the data unit to check
		 * @param timeChunks - a list of time chunks with each long[] a start and end time in millis
		 * @return true if the data unit is within any of the time chunks or data choice is not PROCESS_TME_CHUNKS. 
		 */
		private boolean isInTimeChunk(PamDataUnit dataUnit, ArrayList<long[]> timeChunks) {
			if (taskGroupParams.dataChoice!=TaskGroupParams.PROCESS_TME_CHUNKS) return true; 
			for (int i=0; i<timeChunks.size(); i++) {
				if (dataUnit.getTimeMilliseconds()>=timeChunks.get(i)[0] && dataUnit.getTimeMilliseconds()<timeChunks.get(i)[1]){
					return true;
				}
			}
			return false; 
		}


		private void commitDatabase() {
			DBControlUnit dbcontrol = DBControlUnit.findDatabaseControl();
			if (dbcontrol == null) return;
			dbcontrol.commitChanges();
		}

		private void loadSecondaryData(long procDataStart, long procDataEnd) {
			for (int i = 0; i < requiredDataBlocks.size(); i++) {
				RequiredDataBlockInfo blockInfo = requiredDataBlocks.get(i);
				PamDataBlock aDataBlock = blockInfo.getPamDataBlock();
				long reqStart = procDataStart - blockInfo.getPreLoadTime();
				long reqEnd = procDataEnd + blockInfo.getPostLoadTime();
//				if (aDataBlock.getCurrentViewDataStart() > reqStart ||
//						aDataBlock.getCurrentViewDataEnd() < reqEnd) {
					aDataBlock.loadViewerData(new OfflineDataLoadInfo(reqStart, reqEnd), null);
//				}
			}
		}

		private void completeTasks() {
			int nTasks = getNTasks();
			OfflineTask aTask;
			for (int iTask = 0; iTask < nTasks; iTask++) {
				aTask = getTask(iTask);
				if (aTask.canRun() == false) {
					continue;
				}
				aTask.completeTask();
			}			
			System.out.println(cpuMonitor.getSummary("Offline task processing: "));
		}

		@Override
		protected void done() {
			tasksDone();
		}


		@Override
		protected void process(List<TaskMonitorData> chunks) {
			for (int i = 0; i < chunks.size(); i++) {
				newMonitorData(chunks.get(i));
			}
		}

		@Override
		public void sayProgress(int state, long loadStart, long loadEnd, long lastTime, int nLoaded) {
			TaskMonitorData tmd = new TaskMonitorData(TaskMonitorData.LOADING_DATA);
			tmd.dataType = TaskMonitorData.LOADING_DATA;
			publish(tmd);
			
		}

		@Override
		public boolean cancelLoad() {
			// TODO Auto-generated method stub
			return false;
		}

	}

	private void newMonitorData(TaskMonitorData monData) {
		if (taskMonitor == null) {
			return;
		}
		int dataType = monData.dataType;
		if ((dataType & TaskMonitorData.SET_STATUS) != 0) {
			taskMonitor.setStatus(monData.status);
		}
		if ((dataType & TaskMonitorData.SET_NFILES) != 0) {
			taskMonitor.setNumFiles(monData.nFiles);
		}
		if ((dataType & TaskMonitorData.SET_PROGRESS) != 0) {
			taskMonitor.setProgress(monData.globalProgress, monData.loadedProgress);
			//			taskMonitor.setProgress(monData.globalProgress, .5);
		}
		if ((dataType & TaskMonitorData.SET_FILENAME) != 0) {
			taskMonitor.setFileName(monData.fileName);
		}
		if (dataType == TaskMonitorData.LOADING_DATA) {
			taskMonitor.setStatus(monData.status);
		}
	}

	/**
	 * some bookkeeping - write information about task completion to the database. 
	 */
	public void tasksDone() {
		long currentStart = primaryDataBlock.getCurrentViewDataStart();
		long currentEnd = primaryDataBlock.getCurrentViewDataEnd();
		//System.out.println("TASKS COMPLETE:");
		PamController.getInstance().notifyModelChanged(PamController.OFFLINE_PROCESS_COMPLETE);
	}

	@Override
	public Serializable getSettingsReference() {
		for (int i = 0; i < offlineTasks.size(); i++) {
			taskGroupParams.setTaskSelection(i, offlineTasks.get(i).isDoRun());
		}
		return taskGroupParams;
	}

	@Override
	public long getSettingsVersion() {
		return TaskGroupParams.serialVersionUID;
	}

	@Override
	public String getUnitName() {
		return settingsName;
	}

	@Override
	public String getUnitType() {
		return pamControlledUnit.getUnitType()+pamControlledUnit.getUnitName();
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		this.taskGroupParams = ((TaskGroupParams) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

	/**
	 * @return the taskGroupParams
	 */
	public TaskGroupParams getTaskGroupParams() {
		return taskGroupParams;
	}

	/**
	 * @return the dataTimeLimits
	 */
	public DataTimeLimits getDataTimeLimits() {
		return dataTimeLimits;
	}

	/**
	 * @param dataTimeLimits the dataTimeLimits to set
	 */
	public void setDataTimeLimits(DataTimeLimits dataTimeLimits) {
		this.dataTimeLimits = dataTimeLimits;
	}

	/**
	 * Check whether the task has been cancelled. Sometime used if 
	 * processing a data unit takes a long time and should be cancelled
	 * @return true if task has been cancelled. 
	 */
	public boolean isTaskCancelled() {
		 return this.worker.isInstantKill();
	}

	/**
	 * Set a super detection filter (null if no super detection system available)
	 * @param superDetectionFilter Super detection filter. 
	 */
	public void setSuperDetectionFilter(OfflineSuperDetFilter superDetectionFilter) {
		this.superDetectionFilter = superDetectionFilter;
	}

	/**
	 * @return the superDetectionFilter
	 */
	public OfflineSuperDetFilter getSuperDetectionFilter() {
		return superDetectionFilter;
	}
}
