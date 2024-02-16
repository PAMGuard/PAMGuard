package cpod.dataPlotFX;



import PamView.symbol.PamSymbolChooser;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import cpod.CPODClick;
import cpod.FPODReader;
import dataPlotsFX.TDManagedSymbolChooserFX;
import dataPlotsFX.TDSymbolChooserFX;
import dataPlotsFX.clickPlotFX.ClickSymbolChooserFX;
import dataPlotsFX.data.TDDataInfoFX;
import detectionPlotFX.data.DDDataInfo;
import detectionPlotFX.layout.DetectionPlotDisplay;
import detectionPlotFX.projector.DetectionPlotProjector;
import detectionPlotFX.rawDDPlot.RawSpectrumPlot;
import detectionPlotFX.rawDDPlot.RawWaveformPlot;
import javafx.geometry.Side;
import pamViewFX.fxNodes.PamSymbolFX;

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

	@Override
	 public float getHardSampleRate() {
		return FPODReader.FPOD_WAV_SAMPLERATE;
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
		public void setupAxis(PamDataUnit data, double sR, DetectionPlotProjector plotProjector) {
			super.setupAxis(data, sR, plotProjector);
			
			CPODClick click = (CPODClick) data;
			double lenMS = (1000.*click.getWaveData()[0].length)/FPODReader.FPOD_WAV_SAMPLERATE;
			//set the scroller minimum and maximum 
			plotProjector.setMinScrollLimit(0);
			//need this othewriwse the multiple sample rates relaly screw things up. 
			plotProjector.setMaxScrollLimit(lenMS);
			plotProjector.setEnableScrollBar(true);
			
			
			plotProjector.setAxisMinMax(0, 250, Side.BOTTOM);
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
