package detectionPlotFX.rawDDPlot;

import PamUtils.PamUtils;
import PamguardMVC.PamDataUnit;
import PamguardMVC.RawDataHolder;
import detectionPlotFX.layout.DetectionPlotDisplay;
import detectionPlotFX.plots.WignerPlot;

/**
 * Shows a Wigner plot of a click
 * @author Jamie Macaulay
 *
 */
public class RawWignerPlot extends WignerPlot<PamDataUnit> {

	public RawWignerPlot(DetectionPlotDisplay displayPlot) {
		super(displayPlot);
	}
	
	@Override
	public double[] getWaveform(PamDataUnit pamDetection, int chan) {
		if (pamDetection==null) return null; 		
		
	
		int chanIndex = PamUtils.getChannelPos(chan, pamDetection.getChannelBitmap());
		if (chanIndex==-1) chanIndex=0; 
		return ((RawDataHolder) pamDetection).getWaveData()[chanIndex]; 
	}
	
	@Override
	public String getName() {
		return "Wigner";
	}

}
