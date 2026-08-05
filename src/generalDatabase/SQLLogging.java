/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

/**
 *
 * Facilitates data logging to database 
 *
 * @author David J McLaren, Douglas Gillespie, Paul Redmond
 *
 */

package generalDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
//import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;

import PamController.PamController;
import PamController.PamViewParameters;
import PamUtils.PamCalendar;
import PamguardMVC.DataUnitFinder;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.SaveRequirements;
import PamguardMVC.superdet.SubdetectionInfo;
import PamguardMVC.superdet.SuperDetection;
import binaryFileStorage.DataUnitFileInformation;
import generalDatabase.clauses.FixedClause;
import generalDatabase.clauses.PAMSelectClause;
import generalDatabase.pamCursor.CursorFinder;
import generalDatabase.pamCursor.PamCursor;
import pamScrollSystem.LoadQueueProgressData;
import pamScrollSystem.ViewLoadObserver;
import qa.QASoundDataUnit;
import warnings.PamWarning;
import warnings.RepeatWarning;

/**
 * 
 * SQLLogging performs basic SQL actions to write and read data from a database.
 * <p>
 * Each SQLLogging object is Associated with a PamDataBlock.
 * <p>
 * When a database is first connected, the last values entered into the database
 * will be read back into Pamguard.
 * <p>
 * When a PamDataUnit is added to a PamDataBlock, Pamguard will automatically
 * call the fillTableData function. This will automatically fill in any database
 * columns that are cross referenced to data from other tables. It will then
 * call the abstract function setTableData where the programmer is responsible
 * for filling data for other columns.
 * 
 * 
 * @author Doug Gillespie
 * @see PamguardMVC.PamDataBlock
 * @see generalDatabase.PamTableDefinition
 * @see generalDatabase.PamTableItem
 * 
 */
public abstract class SQLLogging {

	private PreparedStatement selectStatement;

	private Connection selectConnection;

	/**
	 * Reference to the table definition object.
	 * This MUST be set from within the concrete logging class.  
	 */
	private PamTableDefinition pamTableDefinition;
	
	/**
	 * More and more data blocks are starting to use annotations, which require
	 * modifying the table definition, adding additional columns. This get's a bit
	 * awkward, when annotations are being added and removed. To help, we're going 
	 * to try storing a baseTableDefinition the very first time that the main 
	 * table definition is set, so that it can be got and modified by the 
	 * annotation handler shortly after the main table is created.  
	 */
	private PamTableDefinition baseTableDefinition;

	//	private long selectT1, selectT2;
	private PamViewParameters currentViewParameters;

	private PamDataBlock pamDataBlock;

	private ArrayList<SQLLoggingAddon> loggingAddOns;

	/**
	 * time of last item loaded offline or mixed mode
	 */
	private long lastTime;

	/**
	 * Database index of last item loaded offline or mixed mode
	 */
	private int lastLoadIndex;

	/**
	 * UID of last item loaded offline or mixed mode
	 */
	private Long lastLoadUID;

	/**
	 * Channel map of last item loaded offline or mixed mode
	 */
	private int lastChannelBitmap;

	/**
	 * Sequence map of last item loaded offline or mixed mode.  Needs to
	 * be Integer instead of int because it might be null
	 */
	private Integer lastSequenceBitmap;

	private Statement dbStatement;

	private boolean canView = false;

	private boolean loadViewData = false;
	
	private RepeatWarning repeatWarning;

	protected SuperDetLogging superDetLogging;

	private CursorFinder loggingCursorFinder = new CursorFinder();
	private CursorFinder viewerCursorFinder = new CursorFinder();

	public CursorFinder getViewerCursorFinder() {
		return viewerCursorFinder;
	}


	// now only held within the TableDefinition
//	private int updatePolicy = UPDATE_POLICY_WRITENEW;
	public static final int UPDATE_POLICY_OVERWRITE = 1;
	public static final int UPDATE_POLICY_WRITENEW = 2;


	/**
	 * SQLLogging constructor.
	 */
	protected SQLLogging(PamDataBlock pamDataBlock) {
		this.pamDataBlock = pamDataBlock;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if (dbStatement != null) {
			dbStatement.close();
		}
		if (resultSet != null) {
			resultSet.close();
		}
	}

	/**
	 * Searches the Pamguard system for a SQLLogger with a given table name.
	 * Table name blanks are replaced with the _ character during the search.
	 * 
	 * @param tableName
	 *            table name to search for.
	 * @return reference to a SQLLogging
	 */
	final public static SQLLogging findLogger(String tableName) {
		String searchName = EmptyTableDefinition.deblankString(tableName);
		ArrayList<PamDataBlock> blockList = PamController.getInstance()
				.getDataBlocks();
		SQLLogging logger;
		EmptyTableDefinition tableDef;
		for (int i = 0; i < blockList.size(); i++) {
			if ((logger = blockList.get(i).getLogging()) != null) {
				tableDef = logger.getTableDefinition();
				if (tableDef == null) {
					continue;
				}
				if (tableDef.getTableName()
						.equalsIgnoreCase(searchName)) {
					return logger;
				}
			}
		}
		return null;
	}

	/**
	 * Data values going in and out of the database are stored with their
	 * respective PamTableItems. This function is used to set data for a
	 * particular column before it is written to the database.
	 * <p>
	 * It is more efficient to maintain references to each PamTableItem and to
	 * set the values directly in each PamTableItem in the setTableData
	 * function.
	 * 
	 * @param iCol
	 *            Database item index
	 * @param data
	 *            Data object (can be null, otherwise must be correct type for
	 *            the column)
	 * @see PamTableDefinition
	 * @see PamTableItem
	 */
	public void setColumnData(int iCol, Object data) {
		getTableDefinition().getTableItem(iCol).setValue(data);
	}

	/**
	 * Each SQLLogging class must provide a valid Pamguard database definition
	 * object
	 * 
	 * @return a Pamguard database table definition object
	 * @see PamTableDefinition
	 */
	public final PamTableDefinition getTableDefinition() {
		return pamTableDefinition;
	}


	/**
	 * 
	 * @param pamTableDefinition PamTableDefinition to set
	 */
	public void setTableDefinition(PamTableDefinition pamTableDefinition) {
		this.pamTableDefinition = pamTableDefinition;
		if (baseTableDefinition == null && pamTableDefinition != null) {
			baseTableDefinition = pamTableDefinition.clone();
		}
		if (loggingAddOns != null) {
			for (int i = 0; i < loggingAddOns.size(); i++) {
				loggingAddOns.get(i).addTableItems(pamTableDefinition);
			}
		}
		/**
		 * Need to clear any allocated cursors at this point, because it might have
		 * held references to memory locations within the previous table definitions
		 * table items. 
		 */
		resetCursors();
	}

	/**
	 * reset (delete) existing cursors. 
	 */
	private void resetCursors() {
		if (loggingCursorFinder != null) {
			loggingCursorFinder.deleteCursor();
		}
		if (viewerCursorFinder != null) {
			viewerCursorFinder.deleteCursor();
		}
	}

