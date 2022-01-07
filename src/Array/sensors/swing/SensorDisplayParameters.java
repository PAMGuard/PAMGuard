package Array.sensors.swing;

import java.io.Serializable;

import Array.sensors.ArrayDisplayParameters;

public class SensorDisplayParameters implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;

	private ArrayDisplayParameters arrayDisplayParams;
	
	public String dataSource;

	@Override
	protected SensorDisplayParameters clone() {
		try {
			SensorDisplayParameters newP = (SensorDisplayParameters) super.clone();
			newP.arrayDisplayParams = getArrayDisplayParams().clone();
			return newP;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @return the arrArrayDisplayParams
	 */
	public ArrayDisplayParameters getArrayDisplayParams() {
		if (arrayDisplayParams == null) {
			arrayDisplayParams = new ArrayDisplayParameters();
		}
		return arrayDisplayParams;
	}

	/**
	 * @param arrArrayDisplayParams the arrArrayDisplayParams to set
	 */
	public void setArrayDisplayParams(ArrayDisplayParameters arrayDisplayParams) {
		this.arrayDisplayParams = arrayDisplayParams;
	}
	
}
