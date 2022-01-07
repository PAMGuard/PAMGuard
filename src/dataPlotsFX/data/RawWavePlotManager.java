package dataPlotsFX.data;

import java.awt.geom.Path2D;

import PamUtils.PamArrayUtils;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataUnit;
import PamguardMVC.RawDataHolder;
import dataPlots.data.TDSymbolChooser;
import dataPlotsFX.TDSymbolChooserFX;
import dataPlotsFX.clickPlotFX.ScrollingPlotSegmenter;
import dataPlotsFX.clickPlotFX.ScrollingPlotSegmenter.PlotSegment;
import dataPlotsFX.projector.TDProjectorFX;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;

/**
 * Manages plotting raw data.
 * <p>
 * The raw segmenter holds a 2D array of data for a segment of a plot for each
 * channel. Each plot segment keeps a record of which data units have been
 * added. This allows the segments to be reused for plotting without having to
 * iterate through data units again.
 * <p>
 * An example use case is plotting raw waveform data. At high sample rates,
 * each raw bin can only be plotted as the minimum and maximum bin withing a
 * time segment representing a single pixel. iterating through all raw data bins
 * and calculating this each time a plot needs repainting is processor
 * intensive. So the bin data can be processed and added to PlotSegments, then
 * the PlotSegments can be reused until either the plot scale time scale changes
 * sufficiently that new segments are required (e.g. zooming in) or the plot
 * moves to a completely new time section of data.
 * 
 * @author Jamie Macaulay
 *
 */
public abstract class RawWavePlotManager {

	/**
	 * Reference to the TDDataInfoFX. 
	 */
	private TDDataInfoFX rawClipInfo;

	/**
	 * The current detection
	 */
	private PamDataUnit detection;

	private double fftLineWidth = 1; 
	
	private double circleWidth = 3; 

	/**
	 * Image segmented for fast drawing of FFT's
	 */
	private RawWavPlotsegmenter[] waveImageSegmenter = new RawWavPlotsegmenter[PamConstants.MAX_CHANNELS];

	/**
	 * Raw wave plot manager. 
	 * @param rawClipInfo -0 the raw clip. 
	 */
	public RawWavePlotManager(TDDataInfoFX rawClipInfo) {
		this.rawClipInfo=rawClipInfo; 
	}


	public void lastUnitDrawn(GraphicsContext g, double scrollStart, TDProjectorFX tdProjector,int plotnumber) {
		//System.out.println("Plot number: " + plotnumber); 
		//only draw the writable images once we have the last data unit. 
		
		if (rawClipInfo.getScaleInfo()==null) return; 
		
		int plot = PamUtils.getSingleChannel(rawClipInfo.getScaleInfo().getPlotChannels()[plotnumber]); //needs to be the plot channels because the waveSegmenter is organised by channel
		
		
		//System.out.println("Plot plot: " + plot + "  " + plotnumber);
		
		if (plot>=0 && waveImageSegmenter[plot]!=null && rawClipInfo.getScaleInfoIndex()==rawClipInfo.getScaleInfos().indexOf(getWaveScaleInfo())) {
			waveImageSegmenter[plot].paintImages(g, tdProjector, scrollStart, 0);
		}
	}
	
	/**
	 * Reset the image buffer. 
	 */
	public void clear() {
		for (int i=0; i<waveImageSegmenter.length; i++) {
			if (waveImageSegmenter[i]!=null) {
				waveImageSegmenter[i].resetImageBuffer();
			}
		}
	}


	/**
	 * Get the wave scale info. 
	 * @return the wave scale info.
	 */
	public abstract TDScaleInfo getWaveScaleInfo(); 
	
	/**
	 * Get the color
	 * @param pamDataUnit
	 * @return
	 */
	public abstract Color getColor(PamDataUnit pamDataUnit, int type);

	/**
	 * Implementation of the writable image segmenter. 
	 * @author Jamie Macaulay
	 *
	 */
	class RawWavPlotsegmenter extends ScrollingPlotSegmenter {

		/**
		 * The relative channel to plot (index within the channel group, not the absolute channel)
		 */
		private int chanClick;

		//		int count =0; 

