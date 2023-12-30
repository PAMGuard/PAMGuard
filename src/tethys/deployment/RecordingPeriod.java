package tethys.deployment;

import tethys.niluswraps.PDeployment;

public class RecordingPeriod {

	private long recordStart;
	
	private long recordStop;
	
	private boolean selected; // selected in the table or elsewhere for export. 
	/**
	 * Reference to a matched nilus Deployment document retrieved 
	 * from the database. 
	 */
	private PDeployment matchedTethysDeployment;

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
	
}
