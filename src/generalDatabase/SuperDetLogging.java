package generalDatabase;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import PamController.PamViewParameters;
import PamguardMVC.PamDataUnit;
import PamguardMVC.debug.Debug;
import PamguardMVC.superdet.SuperDetDataBlock;
import PamguardMVC.superdet.SuperDetDataBlock.ViewerLoadPolicy;
import PamguardMVC.superdet.SuperDetection;
import generalDatabase.clauses.FixedClause;
import generalDatabase.clauses.PAMSelectClause;
import generalDatabase.clauses.UIDViewParameters;
import generalDatabase.pamCursor.PamCursor;
import pamScrollSystem.ViewLoadObserver;

/**
 * Extensions to SQLLogging to be used with datablocks that log sub detections. All
 * such blocks will be instances of SuperDetDatablock
 * @author Dougl
 *
 */
public abstract class SuperDetLogging extends SQLLogging {

	/**
	 * Datablock, will always be an instance of SuperDetDataBlock
	 */
	private SuperDetDataBlock superDataBlock;
	
	/**
	 * Reference to a subtable - used for objects grouped together by user selection.  If each
	 * row in the main table refers to a group (e.g. a click event) then each row in the subtable
	 * refers to an object held within the group (e.g. a click).  If there are 5 clicks in the
	 * click event then there are 5 rows in the subtable, and each links back to the click event
	 * in the main table.  Note that the subtable only holds REFERENCES to the objects, not
	 * the objects themselves.  Therefore <em>deleting</em> a row in a subtable merely removes
	 * that object from the group, it doesn't delete the object itself. Subtables should always
	 * declare a table definition using PamSubtableDefinition (or extended class thereof).
	 */
	private SQLLogging subLogging = null;
	

	/**
	 * how to load sub table data
	 * @see SUBTABLECLAUSE_TIME
	 * @see SUBTABLECLAUSE_PARENTUID
	 */
	private int subTableClausePolicy = SUBTABLECLAUSE_TIME;
	
	/**
	 * When loading sub table data, load data using the same between times clause as
	 * was applied to the super datablock
	 */
	public static final int SUBTABLECLAUSE_TIME = 0;

	/**
	 * When loading sub table data, load data using a clause which 
	 * will guarantee loading of all sub detections of loaded super detections
	 * based around UID's
	 */
	public static final int SUBTABLECLAUSE_PARENTUID = 1;

	/**
	 * When loading sub table data, load data using a clause which 
	 * will guarantee loading of all sub detections of loaded super detections
	 * based around database indexes. 
	 */
	public static final int SUBTABLECLAUSE_PARENTDATABASEID = 2;
	
	/**
	 * PamTable item for the event end. Can be used in query building. 
	 */
	private PamTableItem eventEndTimeItem;
	
	/**
	 * Flag to say to create actual data units as well as loading subtable data. 
	 * These are not necessarily loaded over the same selection
	 * Should be false if data units created elsewhere. 
	 */
	private boolean createDataUnits = false;


	protected SuperDetLogging(SuperDetDataBlock pamDataBlock) {
		this(pamDataBlock, false);
	}

	protected SuperDetLogging(SuperDetDataBlock pamDataBlock, boolean createDataUnits) {
		super(pamDataBlock);
		this.superDataBlock = pamDataBlock;
		this.createDataUnits = createDataUnits;
	}
	
	


	/**
	 * @return the subLogging
	 */
	public SQLLogging getSubLogging() {
		return subLogging;
	}

	/**
	 * @param subLogging the subLogging to set
	 */
	public void setSubLogging(SQLLogging subLogging) {
		this.subLogging = subLogging;
		subLogging.superDetLogging = this;
	}

	@Override
	public synchronized boolean logData(PamConnection con, PamDataUnit dataUnit, PamDataUnit superDetection) {
		boolean ok = super.logData(con, dataUnit, superDetection);
		if (ok && subLogging != null) {
			subLogging.logSubtableData(con, (SuperDetection) dataUnit);
		}
		return ok;
	}