		public RawWavPlotsegmenter(int chanClick) {
			super(); 
			this.chanClick = chanClick; //which click channel to plot. int count 
		}


		@Override
		public void addPlotData(PamDataUnit pamDataUnit, PlotSegment plotSegment,
				TDProjectorFX tdProjector, double scrollStart) {
			//the writable image covers the entire frequency spectrum.
			RawWavePlotManager.this.addRawData(pamDataUnit,  plotSegment,
					tdProjector,  scrollStart, chanClick, getColor(pamDataUnit, TDSymbolChooser.NORMAL_SYMBOL)); 
			//it is plotted automatically on the graph. 

		}


		@Override
		public Path2D getPath2D(PamDataUnit pamDataUnit, PlotSegment writableImage,
				TDProjectorFX tdProjector, double scrollStart) {
			//the writable image covers the entire frequency spectrum...
			
			//this provides the selectable part of the clicks. 
			int x1 = (int) this.getImageXPixels(pamDataUnit.getTimeMilliseconds(), writableImage); 

			if (x1>writableImage.getWidth()-1) {
				System.err.println("Warning the value of X could not be drawn on the selcted image" + x1); 
			}

		
			//So want to make a box around a click
			double tC = tdProjector.getTimePix(pamDataUnit.getTimeMilliseconds()-scrollStart);
			double tCEnd = tdProjector.getTimePix(pamDataUnit.getTimeMilliseconds()+pamDataUnit.getDurationInMilliseconds()-scrollStart);

			double[] minmax = PamArrayUtils.minmax(((RawDataHolder) pamDataUnit).getWaveData()[chanClick]); 

			double y1= tdProjector.getYPix(minmax[0]);
			double y2= tdProjector.getYPix(minmax[1]); 
			
			Path2D path2D= new Path2D.Double(0,1); 
			path2D.moveTo(tC, y1);
			path2D.lineTo(tC, y2);
			path2D.lineTo(tCEnd, y2);
			path2D.lineTo(tCEnd, y1);
			
			return path2D; 
		}


		@Override
		public void paintPlotData(GraphicsContext g, PlotSegment plotSegmentData, TDProjectorFX tdProjector,
				double scrollStart) {

			//paint the raw data on the display. this is just a series of lines representing the sound data. 
			//System.out.println("Ok, paint the raw wave data: !");  
			
	
			g.setLineWidth(fftLineWidth);

			double y1, y2, x1, x2, prevx1, prevy1; 


			double tcMillis = plotSegmentData.getMillisStart()-scrollStart;

			double samplesPerPixel = (plotSegmentData.getScrollingPLot2DSegmenter().getMillisePerPixel()/1000.)*rawClipInfo.getDataBlock().getSampleRate();

//			System.out.println(" Plot Segment START: " + PamCalendar.formatDBDateTime((long) plotSegmentData.getMillisStart(),true)); 

			prevx1=-1; 
			prevy1=-1; 
			long lastMillis =-1; 
			for (int i =0; i<plotSegmentData.getData().length; i++) {				

				//how many pixels to draw on the line. 
				y1=tdProjector.getYPix(plotSegmentData.getData()[i][0]); 
				
//				if (Double.isNaN(y1)) {
//					System.out.println("Temp Y is NaN"); 
//					y1= Math.random()*100; 
//				}
				
				g.setStroke(plotSegmentData.getColor()[i]);
				g.setFill(plotSegmentData.getColor()[i]);

				//start pixel
				x1=tdProjector.getTimePix(tcMillis + i*millisPerPixel);

				if (x1>=0  && x1<=tdProjector.getGraphTimePixels()) {

					if (samplesPerPixel<1 && !Double.isNaN(y1)) {
						//System.out.println("x1: " + x1 + " x2: " + x2 +  " y1: " + y1 + " y2: " + y2 + " millisPerPixel: " + millisPerPixel +  "  tcMillis: "  +tcMillis  ); 
						g.fillOval(x1-circleWidth/2, y1-circleWidth/4, circleWidth, circleWidth);
						if (prevx1>0) {
							g.strokeLine(prevx1, prevy1, x1, y1);
						}
						prevx1 = x1; 
						prevy1 = y1; 

					}
					else {
						//lower y1 value. 
						y2=tdProjector.getYPix(plotSegmentData.getData()[i][1]); 
						//end pixel
						lastMillis=  ((long) (tcMillis + (i+1)*millisPerPixel)); 
						
						
						x2=tdProjector.getTimePix(tcMillis + (i+1)*millisPerPixel);
						
//						System.out.println(PamCalendar.formatDBDateTime((long) (lastMillis+scrollStart), true) +" " +  x2 + " " + y2); 

						
						//plot the data chunk
						for (double j =x1; j<x2; j++) {
							g.strokeLine(j, y1, j, y2);
						}
					}
				}
			}
			
//			System.out.println("plotSegmentData millis last:  " + PamCalendar.formatDBDateTime((long) plotSegmentData.getMillisEnd(), true) 
//			+ " last drawn: " + PamCalendar.formatDBDateTime((long) (lastMillis+scrollStart), true) + " Millis per pixel: " + millisPerPixel); 

//			//FIXME
//			// for testing - plors where the windows are. 
//			double tC = tdProjector.getTimePix(tcMillis);
//			double tCEnd = tdProjector.getTimePix(tcMillis + plotSegmentData.getWidth()*millisPerPixel);
//			y1=tdProjector.getYPix(-1); 
//			y2=tdProjector.getYPix(1); 
//			g.setStroke(Color.ORANGE);
//			g.strokeLine(tC, y1, tC, y2);
//			g.setStroke(Color.RED);
//			g.strokeLine(tCEnd-2, y1, tCEnd-2, y2);
//			g.strokeText(" Pixels: " + plotSegmentData.getPixelStart() + " tCEnd: " + tCEnd, tC+10, y1+10); 


		}
	}


