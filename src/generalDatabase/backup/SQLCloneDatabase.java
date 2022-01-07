package generalDatabase.backup;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.swing.JFrame;

import PamController.PamController;
import PamView.dialog.warn.WarnOnce;
import PamguardMVC.debug.Debug;
import backupmanager.BackupManager;
import backupmanager.BackupProgress;
import backupmanager.FileLocation;
import backupmanager.action.ActionMaker;
import backupmanager.action.BackupException;
import backupmanager.stream.BackupStream;
import backupmanager.stream.StreamItem;
import generalDatabase.DBControl;
import generalDatabase.DBControlUnit;
import generalDatabase.DBSystem;
import generalDatabase.PamConnection;
import generalDatabase.external.CopyManager;
import generalDatabase.external.CopyTableDefinition;
import generalDatabase.external.ExternalDatabaseControl;
import generalDatabase.external.TableInformation;
import generalDatabase.sqlite.SqliteSystem;

public class SQLCloneDatabase extends CopyDatabaseFile {

	private CopyManager copyManager;

	public SQLCloneDatabase(ActionMaker actionMaker, BackupStream backupStream) {
		super(actionMaker, backupStream);
	}


	@Override
	public boolean doAction(BackupManager backupManager, BackupStream backupStream, StreamItem streamItem) throws BackupException {
		DBControlUnit dbControlUnit = DBControlUnit.findDatabaseControl();
		FileLocation dest = getCopySettings().destLocation;
		if (dest == null) {
			throw new BackupException("No destination folder set");
		}
		String newFile = getNewDatabaseName(dest.path);
		return cloneDatabase(backupManager, backupStream, newFile);
	}

	// bit of a cheat to make a new temp name every call since it's not
	// always clearing it correctly. 
	int testCount = 0;
	/**
	 * Do a complete clone of the database into a new database ...<br>
	 * This method works for an SQLite database. I've no idea if it would 
	 * work for other database formats. 
	 * @param backupManager 
	 * @param parentWindow
	 * @param newDatabaseName
	 * @return
	 */
	public boolean cloneDatabase(BackupManager backupManager, BackupStream stream, String newDatabaseName) {

		DBControlUnit pamguardDatabase = DBControlUnit.findDatabaseControl();

		if (pamguardDatabase.getDatabaseSystem() instanceof SqliteSystem == false) {
			String msg = String.format("Datase backup for database types %s is not implemented", pamguardDatabase.getDatabaseSystem().getSystemName());
			WarnOnce.showWarning("Database backup", msg, WarnOnce.WARNING_MESSAGE, null);
			return false;
		}

		if (copyManager == null) {
			copyManager = new CopyManager(pamguardDatabase);
		}
		DBControl extControl = new ExternalDatabaseControl("Temp Ext");
		/*
		 * Want to make the same type of database, so find out what we're using ...
		 */
		DBSystem currSystem = pamguardDatabase.getDatabaseSystem();
		extControl.selectSystem(currSystem.getClass(), true, newDatabaseName);
		DBSystem extSystem = extControl.getDatabaseSystem();
		PamConnection con = extSystem.getConnection(newDatabaseName);


		backupManager.updateProgress(new BackupProgress(stream, this, BackupProgress.STATE.CATALOGING, 0, 0, "Finding database tables"));
		List<TableInformation> tableInfo = copyManager.getTableInfo(pamguardDatabase, extControl);
		System.out.println("Found tables = " + tableInfo.size());
		//			cloneTable(pamguardDatabase, extControl, newDatabaseName, table);
		// check all the tables
		int nTables = tableInfo.size();
		int nJobs = nTables;
		int iJob = 0;
		for (TableInformation tableInf : tableInfo) {
			//			System.out.println(tableInf.getTableName());
			String msg = String.format("Creating table %s", tableInf.getTableName());
			backupManager.updateProgress(new BackupProgress(stream, this, BackupProgress.STATE.RUNNING, nJobs, ++iJob, msg));
			boolean tblOk = extControl.getDbProcess().checkTable(tableInf.getSourceTableDef());
			if (tblOk == false) {
				msg = String.format("Creating table %s", tableInf.getTableName());
				Debug.out.println(msg);
			}
		}

		if (pamguardDatabase.getDatabaseSystem() instanceof SqliteSystem) {
			return attachAndCopy(backupManager, stream, pamguardDatabase, extControl, newDatabaseName, tableInfo);
		}
		else {
			return false;
			//			return queryCopy(backupManager, stream, pamguardDatabase, extControl, tableInfo);
		}
	}

