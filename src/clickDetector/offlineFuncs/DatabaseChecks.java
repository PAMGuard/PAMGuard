package clickDetector.offlineFuncs;

import generalDatabase.DBControlUnit;
import generalDatabase.PamConnection;
import generalDatabase.PamTableDefinition;
import generalDatabase.SQLTypes;
import generalDatabase.pamCursor.PamCursor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import clickDetector.ClickControl;
import clickDetector.ClickDataBlock;

public class DatabaseChecks {

	private DatabaseCheckObserver databaseCheckObserver;
	private ClickControl clickControl;
	private ClicksOffline clicksOffline;
	private ClickDataBlock clickDatablock;	
	private OfflineEventDataBlock eventDataBlock;
	private OfflineClickLogging clickLogging;
	private ArrayList<Integer> mainEventList;

	public DatabaseChecks(ClickControl clickControl, DatabaseCheckObserver databaseCheckObserver) {
		super();
		this.clickControl = clickControl;
		this.databaseCheckObserver = databaseCheckObserver;
	}

	public boolean runChecks(boolean repair) {
		clicksOffline = clickControl.getClicksOffline();
		clickDatablock = clickControl.getClickDataBlock();
		eventDataBlock = clickControl.getClickDetector().getOfflineEventDataBlock();
		clickLogging = new OfflineClickLogging(clickControl, null);

		checkEvents(repair);
		if (databaseCheckObserver.stopChecks()) {
			return false;
		}
		checkClicks(repair);
		if (databaseCheckObserver.stopChecks()) {
			return false;
		}

		return true;
	}

	private void checkEvents(boolean repair) {
		int nEvents = eventDataBlock.getUnitsCount();
		OfflineEventDataUnit anEvent;
		String message;
		for (int i = 0; i < nEvents; i++) {
			anEvent = eventDataBlock.getDataUnit(i, PamDataBlock.REFERENCE_CURRENT);
			message = String.format("Checking event %d of %d", i, nEvents);
			databaseCheckObserver.checkProgress(message, 2, 0, i*100 / nEvents);
			//			 databaseCheckObserver.checkOutputText(message, 0);
			if (databaseCheckObserver.stopChecks()) {
				return;
			} 
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			checkEvent(anEvent.getDatabaseIndex(), repair);
		}
		databaseCheckObserver.checkProgress("Event checks complete", 2, 1, 100);

	}

