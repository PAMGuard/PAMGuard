package dbht.offline;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

public class DbHtSummaryParams implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	public int intervalSeconds = 60;

	@Override
	protected DbHtSummaryParams clone() {
		try {
			return (DbHtSummaryParams) super.clone();
		}
		catch (CloneNotSupportedException e) {
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}
	
}
