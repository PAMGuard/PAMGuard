package clickDetector.offlineFuncs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import clickDetector.ClickControl;
import clickDetector.ClickDetection;
import PamController.PamViewParameters;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.SaveRequirements;
import generalDatabase.DBControlUnit;
import generalDatabase.PamConnection;
import generalDatabase.PamSubtableDefinition;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;
import generalDatabase.SuperDetLogging;
import generalDatabase.lookupTables.LookUpTables;
import pamScrollSystem.ViewLoadObserver;

public class OfflineEventLogging extends SuperDetLogging {
	
	private PamTableDefinition tableDef;
	
	static public final int COMMENT_LENGTH = 80;
	
	private ClickControl clickControl;
	
	private PamTableItem endTime, nClicks, minNumber, bestNumber, maxNumber, eventType, 
	comment, colourIndex, channels;
	

	public OfflineEventLogging(ClickControl clickControl, OfflineEventDataBlock pamDataBlock) {
		super(pamDataBlock, false);
		this.clickControl = clickControl;
		tableDef = new PamTableDefinition(clickControl.getUnitName()+"_OfflineEvents", SQLLogging.UPDATE_POLICY_OVERWRITE);
		tableDef.addTableItem(endTime = new PamTableItem("EventEnd", Types.TIMESTAMP));
		tableDef.addTableItem(eventType = new PamTableItem("eventType", Types.CHAR, LookUpTables.CODE_LENGTH));
		tableDef.addTableItem(nClicks = new PamTableItem("nClicks", Types.INTEGER));
		tableDef.addTableItem(minNumber = new PamTableItem("minNumber", Types.SMALLINT));
		tableDef.addTableItem(bestNumber = new PamTableItem("bestNumber", Types.SMALLINT));
		tableDef.addTableItem(maxNumber = new PamTableItem("maxNumber", Types.SMALLINT));
		tableDef.addTableItem(colourIndex = new PamTableItem("colour", Types.SMALLINT));
		tableDef.addTableItem(comment = new PamTableItem("comment", Types.CHAR, COMMENT_LENGTH));
		tableDef.addTableItem(channels = new PamTableItem("channels", Types.INTEGER));
		tableDef.setUpdatePolicy(UPDATE_POLICY_OVERWRITE);
		tableDef.setUseCheatIndexing(false);
		
		setTableDefinition(tableDef);
		
		setEventEndTimeItem(endTime);
		
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		OfflineEventDataUnit oedu = (OfflineEventDataUnit) pamDataUnit;
		endTime.setValue(sqlTypes.getTimeStamp(oedu.getEventEndTime()));
		eventType.setValue(oedu.getEventType());
		nClicks.setValue(oedu.getNClicks());
		minNumber.setValue(oedu.getMinNumber());
		bestNumber.setValue(oedu.getBestNumber());
		maxNumber.setValue(oedu.getMaxNumber());
		colourIndex.setValue(oedu.getColourIndex());
		comment.setValue(oedu.getComment());
		channels.setValue(oedu.getChannelBitmap());		
	}

	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {
		OfflineEventDataUnit dataUnit = new OfflineEventDataUnit(eventType.getStringValue(), 
				colourIndex.getIntegerValue(), null);
		dataUnit.setDatabaseIndex(tableDef.getIndexItem().getIntegerValue());
		dataUnit.setTimeMilliseconds(timeMilliseconds);	
		dataUnit.setDatabaseIndex(databaseIndex);
		Object ts = endTime.getValue();
		if (ts != null) {
			long endTime = sqlTypes.millisFromTimeStamp(ts);
			dataUnit.setEventEndTime(endTime);
		}
//		dataUnit.setNClicks(nClicks.getIntegerValue());
//		try {
		dataUnit.setMinNumber(minNumber.getShortValue());
//		}
//		catch (Exception e) {
//			minNumber.getShortValue();
//		}
		dataUnit.setBestNumber(bestNumber.getShortValue());
		dataUnit.setMaxNumber(maxNumber.getShortValue());
		dataUnit.setComment(comment.getStringValue());
		int chans = channels.getIntegerValue(); 
		if (chans == 0) {
			chans = 3;
		}
		dataUnit.setChannelBitmap(chans);
		getPamDataBlock().addPamData(dataUnit);
		return dataUnit;
	}

//	@Override
//	public String getViewerLoadClause(SQLTypes sqlTypes, PamViewParameters pvp) {
//		return "ORDER BY UTC, UTCMilliseconds";
//	}

//	public boolean loadViewData(PamConnection con, PamViewParameters pamViewParameters, ViewLoadObserver loadObserver) {
//		return super.loadViewData(con, pamViewParameters, loadObserver);
//	}

