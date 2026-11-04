package annotationMark.spectrogram;

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

	public SpectrogramAnnotationDDInfo(DetectionPlotDisplay dDPlot, SpectrogramAnnotationModule annotationModule) {
		super(dDPlot, annotationModule.getAnnotationDataBlock());

		addDetectionPlot(new SpectrogramAnnotationFFTPlot(dDPlot, annotationModule));

		super.setCurrentDetectionPlot(0);
	}

}
