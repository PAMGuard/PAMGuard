package backupmanager.filter.alarm;

import java.io.Serializable;
import java.util.HashMap;

import backupmanager.filter.BackupFilterParams;

public class AlarmFilterParams implements Serializable, BackupFilterParams {

	public static final long serialVersionUID = 1L;
	
	private HashMap<String, AlarmParamSet> alarmParameters = new HashMap<>();
	
	private boolean passEverything = true;
	
	/**
	 * Get params for a specific alarm based on module name. Will create
	 * a new instance if there isn't already one in the table. 
	 * @param alarmName
	 * @return
	 */
	public AlarmParamSet getAlarmParamSet(String alarmName) {
		AlarmParamSet paramSet = alarmParameters.get(alarmName);
		if (paramSet == null) {
			paramSet = new AlarmParamSet();
			alarmParameters.put(alarmName, paramSet);
		}
		return paramSet;
	}
	
	public void setAlarmParamSet(String alarmName, AlarmParamSet alarmParamSet) {
		alarmParameters.put(alarmName, alarmParamSet);
	}

	public boolean isPassEverything() {
		return passEverything;
	}

	public void setPassEverything(boolean passEverything) {
		this.passEverything = passEverything;
	}

}