	@Override
	public boolean saveOfflineData(DBControlUnit dbControlUnit,
			PamConnection connection) {
		/**
		 * Needs to delete a load of clicks as well as 
		 * doing the normal delete of events. 
		 */
//		***** No longer need to do this, because it's handled by the subtable ******
//		SaveRequirements sr = getPamDataBlock().getSaveRequirements(dbControlUnit);
//		if (sr.getNumDeletions() > 0) {
//			deleteEventClicks(connection, sr.getDeleteIndexes());
//		}
		return super.saveOfflineData(dbControlUnit, connection);
	}

//	private void deleteEventClicks(PamConnection connection, int[] deleteIndexes) {
//		/*
//		 * First need to find the click logging class. 
//		 */
//		OfflineClickLogging clickLogging = clickControl.getClickDataBlock().getOfflineClickLogging();
//		clickLogging.deleteEventClicks(connection, deleteIndexes);
//	}

	/* (non-Javadoc)
	 * @see generalDatabase.SQLLogging#doSubTableUIDRepairs()
	 */
	@Override
	public void doSubTableUIDRepairs() {
		super.doSubTableUIDRepairs();
		/*
		 * 1. Copy all EventId values into LinkId column
		 * 2. Add the click detector stream name to every row
		 * 3. match LinkUID's in the sub table to UID's in the main table.  
		 */
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		if (dbControl == null) return;
		SQLLogging subTableLogging = getSubLogging();
		PamConnection con = DBControlUnit.findConnection();
		SQLTypes sqlTypes = con.getSqlTypes();

		// now the LinkUIDs - I bet this could be done with a fancy crosstab query, but do one by one. 
		// 99 % of time when we're updating LinkUID's they will be the same as the linked EventId's, so 
		// set them to that first to avoid nulls. 
		String q = String.format("UPDATE %s SET ParentId = EventId, ParentUID = EventId", 
				sqlTypes.formatColumnName(subTableLogging.getTableDefinition().getTableName()));
		System.out.println(q);
		boolean r = false;
		try {
			Statement stmt = con.getConnection().createStatement();
			r = stmt.execute(q);
			dbControl.commitChanges();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		q = String.format("UPDATE %s SET %s = '%s'", sqlTypes.formatColumnName(subTableLogging.getTableDefinition().getTableName()),
				PamSubtableDefinition.LONGDATANAME, subTableLogging.getPamDataBlock().getLongDataName());
		System.out.println(q);
		try {
			Statement stmt = con.getConnection().createStatement();
			r = stmt.execute(q);
			dbControl.commitChanges();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		// then do a more thorough going through the database getting the correct UID from the event table. 
		String q1 = String.format("SELECT Id, UID FROM %s ORDER BY Id", getTableDefinition().getTableName());
		int iD;
		Object uid;
		try {
			Statement stmt = con.getConnection().createStatement();
			ResultSet rs = stmt.executeQuery(q1);
			Statement stmt2 = con.getConnection().createStatement();
			while (rs.next()) {
				iD = rs.getInt(1);
				uid = rs.getObject(2);
				String q2 = String.format("UPDATE %s SET ParentUID=%d WHERE EventId=%d", 
						subTableLogging.getTableDefinition().getTableName(), uid, iD);
				r = stmt2.execute(q2);
				dbControl.commitChanges();
			}
			stmt.close();
			stmt2.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}


//	/* (non-Javadoc)
//	 * @see generalDatabase.SQLLogging#logData(generalDatabase.PamConnection, PamguardMVC.PamDataUnit)
//	 */
//	@Override
//	public synchronized boolean logData(PamConnection con, PamDataUnit dataUnit) {
//		boolean ans = super.logData(con, dataUnit);
//		OfflineEventDataUnit event = (OfflineEventDataUnit) dataUnit;
//		System.out.println("Database write event " + event.getEventId());
//		return ans;
//	}
//
//	/* (non-Javadoc)
//	 * @see generalDatabase.SQLLogging#reLogData(generalDatabase.PamConnection, PamguardMVC.PamDataUnit)
//	 */
//	@Override
//	public synchronized boolean reLogData(PamConnection con, PamDataUnit dataUnit) {
//		boolean ans = super.reLogData(con, dataUnit);
//		OfflineEventDataUnit event = (OfflineEventDataUnit) dataUnit;
//		System.out.println("Database re-write event " + event.getEventId());
//		return ans;
//	}

}
