package d3.plots;

import PamguardMVC.PamDataBlock;
import d3.D3Control;
import d3.D3SensorInfo;
import dataPlots.data.TDDataInfo;
import dataPlots.data.TDDataProvider;
import dataPlots.layout.TDGraph;

public class D3DataPlotProvider extends TDDataProvider {

	private D3Control d3Control;

	public D3DataPlotProvider(D3Control d3Control, PamDataBlock parentDataBlock) {
		super(parentDataBlock);
		this.d3Control = d3Control;
	}

	@Override
	public TDDataInfo createDataInfo(TDGraph tdGraph) {
		D3PlotInfo d3Info = new D3PlotInfo(d3Control, this, tdGraph, d3Control.getD3DataBlock());
		addSensorChannels(d3Info);
		return d3Info;
	}
	
	/**
	 * Add information about all the other sensors that might be in the data stream. 
	 * @param d3Info
	 * @return
	 */
	protected int addSensorChannels(D3PlotInfo d3Info) {
		int ind = 0;
		for (D3SensorInfo sensInfo:d3Control.getD3SensorInfos()) {
			D3DataLineInfo lineInfo = new D3DataLineInfo(sensInfo.getName(), sensInfo.getCal(), 0, ind++);
			d3Info.addDataUnits(lineInfo);
		}
		return ind;
	}

}
