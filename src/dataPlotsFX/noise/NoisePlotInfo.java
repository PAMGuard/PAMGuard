package dataPlotsFX.noise;

import PamguardMVC.DataBlock2D;
import PamguardMVC.PamDataUnit;
import dataPlotsFX.TDSymbolChooserFX;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.data.TDScaleInfo;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.scrollingPlot2D.PlotParams2D;
import dataPlotsFX.scrollingPlot2D.Scrolling2DPlotInfo;
import noiseBandMonitor.NoiseBandControl;
import noiseMonitor.NoiseDataBlock;

//ST: didn't get anywhere inmplementing this.
public class NoisePlotInfo extends TDDataInfoFX{

	public NoisePlotInfo(TDDataProviderFX tdDataProvider, TDGraphFX tdGraph, NoiseBandControl noiseBandControl, NoiseDataBlock dataBlock) {
		super(tdDataProvider, tdGraph, dataBlock);
	}

	@Override
	public Double getDataValue(PamDataUnit pamDataUnit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TDSymbolChooserFX getSymbolChooser() {
		// TODO Auto-generated method stub
		return null;
	}

}
