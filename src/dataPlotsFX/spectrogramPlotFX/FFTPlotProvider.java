package dataPlotsFX.spectrogramPlotFX;

import PamController.PamControlledUnit;
import PamguardMVC.PamDataBlock;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.scrollingPlot2D.Scrolling2DPlotInfo;
import fftManager.FFTDataBlock;
import fftManager.PamFFTControl;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.paint.Color;

public class FFTPlotProvider extends TDDataProviderFX {


	private PamControlledUnit fftControl;

	public FFTPlotProvider(PamControlledUnit fftControl, @SuppressWarnings("rawtypes") PamDataBlock parentDataBlock) {
		super(parentDataBlock);
		this.fftControl = fftControl;
	}

	@Override
	public TDDataInfoFX createDataInfo(TDGraphFX tdGraph) {
		return new FFTPlotInfo(this, tdGraph, fftControl, (FFTDataBlock) getDataBlock());
	}

} 


