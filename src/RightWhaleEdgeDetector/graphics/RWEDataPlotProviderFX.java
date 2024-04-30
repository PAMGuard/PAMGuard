package RightWhaleEdgeDetector.graphics;

import RightWhaleEdgeDetector.RWEDataBlock;
import RightWhaleEdgeDetector.RWEProcess;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.layout.TDGraphFX;

public class RWEDataPlotProviderFX extends TDDataProviderFX {

	private RWEDataBlock rweDataBlock;
	private RWEProcess rweProcess;

	public RWEDataPlotProviderFX(RWEProcess rweProcess, RWEDataBlock rweDataBlock) {
		super(rweDataBlock);
		this.rweProcess = rweProcess;
		this.rweDataBlock = rweDataBlock;
	}

	@Override
	public TDDataInfoFX createDataInfo(TDGraphFX tdGraph) {
		return new RWEDataPlotinfoFX(this, rweProcess, tdGraph, rweDataBlock);
	}

}
