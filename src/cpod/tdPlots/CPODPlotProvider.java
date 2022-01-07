package cpod.tdPlots;

import cpod.CPODClickDataBlock;
import cpod.CPODControl;
import PamguardMVC.PamDataBlock;
import dataPlots.data.TDDataInfo;
import dataPlots.data.TDDataProvider;
import dataPlots.layout.TDGraph;

public class CPODPlotProvider extends TDDataProvider {

	private CPODControl cpodControl;
	private CPODClickDataBlock cpodDataBlock;

	public CPODPlotProvider(CPODControl cpodControl, CPODClickDataBlock parentDataBlock) {
		super(parentDataBlock);
		this.cpodControl = cpodControl;
		this.cpodDataBlock = parentDataBlock;
	}

	@Override
	public TDDataInfo createDataInfo(TDGraph tdGraph) {
		return new CPODPlotinfo(cpodControl, this, tdGraph, cpodDataBlock);
	}

}
