package dbht.offline;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

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
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}
	
}
