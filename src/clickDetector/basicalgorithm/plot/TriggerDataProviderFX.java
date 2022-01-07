package clickDetector.basicalgorithm.plot;

import clickDetector.basicalgorithm.TriggerBackgroundDataBlock;
import clickDetector.basicalgorithm.TriggerBackgroundHandler;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.layout.TDGraphFX;

public class TriggerDataProviderFX extends TDDataProviderFX {

	private TriggerBackgroundHandler triggerBackgroundHandler;
	private TriggerBackgroundDataBlock triggerBackgroundDataBlock;

	public TriggerDataProviderFX(TriggerBackgroundHandler triggerBackgroundHandler, 
			TriggerBackgroundDataBlock triggerBackgroundDataBlock) {
		super(triggerBackgroundDataBlock);
		this.triggerBackgroundHandler = triggerBackgroundHandler;
		this.triggerBackgroundDataBlock = triggerBackgroundDataBlock;
	}

	@Override
	public TDDataInfoFX createDataInfo(TDGraphFX tdGraph) {
		return new TriggerDataPlotFX(this, tdGraph, triggerBackgroundHandler, triggerBackgroundDataBlock);
	}

}
