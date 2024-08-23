package annotationMark.fx;

import annotationMark.MarkDataBlock;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.generic.GenericDataPlotProvider;
import dataPlotsFX.layout.TDGraphFX;

public class MarkPlotProviderFX extends GenericDataPlotProvider {

	public MarkPlotProviderFX(MarkDataBlock parentDataBlock) {
		super(parentDataBlock);
	}

	@Override
	public TDDataInfoFX createDataInfo(TDGraphFX tdGraph) {
		return new MarkDataPlotInfo(this, tdGraph, getDataBlock());
	}

}