	/**
	 * Check a single event. 
	 * Basically, can just query all the clicks for this event, check the 
	 * start and end times and total number of clicks are all correct. 
	 * @param anEvent event
	 * @param repair repair the event if there is an error. 
	 * @return true if event OK / repaired. 
	 */
	private boolean checkEvent(int eventId, boolean repair) {
		OfflineEventDataUnit anEvent = eventDataBlock.findByDatabaseIndex(eventId);
		eventDataBlock.getLogging();
		PamConnection con = DBControlUnit.findConnection();
		if (con == null) {
			return false;
		}
		SQLTypes sqlTypes = DBControlUnit.findDatabaseControl().getDatabaseSystem().getSqlTypes();
		String clause = String.format(" WHERE EventId = %d ORDER BY UTC, UTCMilliseconds", eventId);
		String sqlStr = clickLogging.getTableDefinition().getBasicSelectString(sqlTypes) + clause;
		int nClicks = 0;
		/*
		 * Can't create a click with the standard logging function since it 
		 * expects to match to an existing click read from binary, so just fudge things
		 * a little to decode the UTC time into milliseconds. 
		 */
		Object ts;
		long resultTime;
		long firstTime = Long.MAX_VALUE;
		long lastTime = Long.MIN_VALUE;
		try {
			Statement stmt = con.getConnection().createStatement();
			ResultSet result = stmt.executeQuery(sqlStr);
			while (result.next()) {
				nClicks++;
				clickLogging.transferDataFromResult(sqlTypes, result);			
				ts = clickLogging.getTableDefinition().getTimeStampItem().getValue();
				resultTime = sqlTypes.millisFromTimeStamp(ts);
				if (resultTime%1000 == 0) {
					resultTime += clickLogging.getTableDefinition().getTimeStampMillis().getIntegerValue();
				}
				firstTime = Math.min(firstTime, resultTime);
				lastTime = Math.max(lastTime, resultTime);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		if (anEvent == null) {
			String cm = String.format("%d clicks are assigned to event %d but that event doesnt' exist", nClicks, eventId);
			databaseCheckObserver.checkOutputText(cm, 2);
			if (repair) {
				anEvent = createEvent(eventId, nClicks, firstTime, lastTime);				
			}
			else {
				return false;
			}
		}
		if (anEvent == null) {
			return false;
		}
		boolean updated = false;
		if (anEvent.getNClicks() != nClicks) {
			String msg = String.format("Event %d should have %d clicks, found %d in database", anEvent.getDatabaseIndex(), 
					anEvent.getNClicks(), nClicks);
			databaseCheckObserver.checkOutputText(msg, 1);
			if (repair) {
//				anEvent.setNClicks(nClicks);
				updated = true;
			}
		}
		if (anEvent.getTimeMilliseconds() != firstTime) {
			String msg = String.format("Event %d should have start time %s, not %s", anEvent.getDatabaseIndex(), 
					PamCalendar.formatDateTime(firstTime), PamCalendar.formatDateTime(anEvent.getTimeMilliseconds()));
			databaseCheckObserver.checkOutputText(msg, 1);
			if (repair) {
				anEvent.setTimeMilliseconds(firstTime);
				updated = true;
			}
		}
		if (Math.abs(anEvent.getEventEndTime() - lastTime) > 1000) {
			String msg = String.format("Event %d should have end time %s, not %s", anEvent.getDatabaseIndex(), 
					PamCalendar.formatDateTime(lastTime), PamCalendar.formatDateTime(anEvent.getEventEndTime()));
			databaseCheckObserver.checkOutputText(msg, 1);
			if (repair) {
				anEvent.setEventEndTime(lastTime);
				updated = true;
			}
		}
		if (updated) {
			anEvent.updateDataUnit(lastTime);
		}

		return true;
	}

	/**
	 * Create an event - called when there are clicks which have no event. 
	 * To make this work, will have to generate the event, add it to the datablock, 
	 * then save to the database to get an index, then change the database index
	 * for all the clicks in the click table. 
	 * @param eventId
	 * @param nClicks
	 * @param firstTime
	 * @param lastTime
	 * @return new event. 
	 */
	private OfflineEventDataUnit createEvent(int eventId, int nClicks,
			long firstTime, long lastTime) {
		OfflineEventDataUnit event = new OfflineEventDataUnit(null, 0, null);
		event.setTimeMilliseconds(firstTime);
		event.setEventEndTime(lastTime);
//		event.setNClicks(nClicks);
		eventDataBlock.addPamData(event);
		PamConnection con = DBControlUnit.findConnection();
		// now find a cursor and save it. 
		PamTableDefinition eventTableDef = eventDataBlock.getLogging().getTableDefinition();
		PamCursor cursor = eventDataBlock.getLogging().getViewerCursorFinder().getCursor(con, eventTableDef);
		cursor.immediateInsert(con);
		int newId = event.getDatabaseIndex();
		databaseCheckObserver.checkOutputText(String.format("New event created with Id %d for clicks from Event %d", newId, eventId), 1);
		/*
		 * Now update the event id for those clicks. 
		 */
		String uStr = String.format("UPDATE %s SET EventId = %d WHERE EventId = %d", clickLogging.getTableDefinition().getTableName(), newId, eventId);

		try {
			Statement stmt = con.getConnection().createStatement();
			boolean ans = stmt.execute(uStr);
		} catch (SQLException e) {
			e.printStackTrace();
			databaseCheckObserver.checkOutputText("Can't process click update query: " + uStr, 3);
		}
		
		return event;
	}

	private void checkClicks(boolean repair) {
		mainEventList = getEventListFromClicks();
		/**
		 * Now go through and check the events exist. 
		 */
		int eventId;
		OfflineEventDataUnit event;
		for (int i = 0; i < mainEventList.size(); i++) {
			eventId = mainEventList.get(i);
			event = eventDataBlock.findByDatabaseIndex(eventId);
			if (event == null) {
				checkEvent(eventId, repair);
			}
		}
		
	}
	
	/**
	 * Get a list of events from the clicks table. 
	 * @return list of events indexes. 
	 */
	ArrayList<Integer> getEventListFromClicks() {		
		String groupStr = String.format("SELECT EventId from %s GROUP BY EventId ORDER BY EventId", clickLogging.getTableDefinition().getTableName());
		PamConnection con = DBControlUnit.findConnection();
		Statement stmt;
		ArrayList<Integer> eventInds = new ArrayList<Integer>();
		int evId;
		try {
			stmt = con.getConnection().createStatement();
			ResultSet result = stmt.executeQuery(groupStr);
			while (result.next()) {
				evId = result.getInt(1);
				eventInds.add(evId);
				databaseCheckObserver.checkOutputText(String.format("Found clicks with Event Id %d", evId), 0);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return eventInds;
	}

}
