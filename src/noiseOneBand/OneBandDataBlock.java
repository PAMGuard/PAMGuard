package noiseOneBand;

import noiseOneBand.alarm.OneBandAlarmCounter;
import noiseOneBand.alarm.OneBandAlarmProvider;
import alarm.AlarmCounter;
import alarm.AlarmCounterProvider;
import alarm.AlarmDataSource;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

public class OneBandDataBlock extends PamDataBlock<OneBandDataUnit> implements AlarmDataSource {

	private OneBandAlarmProvider oneBandAlarmCounter;
	private OneBandControl oneBandControl;
	
	public OneBandDataBlock(String dataName, OneBandControl oneBandControl, 
			PamProcess parentProcess, int channelMap) {
		super(OneBandDataUnit.class, dataName, parentProcess, channelMap);
		this.oneBandControl = oneBandControl;
	}

	@Override
	public AlarmCounterProvider getAlarmCounterProvider() {
		if (oneBandAlarmCounter == null) {
			oneBandAlarmCounter = new OneBandAlarmProvider(oneBandControl);
		}
		return oneBandAlarmCounter;
	}

}
