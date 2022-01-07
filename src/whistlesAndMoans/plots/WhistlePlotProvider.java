package whistlesAndMoans.plots; 

import whistlesAndMoans.WhistleMoanControl;
import dataPlots.TDDisplayProvider;
import dataPlots.data.TDDataInfo;
import dataPlots.data.TDDataProvider;
import dataPlots.layout.TDGraph;

public class WhistlePlotProvider extends TDDataProvider {

	private WhistleMoanControl wmControl;

	public WhistlePlotProvider(WhistleMoanControl wmControl) {
		super(wmControl.getWhistleToneProcess().getOutputData());
		this.wmControl = wmControl;
	}

	@Override
	public TDDataInfo createDataInfo(TDGraph tdGraph) {
		return new WhistlePlotInfo(this, wmControl, tdGraph, getDataBlock());
	}
}
