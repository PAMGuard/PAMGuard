package dataPlotsFX.rawClipDataPlot;

import clipgenerator.ClipControl;
import clipgenerator.ClipDataBlock;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.layout.TDGraphFX;

/**
 * The clip plot provider. 
 * @author Jamie Macaulay 
 *
 */
public class ClipPlotProviderFX  extends TDDataProviderFX {

	private ClipControl clipControl;

	public ClipPlotProviderFX(ClipControl clickControl, ClipDataBlock parentDataBlock) {
		super(parentDataBlock); 
		this.clipControl = clickControl;
	}

	@Override
	public TDDataInfoFX createDataInfo(TDGraphFX tdGraph) {
		return new ClipPlotInfoFX(this, clipControl, tdGraph, getDataBlock());
	}

	public ClipDataBlock getDataBlock() {
		return clipControl.getClipDataBlock();
	}

}
