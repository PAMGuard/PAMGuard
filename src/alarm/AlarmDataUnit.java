package alarm;

import PamDetection.PamDetection;
import PamguardMVC.PamDataUnit;


public class AlarmDataUnit extends PamDataUnit implements PamDetection {

	private long[] firstStateTime  =new long[AlarmParameters.COUNT_LEVELS+1]; 
	private long[] lastStateTime = new long[AlarmParameters.COUNT_LEVELS+1];
	private int currentStatus, highestStatus;
	private double currentScore, highestScore;
	private long lastUpdate;
	private boolean isActive = true;
	private String[] extraFieldData;
	
	
	/**
	 * Constructor for normal operation. 
	 * @param timeMilliseconds
	 */
	public AlarmDataUnit(long timeMilliseconds) {
		super(timeMilliseconds);
		lastUpdate = timeMilliseconds;
	}
	
	/**
	 * constructor to use when reading back from databse 
	 * @param timeMilliseconds
	 * @param firstStateTimes
	 * @param lastStateTimes
	 * @param highestStatus
	 * @param highestScore
	 */
	public AlarmDataUnit(long timeMilliseconds, long[] firstStateTimes, long[] lastStateTimes, int highestStatus, double highestScore) {
		super(timeMilliseconds);
		this.firstStateTime = firstStateTimes;
		this.lastStateTime = lastStateTimes;
		this.highestScore = highestScore;
		this.highestStatus = highestStatus;
	}

	/**
	 * Sets the alarm state and records the start and end times for each of those states. 
	 * @param alarmStatus new status
	 * @param timeMillis time milliseconds
	 */
	public void setAlarmStatus(int alarmStatus, double score, long timeMillis) {
		if (firstStateTime[alarmStatus] == 0) {
			firstStateTime[alarmStatus] = timeMillis;
		}
		lastStateTime[alarmStatus] = timeMillis;
		currentStatus = alarmStatus;
		highestStatus = Math.max(highestStatus, currentStatus);
		lastUpdate = timeMillis;
		currentScore = score;
		highestScore = Math.max(highestScore, currentScore);
	}

	public int getCurrentStatus() {
		return currentStatus;
	}

	public void setCurrentStatus(int currentStatus) {
		this.currentStatus = currentStatus;
	}

	public int getHighestStatus() {
		return highestStatus;
	}

	public void setHighestStatus(int highestStatus) {
		this.highestStatus = highestStatus;
	}

	public long[] getFirstStateTime() {
		return firstStateTime;
	}

	public long[] getLastStateTime() {
		return lastStateTime;
	}

	public double getCurrentScore() {
		return currentScore;
	}

	public double getHighestScore() {
		return highestScore;
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	/**
	 * @return the extraFieldData
	 */
	public String[] getExtraFieldData() {
		return extraFieldData;
	}

	/**
	 * @param extraFieldData the extraFieldData to set
	 */
	public void setExtraFieldData(String[] extraFieldData) {
		this.extraFieldData = extraFieldData;
	}

	@Override
	public long getEndTimeInMilliseconds() {
		long end = getTimeMilliseconds();
		for (int i = 0; i < lastStateTime.length; i++) {
			end = Math.max(end, lastStateTime[i]);
		}
		return end;
	}
	

}
