package PamguardMVC.uid;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;

import PamController.PamController;
import PamguardMVC.PamDataBlock;
import dataMap.OfflineDataMap;
import generalDatabase.DBControlUnit;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamConnection;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;
import generalDatabase.pamCursor.NonScrollablePamCursor;
import generalDatabase.pamCursor.PamCursor;
import generalDatabase.pamCursor.PamCursorManager;
import warnings.PamWarning;

public class DatabaseUIDFunctions {

	private PamController pamController;
	private PamConnection checkedTableConnection;
	private EmptyTableDefinition trackerTableDef;
	private PamTableItem tableName;
    private PamTableItem lastUID;
    private String tableNameColumn = "tableName";
    private String uidColumn = "lastUID";
	public static final int STRING_LENGTH = 128;

	public DatabaseUIDFunctions(PamController pamController) {
		this.pamController = pamController;
		
		// Create table definition to keep track of the highest UID values in all the other tables.
		trackerTableDef = new EmptyTableDefinition("UIDTracker");
		trackerTableDef.addTableItem(tableName = new PamTableItem(tableNameColumn, Types.CHAR, STRING_LENGTH));
		trackerTableDef.addTableItem(lastUID = new PamTableItem(uidColumn, Types.BIGINT));
	}

	UIDStatusReport checkDataBlock(DBControlUnit dbControl, PamConnection pamCon, PamDataBlock dataBlock) {
		
		SQLLogging sqlLogging = dataBlock.getLogging();
		if (sqlLogging == null) {
			return null;
		}
		SQLTypes sqlTypes = pamCon.getSqlTypes();
		long nullUID = 0;
		long okUID = 0;
		long largestUID = 0;
		String sql = String.format("SELECT UID FROM %s", sqlLogging.getTableDefinition().getTableName());
		try {
			Statement stmt = pamCon.getConnection().createStatement();
			ResultSet result = stmt.executeQuery(sql);
			while (result.next()) {
				 Object data = result.getObject(1);
				 if (data == null) {
					 nullUID++;
				 }
				 else {
					 okUID++;
					 largestUID = Math.max(largestUID, (Long) data);
				 }
			}
			stmt.close();			
		} catch (SQLException e) {
			System.out.printf("Unable to query UID's in table %s using %s\n", sqlLogging.getTableDefinition().getTableName(), sql);
			e.printStackTrace();
			return new UIDStatusReport(okUID, nullUID, largestUID, UIDStatusReport.UID_DAMAGED);
		}
		return new UIDStatusReport(okUID, nullUID, largestUID);
	}
	
	
	/**
	 * Create the tracker table in the database.  Return a PamCursor for the table
	 * 
	 * @return a PamCursor pointing to the table.  Null if the method was unsuccessful
	 */
	public PamCursor createUIDTrackerTable() {
		DBControlUnit dbc = DBControlUnit.findDatabaseControl();
		if (dbc == null) {
			// no database so quit
			return null;
		}
		
		// check if the table already exists in the database.  If it does, warn the user and replace it with a new one
		if (checkTableExists()) {
			System.err.println("Error UID Tracker table already exists.  Deleting existing table and creating new one.");
			this.removeUIDTrackerTable();
		}
		
		// create the table in the database
		if (!createTable()) {
			System.err.println("Error creating UID Tracker table");
			return null;
		}

		// create the cursor and return
//		PamCursor c = PamCursorManager.createCursor(trackerTableDef);
		PamCursor c = dbc.createPamCursor(trackerTableDef);
//		c.openInsertCursor(checkedTableConnection);
		c.setCurrentConnection(checkedTableConnection);
		return c;
	}

	/**
	 * Check whether or not the tracker table exists.
	 * 
	 * @return true if the table exists
	 */
	public boolean checkTableExists() {
		DBControlUnit dbControlUnit = DBControlUnit.findDatabaseControl();
		if (dbControlUnit == null) {
			return false;
		}
		PamConnection con = DBControlUnit.findConnection();
		if (con == null) {
			return false;
		}
		checkedTableConnection = null;
		if (dbControlUnit.getDbProcess().tableExists(trackerTableDef)) {
			checkedTableConnection = con;
			return true;
		}
		return false;
	}
	
	/**
	 * Create the UID tracker table in the database
	 * 
	 * @return true if successful
	 */
	public boolean createTable() {
		DBControlUnit dbControlUnit = DBControlUnit.findDatabaseControl();
		if (dbControlUnit == null) {
			return false;
		}
		PamConnection con = DBControlUnit.findConnection();
		if (con == null) {
			return false;
		}
		checkedTableConnection = null;
		if (dbControlUnit.getDbProcess().checkTable(trackerTableDef)) {
			checkedTableConnection = con;
			return true;
		}
		return false;
	}
	
