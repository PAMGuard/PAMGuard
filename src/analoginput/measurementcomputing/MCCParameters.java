package analoginput.measurementcomputing;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import analoginput.AnalogDeviceParams;

public class MCCParameters implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	/**
	 * Really the device index, not the device number. 
	 */
	public int deviceNumber;

	private AnalogDeviceParams analogDeviceParams;

	/**
	 * @return the analogDeviceParams
	 */
	public AnalogDeviceParams getAnalogDeviceParams() {
		if (analogDeviceParams == null) {
			analogDeviceParams = new AnalogDeviceParams();
		}
		return analogDeviceParams;
	}

	/**
	 * @param analogDeviceParams the analogDeviceParams to set
	 */
	public void setAnalogDeviceParams(AnalogDeviceParams analogDeviceParams) {
		this.analogDeviceParams = analogDeviceParams;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}

}