	@Override
	public synchronized boolean reLogData(PamConnection con, PamDataUnit dataUnit, PamDataUnit superDetection) {
		boolean ok = super.reLogData(con, dataUnit, superDetection);
		if (ok && subLogging != null) {
			subLogging.updateSubtable(con, (SuperDetection) dataUnit);
		}
		return ok;
	}

	@Override
	public boolean saveOfflineData(DBControlUnit dbControlUnit, PamConnection connection) {
		return super.saveOfflineData(dbControlUnit, connection);
	}
	
	@Override
	public boolean loadViewData(PamConnection con, PamViewParameters pamViewParameters, ViewLoadObserver loadObserver) {
		boolean ok = true;
		
		if (needMainDataLoad()) {
			PamViewParameters modifiedParams = getModifiedViewParams(con, pamViewParameters);
//			System.out.println(getPamDataBlock().getLongDataName() + " : " + pamViewParameters.getSelectClause(DBControlUnit.findConnection().getSqlTypes()));
//			System.out.println(getPamDataBlock().getLongDataName() + " : " + modifiedParams.getSelectClause(DBControlUnit.findConnection().getSqlTypes()));
			ok = super.loadViewData(con, modifiedParams, loadObserver);
			long t1 = System.currentTimeMillis();
			ArrayList<PamSubtableData> subTableData = loadSubTableInformation(con, pamViewParameters, loadObserver);
			long t2 = System.currentTimeMillis();
			superDataBlock.setSubtableData(subTableData, loadObserver);
			long t3 = System.currentTimeMillis();
			if (subTableData != null) {
				Debug.out.printf("Load %d subdets %s %dms, match to data units %dms\n", subTableData.size(), 
						getPamDataBlock().getDataName(), t2-t1, t3-t2);
			}
		}
		
		if (createDataUnits) {
			/**
			 * It's a shame to have to do this separate from the above loadSubTableData call, since it's 
			 * querying into the same database, but the selection may be slightly different in 
			 * the two cases, the first trying to get all the subtableinfo for the superdets, this call
			 * trying to get the sub detection data for the normal load period, getting the same data as
			 * would normally be in binary data. 
			 */
			// call this with the normal load parameters. 
			ok &= subLogging.loadViewData(con, pamViewParameters, loadObserver);
		}
		
		return ok;
	}
	
	/**
	 * Do we need to load the main data ? No, not if all data are always being held in memory 
	 * and haven't been cleared.
	 * @return true if data need to be loaded. 
	 */
	private boolean needMainDataLoad() {
		if (superDataBlock.getViewerLoadPolicy() == ViewerLoadPolicy.LOAD_ALWAYS_EVERYTHING) {
			return getPamDataBlock().getUnitsCount() == 0; // i.e. don't load if data are loaded because they will never be deleted (I hope) 
		}
		else {
			return true;
		}
	}
	
