package cpod.dataPlotFX;

import PamController.PamControlledUnit;
import cpod.CPODClickDataBlock;
import cpod.CPODControl;
import cpod.CPODControl2;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.layout.TDGraphFX;

/**
 * The plot provider for CPOD data. 
 * @author au671271
 *
 */
public class CPODPlotProviderFX extends TDDataProviderFX {


	private PamControlledUnit cpodControl;
	private CPODClickDataBlock cpodDataBlock;

	public CPODPlotProviderFX(CPODControl2 cpodControl, CPODClickDataBlock parentDataBlock) {
		super(parentDataBlock);
		this.cpodControl = cpodControl;
		this.cpodDataBlock = parentDataBlock;
	}

	public CPODPlotProviderFX(CPODControl cpodControl2, CPODClickDataBlock parentDataBlock) {
		super(parentDataBlock);
		this.cpodControl = cpodControl2;
		this.cpodDataBlock = parentDataBlock;
	}

	@Override
	public TDDataInfoFX createDataInfo(TDGraphFX tdGraph) {
		return new CPODPlotInfoFX(cpodControl, this, tdGraph, cpodDataBlock);
	}

}
