package dataPlotsFX.whistlePlotFX;

import whistlesAndMoans.ConnectedRegionDataBlock;
import whistlesAndMoans.WhistleMoanControl;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.layout.TDGraphFX;

public class WhistleMoanProviderFX extends TDDataProviderFX {

	private WhistleMoanControl wmControl;

	public WhistleMoanProviderFX(WhistleMoanControl wmControl) {
		super(wmControl.getWhistleToneProcess().getOutputData());
		this.wmControl = wmControl;
	}

	@Override
	public TDDataInfoFX createDataInfo(TDGraphFX tdGraph) {
		return new WhistlePlotInfoFX(this, wmControl, tdGraph, (ConnectedRegionDataBlock) getDataBlock());
	}
}

