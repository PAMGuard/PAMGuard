package rawDeepLearningClassifier.ddPlotFX;


import detectionPlotFX.data.DDDataInfo;
import detectionPlotFX.layout.DetectionPlotDisplay;
import detectionPlotFX.rawDDPlot.RawHolderFFTPlot;
import detectionPlotFX.rawDDPlot.RawSpectrumPlot;
import detectionPlotFX.rawDDPlot.RawWaveformPlot;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.DLDetection;

/**
 * Data info for the raw data info. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class RawDLDDDataInfo extends DDDataInfo<DLDetection> {

	
	public RawDLDDDataInfo(DLControl dlControl,
			DetectionPlotDisplay displayPlot) {
		super(displayPlot,  dlControl.getDLClassifyProcess().getDLDetectionDatablock());
		
		//add the various click plots
		super.addDetectionPlot(new RawWaveformPlot(displayPlot));
		super.addDetectionPlot(new RawSpectrumPlot(displayPlot));
		super.addDetectionPlot(new RawHolderFFTPlot(displayPlot, displayPlot.getDetectionPlotProjector()));


		super.setCurrentDetectionPlot(0);
	}


}
