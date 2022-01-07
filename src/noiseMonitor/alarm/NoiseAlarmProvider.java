package noiseMonitor.alarm;

import noiseMonitor.NoiseControl;
import noiseMonitor.NoiseDataBlock;
import alarm.AlarmControl;
import alarm.AlarmCounter;
import alarm.AlarmCounterProvider;

public class NoiseAlarmProvider extends AlarmCounterProvider {

	private NoiseDataBlock noiseDataBlock;

	public NoiseAlarmProvider(NoiseDataBlock noiseDataBlock) {
		this.noiseDataBlock = noiseDataBlock;
		
	}

	@Override
	protected AlarmCounter createAlarmCounter(AlarmControl alarmControl) {
		return new NoiseAlarmCounter(alarmControl, noiseDataBlock);
	}

}
