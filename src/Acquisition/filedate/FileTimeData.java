package Acquisition.filedate;

import java.io.File;
import java.io.Serializable;

import PamUtils.PamCalendar;

/**
 * File time information. More than just the file start time, though 
 * that must always be there. Has optional additional fields for the file end
 * the data source (e.g. the file name, or a GNS stamp in some recorder data) and clock 
 * drift information in parts per million if known. . 
 */
public class FileTimeData implements Cloneable, Serializable {
	
	private static final long serialVersionUID = 1L;

	private long fileStart;
	
	private Long fileEnd;
	
	private String dateSource;
	
	private Double driftPPM;
	
	private File file;

	/**
	 * @param file
	 * @param fileStart
	 * @param fileEnd
	 * @param dateSource
	 * @param driftPPM
	 */
	public FileTimeData(File file, long fileStart, Long fileEnd, String dateSource, Double driftPPM) {
		super();
		this.file = file;
		this.fileStart = fileStart;
		this.fileEnd = fileEnd;
		this.dateSource = dateSource;
		this.driftPPM = driftPPM;
	}
	

	/**
	 * @param file
	 * @param fileStart
	 * @param fileEnd
	 * @param dateSource
	 * @param driftPPM
	 */
	public FileTimeData(File file, long fileStart, String dateSource) {
		super();
		this.file = file;
		this.fileStart = fileStart;
		this.dateSource = dateSource;
	}
	
	/**
	 * @param fileStart File start in milliseconds
	 */
	public FileTimeData(long fileStart) {
		super();
		this.fileStart = fileStart;
	}
	/**
	 * @param fileStart File start in milliseconds
	 * @param fileEnd File end in milliseconds
	 */
	public FileTimeData(long fileStart, Long fileEnd) {
		super();
		this.fileStart = fileStart;
		this.fileEnd = fileEnd;
	}
	/**
	 * @param fileStart File start in milliseconds
	 * @param fileEnd File end in milliseconds
	 * @param dateSource source of data for the file start (e.g. File name, GNS, etc.)
	 * @param driftPPM Clock drift if known in parts per million
	 */
	public FileTimeData(long fileStart, Long fileEnd, String dateSource) {
		super();
		this.fileStart = fileStart;
		this.fileEnd = fileEnd;
		this.dateSource = dateSource;
	}
	/**
	 * @param fileStart File start in milliseconds
	 * @param fileEnd File end in milliseconds
	 * @param dateSource source of data for the file start (e.g. File name, GNS, etc.)
	 * @param driftPPM Clock drift if known in parts per million
	 */
	public FileTimeData(long fileStart, Long fileEnd, String dateSource, Double driftPPM) {
		super();
		this.fileStart = fileStart;
		this.fileEnd = fileEnd;
		this.dateSource = dateSource;
		this.driftPPM = driftPPM;
	}
	/**
	 * @return the fileStart
	 */
	public long getFileStart() {
		return fileStart;
	}
	/**
	 * @param fileStart the fileStart to set
	 */
	public void setFileStart(long fileStart) {
		this.fileStart = fileStart;
	}
	/**
	 * @return the fileEnd
	 */
	public Long getFileEnd() {
		return fileEnd;
	}
	/**
	 * @param fileEnd the fileEnd to set
	 */
	public void setFileEnd(Long fileEnd) {
		this.fileEnd = fileEnd;
	}
	/**
	 * @return the dateSource
	 */
	public String getDateSource() {
		return dateSource;
	}
	/**
	 * @param dateSource the dateSource to set
	 */
	public void setDateSource(String dateSource) {
		this.dateSource = dateSource;
	}
	/**
	 * @return the driftPPM
	 */
	public Double getDriftPPM() {
		return driftPPM;
	}
	/**
	 * @param driftPPM the driftPPM to set
	 */
	public void setDriftPPM(Double driftPPM) {
		this.driftPPM = driftPPM;
	}
	@Override
	protected FileTimeData clone()  {
		try {
			return (FileTimeData) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	@Override
	public String toString() {
		String end = "Unknown";
		if (fileEnd != null) {
			end = PamCalendar.formatDBDateTime(fileEnd, true);
		}
		return String.format("File %s start: %s, end %s, source %s, drift %s", getFileName(), 
				PamCalendar.formatDBDateTime(fileStart, true), end, dateSource, driftPPM);
	}


	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}


	/**
	 * @param file the file to set
	 */
	public void setFile(File file) {
		this.file = file;
	}
	
	/**
	 * Just the name part of the file, not the full path. 
	 * @return
	 */
	public String getFileName() {
		if (file == null) {
			return null;
		}
		return file.getName();
	}
	
	/**
	 * Get a 2 element array of the start and end. Second element will be 0
	 * if no end time is set
	 * Included for compatibility. would be good to get rid of this. 
	 * @return
	 */
	public long[] getStartandEnd() {
		long[] t = new long[2];
		t[0] = fileStart;
		if (fileEnd != null) {
			t[1] = fileEnd;
		}
		return t;
	}

}