	/**
	 * Callback function when new data are created that allows the user to set
	 * the data for each column. Columns that have data which can be filled
	 * automatically (counters, primary keys and columns cross referenced to data
	 * in other tables) are filled automatically in fillTableData()
	 * 
	 * @param pamDataUnit
	 */
	abstract public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit);

	/**
	 * Automatically fills table data columns that can be done automatically
	 * (counters, primary keys and columns cross referenced to data in other
	 * tables). The abstract function setTableData is then called to fill in the
	 * other columns with detector specific data.
	 * 
	 * @param pamDataUnit
	 * @param superDetection 
	 */
	protected void fillTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit, PamDataUnit superDetection) {
		
		EmptyTableDefinition emptyTableDef = getTableDefinition();

		PamTableItem tableItem;

		emptyTableDef.getIndexItem().setValue(pamDataUnit.getDatabaseIndex());
		
		if (emptyTableDef instanceof PamTableDefinition) {
			PamTableDefinition tableDef = (PamTableDefinition) emptyTableDef;
			/*
			 * All tables have a timestamp near the front of the table. And all data
			 * units have a time in milliseconds, so always fill this in !
			 */
			tableDef.getTimeStampItem().setValue(
					sqlTypes.getTimeStamp(pamDataUnit.getTimeMilliseconds()));

			tableDef.getTimeStampMillis().setValue((int) (pamDataUnit.getTimeMilliseconds()%1000));

			tableDef.getLocalTimeItem().setValue(sqlTypes.getLocalTimeStamp(pamDataUnit.getTimeMilliseconds()));

			tableDef.getPCTimeItem().setValue(sqlTypes.getTimeStamp(System.currentTimeMillis()));

			tableDef.getUidItem().setValue(pamDataUnit.getUID());

			tableDef.getChannelBitmap().setValue(pamDataUnit.getChannelBitmap());

			tableDef.getSequenceBitmap().setValue(pamDataUnit.getSequenceBitmapObject());

			if (tableDef.getUpdateReference() != null) {
				tableDef.getUpdateReference().setValue(pamDataUnit.getDatabaseIndex());
			}
		}

		for (int i = 0; i < emptyTableDef.getTableItemCount(); i++) {

			tableItem = emptyTableDef.getTableItem(i);
			// if (tableItem.isCounter()) {
			// tableItem.setValue(1);
			// }
			// else
			if (tableItem.getCrossReferenceItem() != null) {
				tableItem.setValue(tableItem.getCrossReferenceItem().getValue());
			}
		}

		if (emptyTableDef instanceof PamSubtableDefinition) {
			PamSubtableDefinition subTableDef = (PamSubtableDefinition) emptyTableDef;
			fillSubTableData(subTableDef, pamDataUnit, superDetection);
		}

		setTableData(sqlTypes, pamDataUnit);

		if (loggingAddOns != null) {
			for (int i = 0; i < loggingAddOns.size(); i++) {
				loggingAddOns.get(i).saveData(sqlTypes, getTableDefinition(), pamDataUnit);
			}
		}
	}

	/**
	 * Called when a new PamDataUnit is added to a PamDataBlock to write those
	 * data to the database. Functionality moved down to PamCursor so that 
	 * exact writing method can become database specific if necessary. 
	 * 
	 * @param con
	 *            Database Connection
	 * @param dataUnit
	 *            Pamguard Data unit.
	 * @return true if written and new index of dataUnit retrieved OK
	 * @see PamDataUnit
	 */
	public synchronized boolean logData(PamConnection con, PamDataUnit dataUnit) {
		return logData(con, dataUnit, null);
	}
	/**
	 * Called when a new PamDataUnit is added to a PamDataBlock to write those
	 * data to the database. Functionality moved down to PamCursor so that 
	 * exact writing method can become database specific if necessary. 
	 * 
	 * @param con
	 *            Database Connection
	 * @param dataUnit
	 *            Pamguard Data unit.
	 * @param superDetection reference to a super detection so additional cross referencing can be filled
	 * @return true if written and new index of dataUnit retrieved OK
	 * @see PamDataUnit
	 */
	public synchronized boolean logData(PamConnection con, PamDataUnit dataUnit, PamDataUnit superDetection) {

		if (con == null) {
			showWarning("LogData: No database connection");
			return false;
		}
		//		if (dataUnit instanceof QASoundDataUnit && superDetection == null) {
		//			System.out.printf("Why? ");
		////			return false;
		//		}
		//		System.out.printf("Log data unit %s with super det %s\n", dataUnit, superDetection);
		fillTableData(con.getSqlTypes(), dataUnit, superDetection);

		PamCursor pamCursor = loggingCursorFinder.getCursor(con, pamTableDefinition);
		if (pamCursor == null) {// null for oodb
			showWarning("Database cursor error");
			return false;
		}

		int newIndex = pamCursor.immediateInsert(con);
		dataUnit.setDatabaseIndex(newIndex);
//
//		// if this logger references a subtable, call it now.  Note that all subtables must
//		// use table definitions that extend PamSubtableDefinition
//		if (subLogging != null) {
//			subLogging.logSubtableData(con, dataUnit);
//		}
		
		if (newIndex > 0) { // logging was OK
			dataUnit.clearUpdateCount();
			clearWarning();
			return true;
		}
		else {
			showWarning("Error logging data " + dataUnit.toString());
			return false;
		}

	}

	
	/**
	 * Called when an old PamDataUnit is updated. The record is either 
	 * updated or a new record is written, but cross referenced to the old
	 * unit for bookkeeping purposes based on the updatePolicy flag. 
	 * 
	 * @param con
	 *            Database Connection
	 * @param dataUnit
	 *            Pamguard Data unit.
	 * @return the number of rows written to the database.
	 * @see PamDataUnit
	 */
	public synchronized boolean reLogData(PamConnection con, PamDataUnit dataUnit) {
		return reLogData(con, dataUnit, null);
	}
	/**
	 * Called when an old PamDataUnit is updated. The record is either 
	 * updated or a new record is written, but cross referenced to the old
	 * unit for bookkeeping purposes based on the updatePolicy flag. 
	 * 
	 * @param con
	 *            Database Connection
	 * @param dataUnit
	 *            Pamguard Data unit.
	 * @param superDetection reference to a super detection so additional cross referencing can be filled
	 * @return the number of rows written to the database.
	 * @see PamDataUnit
	 */
	public synchronized boolean reLogData(PamConnection con, PamDataUnit dataUnit, PamDataUnit superDetection) {

		//		System.out.printf("ReLog data unit %s with super det %s\n", dataUnit, superDetection);

		if (con == null) {
			showWarning("reLogData: No database connection");
			return false;
		}
		if (dataUnit.getDatabaseIndex() <= 0) {
			/** DG 20190828 I don't think the matters since there is no real need to rewrite a 
			 * sub detection. Saves time not to rewrite every click every time we scroll and 
			 * can't really use the db index since a single click might be in multiple different
			 * types of super detection, so can't really have a unique db index. 
			 * Probably need to store a stored db index with subdetection info in main superdetections subdetection list. 
			 */
//			System.out.println("Cannot update data unit with invalid database index in " +
//					getTableDefinition().getTableName());
			return false;
		}
		/* 
		 * If the update policy is to overwrite, then need to update the old database
		 * record. Otherwise, need to update the existing record.
		 */
		if (getUpdatePolicy() == UPDATE_POLICY_WRITENEW) {
			return logData(con, dataUnit);
		}

		fillTableData(con.getSqlTypes(), dataUnit, superDetection);

		PamCursor pamCursor = loggingCursorFinder.getCursor(con, pamTableDefinition);

		boolean logOK = pamCursor.immediateUpdate(con);

//		// if this logger references a subtable, update it also
//		if (subLogging != null) {
//			updateSubtable(con, dataUnit);
//		}

		if (logOK) {
			dataUnit.clearUpdateCount();
			clearWarning();
		}
		else {
			showWarning("Error logging data");
		}
		
		return logOK;
	}





	private PamConnection statementCon;
	private ResultSet resultSet;

	//	private SQLTypes sqlTypes;

	private ResultSet getResultSet(PamConnection con) {

		if (statementCon != con) {
			if (dbStatement != null) {
				try {
					dbStatement.close();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
				dbStatement = null;
			}
			statementCon = con;
		}
		if (dbStatement == null) {
			try {
				//				dbStatement = con.prepareStatement(getTableDefinition().getSQLSelectString(),
				//				ResultSet.TYPE_SCROLL_SENSITIVE,
				//				ResultSet.CONCUR_UPDATABLE);
				//				dbStatement.setFetchDirection(ResultSet.FETCH_UNKNOWN);
				dbStatement = con.getConnection().createStatement(
						ResultSet.TYPE_SCROLL_SENSITIVE,
						ResultSet.CONCUR_UPDATABLE);
				//				dbStatement = insertStatement;
				//				dbStatement.setFetchDirection(ResultSet.FETCH_UNKNOWN);
				resultSet = null;
			} catch (SQLException ex) {
				ex.printStackTrace();
				return null;
			}
		}
		// now put some sql into the statement
		//		if (resultSet == null) {
		EmptyTableDefinition tableDef = getTableDefinition();
		String sqlString = tableDef.getSQLSelectString(con.getSqlTypes());
		// sqlString = "select \"comment\" from userinput";
		try {
			resultSet = dbStatement.executeQuery(sqlString);
			//			resultSet = dbStatement.getResultSet();
		} catch (SQLException ex) {
			ex.printStackTrace();
			return null;
		}
		//		}
		//		else {
		//		try {
		//		dbStatement.getMoreResults();
		//		} catch (SQLException ex) {
		//		ex.printStackTrace();
		//		return null;
		//		}
		//		}
		return resultSet;
	}
	
	/**
	 * Find the data point which is closest in time to that given, or null 
	 * returning whatever type of data unit this deals with. 
	 * @param timeMillis
	 * @return
	 */
	public PamDataUnit findClosestDataPoint(PamConnection con, long timeMillis) {

		PamCursor pamCursor = loggingCursorFinder.getCursor(con, pamTableDefinition);

		// can't really do any math with the string based dates, so will have to query from 
		// a few s before the time we want. 
		PamDataUnit[] beforeNafter = new PamDataUnit[2];
		
		SQLTypes sqlTypes = con.getSqlTypes();
		
		for (int i = 0; i < 2; i++) {
			String clause;
		
			if (i == 0) {
				clause = String.format("WHERE UTC <= %s ORDER BY UTC DESC", sqlTypes.formatDBDateTimeQueryString(timeMillis));
			}
			else {
				clause = String.format("WHERE UTC >= %s ORDER BY UTC ASC", sqlTypes.formatDBDateTimeQueryString(timeMillis));
			}

			ResultSet result = pamCursor.openReadOnlyCursor(con, clause);
			if (result==null) {
				return null;
			}

			PamTableItem tableItem;
			try {
				if (result.next()) {
					//				for (int i = 0; i < pamTableDefinition.getTableItemCount(); i++) {
					//					tableItem = pamTableDefinition.getTableItem(i);
					//					tableItem.setValue(result.getObject(i + 1));
					//				}
					//				return true;
					boolean ok = transferDataFromResult(con.getSqlTypes(), result);
					result.close();
					beforeNafter[i] = createDataUnit(sqlTypes, lastTime, lastLoadIndex);
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
				continue;
			}
		}
		// now pick the closest
		if (beforeNafter[0] == null) {
			return beforeNafter[1];
		}
		if (beforeNafter[1] == null) {
			return beforeNafter[0];
		}
		long t1 = timeMillis-beforeNafter[0].getTimeMilliseconds();
		long t2 = beforeNafter[1].getTimeMilliseconds()-timeMillis;
		if (t1 < t2) {
			return beforeNafter[0];
		}
		else {
			return beforeNafter[1];
		}
	}

	/**
	 * Called when a new database is connected to read the last values back in
	 * from the database.
	 * 
	 * @param con
	 *            Database connection handle.
	 * @return true is successful or false if no data available or an error
	 *         occurred
	 */
	public boolean readLastData(PamConnection con) {

		PamCursor pamCursor = loggingCursorFinder.getCursor(con, pamTableDefinition);

		ResultSet result = pamCursor.openReadOnlyCursor(con, "ORDER BY Id DESC");
		if (result==null) {
			return false;
		}

		PamTableItem tableItem;
		try {
			if (result.next()) {
//				for (int i = 0; i < pamTableDefinition.getTableItemCount(); i++) {
//					tableItem = pamTableDefinition.getTableItem(i);
//					tableItem.setValue(result.getObject(i + 1));
//				}
//				return true;
				boolean ok = transferDataFromResult(con.getSqlTypes(), result);
				result.close();
				return ok;
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
			return false;
		}
		return false;
	}
	
	/**
	 * Load the last data unit in the table. 
	 * @return last data unit in the table. 
	 */
	public PamDataUnit loadLastDataUnit() {
		PamConnection con = DBControlUnit.findConnection();
		if (con == null) {
			return null;
		}
		boolean readOk = readLastData(con);
		if (!readOk) {
			return null;
		}
		PamDataUnit du = createDataUnit(con.getSqlTypes(), lastTime, lastLoadIndex);
		if (du != null) {
			du.setChannelBitmap(lastChannelBitmap);
			du.setSequenceBitmap(lastSequenceBitmap);
		}
		return du;
	}


	public boolean loadViewerData(long dataStart, long dataEnd, ViewLoadObserver loadObserver) {
		//		generalDatabase.DBControl dbControl = 
		//			(DBControl) PamController.getInstance().findControlledUnit(DBControl.getDbUnitType());
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		if (dbControl == null ) {
			return false;
		}
		PamViewParameters vp = new PamViewParameters();
		vp.useAnalysisTime = false;
		vp.viewStartTime = dataStart;
		vp.viewEndTime = dataEnd;
		return loadViewData(dbControl.getConnection(), vp, loadObserver);
	}
	

	/**
	 * Get a count of the number of records in the given viewer range
	 * @param pamViewParameters
	 * @return null if an error, otherwise a count of records. 
	 */
	public Integer countTableItems(PAMSelectClause pamViewParameters) {
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		if (dbControl == null || pamTableDefinition == null) {
			return null;
		}
		PamConnection pamConn = dbControl.getConnection();
		if (pamConn == null) {
			return null;
		}

		SQLTypes sqlTypes = pamConn.getSqlTypes();
		//the clause contains 'WHERE' so it's possible to make a null one.
		String qStr;
		if (pamViewParameters == null) {
			qStr = String.format("SELECT COUNT(Id) FROM %s",
					pamTableDefinition.getTableName());
		}
		else {
			qStr = String.format("SELECT COUNT(%s.Id) FROM %s %s",
					pamTableDefinition.getTableName(),
					pamTableDefinition.getTableName(),
					pamViewParameters.getSelectClause(sqlTypes));
		}
		int count = 0;
		try {
			PreparedStatement stmt = pamConn.getConnection().prepareStatement(qStr);
			ResultSet result = stmt.executeQuery();
			if (result.next()) {
				count = result.getInt(1);
			}
			result.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return count;
	}

	public boolean loadViewData(PamViewParameters pamViewParameters, ViewLoadObserver loadObserver) {
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		if (dbControl == null ) {
			return false;
		}
		return loadViewData(dbControl.getConnection(), pamViewParameters, loadObserver);
	}
	/**
	 * Load viewer data for a single datablock. <p>
	 * this executes in a Swing Worker thread, so needs to send 
	 * notification objects to that thread, and not direct to the 
	 * Controller so that they can be published back in the AWT
	 * thread. 
	 * @param loadViewerData Swing Worker object
	 * @param con database connection 
	 * @param pamViewParameters viewer parameters. 
	 * @param loadObserver 
	 * @return
	 */
	public boolean loadViewData(PamConnection con, PamViewParameters pamViewParameters, ViewLoadObserver loadObserver) {
		
//		Debug.out.println("SQLLogging: Load viewer data from: " + this.pamDataBlock.getDataName() + " start time: "
//		+ PamCalendar.formatDateTime(pamViewParameters.viewStartTime) + " end time: " + 
//				PamCalendar.formatDateTime(pamViewParameters.viewEndTime) + " Unit count: " + pamDataBlock.getUnitsCount()); 

		if (getPamDataBlock().getNumRequiredBeforeLoadTime() > 0) {
			loadEarlyData(con, pamViewParameters);
		}

		long lastProgUpdate = System.currentTimeMillis();
		long now;
		
		if (loadObserver != null && pamViewParameters != null) {
			loadObserver.sayProgress(1, pamViewParameters.viewStartTime, pamViewParameters.viewEndTime, 
					pamViewParameters.viewStartTime, 0);
		}
		
		ResultSet resultSet = createViewResultSet(con, pamViewParameters);
		
		//		return loadViewData(con, resultSet, loadObserver);
		//	}
		//	
		//	public boolean loadViewData(PamConnection con, ResultSet resultSet, ViewLoadObserver loadObserver) {
		if (resultSet == null) return false;
		boolean first = true;
		PamDataUnit newDataUnit = null;
		int nAddOns = 0;
		int nUnits = 0;
		if (loggingAddOns != null) {
			nAddOns = loggingAddOns.size();
		}
		
//		try {
//			while (resultSet.next()) {
//				Debug.out.println("UID: " + resultSet.getObject(2) +"  "+ resultSet.getObject(3) + " " + resultSet.getObject(11)); 
//			}
//		}
//		catch (Exception e) {
//		}
	

		SQLTypes sqlTypes = con.getSqlTypes();
		//		String uidList = "(";
		//int nDets = 0;
		
		try {
			
			while (resultSet.next()) {
				
				transferDataFromResult(sqlTypes, resultSet);
				newDataUnit = createDataUnit(sqlTypes, lastTime, lastLoadIndex);
				if (newDataUnit != null) {
					/**
					 * Some modules may forget to set the database index - which is essential 
					 * so do it here
					 */
					newDataUnit.setDatabaseIndex(lastLoadIndex);
					if (lastLoadUID != null) {
						newDataUnit.setUID(lastLoadUID);
					}
					newDataUnit.setChannelBitmap(lastChannelBitmap);
					newDataUnit.setSequenceBitmap(lastSequenceBitmap);

					/**
					 * check and load data from addons. 
					 */
					for (int i = 0; i < nAddOns; i++) {
						loggingAddOns.get(i).loadData(sqlTypes, getTableDefinition(), newDataUnit);
					}

					/**
					 * Some older sql loggings will add the data unit, but newer implementations
					 * may chose not to in order to allow the addons to be loaded. So check to see
					 * if the newDataUnit has been put into a datablock and if it hasn't, then 
					 * add it now. 
					 */
					if (newDataUnit.getParentDataBlock() == null) {
						this.pamDataBlock.addPamData(newDataUnit);
					}

				}
				if ((now = System.currentTimeMillis()) > lastProgUpdate + 500) {
					lastProgUpdate = now;
					if (loadObserver != null) {
						if (pamViewParameters != null && newDataUnit != null) {
							loadObserver.sayProgress(1, pamViewParameters.viewStartTime, pamViewParameters.viewEndTime, 
									newDataUnit.getTimeMilliseconds(), nUnits);
						}
						if (loadObserver.cancelLoad()) {
							break;
						}
					}
				}
//				if (nUnits%1000==0) {
//					Debug.out.println("SQLLogging: Number of detections loaded: " + nUnits); 
//				}
				nUnits++;
			}
//			Debug.out.println("SQLLogging: Number of detections loaded: " + nUnits); 
			resultSet.close();
		}
		catch (SQLException ex) {
			System.err.printf("Error in SQLLogging.loadViewData(...)");
			ex.printStackTrace();
		}



		//		/**
		//		 * If early data were loaded, they will be in the wrong order, so will unfortunately have to sort the 
		//		 * data block !
		//		 */
		//		if (getPamDataBlock().getNumRequiredBeforeLoadTime() > 0) {
		//			
		//		}
		return true;
	}

	/**
	 * Load all data from a certain time. 
	 * @param startTimeMillis start time in milliseconds
	 * @return true if load successful (does not mean any data were loaded)
	 */
	public boolean loadDataFrom(long startTimeMillis) {
		PamConnection con = DBControlUnit.findConnection();
		if (con == null) {
			return false;
		}
		String clause = String.format("WHERE UTC >= %s", con.getSqlTypes().formatDateTimeMatchString(startTimeMillis));
		FixedClause pvp = new FixedClause(clause);
		return loadEarlyData(con, pvp);
	}

	/**
	 * Load early database data. 
	 * @param pamViewParameters query on what to load. 
	 * @return true if load successful (does not mean any data were loaded)
	 */
	public boolean loadEarlyData(PamViewParameters pamViewParameters) {
		PamConnection con = DBControlUnit.findConnection();
		if (con == null) {
			return false;
		}
		return loadEarlyData(con, pamViewParameters);
	}
	
	/**
	 * Load early database data. 
	 * @param con Database connection
	 * @param pamViewParameters query on what to load. 
	 * @return true if load successful (does not mean any data were loaded)
	 */
	public boolean loadEarlyData(PamConnection con, PamViewParameters pamViewParameters) {
		// temp store, 
		if (con == null) {
			return false;
		}
		ResultSet resultSet = createEarlyResultSet(con, pamViewParameters);
		if (resultSet == null) return false;
		boolean first = true;
		PamDataUnit newDataUnit = null;
		int nAddOns = 0;
		int nUnits = 0;
		if (loggingAddOns != null) {
			nAddOns = loggingAddOns.size();
		}
		SQLTypes sqlTypes = con.getSqlTypes();
		try {
			while (resultSet.next()) {
				transferDataFromResult(sqlTypes, resultSet);
				newDataUnit = createDataUnit(sqlTypes, lastTime, lastLoadIndex);
				if (newDataUnit != null) {
					newDataUnit.setDatabaseIndex(lastLoadIndex);
					if (lastLoadUID != null) {
						newDataUnit.setUID(lastLoadUID);
					}
					for (int i = 0; i < nAddOns; i++) {
						loggingAddOns.get(i).loadData(sqlTypes, getTableDefinition(), newDataUnit);
					}
					if (++nUnits >= getPamDataBlock().getNumRequiredBeforeLoadTime()) {
						break;
					}
					getPamDataBlock().addOldPamData(newDataUnit);
				}
			}
			resultSet.close();
		}
		catch (SQLException ex) {
			ex.printStackTrace();
			return false;
		}
		/*
		 * Now order the datablock, since the units will be in the wrong order. 
		 */
		getPamDataBlock().sortData();
		return true;

	}

	/**
	 * Get a standard select clause for loading viewer data, this 
	 * is basically that UTC is between two times. 
	 * @param sqlTypes SQL types - database specific functions
	 * @param pvp load parameters. 
	 * @return Clause, in the form WHERE UTC BETWEEN A and B ORDER BY UTC
	 */
	public String getViewerLoadClause(SQLTypes sqlTypes, PamViewParameters pvp) {
		if (getTableDefinition() == null) {
			System.out.println("No table");
		}
		String clause = pvp.getSelectClause(sqlTypes);
		if (clause == null) {
			clause = " ORDER BY UTC, UTCMilliseconds";
		}
		else if (!clause.toUpperCase().contains("ORDER BY")) {
			clause +=  " ORDER BY UTC, UTCMilliseconds";
		}
		return clause;
	}

	/**
	 * Get a clause for loading viewer data where an overlap is required between an 
	 * extended event and the data load period. 
	 * an extended "event" and 
	 * @param sqlTypes SQL types - database specific functions
	 * @param pvp load parameters. 
	 * @param endTimeName Name of the field representing the end of the event. 
	 * @return Clause in the format WHERE NOT (UTC&gtloadEnd OR EventEnd&ltLoadStart) ORDER BY UTC
	 */
	public String getViewerOverlapClause(SQLTypes sqlTypes, PamViewParameters pvp, String endTimeName) {
		String e = sqlTypes.formatDBDateTimeQueryString(pvp.getRoundedViewEndTime());
		String s = sqlTypes.formatDBDateTimeQueryString(pvp.getRoundedViewStartTime());
		String clause = String.format("WHERE NOT(%s > %s OR %s < %s) ORDER BY UTC" , 
				"UTC", e, endTimeName, s);
		//		System.out.println(clause);
		return clause;
	}

	/**
	 * Get a clause for loading viewer data where UTC time is less than the passed end time.  This method can also limit
	 * the results to only rows with a null in a specified column.
	 * 
	 * @param sqlTypes SQL types - database specific functions
	 * @param pvp load parameters
	 * @param onlyNulls whether to only include rows with nulls in a specific column (true) or all rows (false)
	 * @param colNameToTest which column to check for nulls - ignored if onlyNulls is false
	 * @return
	 */
	public String getViewerLessThanClause(SQLTypes sqlTypes, PamViewParameters pvp, boolean onlyNulls, String colNameToTest ) {
		String clause = String.format(" WHERE UTC < %s ", sqlTypes.formatDBDateTimeQueryString(pvp.getRoundedViewEndTime()));
		if (onlyNulls && colNameToTest != null) {
			clause += String.format(" AND %s IS NULL ", colNameToTest);
		}
		clause += " ORDER BY UTC, UTCMilliseconds";
		//		System.out.println(clause);
		return clause;
	}

	/**
	 * Get a load clause which selects everything, still ordered by UTC. 
	 * @param sqlTypes SQL types - database specific functions (not used)
	 * @param pvp load parameters. (not used)
	 * @return "ORDER BY UTC";
	 */
	public String getViewerEverythingClause(SQLTypes sqlTypes, PamViewParameters pvp) {
		return "ORDER BY UTC";
	}

	/**
	 * clause for loading data just prior to the normal viewer load time.
	 * @param pvp load view parameters. 
	 * @return a SQL clause string. 
	 */
	public String getEarlyLoadClause(SQLTypes sqlTypes, PamViewParameters pvp) {
		PamViewParameters newPVP = pvp.clone();
		newPVP.viewEndTime = newPVP.viewStartTime;
		newPVP.viewStartTime = 0;
		return newPVP.getSelectClause(sqlTypes) + " ORDER BY UTC DESC, UTCMilliseconds DESC";
	}


	public void deleteData(long dataStart, long dataEnd) {
		PamViewParameters pvp = new PamViewParameters();
		pvp.viewStartTime = dataStart;
		pvp.viewEndTime = dataEnd;
		pvp.useAnalysisTime = false;
		deleteData(pvp);
	}
	
	public boolean deleteData(PAMSelectClause pvp) {
		PamConnection con = DBControlUnit.findConnection();
		if (con == null) {
			return false;
		}
		SQLTypes sqlTypes = con.getSqlTypes();

		// will have to get list of data units to be deleted, and then delete any references to them
		// in the subtable
//		if (subLogging!=null) {
//			String sqlStr = String.format("SELECT Id FROM %s %s", 
//					pamTableDefinition.getTableName(), 
//					pvp.getSelectClause(con.getSqlTypes()));
//			System.out.println(sqlStr);
//			try {
//				Statement stmt = con.getConnection().createStatement();
//				ResultSet result = stmt.executeQuery(sqlStr);
//				while (result.next()) {
//					Object idObj = result.getObject(1);
//					Long id = sqlTypes.getLongValue(idObj);
//					deleteSubtableItems(con, id);
//				}
//				stmt.close();
//			} catch (SQLException e) {
//				System.out.println("Error retrieving parent IDs before deletion");
//				e.printStackTrace();
//			}
//		}

		// now delete the data
		String sqlStr = String.format("DELETE FROM %s %s", 
				pamTableDefinition.getTableName(), 
				pvp.getSelectClause(con.getSqlTypes()));
//		System.out.println(sqlStr);
		try {
			Statement stmt = con.getConnection().createStatement();
			stmt.execute(sqlStr);
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Turn the data, which have been transferred back into the PamTableItems back 
	 * into a usable data.
	 * Don't add to datablock. This will happen in calling function after additional add-ons and 
	 * annotations have been added to the data unit. .
	 * @return true if a data unit was successfully created
	 */
	@SuppressWarnings("rawtypes")
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {
		return null;
	}



	/**
	 * Get a results set for the viewer. In principle, the view parameters can contain information 
	 * about the analysis time as well as the actual data view time, but this is never used
	 * @param con connection
	 * @param pamViewParameters view parameters. 
	 * @return a result set 
	 */
	protected ResultSet createViewResultSet(PamConnection con, PamViewParameters pamViewParameters) {
		if (con == null) {
			return null;
		}
		String viewerClause = getViewerLoadClause(con.getSqlTypes(), pamViewParameters);
		return createViewResultSet(con, viewerClause);
	}



	/**
	 * Create a bespoke result set. 
	 * @param con
	 * @param selectClause
	 * @return
	 */
	protected ResultSet createViewResultSet(PamConnection con, String selectClause) {
		PamCursor pamCursor = viewerCursorFinder.getCursor(con, pamTableDefinition);
		ResultSet resultSet = pamCursor.openReadOnlyCursor(con, selectClause);
		return resultSet;
	}
	//	private ResultSet createSubTableResultSet(con, parentLogging, pamViewParameters)
	/**
	 * Get a result set for data preceding the main view time. Uses similar SQL, but is all 
	 * just before the main time and reverse ordered. 
	 * @param con connection
	 * @param pamViewParameters view parameters. 
	 * @return a result set 
	 */
	protected ResultSet createEarlyResultSet(PamConnection con, PamViewParameters pamViewParameters) {
		PamCursor pamCursor = viewerCursorFinder.getCursor(con, pamTableDefinition);
		String viewerClause = getEarlyLoadClause(con.getSqlTypes(), pamViewParameters);
		ResultSet resultSet = pamCursor.openReadOnlyCursor(con, viewerClause);

		return resultSet;
	}

	public boolean transferDataFromResult(SQLTypes sqlTypes, ResultSet resultSet) {

		EmptyTableDefinition tableDef = getTableDefinition();
		PamTableItem tableItem;
		try {
			for (int i = 0; i < tableDef.getTableItemCount(); i++) {
				tableItem = tableDef.getTableItem(i);
				Object obj = trimIncoming(resultSet.getObject(i + 1));
				tableItem.setValue(obj);
			}
			//			Timestamp ts = (Timestamp) getTableDefinition().getTimeStampItem().getValue();
			//			Timestamp ts = getTableDefinition().getTimeStampItem().getTimestampValue();
			//			lastTime = sqlTypes.millisFromTimeStamp(ts);
			lastLoadIndex = getTableDefinition().getIndexItem().getIntegerValue();
			if (tableDef instanceof PamTableDefinition) {
				PamTableDefinition pamTableDef = (PamTableDefinition) tableDef;
				lastTime = SQLTypes.millisFromTimeStamp(pamTableDef.getTimeStampItem().getValue());
				if (lastTime%1000 == 0) {
					// some databases may have stored the milliseconds, in which 
					// case this next bit is redundant. 
					lastTime += pamTableDef.getTimeStampMillis().getIntegerValue();
				}

				lastLoadUID = pamTableDef.getUidItem().getLongObject();
				lastChannelBitmap = pamTableDef.getChannelBitmap().getIntegerValue();
				lastSequenceBitmap = pamTableDef.getSequenceBitmap().getIntegerObject();
			}
			return true;

		} catch (SQLException ex) {
			ex.printStackTrace();
			return false;
		}
	}

	/**
	 * Trim all String type objects. Some will have ended up 
	 * padded with trailing blanks which stops them matching
	 * existing internal strings. 
	 * @param object Object read from database
	 * @return trimmed if it's a string, otherwise leave it alone. 
	 */
	private Object trimIncoming(Object object) {
		if (object == null) {
			return null;
		}
		if (String.class == object.getClass()) {
			return ((String) object).trim();
		}
		else {
			return object;
		}
	}

	public PamDataBlock getPamDataBlock() {
		return pamDataBlock;
	}

	/**
	 * Written to prepare the AIS module for general data emulation - will try
	 * to put it in this higher level class, but will move to 
	 * AISLogger if there are any problems with it. 
	 * @param times time limits for the emulation. 
	 * @return true if statement prepared OK/ 
	 */
	public boolean prepareEmulation(long times[]) {

		PamConnection con = DBControlUnit.findConnection();
		if (con == null ) {
			return false;
		}
		currentViewParameters = new PamViewParameters();
		currentViewParameters.viewStartTime = times[0];
		currentViewParameters.viewEndTime = times[1];
		currentViewParameters.useAnalysisTime = false;
		mixedModeResult = createViewResultSet(con, currentViewParameters);
		return (mixedModeResult != null);
	}

	public boolean readNextEmulation(SQLTypes sqlTypes) {
		if (mixedModeResult == null) {
			return false;
		}
		boolean ans;
		try {
			ans = mixedModeResult.next();
			if (!ans) {
				return false;
			}
			return transferDataFromResult(sqlTypes, mixedModeResult);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}

	private PreparedStatement mixedModeStatement;

	private ResultSet mixedModeResult;

	public boolean prepareForMixedMode(PamConnection con) {
		long history = pamDataBlock.getRequiredHistory();
		long now = PamCalendar.getTimeInMillis();
		long queryStart = now - history;
//		System.out.println("now and start = " + PamCalendar.formatDateTime(now) + "     " + PamCalendar.formatDateTime(queryStart));
		currentViewParameters = new PamViewParameters();
		currentViewParameters.viewStartTime = queryStart;
		currentViewParameters.viewEndTime = 0;
		currentViewParameters.useAnalysisTime = false;
		if (getPamDataBlock().getNumRequiredBeforeLoadTime() > 0) {
			loadEarlyData(con, currentViewParameters);
		}
		mixedModeResult = createViewResultSet(con, currentViewParameters);
		mixedDataWaiting = false;
		lastTime = 0;
		return readMixedModeData(con.getSqlTypes(), now);
	}
	private boolean mixedDataWaiting = false;

	/**
	 * always creates the data unit on the next pass through each loop so that they are only
	 * created AFTER the tiem cut off ahs passed. 
	 * @param timeTo
	 * @return true if data were read and used
	 */
	public synchronized boolean readMixedModeData(SQLTypes sqlTypes, long timeTo) {
		if (mixedModeResult == null) return false;
		if (lastTime > timeTo) {
			return false;
		}
		PamDataUnit newDataUnit;
		int nAddOns = 0;
		if (loggingAddOns != null) {
			nAddOns = loggingAddOns.size();
		}

		try {
			while (true) {
				if (mixedDataWaiting) {
					newDataUnit = createDataUnit(sqlTypes, lastTime, lastLoadIndex);
					if (newDataUnit != null) {
						newDataUnit.setDatabaseIndex(lastLoadIndex);
						newDataUnit.setUID(lastLoadUID);
						for (int i = 0; i < nAddOns; i++) {
							loggingAddOns.get(i).loadData(sqlTypes, getTableDefinition(), newDataUnit);
						}
					}
				}
				if (!mixedModeResult.next()) {
					mixedDataWaiting = false;
					break;
				}

				transferDataFromResult(sqlTypes, mixedModeResult);
				mixedDataWaiting = true;
				//				System.out.println("last time, time to, " + PamCalendar.formatDateTime(lastTime) + " : " +
				//				PamCalendar.formatDateTime(timeTo));
				if (lastTime > timeTo) {
					break;
				}
			}
		}
		catch (SQLException ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	public int getUpdatePolicy() {
		return pamTableDefinition.getUpdatePolicy();
	}

	public void setUpdatePolicy(int updatePolicy) {
		if (pamTableDefinition != null) {
			pamTableDefinition.setUpdatePolicy(updatePolicy);
		}
	}

	public boolean isCanView() {
		return canView;
	}

	public void setCanView(boolean canView) {
		this.canView = canView;
	}

	public boolean isLoadViewData() {
		return loadViewData;
	}

	public void setLoadViewData(boolean loadViewData) {
		this.loadViewData = loadViewData;
	}

	/**
	 * Re-check the database tables associated with this 
	 * Logger. This only needs to be called if the content of 
	 * tables has changed during PAMGUARD operations.  
	 * @return true if checks successful. 
	 * <p>Note that if no database is present, false will be returned
	 */
	public boolean reCheckTable() {
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		if (dbControl == null) {
			return false;
		}
		return dbControl.getDbProcess().checkTable(getTableDefinition());
	}

	/**
	 * Gives each module a chance to do additional checks and alterations
	 * to the database table at connection time. e.g. the GPS module may
	 * want to check that the table is not an old Logger table and if
	 * necessary alter the columns into the PAMGUARD format. 
	 * @param connection
	 * @return true if tests and alterations OK. 
	 */
	public boolean doExtraChecks(DBProcess dbProcess, PamConnection connection) {
		return true;
	}

	/**
	 * 
	 * @return the last time of data read in during mixed mode or
	 * NMEA emulations. 
	 */
	public long getLastTime() {
		return lastTime;
	}

	public int getLastLoadIndex() {
		return lastLoadIndex;
	}

	public Long getLastLoadUID() {
		return lastLoadUID;
	}

	/**
	 * Save offline data in viewer mode.
	 * <p>
	 * This is a pretty basic function which assumes pretty much a 
	 * 1:1 correspondence between data loaded into the viewer and 
	 * data to be saved. 
	 * <p>
	 * Three types of saving to do 
	 * <ol>
	 * <li>Pre existing 1:1 correspondence between data in memory and database
	 * <li>Viewer will have added or removed data  (e.g. map comments may easily have added data)
	 * <li>Weird stuff like in the click detector where the database is only holding information about
	 * a subset of clicks held in the binary data files 
	 * </ol>
	 * <p>Should be able to deal with first two here, but functions must override for 3. 
	 * @param dbControlUnit 
	 * @param connection 
	 * @return 
	 */
	public boolean saveOfflineData(DBControlUnit dbControlUnit, PamConnection connection) {
//		return standardOfflineSave(dbControlUnit, connection);
		return simpleOfflineSave(dbControlUnit, connection);
	}

	private boolean simpleOfflineSave(DBControlUnit dbControlUnit, PamConnection connection) {
		/**
		 * simpler than standardOfflineSave !
		 */
		synchronized (pamDataBlock.getSynchLock()) {
			ListIterator<PamDataUnit> it = pamDataBlock.getListIterator(0);
			while (it.hasNext()) {
				PamDataUnit dataUnit = it.next();
				if (dataUnit.getDatabaseIndex() <= 0) {
					logData(connection, dataUnit);
				}
				else if (dataUnit.getUpdateCount() > 0) {
					reLogData(connection, dataUnit);
				}
			}
		}
		return false;
	}

	private boolean standardOfflineSave(DBControlUnit dbControlUnit, PamConnection connection) {
		/**
		 * first work out what needs doing. 
		 */
		SaveRequirements sr = getPamDataBlock().getSaveRequirements(dbControlUnit);
		if (sr.getTotalChanges() == 0) {
			return true;
		}
		//		System.out.println("Update requirements for " + getPamDataBlock().getDataName());
		//		System.out.println("Updates   = " + sr.getNumUpdates());
		//		System.out.println("Additions = " + sr.getNumAdditions());
		//		System.out.println("Deletions = " + sr.getNumDeletions());

		int nUpdates = 0;
		int nAdditions = 0;
		String clause;
		//		DataUnitFinder<PamDataUnit> deleteFinder = new DeletedDataUnitFinder(getPamDataBlock());
		PamCursor pamCursor;
		PamDataUnit aUnit;
		int cursorId;
		if (sr.getNumUpdates()+sr.getNumAdditions() > 0) {
			try {
				DataUnitFinder<PamDataUnit> updateFinder = new DataUnitFinder<PamDataUnit>(getPamDataBlock(), 
						new DatabaseIndexUnitFinder());

				if (sr.getNumUpdates() > 0) {
					clause = getViewerUpdateClause(sr);
				}
				else {
					clause = "WHERE Id < 0"; // generate a null set. 
				}
				pamCursor = dbControlUnit.createPamCursor(getTableDefinition());
				if (!pamCursor.openScrollableCursor(connection, true, true, clause)) {
					System.out.println("Error opening update cursor " + pamDataBlock.getDataName());
				}
				if (sr.getNumUpdates() > 0) while(pamCursor.next()) {
					cursorId = pamCursor.getInt(1);
					aUnit = updateFinder.findDataUnit(cursorId);
					if (aUnit == null) {
						System.out.println(String.format("Unable to find data unit for index %d in %s",
								cursorId, pamDataBlock.getDataName()));
						continue;
					}
					if (aUnit.getUpdateCount() == 0) {
						continue;
					}
					updateCursorRow(connection, pamCursor, aUnit);
					nUpdates++;

					// if there is a subtable, update it also
//					if (subLogging != null) {
//						updateSubtable(connection, aUnit);
//					}
				}
				if (sr.getNumAdditions() > 0) {
					ListIterator<PamDataUnit> li = pamDataBlock.getListIterator(0);
					while (li.hasNext()) {
						aUnit = li.next();
						if (aUnit.getDatabaseIndex() > 0) {
							continue;
						}
						cursorId = insertCursorRow(connection, pamCursor, aUnit);
						if (cursorId < 0) {
							System.out.println("Unable to get cursor Id in " + pamDataBlock.getDataName());
						}
						else {
							nAdditions ++;
							aUnit.setDatabaseIndex(cursorId);

							// if there is a subtable, log any subdetections which are linked to this unit
//							if (subLogging != null) {
//								subLogging.logSubtableData(connection, aUnit);
//							}
						}
					}
				}
				pamCursor.updateDatabase();
				pamCursor.closeScrollableCursor();
			}
			catch (SQLException e) {
				System.out.println("Error updating database in " + pamDataBlock.getDataName());
				e.printStackTrace();
			}
		}
		if (sr.getNumUpdates() != nUpdates) {
			System.out.println(String.format("The number of updates (%d) does not match " +
					"the number of records which require updating (%d) in %s", 
					nUpdates, sr.getNumUpdates(), pamDataBlock.getDataName()));
		}
		if (sr.getNumAdditions() != nAdditions) {
			System.out.println(String.format("The number of inserts (%d) does not match " +
					"the number of records which require inserting (%d) in %s", 
					nUpdates, sr.getNumUpdates(), pamDataBlock.getDataName()));
		}
		
		if (sr.getNumDeletions() > 0) {
			if (deleteIndexedItems(connection, sr.getDeleteIndexes()) ) {
				pamDataBlock.clearDeletedList();
			}
		}

		return true;
	}


	/**
	 * Delete one or more rows from the database based on their indexes. 
	 * @param connection
	 * @param deleteIndexes list of Id's
	 * @return true if no exception
	 */
	public boolean deleteIndexedItems(PamConnection connection, int[] deleteIndexes) {
		String sqlString = String.format("DELETE FROM %s WHERE Id %s", 
				pamTableDefinition.getTableName(), createInClause(deleteIndexes));
		try {
			Statement s = connection.getConnection().createStatement();
			s.execute(sqlString);
			s.close();

//			// if we also have a subtable, remove any subdetections linked to the deleted units.
//			// Do this before we clear the list of deleted units from the data block
//			if (subLogging != null) {
//				List<PamDataUnit> delUnits = pamDataBlock.getRemovedItems();
//				for (int i=0; i<delUnits.size(); i++) {
//					PamDataUnit pdu = delUnits.get(i);
//					deleteSubtableData(connection, pdu);
//				}
//			}

		} catch (SQLException e) {
			System.out.println("Delete failed with " + sqlString);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Used in standard offline save to add data to a scrollable cursor and adds 
	 * the cursor row id to the data unit. 
	 * @param connection
	 * @param pamCursor
	 * @param aUnit
	 * @throws SQLException
	 * @return The 
	 */
	protected int insertCursorRow(PamConnection connection, PamCursor pamCursor, PamDataUnit aUnit) throws SQLException {
		pamCursor.moveToInsertRow();
		fillTableData(connection.getSqlTypes(), aUnit, null);
		pamCursor.moveDataToCursor(true);
		int cursorId = pamCursor.insertRow(true);
		aUnit.setDatabaseIndex(cursorId);
		return cursorId;
	}

	/**
	 * Used in standard offline save to update data in a scrollable cursor. 
	 * @param connection
	 * @param pamCursor
	 * @param aUnit
	 * @throws SQLException
	 */
	protected void updateCursorRow(PamConnection connection, PamCursor pamCursor, PamDataUnit aUnit) throws SQLException {
		fillTableData(connection.getSqlTypes(), aUnit, null);
		pamCursor.moveDataToCursor(true);
		pamCursor.updateRow();
		aUnit.clearUpdateCount();		
	}

	/**
	 * Get a select clause for viewer updates. this may be overridden for some
	 * data types depending on what' most optimal for date retrieval.
	 * <p> A couple of examples you may want to use are in 
	 * getTimesUpdateClause and getIdListUpdatClause
	 * <p>getIdListUpdatClause is currently the default. 
	 * @param sr requirements extracted from loaded data 
	 * @return clause string (including any sort)
	 */
	public String getViewerUpdateClause(SaveRequirements sr) {
		return getIdListUpdatClause(sr);
	}	

	/**
	 * Get a select clause for viewer updates. this may be overridden for some
	 * data types depending on what' most optimal for date retrieval.
	 * <p>for example, the default selection is based on time - which won't work 
	 * if the event times may have changed - ok for things which will be fixed in time.   
	 * @param sr requirements extracted from loaded data 
	 * @return clause string (including any sort)
	 */
	public String getTimesUpdateClause(SQLTypes sqlTypes, SaveRequirements sr) {
		PamViewParameters pvp = new PamViewParameters();
		pvp.setViewStartTime(sr.getFirstUpdateTime());
		pvp.setViewEndTime(sr.getLastUpdateTime());
		pvp.useAnalysisTime = false;
		return pvp.getSelectClause(sqlTypes);
	}

	public String getIdListUpdatClause(SaveRequirements sr) {
		return "WHERE Id " + createInClause(sr.getUpdatedIndexes());
	}

	/**
	 * Make an SQL clause in the from IN ( ... )
	 * @param idList
	 * @return string clause. 
	 */
	public String createInClause(int[] idList) {
		if (idList == null) {
			return null;
		}
		Arrays.sort(idList);
		String clause = "IN (";
		boolean first = true;
		for (int i = 0; i < idList.length; i++) {
			if (first) {
				first = false;
			}
			else {
				clause += ", ";
			}
			clause += idList[i];
		}
		clause += ")";
		return clause;
	}

	/**
	 * Add an SQL Logging addon - something which adds some standard columns
	 * to what it probably a non standard table, e.g. adding target motion 
	 * results to a click event detection. <p>
	 * Note that database checks are carried out immediately this gets added. 
	 * @param sqlLoggingAddon
	 */
	public void addAddOn(SQLLoggingAddon sqlLoggingAddon) {
		if (sqlLoggingAddon == null) {
			return;
		}
		if (loggingAddOns == null) {
			loggingAddOns = new ArrayList<SQLLoggingAddon>();
		}
		// see if this addon already esists ...
		SQLLoggingAddon existingAddon = findLoggingAddon(sqlLoggingAddon);
		if (existingAddon != null) {
			removeAddOn(existingAddon, true);
		}

		loggingAddOns.add(sqlLoggingAddon);
		if (pamTableDefinition != null) {
			sqlLoggingAddon.addTableItems(pamTableDefinition);
			if (PamController.getInstance().isInitializationComplete()) {
				// recheck this database table to make sure the columns get added
				DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
				if (dbControl != null) {
					dbControl.getDbProcess().checkTable(pamTableDefinition);
				}
			}
		}
	}

	/**
	 * Find an existing logging addon - this is anything with the same class, 
	 * not necessarily the same reference. 
	 * @param sqlLoggingAddon
	 * @return existing addon or null. 
	 */
	private SQLLoggingAddon findLoggingAddon(SQLLoggingAddon sqlLoggingAddon) {
		if (loggingAddOns == null) {
			return null;
		}
		for (SQLLoggingAddon exAddon:loggingAddOns) {
			if (exAddon.getClass() == sqlLoggingAddon.getClass() && exAddon.getName().equals(sqlLoggingAddon.getName())) {
				return exAddon;
			}
		}
		return null;
	}

	/**
	 * Remove an SQLLogging Addon. 
	 * @param sqlLoggingAddon
	 * @return true if the addon existed. 
	 */
	public boolean removeAddOn(SQLLoggingAddon sqlLoggingAddon) {
		return removeAddOn(sqlLoggingAddon,false);
	}

	/**
	 * Remove an SQLLogging Addon. 
	 * @param sqlLoggingAddon
	 * @return true if the addon existed. 
	 */
	public boolean removeAddOn(SQLLoggingAddon sqlLoggingAddon, boolean removeFields) {
		if (loggingAddOns == null) {
			return false;
		}
		boolean ans = loggingAddOns.remove(sqlLoggingAddon);
		if (removeFields) {
			//now remove all of the table items, based on name.
			/**
			 * Aaaah sqlLoggingAddon is so abstracted that it doesnt' contain 
			 * a list of table items. This is something to rectify in the future, 
			 * for now though, will have to go back to creating a sensible
			 * base table for the modules that have variable addons. 
			 */
			//			sqlLoggingAddon.removeTableItems(this.getTableDefinition());
		}


		return ans;
	}


	/**
	 * Clear all SQL Logging Addons
	 */
	public void clearAllAddOns() {
		if (loggingAddOns == null) {
			return;
		}
		loggingAddOns.clear();
	}

	/**
	 * Convert a Double to a Float, dealing with null
	 * @param val Double object
	 * @return Float object. 
	 */
	public Float double2Float(Double val) {
		if (val == null) {
			return null;
		}
		return val.floatValue();
	}

	/**
	 * Reset anything needing resetting in the binary data source. 
	 * This get's called just before PamStart(). 
	 */
	public void reset() {		
	}

	public boolean deleteData(PamDataUnit aDataUnit) {
		// remove a single entry from the database. 
		int index = aDataUnit.getDatabaseIndex();
		if (index <= 0) {
			return false;
		}
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		if (dbControl == null) {
			return false;
		}
		PamConnection connection = dbControl.getConnection();
		// pass this through the deleteIndexedItems since that is overridden 
		// to also delete sub detections where required. 
		int[] indexList = {index};
		return deleteIndexedItems(connection, indexList);
//		// simple SQL delete string to remove immediately from the database. 
//		String sqlString = String.format("DELETE FROM %s WHERE Id = %d", 
//				pamTableDefinition.getTableName(), index);
//		try {
//			Statement s = connection.getConnection().createStatement();
//			s.execute(sqlString);
//			s.close();
//		} catch (SQLException e) {
//			System.out.println("Delete failed with " + sqlString);
//			e.printStackTrace();
//			return false;
//		}
//
////		// if we also have a subtable, remove any subdetections linked to this unit
////		if (subLogging != null) {
////			return deleteSubtableData(connection, aDataUnit);
////		}
//		return true;
	}

	/**
	 * Get a string used to match binary data with the database records. 
	 * @param pamDataUnit Data unit to match
	 * @param sqlTypes type for specific formatting. 
	 * @return WHERE type clause (without the WHERE)
	 */
	public String getUIDMatchClause(PamDataUnit pamDataUnit, SQLTypes sqlTypes) {
		String qStr = "UTC=" + sqlTypes.formatDateTimeMatchString(pamDataUnit.getTimeMilliseconds());
		return qStr;
	}

	public Integer countTableItems() {
		return countTableItems(new PamViewParameters());
	}

	/**
	 * More and more data blocks are starting to use annotations, which require
	 * modifying the table definition, adding additional columns. This get's a bit
	 * awkward, when annotations are being added and removed. To help, we're going 
	 * to try storing a baseTableDefinition the very first time that the main 
	 * table definition is set, so that it can be got and modified by the 
	 * annotation handler shortly after the main table is created.  
	 * @return the baseTableDefinition
	 */
	public PamTableDefinition getBaseTableDefinition() {
		return baseTableDefinition;
	}

	/*
	 * ***************************************************************************************************************************************
	 * Functions for sub tables. tried to break out to a different class, but can't because some loggers need to be a subdet and a superdet. 
	 */
	/**
	 * If there are subdetections associated with the passed data unit, this method goes through and creates the 
	 * subtable.  Note that this should be called using the expression subtable.logSubtableData, because it uses the getTableDefinition()
	 * method to get the subtable table (so we need to actually be in the subtable when calling this, or else the incorrect table will be returned)
	 * @param con PamConnection to use
	 * @param superDetection the parent data unit that may or may not have subdetections to log in the table
	 * @return
	 */
	protected synchronized boolean logSubtableData(PamConnection con, SuperDetection superDetection) {

		// get the number of subdetections in this dataUnit
		int nChildren = superDetection.getSubDetectionsCount();

		// get the table def and accept we'll crash if it doesn't have the parentId and parentUID fields. 
		// fill in the cross reference fields ....
		PamSubtableDefinition subTableDef = (PamSubtableDefinition) getTableDefinition();
		int superDetIndex = superDetection.getDatabaseIndex();
		long superDetUID = superDetection.getUID();

		for (int i = 0; i < nChildren; i++) {

			SubdetectionInfo subDetectionInfo = superDetection.getSubdetectionInfo(i);
			PamDataUnit subDetection = subDetectionInfo.getSubDetection();
			if (subDetection == null) {
				// the data unit is not currently loaded so there is nothing to do
				continue;
			}

			// if the current child object already has a database index, it means it's already
			// in this table.  If so, just go on to the next child
			// but first check for another layer of tables that might need updating. 
			//			if (subDetection.getDatabaseIndex()>0){
			//				SQLLogging subsubLogging = this.subtable;
			//				if (subsubLogging != null) {
			//					subsubLogging.logSubtableData(con, subDetection);
			//				}
			////				continue;
			//			}

			//			fillSubTableData(subTableDef, subDetection, dataUnit);
			/*
			// fill in the standard table items
			subTableDef.getParentID().setValue(dataUnit.getDatabaseIndex());
			subTableDef.getParentUID().setValue(dataUnit.getUID());
			PamDataBlock subDetDataBlock = subDetection.getParentDataBlock();
			if (subDetDataBlock == null) {
				continue; // can't log this since it's not assigned to a datablock.
			}
			else {
				subTableDef.getLongName().setValue(subDetDataBlock.getLongDataName());
			}



			DataUnitFileInformation fileInfo = subDetection.getDataUnitFileInformation();
			if (fileInfo != null) {
				subTableDef.getBinaryfile().setValue(fileInfo.getShortFileName(PamSubtableDefinition.BINARY_FILE_NAME_LENGTH));
			} else {
				subTableDef.getBinaryfile().setValue(null);
			}
			 */

			// save the current subDetection index, because the logData method will overwrite it with the
			// index in the subtable
//			int origIndex = subDetection.getDatabaseIndex();
			/**
			 * Need to be a bit more clever than just checking to see if there is a non-zero database 
			 * index since the sub data unit may have been added to > 1 super detection, so 
			 * need to go into the sub detection info for the parent unit and see if there is a 
			 * database index there, rather than looking at the global one in the subDetection.
			 * There is also a problem that the subdetection may want all it's data in the same table. 
			 */
//			boolean sameTable = subDetection.getParentDataBlock().
			long origIndex = subDetectionInfo.getChildDatabaseIndex();
			if (origIndex > 0 || (isSameLogging(subDetection) && subDetection.getDatabaseIndex() > 0)) {
				/*
				 * If origIndex > 0 it means that this data unit has already been logged
				 */
				/**
				 * Why relog data - two possible reasons:
				 * 1. The sub detection data unit has been updated (including adding sub detections
				 * to the sub detection. 
				 * 2. When it was first logged, it's super detection wasn't already in 
				 * the database so it didn't have a valid dbIndex for the super detection. 
				 */
				if (needSubDetectionRelog(subDetectionInfo, superDetection)) {
					reLogData(con, subDetection, superDetection);
					//					System.out.printf("ReLog sub detection %s id %d for super det %s UID %d  \n", 
					//							subDetection, subDetection.getDatabaseIndex(), dataUnit, 
					//							(int)dataUnit.getUID());
				}
			}
			else {
				//				System.out.printf("Log sub detection %s for super det %s DBId %d UID %d\n", 
				//						subDetection, dataUnit, dataUnit.getDatabaseIndex(), dataUnit.getUID());
				// call logData to add in the standard column data (using values from the subdetection) and fill everything in 
				if (subDetection instanceof QASoundDataUnit) {

					logData(con, subDetection, superDetection);
				}
				else logData(con, subDetection, superDetection);
				subDetectionInfo.setChildDatabaseIndex(subDetection.getDatabaseIndex());
				subDetectionInfo.setParentID(superDetection.getDatabaseIndex());
//				subDetectionInfo.setParentUID(dataUnit.getUID());
			}

			//			// save both the dataUnit index and the subdetection index in the SubdetectionInfo object 
			//			dataUnit.getSubdetectionInfo(i).setParentID(dataUnit.getDatabaseIndex());
			//			dataUnit.getSubdetectionInfo(i).setDbIndex(subDetection.getDatabaseIndex());
			//
			//			// replace the suDetection index with the original one and return
			//			subDetection.setDatabaseIndex(origIndex);
		}
		return true;
	}
	
	/**
	 * Is a subdetection already using this table. This distinguishes between a sub detection that 
	 * is purely cross referenced and a sub detection which has it's reference info and other data all in the same
	 * table. 
	 * @param subDetection
	 * @return
	 */
	private boolean isSameLogging(PamDataUnit subDetection) {
		PamDataBlock dataBlock = subDetection.getParentDataBlock();
		if (dataBlock == null) {
			return false;
		}
		SQLLogging logging = dataBlock.getLogging();
		if (logging == null) {
			// this would happen when cross referencing to dat aheld in a binary store
			return false;
		}
		return (logging == this); // same logging as we're already in
	}

	private boolean needSubDetectionRelog(SubdetectionInfo subDetectionInfo, PamDataUnit dataUnit) {
		if (subDetectionInfo.getParentID() == 0 || subDetectionInfo.getParentUID() == 0) {
//			Debug.out.printf("Relog subdetection %s because no parent ID\n", subDetectionInfo.getSubDetection());
			return true;
		}
		PamDataUnit subDet = subDetectionInfo.getSubDetection();
		if (subDet == null) {
			return false;// sub detection is not loaded. 
		}
		if (subDet.getUpdateCount() > 0) {
			return true;
		}
		return false;
	}

//	@Override
//	protected void fillTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit, PamDataUnit superDetection) {
//		super.fillTableData(sqlTypes, pamDataUnit, superDetection);
//		fillSubTableData((PamSubtableDefinition) getTableDefinition(), pamDataUnit, superDetection);
//	}

	/**
	 * Loads the subtable data related to sub/superdetection linking, and stores it in a PamSubtableData ArrayList for 
	 * reattachment later.  Note that this DOES NOT load any additional columns of data added into overridden setTableData
	 * methods.
	 * 
	 * @param con database connection
	 * @param parentLogging super detection logging instance.
	 * @param idList list of ID's in the parent data that have been loaded. Note Id, NOT UID 
	 * @return list of all PamSubtableData items
	 */
	public ArrayList<PamSubtableData> loadSubtableData(PamConnection con, SQLLogging parentLogging, String idList, ViewLoadObserver loadObserver) {
		//		ResultSet subtableResults = createViewResultSet(con, pamViewParameters);
		/**
		 * Rewritten to use the UID's of the parent in an IN clause (see https://www.w3schools.com/sql/sql_in.asp)
		 * so that data from the sub detections will be read in over any time period. 
		 * Of course, this doesn't solve the problem of actually loading the sub detections into memory
		 * but can help when we start to be more clever about how data units are loaded. 
		 */
		ResultSet subtableResults = createSubTableResultSet(con, parentLogging, idList);
		return loadSubtableData(con, subtableResults, loadObserver);
	}

	/**
	 * Called within a subLogging from the superLogging
	 * Loads the subtable data related to sub/superdetection linking, and stores it in a PamSubtableData ArrayList for 
	 * reattachment later.  Note that this DOES NOT load any additional columns of data added into overridden setTableData
	 * methods.
	 * 
	 * @param con database connection
	 * @param parentLogging super detection logging instance.
	 * @param pvp view parameters (time based)
	 * @param loadObserver 
	 * @return list of all PamSubtableData items
	 */
	public ArrayList<PamSubtableData> loadSubtableData(PamConnection con, SQLLogging parentLogging, PamViewParameters pvp, ViewLoadObserver loadObserver) {
		ResultSet subtableResults = createSubTableResultSet(con, parentLogging, pvp);
		return loadSubtableData(con, subtableResults, loadObserver);
	}

	/**
	 * Get a sub table result set. Note that this is based on ID, not UID
	 * @param con connection
	 * @param parentLogging parent logging system
	 * @param parentIdList ParentID list. Note that this is ID, not UID, <br>i.e. the query is  WHERE ParentID IN ...
	 * @return child table result set
	 */
	private ResultSet createSubTableResultSet(PamConnection con, SQLLogging parentLogging,
			String parentIdList) {
		String clause = String.format(" WHERE ParentID IN %s ORDER BY UTC, UTCMilliseconds", parentIdList);
		return createViewResultSet(con, clause);
	}
	
	/**
	 * This one gets called within the sub table from the super table.
	 */
	private ResultSet createSubTableResultSet(PamConnection con, SQLLogging parentLogging,
			PamViewParameters pamViewParameters) {
		//		String thisTable = getTableDefinition().getTableName();
		//		String otherTable = parentLogging.getTableDefinition().getTableName();
		//		String clause = String.format(" INNER JOIN %s ON %s.ParentUID=%s.UID WHERE %s.UTC BETWEEN %s AND %s", 
		//				otherTable, 
		//				thisTable,
		//				otherTable,
		//				thisTable,
		//				con.getSqlTypes().formatDBDateTimeQueryString(pamViewParameters.getRoundedViewStartTime()), 
		//				con.getSqlTypes().formatDBDateTimeQueryString(pamViewParameters.getRoundedViewEndTime()));
		//		clause += String.format(" ORDER BY %s.UTC, %s.UTCMilliseconds", thisTable, thisTable);
		String clause = pamViewParameters.getSelectClause(con.getSqlTypes());
		return createViewResultSet(con, clause);
	}
	/**
	 * This one gets called within the sub table. 
	 * Load the sub table data from the results set. 
	 * @param con
	 * @param subtableResults
	 * @param loadObserver 
	 * @return
	 */
	private ArrayList<PamSubtableData> loadSubtableData(PamConnection con, ResultSet subtableResults, ViewLoadObserver loadObserver) {
		ArrayList<PamSubtableData> tableList = new ArrayList<PamSubtableData>();
		int n = 0;
		try {
			PamSubtableDefinition subtableTableDef = (PamSubtableDefinition) getTableDefinition();
			PamTableItem clickNoItem = subtableTableDef.findTableItem("ClickNo");
			while (subtableResults.next()) {
				PamTableItem tableItem;
//				transferDataFromResult(con.getSqlTypes(), subtableResults);
				for (int i = 0; i < subtableTableDef.getTableItemCount(); i++) {
					tableItem = subtableTableDef.getTableItem(i);
					tableItem.setValue(subtableResults.getObject(i + 1));
				}
				if (subtableTableDef.getUidItem().getValue() == null) {
					System.out.println(String.format("Null UID reading %s at Index %d - item cannot be loaded.  Table should be manually corrected.", subtableTableDef.getTableName(), 
							subtableTableDef.getIndexItem().getValue()));
					continue;
				}
				PamSubtableData subtableData = new PamSubtableData();
				long utc = SQLTypes.millisFromTimeStamp(subtableTableDef.getTimeStampItem().getValue());
				if (utc % 1000 == 0) {
					int millis = subtableTableDef.getTimeStampMillis().getIntegerValue();
					utc += millis;
				}
				subtableData.setChildUTC(utc);
				subtableData.setParentID(subtableTableDef.getParentID().getIntegerValue());
				subtableData.setParentUID(subtableTableDef.getParentUID().getLongValue());
				subtableData.setLongName(subtableTableDef.getLongName().getStringValue());
				subtableData.setBinaryFilename(subtableTableDef.getBinaryfile().getStringValue());
				subtableData.setDbIndex(subtableTableDef.getIndexItem().getIntegerValue());
				if (clickNoItem != null) {
					subtableData.setClickNumber(clickNoItem.getIntegerValue());
				}
				try {
					subtableData.setChildUID(subtableTableDef.getUidItem().getLongObject());
				}
				catch (Exception e) {
					e.printStackTrace();
					//				subtableData.setChildUID(subtableTableDef.getUidItem().getLongObject());
				}
				tableList.add(subtableData);
				if (++n%1000 == 0 && loadObserver != null) {
					loadObserver.sayProgress(LoadQueueProgressData.STATE_LINKINGSUBTABLE, 0, 0, utc, n);
				}
				
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return tableList;
	}
	
	/**
	 * Called from within the fillTableData if this is a subtable.   
	 * @param subTableDef
	 * @param subDetection
	 * @param superDetection
	 */
	private void fillSubTableData(PamSubtableDefinition subTableDef, PamDataUnit subDetection, PamDataUnit superDetection) {
		if (superDetection == null) {		
			/**
			 * There is a BIG problem with relogging data in a subtable if it's relogged with 
			 * a superDetection of null since it will lose the superDetection information. 
			 * This call should find any superDetection data unit that's from the 
			 * instance of the super logging class. 
			 */
			superDetection = findSuperDetection(subDetection);
		}
		/**
		 * The super detection may still be null at this point if the super det hasn't
		 * been created. That should be dealt with later when the superdet get logged
		 * and this record will be automatically updated. 
		 */
		if (superDetection != null) {
			subTableDef.getParentID().setValue(superDetection.getDatabaseIndex());
			subTableDef.getParentUID().setValue(superDetection.getUID());
		}
		else {
			subTableDef.getParentID().setValue(null);
			subTableDef.getParentUID().setValue(null);
		}
		PamDataBlock subDetDataBlock = subDetection.getParentDataBlock();
		if (subDetDataBlock != null) {
			subTableDef.getLongName().setValue(subDetDataBlock.getLongDataName());
		}
		else {
			subTableDef.getLongName().setValue(null);
		}
		DataUnitFileInformation fileInfo = subDetection.getDataUnitFileInformation();
		if (fileInfo != null) {
			subTableDef.getBinaryfile().setValue(fileInfo.getShortFileName(PamSubtableDefinition.BINARY_FILE_NAME_LENGTH));
		} else {
			subTableDef.getBinaryfile().setValue(null);
		}

	}
	/**
	 * Updates a subtable by adding new subdetections and removing deleted subdetections
	 * @param connection
	 * @param parentUnit
	 * @return
	 */
	protected boolean updateSubtable(PamConnection connection, SuperDetection parentUnit) {

		// now check if items have been deleted.
		ArrayList<SubdetectionInfo> subDets = parentUnit.getSubdetectionsRemoved();
		if (subDets != null) {
			for (SubdetectionInfo subDet :subDets) {
				deleteSubtableItem(connection, parentUnit.getUID(), subDet.getChildDatabaseIndex());
			}
			parentUnit.clearSubdetectionsRemoved();
		}
		// there are a number of reasons for the data unit's update flag to get
		// set, but the only two we care about are if a subdetection was added or
		// removed.  The logSubtableData method will only add new subdetections
		// to the subtable, so call it first
		return logSubtableData(connection, parentUnit);
	}
	
//	/**
//	 * Deletes ALL data from a subtable that is linked to the parent data unit passed
//	 * to this method.
//	 * 
//	 * @param con PamConnection to use
//	 * @param parentUnit the PamDataUnit that we are trying to find linking subdetections for
//	 * @return
//	 */
//	protected boolean deleteAllSubtableData(PamConnection con, SuperDetection parentUnit) {
//		String sqlString = String.format("DELETE FROM %s WHERE parentID = %d", 
//				getTableDefinition().getTableName(), parentUnit.getDatabaseIndex());
//		try {
//			Statement s = con.getConnection().createStatement();
//			s.execute(sqlString);
//			s.close();
//		} catch (SQLException e) {
//			System.out.println("Delete failed with " + sqlString);
//			e.printStackTrace();
//			return false;
//		}
//		return true;
//	}

	/**
	 * Deletes a single row from the subtable, based on the database index passed
	 * to this method.
	 * 
	 * @param con PamConnection to use
	 * @param dbIndex the database index we are deleting
	 * @param parentUID UID of the super detection
	 * @param subDbIndex UID of the sub detection. Use instead of UID, since more reliable. 
	 * @return true if no SQL error
	 */
	protected boolean deleteSubtableItem(PamConnection con, long parentUID, long subDbIndex) {
		String sqlString = String.format("DELETE FROM %s WHERE ParentUID = %d AND Id = %d", 
				getTableDefinition().getTableName(), parentUID, subDbIndex);
		//		String sqlString = String.format("DELETE FROM %s WHERE UID = %d", 
		//				subtable.getTableDefinition().getTableName(), subUID);
		try {
			Statement s = con.getConnection().createStatement();
			s.execute(sqlString);
			s.close();
		} catch (SQLException e) {
			showWarning(sqlString + " failed: " + e.getMessage());
//			e.printStackTrace();
			return false;
		}
		clearWarning();
		return true;
	}

	/**
	 * Deletes all sub table items associated with this super detection. 
	 * 
	 * @param con PamConnection to use
	 * @param dbIndex the database index we are deleting
	 * @param parentUID UID of the super detection
	 * @return
	 */
	protected boolean deleteSubtableItems(PamConnection con, long parentID) {
		String sqlString = String.format("DELETE FROM %s WHERE ParentID = %d", 
				getTableDefinition().getTableName(), parentID);
		try {
			Statement s = con.getConnection().createStatement();
			s.execute(sqlString);
			s.close();
		} catch (SQLException e) {
			showWarning(sqlString + " failed: " + e.getMessage());
			return false;
		}
		clearWarning();
		return true;
	}

	/**
	 * show a warning in the warning system part of the status bar
	 * Try to do something clever to count up similar types of warning. 
	 * @param title
	 * @param message
	 */
	private void showWarning(String message) {
		if (repeatWarning == null) {
			String tit = String.format("Database error table %s", getTableDefinition().tableName);
			repeatWarning = new RepeatWarning(tit, 50, 5);
		}
		repeatWarning.showWarning(message, 2);
	}
	
	private void clearWarning() {
		if (repeatWarning != null) {
			repeatWarning.clearWarning();
		}
	}

	/**
	 * find the super detection for this sub detection. 
	 * From superLogging we know the super data block, so find a 
	 * super detection that has that parent datablock. 
	 * @param subDetection sub detection in this logging. 
	 * @return a super detection from the datablock referenced in the superLogging class or null. 
	 */
	private PamDataUnit findSuperDetection(PamDataUnit subDetection) {
		if (superDetLogging == null) return null;
		PamDataBlock superDataBlock = superDetLogging.getPamDataBlock();
		return subDetection.getSuperDetection(superDataBlock);
	}

	/**
	 * @return the loggingAddOns
	 */
	public ArrayList<SQLLoggingAddon> getLoggingAddOns() {
		return loggingAddOns;
	}


}
