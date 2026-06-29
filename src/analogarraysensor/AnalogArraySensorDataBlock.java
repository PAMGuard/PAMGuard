package analogarraysensor;

import Array.sensors.ArraySensorFieldType;
import PamguardMVC.PamDataBlock;

public class AnalogArraySensorDataBlock extends PamDataBlock<AnalogArraySensorDataUnit> implements Array.sensors.ArraySensorDataBlock{

	private ArraySensorControl analogSensorControl;

	public AnalogArraySensorDataBlock(String dataName, ArraySensorControl analogSensorControl, ArraySensorProcess arraySensorProcess) {
		super(AnalogArraySensorDataUnit.class, dataName, arraySensorProcess, 0);
		this.analogSensorControl = analogSensorControl;
		setNaturalLifetime(600);
		setMixedDirection(MIX_OUTOFDATABASE);
	}

	@Override
	public boolean hasSensorField(ArraySensorFieldType fieldType) {
		
		return true;
	}

	@Override
	public int getStreamerBitmap() {
		return getChannelMap();
	}

	@Override
	public int getNumSensorGroups() {
		return analogSensorControl.getNumSensorGroups();
	}

}
