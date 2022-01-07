package nmeaEmulator;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

public class NMEAEmulatorParams implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
			
	public boolean repeat;

	@Override
	protected NMEAEmulatorParams clone() {
		try {
			return (NMEAEmulatorParams) super.clone();
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
