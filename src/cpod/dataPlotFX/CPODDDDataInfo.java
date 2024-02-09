package cpod.dataPlotFX;



import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import cpod.CPODClick;
import cpod.FPODReader;
import detectionPlotFX.data.DDDataInfo;
import detectionPlotFX.layout.DetectionPlotDisplay;
import detectionPlotFX.rawDDPlot.RawSpectrumPlot;
import detectionPlotFX.rawDDPlot.RawWaveformPlot;

/**
 * Data info for showing a CPOD waveform. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class CPODDDDataInfo extends DDDataInfo<CPODClick> {


	public CPODDDDataInfo(PamDataBlock<CPODClick> dataBlock,
			DetectionPlotDisplay displayPlot) {
		super(displayPlot,  dataBlock);

		//add the various click plots
		super.addDetectionPlot(new RawWaveformPlot(displayPlot));
		super.addDetectionPlot(new CPODSpectrumPlot(displayPlot));

		super.setCurrentDetectionPlot(0);
	}


	/**
	 * Plots CPOD waveform spectrum. K
	 * @author Jamie Macaulay
	 *
	 */
	class CPODSpectrumPlot extends RawSpectrumPlot {

		public CPODSpectrumPlot(DetectionPlotDisplay detectionPlotDisplay) {
			super(detectionPlotDisplay);
			// TODO Auto-generated constructor stub
		}

		@Override
		public double getSampleRate(PamDataUnit currentDetection) {
			return (double) FPODReader.FPOD_WAV_SAMPLERATE;
		}
	}
	

	/**
	 * Plots CPOD waveform spectrum. 
	 * @author Jamie Macaulay
	 *
	 */
	class CPODWaveformPlot extends RawSpectrumPlot {

		public CPODWaveformPlot(DetectionPlotDisplay detectionPlotDisplay) {
			super(detectionPlotDisplay);
			// TODO Auto-generated constructor stub
		}

		@Override
		public double getSampleRate(PamDataUnit currentDetection) {
			return (double) FPODReader.FPOD_WAV_SAMPLERATE;
		}
	}
	
	

}
