package whistleDetector;

import generalDatabase.PamConnection;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

import java.sql.Connection;
import java.sql.Types;

import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

public class WhistleEventLogger extends SQLLogging {

	private WhistleEventDetector whistleEventDetector;
	
	private PamConnection connection;
	
	private PamTableDefinition tableDef;
	
	private PamTableItem startTime, endTime, duration, nWhistles;
	
	public WhistleEventLogger(WhistleEventDetector whistleEventDetector, PamDataBlock pamDataBlock) {
		super(pamDataBlock);
		this.whistleEventDetector = whistleEventDetector;
		tableDef = new PamTableDefinition(whistleEventDetector.whistleControl.getUnitName() + "_Events", UPDATE_POLICY_WRITENEW);
		tableDef.addTableItem(startTime = new PamTableItem("EventStart", Types.TIMESTAMP));
		tableDef.addTableItem(endTime = new PamTableItem("EventEnd", Types.TIMESTAMP));
		tableDef.addTableItem(duration = new PamTableItem("Duration", Types.DOUBLE));
		tableDef.addTableItem(nWhistles = new PamTableItem("nWhistles", Types.INTEGER));
		tableDef.setUseCheatIndexing(true);

		setTableDefinition(tableDef);
	}

//	@Override
//	public PamTableDefinition getTableDefinition() {
//		return tableDef;
//	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {

		EventDataUnit whistleEvent = (EventDataUnit) pamDataUnit;
		startTime.setValue(sqlTypes.getTimeStamp(whistleEvent.startTimeMillis));
		endTime.setValue(sqlTypes.getTimeStamp(whistleEvent.endTimeMillis));
		duration.setValue((whistleEvent.endTimeMillis - whistleEvent.startTimeMillis)/1000.);
		nWhistles.setValue(whistleEvent.getWhistleCount());

	}

	@Override
	public synchronized boolean logData(PamConnection con, PamDataUnit dataUnit) {
		connection = con;
		return true;
	}
	public synchronized boolean logData(PamDataUnit dataUnit) {
		return super.logData(connection, dataUnit);
	}

}
