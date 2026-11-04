package annotationMark.spectrogram;

import PamUtils.Coordinate3d;
import PamUtils.PamUtils;
import PamguardMVC.PamRawDataBlock;
import annotationMark.MarkDataUnit;
import detectionPlotFX.layout.DetectionPlotDisplay;
import detectionPlotFX.plots.RawFFTPlot;
import detectionPlotFX.projector.DetectionPlotProjector;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Plots a spectrogram of the raw sound data underneath a spectrogram annotation
 * ({@link MarkDataUnit}) and draws the annotated time/frequency box over the top.
 * <p>
 * A {@link MarkDataUnit} does not hold its own raw waveform, so - unlike detections
 * that implement {@code RawDataHolder} - the raw audio has to be loaded for the time
 * span of the mark. It's loaded from the data feeding the display the mark was made
 * on (which may be a decimator) so the pop up shows what the user marked out. This
 * only works in viewer mode where the raw sound files are available.
 *
 * @author Jamie Macaulay
 */
public class SpectrogramAnnotationFFTPlot extends RawFFTPlot<MarkDataUnit> {

	private SpectrogramAnnotationModule annotationModule;

	public SpectrogramAnnotationFFTPlot(DetectionPlotDisplay displayPlot, SpectrogramAnnotationModule annotationModule) {
		super(displayPlot, displayPlot.getDetectionPlotProjector());
		this.annotationModule = annotationModule;
	}

	@Override
	public void loadRawData(MarkDataUnit dataUnit, double padding, int plotChannel) {
		//the annotation's data block has no raw source (the mark process has a null parent
		//process) so load raw data from the source of the display the mark was made on.
		PamRawDataBlock rawDataBlock = findRawDataBlock(dataUnit);
		if (rawDataBlock == null) {
			return;
		}
		int channel = PamUtils.getLowestChannel(dataUnit.getChannelBitmap());
		if (channel < 0) {
			channel = 0;
		}
		long dataStart = dataUnit.getTimeMilliseconds() - (long) padding;
		long dataEnd = dataUnit.getEndTimeInMilliseconds() + (long) padding;
		getRawDataOrder().startRawDataLoad(rawDataBlock, dataStart, dataEnd, channel);
	}

	@Override
	public void setupAxis(MarkDataUnit pamDetection, double sR, DetectionPlotProjector projector) {
		//the mark data block sample rate can be unset (0) so take it from the raw source.
		PamRawDataBlock rawDataBlock = findRawDataBlock(pamDetection);
		if (rawDataBlock != null && rawDataBlock.getSampleRate() > 0) {
			sR = rawDataBlock.getSampleRate();
		}
		super.setupAxis(pamDetection, sR, projector);
	}

	@Override
	public void paintDetections(MarkDataUnit detection, GraphicsContext graphicsContext, Rectangle windowRect,
			DetectionPlotProjector projector) {
		double[] freq = detection.getFrequency();
		if (freq == null || freq.length < 2) {
			return;
		}
		double f0 = freq[0];
		double f1 = freq[1];
		if (isUseKHz()) {
			f0 /= 1000.;
			f1 /= 1000.;
		}
		//the raw data load starts detPadding milliseconds before the mark, so the mark sits
		//detPadding into the plotted (padded) clip. The bottom axis is in seconds.
		double padSecs = getFFTParams().detPadding / 1000.;
		double startSecs = padSecs;
		double endSecs = padSecs + detection.getDurationInMilliseconds() / 1000.;

		Coordinate3d topLeft = projector.getCoord3d(startSecs, Math.max(f0, f1), 0);
		Coordinate3d botRight = projector.getCoord3d(endSecs, Math.min(f0, f1), 0);

		graphicsContext.setStroke(Color.WHITE);
		graphicsContext.setLineWidth(2);
		graphicsContext.strokeRect(topLeft.x, topLeft.y, botRight.x - topLeft.x, botRight.y - topLeft.y);
	}

	/**
	 * Find the raw data block to load sound data from. This is the source of the display
	 * the mark was made on, e.g. a decimator, rather than always the sound acquisition.
	 * @param dataUnit - the annotation data unit.
	 * @return a raw data block, or null if none are available.
	 */
	private PamRawDataBlock findRawDataBlock(MarkDataUnit dataUnit) {
		return annotationModule.findRawSourceDataBlock(dataUnit);
	}

}
