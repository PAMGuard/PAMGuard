package analoginput.calibration;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

/**
 * Really simple calibration data class - may change at later date to 
 * be a lot more sophisticated. 
 * @author dg50
 *
 */
public class CalibrationData implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	private double[] params;
	
	public CalibrationData(double[] params) {
		this.setParams(params);
	}

	/**
	 * @return the params
	 */
	public double[] getParams() {
		return params;
	}

	/**
	 * @param params the params to set
	 */
	public void setParams(double[] params) {
		this.params = params;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}

}
