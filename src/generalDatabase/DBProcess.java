package generalDatabase;

import generalDatabase.ColumnMetaData.METACOLNAMES;
import generalDatabase.clauses.FixedClause;
import generalDatabase.clauses.FromClause;
import generalDatabase.clauses.PAMSelectClause;
import generalDatabase.pamCursor.PamCursor;
import generalDatabase.ucanAccess.UCanAccessSystem;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import warnings.PamWarning;
import warnings.WarningSystem;
import whistlesAndMoans.ConnectedRegionDataBlock;
import loggerForms.FormDescription;
import loggerForms.FormsControl;
import loggerForms.ItemInformation;
import loggerForms.UDFTableDefinition;
import loggerForms.formdesign.FormEditor;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.PamFolders;
import PamController.fileprocessing.StoreStatus;
import PamUtils.PamCalendar;
import PamUtils.PamFileChooser;
import PamView.dialog.warn.WarnOnce;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import PamguardMVC.debug.Debug;

public class DBProcess extends PamProcess {

	private DBControl databaseControll;

	private ArrayList<PamDataBlock> dataBlocks;

	private javax.swing.Timer timer;

	private int dbWriteOKs;

	private int dbWriteErrors;

	private ArrayList<DbSpecial> dbSpecials = new ArrayList<DbSpecial>();

	private Timer viewTimer;

	private PamWarning databaseWarning;

	private LogModules logModules;
	private LogSettings logSettings, logLastSettings;
	private LogSettings logViewerSettings;

	private PamCursor cursor;

	private PamWarning writeWarning;

	private DBCommitter dbCommitter;

	public DBProcess(DBControl databaseControll) {
		super(databaseControll, null);
		this.databaseControll = databaseControll;

		dbCommitter = new DBCommitter(databaseControll);

		databaseWarning = new PamWarning(databaseControll.getUnitName(), "", 0);

		writeWarning = new PamWarning("Database", "Database Write Error", 2);
		writeWarning.setWarningTip("Check your database connection");

		timer = new Timer(1000, new TimerAction());
		timer.start();

		viewTimer = new Timer(500, new ViewTimerAction());

		dbSpecials.add(logModules = new LogModules(databaseControll));
		dbSpecials.add(logSettings = new LogSettings(databaseControll, "Pamguard Settings", false));
		dbSpecials.add(logLastSettings = new LogSettings(databaseControll, "Pamguard Settings Last", true));
		dbSpecials.add(logViewerSettings = new LogSettings(databaseControll, "Pamguard Settings Viewer", true));
		
		dbSpecials.add(new LogXMLSettings(databaseControll));

	}

	@Override
	public void pamStart() {

		if (PamController.getInstance().getRunMode() == PamController.RUN_MIXEDMODE) {
			prepareForMixedMode();
		}
	}

	protected boolean saveStartSettings(long timeNow) {
		PamConnection con = databaseControll.getConnection();
		if (con != null) {
			/**
			 * This first one is the 'old' pre 2022 method which saves a serialised lump of all
			 * the settings in the database. It ain't broke, so not fixing it. 
			 */
			for (int i = 0; i < dbSpecials.size(); i++) {
				dbSpecials.get(i).pamStart(con);
			}
			return true;
		}
		return false;
	}

	protected boolean saveEndSettings(long timeNow) {
		
		return true;
	}

