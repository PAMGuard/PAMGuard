package alarm.actions.tast;

import alarm.AlarmControl;
import alarm.AlarmDataUnit;
import alarm.actions.serial.AlarmSerialAction;

public class TastAction extends AlarmSerialAction {

	public TastAction(AlarmControl alarmControl) {
		super(alarmControl);
	}

	@Override
	public String getActionName() {
		return "TAST Trigger";
	}


	@Override
	protected String createAlarmString(AlarmDataUnit alarmDataUnit) {
		// TODO. To define serial string to send to TAST device once we hear back 
		// from GenusWave. 
		return null;
	}

}
