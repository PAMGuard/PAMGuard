package PamguardMVC.uid.repair;

import java.sql.SQLException;

import PamController.PamViewParameters;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import binaryFileStorage.BinaryOfflineDataMapPoint;
import clickDetector.ClickDetection;
import dataMap.OfflineDataMapPoint;
import generalDatabase.PamConnection;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;
import generalDatabase.pamCursor.NonScrollablePamCursor;

/**
 * Functions for updating data in a linked database table, i.e. UID's are 
 * being primarily set by counting up binary data, but some entries are also in 
 * a database table, so also need updating. <br>
 * Effectively this only ever happens with the offline clicks table. <br>
 * A system whereby a query was run to update record by record based on a search 
 * of the database to match the table record was way too slow, so need to have a quicker way 
 * whereby all the records from the database associated with a binary file are read (based on 
 * time or other information such as file name), then matching can take place in memory and 
 * the records can be more quickly updated in the database based on their Id (which is indexed so faster
 * querying). 
 * @author dg50
 *
 */
public class LinkedDatabaseUpdate {
	
	private PamDataBlock dataBlock;
	
	private NonScrollablePamCursor pamCursor;
	
	private SQLLogging sqlLogging;

	private int uidColumn;

	private int utcColumn;

	private int clickColIndex;
	
	public LinkedDatabaseUpdate(PamDataBlock dataBlock) {
		this.dataBlock = dataBlock;
		sqlLogging = dataBlock.getUIDRepairLogging();
		pamCursor = new NonScrollablePamCursor(sqlLogging.getTableDefinition());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		updateDatabase();
		pamCursor.closeScrollableCursor(); // doesn't do anything, so not really necessary. 
	}

	/**
	 * Return a WHERE clause to select records in the database associated
	 * with a map point. 
	 * @param mapPoint
	 * @return SQL string beginning with the word "WHERE ..."
	 */
	public String getFileQueryString(PamConnection con, BinaryOfflineDataMapPoint mapPoint) {
		SQLTypes sqlTypes = con.getSqlTypes();
		String st = String.format("WHERE UTC BETWEEN %s and %s", sqlTypes.formatDBDateTimeQueryString(mapPoint.getStartTime()-1000),
				sqlTypes.formatDBDateTimeQueryString(mapPoint.getEndTime()+1000));
		return st;
	}
	
	/**
	 * Return a WHERE clause to select records in the database with a time less than
	 * the mapPoint end time.  This method can also limit
	 * the results to only rows with a null in a specified column.
	 * @param con the PamConnection to use
	 * @param mapPoint the mapPoint containing the end time
	 * @param onlyNulls whether to only include rows with nulls in a specific column (true) or all rows (false)
	 * @param colNameToTest which column to check for nulls - ignored if onlyNulls is false
	 * @return
	 */
	public String getLessThanQueryString(PamConnection con, BinaryOfflineDataMapPoint mapPoint, boolean onlyNulls, String colNameToTest ) {
		SQLTypes sqlTypes = con.getSqlTypes();
		String clause = String.format(" WHERE UTC < %s ", sqlTypes.formatDBDateTimeQueryString(mapPoint.getEndTime()+1000));
		if (onlyNulls && colNameToTest != null) {
			clause += String.format(" AND %s IS NULL ", colNameToTest);
		}
		return clause;
	}

	public boolean loadFileDatabaseData(PamConnection con, BinaryOfflineDataMapPoint mapPoint) {
		/*
		 * Run a query to bring in necessary data, and store all in memory. 
		 * Should be able to use a PAMScrollableCursor for this ? 
		 */
		// change this string - instead of searching between two times, search instead
		// for entries less than the end time and where UID does not equal null
//		String qStr = getFileQueryString(con, mapPoint);
		String qStr = getLessThanQueryString(con, mapPoint, true, "UID");
		boolean ok = pamCursor.openScrollableCursor(con, true, true, qStr);
		if (!ok) return false;
		utcColumn = pamCursor.findColumn("UTC");
		uidColumn = pamCursor.findColumn("UID");
		
		return true;
	}
	
	/**
	 * Load up the pamCursor with a dataset of UID values == null
	 * @param con
	 * @return
	 */
	public boolean loadUIDNulls(PamConnection con) {
		SQLTypes sqlTypes = con.getSqlTypes();
		String clause = " WHERE UID IS NULL ";
		boolean ok = pamCursor.openScrollableCursor(con, true, true, clause);
		if (!ok) return false;
		uidColumn = pamCursor.findColumn("UID");
		return true;
	}
	