	/**
	 * Add the name/uid pair to the tracker table.
	 * 
	 * @return true if successful
	 */
	public boolean addToTable(PamCursor c, UIDTrackerData uidData) {
		c.moveToInsertRow();
		tableName.setValue(uidData.getName());
		lastUID.setValue(uidData.getUid());
		try {
			for (int i = 0; i < trackerTableDef.getTableItemCount(); i++) {
				c.updateObject(i+1, trackerTableDef.getTableItem(i).getValue());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		c.immediateInsert(c.getCurrentConnection());
		return true;
	}
	

	/**
	 * Returns an ArrayList of table name / highest UID pairs (contained in uidTrackerData objects) from
	 * the UID tracker table.
	 * 
	 * @return
	 */
	public ArrayList<UIDTrackerData> getAllUIDsfromTrackerTable() {

		// create the ArrayList to return
		ArrayList<UIDTrackerData> theData = new ArrayList<UIDTrackerData>();
		
		// create a pamCursor and get the information from the table
		PamCursor pamCursor = new NonScrollablePamCursor(trackerTableDef);
		ResultSet result = pamCursor.openReadOnlyCursor(checkedTableConnection, "ORDER BY Id");
		
		// go through the information one row at a time, transferring the column info into the ArrayList
		try {
			while (result.next()) {
				for (int i = 0; i < trackerTableDef.getTableItemCount(); i++) {
					
					// jump through some hoops to make sure we actually get a Long value when we retrieve the UID
					if (trackerTableDef.getTableItem(i).getName().equals(uidColumn)) {
						SQLTypes sqlTypes = checkedTableConnection.getSqlTypes();
						Long dummy = sqlTypes.getLongValue(result.getObject(i+1));
						trackerTableDef.getTableItem(i).setValue(dummy);
					} else {
						trackerTableDef.getTableItem(i).setValue(result.getObject(i+1));
					}
				}
				UIDTrackerData uidData = new UIDTrackerData(tableName.getStringValue(),lastUID.getLongValue());
				theData.add(uidData);
			}
		} catch (SQLException e) {
			System.err.println("Can''t add Name: " + tableName.getStringValue() + " UID: " + lastUID.getLongValue() + " to table.  Aborting.");
			e.printStackTrace();
			return null;
		} finally {
			pamCursor.closeCursors();
		}
		
		// return the ArrayList
		return theData;
	}
	
	/**
	 * Returns the highest UID number for the PamDataBlock passed.  This method first looks through
	 * the Tracker Table, and if it can't find a suitable UID it then looks through the database
	 * table directly associated with the PamDataBlock.  Returns -1 if there was a problem
	 * retrieving the information
	 * 
	 * @param dataBlock
	 * @return
	 */
	public long findMaxUIDforDataBlock(PamDataBlock dataBlock) {
		
		// Initialise return variable and get the connection to the database		
		long maxUID = -1;
		PamConnection pamCon = DBControlUnit.findConnection();
		if (pamCon==null) {
			return maxUID;
		}
		SQLTypes sqlTypes = pamCon.getSqlTypes();
		
		// first try the tracker table.  Generate the SQL statement and execute
		if (checkTableExists()) {
			String sql = String.format("SELECT %s FROM %s WHERE %s=\'%s\'",
					uidColumn,
					trackerTableDef.getTableName(),
					sqlTypes.formatColumnName(tableNameColumn),
					dataBlock.getLogging().getTableDefinition().getTableName());
			try {
				Statement stmt = pamCon.getConnection().createStatement();
				ResultSet result = stmt.executeQuery(sql);
				if (result.next()) {
					Object objVal = result.getObject(1);
					if (objVal!=null) {	// only get maxUID if there is a result
						maxUID = sqlTypes.getLongValue(result.getObject(1));
					}
				}
				stmt.close();

			} catch (SQLException e) {
				System.out.printf("Unable to query UID in tracker table using \"%s\": %s\n", sql, e.getMessage());
//				e.printStackTrace();
			}
		}	
		
		// if that didn't work, try to find the specific table
		if (maxUID==-1) {
			// generate the SQL statement and execute
			String sql = String.format("SELECT MAX(UID) FROM %s", dataBlock.getLogging().getTableDefinition().getTableName());
			try {
				Statement stmt = pamCon.getConnection().createStatement();
				ResultSet result = stmt.executeQuery(sql);
				if (result.next()) {
					Object objVal = result.getObject(1);
					if (objVal!=null) {	// only get maxUID if there is a result
						maxUID = sqlTypes.getLongValue(objVal);
					}
				}
				stmt.close();
			} catch (SQLException e) {
				System.out.printf("Unable to query UID in table \"%s\" using \"%s\": %s\n", 
						dataBlock.getLogging().getTableDefinition().getTableName(), sql, e.getMessage());
//				e.printStackTrace();
			}

		}

		// return the UID
		return maxUID;
	}
	
	
	/**
	 * Returns a list of UIDs from the tables in the current database
	 * 
	 * @return
	 */
	public ArrayList<UIDTrackerData> getUIDsFromAllTables() {

		// create the ArrayList to return
		ArrayList<UIDTrackerData> theData = new ArrayList<UIDTrackerData>();
		
		// get the connection to the database		
		DBControlUnit dbControlUnit = DBControlUnit.findDatabaseControl();
		PamConnection con = DBControlUnit.findConnection();
		
		// get list of all tables in the database
		ArrayList<String> allTables = dbControlUnit.getDbProcess().getListOfTables();
		
		// if there are no tables, return an empty ArrayList
		if (allTables == null) {
			return theData;
		}
		
		// loop through the list one table at a time, finding the Max value in the uid column
		for (String aTable : allTables ) {
			
			// create the SQL statement
			// don't bother using a PamCursor in this case, because we don't have
			// EmptyTableDefinition objects to create one with (we only have the
			// table names).  Instead, query the database directly.  Note that the
			// SQL statements here have been copied from the PamCursor class.
			SQLTypes sqlTypes = con.getSqlTypes();
			String str = String.format("SELECT MAX(UID) FROM %s", aTable);
			ResultSet result;
			Long maxUID = null;
			try {
				Statement stmt = con.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY);
				result = stmt.executeQuery(str);
				if (result.next()) {
					maxUID = sqlTypes.getLongValue(result.getObject(1));
				}
				stmt.close();
			} catch (SQLException e) {
				System.out.println("Error retrieving the UID value: " + str);
//				e.printStackTrace();
				continue;
			}
			
			// if the UID returned is null, there might be no data in the table or else there
			// is some sort of problem with the table.  If there aren't any rows, then the table
			// is empty so just ignore it.  If there are rows, it means that the UID values are
			// null and that should get repaired so warn the user
			if (maxUID==null) {
				str = String.format("SELECT COUNT(*) FROM %s", aTable);
				try {
					Statement stmt = con.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY,
							ResultSet.CONCUR_READ_ONLY);
					result = stmt.executeQuery(str);
					if (result.next()) {
						Long numRows = sqlTypes.getLongValue(result.getObject(1));
						if (numRows > 0) {
							System.out.println("Error reading max UID value from " + aTable);
							System.out.println("Please check the table manually and correct any problems");
						}
					}
					stmt.close();
				} catch (SQLException e) {
					System.out.println("Error counting number of records in " + aTable + " : " + str);
				}
				
				
			// add to theData array
			} else {
				UIDTrackerData uidData = new UIDTrackerData(aTable,maxUID);
				theData.add(uidData);
			}
		}
			
		return theData;
	}
	
	
	/**
	 * Remove the UID tracker table from the current database
	 * 
	 * @return true if successful
	 */
	public boolean removeUIDTrackerTable() {
		if (!checkTableExists()) {
			System.err.println("Can't remove - UID Tracker table doesn't exist");
			return true;
		}
		
		// delete the table
		boolean success = DBControlUnit.findDatabaseControl().getDbProcess().deleteTable(trackerTableDef);
		if (success) {
			return (DBControlUnit.findDatabaseControl().commitChanges());
		} else {
			return false;
		}
	}

	/**
	 * @param allDataBlocks
	 * @return
	 */
	public boolean saveUIDData(ArrayList<PamDataBlock> allDataBlocks) {

		// Check if the database is being used.  If not, exit right away
		DBControlUnit dbControlUnit = DBControlUnit.findDatabaseControl();
		if (dbControlUnit == null) {
			return true;
		}

		// create a new tracker table
		PamCursor c = this.createUIDTrackerTable();
		if (c == null) {
			return false;
		}
		
		// loop through list, searching for data blocks which have been saving information to the database
		for (PamDataBlock aDataBlock:allDataBlocks) {
			if (aDataBlock.getCanLog() && aDataBlock.getShouldLog()) {
				UIDTrackerData uidData = new UIDTrackerData(aDataBlock.getLogging().getTableDefinition().getTableName(), aDataBlock.getUidHandler().getCurrentUID());
				this.addToTable(c, uidData);
			}
		}
		
		// save all changes to the database
		c.updateDatabase();
		c.closeCursors();
		return true;
	}

}
