package bearinglocaliser.display;

import detectionPlotFX.plots.simple2d.Simple2DPlot;

/**
 * Can plot lines or scaled images or both. 
 * @author Doug Gillespie
 *
 */
public class Bearing2DPlot extends Simple2DPlot implements BearingDataDisplay {

	
	public Bearing2DPlot(String name) {
		super(name);
	}

	public void setRightAxisVisible(boolean showRight) {
		getPlotPane().setAxisVisible(false, showRight, true, true);
	}
	
}
