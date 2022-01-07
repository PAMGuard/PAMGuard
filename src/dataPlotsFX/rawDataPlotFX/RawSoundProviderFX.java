package dataPlotsFX.rawDataPlotFX;

import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.layout.TDGraphFX;
import Acquisition.AcquisitionControl;
import PamguardMVC.PamRawDataBlock;


public class RawSoundProviderFX extends TDDataProviderFX {
	
	
	private AcquisitionControl soundAqControl;
	
	public RawSoundProviderFX(AcquisitionControl soundAqControl) {
		super(soundAqControl.getAcquisitionProcess().getRawDataBlock());
		this.soundAqControl=soundAqControl;
	}

	

	@Override
	public TDDataInfoFX createDataInfo(TDGraphFX tdGraph) {
		return new RawSoundDataInfo(this, tdGraph, soundAqControl, (PamRawDataBlock) getDataBlock());
	}

	

}