	/**
	 * Copy between databases by running a query, reading and writing data. 
	 * @param backupManager
	 * @param pamguardDatabase
	 * @param extControl
	 * @param tableInfo
	 * @return
	 */
	private boolean queryCopy(BackupManager backupManager, BackupStream stream, DBControlUnit pamguardDatabase, DBControl extControl,
			List<TableInformation> tableInfos) {
		Connection dbCon = pamguardDatabase.getConnection().getConnection();
		Connection dbCon2 = extControl.getConnection().getConnection();
		int nTables = tableInfos.size();
		int nJobs = nTables;
		int iJob = 0;
		for (int iTable = 0; iTable < nTables; iTable++) {
			try {
				TableInformation tableInf = tableInfos.get(iTable);
				CopyTableDefinition tableDef = tableInf.getSourceTableDef();
				String msg = String.format("Copying table %s", tableInf.getTableName());
				backupManager.updateProgress(new BackupProgress(stream, this, BackupProgress.STATE.RUNNING, nJobs, ++iJob, msg));
				int nSrcItems = tableDef.getTableItemCount();
				String selStr = tableDef.getSQLSelectString(pamguardDatabase.getConnection().getSqlTypes());
				String insertStr = tableDef.getSQLInsertString(extControl.getConnection().getSqlTypes(), true);
				/*
				 *  Seems OK to copy Id's so long as they remain unique, for SQLite anyway. 
				 */ 				
				Statement selStmt = dbCon.createStatement();
				ResultSet resultSet = selStmt.executeQuery(selStr);

				PreparedStatement insStmt = dbCon2.prepareStatement(insertStr);
				while (resultSet.next()) {
					for (int i = 0; i < nSrcItems; i++) {
						insStmt.setObject(i+1, resultSet.getObject(i+1));
					}
					insStmt.execute();
				}

				resultSet.close();
			} catch (SQLException e1) {
				System.err.println(e1.getMessage());
			}
		}
		try {
			extControl.commitChanges();
			extControl.getConnection().getConnection().close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		// TODO Auto-generated method stub
		return false;
	}


	/**
	 * This works for SQLite datbasesbut not for all other types, so others will just run a query to 
	 * copy between databases. 
	 * @param backupManager
	 * @param pamguardDatabase
	 * @param extControl
	 * @param tableInfo
	 * @return
	 */
	private boolean attachAndCopy(BackupManager backupManager, BackupStream stream,  DBControlUnit pamguardDatabase, 
			DBControl extControl, String newDatabaseName, List<TableInformation> tableInfo) {
		// then close the other database so it can be attached to this connection
		Connection dbCon = pamguardDatabase.getConnection().getConnection();
		try {
			extControl.commitChanges();
			extControl.getConnection().getConnection().close();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// now get a list of tables to copy ...
		String tempName = "tmpDB" + ++testCount;
		int nTables = tableInfo.size();
		int nJobs = nTables*2;
		int iJob = nTables;
		Statement attStmt = null;
		boolean isAutoCommit = false;
		try {
			isAutoCommit = dbCon.getAutoCommit();
			dbCon.setAutoCommit(true); // needs AC on true. 
			attStmt = dbCon.createStatement();
			String att = String.format("ATTACH DATABASE \"%s\" AS %s", newDatabaseName, tempName);
			boolean ans = attStmt.execute(att);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		for (TableInformation tableInf : tableInfo) {
			String msg = String.format("Copying table %s", tableInf.getTableName());
			backupManager.updateProgress(new BackupProgress(stream, this, BackupProgress.STATE.RUNNING, ++iJob, nJobs, msg));
			System.out.println(tableInf.getTableName());
			try {
				String copySQL = String.format("INSERT INTO %s.%s SELECT * FROM %s", tempName, 
						tableInf.getTableName(), tableInf.getTableName());
				Statement cpyStmt = dbCon.createStatement();
				cpyStmt.execute(copySQL);
				cpyStmt.close();
//				dbCon.commit();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		try {
			attStmt.close();
			Statement detStmt = pamguardDatabase.getConnection().getConnection().createStatement();
			String det = String.format("DETACH DATABASE %s", tempName);
			boolean ans2 = detStmt.execute(det);
			detStmt.close();
			if (isAutoCommit == false) {
				dbCon.setAutoCommit(false);
			}
//			else {
//				dbCon.commit();
//			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return true;
	}


	@Override
	public String getName() {
		return "Clone database";
	}

}
