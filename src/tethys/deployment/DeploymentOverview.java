package tethys.deployment;

import tethys.TethysControl;

/**
 * Class to give a general overview of all the effort in PAMGuard which will form the
 * basis for Deployment Documents. 
 * This will tell us if data were on a regular duty cycle or more adhoc and also provide
 * a list of all start and end times for these periods. 
 * @author dg50
 *
 */
public class DeploymentOverview {
	
	private RecordingList rawDataList;
	
	private RecordingList binaryDataList;
	
//	private DutyCycleInfo dutyCycleInfo;

//	public DeploymentOverview(DutyCycleInfo dutyCycleInfo) {
//		super();
//		this.dutyCycleInfo = dutyCycleInfo;
//	}
	
	public DeploymentOverview(DutyCycleInfo dutyCycleInfo, RecordingList rawDataList, RecordingList binaryDataList) {
//		this.dutyCycleInfo = dutyCycleInfo;
		this.rawDataList = rawDataList;
		this.binaryDataList = binaryDataList;
	}

	/**
	 * @return the rawDataList
	 */
	public RecordingList getRawDataList() {
		return rawDataList;
	}

	/**
	 * @return the binaryDataList
	 */
	public RecordingList getBinaryDataList() {
		return binaryDataList;
	}

//	/**
//	 * @return the dutyCycleInfo
//	 */
//	public DutyCycleInfo getDutyCycleInfo() {
//		return dutyCycleInfo;
//	}
	
	public RecordingList getMasterList(TethysControl tethysControl) {
		return getMasterList(tethysControl.getTethysExportParams().getEffortSourceName());
	}

	public RecordingList getMasterList(String effortSourceName) {
		if (effortSourceName == null) {
			return getLongestList();
		}
		if (binaryDataList != null & binaryDataList.getSourceName().equals(effortSourceName)) {
			return binaryDataList;
		}
		if (rawDataList != null & rawDataList.getSourceName().equals(effortSourceName)) {
			return rawDataList;
		}
		return getLongestList();
	}
	
	/**
	 * Get the recording list with the greatest duration (start to end)
	 * not looking at coverage between those times. 
	 * @return
	 */
	public RecordingList getLongestList() {
		if (binaryDataList == null) {
			return rawDataList;
		}
		if (rawDataList == null) {
			return binaryDataList;
		}
		long lRaw = rawDataList.duration();
		long lBin = binaryDataList.duration();
		
		if (lRaw > lBin) {
			return rawDataList;
		}
		else {
			return binaryDataList;
		}
	}


}
