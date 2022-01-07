package qa.analyser;

import java.util.Arrays;

import PamguardMVC.PamDataBlock;
import qa.QATestDataUnit;

/**
 * Processed and summarised detection data for a single or group of tests and a single detector
 * should contain enough data and functionality to provide everything needed for reporting.  
 * @author dg50
 *
 */
public class QATestResult implements Cloneable {

	private QATestDataUnit testDataUnit;
	private PamDataBlock testDetector;
	
	private SequenceSummary sequenceSummary;
	
	public static final int SEQUENCE_ANAL_SET = 0;
	public static final int SOUND_ANAL_SET = 1;
	/**
	 * Two main analysis types, sounds and sequences. 
	 */
	private QATestAnalysis[] testAnalysis = new QATestAnalysis[2];
	
	/**
	 * @param testDataUnit
	 * @param testDetector
	 */
	public QATestResult(QATestDataUnit testDataUnit, PamDataBlock testDetector) {
		super();
		this.testDataUnit = testDataUnit;
		this.testDetector = testDetector;
	}
	
	/**
	 * @return the testDataUnit
	 */
	public QATestDataUnit getTestDataUnit() {
		return testDataUnit;
	}
	/**
	 * @return the testDetector
	 */
	public PamDataBlock getTestDetector() {
		return testDetector;
	}
	/**
	 * @return the sequenceSummary
	 */
	public SequenceSummary getSequenceSummary() {
		return sequenceSummary;
	}
	
	/**
	 * @param sequenceSummary the sequenceSummary to set
	 */
	public void setSequenceSummary(SequenceSummary sequenceSummary) {
		this.sequenceSummary = sequenceSummary;
	}

	/**
	 * Combine an array of reports into a single report, creating single 
	 * result arrays with data for all ranges.
	 * @param qaTestReports array of reports
	 * @return combined report. 
	 */
	public static QATestResult combineReports(QATestResult[] qaTestReports) throws QAReportException {
		if (qaTestReports == null || qaTestReports.length == 0) {
			return null;
		}
		if (qaTestReports.length == 1) {
			/**
			 * Common situation - there is only one, so send it back 
			 */
			return qaTestReports[0];
		}
		QATestResult cReport = qaTestReports[0].clone();
		for (int i = 1; i < qaTestReports.length; i++) {
			cReport.addReport(qaTestReports[i]);
		}
		
		return cReport;
	}

	/**
	 * Add the contents of another report into this one. 
	 * @param qaTestReport
	 */
	private void addReport(QATestResult qaTestReport) throws QAReportException {
		if (this.testDetector != qaTestReport.testDetector) {
			throw new QAReportException(String.format("Can't combine reports using different detectors this: %s; other %s", 
					testDetector.getLongDataName(), qaTestReport.testDetector.getLongDataName()));
		}
		sequenceSummary = sequenceSummary.add(qaTestReport.sequenceSummary);
	}

	@Override
	protected QATestResult clone() {
		try {
			return (QATestResult) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 
	 * @return range of range values (min and max)
	 */
	public double[] getRangeRange() {
		double[] ranges = sequenceSummary.getSeqRanges();
		if (ranges == null || ranges.length == 0) {
			return null;
		}
		double[] minmax = new double[2];
		Arrays.fill(minmax, ranges[0]);
		for (int i = 1; i < ranges.length; i++) {
			minmax[0] = Math.min(minmax[0], ranges[i]);
			minmax[1] = Math.max(minmax[1], ranges[i]);
		}
		return minmax;
	}

	/**
	 * @return the soundAnalysis
	 */
	public QATestAnalysis getSoundAnalysis() {
		return testAnalysis[SOUND_ANAL_SET];
	}

	/**
	 * @param soundAnalysis the soundAnalysis to set
	 */
	public void setSoundAnalysis(QATestAnalysis soundAnalysis) {
		this.testAnalysis[SOUND_ANAL_SET] = soundAnalysis;
	}

	/**
	 * @return the sequenceAnalysis
	 */
	public QATestAnalysis getSequenceAnalysis() {
		return testAnalysis[SEQUENCE_ANAL_SET];
	}

	/**
	 * @param sequenceAnalysis the sequenceAnalysis to set
	 */
	public void setSequenceAnalysis(QATestAnalysis sequenceAnalysis) {
		this.testAnalysis[SEQUENCE_ANAL_SET] = sequenceAnalysis;
	}
	
	/**
	 * Get one of the test analysis sets. 
	 * @param seqOrSound 0 = Sequence, 1 = Single Sounds. 
	 * @return Analysis set
	 */
	public QATestAnalysis getTestAnalysis(int seqOrSound) {
		return testAnalysis[seqOrSound];
	}
	

}
