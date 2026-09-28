package annotationMark.spectrogram;

import PamguardMVC.PamDataBlock;
import detectionPlotFX.data.DDDataInfo;
import detectionPlotFX.data.DDDataProvider;
import detectionPlotFX.layout.DetectionPlotDisplay;

/**
 * Provides a {@link SpectrogramAnnotationDDInfo} to the detection display so that
 * spectrogram annotations can show a spectrogram of the underlying raw data (e.g. in
 * the advanced overlay mark pop up menu).
 *
 * @author Jamie Macaulay
 */
public class SpectrogramAnnotationDDPlotProvider extends DDDataProvider {

	public SpectrogramAnnotationDDPlotProvider(PamDataBlock markDataBlock) {
		super(markDataBlock);
	}

	@Override
	public DDDataInfo createDataInfo(DetectionPlotDisplay tdGraph) {
		return new SpectrogramAnnotationDDInfo(tdGraph, getDataBlock());
	}

}
