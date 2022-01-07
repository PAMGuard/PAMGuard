package difar.plots; 

import dataPlots.data.TDDataInfo;
import dataPlots.data.TDDataProvider;
import dataPlots.layout.TDGraph;
import difar.DifarControl;

public class DifarBearingPlotProvider extends TDDataProvider {

	private DifarControl difarControl;

	public DifarBearingPlotProvider(DifarControl difarControl) {
		super(difarControl.getDifarProcess().getProcessedDifarData());
		this.difarControl = difarControl;
	}

	@Override
	public TDDataInfo createDataInfo(TDGraph tdGraph) {
		return new DifarBearingPlotInfo(this, difarControl, tdGraph, getDataBlock());
	}
	
	@Override
	public String getName() {
		return getDataBlock().getDataName() + ": Bearings"; 
	};
}
