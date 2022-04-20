package detectionPlotFX.rawDDPlot;

import PamguardMVC.PamDataUnit;
import PamguardMVC.RawDataHolder;
import detectionPlotFX.layout.DetectionPlot;
import detectionPlotFX.layout.DetectionPlotDisplay;
import detectionPlotFX.plots.FFTPlot;
import detectionPlotFX.plots.WaveformPlot;
import detectionPlotFX.projector.DetectionPlotProjector;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Rectangle;

/**
 * Plots the FFT of stored acoustic data. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class RawHolderFFTPlot extends FFTPlot<PamDataUnit> {

	public RawHolderFFTPlot(DetectionPlotDisplay displayPlot, DetectionPlotProjector projector) {
		super(displayPlot, projector);
		this.getFFTParams().detPadding=0; 
		this.enableTimeSpinner(false);
	}

	@Override
	public void paintDetections(PamDataUnit detection, GraphicsContext graphicsContext, Rectangle windowRect,
			DetectionPlotProjector projector) {		
	}

	@Override
	public double[][] getSpectrogram(PamDataUnit pamDetection, int fftLen, int fftHop, int windowType) {
		return ((RawDataHolder) pamDetection).getDataTransforms().getSpectrogram(fftLen, fftHop, windowType).getSpectrogram(0);
	}
	
	@Override
	public void paintPlot(PamDataUnit data, GraphicsContext gc, Rectangle rectangle, DetectionPlotProjector projector, int flag) {
//		this.lastDataUnit=data;
		//System.out.println("Paint plot projector: "+ projector);
		if (flag== DetectionPlot.SCROLLPANE_DRAW) {
			double[][] waveformTemp =  ((RawDataHolder) data).getWaveData();
			
//			System.out.println("Spectrum plot: " +  "  "  + projector.getMinScrollLimit()  + "  " 
//			+ projector.getMaxScrollLimit() + "  " + projector.getAxis(Side.TOP).getMaxVal() + "  wvfrm: " + WaveformPlot.getYScale(waveformTemp));
			
			WaveformPlot.paintWaveform(waveformTemp, data.getSequenceBitmap(),  gc,  rectangle,  0, waveformTemp[0].length,
					WaveformPlot.getYScale(waveformTemp), null, true,  false);	
		}
		else {
			//plot the spectrum as usual. 
			super.paintPlot(data, gc, rectangle, projector, flag);
		}
	}


//	/**
//	 * Load the raw data. This can be overridden if necessary. 
//	 * @param dataUnit - the data unit to load
//	 * @param padding - the padding. 
//	 * @param plotChannel - the plot channel. 
//	 */
//	public void loadRawData(PamDataUnit dataUnit, double padding, int plotChannel) {
//		//force set the raw data instead of loading from raw wav files. 
//		int channelPos = PamUtils.getChannelPos(plotChannel, dataUnit.getChannelBitmap()); 
//		
//		getRawDataOrder().setRawData(((RawDataHolder) dataUnit).getWaveData()[channelPos], dataUnit.getParentDataBlock().getSampleRate(), 
//				plotChannel, dataUnit.getTimeMilliseconds());
//		
//		this.reloadRaw = false; 
//		
//		//need to paint again because this is threaded - this is super messy and needs sorted. 
//		this.detectionPlotDisplay.drawCurrentUnit();
//	}
}
