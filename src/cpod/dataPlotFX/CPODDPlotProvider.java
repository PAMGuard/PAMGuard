package cpod.dataPlotFX;


import PamguardMVC.PamDataBlock;
import cpod.CPODClick;
import cpod.CPODControl2;
import detectionPlotFX.data.DDDataProvider;
import detectionPlotFX.layout.DetectionPlotDisplay;

public class CPODDPlotProvider extends DDDataProvider {

	
	private PamDataBlock<CPODClick> cpodDataBlock;

	public CPODDPlotProvider(CPODControl2 dlControl, PamDataBlock<CPODClick> parentDataBlock) {
		super(parentDataBlock);
		this.cpodDataBlock= parentDataBlock; 
	}

	@Override
	public CPODDDDataInfo createDataInfo(DetectionPlotDisplay dddisplay) {
		return new CPODDDDataInfo(cpodDataBlock, dddisplay);
	}

}

