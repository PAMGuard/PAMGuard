package detectionPlotFX.clickTrainDDPlot;

import clickTrainDetector.ClickTrainControl;
import detectionPlotFX.data.DDDataInfo;
import detectionPlotFX.data.DDDataProvider;
import detectionPlotFX.layout.DetectionPlotDisplay;

public class ClickTrainDDPlotProvider extends DDDataProvider {

	private ClickTrainControl clickTrainControl;

	public ClickTrainDDPlotProvider(ClickTrainControl clickTrainControl) {
		super(clickTrainControl.getClssfdClickTrainDataBlock());
		this.clickTrainControl=clickTrainControl;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public DDDataInfo createDataInfo(DetectionPlotDisplay displayPlot) {
		 return new ClickTrainDDataInfo(clickTrainControl,displayPlot);
	}

}
