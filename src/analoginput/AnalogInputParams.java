package analoginput;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

public class AnalogInputParams implements Serializable, Cloneable, ManagedParameters {
	
	public static final long serialVersionUID = 1L;

	public String selectedType;

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
