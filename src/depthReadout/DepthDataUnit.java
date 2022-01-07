package depthReadout;

import Array.sensors.ArraySensorDataUnit;
import Array.sensors.ArraySensorFieldType;
import PamguardMVC.PamDataUnit;

public class DepthDataUnit extends PamDataUnit implements ArraySensorDataUnit {
	
	private double[] depthData;
	
	private double[] rawDepthData;

	private DepthControl depthControl;

	public DepthDataUnit(DepthControl depthControl, long timeMilliseconds) {
		super(timeMilliseconds);
		this.depthControl = depthControl;
		int streamerMap = 0;
		if (depthControl.depthParameters.hydrophoneMaps != null) {
			for (int i = 0; i < depthControl.depthParameters.hydrophoneMaps.length; i++) {
				streamerMap |= depthControl.depthParameters.hydrophoneMaps[i];
			}
		}
		setChannelBitmap(streamerMap);
	}

	public double[] getDepthData() {
		return depthData;
	}

	public void setDepthData(double[] depthData) {
		this.depthData = depthData;
	}

	public double[] getRawDepthData() {
		return rawDepthData;
	}

	public void setRawDepthData(double[] rawDepthData) {
		this.rawDepthData = rawDepthData;
	}

	@Override
	public Double getField(int streamer, ArraySensorFieldType fieldType) {
		if (fieldType == ArraySensorFieldType.HEIGHT) {
			return getHeight(streamer);
		}
		else {
			return null;
		}
	}

	private Double getHeight(int streamer) {
		// find the first measurement associated with the given streamer. 
		if (depthData == null) {
			return null;
		}
		int n = Math.min(depthData.length, depthControl.depthParameters.nSensors);
		int sMap = 1<<streamer;
		for (int i = 0; i < n; i++) {
			if ((depthControl.depthParameters.hydrophoneMaps[i] & sMap) != 0) {
				return -depthData[i];
			}
		}
		return null;
	}

}