	@Override
	public void pamStop() {
		PamConnection con = databaseControll.getConnection();
		if (con != null) {
			for (int i = 0; i < dbSpecials.size(); i++) {
				dbSpecials.get(i).pamStop(con);
			}
		}
		viewTimer.stop();
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				databaseControll.commitChanges();
			}
		});
	}

	/**
	 * Called from the settings manager whenever settings would normally be saved to
	 * file. Just saves the latest of all PAMGUARD settings, first deleting any
	 * other settings in the logLastSettings table.
	 * <p>
	 * The logSettings object does a slightly different task of always storing the
	 * current PAMGAURD settings in a table which grows and grows, giving a
	 * permanent record of PAMGUARD settings over time.
	 * <p>
	 * Unlike the settings in the growing table of logSettings, the settings stored
	 * from logLastSettings are also stored when viewer or mixed mode is exited.
	 * 
	 * @return true if successful.
	 */
	public boolean saveSettingsToDB() {
		boolean ok = true;
		// DBControlUnit
		PamConnection con = databaseControll.getConnection();
		if (con == null) {
			return false;
		}

		databaseControll.reOpenConnection();
		// con.getDbSystem().reOpenConnection(con);

		if (clearTable(logLastSettings.getTableDefinition())) {
			ok |= logLastSettings.saveAllSettings();
		} else {
			ok = false;
		}
		if (databaseControll.isViewer()) {
			if (clearTable(logViewerSettings.getTableDefinition())) {
				ok |= logViewerSettings.saveAllSettings();
			} else {
				ok = false;
			}
		}
		return ok;
	}

	private void prepareForMixedMode() {
		PamDataBlock dataBlock;
		for (int i = 0; i < dataBlocks.size(); i++) {
			dataBlock = dataBlocks.get(i);
			if (dataBlock.getCanLog() && dataBlock.getMixedDirection() == PamDataBlock.MIX_OUTOFDATABASE) {
				dataBlock.getLogging().prepareForMixedMode(databaseControll.getConnection());
			}
		}
		viewTimer.start();
	}

	private void viewTimerAction() {
		PamDataBlock dataBlock;
		long timeTo = PamCalendar.getTimeInMillis();
		for (int i = 0; i < dataBlocks.size(); i++) {
			dataBlock = dataBlocks.get(i);
			if (dataBlock.getCanLog() && dataBlock.getMixedDirection() == PamDataBlock.MIX_OUTOFDATABASE) {
				dataBlock.getLogging().readMixedModeData(databaseControll.getConnection().getSqlTypes(), timeTo);
			}
		}
	}

	public synchronized void checkTables() {

		PamConnection dbCon = databaseControll.getConnection();
		if (dbCon == null) {
			return;
		}

		dataBlocks = PamController.getInstance().getDataBlocks();
		EmptyTableDefinition tableDefinition;
		SQLLogging logging;

		// for each datablock, check that the process can log (ignoring GPS process)
		if (databaseControll.isFullTablesCheck()) {
			for (int i = 0; i < dataBlocks.size(); i++) {
//				System.out.println("Check datablock " + dataBlocks.get(i).getDataName());
				logging = dataBlocks.get(i).getLogging();
				if (logging != null) {
//					System.out.println("Check table " + logging.getTableDefinition().tableName);
					if ((tableDefinition = logging.getTableDefinition()) != null) {
						if (tableDefinition.getCheckedConnection() != databaseControll.getConnection()) {
							if (checkTable(logging)) {
								tableDefinition.setCheckedConnection(databaseControll.getConnection());
							}
						}
					}
				}
			}
		}

		PamConnection con = databaseControll.getConnection();
		if (con != null) {
			for (int i = 0; i < dbSpecials.size(); i++) {
				logging = dbSpecials.get(i);
				if ((tableDefinition = logging.getTableDefinition()) != null) {
					if (tableDefinition.getCheckedConnection() != databaseControll.getConnection()) {
						if (checkTable(tableDefinition)) {
							tableDefinition.setCheckedConnection(databaseControll.getConnection());
						}
					}
				}
			}
		}
	}

	/**
	 * Similar to the checkTables method, this will copy the logger table
	 * information currently in memory to the database. Note that this refers to the
	 * form definitions table, beginning with the UDF_ prefix. If the table already
	 * exists in the database, the user is given the option to overwrite it with the
	 * current format in memory, or leave it in the database as is.
	 * 
	 * Note that a FormsControl object is also passed to this method, as a backup,
	 * because the user doesn't necessarily need a User Forms module in their setup.
	 * If they are only using the forms for annotations (say, for the detection group localiser)
	 * then all they need are the tables in the database.  But without a FormsControl object registered
	 * to PamController, the repopulateLoggerTables call will fail.
	 * 
	 * If there is a FormsControl object registered to PamController, or if formsControlTemp is null, then
	 * formsControlTemp is ignored.  The only time it is used is if there is no official Logger form module
	 * in the users setup, yet there are UDF tables in the database.
	 * 
	 * @param formsControlTemp a FormsControl object with information about UDF tables in the database.  Can be null.
	 * Ye
	 */
	public synchronized void repopulateLoggerTables(FormsControl formsControlTemp) {
		if (databaseControll.getConnection() == null)
			return; // if there is no database connection, exit immediately

		ArrayList<PamControlledUnit> loggerModules = PamController.getInstance()
				.findControlledUnits(FormsControl.class);
		
		if (loggerModules.isEmpty() && formsControlTemp!=null) {
			loggerModules.add(formsControlTemp);
		}
		
		if (!loggerModules.isEmpty()) {
			for (int i = 0; i < loggerModules.size(); i++) {
				FormsControl loggerModule = (FormsControl) loggerModules.get(i);

				int numTabs = loggerModule.getNumFormDescriptions();
				for (int j = 0; j < numTabs; j++) {
					FormDescription theForm = loggerModule.getFormDescription(j);

					// if the table doesn't exist, create it
					if (tableExists(theForm.getUdfTableDefinition()) == false) {
						checkTable(theForm.getUdfTableDefinition());
					}
					// if this table already exists in the database, warn the user that it's about
					// to
					// get overwritten and give them the option of skipping
					else {
						String title = "Overwrite Logger format table";
						String msg = "PAMGuard is about to overwrite the database table "
								+ theForm.getUdfTableDefinition().getTableName() + " with the " + theForm.getFormName()
								+ " form format currently in memory.  If this is correct, press Ok.  If you do not want to "
								+ "change the format stored in the database, press Cancel.<br><br>";
						int ans = WarnOnce.showWarning(PamController.getMainFrame(),
								title, msg, WarnOnce.OK_CANCEL_OPTION);
						if (ans == WarnOnce.CANCEL_OPTION) {
							continue;
						}
					}
					theForm.writeCompleteUDFTable();
					theForm.setNeedsUDFSave(false);
				}
			}
		}

	}

	/**
	 * Check a table and it's subtable if there is one.
	 * 
	 * @param sqlLogging
	 * @return
	 */
	public boolean checkTable(SQLLogging sqlLogging) {
		boolean ans = checkTable(sqlLogging.getTableDefinition());
		if (sqlLogging.doExtraChecks(this, databaseControll.getConnection()) == false) {
			return false;
		}
		if (sqlLogging instanceof SuperDetLogging) {
			SuperDetLogging superDetLogging = (SuperDetLogging) sqlLogging;
			if (superDetLogging.getSubLogging() == null) {
				System.out.printf("Super det logging %s has no subtable logging\n", sqlLogging.getTableDefinition().getTableName());
				return false;
			}
			ans &= checkTable(superDetLogging.getSubLogging());
		}
		return ans;
	}

	/**
	 * Check a database table. If it does not exist, create it.
	 * <p>
	 * Then check all columns and if a column does not exist, create that too.
	 * 
	 * @param tableDef table definition
	 * @return true if table is OK, i.e. table and all columns either existed or
	 *         were successfully created.
	 */
	public boolean checkTable(EmptyTableDefinition tableDef) {
		// check the table exists and column format for a particular table.
		if (databaseControll.getConnection() == null)
			return false;

//		if (tableDef.getTableName().equalsIgnoreCase("gpsdata")) {
		Debug.out.println("Checking table: " + tableDef.getTableName());
//		}

		if (tableExists(tableDef) == false) {
			createTable(tableDef);
			if (tableExists(tableDef) == false)
				return false;
		}

		if (tableDef.tableName.startsWith("UDF_")) {
			// fixUDFTableColumnNames(tableDef);
		} else { // this should now be PamTable
			fixLocalTimeProblem(tableDef);
		}

		int columnErrors = 0;

//		getAllColumnMetaData(tableDef.tableName);

		for (int i = 0; i < tableDef.getTableItemCount(); i++) {
			if (checkColumn(tableDef, tableDef.getTableItem(i)) == false) {
				columnErrors++;
			}
		}

		return (columnErrors == 0);
	}

	/**
	 * Returns a list of all table names in the database
	 * 
	 * @return
	 */
	public ArrayList<String> getListOfTables() {
		ResultSet tables = null;
		ArrayList<String> tableList = new ArrayList<String>();
		try {
			if (databaseControll.getConnection() == null) {
				return null;
			}
			DatabaseMetaData dbm = databaseControll.getConnection().getConnection().getMetaData();
			if (dbm == null) {
				return null;
			}
			tables = dbm.getTables(null, null, "%", null);

			// put the table names into the ArrayList. Note that the table name is
			// held in the 3rd column of the result set
			while (tables.next()) {
				tableList.add(tables.getString(3));
			}
		} catch (SQLException e) {
			System.out.println("Error retrieving table names");
			e.printStackTrace();
		}
		return tableList;
	}

	/**
	 * Check a database table exists.
	 * 
	 * @param tableDef table definition
	 * @return true if the table exists
	 */
	public boolean tableExists(EmptyTableDefinition tableDef) {
		boolean haveTable = false;
		if (tableDef == null) {
			return false;
		}
//		int hCount = 0;

		// if we don't even have a database connection yet, just return null
		if (databaseControll.getConnection() == null) {
			return false;
		}
		String[] types = { "TABLE" };
		SQLTypes sqlTypes = databaseControll.databaseSystem.getSqlTypes();
		String dbName = databaseControll.getDatabaseName();
		String tName = sqlTypes.formatTableName(tableDef.getTableName());
		try {
			DatabaseMetaData dbm = databaseControll.getConnection().getConnection().getMetaData();
//			ResultSet tables = dbm.getTables(dbName, null, tName, types);
			ResultSet tables = dbm.getTables(dbName, null, tName, types);
			int iTab = 0;
			while (tables.next()) {
				String tableName = tables.getString(3).trim();
//				Debug.out.printf("tableExists: db %s, want %s find %d:%s\n", databaseControll.getDatabaseName(),
//						tableDef.getTableName(), ++iTab, tableName);
				if (tableName == null) {
					continue;
				}
				if (tableName.equalsIgnoreCase(tableDef.getTableName())) {
					haveTable = true;
					break;
//					hCount++;
				}
			}
//			if (databaseControll.databaseSystem.getSystemName().equals(OOoDBSystem.SYSTEMNAME)) {
//				ResultSet oodbTables = dbm.getTables(null, null, /* tableDef.getTableName().toUpperCase() */null, null);
//
//				while (oodbTables.next()) {
//
//					if (oodbTables.getString(3).trim().equalsIgnoreCase(tableDef.getTableName())) {
//						// System.out.println("Table Found: "+oodbTables.getString(3));
//						tableDef.setTableName(oodbTables.getString(3).trim().toUpperCase());
//						haveTable = true;
//					}
//
//				}
//
//				System.out.println("Table Not Found: " + tableDef.getTableName().toUpperCase());
//			}
			tables.close();
		} catch (SQLException e) {
			System.out.println("Error in tableExists(): " + e.getMessage());
//			e.printStackTrace();
		}
//		Debug.out.printf("%d instances of table %s found in %s\n", hCount, 
//				tableDef.getTableName(), databaseControll.getDatabaseName());
		return haveTable;
	}

	/**
	 * From early 2008 (release 1.1) until August 2008 (release 1.1.1) A LocalTime
	 * column had been added to database tables to store the local computer time
	 * with each record as well as UTC. This feature was only tested with MS Access.
	 * <p>
	 * Unfortunately, it transpires that LocalTime is a reserved word in MySQL, so
	 * while MS Access databases worked OK, it became impossible to create tables
	 * with MySQL.
	 * <p>
	 * The column name has now been changed from LocalTime to PCLocalTime. New
	 * databases will not be affected, however, there are now old MS Access
	 * databases out there that have a LocalTime column, where we need a PCLocalTime
	 * column.
	 * <p>
	 * This function attempts to fix this automatically.
	 * 
	 * @param tableDef table def of table to fix.
	 */
	private static String expMessage = "<html>From V 1.1.1 the column LocalTime was renamed to "
			+ "PCLocalTime for compatibility with MySQL. "
			+ "<p>A new PCLocalTime columnn will be created and data in the LocalTime column will be duplicated</html>";

	void fixLocalTimeProblem(EmptyTableDefinition tableDef) {
		/*
		 * If the DB is NOT MS Access, not need to do anything. while the error occurred
		 * it would have been impossible to create any other type of database.
		 */
		if (databaseControll.databaseSystem.getSystemName().equals(MSAccessSystem.SYSTEMNAME) == false) {
			return;
		}
		boolean hasPCLocalTime = columnExists(tableDef, "PCLocalTime", Types.TIMESTAMP);
		boolean hasLocalTime = columnExists(tableDef, "LocalTime", Types.TIMESTAMP);

		/*
		 * If it already has a PCLocalTime column, then we've probably been here before
		 * and all is OK,
		 */
		if (hasLocalTime == true && hasPCLocalTime == false) {
			// we have a problem !
			int ans = JOptionPane.showConfirmDialog(null, expMessage, "Database table " + tableDef.getTableName(),
					JOptionPane.OK_CANCEL_OPTION);
			if (ans == JOptionPane.CANCEL_OPTION) {
				return; // user doens't want to do anything.
			}
			// create the new column, then try to duplicate the data
			PamTableItem ti = tableDef.findTableItem("PCLocalTime");
			addColumn(tableDef, ti);

			String renameStr = String.format("UPDATE \"%s\" SET \"PCLocalTime\" = \"LocalTime\"",
					tableDef.getTableName());

			Statement stmt;

			try {
				stmt = databaseControll.getConnection().getConnection().createStatement();
				ans = stmt.executeUpdate(renameStr);
			} catch (SQLException ex) {
				System.out.println(String.format("Error %d in %s", ex.getErrorCode(), renameStr));
				ex.printStackTrace();
				return;
			}
		}
	}

	/**
	 * 
	 * @param oldName
	 * @param newName
	 * @return true if worked
	 */
	public boolean renameTable(String oldName, String newName) {

		// String renameString= "ALTER TABLE \""+oldName+"\" RENAME TO \""+newName+"\"";

		String copyString = "SELECT * INTO \"" + newName + "\" FROM \"" + oldName + "\"";// SELECT * may not work at
																							// times also quotes
		String dropString = "DROP TABLE \"" + oldName + "\"";

		Statement stmt;

		try {
			stmt = databaseControll.getConnection().getConnection().createStatement();
			int renameResult;
			renameResult = stmt.executeUpdate(copyString);
			// System.out.println("renRes:"+renameResult);

			copyString = dropString;
			renameResult = stmt.executeUpdate(copyString);

		} catch (SQLException ex) {
			System.out.println(copyString);
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 
	 * @param tableName
	 * @param oldName
	 * @param newName
	 * @return true if worked
	 */
	boolean renameColumn(String tableName, String oldName, String newName) {

		String renameString = "ALTER TABLE \"" + tableName + "\" RENAME COLUMN \"" + oldName + "\" TO \"" + newName
				+ "\"";

		String newColumnStrin = "ALTER TABLE NEW COLUMN \"" + newName + "\"";
		String copyDataString = "UPDATE Table1 SET Table1.Field2 = [Table1]![Field1]";

		Statement stmt;

		try {
			stmt = databaseControll.getConnection().getConnection().createStatement();
			int renameResult;
			System.out.println(renameString);
			renameResult = stmt.executeUpdate(renameString);
		} catch (SQLException ex) {
			// System.out.println(renameString);
			System.out.println(ex.getMessage() + " " + renameString);
			// ex.printStackTrace();
			return false;
		}
		return true;
	}

	// private void fixLocalTimeProblem1(EmptyTableDefinition tableDef){
	//
	// //If the DB is NOT MS Access, not need to do anything. while the error
	// occurred
	// //it would have been impossible to create any other type of database.
	//
	//
	// if
	// (databaseControll.databaseSystem.getSystemName().equals(MSAccessSystem.SYSTEMNAME)
	// == false) {
	// return;
	// }
	// renameTableColumn(tableDef, "LocalTime","PCLocalTime",false);
	// }

	// private void fixUDFTableColumnNames(EmptyTableDefinition tableDef){
	// renameTableColumnIfNecessary(tableDef,"NMEA String" ,"NMEA_String" ,
	// true);//Access handles spaces ok.
	// renameTableColumn(tableDef,"Default" ,"DefaultField", true);

	// }

	/**
	 * Check a database table column exists. If it doesn't exist, attempt to create
	 * it.
	 * 
	 * @param tableDef  table definition
	 * @param tableItem table item
	 * @return true if column existed or was created.
	 */
	public boolean checkColumn(EmptyTableDefinition tableDef, PamTableItem tableItem) {
		if (columnExists(tableDef, tableItem) == false) {
			if (addColumn(tableDef, tableItem) == false)
				return false;
			populateNewColumn(tableDef, null, tableItem);
		}
		if (tableItem.isPrimaryKey()) {
			boolean ispk = isPrimarykey(tableDef.getTableName(), tableItem.getName());
			if (ispk == false) {
				String tit = "Database table " + tableDef.tableName;
				String msg = String.format("<html>Column \"%s\" in the table \"%s\" should be a primary key, but this is not the case,"
						+ "<br>Database behaviour may become very unpredictable unless this is fixed." +
						"<p><p>Normally this can only occurr if you've created the table yourself, or 'fiddled' with it in some way.</html>", 
						tableItem.getName(), tableDef.getTableName());
				WarnOnce.showWarning(tit, msg, WarnOnce.WARNING_MESSAGE);
			}
		}
//		if (tableItem.isRequired()) {
//
//		}

		return true;
	}

	/**
	 * Copies the data from one column to another
	 * 
	 * @param tableDef           the table to modify
	 * @param tableColWithValues the table column to copy the values FROM
	 * @param tableColToUpdate   the table column to copy the values TO
	 * @return true if data was properly copied
	 */
	public boolean populateNewColumn(EmptyTableDefinition tableDef, PamTableItem tableColWithValues,
			PamTableItem tableColToUpdate) {

		// Special Case: if we've just added the UID column, populate it with the values
		// from the Index column
		// so that it doesn't just contain nulls (which is the default when a column is
		// added)
		String copyStr = null;
		if (tableColToUpdate.getName().equals("UID")) {
//			copyStr = String.format("UPDATE %s SET %s = %s", tableDef.getTableName(),
//					tableColToUpdate.getName(), tableDef.getIndexItem().getName());
		} else if (tableColWithValues != null) {
			copyStr = String.format("UPDATE %s SET %s = %s", tableDef.getTableName(), tableColToUpdate.getName(),
					tableColWithValues.getName());
		}
		if (copyStr != null && runStmt(copyStr) == false) {
			System.out.println("Error trying to copy data between columns: " + copyStr);
			return false;
		}
		return true;
	}

	/**
	 * Check that a specific table column exists
	 * 
	 * @param tableDef  table definition
	 * @param tableItem table item
	 * @return true if the column exists
	 */
	public boolean columnExists(EmptyTableDefinition tableDef, PamTableItem tableItem) {
		return columnExists(tableDef, tableItem.getName(), tableItem.getSqlType());
	}

	/**
	 * Check a specific table column exists
	 * 
	 * @param tableDef   table definition
	 * @param columnName column name
	 * @param sqlType    column sql type
	 * @return true if the column exists and has the correct format.
	 */
	private boolean columnExists(EmptyTableDefinition tableDef, String columnName, int sqlType) {
		return columnExists(tableDef.getTableName(), columnName, sqlType);
	}
	

	private synchronized ArrayList<ColumnMetaData> getAllColumnMetaData(String tableName) {
		ArrayList<ColumnMetaData> allData = new ArrayList<>();
		try {
			DatabaseMetaData dbm = databaseControll.getConnection().getConnection().getMetaData();
			if (dbm == null) {
				return null;
			}
			/*
			 * Should be more efficient to include the column name as the 4th paramter, BUT
			 * names which are key words such as Default and Order which are used in UDF
			 * tables don't create a valid dataset with UCanAccess. However if we retreive
			 * all column names and go through the list, everything is fine.
			 */
			ResultSet columns = dbm.getColumns(null, null, tableName, null);
//			dbm.get
//			int iCol = 0;
			while (columns.next()) {

				// now check the format
				String colName = columns.getString(4);
				int colType = columns.getInt(5);
				ColumnMetaData colData = new ColumnMetaData(tableName, colName, colType);
				allData.add(colData);
				// and try to add all the other possible information
				METACOLNAMES[] fields = ColumnMetaData.METACOLNAMES.values();
//				for (int i = 0; i < fields.length; i++) {
//					try {
//						Object o = columns.getObject(fields[i].name());
//						Debug.out.printf("column %s field %d type %s is " + o + "\n", colName, i, fields[i].name());
//					}
//					catch (Exception e) {
//						System.out.println("Check table error: " + e.getMessage());
//					}
//				}
			}
			columns.close();

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return allData;
	}
	
	/**
	 * Check if a column is a primary key ...
	 * @param tableName
	 * @param columnName
	 * @return
	 */
	private boolean isPrimarykey(String tableName, String columnName) {
		boolean pkFound = false;
		try {
			DatabaseMetaData dbm = databaseControll.getConnection().getConnection().getMetaData();
			if (dbm == null) {
				return false;
			}
			SQLTypes sqlTypes = databaseControll.databaseSystem.getSqlTypes();
			tableName = sqlTypes.formatTableName(tableName);
			ResultSet pkRows = dbm.getPrimaryKeys(null, null, tableName);
			while (pkRows.next()) {
				String pkName = pkRows.getString("COLUMN_NAME");
				if (columnName.equalsIgnoreCase(pkName)) {
					pkFound = true;
					break;
				}
			}
			pkRows.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return pkFound;
	}

	//	private synchronized boolean columnExists(String tableName, String columnName, int sqlType) {	/**
	/**
	 * Check a specific table column exists
	 * 
	 * @param tableName  table name
	 * @param columnName column name
	 * @param sqlType    column sql type
	 * @return true if the column exists and has the correct format.
	 */
	private synchronized boolean columnExists(String tableName, String columnName, int sqlType) {
		boolean haveColumn = false;
		SQLTypes sqlTypes = databaseControll.databaseSystem.getSqlTypes();
		try {
			DatabaseMetaData dbm = databaseControll.getConnection().getConnection().getMetaData();
			if (dbm == null) {
				return false;
			}
			/**
			 * This is a nightmare. If I don't format the table name and columns (which pushes them 
			 * to lower case) for postgreSQL, then dbm.getcolumns doesn't work. If I do, then 
			 * with the "" for MySQLite, that stops working. for now priority it to make sure 
			 * SQLite is working, will worry about other DBMS later.  
			 */
//			columnName = sqlTypes.formatColumnName(columnName);
			tableName = sqlTypes.formatTableName(tableName);
			/*
			 * Should be more efficient to include the column name as the 4th paramter, BUT
			 * names which are key words such as Default and Order which are used in UDF
			 * tables don't create a valid dataset with UCanAccess. However if we retreive
			 * all column names and go through the list, everything is fine.
			 */
			ResultSet columns = dbm.getColumns(null, null, tableName, null);
//			int iCol = 0;
			while (columns.next()) {

				// now check the format
				String colName = columns.getString(4);
				int colType = columns.getInt(5);
//				Debug.out.printf("Table %s Column %d is %s\n", tableName, iCol++, colName);
				// if (colType == tableItem.getSqlType()) return true;
				// //String strColType = columns.getString(6);
				if (columnName.equalsIgnoreCase(colName)) {
					haveColumn = true;
					break;
				}
			}
			columns.close();

		} catch (SQLException e) {
			System.out.printf("Error in columnExists for %s col %s: %s\n", tableName, columnName, e.getMessage());
//			e.printStackTrace();
			return false;
		}

		return haveColumn;
	}

	/**
	 * Change the format of a column.
	 * 
	 * @return true if change sucessful
	 */
	public boolean changeColumnFormat(String tableName, PamTableItem tableItem) {
		/*
		 * Create a temp column, copy the data over, then rename the column.
		 */
		SQLTypes sqlTypes = databaseControll.databaseSystem.getSqlTypes();
		String tempColName = "tttteeeemmmmmpppp";
		String sqlCmd;
		if (columnExists(tableName, tempColName, tableItem.getSqlType())) {
			sqlCmd = String.format("ALTER TABLE %s DROP COLUMN %s", sqlTypes.formatTableName(tableName), tempColName);
			if (runStmt(sqlCmd) == false) {
				return false;
			}
		}

		// create the temp column
		sqlCmd = String.format("ALTER TABLE %s ADD COLUMN %s %s", sqlTypes.formatTableName(tableName), tempColName,
				sqlTypes.typeToString(tableItem.getSqlType(), tableItem.getLength()));
		if (runStmt(sqlCmd) == false) {
			return false;
		}

		// copy over the data
		sqlCmd = String.format("UPDATE %s SET %s = %s", sqlTypes.formatTableName(tableName), tempColName,
				sqlTypes.formatColumnName(tableItem.getName()));
		if (runStmt(sqlCmd) == false) {
			return false;
		}

		// delete the original column
		sqlCmd = String.format("ALTER TABLE %s DROP COLUMN %s", sqlTypes.formatTableName(tableName),
				sqlTypes.formatColumnName(tableItem.getName()));
		if (runStmt(sqlCmd) == false) {
			return false;
		}

		// create the temp column
		sqlCmd = String.format("ALTER TABLE %s ADD COLUMN %s %s", sqlTypes.formatTableName(tableName),
				sqlTypes.formatColumnName(tableItem.getName()),
				sqlTypes.typeToString(tableItem.getSqlType(), tableItem.getLength()));
		if (runStmt(sqlCmd) == false) {
			return false;
		}

		// copy over the data back to the replaced original column
		sqlCmd = String.format("UPDATE %s SET %s = %s", sqlTypes.formatTableName(tableName), 
				sqlTypes.formatColumnName(tableItem.getName()),
				tempColName);
		if (runStmt(sqlCmd) == false) {
			return false;
		}

		// delete the original column
		sqlCmd = String.format("ALTER TABLE %s DROP COLUMN %s", tableName, tempColName);
		if (runStmt(sqlCmd) == false) {
			return false;
		}

		return true;
	}

	/**
	 * Check to see if a column is empty.
	 * 
	 * @param tableName
	 * @param tableItem
	 * @return 0 if there are data, 1 if no rows at all and 2 if there are data, but
	 *         some are null
	 */
	public int columnNull(String tableName, PamTableItem tableItem) {
		SQLTypes sqlTypes = databaseControll.getConnection().getSqlTypes();
		String qStr = String.format("SELECT %s FROM %s", sqlTypes.formatColumnName(tableItem.getName()), tableName);
		int nRows = 0;
		int nNulls = 0;
		try {
			PamConnection con = databaseControll.getConnection();
			Statement stmt;
			stmt = con.getConnection().createStatement();
			ResultSet result = stmt.executeQuery(qStr);
			Object obj;
			while (result.next()) {
				obj = result.getObject(1);
				if (obj == null) {
					nNulls++;
				}
				nRows++;
			}
			stmt.close();
		} catch (SQLException ex) {
			System.out.println("Error in columnNull executing " + qStr);
			ex.printStackTrace();
			return -1;
		}
		if (nRows == 0) {
			return 1;
		} else if (nNulls > 0) {
			return 2;
		} else {
			return 0;
		}
	}

	private boolean runStmt(String str) {
		PamConnection con = databaseControll.getConnection();
		Statement stmt;
		try {
			stmt = con.getConnection().createStatement();
			int addResult;
			addResult = stmt.executeUpdate(str);
			stmt.close();
		} catch (SQLException ex) {
			System.out.println(str);
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Create the entire table from scratch using a single SQL command
	 * 
	 * @param tableDef Table definition structure
	 * @return true if table created successfully
	 * @see PamTableDefinition
	 */
	private synchronized boolean createTable(EmptyTableDefinition tableDef) {

		if (databaseControll.getConnection() == null)
			return false;
		if (tableDef == null)
			return false;
		SQLTypes sqlTypes = databaseControll.databaseSystem.getSqlTypes();
		PamTableItem tableItem;
		int nPrimaryKey = 0;
		String createString = "CREATE TABLE " + sqlTypes.formatTableName(tableDef.getTableName()) + " (";
		for (int i = 0; i < tableDef.getTableItemCount(); i++) {
			tableItem = tableDef.getTableItem(i);
			createString += sqlTypes.formatColumnName(tableItem.getName()) + " ";
			// command for making counters seems to be different for Access and MySQL !
			// Access uses counter and mysql uses integer auto_increment
			createString += sqlTypes.typeToString(tableItem.getSqlType(), tableItem.getLength(), tableItem.isCounter());
			if (tableItem.isCounter() && tableItem.getSqlType() == Types.INTEGER) {
				// createString += " ";
				// createString += " NOT NULL ";
			}
			if (i < tableDef.getTableItemCount() - 1) {
				createString += ", ";
			}
			if (tableItem.isPrimaryKey()) {
				nPrimaryKey++;
			}
		}
		if (nPrimaryKey > 0) {
			int usedPrimaryKeys = 0;
			createString += ", PRIMARY KEY (";
			for (int i = 0; i < tableDef.getTableItemCount(); i++) {
				tableItem = tableDef.getTableItem(i);
				if (tableItem.isPrimaryKey()) {
					createString += sqlTypes.formatColumnName(tableItem.getName());
					usedPrimaryKeys++;
					if (usedPrimaryKeys < nPrimaryKey) {
						createString += ", ";
					}
				}
			}
			createString += ")";
		}
		createString += " )";

		// createString = "CREATE TABLE testData45 (\"testCol\" TIMESTAMP, otherCol
		// INTEGER)";

//		System.out.println(createString);
		Statement stmt;

		try {
			stmt = databaseControll.getConnection().getConnection().createStatement();
			int createResult;
			createResult = stmt.executeUpdate(createString);
		} catch (SQLException ex) {
			System.out.println("Error in createTable executing " + createString);
			System.out.println("Because " + ex.getMessage());
//			ex.printStackTrace();
			return false;
		}
		// databaseController.registerTables();
		return true;
	}

	/**
	 * Deletes column from table,
	 * 
	 * @param tableDef  tableDef identifying the table.
	 * @param tableItem tableItem to delete if not isRequired or isPrimaryKey.
	 * @return true if successful.
	 */
	private boolean deleteColumn(EmptyTableDefinition tableDef, String columnName) { // Was Based on add column method
		// SQLTypes sqlTypes = databaseControll.databaseSystem.getSqlTypes();
		String delString = "ALTER TABLE " + tableDef.getTableName();
		delString += " DROP COLUMN " + columnName;
		// addString += sqlTypes.typeToString(tableItem.getSqlType(),
		// tableItem.getLength());
		// if (tableItem.isRequired() || tableItem.isPrimaryKey()) {
		// System.out.println(tableDef.getTableName()+" "+tableItem.getName()+" will not
		// be deleted as isRequired or isPrimaryKey");
		// return false;
		// }

		// System.out.println(addString);

		Statement stmt;

		try {
			stmt = databaseControll.getConnection().getConnection().createStatement();
			int delResult;
			delResult = stmt.executeUpdate(delString);
		} catch (SQLException ex) {
			System.out.println(delString);
			ex.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Deletes table
	 * 
	 * @param tableDef tableDef identifying the table to be deleted.
	 * @return true if successful.
	 */
	public boolean deleteTable(EmptyTableDefinition tableDef) {

		String delString = "DROP TABLE " + tableDef.getTableName();
		Statement stmt;

		try {
			stmt = databaseControll.getConnection().getConnection().createStatement();
			int delResult;
			delResult = stmt.executeUpdate(delString);
		} catch (SQLException ex) {
//			System.out.println(delString);
			System.out.printf("deleteTable can't execute \"%s\": %s\n", delString, ex.getMessage());
			return false;
		}

		return true;
	}

	private boolean addColumn(EmptyTableDefinition tableDef, PamTableItem tableItem) {

		if (databaseControll.databaseSystem.getClass() == UCanAccessSystem.class) {
			return addColumnUsingCOM(tableDef, tableItem);
		}

		SQLTypes sqlTypes = databaseControll.databaseSystem.getSqlTypes();
		String addString = "ALTER TABLE " + sqlTypes.formatTableName(tableDef.getTableName());

		if (tableItem.isCounter()
				&& databaseControll.databaseSystem.getSystemName().equals(MSAccessSystem.SYSTEMNAME)) {
			// Access and MySQL may handle these differently as per create table

			addString += " ADD " + sqlTypes.formatColumnName(tableItem.getName()) + " COUNTER ";

		} else {
			addString += " ADD COLUMN " + sqlTypes.formatColumnName(tableItem.getName()) + " ";
//			addString += " ADD COLUMN " + tableItem.getName() + " ";

			addString += sqlTypes.typeToString(tableItem.getSqlType(), tableItem.getLength(), tableItem.isCounter());
		}

		Statement stmt;

		try {
			stmt = databaseControll.getConnection().getConnection().createStatement();
			boolean addResult;
			System.out.println("Executing " + addString);
			addResult = stmt.execute(addString);
			stmt.close();
		} catch (SQLException ex) {
			System.out.println(addString + " " + ex.getMessage());
			// ex.printStackTrace();
			databaseWarning.setWarningMessage("Unable to execute: " + addString);
			databaseWarning.setWarnignLevel(2);
			databaseWarning.setWarningTip(ex.getMessage());
			WarningSystem.getWarningSystem().addWarning(databaseWarning);
			return false;
		}

		if (tableItem.isPrimaryKey()) {
			String primaryString = "ALTER TABLE " + tableDef.getTableName() + " ADD PRIMARY KEY ( "
					+ sqlTypes.formatColumnName(tableItem.getName()) + " )";
			;

			try {
				stmt = databaseControll.getConnection().getConnection().createStatement();
				int primaryResult;
				System.out.println(primaryString);
				primaryResult = stmt.executeUpdate(primaryString);
			} catch (SQLException ex) {
				System.out.println("Column added but could not be made primary key");
				System.out.println(primaryString);
				databaseWarning.setWarningMessage("Unable to execute: " + primaryString);
				databaseWarning.setWarnignLevel(2);
				databaseWarning.setWarningTip(ex.getMessage());
				WarningSystem.getWarningSystem().addWarning(databaseWarning);
				// ex.printStackTrace();

				return false;
			}
		}
		if (databaseWarning.getWarnignLevel() > 0) {
			WarningSystem.getWarningSystem().removeWarning(databaseWarning);
			databaseWarning.setWarnignLevel(0);
		}
		return true;

	}

	/**
	 * Special version for UCAnAccess according to ideas at
	 * http://stackoverflow.com/questions/35260429/how-to-alter-table-using-ucanaccess
	 * 
	 * @param tableDef
	 * @param tableItem
	 * @return
	 */
	private boolean addColumnUsingCOM(EmptyTableDefinition tableDef, PamTableItem tableItem) {
		// close the database first.

		String dbFileSpec = databaseControll.databaseSystem.getDatabaseName();
		SQLTypes sqlTypes = databaseControll.databaseSystem.getSqlTypes();
//		databaseControll.closeConnection();
		PrintWriter pw = null;
		File vbsFile = null;
		try {
			vbsFile = File.createTempFile("AlterTable", ".vbs");
			vbsFile.deleteOnExit();
			pw = new PrintWriter(vbsFile);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pw.println("Set conn = CreateObject(\"ADODB.Connection\")");
		pw.println("conn.Open \"Driver={Microsoft Access Driver (*.mdb, *.accdb)};DBQ=" + dbFileSpec + "\"");

//		addString += " ADD COLUMN " + sqlTypes.formatColumnName(tableItem.getName()) + " ";
//		addString += sqlTypes.typeToString(tableItem.getSqlType(), tableItem.getLength(), tableItem.isCounter());

		String exeString = String.format("conn.Execute \"ALTER TABLE %s ADD COLUMN %s %s\"", tableDef.getTableName(),
				tableItem.getName(), sqlTypes.typeToString(tableItem.getSqlType(), tableItem.getLength()));
		System.out.println(exeString);
//		pw.println("conn.Execute \"ALTER TABLE " + tableDef.tableName + "  ADD COLUMN " + 
//		tableItem.getName() + " " + " YESNO\"");
		pw.println(exeString);
		pw.println("conn.Close");
		pw.println("Set conn = Nothing");
		pw.close();

		// ... and execute it
		Process p;
		try {
			p = Runtime.getRuntime().exec("CSCRIPT.EXE \"" + vbsFile.getAbsolutePath() + "\"");
			p.waitFor();
			BufferedReader rdr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			int errorLines = 0;
			String line = rdr.readLine();
			while (line != null) {
				errorLines++;
				System.out.println(line); // display error line(s), if any
				line = rdr.readLine();
			}
			if (errorLines == 0) {
				System.out.println("The operation completed successfully.");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		databaseControll.reOpenConnection();
		return false;
	}

	/**
	 * Completely clear the contents of a table
	 * 
	 * @param tableDef tabledef identifying the table.
	 * @return true if successful.
	 */
	public boolean clearTable(EmptyTableDefinition tableDef) {
		return clearTable(tableDef.getTableName());
	}

	public boolean clearTable(String tableName) {

		String deleteStr = String.format("DELETE FROM %s", tableName);

		Statement stmt;

		if (databaseControll.getConnection() == null) {
			return false;
		}

		try {
			stmt = databaseControll.getConnection().getConnection().createStatement();
			int addResult;
			addResult = stmt.executeUpdate(deleteStr);
		} catch (SQLException ex) {
			System.out.println(deleteStr);
			ex.printStackTrace();
			return false;
		}

		return true;
	}

	public void updateProcessList() {
		ArrayList<PamDataBlock> dataBlocks = PamController.getInstance().getDataBlocks();
		PamDataBlock dataBlock;
		for (int i = 0; i < dataBlocks.size(); i++) {
			dataBlock = dataBlocks.get(i);
			if (dataBlock.getCanLog()) {
				dataBlock.addObserver(this);
			}
		}
	}

	@Override
	public void newData(PamObservable o, PamDataUnit dataUnit) {
		PamDataBlock dataBlock = (PamDataBlock) o;

		if (dataBlock.getLogging() != null && shouldLog(dataBlock, dataUnit)) {
			if (dataUnit.getDatabaseIndex() <= 0) {
				logData(dataBlock, dataUnit);
			}
			else {
				reLogData(dataBlock, dataUnit);
			}
		}
	}

	@Override
	public void updateData(PamObservable o, PamDataUnit dataUnit) {
		/*
		 * Some detectors may have inadvertently set the updateCount in the data unit >
		 * 0 in which case the notification of new data will end up here. Make the
		 * decision as to whether to update or write new based on existing database
		 * index information
		 */
		if (dataUnit.getDatabaseIndex() == 0) {
			newData(o, dataUnit);
			return;
		}
		// System.out.println("Updating database record for " + dataUnit);
		PamDataBlock dataBlock = (PamDataBlock) o;
		if (dataBlock.getLogging() != null && shouldLog(dataBlock, dataUnit)) {
			reLogData(dataBlock, dataUnit);
		}
	}

	/**
	 * Hope this doesn't happen during PamguardViewer. May sometimes happen during
	 * mixed operation. May need some fudges to make sure the right data are logged.
	 * 
	 * @param block
	 * @param unit
	 * @return
	 */
	private boolean logData(PamDataBlock block, PamDataUnit unit) {
		SQLLogging logger = block.getLogging();
		boolean ok = logger.logData(databaseControll.getConnection(), unit);
		if (ok) {
			dbWriteOKs++;
		} else {
			dbWriteErrors++;
			writeWarning.setWarningMessage("Write error for " + block.getDataName());
			writeWarning.setEndOfLife(unit.getTimeMilliseconds() + 5000);
			WarningSystem.getWarningSystem().addWarning(writeWarning);
		}

		dbCommitter.checkCommit(databaseControll.getConnection());

		return ok;
	}

	/**
	 * Hope this doesn't happen during PamguardViewer. May sometimes happen during
	 * mixed operation. May need some fudges to make sure the right data are logged.
	 * 
	 * @param block
	 * @param unit
	 * @return
	 */
	private boolean reLogData(PamDataBlock block, PamDataUnit unit) {
		SQLLogging logger = block.getLogging();
		boolean ok = logger.reLogData(databaseControll.getConnection(), unit);
		if (ok) {
			dbWriteOKs++;
		} else {
			dbWriteErrors++;
		}
		return ok;
	}

	private boolean shouldLog(PamDataBlock pamDataBlock, PamDataUnit pamDataUnit) {
		switch (PamController.getInstance().getRunMode()) {
		case PamController.RUN_NORMAL:
		case PamController.RUN_NETWORKRECEIVER:
			return pamDataBlock.getShouldLog(pamDataUnit);
		case PamController.RUN_MIXEDMODE:
			return (pamDataBlock.getMixedDirection() == PamDataBlock.MIX_INTODATABASE
					&& pamDataBlock.getShouldLog(pamDataUnit));
		case PamController.RUN_PAMVIEW:
			return pamDataBlock.getShouldLog(pamDataUnit);
		}
		return false;
	}

	class TimerAction implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			databaseControll.setWriteCount(dbWriteOKs, dbWriteErrors);
			dbWriteOKs = dbWriteErrors = 0;
		}

	}

	class ViewTimerAction implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			viewTimerAction();
		}

	}

	public LogSettings getLogSettings() {
		return logSettings;
	}

	public LogSettings getLogLastSettings() {
		return logLastSettings;
	}

	/**
	 * @return the logViewerSettings
	 */
	public LogSettings getLogViewerSettings() {
		return logViewerSettings;
	}

	/**
	 * Export all available database schema, converting PamTableDefinitions into 
	 * valid xsd documents. 
	 * @param parentFrame
	 */
	public void exportDatabaseSchema(JFrame parentFrame) {
		File startLoc = PamFolders.getFileChooserPath(PamFolders.getDefaultProjectFolder());
			PamFileChooser fc = new PamFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		startLoc = PamFolders.getFileChooserPath(startLoc);
		fc.setCurrentDirectory(startLoc);

		int ans = fc.showDialog(parentFrame, "Select storage folder");

		if (ans == JFileChooser.APPROVE_OPTION) {
			startLoc = fc.getSelectedFile();		
			exportDatabaseSchema(parentFrame, startLoc);
		}
		
	}

	/**
	 * Export all table definitions to xsd files in given folder. 
	 * @param parentFrame
	 * @param folder
	 */
	private void exportDatabaseSchema(JFrame parentFrame, File folder) {
		ArrayList<PamDataBlock> allDataBlocks = PamController.getInstance().getDataBlocks();
		DBSchemaWriter schemaWriter = new DBSchemaWriter();
		for (PamDataBlock aBlock : allDataBlocks) {
			SQLLogging logging = aBlock.getLogging();
			if (logging == null) {
				continue;
			}
			schemaWriter.writeSchema(folder, aBlock);
		}
	}

	/**
	 * Get the store status for use with pre-process checks. 
	 * @param getDetail get full details of start and end times. 
	 * @return database store status. 
	 */
	public StoreStatus getStoreStatus(DBControlUnit dbControlUnit, boolean getDetail) {
		DatabaseStoreStatus dbStoreStatus = new DatabaseStoreStatus(dbControlUnit);
		// and work out if any tables have anything in them already ...
		int status = 0;
		if (dbControlUnit.getConnection() == null) {
			status = StoreStatus.STATUS_MISSING;
		}
		else {
			boolean anyData = hasAnyOutputData(); 
			if (anyData) {
				status = StoreStatus.STATUS_HASDATA;
			}
			else {
				status = StoreStatus.STATUS_EMPTY;
			}
		}
		if (status == StoreStatus.STATUS_HASDATA && getDetail) {
			getStoreLimits(dbStoreStatus);
		}
		dbStoreStatus.setStoreStatus(status);
		return dbStoreStatus;
	}

	private void getStoreLimits(DatabaseStoreStatus dbStoreStatus) {
		ArrayList<PamDataBlock> allDataBlocks = PamController.getInstance().getDataBlocks();
		PamTableDefinition tableDefinition;
		SQLLogging logging;

		// for each datablock, check that the process can log (ignoring GPS process)
		for (int i = 0; i < allDataBlocks.size(); i++) {
			PamDataBlock aBlock = allDataBlocks.get(i);
			logging = aBlock.getLogging();
			if (logging == null) {
				continue; 
			}
			if (aBlock.getMixedDirection() != PamDataBlock.MIX_INTODATABASE) {
				continue; // don't want things like GPS data. 
			}
			getStoreLimits(aBlock, dbStoreStatus);
		}
		
	}

	/**
	 * Get first and last records for a table. 
	 * @param aBlock
	 * @param dbStoreStatus
	 */
	private void getStoreLimits(PamDataBlock aBlock, DatabaseStoreStatus dbStoreStatus) {
		// TODO Auto-generated method stub
		SQLLogging logging = aBlock.getLogging();
		PamConnection con = databaseControll.getConnection();
		SQLTypes sqlTypes = con.getSqlTypes();
		String q1 = String.format("SELECT MIN(UTC) FROM %s",sqlTypes.formatTableName(logging.getTableDefinition().getTableName())); 
		Long t = getUTC(con, q1);
		dbStoreStatus.testFirstDataTime(t);
		String q2 = String.format("SELECT MAX(UTC) FROM %s",sqlTypes.formatTableName(logging.getTableDefinition().getTableName())); 
		Long t2 = getUTC(con, q2);
		dbStoreStatus.testLastDataTime(t2);
	}

	private Long getUTC(PamConnection con, String qStr) {

		Object utcObject = null;
		try {
			PreparedStatement stmt = con.getConnection().prepareStatement(qStr);
			ResultSet result = stmt.executeQuery();
			if (result.next()) {
				utcObject = result.getObject(1);
				
			}
			result.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		if (utcObject == null) {
			return null;
		}
		Long millis = SQLTypes.millisFromTimeStamp(utcObject);
		return millis;
	}

	/**
	 * Is there any data in any output tables ? 
	 * @return
	 */
	private boolean hasAnyOutputData() {
		ArrayList<PamDataBlock> allDataBlocks = PamController.getInstance().getDataBlocks();
		PamTableDefinition tableDefinition;
		SQLLogging logging;

		// for each datablock, check that the process can log (ignoring GPS process)
		for (int i = 0; i < allDataBlocks.size(); i++) {
			PamDataBlock aBlock = allDataBlocks.get(i);
			logging = aBlock.getLogging();
			if (logging == null) {
				continue; 
			}
			if (aBlock.getMixedDirection() != PamDataBlock.MIX_INTODATABASE) {
				continue; // don't want things like GPS data. 
			}
			// get a record count.
			Integer count = logging.countTableItems(null);
			if (count != null && count > 0) {
				return true;
			}
		}

		return false;
	}

	public boolean deleteDataFrom(long timeMillis) {
		ArrayList<PamDataBlock> allDataBlocks = PamController.getInstance().getDataBlocks();
		PamTableDefinition tableDefinition;
		SQLLogging logging;

		// for each datablock, check that the process can log (ignoring GPS process)
		boolean ok = true;
		for (int i = 0; i < allDataBlocks.size(); i++) {
			PamDataBlock aBlock = allDataBlocks.get(i);
			logging = aBlock.getLogging();
			if (logging == null) {
				continue;
			}
			PAMSelectClause clause = new FromClause(timeMillis);			
			ok &= logging.deleteData(clause);
		}
		return ok;
	}

}
