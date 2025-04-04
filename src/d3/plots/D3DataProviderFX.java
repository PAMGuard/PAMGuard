package d3.plots;

import java.util.ArrayList;

import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import d3.D3Control;
import d3.D3DataBlock;
import d3.D3SensorInfo;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.data.TDScaleInfo;
import dataPlotsFX.layout.TDGraphFX;

public class D3DataProviderFX extends TDDataProviderFX {

	private D3DataBlock d3DataBlock;
	private D3Control d3Control;

	public D3DataProviderFX(D3Control d3Control, D3DataBlock d3DataBlock) {
		super(d3DataBlock);
		this.d3Control = d3Control;
		this.d3DataBlock = d3DataBlock;
	}

	@Override
	public TDDataInfoFX createDataInfo(TDGraphFX tdGraph) {
		D3PlotInfoFX dataInfo = new D3PlotInfoFX(d3Control, this, tdGraph, d3DataBlock);
		createDataChannels(dataInfo);
		return dataInfo;
	}

	public int createDataChannels(D3PlotInfoFX dataInfo) {
		int ind = 0;
		ArrayList<D3SensorInfo> sensorInfos = d3Control.getD3SensorInfos();
		for (D3SensorInfo sensInfo:sensorInfos) {
			// need min max type units. 
			dataInfo.addScaleInfo(new TDScaleInfo(-1, 1, ParameterType.AMPLITUDE, ParameterUnits.NONE));
		}
		return ind;
	}

}
