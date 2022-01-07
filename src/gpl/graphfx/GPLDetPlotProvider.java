package gpl.graphfx;

import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.layout.TDGraphFX;
import gpl.GPLControlledUnit;
import gpl.GPLDetectionBlock;

public class GPLDetPlotProvider extends TDDataProviderFX {

	private GPLDetectionBlock gplDetectionBlock;
	private GPLControlledUnit gplControlledUnit;
	public GPLDetPlotProvider(GPLControlledUnit gplControlledUnit,
			GPLDetectionBlock gplDetectionBlock) {
		super(gplDetectionBlock);
		this.gplControlledUnit = gplControlledUnit;
		this.gplDetectionBlock = gplDetectionBlock;
	}
	
	@Override
	public TDDataInfoFX createDataInfo(TDGraphFX tdGraph) {
		return new GPLDetPlotinfo(this, gplControlledUnit, tdGraph, gplDetectionBlock);
	}
	
}
