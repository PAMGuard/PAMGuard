package qa.database;

import java.sql.Types;

import PamUtils.LatLong;
import PamUtils.LatLongDatabaseSet;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SubdetectionInfo;
import generalDatabase.PamConnection;
import generalDatabase.PamSubtableDefinition;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;
import generalDatabase.SuperDetLogging;
import qa.QASequenceDataBlock;
import qa.QASequenceDataUnit;
import qa.QATestDataUnit;
import qa.generator.sequence.SoundSequence;
import qa.generator.testset.QATestSet;

// did extend QASubTableLogging
public class QASequenceLogging extends SuperDetLogging {

//	private PamTableItem genLat, genLong, genDepth, rxLat, rxLong, rxDepth;
	private LatLongDatabaseSet genLatLong, rxLatLong;
	private PamTableItem signalType, nSounds;
	private PamTableItem minHydRange, minGunRange;
	private QASequenceDataBlock sequenceDataBlock;

	public QASequenceLogging(QASequenceDataBlock sequenceDataBlock) {
		super(sequenceDataBlock, true);
		this.sequenceDataBlock = sequenceDataBlock;
		genLatLong = new LatLongDatabaseSet("Gen ", LatLongDatabaseSet.VERTICAL_DEPTH, false, false);
		rxLatLong = new LatLongDatabaseSet("Rx ", LatLongDatabaseSet.VERTICAL_DEPTH, false, false);
		minHydRange = new PamTableItem("Hydrophone Distance", Types.REAL);
		minGunRange = new PamTableItem("Airgun Distance", Types.REAL);
		signalType = new PamTableItem("Signal", Types.CHAR, 20);
		nSounds = new PamTableItem("N Sounds", Types.INTEGER);
		PamTableDefinition tableDef = new PamSubtableDefinition("SIDE Sequences");
		this.setUpdatePolicy(UPDATE_POLICY_OVERWRITE);
		tableDef.addTableItem(signalType);
		tableDef.addTableItem(nSounds);
		genLatLong.addTableItems(tableDef);
		rxLatLong.addTableItems(tableDef);
		tableDef.addTableItem(minHydRange);
		tableDef.addTableItem(minGunRange);
		setTableDefinition(tableDef);
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		QASequenceDataUnit seqUnit = (QASequenceDataUnit) pamDataUnit;
		SoundSequence soundSequence = seqUnit.getSoundSequence();
		QATestSet testSet = soundSequence.getQaTestSet();
		genLatLong.setLatLongData(soundSequence.getSourceLocation());
		rxLatLong.setLatLongData(soundSequence.getRxLocation());
		minHydRange.setValue(sqlTypes.makeFloat(seqUnit.getDistanceToHydrophone()));
		minGunRange.setValue(sqlTypes.makeFloat(seqUnit.getDistanceToAirgun()));
		signalType.setValue(testSet.getQaCluster().getSoundGenerator().getName());
		nSounds.setValue(seqUnit.getSubDetectionsCount());
	}

	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {
		LatLong gLL = genLatLong.getLatLongData(sqlTypes);
		LatLong rxLL = rxLatLong.getLatLongData(sqlTypes);
		SoundSequence soundSequence = new SoundSequence(null, gLL, rxLL, null);
		QASequenceDataUnit seqUnit = new QASequenceDataUnit(timeMilliseconds, soundSequence);
		seqUnit.setDistanceToAirgun(sqlTypes.makeDouble(minGunRange.getValue()));
		seqUnit.setDistanceToHydrophone(sqlTypes.makeDouble(minHydRange.getValue()));
		return seqUnit;
	}


//	/* (non-Javadoc)
//	 * @see generalDatabase.SQLLogging#logData(generalDatabase.PamConnection, PamguardMVC.PamDataUnit, PamguardMVC.PamDataUnit)
//	 */
//	@Override
//	public synchronized boolean logData(PamConnection con, PamDataUnit dataUnit, PamDataUnit superDetection) {
//		if (superDetection != null) {
//			return super.logData(con, dataUnit, superDetection);
//		}
//		superDetection = dataUnit.getSuperDetection(QATestDataUnit.class);
//		boolean ans = super.logData(con, dataUnit, superDetection);
//		if (superDetection != null) {
//			int superSub = superDetection.findSubdetectionInfo(dataUnit);
//			if (superSub >= 0) {
//				SubdetectionInfo superSubInfo = superDetection.getSubdetectionInfo(superSub);
//				if (superSubInfo != null) {
//					superSubInfo.setDbIndex(dataUnit.getDatabaseIndex());
//				}
//			}
//		}
//	}
	

}
