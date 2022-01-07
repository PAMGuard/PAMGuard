package clickDetector.offlineFuncs;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

public class OfflineParameters implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	public static final int ANALYSE_LOADEDDATA = 0;
	public static final int ANALYSE_ALLDATA = 1; 
	
	public int analSelection = ANALYSE_LOADEDDATA;
	
	public boolean doClickId = true;
	
	@Override
	protected OfflineParameters clone() {
		try {
			return (OfflineParameters) super.clone();
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
