package generalDatabase.external;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingsGroup;
import PamView.dialog.PamDialog;
import PamView.dialog.warn.WarnOnce;
import generalDatabase.DBControl;
import generalDatabase.DBControlSettings;
import generalDatabase.DBControlUnit;
import generalDatabase.DBSystem;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamConnection;
import generalDatabase.PamTableItem;
import generalDatabase.SQLTypes;
import generalDatabase.external.crossreference.CrossReferenceStatusMonitor;
import generalDatabase.external.crossreference.CrossReferenceUpdater;
import generalDatabase.pamCursor.PamCursor;

/**
 * Imports or exports data from an external database. 
 * @author dg50
 *
 */
public class CopyManager {

	private DBControlUnit pamguardDatabase;

	public CopyManager(DBControlUnit dbControl) {
		this.pamguardDatabase = dbControl;
	}

	public void addMenuItems(JMenu menu, final JFrame parentFrame) {
		JMenu exMenu = new JMenu("External");
		menu.add(exMenu);
		JMenuItem menuItem;
		menuItem = new JMenuItem("Export to database ...");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exportDatabase(parentFrame);
			}
		});
		exMenu.add(menuItem);
		menuItem = new JMenuItem("Import from database ...");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				importDatabase(parentFrame);
			}
		});
		exMenu.add(menuItem);
	}
	
	private void exportDatabase(JFrame parentFrame) {
		ExternalDatabaseControl externalDatabase = getExternalDatabase(parentFrame);
		if (externalDatabase != null) {
			copyDatabase(parentFrame, externalDatabase, CopyTypes.EXPORT);
		}
	}

	private void importDatabase(JFrame parentFrame) {
		ExternalDatabaseControl externalDatabase = getExternalDatabase(parentFrame);
		if (externalDatabase != null) {
			copyDatabase(parentFrame, externalDatabase, CopyTypes.IMPORT);
		}
	}

	private ExternalDatabaseControl getExternalDatabase(JFrame parentFrame) {
		ExternalDatabaseControl externalDatabase = new ExternalDatabaseControl("External Database"); 
		boolean selected = false;

		while (true) {
			selected = externalDatabase.selectDatabase(parentFrame, "Select External Database");
			if (selected == false) {
				PamDialog.showWarning(parentFrame, "External Database", "No External database selected");
				return null;
			}
			if (externalDatabase.getDatabaseSystem() == null) {
				PamDialog.showWarning(parentFrame, "External Database", "No External database selected");
				return null;
			}
			if (externalDatabase.getDatabaseName() == null) {
				PamDialog.showWarning(parentFrame, "External Database", "No External database selected");
				return null;
			}
			// make sure the external database is not the same as the current database.
			boolean isSame = isSameDatabase(externalDatabase);
			if (isSame) {
				PamDialog.showWarning(parentFrame, "External Database", "External Database must be different from the current PAMGuard database");
			}
			else {
				break;
			}

		}
		// check the connection is OK
		PamConnection con = externalDatabase.getConnection();
		if (con == null || con.getConnection() == null) {
			con = externalDatabase.reOpenConnection();
		}
		if (con == null || con.getConnection() == null) {
			PamDialog.showWarning(parentFrame, "External Database", "Unable to open external database " + externalDatabase.getDatabaseName());
			return null;
		}

		return externalDatabase;
	}

	boolean isSameDatabase(ExternalDatabaseControl externalDatabase) {
		DBSystem exSystem = externalDatabase.getDatabaseSystem();
		DBSystem intSystem = pamguardDatabase.getDatabaseSystem();
		String exName = exSystem.getDatabaseName();
		String intName = intSystem.getDatabaseName();
		if (exName.equalsIgnoreCase(intName) == false) {
			return false;
		}
		if (exSystem.getSystemName().equals(intSystem.getSystemName()) == false) {
			return false;
		}
		// it failed both tests to see if it's different, so it must be the same. 
		return true;
	}

	private void copyDatabase(JFrame frame, DBControl externalDatabase, CopyTypes copyDirection) {
		CopyDialog.showDialog(frame, this, externalDatabase, copyDirection);
	}

	/**
	 * check a table in the output database. 
	 * @param dbTo database controller data will be sent to
	 * @param tableDef table definition. 
	 */
	private void checkTable(DBControl dbTo, EmptyTableDefinition tableDef) {
		boolean tableOk = dbTo.getDbProcess().checkTable(tableDef);
		System.out.printf("%s table %s checked %s\n", dbTo.getDatabaseName(), tableDef.getTableName(), Boolean.toString(tableOk));
	}

	/**
	 * Extract all information about all tables in a database but don't do any checking 
	 * or changing of any of those tables. 
	 * @param sourceDatabase
	 * @param destDatabase 
	 * @return List of table definitions for all source database tables. 
	 */
	public List<TableInformation> getTableInfo(DBControl sourceDatabase, DBControl destDatabase) {
		List<String> tableNames = getTableNames(sourceDatabase);
		ArrayList<TableInformation> tableInformation = new ArrayList<>();
		int tableIndex = 0;
		for (String aTable:tableNames) {
			System.out.printf("Gathering table information for %s ignore printed SQL errors", aTable);
			TableInformation tableInfo = createPamTableInformation(sourceDatabase, destDatabase, tableIndex++, aTable);
			
			tableInformation.add(tableInfo);
		}
		return tableInformation;
	}
	
	/**
	 * Get a list of tables in a database.
	 * @param database
	 * @return list of table names
	 */
	private List<String> getTableNames(DBControl database) {
		DatabaseMetaData dbm;
		ArrayList<String> tableNames = new ArrayList<>();
		try {
			dbm = database.getConnection().getConnection().getMetaData();
			ResultSet tables = dbm.getTables(null, null, null, null);
			ResultSetMetaData rsmd = tables.getMetaData();
			
			int tableNameCol = 3;
			int sysInfCol = 4;
			while (tables.next()){
				String tableName = tables.getString(tableNameCol);
				String tableType = tables.getString(sysInfCol);
				if (tableType != null && tableType.contains("SYSTEM")) { 
					// some system tables which we can't import
					continue;
				}
				if (tableType != null && tableType.contains("VIEW")) { 
					// Queries from an MS Access database (and possibly others).
					// perhaps a future version should at least warn the user that there are queries in 
					// a database which will not be transferred across ?
					continue;
				}
				if (tableName != null) {
					tableNames.add(tableName);
				}
							
			}
						
			tables.close();
						
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return tableNames;
	}

//	/**
//	 * Slightly enhanced version Nov 2020 which will do things a little differently since we now need to 
//	 * merge tables, whereas previous versions only ever creating perfect copies and would drop existing tables
//	 * in order to make perfect copies. <br>
//	 * This now needs to look in the source and get the Id and date range, look in the dest and get the Id and date range, 
//	 * Check all columns in source, then copy over new data. 
//	 * @param sourceDatabase
//	 * @param destDatabase
//	 * @param tableIndex
//	 * @param tableName
//	 * @return
//	 */
//	private TableInformation createPamTableInformation2(DBControl sourceDatabase, DBControl destDatabase, int tableIndex, String tableName) {
//		return null;
//	}

	/**
	 * Get the minimum and maximum values for the Id field and corresponding values of the UTC field
	 * for one of the databases. This is needed for table merging. <br> It's possible that the table 
	 * doesn't exist, or is empty in which case that's OK, and values will be left null. 
	 * @param database Database
	 * @param tableName table name
	 * @param tableInfo existing table into from createPamTableInformation(...)
	 * @return
	 */
	private boolean getMinandMax(DBControl database, CopyTableDefinition tableInfo) {
		PamConnection pamConnection = database.getConnection();
		Long idMin = null, idMax = null;
		String utcMin = null, utcMax = null;
		Long utcMinVal = null, utcMaxVal = null;
		SQLTypes sqlTypes = pamConnection.getSqlTypes();
		// get the minimum
		if (database.getDbProcess().tableExists(tableInfo) == false) {
			return false; // the case for new tables in dest database
		}
		String qStr = String.format("SELECT %s, UTC FROM %s ORDER BY %s ASC", 
				sqlTypes.formatColumnName(EmptyTableDefinition.indexColName), 
				sqlTypes.formatTableName(tableInfo.getTableName()), 
				sqlTypes.formatColumnName(EmptyTableDefinition.indexColName));
		Connection con = pamConnection.getConnection();
		Statement stmt;
		try {
			stmt = con.createStatement();
			ResultSet result = stmt.executeQuery(qStr);
			if (result.next()) {
				idMin = (Long) result.getLong(1);
				utcMin = result.getString(2);
			}
			stmt.close();
		} catch (SQLException e) {
			System.out.println(qStr + ": " + e.getMessage());
			return false; // if there was no min, there is no chance of a max
		}

//		qStr = String.format("SELECT MAX(%s), UTC FROM %s", sqlTypes.formatColumnName(EmptyTableDefinition.indexColName), 
//				sqlTypes.formatTableName(tableInfo.getTableName()));
		// above wont work with postgresql
		 qStr = String.format("SELECT %s, UTC FROM %s ORDER BY %s DESC", 
				sqlTypes.formatColumnName(EmptyTableDefinition.indexColName), 
				sqlTypes.formatTableName(tableInfo.getTableName()), 
				sqlTypes.formatColumnName(EmptyTableDefinition.indexColName));
		try {
			stmt = con.createStatement();
			ResultSet result = stmt.executeQuery(qStr);
			if (result.next()) {
				idMax = (Long) result.getLong(1);
				utcMax = result.getString(2);
			}
			stmt.close();
		} catch (SQLException e) {
			System.out.println(qStr + ": " + e.getMessage());
		}

		if (utcMin != null) {
			utcMinVal = SQLTypes.millisFromTimeStamp(utcMin);
		}
		if (utcMax != null) {
			utcMaxVal = SQLTypes.millisFromTimeStamp(utcMax);
		}
		tableInfo.idMin = idMin;
		tableInfo.idMax = idMax;
		tableInfo.utcMin = utcMinVal;
		tableInfo.utcMax = utcMaxVal;
		
		return true;
	}

	/**
	 * Work out what types of copy are possible ...
	 * @param tableInfo table info should contain everything we need to know. 
	 * @return
	 */
	private int getCopyScenarios(TableInformation tableInfo) {
		// TODO Auto-generated method stub
		if (tableInfo.getSourceRecords() != null && tableInfo.getSourceRecords() == 0) {
			return TableInformation.SOURCE_EMPTY;
		}
		if (tableInfo.getDestTableExists() == false) {
			return TableInformation.DEST_EMPTY;
		}
		if (tableInfo.getDestRecords() != null && tableInfo.getDestRecords() == 0) {
			return TableInformation.DEST_EMPTY;
		}
		/*
		 *  there are data in both the source and dest databases. Check they both have an Id
		 *  and a utc column, otherwise it gets tricky to cross check.  
		 */
		if (doTableMatch(tableInfo)) {
			return TableInformation.TABLES_MATCH;
		}
		/**
		 * Otherwise, hoping that it's just a case of selecting everything
		 * with a higher UTC value ...
		 */
		CopyTableDefinition src = tableInfo.getSourceTableDef();
		CopyTableDefinition dst = tableInfo.getDestTableDef();
		if (src.utcMax != null && dst.utcMax != null && src.utcMax > dst.utcMax) {
			return TableInformation.COPY_ADDDATAOVERLAP;
		}
		
		return TableInformation.COPY_CONFUSED;
	}
	
	/**
	 * Check to see if UTC and UID information in two tables are the same. 
	 * @param tableInfo
	 * @return true if the tables seem already identical. 
	 */
	private boolean doTableMatch(TableInformation tableInfo) {
		CopyTableDefinition src = tableInfo.getSourceTableDef();
		CopyTableDefinition dst = tableInfo.getDestTableDef();
		if (src.idMin != dst.idMin) {
			return false;
		}
		if (src.idMax != dst.idMax) {
			return false;
		}
		if (src.utcMin != dst.utcMin) {
			return false;
		}
		if (src.utcMax != dst.utcMax) {
			return false;
		}
		if (tableInfo.getSourceRecords() != tableInfo.getDestRecords()) {
			return false;
		}
		return true;
	}

	/**
	 * Create a PAM table definition for a given table by extracting meta data from a database table. This
	 * doesn't count the records. that happens in the copy dialog later in a separate thread. 
	 * @param destDatabase 
	 * @param tableName
	 * @return
	 */
	private TableInformation createPamTableInformation(DBControl sourceDatabase, DBControl destDatabase, int tableIndex, String tableName) {
		
		CopyTableDefinition sourceTableDef = new CopyTableSourceDefinition(tableName);
		DestinationTableDefinition destTableDef = new DestinationTableDefinition(tableName);

		PamConnection pamConnection = sourceDatabase.getConnection();
		SQLTypes sqlTypes = pamConnection.getSqlTypes();
		try {
			DatabaseMetaData dbm = sourceDatabase.getConnection().getConnection().getMetaData();
			ResultSet columns = dbm.getColumns(null, null, tableName, null);
			ResultSetMetaData cmd = columns.getMetaData();
			int nCol = cmd.getColumnCount();

			while (columns.next()) {
				PamTableItem tableItem = sqlTypes.createTableItem(columns, cmd);
				PamTableItem existingItem = sourceTableDef.findTableItem(tableItem.getName());
				// these checks not needed since done anyway in addTableItem.
//				if (existingItem != null) {
////					System.out.printf("Table %s item %s already exists\n", tableName, tableItem.getName());
//				}
				//				else {
				sourceTableDef.addTableItem(tableItem);
				destTableDef.addTableItem(tableItem);
//				}
			}
			columns.close();
			
		}
		catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return new TableInformation(tableIndex, sourceTableDef, destTableDef);
	}

	/**
	 * @return the pamguardDatabase
	 */
	public DBControlUnit getPamguardDatabase() {
		return pamguardDatabase;
	}
	
	class TableInfoWorker extends SwingWorker<Integer, TableInformation> implements TableInformationMonitor {
		
		List<TableInformation> tableInfo;
		DBControl sourceDatabase;
		DBControl destDatabase;
		TableInformationMonitor tableInfoMonitor;
		
		public TableInfoWorker(List<TableInformation> tableInfo,
				DBControl sourceDatabase, DBControl destDatabase,
				TableInformationMonitor tableInfoMonitor) {
			this.tableInfo = tableInfo;
			this.sourceDatabase = sourceDatabase;
			this.destDatabase = destDatabase;
			this.tableInfoMonitor = tableInfoMonitor;
		}

		@Override
		protected Integer doInBackground() throws Exception {
			getTableInformation(tableInfo, sourceDatabase, destDatabase, this);
			return null;
		}

		@Override
		protected void process(List<TableInformation> chunks) {
			for (TableInformation chunk:chunks) {
				tableInfoMonitor.setTableInformation(chunk);
			}
		}

		@Override
		protected void done() {
			tableInfoMonitor.setTableInformation(null);
		}

		@Override
		public void setTableInformation(TableInformation tableInformation) {
			publish(tableInformation);
		}

		@Override
		public boolean carryOn() {
			return tableInfoMonitor.carryOn();
		}
		
	}
	
	/**
	 * Call the table information functions in a separate SwingWorker thread. 
	 * @param tableInfo
	 * @param sourceDatabase
	 * @param destDatabase
	 * @param tableInfoMonitor
	 */
	public void getTableInformationW(List<TableInformation> tableInfo, DBControl sourceDatabase, DBControl destDatabase, TableInformationMonitor tableInfoMonitor) {
		TableInfoWorker tableInfoWorker = new TableInfoWorker(tableInfo, sourceDatabase, destDatabase, tableInfoMonitor);
		tableInfoWorker.execute();
	}

	/**
	 * Get information about the state of all tables. 
	 * @param tableInfo
	 * @param sourceDatabase
	 * @param destDatabase
	 * @param tableInfoMonitor
	 */
	public void getTableInformation(List<TableInformation> tableInfo, DBControl sourceDatabase, DBControl destDatabase, TableInformationMonitor tableInfoMonitor) {
		for (TableInformation tableInf:tableInfo) {
			getTableInformation(tableInf, sourceDatabase, destDatabase, tableInfoMonitor);
		}
	}

	/**
	 * Get information about the state of a single table. 
	 * @param tableIndex 
	 * @param tableDef
	 * @param sourceDatabase
	 * @param destDatabase
	 * @param tableInfoMonitor
	 */
	private void getTableInformation(TableInformation tableInf,
			DBControl sourceDatabase, DBControl destDatabase,
			TableInformationMonitor tableInfoMonitor) {
		fillTableInformation(tableInf, sourceDatabase, destDatabase, tableInfoMonitor);
	}
	
	private void fillTableInformation(TableInformation tableInfo,
			DBControl sourceDatabase, DBControl destDatabase, TableInformationMonitor tableInfoMonitor) {
		if (sourceDatabase != null) {
			// count records in source database ...
			tableInfo.setCurrentAction("Counting source records");
			tableInfoMonitor.setTableInformation(tableInfo);
			int sourceRecords = countRecords(sourceDatabase, tableInfo.getSourceTableDef());
			tableInfo.setSourceRecords(sourceRecords);
		}		
		
		boolean destTableExists = destDatabase.getDbProcess().tableExists(tableInfo.getDestTableDef());
		tableInfo.setDestTableExists(destTableExists);
		
		if (destTableExists) {
			/*
			 * Only do these test if the table already exists so that the system doens't go wild 
			 * creating tables that the user may not want to export to. 
			 */
			// check output table is OK. 
			tableInfo.setCurrentAction("Checking output table");
			tableInfoMonitor.setTableInformation(tableInfo);
			boolean outTableOk = destDatabase.getDbProcess().checkTable(tableInfo.getDestTableDef());
			tableInfo.setDestTableOk(outTableOk);
			
			// count records in destination database ...
			tableInfo.setCurrentAction("Counting destination records");
			tableInfoMonitor.setTableInformation(tableInfo);
			Integer destRecords = countRecords(destDatabase, tableInfo.getDestTableDef());
			tableInfo.setDestRecords(destRecords);
		}

		tableInfo.setCurrentAction("Getting data ranges");
		tableInfoMonitor.setTableInformation(tableInfo);
		getMinandMax(sourceDatabase, tableInfo.getSourceTableDef());
		getMinandMax(destDatabase, tableInfo.getDestTableDef());
		Long minUTC = null;
		if (tableInfo.getDestTableDef() != null) {
			minUTC = tableInfo.getDestTableDef().utcMax;
		}
		tableInfo.setCurrentAction("Counting new records");
		Integer newRecords = countRecords(sourceDatabase, tableInfo.getTableName(), minUTC);
		tableInfo.setNewRecords(newRecords);
		
		tableInfo.setCopyScenario(getCopyScenarios(tableInfo));

		tableInfo.setCurrentAction("Table checks complete");
		tableInfoMonitor.setTableInformation(tableInfo);
	}

	/**
	 * Count the records in a database table.
	 * @param dbControl
	 * @param tableDef
	 * @return
	 */
	private Integer countRecords(DBControl dbControl,
			EmptyTableDefinition tableDef) {
		return countRecords(dbControl, tableDef.getTableName());
	}

	public static Integer countRecords(DBControl dbControl, String tableName) {
		return countRecords(dbControl, tableName, null);
	}
	/**
	 * Count the records in a database table. 
	 * @param dbControl
	 * @param tableName
	 * @return
	 */
	public static Integer countRecords(DBControl dbControl, String tableName, Long minUTC) {
		// really simple query to see how many records there are in the table. 
		int nRec = 0;
		SQLTypes sqlTypes = dbControl.getConnection().getSqlTypes();
		// use * since some non PAMguard tables may not have an Id column 
		String qStr = "SELECT COUNT(*) FROM "  + sqlTypes.formatColumnName(tableName);
		if (minUTC != null) {
			qStr += String.format(" WHERE UTC > %s", sqlTypes.formatDBDateTimeQueryString(minUTC));
		}
		PamConnection pCon = dbControl.getConnection();
		Connection con = pCon.getConnection();
		try {
			Statement stmt = con.createStatement();
			ResultSet result = stmt.executeQuery(qStr);
			if (result.next()) {
				nRec = result.getInt(1);
			}
			result.close();
			stmt.close();
		} catch (SQLException e) {
			System.out.println("Error executing query " + qStr);
			e.printStackTrace();
			return null;
		}
		
		return nRec;
	}

	/**
	 * Bit of a bodge to get it to call back round to the same point. Hasn't been correctly rethreaded. 
	 * @param sourceDatabase
	 * @param destDatabase
	 * @param tableInformation
	 * @param tableInformationMonitor
	 */
	public void dropAndReplace(DBControl sourceDatabase, DBControl destDatabase, TableInformation tableInformation, TableInformationMonitor tableInformationMonitor) {
		// attempt to drop the table, then call the standard getTableInfo worker functions which will eventually call 
		// back to the dialog. 
		if (tableInformation.getDestTableExists()) {
			dropTable(destDatabase, tableInformation.getDestTableDef().getTableName());
		}
		destDatabase.getDbProcess().checkTable(tableInformation.getDestTableDef());
		fillTableInformation(tableInformation, sourceDatabase, destDatabase, tableInformationMonitor);
	}

	private boolean dropTable(DBControl dbControl, String tableName) {
		String qStr = "DROP TABLE "  + tableName;
		PamConnection pCon = dbControl.getConnection();
		Connection con = pCon.getConnection();
		boolean result = false;
		try {
			Statement stmt = con.createStatement();
			result = stmt.execute(qStr);
		} catch (SQLException e) {
			System.out.println("Error executing statement " + qStr);
			e.printStackTrace();
			return false;
		}
		return result;
	}

	/**
	 * Attempt to create / automatically fix an output table 
	 * @param tableInformation
	 * @param tableInfoMonitor
	 */
	public boolean autoFixTable(DBControl destDatabase, TableInformation tableInformation,
			TableInformationMonitor tableInfoMonitor) {
		boolean tableOk = destDatabase.getDbProcess().checkTable(tableInformation.getDestTableDef());
		if (tableOk == false) {
			String msg = String.format("Output table %s has errors,  do you want to delete and recreate this table ?", 
					tableInformation.getTableName());
			int ans = JOptionPane.showConfirmDialog(null, msg, "Output Database Error", JOptionPane.YES_NO_OPTION);
			if (ans == JOptionPane.YES_OPTION) {
				dropAndReplace(null, destDatabase, tableInformation, tableInfoMonitor);
			}
		}
		return true;
	}

	class CopyDataWorker extends SwingWorker<Integer, TableInformation> implements TableInformationMonitor {
		
		private TableInformationMonitor tableInfoMonitor;
		
		private List<TableInformation> tableList;
		
		DBControl sourceDatabase, destDatabase;
		
		/**
		 * @param destDatabase 
		 * @param sourceDatabase 
		 * @param tableInfoMonitor
		 */
		public CopyDataWorker(DBControl sourceDatabase, DBControl destDatabase, List<TableInformation> tableList, TableInformationMonitor tableInfoMonitor) {
			super();
			this.sourceDatabase = sourceDatabase;
			this.destDatabase = destDatabase;
			this.tableList = tableList;
			this.tableInfoMonitor = tableInfoMonitor;
		}

		@Override
		protected Integer doInBackground() {
			copyData(sourceDatabase, destDatabase, tableList, tableInfoMonitor);
			return null;
		}

		@Override
		public void setTableInformation(TableInformation tableInformation) {
			this.publish(tableInformation);
		}

		@Override
		public boolean carryOn() {
			return tableInfoMonitor.carryOn();
		}

		/* (non-Javadoc)
		 * @see javax.swing.SwingWorker#process(java.util.List)
		 */
		@Override
		protected void process(List<TableInformation> chunks) {
			for (TableInformation tableInfo:chunks) {
				tableInfoMonitor.setTableInformation(tableInfo);
			}
			
		}
		
	}
	
	/**
	 * Copy the data in a list of tables, but in a separate Swing Worker thread. 
	 * @param tableList
	 * @param tableInfoMonitor
	 */
	public void copyDataW(DBControl sourceDatabase, DBControl destDatabase, List<TableInformation> tableList, TableInformationMonitor tableInfoMonitor) {
		CopyDataWorker cdw = new CopyDataWorker(sourceDatabase, destDatabase, tableList, tableInfoMonitor);
		cdw.execute();		
	}
	
	/**
	 * Copy the data in the list of Tableinformation's
	 * @param destDatabase 
	 * @param sourceDatabase 
	 * @param tableInfo list of table information
	 */
	public void copyData(DBControl sourceDatabase, DBControl destDatabase, List<TableInformation> tableInfo, 
			TableInformationMonitor tableInfoMonitor) {
		for (TableInformation tableInf:tableInfo) {
			copyData(sourceDatabase, destDatabase, tableInf, tableInfoMonitor);
			if (tableInfoMonitor.carryOn() == false) {
				break;
			}
		}
		tableInfoMonitor.setTableInformation(null);
	}

	/**
	 * Copy data from a single table
	 * @param tableInf
	 * @param tableInfoMonitor
	 */
	private void copyData(DBControl sourceDatabase, DBControl destDatabse, 
			TableInformation tableInf, TableInformationMonitor tableInfoMonitor) {
		/*
		 *  in future, may have to work a bit harder to part copy, overwrite, etc. 
		 *  but for now just copy everything over
		 */
		if (tableInf.getCopyChoice() == ImportOption.DONOTHING) {
			tableInf.setCopyStatus(CopyStatus.SKIPPED);
			tableInfoMonitor.setTableInformation(tableInf);
			return;
		}
		int nToDo = tableInf.getSourceRecords();
		int reportStep = nToDo / 200;
		reportStep = Math.min(Math.max(2, reportStep), 1000);
		if (nToDo == 0) {
			tableInf.setCopyStatus(CopyStatus.SKIPPED);
			tableInfoMonitor.setTableInformation(tableInf);
			return;
		}
		tableInf.setCopyStatus(CopyStatus.COPYING);
		tableInf.setCurrentAction("Copying Data");
		// set up a query for the source database. 
		EmptyTableDefinition sourceTableDef = tableInf.getSourceTableDef();
		DestinationTableDefinition destTableDef = tableInf.getDestTableDef();
		// if it's drop and replace, then drop it here ...
		if (tableInf.getCopyChoice() == ImportOption.DROPANDCOPY) {
			String msg = String.format("Are you sure you want to delete the contents of table %s ?", tableInf.getTableName());
			int ans = WarnOnce.showWarning("Copy database", msg, WarnOnce.OK_CANCEL_OPTION, null);
			if (ans == WarnOnce.CANCEL_OPTION) {
				return;
			}
			dropAndReplace(sourceDatabase, destDatabse, tableInf, tableInfoMonitor);
		}
		else { // new table or merge, still needs to check the table (will create table and columms if needed)
			// check the output table at this point. 
			boolean ok = destDatabse.getDbProcess().checkTable(destTableDef);
			if (ok == false) {
				String msg = String.format("Possible error checking table %s, do you want to continue ?", tableInf.getTableName());
				int ans = WarnOnce.showWarning("Copy database", msg, WarnOnce.OK_CANCEL_OPTION, null);
				if (ans == WarnOnce.CANCEL_OPTION) {
					return;
				}
			}
		}
		
		PamConnection sourceConnection = sourceDatabase.getConnection();
		PamConnection destConnection = destDatabse.getConnection();
		sourceDatabase.getDatabaseSystem().getSqlTypes().setAllowBlanks(true);
		PamCursor sourceCursor = sourceDatabase.getDatabaseSystem().createPamCursor(sourceTableDef);
		SQLTypes sourceTypes = sourceConnection.getSqlTypes();
		// If its a PAMGuard table, then it will have an Id column and we should sort by that. Otherwise it may not have an Id
		// column so it should not be sorted at all. 
		String clause = "";
		if (tableInf.getCopyChoice() == ImportOption.MERGERECORDS) {
			Long minUTC = tableInf.getDestTableDef().utcMax;
			if (minUTC != null) {
				clause = String.format("WHERE UTC > %s ", destDatabse.getConnection().getSqlTypes().formatDBDateTimeQueryString(minUTC));
			}
		}
		PamTableItem idCol = sourceTableDef.findTableItem("Id");
		ResultSet readCursor = null;
		if (idCol != null) {
			readCursor = sourceCursor.openReadOnlyCursor(sourceConnection, clause + " ORDER BY " + sourceTypes.formatColumnName(idCol.getName()) + " ASC");
		}
		else {
			readCursor = sourceCursor.openReadOnlyCursor(sourceConnection, clause);
		}
		if (readCursor == null) {
			// print a list of keywords into the console. 
			System.out.println("Database keywords for import database " + sourceConnection.getDbSystem().getDatabaseName());
			System.out.println(sourceConnection.getKeywords());
			
			String msg = "Unable to import table " + tableInf.getSourceTableDef().getTableName();
			String msg2 = "The table may have a column with spaces in it's name or which uses a reserved keyword\n" + 
			"See the console for a list of keywords for the database you are importing from.\n" + 
					"If a column name has a space, manually change this and then try the import again";
			JOptionPane.showMessageDialog(PamController.getMainFrame(), msg2, msg, JOptionPane.ERROR_MESSAGE);
			return;
		}
		PamCursor destCursor = destDatabse.getDatabaseSystem().createPamCursor(destTableDef);
		boolean autoCommit = true;
		try {
			destConnection.getConnection().setAutoCommit(false);
			autoCommit = false;
		} catch (SQLException e1) {
			autoCommit = true;
			System.out.println("Unable to turn off Auto Commit in dest database: " + e1.getLocalizedMessage());
		}
		int nRowsRead = 0;
		tableInf.setRowsCopied(0);
		PamTableItem tableItem;
		long uSec = System.currentTimeMillis();
		try {
			while (readCursor.next()) {
				// copy the data from the cursor to the table definition. 
				for (int i = 0; i < sourceTableDef.getTableItemCount(); i++) {
					tableItem = sourceTableDef.getTableItem(i);
					Object obj = readCursor.getObject(i + 1);
					if (obj != null && String.class.isAssignableFrom(obj.getClass())) {
						obj = ((String) obj).trim(); // deblank strings before copying over. Otherwise Lookups fail.
					}
					tableItem.setValue(obj);
				}
				// the table definition times are common to both the source and the dest definitions. 
				destTableDef.getCopyIdItem().setValue(sourceTableDef.getIndexItem().getValue());
				destCursor.immediateInsert(destConnection);
				
				nRowsRead++;
				tableInf.setRowsCopied(nRowsRead);
				if (nRowsRead%1000 == 0) {
					destCursor.closeCursors();
					if (autoCommit == false) {
						destConnection.getConnection().commit();
					}
				}
				
				if (nRowsRead%reportStep == 0) {
					long now = System.currentTimeMillis();
					double rate = (double) reportStep / ((double) (now - uSec + 1)) * 1000.;
					uSec = now;
					tableInf.setCurrentAction(String.format("Copied record %d of %d", nRowsRead, tableInf.getSourceRecords()));
					tableInfoMonitor.setTableInformation(tableInf);
					tableInf.setCopyRate(rate);
					tableInfoMonitor.setTableInformation(tableInf);
					if (tableInfoMonitor.carryOn() == false) {
						break;
					}
				}
			}
			if (autoCommit == false) {
				destConnection.getConnection().commit();
			}
			readCursor.close();
			destCursor.close();
			sourceCursor.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
			tableInf.setCopyStatus(CopyStatus.FAILED);
			tableInf.setCurrentAction("Copy Error");
			tableInfoMonitor.setTableInformation(tableInf);
		}
		tableInf.setCopyStatus(CopyStatus.DONE);
		tableInf.setCurrentAction("Copy Complete");
		fillTableInformation(tableInf, sourceDatabase, destDatabse, tableInfoMonitor);
		tableInfoMonitor.setTableInformation(tableInf);
	}

	/**
	 * Load the just imported settings from the database tables. 
	 * @param sourceDatabase 
	 */
	public void loadImportedSettings(DBControl sourceDatabase) {
		
//		sourceDatabase
		DBControlSettings dbControlSettings = new DBControlSettings();
		ArrayList<PamControlledUnitSettings> initialSettingList = dbControlSettings.loadSettingsFromDB(sourceDatabase.getConnection());
		
//		PamSettingManager psm = PamSettingManager.getInstance();
//		psm.loadDBSettings(0);
//		ArrayList<PamControlledUnitSettings> initialSettingList = psm.getInitialSettingsList();
		if (initialSettingList != null) {
			PamSettingsGroup psg = new PamSettingsGroup(0, initialSettingList);
			PamController.getInstance().loadOldSettings(psg, false);;
		}
	}

	public void createTables(DBControl destDatabase, List<TableInformation> tableInfo, TableInformationMonitor tableInformationMonitor) {
		for (TableInformation tableInf:tableInfo) {
			if (tableInf.getDestTableExists() == false) {
				dropAndReplace(null, destDatabase, tableInf, tableInformationMonitor);
			}
		}
	}

	/**
	 * Hopefully, the correct configuration is now loaded and we can check cross
	 * references. These may come from any module, but in reality, it's just the click
	 * detector that currently requires it. 
	 * @param crossReferenceMonitor 
	 */
	public void checkCrossReferencing(DBControl destDatabase, CrossReferenceStatusMonitor crossReferenceMonitor) {
		CrossReferenceUpdater crud = new CrossReferenceUpdater(destDatabase);
		crud.runInBackground(crossReferenceMonitor);
	}
	
}
