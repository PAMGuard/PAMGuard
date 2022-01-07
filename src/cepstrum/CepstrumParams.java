package cepstrum;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

public class CepstrumParams implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	public String sourceDataBlock;
	
	public int channelMap;

	@Override
	protected CepstrumParams clone() {
		try {
			return (CepstrumParams) super.clone();
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
