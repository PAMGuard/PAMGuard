package alarm;

import java.util.ArrayList;

public abstract class AlarmCounterProvider {
	
	private ArrayList<AlarmCounter> alarmCounters = new ArrayList<>();

	public AlarmCounter getAlarmCounter(AlarmControl alarmControl) {
		AlarmCounter alarmCounter = findAlarmCounter(alarmControl);
		if (alarmCounter == null) {
			alarmCounter = createAlarmCounter(alarmControl);
			if (alarmCounter != null) {
				alarmCounters.add(alarmCounter);
			}
		}
		return alarmCounter;
	}
	
	private AlarmCounter findAlarmCounter(AlarmControl alarmControl) {
		for (AlarmCounter alarmCounter:alarmCounters) {
			if (alarmCounter.getAlarmControl() == alarmControl) {
				return alarmCounter;
			}
		}
		return null;
	}

	/**
	 * Create a specific type of alarm counter. 
	 * @param alarmControl
	 * @return an alarm counter specific to a data block. 
	 */
	protected abstract AlarmCounter createAlarmCounter(AlarmControl alarmControl);
}
