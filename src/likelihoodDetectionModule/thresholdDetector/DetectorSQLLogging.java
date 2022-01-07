package likelihoodDetectionModule.thresholdDetector;

import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

import java.sql.Types;

import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import likelihoodDetectionModule.LikelihoodDetectionUnit;

/**
 *  This class is responsible for writing ThresholdDetectorDataUnits to the database (if configured).  This code was mostly
 *  borrowed from the workshop demo, with some custom fields added. 
 *  
 * @author Dave Flogeras
 *
 */
public class DetectorSQLLogging extends SQLLogging {

	LikelihoodDetectionUnit likelihoodController;
	
	PamTableDefinition tableDefinition;
	ThresholdDetectorProcess thresholdDetectorProcess;
	
	PamTableItem dateItem, durationItem, lowFreqItem, highFreqItem, energyPeakItem, peakTimeItem, channelItem;
	PamTableItem detectionType;
	
	public DetectorSQLLogging( LikelihoodDetectionUnit likelihoodController,
							   ThresholdDetectorProcess thresholdDetectorProcess,
							   PamDataBlock pamDataBlock) {
		// call the super constructor. 
		super(pamDataBlock);
		
		// hold a reference to the Controller. 
		this.likelihoodController = likelihoodController;
	
		this.thresholdDetectorProcess = thresholdDetectorProcess;
		
		// create the table definition. 
		tableDefinition = createTableDefinition();
	}
	
	public PamTableDefinition createTableDefinition() {
		PamTableDefinition tableDef = new PamTableDefinition(likelihoodController.getUnitName(), UPDATE_POLICY_WRITENEW);
		
		PamTableItem tableItem;
		// add table items. 
//		PamTableItem dateItem, durationItem, lowFreqItem, highFreqItem, energyItem;
		
		tableDef.addTableItem(dateItem = new PamTableItem("SystemDate", Types.TIMESTAMP));
		tableDef.addTableItem(channelItem = new PamTableItem("Channel", Types.INTEGER));
		tableDef.addTableItem(durationItem = new PamTableItem("Duration", Types.DOUBLE));
		tableDef.addTableItem(lowFreqItem = new PamTableItem("lowFrequency", Types.DOUBLE));
		tableDef.addTableItem(highFreqItem = new PamTableItem("highFrequency", Types.DOUBLE));
		tableDef.addTableItem(energyPeakItem = new PamTableItem("peakEnergyDB", Types.DOUBLE));
		tableDef.addTableItem(peakTimeItem = new PamTableItem("peakTime", Types.DOUBLE ));
		// 100 characters should be enough
		tableDef.addTableItem(detectionType = new PamTableItem("detectionType", Types.CHAR, 100 ));


		setTableDefinition(tableDef);
		
		return tableDef;
	}

//	@Override
//	/**
//	 * This information will get used to automatically create an appropriate database
//	 * table and to generate SQL fetch and insert statements. 
//	 */
//	public PamTableDefinition getTableDefinition() {
//		/*
//		 * return the single instance of tableDefinition that was created in 
//		 * the constructor. This gets called quite often and we don't want 
//		 * to be creating a ne one every time. 
//		 */
//		return tableDefinition;
//	}

	@Override
	/*
	 * This gets called back from the database manager whenever a new dataunit is
	 * added to the datablock. All we have to do is set the data values for each 
	 * field and they will be inserted into the database. 
	 * If formats are incorrect, the SQL write statement is likely to fail !
	 */
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
//		PamTableItem dateItem, durationItem, lowFreqItem, highFreqItem, energyItem;

		ThresholdDetectorDataUnit wdu = (ThresholdDetectorDataUnit) pamDataUnit;
		channelItem.setValue(PamUtils.getSingleChannel(wdu.getChannelBitmap()));
		dateItem.setValue(sqlTypes.getTimeStamp(wdu.getTimeMilliseconds()));
		durationItem.setValue((double) wdu.getSampleDuration() / thresholdDetectorProcess.getSampleRate());
		lowFreqItem.setValue(wdu.getFrequency()[0]);
		highFreqItem.setValue(wdu.getFrequency()[1]);
		energyPeakItem.setValue(wdu.getAmplitudeDB());
		peakTimeItem.setValue( wdu.getPeakTime() );
		this.detectionType.setValue( wdu.getDetectionType() );
		
		
	}

}
