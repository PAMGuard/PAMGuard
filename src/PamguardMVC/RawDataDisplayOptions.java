package PamguardMVC;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

public class RawDataDisplayOptions implements Serializable, Cloneable, ManagedParameters {

	static final long serialVersionUID = 1;
	
	boolean autoScale;
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
