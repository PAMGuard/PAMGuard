package clickDetector;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

public class WignerPlotOptions implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	public boolean limitLength = true;
	
	public int manualLength = 128;

	@Override
	protected WignerPlotOptions clone() {
		try {
			return (WignerPlotOptions) super.clone();
		} catch (CloneNotSupportedException e) {
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
