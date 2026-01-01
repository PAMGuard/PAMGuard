package noiseOneBand;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import alarm.AlarmCounterProvider;
import alarm.AlarmDataSource;
import noiseMonitor.NoiseDataUnit;
import noiseOneBand.alarm.OneBandAlarmProvider;
import noiseOneBand.tethys.OneBandTethysProvider;
import tethys.TethysControl;
import tethys.pamdata.TethysDataProvider;
import tethys.species.DataBlockSpeciesManager;
import tethys.species.FixedSpeciesManager;

public class OneBandDataBlock extends PamDataBlock<OneBandDataUnit> implements AlarmDataSource {

	private OneBandAlarmProvider oneBandAlarmCounter;
	private OneBandControl oneBandControl;
	private OneBandTethysProvider oneBandTethysProvider;
	private FixedSpeciesManager<OneBandDataUnit> fixedSpeciesManager;
	
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

	@Override
	public TethysDataProvider getTethysDataProvider(TethysControl tethysControl) {
		if (oneBandTethysProvider == null) {
			oneBandTethysProvider = new OneBandTethysProvider(tethysControl, oneBandControl, this);
		}
		return oneBandTethysProvider;
	}
	
	@Override
	public DataBlockSpeciesManager<OneBandDataUnit> getDatablockSpeciesManager() {
		if (fixedSpeciesManager == null) {
			fixedSpeciesManager = new FixedSpeciesManager<OneBandDataUnit>(this, -10, "anthropogenic", "noise");
		}
		return fixedSpeciesManager;
	}
	
}
