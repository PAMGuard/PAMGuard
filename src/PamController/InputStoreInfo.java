package PamController;

import PamUtils.PamCalendar;

public class InputStoreInfo {

	private DataInputStore dataInputStore;
	private int nFiles;
	private long firstFileStart, lastFileStart, lastFileEnd;
	private long[] fileStartTimes;
	
	public InputStoreInfo(DataInputStore dataInputStore, int nFiles, long firstFileStart, long lastFileStart, long lastFileEnd) {
		super();
		this.dataInputStore = dataInputStore;
		this.nFiles = nFiles;
		this.firstFileStart = firstFileStart;
		this.lastFileStart = lastFileStart;
		this.lastFileEnd = lastFileEnd;
	}

	/**
	 * @return the nFiles
	 */
	public int getnFiles() {
		return nFiles;
	}

	/**
	 * @return the firstFileStart
	 */
	public long getFirstFileStart() {
		return firstFileStart;
	}

	/**
	 * @return the lastFileStart
	 */
	public long getLastFileStart() {
		return lastFileStart;
	}

	/**
	 * @return the lastFileEnd
	 */
	public long getLastFileEnd() {
		return lastFileEnd;
	}

	@Override
	public String toString() {
		return String.format("%s: %d files. First start %s, last start %s, last end %s", dataInputStore.getClass().getName(), nFiles, 
				PamCalendar.formatDBDateTime(firstFileStart), PamCalendar.formatDBDateTime(lastFileStart),
				PamCalendar.formatDBDateTime(lastFileEnd));
	}

	/**
	 * @return the dataInputStore
	 */
	public DataInputStore getDataInputStore() {
		return dataInputStore;
	}

	/**
	 * Set the start times of all files in data set. 
	 * @param allFileStarts
	 */
	public void setFileStartTimes(long[] allFileStarts) {
		this.fileStartTimes = allFileStarts;
		
	}

	/**
	 * @return the fileStartTimes
	 */
	public long[] getFileStartTimes() {
		return fileStartTimes;
	}

	/**
	 * @param firstFileStart the firstFileStart to set
	 */
	public void setFirstFileStart(long firstFileStart) {
		this.firstFileStart = firstFileStart;
	}

	/**
	 * @param lastFileStart the lastFileStart to set
	 */
	public void setLastFileStart(long lastFileStart) {
		this.lastFileStart = lastFileStart;
	}

	/**
	 * @param lastFileEnd the lastFileEnd to set
	 */
	public void setLastFileEnd(long lastFileEnd) {
		this.lastFileEnd = lastFileEnd;
	}
	


}
