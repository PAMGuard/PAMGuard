package dataPlots;

import java.io.Serializable;
import java.util.ArrayList;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import dataPlots.layout.GraphParameters;
import pamScrollSystem.PamScroller;


public class TDParameters implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	public int orientation = PamScroller.HORIZONTAL;

	public double visibleTimeRange = 60;
	
	public long scrollableTimeRange = 300000L;

	public long scrollStartMillis; 
	
	public ArrayList<GraphParameters> graphParameters;

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected TDParameters clone() {
		try {
			return (TDParameters) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void addGraphParameters(GraphParameters newGraph) {
		if (graphParameters == null) {
			graphParameters = new ArrayList<GraphParameters>();
		}
		graphParameters.add(newGraph);
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DISPLAY);
		return ps;
	}

}
