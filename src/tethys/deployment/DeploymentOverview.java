package tethys.deployment;

import java.util.ArrayList;

/**
 * Class to give a general overview of all the effort in PAMGuard which will form the
 * basis for Deployment Documents. 
 * This will tell us if data were on a regular duty cycle or more adhoc and also provide
 * a list of all start and end times for these periods. 
 * @author dg50
 *
 */
public class DeploymentOverview {
	
	private ArrayList<RecordingPeriod> recordingPeriods = new ArrayList<>();
	
	private boolean dutyCycled;

	public DeploymentOverview(boolean dutyCycled) {
		super();
		this.dutyCycled = dutyCycled;
	}
	
	public DeploymentOverview(boolean b, ArrayList<RecordingPeriod> tempPeriods) {
		this.recordingPeriods = tempPeriods;
	}

	public void addRecordingPeriod(long start, long stop) {
		addRecordingPeriod(new RecordingPeriod(start, stop));
	}

	private void addRecordingPeriod(RecordingPeriod recordingPeriod) {
		recordingPeriods.add(recordingPeriod);
	}

	public boolean isDutyCycled() {
		return dutyCycled;
	}

	public void setDutyCycled(boolean dutyCycled) {
		this.dutyCycled = dutyCycled;
	}

	public ArrayList<RecordingPeriod> getRecordingPeriods() {
		return recordingPeriods;
	}
	
	

}
