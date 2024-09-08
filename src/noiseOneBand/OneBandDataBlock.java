package noiseOneBand;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import alarm.AlarmCounterProvider;
import alarm.AlarmDataSource;
import noiseOneBand.alarm.OneBandAlarmProvider;

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
