package ravendata.fx;

import PamguardMVC.PamDataBlock;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.data.generic.GenericDataPlotProvider;
import dataPlotsFX.layout.TDGraphFX;
import ravendata.RavenDataBlock;

public class RavenPlotProviderFX extends GenericDataPlotProvider {

	public RavenPlotProviderFX(RavenDataBlock parentDataBlock) {
		super(parentDataBlock);
	}

	@Override
	public TDDataInfoFX createDataInfo(TDGraphFX tdGraph) {
		return new RavenDataPlotInfo(this, tdGraph, getDataBlock());
	}

}
