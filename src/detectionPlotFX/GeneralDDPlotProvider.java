package detectionPlotFX;

import PamguardMVC.PamDataBlock;
import detectionPlotFX.data.DDDataInfo;
import detectionPlotFX.data.DDDataProvider;
import detectionPlotFX.layout.DetectionPlotDisplay;

public class GeneralDDPlotProvider extends DDDataProvider {

	public GeneralDDPlotProvider(PamDataBlock parentDataBlock) {
		super(parentDataBlock);
		// TODO Auto-generated constructor stub
	}

	@Override
	public DDDataInfo createDataInfo(DetectionPlotDisplay tdGraph) {
		return null;
	}

}
