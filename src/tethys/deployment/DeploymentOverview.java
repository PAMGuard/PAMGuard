package tethys.deployment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ListIterator;

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionParameters;
import Acquisition.DaqStatusDataUnit;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamguardMVC.PamDataBlock;

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
	
	private DutyCycleInfo dutyCycleInfo;

	public DeploymentOverview(DutyCycleInfo dutyCycleInfo) {
		super();
		this.dutyCycleInfo = dutyCycleInfo;
	}
	
	public DeploymentOverview(DutyCycleInfo dutyCycleInfo, ArrayList<RecordingPeriod> tempPeriods) {
		this.dutyCycleInfo = dutyCycleInfo;
		this.recordingPeriods = tempPeriods;
	}
	
	
	public void addRecordingPeriod(long start, long stop) {
		addRecordingPeriod(new RecordingPeriod(start, stop));
	}

	private void addRecordingPeriod(RecordingPeriod recordingPeriod) {
		recordingPeriods.add(recordingPeriod);
	}

	public ArrayList<RecordingPeriod> getRecordingPeriods() {
		return recordingPeriods;
	}

	public DutyCycleInfo getDutyCycleInfo() {
		return dutyCycleInfo;
	}
	
	/**
	 * Get the start time of the first recording
	 * @return
	 */
	public Long getFirstStart() {
		if (recordingPeriods.size() > 0) {
			return recordingPeriods.get(0).getRecordStart();
		}
		return null;
	}
	
	/**
	 * Get the end time of the last recording
	 * @return
	 */
	public Long getLastEnd() {
		if (recordingPeriods.size() > 0) {
			return recordingPeriods.get(recordingPeriods.size()-1).getRecordStop();
		}
		return null;
	}
	
	

}
