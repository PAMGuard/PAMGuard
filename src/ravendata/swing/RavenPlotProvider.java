package ravendata.swing;

import PamguardMVC.PamDataBlock;
import dataPlots.data.TDDataInfo;
import dataPlots.data.TDDataProvider;
import dataPlots.layout.TDGraph;

public class RavenPlotProvider extends TDDataProvider {

	public RavenPlotProvider(PamDataBlock parentDataBlock) {
		super(parentDataBlock);
		// TODO Auto-generated constructor stub
	}

	@Override
	public TDDataInfo createDataInfo(TDGraph tdGraph) {
		// TODO Auto-generated method stub
		return new RavenDataInfo(this, tdGraph, getDataBlock());
	}

}
