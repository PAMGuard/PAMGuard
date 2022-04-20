package dataPlotsFX.clickPlotFX;

import java.util.Arrays;

import Array.ArrayManager;
import PamUtils.PamUtils;
import PamguardMVC.PamDataUnit;
import clickDetector.ClickDetection;
import clickDetector.ClickSpectrogram;
import clickDetector.tdPlots.ClickDetSymbolManager;
import clickDetector.tdPlots.ClickSymbolOptions;
import dataPlotsFX.clickPlotFX.ScrollingImageSegmenter.WritableImageSegment;
import dataPlotsFX.data.FFTPlotManager;
import dataPlotsFX.data.TDScaleInfo;
import dataPlotsFX.projector.TDProjectorFX;
import dataPlotsFX.rawClipDataPlot.FFTPlotSettings;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;



/**
 * Handles efficiently plotting click FFT's on the frequency axis. 
 * This can be used to visualise longer clicks, for example, bat calls. 
 * 
 * <p>
 * 
 * Warning: this display does not work if there are not unique UID values for clicks. 
 * 
 * @author Jamie Macaulay
 *
 */
public class ClickFFTPlotManager2 extends FFTPlotManager  {
	

	/**
	 * Reference to the click plot info. 
	 */
	private ClickPlotInfoFX clickPlotInfoFX;

	/**
	 * The click plot info. 
	 * @param clickPlotInfoFX
	 */
	public ClickFFTPlotManager2(ClickPlotInfoFX clickPlotInfoFX) {
		super(clickPlotInfoFX);
		this.clickPlotInfoFX=clickPlotInfoFX; 
		this.update(); 
	}

	@Override
	public FFTPlotSettings getFFTPlotParams() {
		return clickPlotInfoFX.clickDisplayParams;
	}

	@Override
	public TDScaleInfo getFrequencyScaleInfo() {
		return clickPlotInfoFX.frequencyInfo;
	}


