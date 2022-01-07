package detectionPlotFX.whistleDDPlot;

import whistlesAndMoans.ConnectedRegionDataUnit;
import whistlesAndMoans.WhistleMoanControl;
import detectionPlotFX.data.DDDataInfo;
import detectionPlotFX.data.DDDataProvider;
import detectionPlotFX.layout.DetectionPlotDisplay;

public class WhistleDDInfo extends DDDataInfo<ConnectedRegionDataUnit>  {

	public WhistleDDInfo(DDDataProvider dDDataProvider, WhistleMoanControl whistleMoanControl,
			DetectionPlotDisplay dDPlot) {
		
		super(dDPlot, whistleMoanControl.getWhistleToneProcess().getOutputData());
		
		//create the whistle wigner plot. 
		this.addDetectionPlot(new WhistleFFTPlot(dDPlot, whistleMoanControl));
//		this.addDetectionPlot(new WhistleWignerPlot(dDPlot));
		
		
		super.setCurrentDetectionPlot(0);

	}

}
