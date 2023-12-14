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

package GPS;

import generalDatabase.DBControlUnit;
import generalDatabase.DBProcess;
import generalDatabase.PamConnection;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import javax.swing.JOptionPane;







import PamController.PamViewParameters;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

public class GpsLogger extends SQLLogging {
	
	private PamTableDefinition tableDefinition;
	
	private int gpsDateCol;
	
	private ProcessNmeaData nmeaProcess;
	
	public static final String defaultDataTableName = "gpsData";
	
	private PamTableItem gpsDate, gpsTime, latitude, longitude, speed, 
	speedType, heading, headingType, gpsError, dataStatus, 
	trueHeading, magneticHeading, magneticVariation, fixType;

	private GPSControl gpsControl;
	
	
	
	public GpsLogger(PamDataBlock pamDataBlock) {
		
		super(pamDataBlock);
		
		try {
			nmeaProcess = (ProcessNmeaData) pamDataBlock.getParentProcess();
		}
		catch (ClassCastException e) {
			nmeaProcess = null;
		}
		String tableName = defaultDataTableName;
		if (nmeaProcess != null) {
			gpsControl = nmeaProcess.getGpsController();
			tableName = gpsControl.getDataTableName();
		}
		
		setCanView(true);
		
		tableDefinition = new PamTableDefinition(tableName, UPDATE_POLICY_OVERWRITE);
		gpsDateCol = tableDefinition.addTableItem(gpsDate = new PamTableItem("GpsDate", Types.TIMESTAMP));
//		tableDefinition.addTableItem(pcTime = new PamTableItem("PCTime", Types.TIMESTAMP));
		tableDefinition.addTableItem(gpsTime = new PamTableItem("GPSTime", Types.INTEGER));
		tableDefinition.addTableItem(latitude = new PamTableItem("Latitude", Types.DOUBLE));
		tableDefinition.addTableItem(longitude = new PamTableItem("Longitude", Types.DOUBLE));
		tableDefinition.addTableItem(speed = new PamTableItem("Speed", Types.DOUBLE));
		tableDefinition.addTableItem(speedType = new PamTableItem("SpeedType", Types.CHAR, 2));
		tableDefinition.addTableItem(heading = new PamTableItem("Heading", Types.DOUBLE));
		tableDefinition.addTableItem(headingType = new PamTableItem("HeadingType", Types.CHAR, 2));
		tableDefinition.addTableItem(trueHeading = new PamTableItem("TrueHeading", Types.DOUBLE));
		tableDefinition.addTableItem(magneticHeading = new PamTableItem("MagneticHeading", Types.DOUBLE));
		tableDefinition.addTableItem(magneticVariation = new PamTableItem("MagneticVariation", Types.DOUBLE));
		tableDefinition.addTableItem(gpsError = new PamTableItem("GPSError", Types.INTEGER));
		tableDefinition.addTableItem(dataStatus = new PamTableItem("DataStatus", Types.CHAR, 3));
		tableDefinition.addTableItem(fixType = new PamTableItem("FixType", Types.CHAR, 3));
		
		tableDefinition.setUseCheatIndexing(true);
		
		setTableDefinition(tableDefinition);
	}

	@Override
	public synchronized boolean doExtraChecks(DBProcess dbProcess, PamConnection connection) {
		if (dbProcess.tableExists(tableDefinition) == false) {
			return true; // PAMGUARD will create the table normally. 
		}
		// make sure the table has the right columns. 
		dbProcess.checkTable(tableDefinition);
		/*
		 *  the table exists - is it a Logger table ?
		 *  A logger table will have an Index column, but not
		 *  Id, UTC, pcLocaltime or updateOf column. 
		 */
		PamTableItem indexItem = new PamTableItem("Index", Types.INTEGER);
		PamTableItem pctimeItem = new PamTableItem("PCTime", Types.TIMESTAMP);
		indexItem.setPrimaryKey(true);
		indexItem.setCounter(true);
		boolean hasIndex = dbProcess.columnExists(tableDefinition, indexItem);
		boolean hasPCTime = dbProcess.columnExists(tableDefinition, pctimeItem);
//		boolean hasId = dbProcess.columnExists(tableDefinition, tableDefinition.getIndexItem());
		boolean hasUTC = dbProcess.columnNull(tableDefinition.getTableName(), tableDefinition.getTimeStampItem()) != 2;
//		boolean hasLoctime = dbProcess.columnExists(tableDefinition, tableDefinition.getLocalTimeItem());
		if (hasIndex && hasPCTime  && hasUTC == false) {
			// seems to be a logger table
			String warningText = String.format("The data in table %s appear to be from the IFAW\n" +
					"Logger 2000 software and are incompatible with PAMGUARD\n\n" +
					"Click Yes to convert the data to a PAMGUARD compatible format " +
					"or Canel to leave the table unchanged. \n\n" +
					"Converting the data may make the data incompatible with the Logger software",
					tableDefinition.getTableName());
			int ans = JOptionPane.showConfirmDialog(null, warningText, "GPS Data Format", 
					JOptionPane.OK_CANCEL_OPTION);
			if (ans == JOptionPane.CANCEL_OPTION) {
				JOptionPane.showMessageDialog(null, "You should exit PAMGUARD and either convert the\n" +
						"data yourself, or create a new database table");
			}
			else {
				return convertLoggerGpsTable(dbProcess, connection);
			}
			
		}		
		return true;
	}