	/**
	 * Get the sub detection table information. This is just the cross reference information for the sub detections 
	 * and does not load the actual data units. 
	 * @param con
	 * @param pamViewParameters
	 * @param loadObserver 
	 * @return
	 */
	private ArrayList<PamSubtableData> loadSubTableInformation(PamConnection con, PamViewParameters pamViewParameters, ViewLoadObserver loadObserver) {
		/**
		 * Needs modifying. Should probably use an in clause, or just load everyting depending on the load type for the data
		 * or could get the min and max start and end times and query everything in that range - good compromise. Might load a couple 
		 * of things that don't have data loaded, but I think that's OK. Just don't print a warning if we can't find a datablock for them. 
		 */
		/*
		 * this is called after the data have been loaded, so we can simply take the time limits of them both
		 */
		if (superDataBlock.getUnitsCount() == 0) {
			return null; // no need to do anything if there are no super detections. 
		}
		
		PamViewParameters subTableClause = getSubTableLoadClause(con, pamViewParameters);
		if (subLogging == null) {
			System.out.printf("loadSubTableInformation cannot since no subtable present for %s\n", getPamDataBlock().getDataName());
			return null;
		}
		return subLogging.loadSubtableData(con, this, subTableClause, loadObserver);
			
//		if (subTableClausePolicy == SUBTABLECLAUSE_TIME) {
//			return subLogging.loadSubtableData(con, this, pamViewParameters);
//		}
//		else if (subTableClausePolicy == SUBTABLECLAUSE_PARENTUID) {
//			UIDViewParameters uidViewParameters = UIDViewParameters.createUIDViewParameters(getPamDataBlock());
//			//				uidList += ")";
//			return subLogging.loadSubtableData(con, this, uidViewParameters);
//		}
//		else if (subTableClausePolicy == SUBTABLECLAUSE_PARENTDATABASEID) {
//			UIDViewParameters uidViewParameters = UIDViewParameters.createDatabaseIDViewParameters(getPamDataBlock());
//			return subLogging.loadSubtableData(con, this, uidViewParameters);
//		}
//		return null;
}
	private PamViewParameters getSubTableLoadClause(PamConnection con, PamViewParameters pamViewParameters) {
		if (superDataBlock.getViewerLoadPolicy() == ViewerLoadPolicy.LOAD_ALWAYS_EVERYTHING) {
			return loadEverythingClause(null, pamViewParameters); // same clause since no columns mentioned 
		}
		// get a big IN clause for the loaded super detections. 
		return UIDViewParameters.createDatabaseIDViewParameters(getPamDataBlock());
	}

	
	/**
	 * Modified view clause for super detections, may change the basic clause to either get absolutely
	 * everything, or to get events that overlap with the load time, not just start within it (requires a 
	 * reference to an endtime column)
	 * @param con
	 * @param pamViewParameters
	 * @return
	 */
	private PamViewParameters getModifiedViewParams(PamConnection con, PamViewParameters pamViewParameters) {
		switch (superDataBlock.getViewerLoadPolicy()) {
		case LOAD_ALWAYS_EVERYTHING:
			return loadEverythingClause(con, pamViewParameters);
		case LOAD_OVERLAPTIME:
			return loadOverlapClause(con, pamViewParameters);
		case LOAD_UTCNORMAL:
			return pamViewParameters;
		}
		return pamViewParameters;
	}

	/**
	 * Clause for overlaps. 
	 * @param con
	 * @param pamViewParameters
	 * @return
	 */
	private PamViewParameters loadOverlapClause(PamConnection con, PamViewParameters pamViewParameters) {
		if (eventEndTimeItem == null) {
			return pamViewParameters; // can't do anything if we don't know the end time column name. 
		}
		// need something like WHERE NOT (EndTime < firstTime OR UTC > lastTime);
		SQLTypes sqlTypes = con.getSqlTypes();
		String clause = String.format(" WHERE NOT (%s < %s OR  %s > %s) ORDER BY UTC, UTCMilliseconds", 
				sqlTypes.formatColumnName(eventEndTimeItem.getName()), sqlTypes.formatDBDateTimeQueryString(pamViewParameters.viewStartTime), 
				"UTC", sqlTypes.formatDBDateTimeQueryString(pamViewParameters.viewEndTime));
		return new FixedClause(clause);
	}

	/**
	 * Clause to load absolutely everything
	 * @param con
	 * @param pamViewParameters
	 * @return
	 */
	private PamViewParameters loadEverythingClause(PamConnection con, PamViewParameters pamViewParameters) {
		return new FixedClause(" ORDER BY UTC, UTCMilliseconds");
	}


	@Override
	public boolean deleteData(PAMSelectClause pvp) {
		PamConnection con = DBControlUnit.findConnection();
		if (con == null) {
			return false;
		}
		SQLTypes sqlTypes = con.getSqlTypes();
		if (subLogging!=null) {
			String sqlStr = String.format("SELECT Id FROM %s %s", 
					getTableDefinition().getTableName(), 
					pvp.getSelectClause(con.getSqlTypes()));
			System.out.println(sqlStr);
			try {
				Statement stmt = con.getConnection().createStatement();
				ResultSet result = stmt.executeQuery(sqlStr);
				while (result.next()) {
					Object idObj = result.getObject(1);
					Long id = sqlTypes.getLongValue(idObj);
					subLogging.deleteSubtableItems(con, id);
				}
				stmt.close();
			} catch (SQLException e) {
				System.out.println("Error retrieving parent IDs before deletion");
				e.printStackTrace();
			}
		}
		
		boolean ok = super.deleteData(pvp);

		return ok;
	}
	
//	@Override
//	public boolean deleteData(PamDataUnit aDataUnit) {
	/*
	 * No longer needed since super.deleteData gois back into deleteIndexedItems which 
	 * is nicely overridden. 
	 */
//		boolean ok = super.deleteData(aDataUnit);
//		// if we also have a subtable, remove any subdetections linked to this unit
//		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
//		if (dbControl == null) {
//			return false;
//		}
//		PamConnection connection = dbControl.getConnection();
//		return subLogging.deleteSubtableData(connection, (SuperDetection) aDataUnit);
//	}
	
