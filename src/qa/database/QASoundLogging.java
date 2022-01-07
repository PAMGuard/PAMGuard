package qa.database;

import java.sql.Types;
import java.util.ArrayList;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.BufferedSQLLogging;
import generalDatabase.DBControlUnit;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamConnection;
import generalDatabase.PamSubtableDefinition;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;
import qa.QAControl;
import qa.QASoundDataBlock;
import qa.QASoundDataUnit;
import qa.generator.QASound;
import qa.generator.sequence.SequenceData;

/**
 * SIDE logging class is going to have to do something a bit fancy to work out which sounds have
 * been detected and by which detectors. Since these may include manually marking on displays, 
 * it's possible that detection data may only be available a long time after a sound has been 
 * generated. Will have to do something fancy such as keep a sound in a memory buffer for a fair old
 * while, then log it anyway and be prepared to update it if another detector updates it. 
 * @author dg50
 *
 */
public class QASoundLogging extends BufferedSQLLogging {
	
	private PamTableDefinition sideTable;
	private PamTableItem sequencePos, durationSeconds, genAmplitudeDB, rxAmplitudeDB;
	private PamTableItem minFreq, maxFreq;
	private PamTableItem minHydRange, minGunRange;
//	private PamTableItem latitude, longitude, depth;
	private QASoundDataBlock qaGeneratorDataBlock;
	private QAControl qaControl;
	private ArrayList<PamDataBlock> detectorList;
	private ArrayList<QADetectionItem> detectorItems = new ArrayList<>();
	private PamTableItem signalType;

	public QASoundLogging(QAControl qaControl, QASoundDataBlock qaGeneratorDataBlock) {
		super(qaGeneratorDataBlock, 5);
		this.qaControl = qaControl;
		this.qaGeneratorDataBlock = qaGeneratorDataBlock;

		signalType = new PamTableItem("Signal", Types.CHAR, 20);
		sequencePos = new PamTableItem("Sequence Pos", Types.INTEGER);
		durationSeconds = new PamTableItem("Duration_s", Types.REAL);
		genAmplitudeDB = new PamTableItem("Gen Amplitude", Types.REAL);
		rxAmplitudeDB = new PamTableItem("Rx Amplitude", Types.REAL);
		minFreq = new PamTableItem("Min Frequency", Types.REAL);
		maxFreq = new PamTableItem("Max Frequency", Types.REAL);
		minHydRange = new PamTableItem("Hydrophone Distance", Types.REAL);
		minGunRange = new PamTableItem("Airgun Distance", Types.REAL);
//		latitude = new PamTableItem("Latitude", Types.REAL);
//		longitude = new PamTableItem("Longitude", Types.REAL);
//		depth = new PamTableItem("Depth", Types.REAL);
				
		sideTable = createBaseTable();
		
		setTableDefinition(sideTable);
		
		setUpdatePolicy(UPDATE_POLICY_OVERWRITE);
	}
	
