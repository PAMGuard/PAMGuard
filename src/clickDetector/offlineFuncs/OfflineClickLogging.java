package clickDetector.offlineFuncs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ListIterator;

import binaryFileStorage.DataUnitFileInformation;
import clickDetector.ClickControl;
import clickDetector.ClickDataBlock;
import clickDetector.ClickDetection;
import PamguardMVC.DataUnitFinder;
import PamguardMVC.PamDataUnit;
import generalDatabase.DBControlUnit;
import generalDatabase.PamConnection;
import generalDatabase.PamSubtableDefinition;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;
import generalDatabase.pamCursor.PamCursor;

public class OfflineClickLogging extends SQLLogging {

	private ClickControl clickControl;

	private ClickDataBlock clickDataBlock;

	protected PamTableItem eventId;

//	protected PamTableItem binaryFile;	binary filename removed from here, added to common data saved for each subtable item in PamSubtableDefinition/SQLLogging

	protected PamTableItem clickNumber;

	protected PamTableItem amplitude;
	
	protected PamTableItem channelNumbers;

//	static public final int BINARY_FILE_NAME_LENGTH = 80;

	public OfflineClickLogging(ClickControl clickControl, ClickDataBlock pamDataBlock) {
		super(pamDataBlock);
		this.clickControl = clickControl;
		this.clickDataBlock = pamDataBlock;

		PamTableDefinition tableDef;
		// name must be +"_OfflineClicks" for backwards compatibility
		tableDef = new PamSubtableDefinition(clickControl.getUnitName()+"_OfflineClicks");
		tableDef.addTableItem(eventId = new PamTableItem("EventId", Types.INTEGER));
//		tableDef.addTableItem(binaryFile = new PamTableItem("BinaryFile", Types.CHAR, BINARY_FILE_NAME_LENGTH));
		tableDef.addTableItem(clickNumber = new PamTableItem("ClickNo", Types.INTEGER));
		tableDef.addTableItem(amplitude = new PamTableItem("Amplitude", Types.DOUBLE));
		tableDef.addTableItem(channelNumbers = new PamTableItem("Channels", Types.INTEGER));
		tableDef.setUseCheatIndexing(false);

		setTableDefinition(tableDef);
	}

	private PamConnection lastConnection;

	private DataUnitFinder<ClickDetection> dataFinder;
	private void checkTable() {
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		if (lastConnection == null || lastConnection != dbControl.getConnection()) {
			lastConnection = dbControl.getConnection();
			dbControl.getDbProcess().checkTable(getTableDefinition());
		}
	}

// **** Not used anymore, since this has become a subtable of OfflineEventLogging ****
//	@Override
//	public boolean loadViewerData(long dataStart, long dataEnd, ViewLoadObserver loadObserver) {
//		checkTable();
//		/*
//		 * Want to get a fast find of the click data going, so initialise it
//		 * here and clean up after the data are loaded. 
//		 */
//		OfflineEventDataBlock eventDataBlock = clickControl.getClickDetector().getOfflineEventDataBlock();
////		System.out.println(String.format("loading clicks between %s and %s, %d events and %d clicks in memory", 
////				PamCalendar.formatDateTime(dataStart), 
////				PamCalendar.formatDateTime(dataEnd), eventDataBlock.getUnitsCount(), 
////				getPamDataBlock().getUnitsCount()));
//		dataFinder = new DataUnitFinder<ClickDetection>(getPamDataBlock(), new BinaryFileMatcher());
////		dataStart -= 3600000;
////		dataEnd += 3600000;
//		boolean ok = super.loadViewerData(dataStart, dataEnd, loadObserver);
//		return ok;
//	}


	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		
		DataUnitFileInformation fileInfo = pamDataUnit.getDataUnitFileInformation();
		if (fileInfo != null) {
//			binaryFile.setValue(fileInfo.getShortFileName(BINARY_FILE_NAME_LENGTH));
			clickNumber.setValue((int)fileInfo.getIndexInFile());
		}
		OfflineEventDataUnit offlineEvent = (OfflineEventDataUnit) 
				pamDataUnit.getSuperDetection(OfflineEventDataUnit.class);
		if (offlineEvent == null) {
			eventId.setValue(((ClickDetection) pamDataUnit).getEventId());
		}
		else {
			eventId.setValue(offlineEvent.getDatabaseIndex());
		}
		channelNumbers.setValue(pamDataUnit.getChannelBitmap());
	
		// specific checks for ClickDetection objects
		if (pamDataUnit.getClass() == ClickDetection.class) {
			ClickDetection clickDetection = (ClickDetection) pamDataUnit;
			amplitude.setValue(clickDetection.getAmplitudeDB());
			if (eventId.getIntegerValue()==-1) {
				eventId.setValue(clickDetection.getEventId());				
			}
		}
		else {
			amplitude.setValue(null);
		}
	}

