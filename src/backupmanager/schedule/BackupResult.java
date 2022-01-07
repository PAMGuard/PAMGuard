package backupmanager.schedule;

import PamUtils.PamCalendar;

public class BackupResult {
	
	public enum RESULT {Success, Fail, Unknown};

	private String backupSchedule; 
	
	private Long startTime;
	
	private Long endTime;
	
	private RESULT result;

	private String detail;

	private Integer dbIndex;
	
	public BackupResult(Integer dbIndex, String backupSchedule, Long startTime, Long endTime, RESULT result, String detail) {
		super();
		this.setDbIndex(dbIndex);
		this.backupSchedule = backupSchedule;
		this.startTime = startTime;
		this.endTime = endTime;
		this.result = result;
		this.detail = detail;
	}

	public BackupResult(String backupSchedule, Long startTime) {
		super();
		this.backupSchedule = backupSchedule;
		this.startTime = startTime;
	}

	/**
	 * @return the backupSchedule
	 */
	public String getBackupSchedule() {
		return backupSchedule;
	}

	/**
	 * @return the startTime
	 */
	public Long getStartTime() {
		return startTime;
	}

	/**
	 * @return the endTime
	 */
	public Long getEndTime() {
		return endTime;
	}

	/**
	 * @return the result
	 */
	public RESULT getResult() {
		return result;
	}

	/**
	 * @return the detail
	 */
	public String getDetail() {
		return detail;
	}

	/**
	 * @return the dbIndex
	 */
	public Integer getDbIndex() {
		return dbIndex;
	}

	/**
	 * @param dbIndex the dbIndex to set
	 */
	public void setDbIndex(Integer dbIndex) {
		this.dbIndex = dbIndex;
	}

	/**
	 * @param endTime the endTime to set
	 */
	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(RESULT result) {
		this.result = result;
	}

	/**
	 * @param detail the detail to set
	 */
	public void setDetail(String detail) {
		this.detail = detail;
	}

	@Override
	public String toString() {
		if (result == null) {
			return null;
		}
		String str = String.format("%s", result.toString());
		if (startTime != null) {
			str += String.format(" at %s", PamCalendar.formatDateTime(startTime));
			if (endTime != null) {
				str += String.format(" took %3.1fs", (double) (endTime-startTime) / 1000.);
			}
		}
		return str;
	}
	
	

}
