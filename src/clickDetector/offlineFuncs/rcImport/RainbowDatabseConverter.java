package clickDetector.offlineFuncs.rcImport;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import binaryFileStorage.BinaryStore;
import PamController.OfflineDataStore;
import PamController.PamController;
import clickDetector.ClickControl;
import clickDetector.ClickDataBlock;
import clickDetector.offlineFuncs.OfflineEventDataUnit;
import clickDetector.offlineFuncs.OfflineEventLogging;
import generalDatabase.DBControlUnit;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.MSAccessSQLTypes;
import generalDatabase.MSAccessSystem;
import generalDatabase.PamConnection;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.TableUtilities;

/**
 * Does the work of converting a RainbowClick databse. 
 * @author Doug Gillespie
 *
 */
public class RainbowDatabseConverter {

	private RainbowEventsTableDef rcEventsTableDef = new RainbowEventsTableDef();
	private RainbowClicksTableDef rcClicksTableDef = new RainbowClicksTableDef();
	private String rainbowDatabseName;
	private PamConnection rcConnection = null;
	private ClickControl clickControl;
	private int[] eventLUT;
	private OfflineEventLogging eventLogging;
	private ClickImportLogging clickLogging;
	private PamConnection pamConnection;
	private DBConvertProgress dbConvertProgress;
	private int totalClicks;
	private int[] badEvents;
	private int totalEvents;
	
	public RainbowDatabseConverter(ClickControl clickControl, String rainbowDatabaseName) {
		super();
		this.clickControl = clickControl;
		this.rainbowDatabseName = rainbowDatabaseName;
		OfflineDataStore OfflineDataStore = PamController.getInstance().findOfflineDataStore(BinaryStore.class);
	}
	
