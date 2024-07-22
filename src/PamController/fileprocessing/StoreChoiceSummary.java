package PamController.fileprocessing;

import java.util.ArrayList;
import java.util.List;

import PamController.InputStoreInfo;

/**
 * Summary information about the data stores. 
 * @author dg50
 *
 */
public class StoreChoiceSummary {
	
	private long outputEndTime;
	
	private long outputStartTime;
	
	private List<ReprocessStoreChoice> choices = new ArrayList<>();
	
	private InputStoreInfo inputStoreInfo;
	
	public StoreChoiceSummary(InputStoreInfo info, ReprocessStoreChoice singleChoice) {
		this.inputStoreInfo = info;
		addChoice(singleChoice);
	}

	public StoreChoiceSummary(long outputEndTime, InputStoreInfo inputStoreInfo) {
		super();
		this.outputEndTime = outputEndTime;
		this.inputStoreInfo = inputStoreInfo;
	}
	
	public StoreChoiceSummary(InputStoreInfo inputInfo) {
		this.inputStoreInfo = inputInfo;
	}
	
	/**
	 * Get the number of choices. If it's only one, then there
	 * isn't a lot to do. If it's >1, then need a decision in the 
	 * form of a command line instruction or a user dialog. 
	 * @return number of choices. 
	 */
	public int getNumChoices() {
		return choices.size();
	}
	
	/**
	 * Is processing complete, i.e. last time in output matches last time 
	 * in input data. 
	 * @return true if processing appears to be complete. 
	 */
	public boolean isProcessingComplete() {
		if (inputStoreInfo == null) {
			return false;
		}
		long inputEnd = getInputEndTime();
		long outputEnd = getOutputEndTime();
		long diff = inputEnd-outputEnd;
		return (diff < 1000);
	}

	/**
	 * Add a reasonable choice to what the user can select to do. 
	 * @param choice
	 */
	public void addChoice(ReprocessStoreChoice choice) {
		if (choices.contains(choice)) {
			return;
		}
		choices.add(choice);
	}

	/**
	 * @return the start time of the first input file
	 */
	public Long getInputStartTime() {
		if (inputStoreInfo == null) {
			return null;
		}
		return inputStoreInfo.getFirstFileStart();
	}

	/**
	 * @return the start time of the first input file
	 */
	public Long getInputEndTime() {
		if (inputStoreInfo == null) {
			return null;
		}
		return inputStoreInfo.getLastFileEnd();
	}

	/**
	 * @return the outputEndTime
	 */
	public long getOutputEndTime() {
		return outputEndTime;
	}


	/**
	 * Set the last data time, but only if the passed value
	 * is not null and is bigger than the current value. 
	 * @param lastDataTime 
	 * @return largest of current and passed value. 
	 */
	public long testOutputEndTime(Long lastDataTime) {
		if (lastDataTime == null) {
			return this.getOutputEndTime();
		}
		setOutputEndTime(Math.max(outputEndTime, lastDataTime));
		return getOutputEndTime();
	}

	/**
	 * Set the last data time, but only if the passed value
	 * is not null and is bigger than the current value. 
	 * @param lastDataTime 
	 * @return largest of current and passed value. 
	 */
	public long testOutputStartTime(Long firstDataTime) {
		if (firstDataTime == null) {
			return this.getOutputStartTime();
		}
		if (outputStartTime == 0 || firstDataTime < outputStartTime) {
			outputStartTime = firstDataTime;
		}
		return getOutputStartTime();
	}
	
	/**
	 * @param outputEndTime the outputEndTime to set
	 */
	public void setOutputEndTime(long outputEndTime) {
		this.outputEndTime = outputEndTime;
	}

	/**
	 * @return the inputStoreInfo
	 */
	public InputStoreInfo getInputStoreInfo() {
		return inputStoreInfo;
	}

	/**
	 * @param inputStoreInfo the inputStoreInfo to set
	 */
	public void setInputStoreInfo(InputStoreInfo inputStoreInfo) {
		this.inputStoreInfo = inputStoreInfo;
	}

	/**
	 * @return the choices
	 */
	public List<ReprocessStoreChoice> getChoices() {
		return choices;
	}

	/**
	 * @return the outputStartTime
	 */
	public long getOutputStartTime() {
		return outputStartTime;
	}

	/**
	 * @param outputStartTime the outputStartTime to set
	 */
	public void setOutputStartTime(long outputStartTime) {
		this.outputStartTime = outputStartTime;
	}

	/**
	 * Get the index of the file that starts before or exactly at the given time. 
	 * @param inputEndTime
	 * @return index of file, or -1 if none found. 
	 */
	public int getFileIndexBefore(Long inputEndTime) {
		if (inputStoreInfo == null) {
			return -1;
		}
		long[] fileStarts = inputStoreInfo.getFileStartTimes();
		if (fileStarts == null) {
			return -1;
		}
		for (int i = fileStarts.length-1; i>= 0; i--) {
			if (fileStarts[i] <= inputEndTime) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Get the start time in millis of a file for the given index. 
	 * @param fileIndex
	 * @return file time, or null if no file available. 
	 */
	public Long getInputTimeForIndex(int fileIndex) {
		if (inputStoreInfo == null) {
			return null;
		}
		long[] fileStarts = inputStoreInfo.getFileStartTimes();
		if (fileStarts == null) {
			return null;
		}
		if (fileIndex < 0 || fileIndex >= fileStarts.length) {
			return null;
		}
		return fileStarts[fileIndex];
	}

	/**
	 * Get the index of the file that starts after the given time. 
	 * @param inputEndTime
	 * @return index of file, or -1 if none found. 
	 */
	public int getFileIndexAfter(Long inputEndTime) {
		if (inputStoreInfo == null) {
			return -1;
		}
		long[] fileStarts = inputStoreInfo.getFileStartTimes();
		if (fileStarts == null) {
			return -1;
		}
		for (int i = 0; i < fileStarts.length; i++) {
			if (fileStarts[i] > inputEndTime) {
				return i;
			}
		}
		return -1;
	}

	
	
}
