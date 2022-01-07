package fftManager.newSpectrogram;

import PamguardMVC.PamDataBlock;
import dataPlots.data.TDDataInfo;
import dataPlots.data.TDDataProvider;
import dataPlots.layout.TDGraph;
import fftManager.FFT;
import fftManager.PamFFTControl;

public class SpectrogramPlotProvider extends TDDataProvider {


	private PamFFTControl fftControl;

	public SpectrogramPlotProvider(PamFFTControl fftControl, PamDataBlock parentDataBlock) {
		super(parentDataBlock);
		this.fftControl = fftControl;
	}

	@Override
	public TDDataInfo createDataInfo(TDGraph tdGraph) {
		return new SpectrogramPlotInfo(this, fftControl, tdGraph, getDataBlock());
	}

}
