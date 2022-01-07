package gpl.graphfx;

import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.layout.TDGraphFX;
import gpl.GPLControlledUnit;
import gpl.GPLStateDataBlock;

public class GPLStatePlotProvider extends TDDataProviderFX {

	private GPLStateDataBlock stateDataBlock;
	private GPLControlledUnit gplControlledUnit;

	public GPLStatePlotProvider(GPLControlledUnit gplControlledUnit, GPLStateDataBlock stateDataBlock) {
		super(stateDataBlock);
		this.gplControlledUnit = gplControlledUnit;
		this.stateDataBlock = stateDataBlock;
	}

	@Override
	public TDDataInfoFX createDataInfo(TDGraphFX tdGraph) {
		return new GPLStatePlotInfo(this, gplControlledUnit, tdGraph, stateDataBlock);
	}

}