	public boolean openRainbowConnection() {
		if (rainbowDatabseName == null) {
			return false;
		}
		if (rcConnection != null) {
			return true;
		}
		rainbowDatabseName = rainbowDatabseName.trim();
		Exception mdbException = null;
		Exception accdbException = null;
		rcConnection = null;
		if (rainbowDatabseName.endsWith(".mdb")) {
			try {
				rcConnection = MSAccessSystem.getMDBConnection(null, rainbowDatabseName);
			}
			catch (Exception e) {
				mdbException = e;
				e.printStackTrace();
				return false;
			}
		}
		if (rcConnection == null) {
			try {
				rcConnection = MSAccessSystem.getACCDBConnection(null, rainbowDatabseName);
			}
			catch (Exception e) {
				accdbException = e;
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
	public boolean checkRainbowTables() {
		if (!openRainbowConnection()) {
			return false;
		}
		boolean ok = checkTable(rcConnection, rcEventsTableDef);
		ok &= checkTable(rcConnection, rcClicksTableDef);
		return ok;
	}

	private boolean checkTable(PamConnection con, EmptyTableDefinition tableDef) {
		if (TableUtilities.tableExists(con, MSAccessSystem.SYSTEMNAME, tableDef) == false) {
			System.out.println(String.format("Table %s does not exist", tableDef.getTableName()));
			return false;
		}
		int errors = 0;
		PamTableItem tableItem;
		for (int i = 0; i < tableDef.getTableItemCount(); i++) {
			tableItem = tableDef.getTableItem(i);
			if (TableUtilities.columnExists(con, tableDef.getTableName(), tableItem.getName(), tableItem.getSqlType()) == false){
				errors++;
				System.out.println(String.format("Column %s in table %s does not exist", tableItem.getName(), tableDef.getTableName()));
			}
		}
		return (errors == 0);
	}
	

	public void closeConnection() {
		if (rcConnection == null || rcConnection.getConnection() == null) {
			return;
		}
		try {
			rcConnection.getConnection().close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		rcConnection = null;
	}

	/**
	 * @return the rcEventsTableDef
	 */
	public RainbowEventsTableDef getRcEventsTableDef() {
		return rcEventsTableDef;
	}

	/**
	 * @return the rcClicksTableDef
	 */
	public RainbowClicksTableDef getRcClicksTableDef() {
		return rcClicksTableDef;
	}

	public boolean checkPamguardTables() {
		/*
		 *  need to get hold of definitions for the two tables
		 *  Can get the click table from the logging class.  
		 */
		pamConnection = DBControlUnit.findConnection();
		if (pamConnection == null) {
			return false; 
		}
		
		ClickDataBlock clickDataBlock = clickControl.getClickDataBlock();
		clickLogging = new ClickImportLogging(clickControl);
		PamTableDefinition tableDef = clickLogging.getTableDefinition();
		boolean ok = checkTable(pamConnection, tableDef);
		if (!ok) {
			System.out.println("Error in Pamguard clicks table");
			return false;
		}
		
		eventLogging = new OfflineEventLogging(clickControl, clickControl.getClickDetector().getOfflineEventDataBlock());
		tableDef = eventLogging.getTableDefinition();
		ok = checkTable(pamConnection, tableDef);
		if (!ok) {
			System.out.println("Error in Pamguard events table");
			return false;
		}
		
		
		return true;
	}

	/**
	 * Read events from the rainbow click database and write them 
	 * to the PAMguard database. Note that event indexes may 
	 * change during this process, so will need to keep a 
	 * lookup table of event numbers which can be used to 
	 * correct event cross reference numbers when the clicks are read in
	 * <p>
	 * Not attempting to do this in the most elegant way, since there isn't a lot of point
	 * since RC will not develop or change.  
	 */
	public boolean importEvents() {
		eventLUT = new int[0];
		totalClicks = 0;
		String selStr = rcEventsTableDef.getSQLSelectString(new MSAccessSQLTypes()) + " ORDER BY StartDate";
		totalEvents = 0;
		String progress;
		try {
			Statement stmt = rcConnection.getConnection().createStatement();
			ResultSet resultSet = stmt.executeQuery(selStr);
			while (resultSet.next()) {
				rcEventsTableDef.unpackResultSet(resultSet);
				convertEvent();
				totalEvents++;
				if (dbConvertProgress != null) {
					progress = String.format("Imported %d events from RainbowClick database", totalEvents);
					dbConvertProgress.setProgress(ConversionProgress.STATE_IMPORTEVENTS, totalEvents, totalClicks, 0);
				}
				
			}
			resultSet.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		System.out.println("Rows read from RC events table = " + totalEvents);
		return true;
	}

	/**
	 * The rcEventsTableDef will now contain all the data read in from 
	 * one row of the RC events table. Unpack it and 
	 * write to the events table. 
	 */
	private void convertEvent() {
		long millis = rcEventsTableDef.getStartTime();
		Integer colour = rcEventsTableDef.getColour();
		String evType = rcEventsTableDef.getSecies();
		OfflineEventDataUnit eventDataUnit = new OfflineEventDataUnit(evType, colour, null);
		eventDataUnit.setTimeMilliseconds(millis);
		Integer nClicks = rcEventsTableDef.getNClicks();
		Short min = rcEventsTableDef.getMinNumber();
		Short best = rcEventsTableDef.getBestNumber();
		Short max = rcEventsTableDef.getMaxNumber();
//		if (nClicks != null) eventDataUnit.setNClicks(nClicks);
		eventDataUnit.setMinNumber(min);
		eventDataUnit.setBestNumber(best);
		eventDataUnit.setMaxNumber(max);
		Double duration = rcEventsTableDef.getDuration();
		if (duration != null) eventDataUnit.setSampleDuration((long) (duration * 1000));
		eventDataUnit.setComment(rcEventsTableDef.getComment());
		eventDataUnit.setChannelBitmap(3);
		eventLogging.logData(pamConnection, eventDataUnit);
		
		// data unit should have been given an index when it was logged. 
		// use that to make a LUT to convert event id's from the clicks table when we read that. 
		int newInd = eventDataUnit.getDatabaseIndex();
		int oldInd = rcEventsTableDef.getIndex();
		if (eventLUT.length <= oldInd) {
			eventLUT = Arrays.copyOf(eventLUT, oldInd+1);
		}
		eventLUT[oldInd] = newInd; 
		
		totalClicks += nClicks;
	}

	public boolean importClicks(RainbowFileMap rainbowFileMap) {
		badEvents = new int[eventLUT.length];
		String selStr = rcClicksTableDef.getSQLSelectString(new MSAccessSQLTypes()) + " ORDER BY EventId, DateNum";
		int rowsRead = 0;
		int reportStep = Math.max(1, totalClicks/200);
		String progress;
		boolean cancelled = false;
		int percentProgress = 0;
		try {
			Statement stmt = rcConnection.getConnection().createStatement();
			ResultSet resultSet = stmt.executeQuery(selStr);
			while (resultSet.next()) {
				rcClicksTableDef.unpackResultSet(resultSet);
				convertClick(rainbowFileMap);
				rowsRead++;
				if (dbConvertProgress != null) { 
					if (dbConvertProgress.stop()) {
						percentProgress = rowsRead * 100 / totalClicks;
						progress = String.format("Import CANCELLED %d of %d clicks (%d%%)", rowsRead, totalClicks, percentProgress);
						dbConvertProgress.setProgress(ConversionProgress.STATE_IDLE, totalEvents, totalClicks, rowsRead);
						cancelled = true;
						break;
					}
					if (rowsRead%reportStep == 0) {
						percentProgress = rowsRead * 100 / totalClicks;
						progress = String.format("Imported %d of %d clicks (%d%%)", rowsRead, totalClicks, percentProgress);
						dbConvertProgress.setProgress(ConversionProgress.STATE_IMPORTCLICKS, totalEvents, totalClicks, rowsRead);
					}
				}
			}
			resultSet.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		if (dbConvertProgress != null && rowsRead%reportStep == 0) {
			percentProgress = 100;
			dbConvertProgress.setProgress(ConversionProgress.STATE_DONECLICKS, totalEvents, totalClicks, rowsRead);
		}
		if (!cancelled) {
			System.out.println("Rows read from RC clicks table = " + rowsRead);
		}
		return true;
		
	}

	private void convertClick(RainbowFileMap rainbowFileMap) {
		long millis = rcClicksTableDef.getMillis();
		int eventId = rcClicksTableDef.getEventId();
		int pamEventId = eventLUT[eventId];
		String oldFile = rcClicksTableDef.getFile();
		int rainbowSection = rcClicksTableDef.getSection();
		int clickNo = rcClicksTableDef.getClickNumber();
		float amplitude = rcClicksTableDef.getAmplitude();
		/*
		 * Need to work out the new file name. How the F....
		 * Seems the only way is to look at the data map and see whats
		 * there. 
		 * 
		 */
		String pamFile = rainbowFileMap.getPamFileName(oldFile, rainbowSection-1);
		if (pamFile == null) {
			System.out.println(String.format("Unable to find PAMguard file for click %d in section %d of file %s",
					clickNo, rainbowSection, oldFile));
			badEvents[eventId] ++;
		}
		
		DummyClick click = new DummyClick(millis, pamEventId, clickNo-1, pamFile, amplitude);
		clickLogging.logData(pamConnection, click);
//		click.set
		
	}

	/**
	 * @param dbConvertProgress the dbConvertProgress to set
	 */
	public void setDbConvertProgress(DBConvertProgress dbConvertProgress) {
		this.dbConvertProgress = dbConvertProgress;
	}

	
}