	/**
	 * Create a base table - will have to add a load of stuff to this to record output 
	 * of every possible detector. 
	 * @return base table to record sound gen information. 
	 */
	private PamTableDefinition createBaseTable() {
		PamTableDefinition tableDef = new PamSubtableDefinition("SIDE Sounds");
		tableDef.addTableItem(signalType);
		tableDef.addTableItem(sequencePos);
		tableDef.addTableItem(durationSeconds);
		tableDef.addTableItem(genAmplitudeDB);
		tableDef.addTableItem(rxAmplitudeDB);
		tableDef.addTableItem(minFreq);
		tableDef.addTableItem(maxFreq);
		tableDef.addTableItem(minHydRange);
		tableDef.addTableItem(minGunRange);
//		tableDef.addTableItem(depth);
		return tableDef;
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		QASoundDataUnit qaDataUnit = (QASoundDataUnit) pamDataUnit;
		QASound qaSound = qaDataUnit.getStandardSound();
		SequenceData seqData = qaDataUnit.getSequenceData();
		sequencePos.setValue(qaDataUnit.getSequenceData().getSequencePosition()+1);
		signalType.setValue(qaSound.getSignalType());
		durationSeconds.setValue((float) ((qaDataUnit.getArrivalEndMillis()-qaDataUnit.getArrivalStartMillis()) / 1000.));
		genAmplitudeDB.setValue((float) seqData.getAmplitude()) ; 
		rxAmplitudeDB.setValue((float) qaDataUnit.getReceivedLevel());
		double[] freqRange = qaSound.getFrequencyRange();
		if (freqRange == null) {
			minFreq.setValue(null);
			maxFreq.setValue(null);
		}
		else {
			float miF = (float) freqRange[0];
			float maF = (float) freqRange[1];
			minFreq.setValue(miF);
			maxFreq.setValue(maF);
		}
		for (QADetectionItem detItem:detectorItems) {
			long isDetected = qaDataUnit.getDetectorHit(detItem.dataBlock);
			detItem.setValue(isDetected);
		}
		minHydRange.setValue(sqlTypes.makeFloat(qaDataUnit.getDistanceToHydrophone()));
		minGunRange.setValue(sqlTypes.makeFloat(qaDataUnit.getDistanceToAirgun()));
		
	}

	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {
		int seqPos = sequencePos.getIntegerValue();
		double durS = durationSeconds.getFloatValue();
		double amp = genAmplitudeDB.getFloatValue();
		double rxAmp = rxAmplitudeDB.getFloatValue();
		SequenceData seqData = new SequenceData(null, seqPos, 0, amp);
		double f1 = minFreq.getFloatValue();
		double f2 = maxFreq.getFloatValue();
		double[] freqRange = null;
		if (!Double.isNaN(f1)) {
			freqRange = new double[2];
			freqRange[0] = f1;
			freqRange[1] = f2;
		}
		QASoundDataUnit qaDataUnit = new QASoundDataUnit(null, seqData, timeMilliseconds, 0, 0, null, rxAmp);
		qaDataUnit.setFrequency(freqRange);
		for (QADetectionItem detItem:detectorItems) {
			Integer det = detItem.getIntegerObject();
			if (det != null && det != 0) {
				qaDataUnit.setDetectorHit(detItem.dataBlock, det);
			}
		}
		qaDataUnit.setDistanceToAirgun(sqlTypes.makeDouble(minGunRange.getValue()));
		qaDataUnit.setDistanceToHydrophone(sqlTypes.makeDouble(minHydRange.getValue()));
		return qaDataUnit;
	}

	/**
	 * Check that all detectors have got a column in the database. 
	 * @param allDetectors
	 */
	public void checkDetectorList(ArrayList<PamDataBlock> allDetectors) {
		PamTableDefinition tableDef = createBaseTable();
		this.detectorList = allDetectors;
		detectorItems.clear();
		for (int i = 0; i < allDetectors.size(); i++) {
			QADetectionItem qaItem = new QADetectionItem(allDetectors.get(i));
			detectorItems.add(qaItem);
			tableDef.addTableItem(qaItem);
		}
		setTableDefinition(tableDef);
		// now find the database control and check the table. 
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		if (dbControl != null) {
			dbControl.getDbProcess().checkTable(this);
		}
	}
	@Override
	public synchronized boolean logData(PamConnection con, PamDataUnit dataUnit, PamDataUnit superDetection) {
		if (superDetection == null) {
			return true;
		}
		else {
			return super.logData(con, dataUnit, superDetection);
		}
	}
	
	private class QADetectionItem extends PamTableItem {

		private PamDataBlock dataBlock;

		public QADetectionItem(PamDataBlock dataBlock) {
			super("", Types.INTEGER);
			this.setName(EmptyTableDefinition.deblankString(getDBCompatibleString(dataBlock.getLongDataName())));
			this.dataBlock = dataBlock;
		}
	}
	
	/**
	 * Long data names tend to have commas in them. Get rid of them. 
	 * @param string
	 * @return
	 */
	private String getDBCompatibleString(String string) {
		return string.replace(",", "");
	}

}
