package beamformer.plot;

import PamguardMVC.DataBlock2D;
import PamguardMVC.PamDataBlock;
import beamformer.BeamFormerBaseControl;
import beamformer.continuous.BeamOGramDataBlock;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.layout.TDGraphFX;

public class BeamOGramPlotProvider extends TDDataProviderFX {

	private BeamOGramDataBlock beamDataBlock;
	private BeamFormerBaseControl beamFormerControl;

	public BeamOGramPlotProvider(BeamFormerBaseControl beamFormerBaseControl, BeamOGramDataBlock beamOGramOutput) {
		super(beamOGramOutput);
		this.beamFormerControl = beamFormerBaseControl;
		this.beamDataBlock = beamOGramOutput;
	}

	@Override
	public TDDataInfoFX createDataInfo(TDGraphFX tdGraph) {
		return new BeamOGramPlotInfo(this, tdGraph, beamFormerControl, (DataBlock2D) getDataBlock());
	}

}
