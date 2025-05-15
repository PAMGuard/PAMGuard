package tethys.deployment;

import java.util.ArrayList;

import PamUtils.PamCalendar;
import tethys.niluswraps.PDeployment;

public class RecordingPeriod implements Cloneable {

	private long recordStart;
	
	private long recordStop;
	
	private boolean selected; // selected in the table or elsewhere for export. 
	
	// record gaps in this. 
	private ArrayList<RecordingPeriod> recordingGaps;
	
	/**
	 * Reference to a matched nilus Deployment document retrieved 
	 * from the database. 
	 */
	private PDeployment matchedTethysDeployment;
	
	private DutyCycleInfo dutyCycleInfo;

	public RecordingPeriod(long recordStart, long recordStop) {
		super();
		this.recordStart = recordStart;
		this.recordStop = recordStop;
	}

	public long getRecordStart() {
		return recordStart;
	}

	public void setRecordStart(long recordStart) {
		this.recordStart = recordStart;
	}

	public long getRecordStop() {
		return recordStop;
	}

	public void setRecordStop(long recordStop) {
		this.recordStop = recordStop;
	}
	
	public long getDuration() {
		return recordStop-recordStart;
	}

	public PDeployment getMatchedTethysDeployment() {
		return matchedTethysDeployment;
	}

	public void setMatchedTethysDeployment(PDeployment closestDeployment) {
		this.matchedTethysDeployment = closestDeployment;
	}

	/**
	 * @return the selected
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * @param selected the selected to set
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	/**
	 * toggle the selected state
	 * @return the new state
	 */
	public boolean toggleSelected() {
		selected = !selected;
		return selected;
	}

	@Override
	public String toString() {
		return String.format("%s to %s, %s", PamCalendar.formatDBDateTime(recordStart), 
				PamCalendar.formatDBDateTime(recordStop), PamCalendar.formatDuration(getDuration()));
	}

	@Override
	public RecordingPeriod clone() {
		try {
			return (RecordingPeriod) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Add a gap in the recording period. 
	 * @param gap
	 */
	public void addRecordingGap(RecordingPeriod gap) {
		if (recordingGaps == null) {
			recordingGaps = new ArrayList<>();
		}
		recordingGaps.add(gap);
	}

	/**
	 * @return the recordingGaps
	 */
	public ArrayList<RecordingPeriod> getRecordingGaps() {
		return recordingGaps;
	}

	/**
	 * @return the dutyCycleInfo
	 */
	public DutyCycleInfo getDutyCycleInfo() {
		return dutyCycleInfo;
	}

	/**
	 * @param dutyCycleInfo the dutyCycleInfo to set
	 */
	public void setDutyCycleInfo(DutyCycleInfo dutyCycleInfo) {
		this.dutyCycleInfo = dutyCycleInfo;
	}
	
}