	/**
	 * 	Draw the highlighted data. 
	 * @param g
	 * @param pamDataUnit
	 * @param tdProjector
	 * @param scrollStart
	 * @param type
	 */
	private void drawHighlightedRaw(GraphicsContext g, PamDataUnit pamDataUnit, TDProjectorFX tdProjector,
			double scrollStart, int type, int chan) {
		
		
		double tC = tdProjector.getTimePix(pamDataUnit.getTimeMilliseconds()-scrollStart);
		double tCEnd = tdProjector.getTimePix(pamDataUnit.getTimeMilliseconds()+pamDataUnit.getDurationInMilliseconds()-scrollStart);

		double[] minmax = PamArrayUtils.minmax(((RawDataHolder) pamDataUnit).getWaveData()[PamUtils.getChannelPos(chan, pamDataUnit.getChannelBitmap())]); 
		
		
		double y1= tdProjector.getYPix(minmax[0]);
		double y2= tdProjector.getYPix(minmax[1]); 

		//g.setStroke(Color.RED);
		g.setFill(PamUtilsFX.addColorTransparancy(getHighLightColor(pamDataUnit), type == TDSymbolChooserFX.HIGHLIGHT_SYMBOL_MARKED ? 0.5 : 0.7));
		g.setStroke(getHighLightColor(pamDataUnit));

		//System.out.println(" y1 " + y1 + " y2 " + y2); 
		//g.strokeRect(tC, y2, tCEnd-tC, y1-y2); 
		g.fillRect(tC, y2, Math.max(tCEnd-tC,1), y1-y2); 
		g.strokeRect(tC, y2, Math.max(tCEnd-tC,1), y1-y2); 

	}

	
	protected Color getHighLightColor(PamDataUnit pamDataUnit) {
		return Color.CYAN; 
	}


