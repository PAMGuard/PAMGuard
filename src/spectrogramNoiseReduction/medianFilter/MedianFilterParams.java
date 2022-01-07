package spectrogramNoiseReduction.medianFilter;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

public class MedianFilterParams implements Serializable, Cloneable, ManagedParameters {

	static public final long serialVersionUID = 0;
	
	public int filterLength = 61;
	

	@Override
	public MedianFilterParams clone() {
		try {
			return (MedianFilterParams) super.clone();
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
