package detectionPlotFX.rawDDPlot;

import java.util.Arrays;

import Filters.SmoothingFilter;
import PamUtils.PamUtils;
import PamguardMVC.PamDataUnit;
import PamguardMVC.RawDataHolder;
import clickDetector.ClickSpectrumParams;
import detectionPlotFX.layout.DetectionPlot;
import detectionPlotFX.layout.DetectionPlotDisplay;
import detectionPlotFX.plots.SpectrumPlot;
import detectionPlotFX.plots.WaveformPlot;
import detectionPlotFX.projector.DetectionPlotProjector;
import javafx.geometry.Side;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Rectangle;

/**
 * Implementation of the spectrum plot for any data unit that impleements raw data holder. 
 * @author Jamie Macaulay
 *
 */
public class RawSpectrumPlot extends SpectrumPlot<PamDataUnit> {
	

	/*
	 *The last stored detection 
	 */
	private PamDataUnit storedClick;
//	
	/**
	 * Storage of the spectrum
	 */
	private double[][] spectrum;
	
	/**
	 * Storage of the cepstrum
	 */
	private double[][] cepstrum;
	
	public RawSpectrumPlot(DetectionPlotDisplay detectionPlotDisplay) {
		super(detectionPlotDisplay);
	}

	
	@Override
	public void setupAxis(PamDataUnit data, double sR, DetectionPlotProjector plotProjector) {
		super.setupAxis(data, sR, plotProjector);
		
		//set the scroller minimum and maximum 
		plotProjector.setMinScrollLimit(0);
		plotProjector.setMaxScrollLimit((data.getDurationInMilliseconds()));
		plotProjector.setEnableScrollBar(true);
	}


//	@Override
//	public double[][] getPowerSpectrum(PamDataUnit data) {
//		return ((RawDataHolder) data).getDataTransforms().getPowerSpectrum(((RawDataHolder) data).getWaveData()[0].length); 
//	}
//	@Override
//	public double[][] getCepstrum(PamDataUnit data) {
//		return ((RawDataHolder) data).getDataTransforms().getCepstrum(((RawDataHolder) data).getWaveData()[0].length); 
//		//return data.getRawDataTransforms().getCepstrum(channel, cepLength)
//	}



//
//	public ClickSpectrumPlot(DetectionPlotDisplay displayPlot) {
//		super(displayPlot);
//	}
//	
//	
//	@Override
//	public String getName() {
//		return "Spectrum";
//	}
//	
	/**
	 * New click added to the display. 
	 * @param newClick
	 * @param forceRecalc
	 */
	private void newClick(PamDataUnit newClick, int bin1, int bin2, boolean forceRecalc){
		
		//System.out.println("Hello: RawSpectrumPlot");
		
		
		RawDataHolder rawDataHolder = (RawDataHolder) newClick; 
		
		if (rawDataHolder.getDataTransforms()==null) return;

		
		int nChan = PamUtils.getNumChannels(newClick.getChannelBitmap()); 
		
		//if we don;t need to recalc the spectrum then don't!
		if (!forceRecalc && newClick==storedClick) return; 
			
		storedClick=newClick;
		
		if (storedClick==null) return; 
		
		double[][] tempSpec = new double[nChan][];
		double[][] tempCep = new double[nChan][];
		
		/**
		 * Muck with FFT lengths a bit to ensure they are all the same length
		 * as dictated by the first channel returned. 
		 */
		int fftLen = 0;
		for (int iChan = 0; iChan < PamUtils.getNumChannels(storedClick.getChannelBitmap()); iChan++) {
			tempSpec[iChan] = rawDataHolder.getDataTransforms().getPowerSpectrum(iChan, bin1, bin2,  fftLen);
//			System.out.println(String.format("TempSpec click %d is %d points", storedClick.clickNumber, tempSpec[iChan].length));
			fftLen = tempSpec[iChan].length*2;
			tempCep[iChan] = rawDataHolder.getDataTransforms().getCepstrum(iChan, fftLen);
//			crossNormalise(tempSpec[iChan], tempCep[iChan]);
		}
		
		if (getSpectrumParams().channelChoice == ClickSpectrumParams.CHANNELS_SINGLE) {
			spectrum = tempSpec;
			cepstrum = tempCep;
		}
		else {
			//combine the channels as a mean
			spectrum = new double[1][];
			int specLen = tempSpec[0].length;
			spectrum[0] = Arrays.copyOf(tempSpec[0],specLen);
			for (int iChan = 1; iChan < tempSpec.length; iChan++) {
				for (int i = 0; i < specLen; i++) {
					spectrum[0][i] += tempSpec[iChan][i];
				}
			}
			cepstrum = new double[1][];
			int cepLen = tempCep[0].length;
			cepstrum[0] = Arrays.copyOf(tempCep[0], cepLen);
			for (int iChan = 1; iChan < tempCep.length; iChan++) {
				for (int i = 0; i < cepLen; i++) {
					cepstrum[0][i] += tempCep[iChan][i];
				}
			}				
		}
		
		if (getSpectrumParams().smoothPlot && getSpectrumParams() .plotSmoothing > 1) {
			//System.out.println("Hello: RawSpectrumPlot 2: " +  getSpectrumParams() .plotSmoothing); 

			for (int i = 0; i < spectrum.length; i++) {
				spectrum[i] = SmoothingFilter.smoothData(spectrum[i], getSpectrumParams() .plotSmoothing);
			}
		}
		
//		this.spectrum=tempSpec; 
//		this.cepstrum=tempCep; 
	}
//	
//
	@Override
	public double[][] getPowerSpectrum(PamDataUnit newClick, int min, int max) {
		//System.out.println("Get power spectrum: min: " + min + " max: " + max); 
		newClick(newClick,min, max, true); 
		return spectrum; 
	}


	@Override
	public double[][] getCepstrum(PamDataUnit newClick, int min, int max) {
		newClick(newClick, min, max, true);
		return cepstrum; 
	}
	
	@Override
	public void paintPlot(PamDataUnit data, GraphicsContext gc, Rectangle rectangle, DetectionPlotProjector projector, int flag) {
//		this.lastDataUnit=data;
		//System.out.println("Paint plot projector: "+ projector);
		if (flag== DetectionPlot.SCROLLPANE_DRAW) {
			
			
			double[][] waveformTemp =  ((RawDataHolder) data).getWaveData();
			
			if (waveformTemp==null) return;
			
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
	

}

