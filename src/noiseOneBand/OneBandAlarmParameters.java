package noiseOneBand;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

public class OneBandAlarmParameters implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	private int returnedMeasure = 0;

	@Override
	public OneBandAlarmParameters clone() {
		try {
			return (OneBandAlarmParameters) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @return the returnedMeasure
	 */
	public int getReturnedMeasure() {
		return returnedMeasure;
	}

	/**
	 * @param returnedMeasure the returnedMeasure to set
	 */
	public void setReturnedMeasure(int returnedMeasure) {
		this.returnedMeasure = returnedMeasure;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	} 

}
