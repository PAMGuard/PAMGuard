package offlineProcessing.logging;

import java.sql.Connection;
import java.sql.Types;
import java.util.HashMap;

import PamController.PamControlledUnit;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.DBControlUnit;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamConnection;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;
import generalDatabase.pamCursor.CursorFinder;
import generalDatabase.pamCursor.PamCursor;
import offlineProcessing.OfflineTask;
import offlineProcessing.OfflineTaskGroup;
import offlineProcessing.TaskGroupParams;
import offlineProcessing.TaskMonitorData;

/**
 * Handles logging of tasks to the database. 
 * 
 * @author Doug Gillespie
 *
 */
public class TaskLogging {
	

	private static TaskLogging taskLogging;
	
	private EmptyTableDefinition tableDef;
	private PamTableItem utc, moduleType, moduleName, taskName, dataStart, dataEnd, runEnd,
	completionCode; 
	
	/**
	 * Storage mostly to keep id's of last database index for each task. 
	 */
	private HashMap<OfflineTask, TaskLoggingData> loggingData = new HashMap();
	
	/*
	 * May want several different write and update cursors for this. e.g. when a task starts
	 * we want to write the UTC and id comlumns and the dataStart, but probably not the dataEnd and 
	 * the runEnd columns. Then when we update (if we update) we'll occasionally update the dataEnd
	 * time, then only will in the runEnd and completionCode when the task has finished. 
	 */
	private EmptyTableDefinition startTableDef, updateTableDef, completeTableDef;

	private PamConnection con;

	private static final String tableName = "OfflineTasks";
	
//	private StartLogging startLogging;
	private CursorFinder startCursorFinder, completeCursorFinder;
	
	private TaskLoggingDataBlock taskLoggingDataBlock;
	
	private TaskLogging() {
		// table for everything (mostly used for reading)
		tableDef = new EmptyTableDefinition(tableName);
		tableDef.addTableItem(utc = new PamTableItem("UTC", Types.TIMESTAMP));
		tableDef.addTableItem(moduleType = new PamTableItem("Module Type", Types.CHAR, 50));
		tableDef.addTableItem(moduleName = new PamTableItem("Module Name", Types.CHAR, 50));
		tableDef.addTableItem(taskName = new PamTableItem("Task Name", Types.CHAR, 50));
		tableDef.addTableItem(dataStart = new PamTableItem("DataStart", Types.TIMESTAMP));
		tableDef.addTableItem(dataEnd = new PamTableItem("DataEnd", Types.TIMESTAMP));
		tableDef.addTableItem(runEnd = new PamTableItem("RunEnd", Types.TIMESTAMP));
		tableDef.addTableItem(completionCode = new PamTableItem("CompletionCode", Types.CHAR, 20));
		
		taskLoggingDataBlock = new TaskLoggingDataBlock();
		
		/**
		 * Note that completionCode Strings can be got from 
		 * TaskMonitorData.getStatusString
		 */
		// table for startup write
		startTableDef = new EmptyTableDefinition(tableName);
		startTableDef.addTableItem(utc);
		startTableDef.addTableItem(moduleType);
		startTableDef.addTableItem(moduleName);
		startTableDef.addTableItem(taskName);
		startTableDef.addTableItem(dataStart);
//		startLogging = new StartLogging(taskLoggingDataBlock, startTableDef);
		startCursorFinder = new CursorFinder();
		

		// table for startup write
		updateTableDef = new EmptyTableDefinition(tableName);
		updateTableDef.addTableItem(dataEnd);

		// table for startup write
		completeTableDef = new EmptyTableDefinition(tableName);
		completeTableDef.addTableItem(dataEnd);
		completeTableDef.addTableItem(runEnd);
		completeTableDef.addTableItem(completionCode);
		completeCursorFinder = new CursorFinder();
		
		
	}

