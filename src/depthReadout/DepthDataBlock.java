package depthReadout;

import com.drew.imaging.FileType;

import Array.sensors.ArraySensorDataBlock;
import Array.sensors.ArraySensorFieldType;
import PamguardMVC.PamDataBlock;

public class DepthDataBlock extends PamDataBlock<DepthDataUnit> implements ArraySensorDataBlock {
	
	DepthProcess depthProcess;
	private DepthControl depthControl;
	
	public DepthDataBlock(String dataName, DepthControl depthControl, DepthProcess depthProcess) {
		super(DepthDataUnit.class, dataName, depthProcess, 0);
		this.depthControl = depthControl;
		this.depthProcess = depthProcess;
	}

	@Override
	public boolean hasSensorField(ArraySensorFieldType fieldType) {
		return fieldType == ArraySensorFieldType.HEIGHT;
	}

	@Override
	public int getStreamerBitmap() {
		return depthControl.depthParameters.getTotalStreamerMap();
	}

	@Override
	public int getNumSensorGroups() {
		return depthControl.depthParameters.nSensors;
	}

}
