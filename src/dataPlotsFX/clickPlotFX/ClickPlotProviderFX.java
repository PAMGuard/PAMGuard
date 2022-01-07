package dataPlotsFX.clickPlotFX;

import clickDetector.ClickControl;
import PamguardMVC.PamDataBlock;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.layout.TDGraphFX;

@SuppressWarnings("rawtypes") 
public class ClickPlotProviderFX extends TDDataProviderFX {

	private ClickControl clickControl;

	public ClickPlotProviderFX(ClickControl clickControl, PamDataBlock parentDataBlock) {
		super(parentDataBlock);
		this.clickControl = clickControl;
	}

	@Override
	public TDDataInfoFX createDataInfo(TDGraphFX tdGraph) {
		return new ClickPlotInfoFX(this, clickControl, tdGraph, getDataBlock());
	}

}
