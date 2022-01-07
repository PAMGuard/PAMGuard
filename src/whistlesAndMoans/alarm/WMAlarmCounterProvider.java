package whistlesAndMoans.alarm;

import whistlesAndMoans.WhistleMoanControl;
import alarm.AlarmControl;
import alarm.AlarmCounter;
import alarm.AlarmCounterProvider;

public class WMAlarmCounterProvider extends AlarmCounterProvider {

	private WhistleMoanControl wmControl;

	public WMAlarmCounterProvider(WhistleMoanControl wmControl) {
		this.wmControl = wmControl;
	}

	@Override
	protected AlarmCounter createAlarmCounter(AlarmControl alarmControl) {
		return new WMAlarmCounter(alarmControl, wmControl);
	}

}
