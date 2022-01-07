package noiseOneBand.offline;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

public class OneBandSummaryParams implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	public int intervalSeconds = 60;

	@Override
	protected OneBandSummaryParams clone() {
		try {
			return (OneBandSummaryParams) super.clone();
		}
		catch (CloneNotSupportedException e) {
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}
	
}
