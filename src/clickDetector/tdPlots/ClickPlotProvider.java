package clickDetector.tdPlots;

import clickDetector.ClickControl;
import PamguardMVC.PamDataBlock;
import dataPlots.data.TDDataInfo;
import dataPlots.data.TDDataProvider;
import dataPlots.layout.TDGraph;
import dataPlotsFX.data.TDScaleInfo;

public class ClickPlotProvider extends TDDataProvider {

	private ClickControl clickControl;

	public ClickPlotProvider(ClickControl clickControl, PamDataBlock parentDataBlock) {
		super(parentDataBlock);
		this.clickControl = clickControl;
	}

	@Override
	public TDDataInfo createDataInfo(TDGraph tdGraph) {
		return new ClickPlotInfo(this, clickControl, tdGraph, getDataBlock());
	}

}
