package dataPlotsFX.data;

import dataPlotsFX.layout.TDGraphFX;
import PamguardMVC.PamDataBlock;

/**
 * Provider class for TD data. 
 * <br> A registry is built of providers rather than 
 * TDDataInfo's since a type of TDDataInfo may get used
 * multiple times with slightly different settings and options.
 * e.g. There will be a single click TDDataInfo, but it will be able
 * to display multiple things on the data axis - bearing, amplitude, ICI, slant angle
 * etc. 
 * @author Doug Gillespie
 *
 */
@SuppressWarnings("rawtypes")
public abstract class TDDataProviderFX {

	private PamDataBlock  parentDataBlock;

	public TDDataProviderFX(PamDataBlock parentDataBlock) {
		super();
		this.parentDataBlock = parentDataBlock;
	}
	
	public abstract TDDataInfoFX createDataInfo(TDGraphFX tdGraph);

	public PamDataBlock getDataBlock() {
		return parentDataBlock;
	}

	public String getName() {
		return parentDataBlock.getLongDataName();
	}
}
