package detectionPlotFX;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import dataPlotsFX.TDParametersFX;
import userDisplayFX.UserDisplayNodeParams;

/**
 * Parameters class for the Detection Plot Display
 * 
 * @author Jamie Macaulay
 *
 */
public class DetectionPlotParams extends UserDisplayNodeParams implements Cloneable, ManagedParameters  {
	
	/**
	 * The data source for the detection plot. 
	 */
	public String dataSource = null;


	/**
	 * 
	 */
	static final long serialVersionUID = 1L;


	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public DetectionPlotParams clone() {
		try {
			return (DetectionPlotParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DISPLAY);
		return ps;
	}

}
