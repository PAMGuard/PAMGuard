package beamformer.plot;

import dataPlotsFX.scrollingPlot2D.PlotParams2D;

public class BeamOGramPlotParams extends PlotParams2D {

	private double[] angleLimits = {180., 0.};
	
	public BeamOGramPlotParams() {
		
	}
	
	public double[] getAngleLimits() {
		if (angleLimits == null) {
			angleLimits = new double[2];
			angleLimits[0] = 180;
			angleLimits[1] = 0;
		}
		return angleLimits;
	}

}
