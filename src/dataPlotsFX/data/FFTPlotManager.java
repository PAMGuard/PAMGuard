package dataPlotsFX.data;

import java.awt.geom.Path2D;

import Array.ArrayManager;
import PamUtils.PamUtils;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataUnit;
import dataPlotsFX.TDSymbolChooserFX;
import dataPlotsFX.clickPlotFX.ScrollingImageSegmenter;
import dataPlotsFX.clickPlotFX.ScrollingImageSegmenter.WritableImageSegment;
import dataPlotsFX.projector.TDProjectorFX;
import dataPlotsFX.rawClipDataPlot.FFTPlotSettings;
import dataPlotsFX.scrollingPlot2D.StandardPlot2DColours;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import pamViewFX.fxNodes.utilsFX.ColourArray;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;

/**
 * 
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
@SuppressWarnings("rawtypes") 
public abstract class FFTPlotManager {

	/**
	 * Reference to the clip plot info. 
	 */
	private TDDataInfoFX rawClipInfo;

	/**
	 * The current click detection. 
	 */
	protected PamDataUnit detection; 

	/**
	 * The line width of the FFT data unit its drawn on the frequency axis. 
	 * 
	 */
	private static double fftLineWidth=1; 

	//	/**
	//	 * The data units that need to be painted. 
	//	 */
	//	@SuppressWarnings("unused")
	//	private volatile ArrayList<PamDataUnit> clickPaintQue; 

	/**
	 * Image segmented for fast drawing of FFT's
	 */
	private FFTImageSegmenter[] fftImageSegmenter = new FFTImageSegmenter[PamConstants.MAX_CHANNELS];

	/**
	 * The current FFT colour. 
	 */
	public Color ffColor;

	/**
	 * The current colour array. 
	 */
	private Color[] colourArray;

	/**
	 * The highlight color in red. 
	 */
	private Color highlightColor = Color.RED; 


	/**
	 * Constructor for the FFT plot manager. 
	 * @param rawClipPlotInfo - the RawClipDataInfo 
	 */
	public FFTPlotManager(TDDataInfoFX rawClipPlotInfo) {
		this.rawClipInfo=rawClipPlotInfo; 
		//		update(); 
	}


	public void lastUnitDrawn(GraphicsContext g, double scrollStart, TDProjectorFX tdProjector,int plotnumber) {

		if (rawClipInfo.getScaleInfo()==null) return; 

		int plot = PamUtils.getSingleChannel(rawClipInfo.getScaleInfo().getPlotChannels()[plotnumber]); //needs to be the plot channels because the waveSegmenter is organised by channel


		//only draw the writable images once we have the last data unit. 
		if (plot>=0 && fftImageSegmenter[plot]!=null && rawClipInfo.getScaleInfoIndex()==rawClipInfo.getScaleInfos().indexOf(getFrequencyScaleInfo())) {
			fftImageSegmenter[plot].paintImages(g, tdProjector, scrollStart, 0);
		}
	}

	/**
	 * Get the FFT parameters for the display. 
	 * @return the FFT parameters. 
	 */
	public abstract FFTPlotSettings getFFTPlotParams(); 


	/**
	 * Get the frequency scale info for the TDScaleInfoFX. 
	 * @return the frequency scale info. 
	 */
	public abstract TDScaleInfo getFrequencyScaleInfo(); 


	/**
	 * Get the spectrogram from a data unit based on the FFT settings
	 * @param pamDataUnit - the data unit. 
	 * @param chanClick - the spectrogram channel - this is the realtive channel within the data unit. Not the absolute channel. 
	 * @return the absolute spectrogram data. 
	 */
	public abstract double[][] getSpectrogram(PamDataUnit pamDataUnit, int chanClick); 


	/**
	 * Draw a click FFT on a frequency time axis. FFT is a long line of the FFT above a certain cutoff. 
	 * @param plotNumber - the plot on which the FFT is drawing. 
	 * @param pamDataUnit - the click data unit to draw.
	 * @param g - the graphics handle. 
	 * @param windowRect - the window in which to draw in.  
	 * @param orientation - the orientation of the display.
	 * @param scrollStart - the scroll start in millis datenum. 
	 * @param tdProjector - the projector which dictates the the position of the unit on the screen. 
	 * @param type - type flag for the data unit. e.g. whether selected or not. 
	 * @param lastunit - indicates that this is the last unit to be drawn in this draw. 
	 * @return  a 2D path of the data if it has been drawn. Null if no draw occurred.
	 */
	public Path2D drawClipFFT(int plotNumber, PamDataUnit pamDataUnit, GraphicsContext g , double scrollStart, TDProjectorFX tdProjector, 
			int type){
		long timeMillis=pamDataUnit.getTimeMilliseconds();

		//System.out.println("plotNumber; "+ plotNumber+ " chan: "+PamUtils.getChannelArray(pamDataUnit.getChannelBitmap())[0]);
		//check if we can plot click on this plot pane. 
		if (!rawClipInfo.shouldDraw(plotNumber, pamDataUnit)) {
			return null; 
		}

		//get position on time axis
		double tC = tdProjector.getTimePix(timeMillis-scrollStart);

		//		System.out.println("TDDataInfoFX: tc: "+tC+"  timeMillis"+timeMillis+" scrollStart: "+scrollStart+" (timeMillis-scrollStart)/1000. "+((timeMillis-scrollStart)/1000.));
		if (tC < 0 || tC>tdProjector.getWidth()) {
			return null;
		}

		//cycle through the  number of channels the detection contains. 
		detection=pamDataUnit; 
		double maxFreq=rawClipInfo.getDataBlock().getSampleRate()/2; 

		int[] chanClick=PamUtils.getChannelArray(detection.getChannelBitmap());
		int[] chanPlot;
		if (rawClipInfo.getTDGraph().getCurrentScaleInfo().getPlotChannels()[plotNumber]!=0) {
			//0 indicates that all channels are can be plotted. 
			chanPlot=PamUtils.getChannelArray(rawClipInfo.getTDGraph().getCurrentScaleInfo().getPlotChannels()[plotNumber]); 
		}
		else {
			chanPlot=chanClick; 
		}

		ffColor = rawClipInfo.getSymbolChooser().getPamSymbol(pamDataUnit, type).getFillColor();
		//int col=colorToInt(Color.valueOf(clickDisplayParams.fftColor));


		if (type==TDSymbolChooserFX.HIGHLIGHT_SYMBOL_MARKED ) g.setLineWidth(2);
		else if (type==TDSymbolChooserFX.HIGHLIGHT_SYMBOL ) g.setLineWidth(6);
		else g.setLineWidth(fftLineWidth);

		g.setStroke(ffColor);

		if (chanPlot==null || chanPlot.length==0) {
			System.err.println("ClickPlotInfo: the chanPlot variable is null when painting FFT");
			return null;
		}


		Path2D path2D = null; 
		//draw click spectrum
		for (int i=0; i<chanClick.length; i++){
			//chanPlot.length is almost going to be one as generally for frequency time plot one plot pane is for one channel. 
			for (int j=0; j<chanPlot.length; j++){
				if (chanClick[i]==chanPlot[j]){

					//create a new FFT image segmenter if needed.
					if (fftImageSegmenter[chanPlot[j]]==null) {
						fftImageSegmenter[chanPlot[j]] = new FFTImageSegmenter(0, maxFreq, i);
					}

					fftImageSegmenter[chanPlot[j]].setYMinMax(0, maxFreq); //just in case the max frequency changes...
					path2D = fftImageSegmenter[chanPlot[j]].drawDataUnit(g, pamDataUnit, tdProjector, scrollStart);


					//now draw the data units if they have been marked
					if (type==TDSymbolChooserFX.HIGHLIGHT_SYMBOL_MARKED || type==TDSymbolChooserFX.HIGHLIGHT_SYMBOL) {
						g.setLineWidth(2);
						drawHighlightedFFT( g,  pamDataUnit,  tdProjector,  scrollStart, type); 
					}
				}
			}
		}
		return path2D;
	}


	/**
	 * Reset the image buffer. 
	 */
	public void clear() {
		for (int i=0; i<fftImageSegmenter.length; i++) {
			if (fftImageSegmenter[i]!=null) {
				//System.out.println("CLICK FFT BUFFER MANAGER IMAGE BUFFER RESET"); 
				fftImageSegmenter[i].resetImageBuffer();
			}
		}
	}

	/**
	 * Draw the highlight FFT. 
	 * @param g
	 * @param pamDataUnit
	 * @param durationInMilliseconds
	 * @param tdProjector
	 * @param scrollStart
	 */
	private void drawHighlightedFFT(GraphicsContext g, PamDataUnit pamDataUnit, TDProjectorFX tdProjector, double scrollStart, int type) {
		double tC = tdProjector.getTimePix(pamDataUnit.getTimeMilliseconds()-scrollStart);
		double tCEnd = tdProjector.getTimePix(pamDataUnit.getTimeMilliseconds()+pamDataUnit.getDurationInMilliseconds()-scrollStart);

		double y1= tdProjector.getYPix(0);
		double y2= tdProjector.getYPix((rawClipInfo.getDataBlock().getSampleRate()/2)); 

		//g.setStroke(Color.RED);
		g.setFill(PamUtilsFX.addColorTransparancy(getHighLightColor(), type == TDSymbolChooserFX.HIGHLIGHT_SYMBOL_MARKED ? 0.5 : 0.7));

		//System.out.println(" y1 " + y1 + " y2 " + y2); 
		//g.strokeRect(tC, y2, tCEnd-tC, y1-y2); 
		g.fillRect(tC, y2, Math.max(tCEnd-tC,1), y1-y2); 

	}

	/**
	 * 	Get the correct colour to highlight symbols with. 
	 */
	private Color getHighLightColor() {
		return highlightColor;
	}

	/**
	 * Get the 2D path for the click spectrogram plot. 
	 * @param timeMilliseconds - the time in millis
	 * @param durationInMilliseconds - the duration in millis
	 * @return the Path2D. 
	 */
	private Path2D getClipSpectrogramPath(double timeMilliseconds, Double durationInMilliseconds, TDProjectorFX tdProjector, double scrollStart) {
		double tC = tdProjector.getTimePix(timeMilliseconds-scrollStart);
		double tCEnd = tdProjector.getTimePix(timeMilliseconds+durationInMilliseconds-scrollStart);

		double y1= tdProjector.getYPix(0);
		double y2= tdProjector.getYPix((rawClipInfo.getDataBlock().getSampleRate()/2)); 

		//		double len = (y1-y2); 
		//		y1 = y2+len*0.2; 
		//		y2 = y2-len*0.2; 

		/**
		 * Bit of a hack but make the selectable path a rectangle in the middle of the FFT so it can be 
		 * slected without having to draw a box right from the top of the frequency axis right to the bottom. 
		 */
		double pix = 0.25*Math.abs(y2-y1); 

		Path2D path2D= new Path2D.Double(0,1); 
		path2D.moveTo(tC, y1-pix);
		path2D.lineTo(tC, y2+pix);
		path2D.lineTo(tCEnd, y2+pix);
		path2D.lineTo(tCEnd, y1-pix);

		//System.out.println("Path2D: get power spectrum" +  tC); 

		return path2D; 
	}


	/**
	 * Draw a spectrogram of a click. 
	 * @param g - the graphics handle
	 * @param spectrum - the click spectrum in FFT bins. 
	 * @param tC -the time in pixels 
	 * @param maxFreq - the max frequency in Hz;  
	 * @param the clip level - add the clip level. Note that we add the clip level because it is one number that
	 * takes the voltage range into account
	 * @param tdProjector - the projector 
	 */
	protected void drawClipSpectrogram(double[][] spectrogram, double timeMillis, double maxFreq,  int fftHop, int fftLength,  double clipLevel, WritableImageSegment writableImage){
		//do not draw

		if (spectrogram==null) {
			System.err.println("ClickFFTPlotManager.drawClickSpectorgram: spectrogram is null" ); 
			return; 
		}
		int tc = -1;

		double timeMillisFFT; 
		int newtc; 
		//if zero just draw one line to be efficient
		//		System.out.println("SpectrogramLength: "  + spectrogram.length); 

		//maybe compress image? 

		//the number of pixel slices that each FFT line trake 
		int nslices = (int) Math.ceil((1000*fftLength/rawClipInfo.getDataBlock().getSampleRate())/writableImage.getScrollingPLot2DSegmenter().getMillisePerPixel()); 

		for (int i=0; i<spectrogram.length; i++) {

			timeMillisFFT = (timeMillis + (i*fftHop*1000.)/rawClipInfo.getDataBlock().getSampleRate()); 



			newtc = (int) writableImage.getScrollingPLot2DSegmenter().getImageXPixels(timeMillisFFT, writableImage); 
			//			if (tc!=newtc) {
			//plot the spectrogram line

			//System.out.println("Plot spectrogram: timeMillisFFT " +  i +  "  "  + timeMillisFFT + " tc: " + newtc + " fftLength: " + fftLength + " fftHop: " + fftHop ); 

			tc=newtc; 

			//double[] minmax = PamUtils.getMinAndMax(spectrogram); 
			//System.out.println("Plot spectrogram: tc " + tc + " minmax: " +  20*Math.log10(minmax[0])+clipLevel + " " + 20*Math.log10(minmax[1])+ " Clip level: " + clipLevel); 


			//how many lines in the image does the FFT take up?

			for (int j=0; j<writableImage.getHeight(); j++) {

				//what is the spectrum value for the height?
				int spec = (int) ((j/(double) writableImage.getHeight())*spectrogram[i].length); 

				if (spec>spectrogram[i].length-1) spec = spectrogram[i].length-1; 

				//find the correct colour for the spectrogram value and draw
				for (int k=0; k<nslices; k++) {
					if ((tc+k)>=writableImage.getWidth()) continue; 
					writableImage.getPixelWriter().setColor(tc+k, j, getSpectrumColour(spectrogram[i][spec], clipLevel, rawClipInfo.getDataBlock().getSampleRate(), fftLength));
				}
			}
			//			}

		}
	}


	/**
	 * Get the colour for the spectrum. 
	 * @param spectrumValue - the spectrum value
	 * @param the clip level - add the clip level. Note that we add the clip level because it is one number that
	 * takes the voltage range into account. 
	 * @return the new color. 
	 */
	private Color getSpectrumColour(double spectrumValue, double clipLevel, float sampleRate, int fftLength) {
		//TODO - need to add microphone and hydrophone senstivities here. 
		//System.out.println("Clip level: " + clipLevel); 
		if (colourArray==null) {
			update();
		}
		double dBcol = fftAmplitude2dB(spectrumValue,  sampleRate, clipLevel, fftLength, true); 
		return colourArray[StandardPlot2DColours.getColourIndex(dBcol, getFFTPlotParams().freqAmplitudeRange[0], 
				getFFTPlotParams().freqAmplitudeRange[1], colourArray.length)]; 
	}


	/**
	 * Draw the data unit on a writeable image segment. This function can be overriden for more options on drawing. 
	 * @param g - the graphics context
	 * @param pamDataUnit - the data unit to draw. 
	 * @param writableImage - the writiable image. 
	 * @param tdProjector - the projector. 
	 * @param scrolLStart - the start of the scroller. 
	 * @param chanClick - the channel of the data unit to plot. This is the relative, not the absolute channel  
	 */
	public void drawDataUnit(GraphicsContext g,PamDataUnit pamDataUnit, WritableImageSegment writableImage,
			TDProjectorFX tdProjector, double scrolLStart, int chanClick) {
		//the writable image covers the entire frequency spectrum...
		int x1 = (int) writableImage.getScrollingPLot2DSegmenter().getImageXPixels(pamDataUnit.getTimeMilliseconds(), writableImage); 

		if (x1>writableImage.getWidth()-1) {
			System.err.println("Warning the value of X could not be drawn on the selcted image" + x1); 
		}

		double[][] spectrum= getSpectrogram(pamDataUnit, chanClick); 

		if (spectrum==null) {
			return;
		}

		double clipLevel = ArrayManager.getArrayManager().getCurrentArray().getClipLevel(PamUtils.getChannelArray(pamDataUnit.getChannelBitmap())[chanClick]); 

		//draw the spectrogram. 
		drawClipSpectrogram(spectrum,  pamDataUnit.getTimeMilliseconds(),   
				writableImage.getScrollingPLot2DSegmenter().getMaxY(), getFFTPlotParams().fftHop, getFFTPlotParams().fftLength, clipLevel, writableImage);
	}


	private static double sqrt2 = Math.sqrt(2.0);


	/**
	 * Convert the amplitude of fft data into a spectrum level measurement in
	 * dB re 1 micropacal / sqrt(Hz).
	 * @param fftAmplitude magnitude of the fft data (not the magnitude squared !)
	 * @param the clip level in dB re 1uPa. 
	 * @param sampleRate sample rate - this needs to be sent, since this function is 
	 * often called from decimated data, in which case the sample rate will be different. 
	 * @param fftLength length of the FFT (needed for Parsevals correction) 
	 * @return spectrum level amplitude.
	 */
	public static double fftAmplitude2dB(double fftAmplitude, float sampleRate, double clipLevel, int fftLength, boolean isSquared){
		if (isSquared) {
			fftAmplitude = Math.sqrt(fftAmplitude);
		}
		// correct for PArsevel (1/sqrt(fftLength and for the fact that the data were summed
		// over a fft length which requires an extra 1/sqrt(fftLength) correction.
		fftAmplitude /= fftLength;
		// allow for negative frequencies
		fftAmplitude *= sqrt2;
		// thats the energy in an nHz bandwidth. also need bandwidth correction to get
		// to spectrum level data
		double binWidth = sampleRate / fftLength;
		fftAmplitude /= Math.sqrt(binWidth);
		double dB = 20*Math.log10(fftAmplitude)+clipLevel; 
		return dB;
	}

	//	/**
	//	 * Stroke a vertical line in the writable image. 
	//	 * @param writableImage - the writable image
	//	 * @param x1 - x start of the line to stroke.
	//	 * @param y1 - y start of the line to stroke.  
	//	 * @param x2 - x end of the line to stroke. 
	//	 * @param y2 - 
	//	 */
	//	private void strokeLine(WritableImageSegment writableImage, int x1, int y1, int y2, Color color) {
	//		//weird. y==y2 does not work but i<y2 does?
	//		for (int i=y1; i<y2; i++) {
	//			writableImage.getPixelWriter().setColor((int) Math.min(x1, writableImage.getWidth()-1), i, color);
	//		}
	//	}


	/**
	 * Implementation of the writable image segmenter. 
	 * @author Jamie Macaulay
	 *
	 */
	class FFTImageSegmenter extends ScrollingImageSegmenter {

		/**
		 * The relative channel to plot (index within the channel group, not the absolute channel)
		 */
		private int chanClick;

		//		int count =0; 

		public FFTImageSegmenter(double minY, double maxY, int chanClick) {
			super(minY, maxY);
			this.chanClick = chanClick; //which click channel to plot. int count 
		}


		@Override
		public void drawADataUnit(GraphicsContext g, PamDataUnit pamDataUnit, WritableImageSegment writableImage,
				TDProjectorFX tdProjector, double scrolLStart) {
			//the writable image covers the entire frequency spectrum...
			FFTPlotManager.this.drawDataUnit( g,  pamDataUnit,  writableImage,
					tdProjector,  scrolLStart, chanClick); 
			//it is plotted automatically on the graph. . 

		}


		@Override
		public Path2D getPath2D(PamDataUnit pamDataUnit, WritableImageSegment writableImage,
				TDProjectorFX tdProjector, double scrollStart) {
			//the writable image covers the entire frequency spectrum...
			int x1 = (int) this.getImageXPixels(pamDataUnit.getTimeMilliseconds(), writableImage); 

			if (x1>writableImage.getWidth()-1) {
				System.err.println("Warning the value of X could not be drawn on the selcted image" + x1); 
			}

			//			if (count%100==0) {
			//				System.out.println("The number of image segments is: " + writableImage.getScrollingPLot2DSegmenter().getImageSegments().size()); 
			//
			//			}
			//			count++

			return getClipSpectrogramPath(pamDataUnit.getTimeMilliseconds(), pamDataUnit.getDurationInMilliseconds(),  tdProjector, scrollStart); 

		}


	}

	/**
	 * Called whenever settings are updated. 
	 */
	public void update() {
		colourArray=ColourArray.createStandardColourArray(256, getFFTPlotParams().colourMap).getColours();
	}



}

