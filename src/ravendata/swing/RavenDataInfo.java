package ravendata.swing;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import dataPlots.data.TDDataInfo;
import dataPlots.data.TDDataProvider;
import dataPlots.data.TDSymbolChooser;
import dataPlots.layout.TDGraph;

public class RavenDataInfo extends TDDataInfo {

	public RavenDataInfo(TDDataProvider tdDataProvider, TDGraph tdGraph, PamDataBlock dataBlock) {
		super(tdDataProvider, tdGraph, dataBlock);
	}

	@Override
	public Double getDataValue(PamDataUnit pamDataUnit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TDSymbolChooser getSymbolChooser() {
		return null;//getDataBlock().getPamSymbolManager().getSymbolChooser("TDPlot" + getTdGraph().getGraphNumber(), null);
	}

}
