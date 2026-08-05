package fftManager.newSpectrogram;

import PamController.PamControlledUnit;
import PamguardMVC.PamDataBlock;
import dataPlots.data.TDDataInfo;
import dataPlots.data.TDDataProvider;
import dataPlots.layout.TDGraph;

public class SpectrogramPlotProvider extends TDDataProvider {


	private PamControlledUnit fftControl;

	public SpectrogramPlotProvider(PamControlledUnit fftControl, PamDataBlock parentDataBlock) {
		super(parentDataBlock);
		this.fftControl = fftControl;
	}

	@Override
	public TDDataInfo createDataInfo(TDGraph tdGraph) {
		return new SpectrogramPlotInfo(this, fftControl, tdGraph, getDataBlock());
	}

}