	public static TaskLogging getTaskLogging() {
		if (taskLogging == null) {
			taskLogging = new TaskLogging();
		}
		taskLogging.checkConnection();
		return taskLogging;
	}

	private boolean checkConnection() {
		// TODO Auto-generated method stub
		PamConnection currentCon = DBControlUnit.findConnection();
		if (currentCon != con) {
			/**
			 * Need to check tables, etc. 
			 */
			con = currentCon;
			DBControlUnit.findDatabaseControl().getDbProcess().checkTable(tableDef);
		}
		return currentCon != null;
	}

	public boolean logTask(OfflineTaskGroup taskGroup, OfflineTask task, TaskMonitorData monitorData) {
		if (!checkConnection()) {
			return false;
		}
		switch (monitorData.taskStatus) {
//		case IDLE:
		case STARTING:
			return logStart(taskGroup, task, monitorData);
		case RUNNING:
			return logUpdate(taskGroup, task, monitorData);
		case COMPLETE:
		case CRASHED:
		case INTERRUPTED:
			return logComplete(taskGroup, task, monitorData);
		default:
			break;
		
		}
		
		
		return true;
	}
	
	/**
	 * Move all data from the cursor, even though not all objects may get used by the individual cursors. 
	 * @param taskGroup
	 * @param task
	 * @param monitorData
	 */
	private void fillTableData(OfflineTaskGroup taskGroup, OfflineTask task, TaskMonitorData monitorData) {
		TaskGroupParams groupParams = taskGroup.getTaskGroupParams();
		SQLTypes sqlTypes = con.getSqlTypes();
		utc.setValue(sqlTypes.getTimeStamp(System.currentTimeMillis()));
		moduleType.setValue(task.getUnitType());
		moduleName.setValue(task.getUnitName());
		taskName.setValue(task.getName());
		dataStart.setValue(sqlTypes.getTimeStamp(groupParams.startRedoDataTime));
		dataEnd.setValue(sqlTypes.getTimeStamp(monitorData.lastDataDate));
		runEnd.setValue(sqlTypes.getTimeStamp(System.currentTimeMillis()));
		completionCode.setValue(monitorData.taskStatus.toString());
	}

	private boolean logStart(OfflineTaskGroup taskGroup, OfflineTask task, TaskMonitorData monitorData) {
		fillTableData(taskGroup, task, monitorData);
		PamCursor startCursor = startCursorFinder.getCursor(con, startTableDef);
		int dbInd = startCursor.immediateInsert(con);
		if (dbInd > 0) {
			loggingData.put(task, new TaskLoggingData(task, dbInd, monitorData));
			return true;
		}
		else {
			return false;
		}
	}
	
	private boolean logUpdate(OfflineTaskGroup taskGroup, OfflineTask task, TaskMonitorData monitorData) {
		// TODO Auto-generated method stub
		return true;
	}
	
	private boolean logComplete(OfflineTaskGroup taskGroup, OfflineTask task, TaskMonitorData monitorData) {
		fillTableData(taskGroup, task, monitorData);
		PamCursor startCursor = completeCursorFinder.getCursor(con, completeTableDef);
		TaskLoggingData taskLogData = loggingData.get(task);
		if (taskLogData == null) {
			System.out.println("No logging data to update for offline task " + task.getName());
			return false;
		}
		completeTableDef.getIndexItem().setValue(taskLogData.databaseIndex);
		boolean updateOk = startCursor.immediateUpdate(con);
		if (updateOk) {
			taskLogData.monitorData = monitorData;
			return true;
		}
		else {
			return false;
		}
	}
	
//	private class StartLogging extends SQLLogging {
//
//		protected StartLogging(TaskLoggingDataBlock pamDataBlock, EmptyTableDefinition startTableDef) {
//			super(pamDataBlock);
//			setTableDefinition(startTableDef);
//		}
//
//		@Override
//		public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
//			// TODO Auto-generated method stub
//			
//		}
//		
//	}

}
