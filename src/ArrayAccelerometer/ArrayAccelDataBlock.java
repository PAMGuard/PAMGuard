package ArrayAccelerometer;

import Array.sensors.ArraySensorDataBlock;
import Array.sensors.ArraySensorFieldType;
import PamguardMVC.PamDataBlock;

public class ArrayAccelDataBlock extends PamDataBlock<ArrayAccelDataUnit> implements ArraySensorDataBlock {

	private ArrayAccelControl accelControl;

	public ArrayAccelDataBlock(String dataName, ArrayAccelControl accelControl, ArrayAccelProcess parentProcess) {
		super(ArrayAccelDataUnit.class, dataName, parentProcess, 0);
		this.accelControl = accelControl;
		setNaturalLifetime(60);
	}

	@Override
	public boolean hasSensorField(ArraySensorFieldType fieldType) {
		switch (fieldType) {
		case PITCH:
			return true;
		case ROLL:
			return true;
		default:
			return false;		
		}
	}

	@Override
	public int getStreamerBitmap() {
		return 1<<accelControl.accelParams.streamerIndex;
	}

	@Override
	public int getNumSensorGroups() {
		return 1;
	}

}