	/**
	 * Replace all the UID's in the PamCursors dataset with negative values.
	 * @param startingValue the negative value to start at
	 * @return the next negative value to use
	 */
	public Long replaceUIDsWithNegatives(Long startingValue) {
		pamCursor.beforeFirst();
		while (pamCursor.next()) {
			try {
				pamCursor.updateLong(uidColumn, startingValue);
				pamCursor.updateRow();
			} catch (SQLException e) {
				System.out.println("Error replacing UID with negative");
				e.printStackTrace();
			}
			startingValue--;
		}
		return startingValue;
	}
	
	/**
	 * Match the data unit to a cursor record and update that
	 * cursor record with the UID. 
	 * @param dataUnit
	 * @return
	 */
	public boolean updateCursorRecord(PamDataUnit dataUnit) {
		boolean ok = false;
		try {
			ok = moveToCursorRecord(dataUnit);
		} catch (SQLException e1) {
			e1.printStackTrace();
			return false;
		}
		// can return false quite often - for any record that's not in the database. 
		if (!ok) return false;
		try {
			pamCursor.updateLong(uidColumn, dataUnit.getUID());
			pamCursor.updateRow();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

	/**
	 * Scroll through the data in order to find the correct record. 
	 * @param dataUnit data unit to search for
	 * @return true if the correct record has been identified. 
	 * @throws SQLException
	 */
	private boolean moveToCursorRecord(PamDataUnit dataUnit) throws SQLException {
		
		// do a quick check to see if the dataUnit is a ClickDetection.  If so, figure
		// out the database column index now to save time later
		if (dataUnit instanceof ClickDetection) {
			clickColIndex = pamCursor.findColumn("ClickNo");
		}

		int startRow = pamCursor.getRow();
		if (startRow <= 0) {
			pamCursor.beforeFirst();
		}
		while (pamCursor.next()) {
			if (matchCursorRecord(dataUnit)) {
				return true;
			}
		}
		// if nothing was found wrap back to the start and work up 
		// until we reach the start row number
		pamCursor.beforeFirst();
		while (pamCursor.next()) {
			if (matchCursorRecord(dataUnit)) {
				// found it earlier in the recordset - data must be out of order which is not impossible.  
				return true;
			}
			if (pamCursor.getRow() >= startRow) {
				// give up
//				System.out.println("*** Warning: PAMGuard was not able to match binary file entry (file = " + dataUnit.getDataUnitFileInformation().getFile().getName() +
//						", UTC Time = " + PamCalendar.formatDBDateTime(pamCursor.getUTCTime()) +
//						" to a database entry (table = " + pamCursor.getTableDefinition().getTableName() +").  This may result in a database entry with null UID, which will prevent it from being loaded.  It is recommended that" +
//						" the user attempt to manually correct the database entry to match a binary file object.");
				return false;
			}
		}
		return false;
	}

	/**
	 * Function to match data units with a database record. If the data unit
	 * is a ClickDetection object, try to match the click number.  Otherwise,
	 * try to match the time.
	 * 
	 * @param dataUnit
	 * @return true if the data unit matches the selected database row. 
	 */
	public boolean matchCursorRecord(PamDataUnit dataUnit) {
//		long cursorMillis = pamCursor.getUTCTime();
//		return cursorMillis == dataUnit.getTimeMilliseconds();
		boolean matchFound = false;
		
		// check if the data unit is a ClickDetection.  If so, match the ClickNo column
		if (dataUnit instanceof ClickDetection) {

			ClickDetection thisUnit = (ClickDetection) dataUnit;
			int clickNo = pamCursor.getInt(clickColIndex);
			matchFound = (clickNo == thisUnit.getClickNumber());
		}
			
		// otherwise, match the time/milliseconds
		else {
			long cursorMillis = pamCursor.getUTCTime();
			matchFound = (cursorMillis == dataUnit.getTimeMilliseconds());
		}
		return matchFound;
	}
	
	/**
	 * push everything down into the database. 
	 * @return true if successful. 
	 */
	public boolean updateDatabase() {
		return pamCursor.updateDatabase();
	}
	
}
