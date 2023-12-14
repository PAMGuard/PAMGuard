package group3dlocaliser.dataselector;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamguardMVC.dataSelector.DataSelectParams;

public class Group3DDataSelectParams extends DataSelectParams implements Serializable, Cloneable, ManagedParameters {

	public double maxChi2 = 20;
	
	public double maxError = 100;
	
	public int minDF = 0;
	
	public static final long serialVersionUID = 0L;

	@Override
	protected Group3DDataSelectParams clone() {
		try {
			return (Group3DDataSelectParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}

}
