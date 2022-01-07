package detectionPlotFX.rawDDPlot;

import clickDetector.ClickControl;
import detectionPlotFX.data.DDDataInfo;
import detectionPlotFX.data.DDDataProvider;
import detectionPlotFX.layout.DetectionPlotDisplay;

public class ClickDDPlotProvider extends DDDataProvider {

	private ClickControl clickControl;

	public ClickDDPlotProvider(ClickControl clickControl) {
		super(clickControl.getClickDataBlock());
		this.clickControl=clickControl;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public DDDataInfo createDataInfo(DetectionPlotDisplay displayPlot) {
		 return new RawDDDataInfo(clickControl.getClickDataBlock(),displayPlot);
	}

}
