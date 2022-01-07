package qa.generator.testset;

import PamUtils.LatLong;
import qa.QATestDataUnit;
import qa.generator.QASoundGenerator;
import qa.generator.clusters.QACluster;
import qa.generator.location.QALocationGenerator;
import qa.generator.sequence.SoundSequence;

abstract public class QATestSet {
	
	public static String STATUS_ACTIVE = "Active";
	public static String STATUS_COMPLETE = "Complete";
	public static String STATUS_CANCELLED = "Cancelled";
	public static String STATUS_IDLE = "Idle";
	
	private double sampleRate;
	/**
	 * @return the sampleRate
	 */
	public double getSampleRate() {
		return sampleRate;
	}

	private String testName;
	private Long endTime;
	private QATestDataUnit testDataUnit;
	private QACluster qaCluster;
	private QALocationGenerator locationGenerator;
	private String status;

	/**
	 * @param soundGenerator
	 */
	public QATestSet(String testName, QALocationGenerator locationGenerator, QACluster qaCluster, double sampleRate) {
		super();
		this.testName = testName;
		this.locationGenerator = locationGenerator;
		this.setQaCluster(qaCluster);
		this.sampleRate = sampleRate;
		status = STATUS_ACTIVE;
	}

	/**
	 * 
	 * @param currentSample current daq sample. 
	 * @return true when the test is complete so that it can be removed from test lists. 
	 */
	abstract public boolean isFinsihed(long currentSample);
	
	/**
	 * Get the next sequence of sounds. Will return null unless it's the right 
	 * time to generate a new sequence
	 * @param startSample end sample in latest arriving raw data
	 * @param currentLocation current reference location for tests. 
	 * @return a new sound sequence or null. 
	 */
	abstract public SoundSequence getNextSequence(long startSample, LatLong currentLocation);

	
	/**
	 * Get the number of sequences in the test. This is the number 
	 * that have actually been generated, not the number planned. 
	 * @return Number of sequences generated so far. 
	 */
	public abstract int getNumSequences();
	
	/**
	 * Get a version number for the test set. 
	 * @return
	 */
	public abstract String getVersion();

	/**
	 * @return the endTime
	 */
	public Long getEndTime() {
		return endTime;
	}

	/**
	 * @param endTime the endTime to set
	 */
	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}

	/**
	 * @return the testName
	 */
	public String getTestName() {
		return testName;
	}

	/**
	 * @return the testDataUnit
	 */
	public QATestDataUnit getTestDataUnit() {
		return testDataUnit;
	}

	/**
	 * @param testDataUnit the testDataUnit to set
	 */
	public void setTestDataUnit(QATestDataUnit testDataUnit) {
		this.testDataUnit = testDataUnit;
	}

	/**
	 * @return the qaCluster
	 */
	public QACluster getQaCluster() {
		return qaCluster;
	}

	/**
	 * @param qaCluster the qaCluster to set
	 */
	public void setQaCluster(QACluster qaCluster) {
		this.qaCluster = qaCluster;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	public void cancelTest() {
		setStatus(STATUS_CANCELLED);		
	}

	/**
	 * @return the locationGenerator
	 */
	public QALocationGenerator getLocationGenerator() {
		return locationGenerator;
	}
	
	
	
}
