package generalDatabase.lineplots;

import PamguardMVC.PamDataBlock;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.layout.TDGraphFX;

public class LinePlotDataProvider extends TDDataProviderFX {

	private LinePlotControl linePlotControl;

	public LinePlotDataProvider(LinePlotControl linePlotControl, PamDataBlock parentDataBlock) {
		super(parentDataBlock);
		this.linePlotControl = linePlotControl;
	}

	public LinePlotControl getLinePlotControl() {
		return linePlotControl;
	}

	@Override
	public TDDataInfoFX createDataInfo(TDGraphFX tdGraph) {
		return new LinePlotDataInfo(this, tdGraph, getDataBlock());
	}

}
