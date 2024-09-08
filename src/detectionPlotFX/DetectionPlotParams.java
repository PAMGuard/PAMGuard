package detectionPlotFX;

import java.util.HashMap;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import userDisplayFX.UserDisplayNodeParams;

/**
 * Parameters class for the Detection Plot Display
 * 
 * @author Jamie Macaulay
 *
 */
public class DetectionPlotParams extends UserDisplayNodeParams implements Cloneable, ManagedParameters  {


	/**
	 * 
	 */
	static final long serialVersionUID = 3L;
	
	/**
	 * The data source for the detection plot. 
	 */
	public String dataSource = null;
	
	/**
	 * True to show the scroll bar. 
	 */
	public boolean showScrollBar = true;

	/**
	 * Saves which data axis is used for which data block. The key is the data block long name and the 
	 * result is the name of the plot e.g. waveform. In this way users can set how they want the data plots to display
	 * different types of data units and the dispay types are saved on PAMGuard opne and close. 
	 */
	public HashMap<String , String > dataAxisMap = new HashMap<String , String >();


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
