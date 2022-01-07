package dataPlotsFX.data.generic;

import PamguardMVC.PamDataBlock;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.layout.TDGraphFX;

/**
 * A very generic data plot provider. Possibly too generic, so most plotting things
 * will want to override this I'd guess. 
 * @author Doug Gillespie
 *
 */
public class GenericDataPlotProvider extends TDDataProviderFX {
	
	public GenericDataPlotProvider(PamDataBlock parentDataBlock) {
		super(parentDataBlock);
	}

	@Override
	public TDDataInfoFX createDataInfo(TDGraphFX tdGraph) {
		return new GenericDataPlotInfo(this, tdGraph, getDataBlock());
	}


}
