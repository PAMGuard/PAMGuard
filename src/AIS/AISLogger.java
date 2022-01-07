package AIS;

import generalDatabase.DBProcess;
import generalDatabase.PamConnection;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import javax.naming.ldap.HasControls;

import PamController.PamController;
import PamView.dialog.warn.WarnOnce;
import PamguardMVC.PamDataUnit;

public class AISLogger extends SQLLogging {

	private ProcessAISData aisProcess;
	private PamTableDefinition tableDef;
	private AISDataBlock aisDataBlock;
	protected PamTableItem mmsiNumber, imoNumber,callSign,shipName,shipType,	//messageID,
	eta,	draft,	destination,	//dataTerminalReady,	//dataIndicator,
	navigationStatus,	rateOfTurn,	speedOverGround,	//positionAccuracy,
	longitude,	latitude,	courseOverGround,	trueHeading;
	protected PamTableItem dataString;

	static private final int AISRAWLENGTH = 128;
	//utcSeconds,	//utcMinutes,	//utcHours,
	//repeatIndicator,
	//commsState;

	
	public AISLogger(AISDataBlock aisDataBlock, ProcessAISData aisProcess) {
		super(aisDataBlock);
		this.aisProcess = aisProcess;
		this.aisDataBlock = aisDataBlock;


		setCanView(true);

		PamTableItem tableItem;
		tableDef = new PamTableDefinition("AISData", UPDATE_POLICY_WRITENEW);
		tableDef.addTableItem(imoNumber = new PamTableItem("imoNumber", Types.INTEGER));
		tableDef.addTableItem(mmsiNumber = new PamTableItem("mmsiNumber", Types.INTEGER));
		tableDef.addTableItem(callSign = new PamTableItem("callSign", Types.CHAR, 7));
		tableDef.addTableItem(shipName = new PamTableItem("shipName", Types.CHAR, 20));
		tableDef.addTableItem(shipType = new PamTableItem("shipType", Types.CHAR, 20));
		tableDef.addTableItem(eta = new PamTableItem("eta", Types.TIMESTAMP));
		tableDef.addTableItem(draft = new PamTableItem("draft", Types.DOUBLE));
		tableDef.addTableItem(destination = new PamTableItem("destination", Types.CHAR, 20));
		tableDef.addTableItem(navigationStatus = new PamTableItem("navigationStatus", Types.INTEGER));
		tableDef.addTableItem(rateOfTurn = new PamTableItem("rateOfTurn", Types.DOUBLE));
		tableDef.addTableItem(speedOverGround = new PamTableItem("speedOverGround", Types.DOUBLE));
		tableDef.addTableItem(latitude = new PamTableItem("latitude", Types.DOUBLE));
		tableDef.addTableItem(longitude = new PamTableItem("longitude", Types.DOUBLE));
		tableDef.addTableItem(courseOverGround = new PamTableItem("courseOverGround", Types.DOUBLE));
		tableDef.addTableItem(trueHeading = new PamTableItem("trueHeading", Types.DOUBLE));
		tableDef.addTableItem(dataString = new PamTableItem("Data String", Types.CHAR, AISRAWLENGTH));
		tableDef.setUseCheatIndexing(true);

		setTableDefinition(tableDef);
	}

//	@Override
//	public PamTableDefinition getTableDefinition() {
//		return tableDef;
//	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {

		AISDataUnit aisData = (AISDataUnit) pamDataUnit;