	/**
	 * Draw the raw data. If type == TDSymbolChooserFX.HIGHLIGHT_SYMBOL_MARKED then the highlighted symbol is drawn using the fgraphics context. 
	 * If a normal draw then the raw data is simply passed to the plot segmenter which handles drawing later during a paintPlotData call. 
	 * @param plotNumber - the plot number i..e the plot on the TDGraphFX.
	 * @param pamDataUnit - the PAM data unit.
	 * @param g - the graphics context. 
	 * @param scrollStart - the scroll start. 
	 * @param tdProjector - the tdProjector for converting data values to pixel positions. 
	 * @param type - the type of drawing. E.g. HIGHLIGHT
	 * @return the Path2D which describes the data unit. 
	 */
	public Path2D drawRawData(int plotNumber, PamDataUnit pamDataUnit, GraphicsContext g, double scrollStart,
			TDProjectorFX tdProjector, int type) {
		
		//System.out.println("PlotRawData: plotNumber; "+ plotNumber+ " chan: "+PamUtils.getChannelArray(pamDataUnit.getChannelBitmap())[0]);
		//check if we can plot click on this plot pane. 
		if (!rawClipInfo.shouldDraw(plotNumber, pamDataUnit)) {
			//System.out.println("Shoudl not draw!");

			return null; 
		}

		long timeMillis=pamDataUnit.getTimeMilliseconds();

		//get position on time axis
		double tC = tdProjector.getTimePix(timeMillis-scrollStart);

		//System.out.println("TDDataInfoFX: tc: "+tC+"  timeMillis"+timeMillis+" scrollStart: "+scrollStart+" (timeMillis-scrollStart)/1000. "+((timeMillis-scrollStart)/1000.));
		
		//if a normal symbol then this is passed to the plot segmenter regardless of it's time. 
		//TODO - could this introduce bugs somehow?
		if ((tC < 0 || tC>tdProjector.getWidth()) && type!=TDSymbolChooserFX.NORMAL_SYMBOL) {
			return null;
		}

		//cycle through the  number of channels the detection contains. 
		detection=pamDataUnit; 
		//double maxFreq=rawClipInfo.getDataBlock().getSampleRate()/2; 

		int[] chanClick=PamUtils.getChannelArray(detection.getChannelBitmap());
	
		int[] chanPlot;;
		if (rawClipInfo.getTDGraph().getCurrentScaleInfo().getPlotChannels()[plotNumber]!=0) {
			//0 indicates that all channels are can be plotted. 
			chanPlot=PamUtils.getChannelArray(rawClipInfo.getTDGraph().getCurrentScaleInfo().getPlotChannels()[plotNumber]); 
		}
		else {
			chanPlot=chanClick; 
		}

		//System.out.println("rawClipInfo.getTDGraph().getCurrentScaleInfo().getPlotChannels(): " + rawClipInfo.getTDGraph().getCurrentScaleInfo().getPlotChannels()[plotNumber]); 

		if (type==TDSymbolChooserFX.HIGHLIGHT_SYMBOL_MARKED ) g.setLineWidth(2);
		else if (type==TDSymbolChooserFX.HIGHLIGHT_SYMBOL ) g.setLineWidth(6);
		else g.setLineWidth(fftLineWidth);

		if (chanPlot==null || chanPlot.length==0) {
			System.err.println("RawWavePlotManager: the chanPlot variable is null when painting wave data");
			return null;
		}

		//System.out.println("RawWavePlotManager: chanPlot: " + chanClick[0] + "  chanplot: " + chanPlot[0]);
		Path2D path2D = null; 
		//draw click spectrum
		for (int i=0; i<chanClick.length; i++){
			//chanPlot.length is almost going to be one as generally for frequency time plot one plot pane is for one channel. 
			for (int j=0; j<chanPlot.length; j++){

				if (chanClick[i]==chanPlot[j]){

					//create a new FFT image segmenter if needed.
					if (waveImageSegmenter[chanPlot[j]]==null) {
						waveImageSegmenter[chanPlot[j]] = new RawWavPlotsegmenter(i);
					}		
					
					//System.out.println("Ok, Add plot data: ! " + chanPlot[j]);  

					path2D = waveImageSegmenter[chanPlot[j]].addPlotData(pamDataUnit, tdProjector, scrollStart);

					//now draw the data units if they have been marked
					if (type==TDSymbolChooserFX.HIGHLIGHT_SYMBOL_MARKED || type==TDSymbolChooserFX.HIGHLIGHT_SYMBOL) {
						g.setLineWidth(2);
						drawHighlightedRaw( g,  pamDataUnit,  tdProjector,  scrollStart, type, chanPlot[j]); 
					}
				}
			}
		}
		return path2D;
	}



