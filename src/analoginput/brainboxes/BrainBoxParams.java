package analoginput.brainboxes;

import java.io.Serializable;
import java.util.Hashtable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import analoginput.AnalogDeviceParams;
import analoginput.AnalogRangeData;

public class BrainBoxParams implements Serializable, Cloneable, ManagedParameters {

	
	public static final long serialVersionUID = 1L;
	
	public String ipAddress = "192.168.127.254";
	
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
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

	
}
