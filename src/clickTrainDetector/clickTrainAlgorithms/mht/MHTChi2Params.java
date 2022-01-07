package clickTrainDetector.clickTrainAlgorithms.mht;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

/**
 * Parameters class must extend this. 
 */
public class MHTChi2Params implements Cloneable, Serializable, ManagedParameters  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The absolute maximum ICI for a click train
	 */
	public double maxICI = 0.4; 
	
	/**
	 * Called whenever settings have been restored. 
	 */
	public void restoreSettings() {
		// TODO Auto-generated method stub
	}

	@Override
	public MHTChi2Params clone() {
		try {
			return (MHTChi2Params) super.clone();
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