	/**
	 * Draw the data unit. 
	 * @param g = the graphics context.  
	 * @param pamDataUnit - the raw data unit
	 * @param plotSegment - the plot segment containing data to plot. 
	 * @param tdProjector - the td projector.  
	 * @param scrolLStart - the scroll data. 
	 * @param chanClick - the channle clicks. 
	 */
	public void addRawData(PamDataUnit pamDataUnit, PlotSegment plotSegment,
			TDProjectorFX tdProjector, double scrollStart, int chanClick, Color color) {

		RawDataHolder rawDataHolder = (RawDataHolder) pamDataUnit; 

		//we assume that the waveform starts at the start time and that the bins are the sample rate of the datablock. 
		double[][] rawData = rawDataHolder.getWaveData(); 
/**
 * 
 * Do not use start samples as does not work with short wav files. 
 */
//		//lt's make sure that we assign a start sample to the start of the writable image. 
//		if (plotSegment.getExtraInfo()==null) {
//			//need to set the start sample of the writable image. This means that all data units are referenced using samples 
//			//instead if millis and so at when zoomed will be correctly drawn with respect to each other. 
//
//			///number of samples from the start of the writable image to the start of the data unit. 
//			double samples = (pamDataUnit.getParentDataBlock().getSampleRate()/1000.)*(pamDataUnit.getTimeMilliseconds()-plotSegment.getMillisStart());
//			
//			Double imageStartSample = pamDataUnit.getStartSample()-samples; 
//			plotSegment.setExtraInfo(imageStartSample);
//		}
		
		//lt's make sure that we assign a nanosecond to the start of the writable image. 
		if (plotSegment.getExtraInfo()==null) {
			//need to set the start sample of the writable image. This means that all data units are referenced using samples 
			//instead if millis and so at when zoomed will be correctly drawn with respect to each other. 

			///number of samples from the start of the writable image to the start of the data unit. 
			long nanoSecondsFromStart = (long) ((pamDataUnit.getTimeMilliseconds()-plotSegment.getMillisStart())*1000*1000);
			
			Long imageStartNano = pamDataUnit.getTimeNanoseconds()- nanoSecondsFromStart; 
			plotSegment.setExtraInfo(imageStartNano);
		}

		if (rawData==null) {
			return; 
		}

		addRawData(rawData[chanClick], pamDataUnit.getTimeMilliseconds(), pamDataUnit.getTimeNanoseconds(),
				pamDataUnit.getParentDataBlock().getSampleRate(), plotSegment, color); 

	}



