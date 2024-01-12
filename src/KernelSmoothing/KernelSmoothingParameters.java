package KernelSmoothing;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

public class KernelSmoothingParameters implements Serializable, Cloneable, ManagedParameters {

	static public final long serialVersionUID = 0;
	
	public int fftBlockIndex = 0;
	
	public int channelList = 1;

	@Override
	protected KernelSmoothingParameters clone() {
		try {
			return (KernelSmoothingParameters) super.clone();
		}
		catch (CloneNotSupportedException Ex) {
			return null;
		}
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}

}
