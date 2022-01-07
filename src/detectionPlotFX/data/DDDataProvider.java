package detectionPlotFX.data;

import PamguardMVC.PamDataBlock;
import detectionPlotFX.layout.DetectionPlotDisplay;


/**
* Provider class for detection display data. 
* @author Jamie Macaulay
 *
 */
public abstract class DDDataProvider {

	private PamDataBlock  parentDataBlock;

	public DDDataProvider(PamDataBlock parentDataBlock) {
		super();
		this.parentDataBlock = parentDataBlock;
	}
	
	public abstract DDDataInfo createDataInfo(DetectionPlotDisplay tdGraph);

	public PamDataBlock getDataBlock() {
		return parentDataBlock;
	}

	public String getName() {
		return parentDataBlock.getDataName();
	}
	
}