	/**
	 * Draw the raw data on the image. 
	 * @param rawData - the raw data to draw. 
	 * @param timeMilliseconds - the time in milliseconds. 
	 * @param plotSegment - the writable image. 
	 */
	private void addRawData(double[] rawData, long timeMilliseconds, long timeNano, float sR,  PlotSegment plotSegment, Color color) {

		//the way we do this is we apply a start sample
		//the problem with PAMGuard in this instance is that the time in millis is a very rough measure of stuff at high sample rates. 	
		double samplesPerPixel = (plotSegment.getScrollingPLot2DSegmenter().getMillisePerPixel()/1000.)*sR; 
		
		/**
		 * Ok this is a bit of a hack. here's the issue. We use the start of the plot segment as the time reference to keep everything 
		 *time aligned. That works well, except if there is a new wav file - the sample and nano times change and thus the times all mess up. It's not easy to 
		 *know where the start of the new wav file was so this is a problem 
		 */
		
		double timeDiffSamples; 
		
		//we take two different approaches depending on whether we are very zoomed or not. We hope that no one is using ultra small 
		//continuous wav files... this is a little hackey but can't think of a better way
		if (samplesPerPixel<2.0) {
			timeDiffSamples = (double) sR*(timeNano - plotSegment.getExtraInfo().longValue()) / 1.e9;
		}
		else {
			timeDiffSamples = (double) sR*(timeMilliseconds - plotSegment.getMillisStart()) / 1.e3;
		}
 
		//we draw a line of the minimum and maximum pixels			
		int drawPixel =  (int) ((timeDiffSamples)/samplesPerPixel); 
		
		int drawStart = drawPixel;
//		
//		System.out.println("Add raw data: " + PamCalendar.formatDBDateTime(timeMilliseconds, true) 
//		+ " drawPixel: " + drawPixel + " timeNano: " + (timeNano - plotSegment.getExtraInfo().longValue())); 

		if (samplesPerPixel<1) {
			//simply save one data point for the raw data. 
			for (int i=0; i<rawData.length; i++) {

				drawPixel =  (int) ((timeDiffSamples+i)/samplesPerPixel); 

				if (drawPixel>=0 && drawPixel<plotSegment.getData().length) {
					//System.out.println("Draw a pixel: "+ drawPixel); 
					plotSegment.getData()[drawPixel][0] = (float) rawData[i];
					plotSegment.getColor()[drawPixel] = color; //FIXME - if multipe data units in one pixel then want the non default colour...

				}
			}
		}
		else {
			//the amplitude limits 
			double minX = Double.MAX_VALUE; 
			double maxX= -Double.MAX_VALUE; 

			//now bin the
			int count = 0; 
			for (int i=0; i<rawData.length; i++) {
				
				
				//had to do this because the count>samples pixel ignores the cumulative remainder and this does not work well. 
				//TODO - does this need fixed in PG?
				if (((int) Math.floor(i/samplesPerPixel))>(drawPixel-drawStart)) {


					//				if (count>=samplesPerPixel) {	//FIXME - issue here with rounding...
					if (drawPixel>=0 && drawPixel<plotSegment.getData().length) {
						//add the previous pixel to the plot segment. 

						//it's possible the same pixel will drawn on more than once. in this case must ensure that values are added if current value <max or >min . 
						if (Float.isNaN(plotSegment.getData()[drawPixel][0]) || plotSegment.getData()[drawPixel][0]>minX) {
							plotSegment.getData()[drawPixel][0] = (float) minX;
						}
						if (Float.isNaN(plotSegment.getData()[drawPixel][1]) || plotSegment.getData()[drawPixel][1]<maxX) {
							plotSegment.getData()[drawPixel][1] = (float) maxX;
						}
						if (plotSegment.getColor()[drawPixel]!=null) {
							plotSegment.getColor()[drawPixel] = color;//blend((Color) plotSegment.getColor()[drawPixel], color); 
						}
						else {
							plotSegment.getColor()[drawPixel] = color; 
						}
					}

					//move to the next pixel and reset min max
					minX = Double.MAX_VALUE; 
					maxX= -Double.MAX_VALUE; 
					drawPixel++; 

					count = 0; 
				}

				if (rawData[i]>maxX ) maxX = rawData[i]; 
				if (rawData[i]<minX ) minX = rawData[i]; 

				count++; 	
			}

			if (drawPixel>=0 && drawPixel<plotSegment.getData().length) {
				//System.out.println("Draw a pixel: " + drawPixel); 
				//add the previous pixel to the plot segment. 
				//it's possible the same pixel will drawn on more than once. in this case must ensure that values are added if current value <max or >min . 
				if (Float.isNaN(plotSegment.getData()[drawPixel][0]) || plotSegment.getData()[drawPixel][0]>minX) {
					plotSegment.getData()[drawPixel][0] = (float) minX;
				}
				if (Float.isNaN(plotSegment.getData()[drawPixel][1]) || plotSegment.getData()[drawPixel][1]<maxX) {
					plotSegment.getData()[drawPixel][1] = (float) maxX;
				}
				if (plotSegment.getColor()[drawPixel]!=null) {
					plotSegment.getColor()[drawPixel] = color;//blend((Color) plotSegment.getColor()[drawPixel], color); 
				}
				else {
					plotSegment.getColor()[drawPixel] = color; 
				}
			}
		}
	}
	
	  public static Color blend(Color c0, Color c1) {
		    double totalAlpha = c0.getOpacity()+ c1.getOpacity();
		    double weight0 = c0.getOpacity() / totalAlpha;
		    double weight1 = c1.getOpacity() / totalAlpha;

		    double r = weight0 * c0.getRed() + weight1 * c1.getRed();
		    double g = weight0 * c0.getGreen() + weight1 * c1.getGreen();
		    double b = weight0 * c0.getBlue() + weight1 * c1.getBlue();
		    double a = Math.max(c0.getOpacity(), c1.getOpacity());

		    return new Color((int) r, (int) g, (int) b, (int) a);
		  }

}

