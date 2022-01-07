package dataPlots.data;


import dataPlots.layout.TDGraph;
import PamguardMVC.PamDataBlock;

/**
 * Provider class from TD data. 
 * <br> A registry is built of providers rather than 
 * TDDataInfo's since a type of TDDataInfo may get used
 * multiple times with slightly different settings and options.
 * e.g. There will be a single click TDDataInfo, but it will be able
 * to display multiple things on the data axis - bearing, amplitude, ici, slant angle
 * etc. 
 * @author Doug Gillespie
 *
 */
public abstract class TDDataProvider {

	private PamDataBlock  parentDataBlock;

	public TDDataProvider(PamDataBlock parentDataBlock) {
		super();
		this.parentDataBlock = parentDataBlock;
	}
	
	public abstract TDDataInfo createDataInfo(TDGraph tdGraph);

	public PamDataBlock getDataBlock() {
		return parentDataBlock;
	}

	public String getName() {
		return parentDataBlock.getDataName();
	}
}
