package detectionPlotFX.clickTrainDDPlot;

import clickTrainDetector.CTDataUnit;
import clickTrainDetector.ClickTrainControl;
import detectionPlotFX.data.DDDataInfo;
import detectionPlotFX.data.DDDataProvider;
import detectionPlotFX.layout.DetectionPlotDisplay;

/**
 * The DetectionDisplay information for ClickTrain detections. 
 * 
 * @author Jamie Macaulay
 *
 */
public class ClickTrainDDataInfo  extends DDDataInfo<CTDataUnit> {

	public ClickTrainDDataInfo(ClickTrainControl clickTrainControl, DetectionPlotDisplay displayPlot) {
		super(displayPlot, clickTrainControl.getClickTrainDataBlock());
		
		//average waveform plot
		//super.addDetectionPlot(new CTWaveformPlot(displayPlot));

		//average spectrum plot
		super.addDetectionPlot(new CTSpectrumPlot(displayPlot));

		//waterfall spectrogram. 
		super.addDetectionPlot(new WaterfallSpecPlot<CTDataUnit>(displayPlot, displayPlot.getDetectionPlotProjector()));
		
		//set current detection plot
		super.setCurrentDetectionPlot(0);
	}

}