	@Override
	protected int insertCursorRow(PamConnection connection, PamCursor pamCursor, PamDataUnit aUnit) throws SQLException {
		int row = super.insertCursorRow(connection, pamCursor, aUnit);
		if (aUnit instanceof SuperDetection) {
			SuperDetection superDet = (SuperDetection) aUnit;
			subLogging.logSubtableData(connection, superDet);
		}
		return row;
	}
	
	@Override
	protected void updateCursorRow(PamConnection connection, PamCursor pamCursor, PamDataUnit aUnit) throws SQLException {
		super.updateCursorRow(connection, pamCursor, aUnit);
		subLogging.updateSubtable(connection, (SuperDetection) aUnit);
	}
	
	@Override
	public boolean deleteIndexedItems(PamConnection connection, int[] deleteIndexes) {
		
		boolean ok = super.deleteIndexedItems(connection, deleteIndexes);
		
		// if we also have a subtable, remove any subdetections linked to the deleted units.
					// Do this before we clear the list of deleted units from the data block
//					if (subLogging != null) {
//						List<PamDataUnit> delUnits = pamDataBlock.getRemovedItems();
//						for (int i=0; i<delUnits.size(); i++) {
//							PamDataUnit pdu = delUnits.get(i);
//							deleteSubtableData(connection, pdu);
//						}
//					}
		//do it with one big 'in' clause
		String sqlString = String.format("DELETE FROM %s WHERE ParentId %s", 
				subLogging.getTableDefinition().getTableName(), createInClause(deleteIndexes));
		try {
			Statement s = connection.getConnection().createStatement();
			s.execute(sqlString);
			s.close();
		} catch (SQLException e) {
			System.out.println("Delete of subtable items failed with " + sqlString);
			e.printStackTrace();
			return false;
		}
		return ok;
	}

