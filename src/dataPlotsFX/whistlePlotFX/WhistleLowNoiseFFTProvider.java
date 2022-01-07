package dataPlotsFX.whistlePlotFX;

import whistlesAndMoans.WhistleMoanControl;
import PamguardMVC.PamDataBlock;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.scrollingPlot2D.Scrolling2DPlotInfo;
import dataPlotsFX.spectrogramPlotFX.FFTPlotInfo;
import fftManager.FFTDataBlock;

public class WhistleLowNoiseFFTProvider  extends TDDataProviderFX {
	
	private WhistleMoanControl whsitleMoanControl;

	public WhistleLowNoiseFFTProvider(WhistleMoanControl whsitleMoanControl, @SuppressWarnings("rawtypes") PamDataBlock parentDataBlock) {
		super(parentDataBlock);
		this.whsitleMoanControl=whsitleMoanControl; 
	}

	@Override
	public TDDataInfoFX createDataInfo(TDGraphFX tdGraph) {
		return new FFTPlotInfo(this, tdGraph, null, (FFTDataBlock) getDataBlock());
	}


}
