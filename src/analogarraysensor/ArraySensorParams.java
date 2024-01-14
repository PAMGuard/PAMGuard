package analogarraysensor;

import java.io.Serializable;

import Array.sensors.ArrayDisplayParameters;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

public class ArraySensorParams implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	public volatile int readIntervalMillis = 1000;
	
	private ArrayDisplayParameters arrayDisplayParameters;
	
//	public boolean storeRawValues = false;

	@Override
	protected ArraySensorParams clone()  {
		try {
			ArraySensorParams newP = (ArraySensorParams) super.clone();
			newP.arrayDisplayParameters = getArrayDisplayParameters().clone();
			return newP;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

//	/**
//	 * @return the deviceParams
//	 */
//	public AnalogSensorParams getDeviceParams() {
//		if (deviceParams == null) {
//			deviceParams = new AnalogSensorParams();
//		}
//		return deviceParams;
//	}
//
//	/**
//	 * @param deviceParams the deviceParams to set
//	 */
//	public void setDeviceParams(AnalogSensorParams deviceParams) {
//		this.deviceParams = deviceParams;
//	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}

	/**
	 * @return the arrayDisplayParameters
	 */
	public ArrayDisplayParameters getArrayDisplayParameters() {
		if (arrayDisplayParameters == null) {
			arrayDisplayParameters = new ArrayDisplayParameters();
		}
		return arrayDisplayParameters;
	}

	/**
	 * @param arrayDisplayParameters the arrayDisplayParameters to set
	 */
	public void setArrayDisplayParameters(ArrayDisplayParameters arrayDisplayParameters) {
		this.arrayDisplayParameters = arrayDisplayParameters;
	}

}
