package annotationMark.spectrogram;

import PamguardMVC.PamDataBlock;
import annotationMark.MarkDataUnit;
import detectionPlotFX.data.DDDataInfo;
import detectionPlotFX.layout.DetectionPlotDisplay;

/**
 * Detection display data info for spectrogram annotations. Shows a spectrogram of the
 * raw sound data underneath the annotation (if the raw data are available).
 *
 * @author Jamie Macaulay
 */
public class SpectrogramAnnotationDDInfo extends DDDataInfo<MarkDataUnit> {

	public SpectrogramAnnotationDDInfo(DetectionPlotDisplay dDPlot, PamDataBlock markDataBlock) {
		super(dDPlot, markDataBlock);

		addDetectionPlot(new SpectrogramAnnotationFFTPlot(dDPlot));

		super.setCurrentDetectionPlot(0);
	}

}
