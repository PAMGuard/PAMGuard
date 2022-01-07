package spectrogramNoiseReduction.threshold;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

public class ThresholdParams implements Serializable, Cloneable, ManagedParameters {

	static public final long serialVersionUID = 0;
	
	public double thresholdDB = 8;
	
	public int finalOutput = SpectrogramThreshold.OUTPUT_RAW;
	

	@Override
	public ThresholdParams clone() {
		try {
			return (ThresholdParams) super.clone();
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
