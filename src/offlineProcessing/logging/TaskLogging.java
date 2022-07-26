package offlineProcessing.logging;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
import offlineProcessing.TaskActivity;
import offlineProcessing.TaskGroupParams;
import offlineProcessing.TaskMonitorData;
import offlineProcessing.TaskStatus;

/**
 * Handles logging of tasks to the database. 
 * 
 * @author Doug Gillespie
 *
 */
public class TaskLogging {
	

	private static TaskLogging taskLogging;
	
	public static final int TASK_NOTE_LENGTH = 80;
	
	private EmptyTableDefinition tableDef;
	private PamTableItem utc, moduleType, moduleName, taskName, dataStart, dataEnd, runEnd,
	completionCode, note; 
	
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
		tableDef.addTableItem(note = new PamTableItem("Notes", Types.CHAR, TASK_NOTE_LENGTH));
		
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
		startTableDef.addTableItem(note);
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
		note.setValue(groupParams.taskNote);
	}
	
	/**
	 * Get TaskMonitorData from the database cursor. 
	 * @param taskGroup
	 * @param task
	 * @return
	 */
	private OldTaskData readTableData() {
		SQLTypes sqlTypes = con.getSqlTypes();
		long utc = SQLTypes.millisFromTimeStamp(this.utc.getValue());
		String modType = moduleType.getDeblankedStringValue();
		String modName = moduleName.getDeblankedStringValue();
		String tskName = taskName.getDeblankedStringValue();
		long dStart = SQLTypes.millisFromTimeStamp(dataStart.getValue());
		long dEnd = SQLTypes.millisFromTimeStamp(dataEnd.getValue());
		long procEnd = SQLTypes.millisFromTimeStamp(runEnd.getValue());
		String compStatus = completionCode.getDeblankedStringValue();
		TaskStatus status = null;
		try {
			status = TaskStatus.valueOf(TaskStatus.class, compStatus);
		}
		catch (IllegalArgumentException e) {
			System.out.printf("Uknown completion code \"%s\" for task %s ended at %s\n", compStatus, tskName, PamCalendar.formatDateTime(dEnd));
		}
		String taskNote = note.getDeblankedStringValue();
		OldTaskData monData = new OldTaskData(status, dStart, dEnd, utc, procEnd, taskNote);
		return monData;
	}
	
	/**
	 * Get the last data for an offline task. 
	 * @param taskGroup
	 * @param task
	 * @return
	 */
	public OldTaskData readLastTaskData(OfflineTaskGroup taskGroup, OfflineTask task) {
		if (!checkConnection()) {
			return null;
		}
		OldTaskData taskMonitorData = null;
		String clause;
		if (taskGroup == null) {
			// only query on the task name (not good if there is more than one module with the same tasks)
			clause = String.format(" WHERE TRIM(%s)='%s' ORDER BY Id DESC", taskName.getName(), task.getName());
		}
		else {
			clause = String.format(" WHERE TRIM(%s)='%s' AND TRIM(%s)='%s' AND TRIM(%s)='%s' ORDER BY Id DESC", 
					moduleType.getName(), task.getUnitType(),
					moduleName.getName(), task.getUnitName(),
					taskName.getName(), task.getName());
		}
		String selStr = tableDef.getSQLSelectString(con.getSqlTypes()) + clause;
		try {
			Statement selStmt = con.getConnection().createStatement();
			ResultSet results = selStmt.executeQuery(selStr);
			if (results.next()) {
				for (int i = 0; i < tableDef.getTableItemCount(); i++) {
					PamTableItem tableItem = tableDef.getTableItem(i);
					tableItem.setValue(results.getObject(i+1));
				}
				taskMonitorData = readTableData();
			}
			selStmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return taskMonitorData;
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
