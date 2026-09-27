package cpod.dataPlotFX;

import PamguardMVC.PamDataBlock;
import cpod.CPODClickTrainDataUnit;
import detectionPlotFX.data.DDDataInfo;
import detectionPlotFX.layout.DetectionPlotDisplay;
import detectionPlotFX.plots.SpectrumPlot;
import detectionPlotFX.projector.DetectionPlotProjector;
import javafx.geometry.Side;
import javafx.scene.layout.Pane;

/**
 * Data info for showing a summary of a CPOD or FPOD click train. Click trains are super
 * detections, so this is what is shown in the super detection tab of the advanced pop up
 * menu when a CPOD detection is selected.
 *
 * @author Jamie Macaulay
 *
 */
public class CPODClickTrainDDDataInfo extends DDDataInfo<CPODClickTrainDataUnit> {

	/**
	 * The highest frequency, in kHz, to show on the frequency axis. POD clicks are all
	 * well below this, but FPOD waveforms are sampled at 2MHz, so without a limit the
	 * spectrum would be squashed into the leftmost eighth of the plot.
	 */
	private static final double MAX_PLOT_KHZ = 250;

	public CPODClickTrainDDDataInfo(PamDataBlock<CPODClickTrainDataUnit> dataBlock,
			DetectionPlotDisplay displayPlot) {
		super(displayPlot, dataBlock);

		super.addDetectionPlot(new CPODCTSpectrumPlot(displayPlot));

		super.setCurrentDetectionPlot(0);
	}

	/**
	 * Plots the average spectrum of all the clicks within a click train.
	 *
	 * @author Jamie Macaulay
	 *
	 */
	class CPODCTSpectrumPlot extends SpectrumPlot<CPODClickTrainDataUnit> {

		public CPODCTSpectrumPlot(DetectionPlotDisplay detectionPlotDisplay) {
			super(detectionPlotDisplay);
		}

		@Override
		public void setupAxis(CPODClickTrainDataUnit data, double sR, DetectionPlotProjector plotProjector) {
			super.setupAxis(data, getSampleRate(data), plotProjector);

			//no scroll bar for an average spectrum - there is nothing to scroll through.
			plotProjector.setEnableScrollBar(false);

			if (data != null) {
				plotProjector.setAxisMinMax(0, Math.min(MAX_PLOT_KHZ, getSampleRate(data)/2/1000.), Side.BOTTOM);
			}
		}

		@Override
		public double[][] getPowerSpectrum(CPODClickTrainDataUnit data, int min, int max) {
			if (data == null) {
				return null;
			}
			double[] averageSpectra = data.getAverageSpectra();
			if (averageSpectra == null) {
				return null;
			}
			return new double[][] {averageSpectra};
		}

		@Override
		public double[][] getCepstrum(CPODClickTrainDataUnit data, int min, int max) {
			return null;
		}

		@Override
		public double getSampleRate(CPODClickTrainDataUnit currentDetection) {
			if (currentDetection == null) {
				return super.getSampleRate(currentDetection);
			}
			return currentDetection.getAverageSpectraSampleRate();
		}

		@Override
		public String getName() {
			return "Average Spectrum";
		}

		@Override
		public Pane getSettingsPane() {
			return null; //no settings pane for an average spectrum.
		}

	}

}
