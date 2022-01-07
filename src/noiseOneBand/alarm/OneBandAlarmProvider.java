package noiseOneBand.alarm;

import noiseOneBand.OneBandControl;
import alarm.AlarmControl;
import alarm.AlarmCounter;
import alarm.AlarmCounterProvider;

public class OneBandAlarmProvider extends AlarmCounterProvider{

	private OneBandControl oneBandControl;

	public OneBandAlarmProvider(OneBandControl oneBandControl) {
		this.oneBandControl = oneBandControl;
	}

	@Override
	protected AlarmCounter createAlarmCounter(AlarmControl alarmControl) {
		return new OneBandAlarmCounter(alarmControl, oneBandControl);
	}

}
