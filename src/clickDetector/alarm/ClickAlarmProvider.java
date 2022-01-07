package clickDetector.alarm;

import clickDetector.ClickControl;
import alarm.AlarmControl;
import alarm.AlarmCounter;
import alarm.AlarmCounterProvider;

public class ClickAlarmProvider extends AlarmCounterProvider {

	private ClickControl clickControl;

	public ClickAlarmProvider(ClickControl clickControl) {
		this.clickControl = clickControl;
	}

	@Override
	protected AlarmCounter createAlarmCounter(AlarmControl alarmControl) {
		return new ClickAlarmCounter(alarmControl, clickControl);
	}

}
