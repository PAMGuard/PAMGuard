package detectionPlotFX.whistleDDPlot;

import whistlesAndMoans.WhistleMoanControl;
import detectionPlotFX.data.DDDataInfo;
import detectionPlotFX.data.DDDataProvider;
import detectionPlotFX.layout.DetectionPlotDisplay;

public class WhistleDDPlotProvider extends DDDataProvider {
	
	private WhistleMoanControl whistleMoanControl;

	public WhistleDDPlotProvider(WhistleMoanControl whistleMoanControl) {
		super(whistleMoanControl.getWhistleToneProcess().getOutputData());
		this.whistleMoanControl=whistleMoanControl;
	}

	@Override
	public DDDataInfo createDataInfo(DetectionPlotDisplay displayPlot) {
		 return new WhistleDDInfo(this, whistleMoanControl,displayPlot);
	}

}
