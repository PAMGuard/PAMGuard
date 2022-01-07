package backupmanager;

import java.io.Serializable;

import backupmanager.schedule.ScheduleState;
import backupmanager.schedule.SmallHoursSchedule;

public class BackupParams implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;

	private ScheduleState scheduleState = ScheduleState.RUNNING;
	
	private String scheduleType = SmallHoursSchedule.class.getName();

	/**
	 * @return the scheduleState
	 */
	public ScheduleState getScheduleState() {
		if (scheduleState == null) {
			scheduleState = ScheduleState.RUNNING;
		}
		return scheduleState;
	}

	/**
	 * @param scheduleState the scheduleState to set
	 */
	public void setScheduleState(ScheduleState scheduleState) {
		this.scheduleState = scheduleState;
	}
	
	
}
