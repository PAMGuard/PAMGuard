package difar.plots; 

import dataPlots.data.TDDataInfo;
import dataPlots.data.TDDataProvider;
import dataPlots.layout.TDGraph;
import difar.DifarControl;

public class DifarIntensityPlotProvider extends TDDataProvider {

	private DifarControl difarControl;

	public DifarIntensityPlotProvider(DifarControl difarControl) {
		super(difarControl.getDifarProcess().getProcessedDifarData());
		this.difarControl = difarControl;
	}

	@Override
	public TDDataInfo createDataInfo(TDGraph tdGraph) {
		return new DifarIntensityPlotInfo(this, difarControl, tdGraph, getDataBlock());
	}
	
	@Override
	public String getName() {
		return getDataBlock().getDataName() + ": Intensity"; 
	};
}