	@Override
	public void drawDataUnit(GraphicsContext g, PamDataUnit pamDataUnit, WritableImageSegment writableImage,
			TDProjectorFX tdProjector, double scrolLStart, int chanClick) {
		//the writable image covers the entire frequency spectrum...
		int x1 = (int) writableImage.getScrollingPLot2DSegmenter().getImageXPixels(pamDataUnit.getTimeMilliseconds(), writableImage); 

		if (x1>writableImage.getWidth()-1) {
			System.err.println("Warning the value of X could not be drawn on the selcted image" + x1); 
		}

		//			if (count%100==0) {
		//				System.out.println("The number of image segments is: " + writableImage.getScrollingPLot2DSegmenter().getImageSegments().size()); 
		//
		//			}
		//			count++;

//		int colourChoice = ((ClickSymbolOptions) clickPlotInfoFX.getClickSymbolChooser().getSymbolChooser().getSymbolOptions()).colourChoice;

		if (!clickPlotInfoFX.clickDisplayParams.thresholdFFT) {

			//System.out.println("Draw click: " + pamDataUnit.getUID()); 
			//the click detection will handle efficiently storing and recalcuting the spectrogram based on FFT length and hop. 
			ClickSpectrogram clickSpectrogram = ((ClickDetection) detection).getClickSpectrogram(clickPlotInfoFX.getClickDisplayParams().fftLength, clickPlotInfoFX.getClickDisplayParams().fftHop); 

			if (clickSpectrogram.getSpectrogram(chanClick)==null) {
				System.err.println("The click spectrum is null: "); 
				return; 
			}
			double[][] spectrum=clickSpectrogram.getSpectrogram(chanClick); 		
			
			double clipLevel = ArrayManager.getArrayManager().getCurrentArray().getClipLevel(PamUtils.getChannelArray(pamDataUnit.getChannelBitmap())[chanClick]); 
			
			//System.out.println("clipLevel: " + clipLevel); 

			drawClipSpectrogram(spectrum,  pamDataUnit.getTimeMilliseconds(), 
					writableImage.getScrollingPLot2DSegmenter().getMaxY(), clickPlotInfoFX.getClickDisplayParams().fftHop,
					clickPlotInfoFX.getClickDisplayParams().fftLength, clipLevel, writableImage);
		}
		else {
			double[] spectrum=((ClickDetection) detection).getPowerSpectrum(chanClick, clickPlotInfoFX.isViewer() ? true : false);
			spectrum=((ClickDetection) detection).getPowerSpectrum(chanClick, clickPlotInfoFX.isViewer() ? true : false);
			drawClickFFT(Arrays.copyOf(spectrum, spectrum.length),  pamDataUnit.getTimeMilliseconds(),  writableImage.getScrollingPLot2DSegmenter().getMaxY(),  writableImage);
		}
	}
	
	
	/**
	 * Draw a single click FFT
	 * @param g - the graphics handle
	 * @param spectrum - the click spectrum in FFT bins. 
	 * @param tC -the time in pixels 
	 * @param maxFreq - the max frequency in Hz;  
	 * @param tdProjector - the projector 
	 */
	public void drawClickFFT(double[] spectrum, double timeMillis, double maxFreq, WritableImageSegment writableImage){
		//do not draw
		if (clickPlotInfoFX.getClickDisplayParams().fftCutOf==1) return;

		int tc = (int) writableImage.getScrollingPLot2DSegmenter().getImageXPixels(timeMillis, writableImage); 

		//		System.out.println("tC: " + tc + " timeMillis: " + PamCalendar.formatDBDateTime(timeMillis, true) +
		//				"  millisPerPixel: " + writableImage.getScrollingPLot2DSegmenter().millisPerPixel); 

		double[] minMax=PamUtils.getMinAndMax(spectrum);
		double cutOff=clickPlotInfoFX.getClickDisplayParams().fftCutOf*minMax[1];  
		double binSize = maxFreq/(double) spectrum.length; 
		//		System.out.println("cutOff: "+cutOff+ " fftCutOf: " + clickDisplayParams.fftCutOf);

		int y1, y2; 

		y1=writableImage.getScrollingPLot2DSegmenter().getImageYPixels(0, writableImage); 
		y2=writableImage.getScrollingPLot2DSegmenter().getImageYPixels(maxFreq, writableImage); 
		//		System.out.println("Y2 and y1: " + y2 + "  " + y1 + " Color: " +ffColor.getRed()) ; 

		//		writableImage.getPixelWriter().setColor(tc, 199,  Color.BLUE); 
		//		writableImage.getPixelWriter().setColor(tc, 200,  Color.BLUE); 
		//		writableImage.getPixelWriter().setColor(tc, 201,  Color.BLUE); 

		//if zero just draw one line to be efficient
		if (clickPlotInfoFX.getClickDisplayParams().fftCutOf==0){
			y1=writableImage.getScrollingPLot2DSegmenter().getImageYPixels(0, writableImage); 
			y2=writableImage.getScrollingPLot2DSegmenter().getImageYPixels(maxFreq, writableImage); 

			strokeLine(writableImage, tc, y1, y2, ffColor); 
		}

		//draw a fraction of the fft
		for (int i=0; i<spectrum.length-1; i++){
			if (spectrum[i]<cutOff) {
				continue; 
			}
			y1=writableImage.getScrollingPLot2DSegmenter().getImageYPixels(binSize*i, writableImage); 
			y2=writableImage.getScrollingPLot2DSegmenter().getImageYPixels(binSize*(i+1), writableImage); 			

			strokeLine(writableImage, tc, y1, y2, ffColor); 
		}
	}
	
	
	/**
	 * Stroke a vertical line in the writable image. 
	 * @param writableImage - the writable image
	 * @param x1 - x start of the line to stroke.
	 * @param y1 - y start of the line to stroke.  
	 * @param x2 - x end of the line to stroke. 
	 * @param y2 - 
	 */
	private void strokeLine(WritableImageSegment writableImage, int x1, int y1, int y2, Color color) {
		//weird. y==y2 does not work but i<y2 does?
		for (int i=y1; i<y2; i++) {
			writableImage.getPixelWriter().setColor((int) Math.min(x1, writableImage.getWidth()-1), i, color);
		}
	}

	@Override
	public double[][] getSpectrogram(PamDataUnit pamDataUnit, int chanClk) {
		//this is not used because drawDataUnit has been overriden
		return null;
	}



}
