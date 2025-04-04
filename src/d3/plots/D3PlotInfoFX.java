package d3.plots;

import PamController.PamControllerInterface;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import d3.D3Control;
import d3.D3DataBlock;
import dataPlotsFX.TDSymbolChooserFX;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.projector.TDProjectorFX;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Polygon;

public class D3PlotInfoFX extends TDDataInfoFX {

	private D3Control d3Control;
	private D3DataBlock d3DataBlock;
	private int dataChannels;
	private D3DataProviderFX d3DataProvider;

	public D3PlotInfoFX(D3Control d3Control, D3DataProviderFX d3DataProvider, TDGraphFX tdGraph, D3DataBlock pamDataBlock) {
		super(d3DataProvider, tdGraph, pamDataBlock);
		this.d3Control = d3Control;
		this.d3DataProvider = d3DataProvider;
		this.d3DataBlock = pamDataBlock;
	}

	@Override
	public Polygon drawDataUnit(int plotNumber, PamDataUnit pamDataUnit, GraphicsContext g, double scrollStart,
			TDProjectorFX tdProjector, int type) {
		// need to draw a line for this. 
		return null;
	}
	
	@Override
	public Double getDataValue(PamDataUnit pamDataUnit) {
		// TODO Auto-generated method stub
		return 0.2;
	}

	@Override
	public TDSymbolChooserFX getSymbolChooser() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void notifyChange(int changeType) {
		if (changeType == PamControllerInterface.OFFLINE_DATA_LOADED && dataChannels == 0) {
		dataChannels = d3DataProvider.createDataChannels(this);
		if (dataChannels > 0) {
//			getTdGraph().listAvailableAxisNames();
		}
	}
	}


}