	/**
	 * Check the number of entries in the sub table which have this database index. 
	 * @param databaseIndex
	 * @return number of sub detections. 
	 */
	public int checkSubTableCount(int databaseIndex) {
//		String qStr = String.format("SELECT Id FROM %s WHERE %s=%d", subLogging.getTableDefinition().getTableName(), 
//				PamSubtableDefinition.PARENTIDNAME, databaseIndex);
		String qStr = String.format("SELECT COUNT (Id) FROM %s WHERE %s=%d", subLogging.getTableDefinition().getTableName(), 
		PamSubtableDefinition.PARENTIDNAME, databaseIndex);
//		int n = 0;
		PamConnection con = DBControlUnit.findConnection();
		try {
			Statement stmt = con.getConnection().createStatement();
			ResultSet res = stmt.executeQuery(qStr);
			if (res.next()) {
				return res.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}


		return 0;
	}



	/** 
	 * find if any sub table items that satisfy a particular clause. 
	 * @param mainTableClause
	 * @param subTableClause
	 * @return true if any items satisfy clause.
	 */
	public Boolean anySubTableItems(PAMSelectClause mainTableClause, PAMSelectClause subTableClause) {
		/**
		 * This is a bit of a nightmare since we're querying both tables with an inner join on the 
		 * sub table. e.g. 
		 * SELECT COUNT(Id) FROM OfflineEvents INNER JOIN OfflineEvents.Id ON OfflineClicks.PArentId WHERE
		 * OfflineEvent.Type IN ('HP', 'SPW', 'DO') AND OfflineClicks.UTC BETWEEN ... AND ...
		 */
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
			if (dbControl == null || getTableDefinition() == null) {
				return null;
			}
			PamConnection pamConn = dbControl.getConnection();
			if (pamConn == null) {
				return null;
			}
			SQLTypes sqlTypes = pamConn.getSqlTypes();
		String thisTable = getTableDefinition().getTableName();
		String subTable = subLogging.getTableDefinition().getTableName();
		String qStr = String.format("SELECT %s.Id FROM %s INNER JOIN %s ON %s.Id = %s.parentID %s", 
				thisTable, thisTable, subTable, thisTable, subTable,  mainTableClause.getSelectClause(sqlTypes) );
//		System.out.println(qStr);
//		qStr = "SELECT COUNT(Id) FROM Click_Detector_OfflineEvents INNER JOIN %s ON Click_Detector_OfflineClicks.parentID ON Click_Detector_OfflineEvents.Id";
		int count = 0;
		try {
			PreparedStatement stmt = pamConn.getConnection().prepareStatement(qStr);
			ResultSet result = stmt.executeQuery();
			if (result.next()) {
				count = 1;
			}
			result.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return count>0;
	}

	/** 
	 * Count the number of sub table items that satisfy a particular clause. 
	 * @param mainTableClause
	 * @param subTableClause
	 * @return count of items.
	 */
	public Integer countSubTableItems(PAMSelectClause mainTableClause, PAMSelectClause subTableClause) {
		/**
		 * This is a bit of a nightmare since we're querying both tables with an inner join on the 
		 * sub table. e.g. 
		 * SELECT COUNT(Id) FROM OfflineEvents INNER JOIN OfflineEvents.Id ON OfflineClicks.PArentId WHERE
		 * OfflineEvent.Type IN ('HP', 'SPW', 'DO') AND OfflineClicks.UTC BETWEEN ... AND ...
		 */
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
			if (dbControl == null || getTableDefinition() == null) {
				return null;
			}
			PamConnection pamConn = dbControl.getConnection();
			if (pamConn == null) {
				return null;
			}
			SQLTypes sqlTypes = pamConn.getSqlTypes();
		String thisTable = getTableDefinition().getTableName();
		String subTable = subLogging.getTableDefinition().getTableName();
		String qStr = String.format("SELECT COUNT(%s.Id) FROM %s INNER JOIN %s ON %s.Id = %s.parentID %s", 
				thisTable, thisTable, subTable, thisTable, subTable,  mainTableClause.getSelectClause(sqlTypes) );
//		System.out.println(qStr);
//		qStr = "SELECT COUNT(Id) FROM Click_Detector_OfflineEvents INNER JOIN %s ON Click_Detector_OfflineClicks.parentID ON Click_Detector_OfflineEvents.Id";
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

	/**
	 * @return the subTableClausePolicy
	 * @see SUBTABLECLAUSE_TIME
	 * @see SUBTABLECLAUSE_PARENTUID
	 */
	public int getSubTableClausePolicy() {
		return subTableClausePolicy;
	}

	/**
	 * @param subTableClausePolicy the subTableClausePolicy to set
	 * @see SUBTABLECLAUSE_TIME
	 * @see SUBTABLECLAUSE_PARENTUID
	 */
	public void setSubTableClausePolicy(int subTableClausePolicy) {
		this.subTableClausePolicy = subTableClausePolicy;
	}


	public void doSubTableUIDRepairs() {
		// TODO Auto-generated method stub
		
	}


	/**
	 * @return the eventEndTimeItem
	 */
	public PamTableItem getEventEndTimeItem() {
		return eventEndTimeItem;
	}


	/**
	 * @param eventEndTimeItem the eventEndTimeItem to set
	 */
	public void setEventEndTimeItem(PamTableItem eventEndTimeItem) {
		this.eventEndTimeItem = eventEndTimeItem;
	}




	/**
	 * @return the createDataUnits
	 */
	public boolean isCreateDataUnits() {
		return createDataUnits;
	}




	/**
	 * @param createDataUnits the createDataUnits to set
	 */
	public void setCreateDataUnits(boolean createDataUnits) {
		this.createDataUnits = createDataUnits;
	}
}
