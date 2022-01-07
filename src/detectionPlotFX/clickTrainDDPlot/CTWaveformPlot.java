package detectionPlotFX.clickTrainDDPlot;

import clickTrainDetector.CTDataUnit;
import detectionPlotFX.layout.DetectionPlotDisplay;
import detectionPlotFX.plots.WaveformPlot;
import javafx.scene.layout.Pane;
import signal.Hilbert;

public class CTWaveformPlot extends WaveformPlot<CTDataUnit> {
	
	Hilbert hilbert = new Hilbert(); 

	public CTWaveformPlot(DetectionPlotDisplay detectionPlotDisplay) {
		super(detectionPlotDisplay);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getName() {
		return "Average Waveform";
	}

	@Override
	public double[][] getWaveform(CTDataUnit pamDetection) {
		return new double[][] {pamDetection.getAverageWaveform()};
	}

	@Override
	public double[][] getEnvelope(CTDataUnit pamDetection) {
		// TODO Auto-generated method stub
		return new double[][] {hilbert.getHilbert(pamDetection.getAverageWaveform())};
	}
	
	@Override
	public Pane getSettingsPane() {
		return null; //no settings pane for average waveform. 
	}

}
