package detectionPlotFX.clickTrainDDPlot;

import clickTrainDetector.CTDataUnit;
import detectionPlotFX.layout.DetectionPlotDisplay;
import detectionPlotFX.plots.SpectrumPlot;
import detectionPlotFX.projector.DetectionPlotProjector;
import javafx.scene.layout.Pane;

/**
 * Average spectrum plot for click trains. 
 * @author Jamie Macaulay 
 *
 */
public class CTSpectrumPlot extends SpectrumPlot<CTDataUnit> { 

	public CTSpectrumPlot(DetectionPlotDisplay detectionPlotDisplay) {
		super(detectionPlotDisplay);
	}
	
	@Override
	public void setupAxis(CTDataUnit data, double sR, DetectionPlotProjector plotProjector) {
		super.setupAxis(data, sR, plotProjector);
		plotProjector.setEnableScrollBar(false); //no scroll bar for an average spectrum - that would get way too complicated. 
	}

	@Override
	public double[][] getPowerSpectrum(CTDataUnit data, int min, int max) {
		return new double[][] {data.getAverageSpectra()};
	}

	@Override
	public double[][] getCepstrum(CTDataUnit data, int min, int max) {
		return null;
	}
	
	@Override
	public String getName() {
		return "Average Spectrum";
	}
	
	@Override
	public Pane getSettingsPane() {
		return null; //no settings pane for average waveform. 
	}
	
}