// **** Not used anymore, since this has become a subtable of OfflineEventLogging ****
//	@Override
//	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {
//		String fileName = binaryFile.getStringValue();
//		long time = getLastTime();
//		int clickNum = clickNumber.getIntegerValue();
//		int eventId = this.eventId.getIntegerValue();
//		int clickIndex = getTableDefinition().getIndexItem().getIntegerValue();
//		OfflineEventDataBlock eventDataBlock = clickControl.getClickDetector().getOfflineEventDataBlock();
//
//		ClickDetection aClick = dataFinder.findDataUnit(fileName, clickNum);
//		if (aClick == null) {
////			System.out.println(String.format("Unable to find click %d in %s", clickNum, fileName));
//		}
//		else {
//			aClick.setDatabaseIndex(clickIndex);
//			OfflineEventDataUnit event = eventDataBlock.findByDatabaseIndex(eventId);
//			if (event != null) {
//				event.addSubDetection(aClick, false);
//			}
//		}
//		// amplitude doesn't get read back since it's already known from the binary data
//		
//
//		return aClick;
//	}

// **** Not used anymore, since this has become a subtable of OfflineEventLogging ****
//	public boolean saveViewerData() {
//		checkTable();
//		PamDataBlock pamDataBlock = getPamDataBlock();
////		long dataStart = pamDataBlock.getCurrentViewDataStart();
////		long dataEnd = pamDataBlock.getCurrentViewDataEnd();
//		if (pamDataBlock.getUnitsCount() == 0) {
//			return true;
//		}
//		long dataStart = pamDataBlock.getFirstUnit().getTimeMilliseconds() - 1000;
//		long dataEnd = pamDataBlock.getLastUnit().getTimeMilliseconds() + 1000;
//		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
//		PamConnection con = DBControlUnit.findConnection();
//		PamCursor pamCursor = dbControl.createPamCursor(getTableDefinition());
//		PamViewParameters pvp = new PamViewParameters();
//		pvp.viewStartTime = dataStart;
//		pvp.viewEndTime = dataEnd;
//		pvp.useAnalysisTime = false;
//		String clause = getTableDefinition().getBetweenString(con.getSqlTypes(), pvp) + " ORDER BY UTC";
//		boolean ok = pamCursor.openScrollableCursor(con, true, true, clause);
//		if (ok == false) {
//			System.out.println("OfflineClickLogging.SaveViewerData():  Unable to open scrollable cursor for " + getTableDefinition().getTableName());
//			return false;
//		}
//		/*
//		 * Now scroll through the data trying to match each point from the database to a data unit. 
//		 * If a data unit cannot be found it's an error - all loaded data should have a click from 
//		 * the binary store in memory. 
//		 * IF the click has no super detection, then it will need deleting from the database
//		 * IF the click HAS a super detection, then it will need updating in the database. 
//		 * Finally go through all clicks and add any which don't have a database index number. 
//		 */
//		PamDataUnit dataUnit;
//		PamDataUnit superDetection;
//		int dbId;
//		pamCursor.beforeFirst();
//		try {
//			/*
//			 * First go through the cursor data and try to match with 
//			 * loaded data. then either update or delete. 
//			 */
//			while (pamCursor.next()) {
//				dbId = pamCursor.getInt(1);
//				dataUnit = pamDataBlock.findByDatabaseIndex(dbId);
//				if (dataUnit == null) {
//					System.out.println(pamDataBlock.getDataName() + "OfflineClickLogging.SaveViewerData(): Unable to find data unit for database index " + dbId);
//					continue;
//				}
//				superDetection = dataUnit.getSuperDetection(OfflineEventDataUnit.class);
//				if (superDetection == null) {
//					pamCursor.deleteRow();
//				}
//				else {
//					fillTableData(con.getSqlTypes(), dataUnit);
//					pamCursor.moveDataToCursor(true);
//					pamCursor.updateRow();
//				}
//			}
//			/*
//			 * Then go through loaded data, and anything which isn't matched to 
//			 * data in the database can be used to make a new record. 
//			 */
//			ListIterator<PamDataUnit> listIt = pamDataBlock.getListIterator(0);
//			while (listIt.hasNext()) {
//				dataUnit = listIt.next();
//				superDetection = dataUnit.getSuperDetection(OfflineEventDataUnit.class);
//				if (superDetection == null) {
//					continue;
//				}
//				if (dataUnit.getDatabaseIndex() == 0) {
//					pamCursor.moveToInsertRow();
//					fillTableData(con.getSqlTypes(), dataUnit);
//					pamCursor.moveDataToCursor(true);
//					dbId = pamCursor.insertRow(true);
//					dataUnit.setDatabaseIndex(dbId);
//				}
//			}
//			pamCursor.updateDatabase();
//			pamCursor.closeScrollableCursor();
//		}
//		catch (SQLException e) {
//			e.printStackTrace();
//		}
//
//		return true;
//	}

