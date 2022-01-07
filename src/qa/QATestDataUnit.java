package qa;


import PamguardMVC.PamDataUnit;
import qa.generator.testset.QATestSet;

public class QATestDataUnit extends QADataUnit<QASequenceDataUnit, PamDataUnit> {

	private QATestSet qaTestSet;
	private String testType;
	

	public QATestDataUnit(long timeMilliseconds, String testType, QATestSet qaTestSet) {
		super(timeMilliseconds);
		this.testType = testType;
		this.qaTestSet = qaTestSet;
		qaTestSet.setTestDataUnit(this);
//		setLocalisation(new GeneratorLocalisation(this));
	}

	/**
	 * @return the qaTestSet
	 */
	public QATestSet getQaTestSet() {
		return qaTestSet;
	}

	/**
	 * @param qaTestSet the qaTestSet to set
	 */
	public void setQaTestSet(QATestSet qaTestSet) {
		this.qaTestSet = qaTestSet;
	}

	/**
	 * @return the testType
	 */
	public String getTestType() {
		return testType;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataUnit#addSubDetection(PamguardMVC.PamDataUnit)
	 */
	@Override
	public int addSubDetection(QASequenceDataUnit subDetection) {
		// TODO Auto-generated method stub
		return super.addSubDetection(subDetection);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("QATestDataUnit %s %s, UID %d", testType, qaTestSet.getTestName(), getUID());
	}

	@Override
	public long getEndTimeInMilliseconds() {
		return super.getEndTimeInMilliseconds();
//		long endT = super.getEndTimeInMilliseconds();
//		if (endT > getTimeMilliseconds()) {
//			return endT; // probably OK. 
//		}
//		endT = getTimeMilliseconds();
//		synchronized (getSubDetectionSyncronisation()) {
//			int nSub = getSubDetectionsCount();
//			if (nSub >0) {
//				QASequenceDataUnit lastSub = getSubDetection(nSub-1);
//				endT = lastSub.getEndTimeInMilliseconds();
//			}
//		}
//		return endT;
	}

}
