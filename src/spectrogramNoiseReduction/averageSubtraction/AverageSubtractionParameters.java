package spectrogramNoiseReduction.averageSubtraction;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

public class AverageSubtractionParameters implements Serializable, Cloneable, ManagedParameters {

	static public final long serialVersionUID = 0;
	
	public double updateConstant = 0.02;
	

	@Override
	public AverageSubtractionParameters clone() {
		try {
			return (AverageSubtractionParameters) super.clone();
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
