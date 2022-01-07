package Array.plot;

import Array.StreamerDataBlock;
import PamguardMVC.PamDataBlock;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.layout.TDGraphFX;

public class ArrayPlotProviderFX extends TDDataProviderFX {

	private StreamerDataBlock streamerDataBlock;

	public ArrayPlotProviderFX(StreamerDataBlock streamerDataBlock) {
		super(streamerDataBlock);
		this.streamerDataBlock = streamerDataBlock;
	}

	@Override
	public TDDataInfoFX createDataInfo(TDGraphFX tdGraph) {
		return new ArrayTDDataInfo(this, tdGraph, streamerDataBlock);
	}

}
