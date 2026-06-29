package dataPlotsFX.noise;

import PamguardMVC.PamDataBlock;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.layout.TDGraphFX;
import noiseBandMonitor.NoiseBandControl;
import noiseMonitor.NoiseDataBlock;

//ST: didn't get anywhere inmplementing this.
public class NoisePlotProvider extends TDDataProviderFX{
	
	NoiseBandControl noiseBandControl;

	public NoisePlotProvider(NoiseBandControl noiseBandControl, @SuppressWarnings("rawtypes") PamDataBlock parentDataBlock) {
		super(parentDataBlock);
		this.noiseBandControl = noiseBandControl;
	}

	@Override
	public TDDataInfoFX createDataInfo(TDGraphFX tdGraph) {
		return new NoisePlotInfo(this, tdGraph, noiseBandControl, (NoiseDataBlock) getDataBlock());
	}

}
