package clickDetector;

import generalDatabase.PamConnection;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

import java.sql.Connection;
import java.sql.Types;

import PamguardMVC.PamDataUnit;

public class ClickTrainLogger extends SQLLogging {


	ClickGroupDataBlock clickTrainDataBlock;
	
	ClickControl clickControl;
	
	ClickTrainDetector clickTrainDetector;
	
	private PamConnection con;
	
	PamTableDefinition tableDef;
	
	PamTableItem channels, numClicks, eRange, eT0, eventStart, eventEnd, currentSpeed, runningICI, lastClickTime, firstClickAngle,
	lastClickAngle;
	
	public ClickTrainLogger(ClickGroupDataBlock clickTrainDataBlock, ClickControl clickControl, ClickTrainDetector clickTrainDetector) {
		super(clickTrainDataBlock);
		this.clickTrainDataBlock = clickTrainDataBlock;
		this.clickControl = clickControl;
		this.clickTrainDetector = clickTrainDetector;
		/*
		 * 
	double eRange; // estimated perpendicular distance. 
	double eT0; // estimated time abeam in milliseconds
	long eventStart; // event start in milliseconds. 
	long eventEnd; // event start in milliseconds. 
	double currentSpeed = 5; // do it all in metres per second.
	double runningICI = -1;
	long lastClickTime;
	double firstClickAngle, lastFittedAngle;
	
	PamTableItem numClicks, eRange, eT0, eventStart, eventEnd, currentSpeed, runningICI, lastClickTime, firstClickAngle,
	lastClickAngle;
		 */
		
		tableDef = new PamTableDefinition(clickControl.getUnitName() + "_Trains", SQLLogging.UPDATE_POLICY_OVERWRITE);
		tableDef.addTableItem(channels = new PamTableItem("channels", Types.INTEGER));
		tableDef.addTableItem(numClicks = new PamTableItem("numClicks", Types.INTEGER));
		tableDef.addTableItem(eRange = new PamTableItem("eRange", Types.DOUBLE));
		tableDef.addTableItem(eT0 = new PamTableItem("eT0", Types.DOUBLE));
		tableDef.addTableItem(eventStart = new PamTableItem("eventStart", Types.TIMESTAMP));
		tableDef.addTableItem(eventEnd = new PamTableItem("eventEnd", Types.TIMESTAMP));
		tableDef.addTableItem(currentSpeed = new PamTableItem("currentSpeed", Types.DOUBLE));
		tableDef.addTableItem(runningICI = new PamTableItem("runningICI", Types.DOUBLE));
		tableDef.addTableItem(lastClickTime = new PamTableItem("lastClickTime", Types.INTEGER));
		tableDef.addTableItem(firstClickAngle = new PamTableItem("firstClickAngle", Types.DOUBLE));
		tableDef.addTableItem(lastClickAngle = new PamTableItem("lastClickAngle", Types.DOUBLE));
		setTableDefinition(tableDef);
	}
//
//	@Override
//	public PamTableDefinition getTableDefinition() {
//		// TODO Auto-generated method stub
//		return tableDef;
//	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {

		ClickTrainDetection clickTrain = (ClickTrainDetection) pamDataUnit;
		
		numClicks.setValue(catchBadInt(clickTrain.getSubDetectionsCount()));
//		eRange.setValue(catchBadDouble(clickTrain.eRange));
//		eT0.setValue(catchBadDouble(clickTrain.eT0));
//		eventStart.setValue(PamCalendar.getTimeStamp(catchBadLong(clickTrain.eventStart)));
//		eventEnd.setValue(PamCalendar.getTimeStamp(catchBadLong(clickTrain.eventEnd)));
//		currentSpeed.setValue(catchBadDouble(clickTrain.currentSpeed));
		runningICI.setValue(catchBadDouble(clickTrain.runningICI));
		lastClickTime.setValue((int) catchBadLong(clickTrain.lastClickTime));
		firstClickAngle.setValue(catchBadDouble(clickTrain.firstClickAngle));
		lastClickAngle.setValue(catchBadDouble(clickTrain.lastFittedAngle));
		//TODO I added this line in below - hope it's correct - DJM
		channels.setValue(catchBadInt(clickTrain.getChannelBitmap()));


	}

	@Override
	public synchronized boolean logData(PamConnection con, PamDataUnit dataUnit) {
		// this will get called when there is a new data unit, but don't do anything 
		// except remember the con.
		this.con = con;
		return true;
		// TODO Auto-generated method stub
//		return super.logData(con, dataUnit);
	}
	
	/*
	 * This gets called instead of logData when the click train has been closed. 
	 * Ideally we'd update an existing record, but that is too complicated for now. 
	 */
	public synchronized boolean logDataNow(PamDataUnit dataUnit) {
		// TODO Auto-generated method stub
		return super.logData(con, dataUnit);
	}

	private double catchBadDouble(double val){
		if(val== Double.NaN
			|| val == Double.POSITIVE_INFINITY
			|| val == Double.NEGATIVE_INFINITY){
			return 0.0;
		}
		return val;
	}
	private int catchBadInt(int val){
		if(val== Double.NaN
			|| val == Double.POSITIVE_INFINITY
			|| val == Double.NEGATIVE_INFINITY){
			return 0;
		}
		return val;
	}
	private long catchBadLong(long val){
		if(val== Double.NaN
			|| val == Double.POSITIVE_INFINITY
			|| val == Double.NEGATIVE_INFINITY){
			return 0;
		}
		return val;
	}

	
}
