package backupmanager.schedule;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import backupmanager.schedule.BackupResult.RESULT;
import generalDatabase.DBControlUnit;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamConnection;
import generalDatabase.PamTableItem;
import generalDatabase.SQLTypes;
import generalDatabase.pamCursor.PamCursor;
import generalDatabase.pamCursor.PamCursorManager;

public class ScheduleDatabase {

	private BackupSchedule backupSchedule;
	
	private EmptyTableDefinition tableDef;
	
	private PamTableItem utc, endUTC, schedule, result, detail;
	
	private PamConnection checkedConnection;

	private PamCursor cursor;

	public ScheduleDatabase(BackupSchedule backupSchedule) {
		super();
		this.backupSchedule = backupSchedule;
		makeTableDef();
	}

	private void makeTableDef() {
		String tableName = "Backup History";
		tableDef = new EmptyTableDefinition(tableName);
		tableDef.addTableItem(utc = new PamTableItem("UTC", Types.TIMESTAMP));
		tableDef.addTableItem(endUTC = new PamTableItem("EndUTC", Types.TIMESTAMP));
		tableDef.addTableItem(schedule = new PamTableItem("Schedule", Types.CHAR, 50));
		tableDef.addTableItem(result = new PamTableItem("Result", Types.CHAR, 50));
		tableDef.addTableItem(detail = new PamTableItem("Detail", Types.CHAR, 50));
	}
	
	public boolean writeResult(BackupResult backupResult) {
		if (!checkTable()) {
			return false;
		}
		PamConnection con = DBControlUnit.findConnection();
		
		setData(backupResult);
		
		int ind = cursor.immediateInsert(con);
				
		backupResult.setDbIndex(ind);
		
		return ind != 0;
	}
	
	private boolean setData(BackupResult backupResult) {
		PamConnection con = DBControlUnit.findConnection();
		SQLTypes sqlTypes = con.getSqlTypes();
		tableDef.getIndexItem().setValue(backupResult.getDbIndex());
		utc.setValue(sqlTypes.getTimeStamp(backupResult.getStartTime()));
		endUTC.setValue(sqlTypes.getTimeStamp(backupResult.getEndTime()));
		schedule.setValue(backupSchedule.getName());
		result.setValue(backupResult.getResult() == null ? null : backupResult.getResult().toString());
		detail.setValue(backupResult.getDetail() == null ? null : backupResult.getDetail());
		return true;
	}
	
	public boolean updateResult(BackupResult backupResult) {
		if (!checkTable()) {
			return false;
		}
		PamConnection con = DBControlUnit.findConnection();
		
		setData(backupResult);
		
		return cursor.immediateUpdate(con);		
	}
	
	public BackupResult getLastResult() {
		return getLastResult(null);
	}
	
	public BackupResult getLastSuccess() {
		String clause = String.format("WHERE %s='%s'", result.getName(), RESULT.Success.toString());
		return getLastResult(clause);
	}
	
	private BackupResult getLastResult(String clause) {
		String qStr = String.format("SELECT MAX(Id), UTC, EndUTC, Schedule, Result, Detail FROM %s", tableDef.getTableName());
		if (clause != null) {
			qStr += " " + clause;
		}
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		if (dbControl == null) {
			return null;
		}
		PamConnection con = dbControl.getConnection();
		if (con == null) {
			return null;
		}
		if (!checkTable()) {
			return null;
		}
		SQLTypes sqlTypes = con.getSqlTypes();
		BackupResult backupResult = null;
		try {
			Statement stmt = con.getConnection().createStatement();
			ResultSet resultSet = stmt.executeQuery(qStr);
			while (resultSet.next()) {
				int id = resultSet.getInt(1);
				if (id == 0) {
					break;
				}
				Long t1 = SQLTypes.millisFromTimeStamp(resultSet.getObject(2));
				Long t2 = SQLTypes.millisFromTimeStamp(resultSet.getObject(3));
				String shed = resultSet.getString(4);
				if (shed != null) {
					shed = shed.trim();
				}
				String res = resultSet.getString(5);
				BackupResult.RESULT r = null;
				if (res != null) {
					res = res.trim();
					if (res.equals(RESULT.Success.toString())) {
						r = RESULT.Success;
					}
					else if (res.equals(RESULT.Fail.toString())) {
						r = RESULT.Fail;
					}
					else {
						r = RESULT.Unknown;
					}
				}
				String det = resultSet.getString(6);
				if (det != null) {
					det = det.trim();
				}
				backupResult = new BackupResult(id, shed, t1, t2, r, det);
				break;
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return backupResult;
	}

	/**
	 * Check the table and be a little clever - not checking it again 
	 * if it's the same connection and it was OK last time. 
	 * <br>Also make a PamCursor at this point for i/o
	 * @return
	 */
	private boolean checkTable() {
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		if (dbControl == null) {
			return false;
		}
		PamConnection con = dbControl.getConnection();
		if (con == checkedConnection) {
			return true;
		}
		boolean ok = dbControl.getDbProcess().checkTable(tableDef);
		if (ok) {
			checkedConnection = con;
			cursor = PamCursorManager.createCursor(tableDef);
		}
		else {
			checkedConnection = null;
		}
		return ok;
	}
	
}