		//		dateTime.setValue(PamCalendar.getTimeStamp(aisData.getTimeMilliseconds()));
		mmsiNumber.setValue(aisData.mmsiNumber);
		AISStaticData staticData = aisData.getStaticData();
		AISPositionReport positionReport = aisData.getPositionReport();
		if (staticData != null) {
			imoNumber.setValue(staticData.imoNumber);
			callSign.setValue(staticData.callSign);
			shipName.setValue(staticData.shipName);
			shipType.setValue(staticData.getStationTypeString(aisData.stationType, staticData.shipType));
			eta.setValue(sqlTypes.getTimeStamp(staticData.etaMilliseconds));
			draft.setValue(staticData.staticDraught);
			destination.setValue(staticData.destination);
		}
		else {
			imoNumber.setValue(null);
			callSign.setValue(null);
			shipName.setValue(null);
			shipType.setValue(aisData.getStationtypeString());
			eta.setValue(null);
			draft.setValue(null);
			destination.setValue(null);
		}
		if (positionReport != null) {
			navigationStatus.setValue(positionReport.navigationStatus);
			rateOfTurn.setValue(positionReport.rateOfTurn);
			speedOverGround.setValue(positionReport.speedOverGround);
			latitude.setValue(positionReport.getLatitude());
			longitude.setValue(positionReport.getLongitude());
			courseOverGround.setValue(positionReport.courseOverGround);
			trueHeading.setValue(positionReport.trueHeading);
		}
		else {
			navigationStatus.setValue(null);
			rateOfTurn.setValue(null);
			speedOverGround.setValue(null);
			latitude.setValue(null);
			longitude.setValue(null);
			courseOverGround.setValue(null);
			trueHeading.setValue(null);
		}
		dataString.setValue(aisData.charData);

	}

	private int lastMMSINumber = 0;
	private long lastTime = 0;
	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {
		AISStaticData aisStaticData = null;
		AISPositionReport aisPositionReport = null;

//		Timestamp ts = (Timestamp) tableDef.getTimeStampItem().getValue();
//		long t = PamCalendar.millisFromTimeStamp(ts);

		int ammsiNumber;
		// static data
		String acallSign = null;
		String ashipName;
		String ashipType;
		long aetaMillis;
		double adraught;
		String adestination;
		// position report
		int anavStatus;
		double arateOfTurn;
		double aspeedOverGround;
		double alatitude;
		double alongitude;
		double acourseOverGround; 
		double atrueHeading;
		String aisString;

		ammsiNumber = (Integer) mmsiNumber.getValue();

		/*
		 * Problem in some 2008 data where all records are repeated
		 * So check for identical consecutive records. 
		 */
		if (lastMMSINumber == ammsiNumber && lastTime == timeMilliseconds) {
			return null;
		}
		else {
			lastMMSINumber = ammsiNumber;
			lastTime = timeMilliseconds;
		}

		if (callSign.getValue() != null) {
			// assume all static data are OK
			acallSign = (String) callSign.getValue();
			ashipName = (String) shipName.getValue();
			ashipType = (String) shipType.getValue();
			aetaMillis = sqlTypes.millisFromTimeStamp(eta.getValue());
			//			aetaMillis = (Long) eta.getValue();
			adraught = (Double) draft.getValue();
			adestination = (String) destination.getValue();
			aisStaticData = new AISStaticData(acallSign, ashipName, 0,
					aetaMillis, adraught, adestination);
			Integer imoNum = imoNumber.getIntegerObject();
			if (imoNum != null) {
				aisStaticData.imoNumber = imoNum;
			}
		}
		if (navigationStatus.getValue() != null) {
			anavStatus = (Integer) navigationStatus.getValue();
			arateOfTurn = (Double) rateOfTurn.getValue();
			aspeedOverGround = (Double) speedOverGround.getValue();
			alatitude = (Double) latitude.getValue();
			alongitude = (Double) longitude.getValue();
			acourseOverGround = (Double) courseOverGround.getValue();
			atrueHeading = (Double) trueHeading.getValue();
			aisPositionReport = new AISPositionReport(lastTime, anavStatus, arateOfTurn,
					aspeedOverGround, alatitude, alongitude, acourseOverGround, atrueHeading);
			aisPositionReport.timeMilliseconds = timeMilliseconds;
		}

		AISDataUnit aisDataUnit = new AISDataUnit(timeMilliseconds);
		aisDataUnit.setDatabaseIndex(databaseIndex);
		aisDataUnit.mmsiNumber = ammsiNumber;
		if (aisStaticData != null) {
			aisDataUnit.setStaticData(aisStaticData);
		}
		if (aisPositionReport != null) {
			aisDataUnit.addPositionReport(aisPositionReport);
		}
		aisString = (String) dataString.getValue();
		if (aisString != null) {
			int nBytes = (int) Math.floor(aisString.length() * 6 / 8);
			int nBits = nBytes * 8;
			int fillBits = aisString.length() * 6 - nBits;
			aisDataUnit.charData = aisString.trim();
		}
		aisDataBlock.addAISData(aisDataUnit);
		return aisDataUnit;

	}

	@Override
	public boolean doExtraChecks(DBProcess dbProcess, PamConnection connection) {
		/*
		 * Swap around some column names if the table has an ""imoNumber"" number column, but not an mmmsi column.
		 * by the time this gets called the mmsi column will have been created, so need to see if it's empty and copy 
		 * the data over from the imo columns. 
		 */
//		dbProcess.columnExists(getTableDefinition(), tableItem)
		Integer nIMO = countColData(connection, "imoNumber");
		Integer nMMSI = countColData(connection, "mmsiNumber");
		if (nIMO > 0 && nMMSI == 0) {
			String msg = "Updating AIS tabe column names";
			WarnOnce.showWarning(PamController.getMainFrame(), "AIS Database", msg, WarnOnce.OK_OPTION);
		try {
			Statement stmt = connection.getConnection().createStatement();
			stmt.executeUpdate(String.format("UPDATE %s SET mmsiNumber = imoNumber WHERE imoNumber IS NOT NULL", getTableDefinition().getTableName()));
			stmt.close();
			stmt = connection.getConnection().createStatement();
			stmt.executeUpdate(String.format("UPDATE %s SET imoNumber = NULL", getTableDefinition().getTableName()));
			stmt.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		}
		
		return true;
	}
	
	private Integer countColData(PamConnection connection, String colName) {
		if (connection == null || connection.getConnection() == null) {
			return null;
		}
		Integer n = null;
		String qStr = String.format("SELECT COUNT(*) FROM %s WHERE %s IS NOT NULL", 
				getTableDefinition().getTableName(), connection.getSqlTypes().formatColumnName(colName));
//		System.out.println(qStr);
		try {
			Statement stmt = connection.getConnection().createStatement();
			ResultSet res = stmt.executeQuery(qStr);
//				res.next();
				n = res.getInt(1);
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return n;
	}

}
