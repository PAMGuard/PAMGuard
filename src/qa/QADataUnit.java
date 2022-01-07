package qa;

import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;
import qa.operations.QAOpsDataUnit;

/**
 * Generic class for QA Tests, Sequences and Sounds so that they 
 * can hold their operational status data. 
 * @author dg50
 *
 * @param <T>
 * @param <U>
 */
public class QADataUnit<T extends PamDataUnit, U extends PamDataUnit> extends SuperDetection<T> {

	private QAOpsDataUnit qaOpsDataUnit; 
	
	public QADataUnit(long timeMilliseconds) {
		super(timeMilliseconds);
	}

	/**
	 * @param basicData
	 */
	public QADataUnit(DataUnitBaseData basicData) {
		super(basicData);
	}

	/**
	 * @param timeMilliseconds
	 * @param channelBitmap
	 * @param startSample
	 * @param durationSamples
	 */
	public QADataUnit(long timeMilliseconds, int channelBitmap, long startSample, long durationSamples) {
		super(timeMilliseconds, channelBitmap, startSample, durationSamples);
	}

	/**
	 * @return the qaOpsDataUnit
	 */
	public QAOpsDataUnit getQaOpsDataUnit() {
		return qaOpsDataUnit;
	}

	/**
	 * @param qaOpsDataUnit the qaOpsDataUnit to set
	 */
	public void setQaOpsDataUnit(QAOpsDataUnit qaOpsDataUnit) {
		this.qaOpsDataUnit = qaOpsDataUnit;
	}

}