	private boolean convertLoggerGpsTable(DBProcess dbProcess, PamConnection connection) {
		/**
		 * Jobs list is to 
		 * 1. Rename Index to Id
		 * 2. add columns for UTC and Local time
		 * 3. Copy the data from PCTime into UTC and Localtime
		 */
		
		
		String sqlStr;
		
		SQLTypes sqlTypes = connection.getSqlTypes();
		/*
		 * Can't rename, so create an Integer index column. 
		 * copy, fill and then try to swap over primary key
		 */
//		sqlStr = String.format("ALTER TABLE %s ADD COLUMN Id LONG", tableDefinition.getTableName());
//		if (!runStmt(connection, sqlStr)) {
//			return false;
//		} 
		int idColState = dbProcess.columnNull(tableDefinition.getTableName(), tableDefinition.getIndexItem());
		if (idColState == 2){ 
			DBControlUnit.findDatabaseControl().commitChanges();
			sqlStr = String.format("UPDATE %s SET %s = %s",
					tableDefinition.getTableName(), sqlTypes.formatColumnName("Id"), sqlTypes.formatColumnName("Index"));
			//		sqlStr = String.format("ALTER TABLE %s RENAME COLUMN \"Index\" to \"Id\"",
			//				tableDefinition.getTableName());
			if (!runStmt(connection, sqlStr)) {
				return false;
			}
			DBControlUnit.findDatabaseControl().commitChanges();
		}
		
		// now add the three other columns. 
//		System.out.println("Add standard gps data table time comumns");
//		dbProcess.checkColumn(tableDefinition, tableDefinition.getTimeStampItem());
//		dbProcess.checkColumn(tableDefinition, tableDefinition.getLocalTimeItem());
//		dbProcess.checkColumn(tableDefinition, gpsDate);
		String[] timeCols = new String[3];
		timeCols[0] = tableDefinition.getTimeStampItem().getName();
		timeCols[1] = tableDefinition.getLocalTimeItem().getName();
		timeCols[2] = gpsDate.getName();
		sqlStr = String.format("UPDATE %s SET ", tableDefinition.getTableName());
		for (int i = 0; i < 3; i++) {
			 sqlStr += String.format("%s=PCTime, ", timeCols[i]);
		}
		sqlStr += "UTCMilliseconds=0";
		if (!runStmt(connection, sqlStr)) {
			return false;
		}
		DBControlUnit.findDatabaseControl().commitChanges();
		// finally, convert the Lat, Long, Speed and Head columns from single to Double
//		System.out.println("Change column format for " + latitude.getName());
		dbProcess.changeColumnFormat(tableDefinition.getTableName(), latitude);
		DBControlUnit.findDatabaseControl().commitChanges();
//		System.out.println("Change column format for " + longitude.getName());
		dbProcess.changeColumnFormat(tableDefinition.getTableName(), longitude);
		DBControlUnit.findDatabaseControl().commitChanges();
//		System.out.println("Change column format for " + speed.getName());
		dbProcess.changeColumnFormat(tableDefinition.getTableName(), speed);
		DBControlUnit.findDatabaseControl().commitChanges();
//		System.out.println("Change column format for " + heading.getName());
		dbProcess.changeColumnFormat(tableDefinition.getTableName(), heading);
		DBControlUnit.findDatabaseControl().commitChanges();
		boolean hasHeading;
		hasHeading = dbProcess.columnExists(tableDefinition, magneticHeading);
		if (hasHeading) {
//			System.out.println("Change column format for " + magneticHeading.getName());
			dbProcess.changeColumnFormat(tableDefinition.getTableName(), magneticHeading);
		}
		hasHeading = dbProcess.columnExists(tableDefinition, trueHeading);
		if (hasHeading) {
//			System.out.println("Change column format for " + trueHeading.getName());
			dbProcess.changeColumnFormat(tableDefinition.getTableName(), trueHeading);
		}
		DBControlUnit.findDatabaseControl().commitChanges();
		
		return true;
	}
	private boolean runStmt(PamConnection con, String str) {
		Statement stmt;
		try {
			stmt = con.getConnection().createStatement();   
			int addResult;
//			addResult = 
					stmt.execute(str); 
		}
		catch (SQLException ex) {
			System.out.println(str);
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		
		GpsData gpsData = ((GpsDataUnit) pamDataUnit).getGpsData();
		int gpsTimeVal = gpsData.getHours()*10000000 + gpsData.getMins() * 100000 +
				gpsData.getSecs()*1000+gpsData.getMillis();
		
		gpsDate.setValue(sqlTypes.getTimeStamp(gpsData.getTimeInMillis()));
		gpsTime.setValue(gpsTimeVal);
		latitude.setValue(gpsData.getLatitude());
		longitude.setValue(gpsData.getLongitude());
		speed.setValue(gpsData.getSpeed());
		speedType.setValue(new String("N"));
		heading.setValue(gpsData.getCourseOverGround());
		headingType.setValue(new String("T"));
		trueHeading.setValue(gpsData.getTrueHeading());
		magneticHeading.setValue(gpsData.getMagneticHeading());
		magneticVariation.setValue(gpsData.getMagneticVariation());
		gpsError.setValue(0);
		dataStatus.setValue(new String("A"));
		fixType.setValue(gpsData.getFixType());
		
	}

//	long prevT;
//	int prevInd;
	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long dataTime, int iD) {

		double lat = (Double) latitude.getDoubleValue();
		double longi = (Double) longitude.getDoubleValue();
		double head = (Double) heading.getDoubleValue();
		double spd = (Double) speed.getDoubleValue();
		Double magHead, trueHead, magVar;
		magHead = (Double) magneticHeading.getDoubleValue();
		trueHead = (Double) trueHeading.getDoubleValue();
		magVar = (Double) magneticVariation.getDoubleValue();
		/*
		 * Get the datetime value back out of the database. The passed one is just the dataunit 
		 * time from the UTC column which is NOT the GpsData time which was the real GPS time. 
		 */
		int gpsIntTimeVal = gpsTime.getIntegerValue();
		PamTableDefinition pamTableDef = (PamTableDefinition) getTableDefinition();
		Object ts = pamTableDef.getTimeStampItem().getValue();
		long gpsDate = sqlTypes.millisFromTimeStamp(ts);
		if (gpsDate%1000 == 0) {
			// some databases may have stored the milliseconds, in which 
			// case this next bit is redundant. 
			gpsDate += gpsIntTimeVal%1000;
		}
		
		GpsData gpsData = new GpsData(lat, longi, spd, head, gpsDate, trueHead, magHead);
		gpsData.setMagneticVariation(magVar);
		
		
		GpsDataUnit gpsDataUnit = new GpsDataUnit(dataTime, gpsData);
		gpsDataUnit.setDatabaseIndex(iD);
		if (nmeaProcess == null || nmeaProcess.wantDataUnit(gpsDataUnit)) {
			((PamDataBlock<GpsDataUnit>) getPamDataBlock()).addPamData(gpsDataUnit);
		}
		gpsDataUnit.setChannelBitmap(-1);
		
		if (nmeaProcess != null) {
			// this should apply the rules of how much GPD data to keep as it's loading
			// can reduce amount of data loaded in viewer mode to something sensible. 
			if (nmeaProcess.wantDataUnit(gpsDataUnit) == false) {
				return null;
			};
		}
		
		String fixTypeStr = fixType.getDeblankedStringValue();
		gpsData.setFixType(fixTypeStr);
		
		
		return gpsDataUnit;
	}

	
	public boolean getCanLog() {
		return true;
	}

	/* (non-Javadoc)
	 * @see generalDatabase.SQLLogging#createViewResultSet(generalDatabase.PamConnection, PamController.PamViewParameters)
	 */
	@Override
	protected ResultSet createViewResultSet(PamConnection con,
			PamViewParameters pamViewParameters) {
		if (nmeaProcess != null) {
			// reset the last data unit in the nmea process so that we can run code to 
			// see if we want all the data units. 
			nmeaProcess.pamStart();
		}
		return super.createViewResultSet(con, pamViewParameters);
	}


}
