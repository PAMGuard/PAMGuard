package qa.database;

import java.sql.Types;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetDataBlock;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;
import generalDatabase.SuperDetLogging;
import qa.QAControl;
import qa.QATestDataUnit;
import qa.generator.clusters.QACluster;
import qa.generator.testset.QATestSet;
import qa.generator.testset.ViewerTestSet;

public class QATestLogging extends SuperDetLogging {

	private PamTableItem testType, testEnd, signalType, nSequences, version, status;
	private QAControl qaControl;
	public QATestLogging(QAControl qaControl, SuperDetDataBlock pamDataBlock) {
		super(pamDataBlock, true);
		this.qaControl = qaControl;
		PamTableDefinition tableDef = new PamTableDefinition("SIDE Tests", UPDATE_POLICY_OVERWRITE);
		this.setUpdatePolicy(UPDATE_POLICY_OVERWRITE);
		testType = new PamTableItem("Test Type", Types.CHAR, 20);
		testEnd = new PamTableItem("End Time", Types.TIMESTAMP);
		signalType = new PamTableItem("Signal", Types.CHAR, 20);
		nSequences = new PamTableItem("N Sequences", Types.INTEGER);
		status = new PamTableItem("Status", Types.CHAR, 20);
		version = new PamTableItem("Version", Types.CHAR, 10);
		tableDef.addTableItem(signalType);
		tableDef.addTableItem(testType);
		tableDef.addTableItem(version);
		tableDef.addTableItem(status);
		tableDef.addTableItem(testEnd);
		tableDef.addTableItem(nSequences);
		setTableDefinition(tableDef);
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		QATestDataUnit testUnit = (QATestDataUnit) pamDataUnit;
		QATestSet testSet = testUnit.getQaTestSet();
		testType.setValue(testUnit.getTestType());
		signalType.setValue(testSet.getTestName());
		status.setValue(testSet.getStatus());
		Long end = testSet.getEndTime();
		if (end == null) {
			testEnd.setValue(null);
		}
		else {
			testEnd.setValue(sqlTypes.getTimeStamp(end));
		}
		nSequences.setValue(testSet.getNumSequences());
		version.setValue(testSet.getQaCluster().getVersion());
	}

	/* (non-Javadoc)
	 * @see generalDatabase.SQLLogging#createDataUnit(generalDatabase.SQLTypes, long, int)
	 */
	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {
		Long endT = null;
		if (testEnd.getStringValue() != null) {
			endT = SQLTypes.millisFromTimeStamp(testEnd.getStringValue());
		}
		String testType = this.testType.getDeblankedStringValue();
		String signalType = this.signalType.getDeblankedStringValue();
		int nSeq = nSequences.getIntegerValue();
		String state = status.getDeblankedStringValue();
		String testVersion = version.getDeblankedStringValue();
		QACluster qaCluster = qaControl.findCluster(signalType);
		QATestSet testSet = new ViewerTestSet(signalType, qaCluster, testVersion, nSeq, endT);
		testSet.setStatus(state);
//		boolean isRandom = QAControl.randomTestName.equals(testType);
		QATestDataUnit dataUnit = new QATestDataUnit(timeMilliseconds, testType, testSet);
		dataUnit.setQaOpsDataUnit(qaControl.getQaOperationsStatus().getCurrentStatus());
		return dataUnit;
	}


}