// **** Not used anymore, since this has become a subtable of OfflineEventLogging ****
//	public void deleteEventClicks(PamConnection connection, int[] deleteIndexes) {
//		String sqlString = String.format("DELETE FROM %s WHERE EventId %s", 
//				getTableDefinition().getTableName(), createInClause(deleteIndexes));
//		try {
//			Statement s = connection.getConnection().createStatement();
//			s.execute(sqlString);
//			s.close();
//		} catch (SQLException e) {
//			System.out.println("Delete failed with " + sqlString);
//			e.printStackTrace();
//		}
//	}

	/**
	 * Check suspect offline event times for an event data block. 
	 * @param offlineEventDataBlock
	 */
	public void checkSuspectEventTimes(OfflineEventDataBlock offlineEventDataBlock) {
		synchronized (offlineEventDataBlock.getSynchLock()) {
			ListIterator<OfflineEventDataUnit> li = offlineEventDataBlock.getListIterator(0);
			OfflineEventDataUnit event;
			while (li.hasNext()) {
				event = li.next();
				if (event.isSuspectEventTimes()) {
					checkSuspectEventTimes(event);
				}
			}
		}
	}
	/**
	 * Update suspect event times for a single event. 
	 * @param event
	 */
	private void checkSuspectEventTimes(OfflineEventDataUnit event) {
		String criteria = String.format("WHERE EventId = %d ORDER BY UTC, UTCMilliseconds", 
				event.getDatabaseIndex());
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		PamCursor pamCursor = dbControl.createPamCursor(getTableDefinition());
		ResultSet resultSet = pamCursor.openReadOnlyCursor(dbControl.getConnection(), criteria);
		long newStart = Long.MAX_VALUE, newEnd = 0;
		Integer millis;
		Object ts;
		SQLTypes sqlTypes = dbControl.getConnection().getSqlTypes();
		PamTableDefinition tableDef = (PamTableDefinition) getTableDefinition();
		try {
			while (resultSet.next()) {
				transferDataFromResult(sqlTypes, resultSet);
				ts = tableDef.getTimeStampItem().getValue();
				long m = SQLTypes.millisFromTimeStamp(ts); 
				if (m%1000 == 0) {
					millis = (Integer) tableDef.getTimeStampMillis().getValue();
					if (millis != null) {
						m += millis;
					}
				}
				newStart = Math.min(newStart, m);
				newEnd = Math.max(newEnd, m);
			}
//			if (resultSet.first()) {
//				transferDataFromResult(sqlTypes, resultSet);
//				ts = getTableDefinition().getTimeStampItem().getValue();
//				newStart = sqlTypes.millisFromTimeStamp(ts);
//				if (newStart%1000 == 0) {
//					millis = (Integer) getTableDefinition().getTimeStampMillis().getValue();
//					if (millis != null) {
//						newStart += millis;
//					}
//				}
//			}
//			if (resultSet.last()) {
//				transferDataFromResult(sqlTypes, resultSet);
//				ts = getTableDefinition().getTimeStampItem().getValue();
//				newEnd = sqlTypes.millisFromTimeStamp(ts);
//				if (newEnd%1000 == 0) {
//					millis = (Integer) getTableDefinition().getTimeStampMillis().getValue();
//					if (millis != null) {
//						newEnd += millis;
//					}
//				}
//			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (newStart != 0 && newEnd != 0) {
			event.setTimeMilliseconds(newStart);
			event.setEventEndTime(newEnd);
			event.setSuspectEventTimes(false);
		}
	}
//
//	/* (non-Javadoc)
//	 * @see generalDatabase.SQLLogging#logData(generalDatabase.PamConnection, PamguardMVC.PamDataUnit)
//	 */
//	@Override
//	public synchronized boolean logData(PamConnection con, PamDataUnit dataUnit) {
//		ClickDetection click = (ClickDetection) dataUnit;
//		System.out.println("Database write click " + click.getClickNumber());
//		return super.logData(con, dataUnit);
//	}
//
//	/* (non-Javadoc)
//	 * @see generalDatabase.SQLLogging#reLogData(generalDatabase.PamConnection, PamguardMVC.PamDataUnit)
//	 */
//	@Override
//	public synchronized boolean reLogData(PamConnection con, PamDataUnit dataUnit) {
//		ClickDetection click = (ClickDetection) dataUnit;
//		System.out.println("Database re-write click " + click.getClickNumber());
//		return super.reLogData(con, dataUnit);
//	}

}
