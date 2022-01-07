package dataPlotsFX.clickPlotFX;

import java.awt.geom.Path2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import Array.ArrayManager;
import GPS.GpsData;
import PamController.PamController;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import pamMaths.PamVector;
import clickDetector.ClickControl;
import clickDetector.ClickDetection;
import PamUtils.PamUtils;
import PamView.HoverData;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.debug.Debug;
import dataPlotsFX.TDSymbolChooserFX;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.data.TDScaleInfo;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.layout.TDSettingsPane;
import dataPlotsFX.overlaymark.menuOptions.OverlayMenuItem;
import dataPlotsFX.projector.TDProjectorFX;
import detectionPlotFX.data.DDDataInfo;
import detectionPlotFX.layout.DetectionPlotDisplay;
import detectionPlotFX.rawDDPlot.ClickDDPlotProvider;

/**
 * Handles the plotting of click data in the TDDisplayFX. Clicks can be plotted
 * in relatively standard ways on bearing time, amplitude time etc. They are
 * also plotted as stem plots to replicate the look of CPOD.exe (credit where
 * credit is due) and on a spectrogram showing FFT.
 * 
 * @author Jamie Macaulay
 *
 */
@SuppressWarnings("unused")
public class ClickPlotInfoFX extends TDDataInfoFX {

	/**
	 * Draw a click as an FFT line wityh a cutoff energy percentage showing
	 */
	public static final int FFT_CLICK=0; 

	/**
	 * DRaw a click on a stem plot coloured by mean frequency 
	 */
	public static final int STEM_CLICK_FREQ=1; 

	/**
	 * The click symbol chooser. 
	 */
	private ClickSymbolChooserFX clickSymbolChooser;

	/**
	 * Reference to the click control
	 */
	private ClickControl clickControl;

	/**
	 * The last click which was drawn on the graph. 
	 */
	private ClickDetection lastDrawnClick;

	/**
	 * The display parameters
	 */
	//	protected BTDisplayParameters btDisplayParams = new BTDisplayParameters(); //TODO-legacy code so eventually move relevant bits to clickDisplayParams. 

	/**
	 * The display parameters
	 */
	protected ClickDisplayParams clickDisplayParams = new ClickDisplayParams();

	/**
	 * Scale infos to show what axis clicks can be plotted on. 
	 */
	private TDScaleInfo bearingScaleInfo;

	/**
	 * The ICI axis
	 */
	private TDScaleInfo iciScaleInfo;

	/**
	 * The amplitude axis
	 */
	private TDScaleInfo ampScaleInfo;

	/**
	 * The slant angle axis 
	 */
	private TDScaleInfo slantScaleInfo;

	/**
	 * The frequency info 
	 */
	protected TDScaleInfo frequencyInfo;

	/**
	 * The stem plot info. Similar to a CPOD data display for CP1 data 
	 */
	private TDScaleInfo stemScaleInfo;

	/**
	 * The raw scale info. 
	 */
	private TDScaleInfo rawScaleInfo;


	/**
	 * Settings pane to change display properties of clicks. 
	 */
	private ClickControlPane2 clickControlPane;

	/**
	 * The maximum number of FFT clicks to paint before deciding to give up and just draw lines. 
	 * Reduces processing issues in real time. 
	 * 
	 */
	private static int maxfftClicks=200; 

	/**
	 * The click detection display info. This is used to preview data units e.g. by showing waveforms, frequency plots and wigner plots
	 */
	private DDDataInfo clickDDataInfo; 

	/**
	 * The data selector
	 */
	private DataSelector dataSelector;

	/**
	 * Additional menu item added to pop up. 
	 */
	private ArrayList<OverlayMenuItem> menuLists;

	/**
	 * Handles plotting clicks on the frequency axis. 
	 */
	private ClickFFTPlotManager2 clickFFTplotManager;

	/**
	 * Click raw plot manager. 
	 */
	private ClickRawPlotManager clickRawPlotManager;


	public ClickPlotInfoFX(TDDataProviderFX tdDataProvider, ClickControl clickControl, TDGraphFX tdGraph,
			@SuppressWarnings("rawtypes") PamDataBlock pamDataBlock) {
		super(tdDataProvider, tdGraph, pamDataBlock);

		this.clickControl = clickControl;

		clickSymbolChooser = new ClickSymbolChooserFX(clickControl, this);

		clickFFTplotManager = new  ClickFFTPlotManager2(this); 

		clickRawPlotManager = new ClickRawPlotManager(this); 

		bearingScaleInfo = new TDScaleInfo(0,180, ParameterType.BEARING, ParameterUnits.DEGREES);
		bearingScaleInfo.setReverseAxis(true); //set the axis to be reverse so 0 is at top of graph
		iciScaleInfo = new TDScaleInfo(0, 2, ParameterType.ICI, ParameterUnits.SECONDS);
		ampScaleInfo = new TDScaleInfo(100, 200, ParameterType.AMPLITUDE, ParameterUnits.DB);
		slantScaleInfo = new TDScaleInfo(0, 180, ParameterType.SLANTBEARING, ParameterUnits.DEGREES);
		frequencyInfo = new TDScaleInfo(0, 1, ParameterType.FREQUENCY, ParameterUnits.HZ);
		stemScaleInfo = new TDScaleInfo(100, 200, ParameterType.AMPLITUDE_STEM, ParameterUnits.DB);
		rawScaleInfo = new TDScaleInfo(-1, 1, ParameterType.AMPLITUDE, ParameterUnits.RAW);


		Arrays.fill(frequencyInfo.getPlotChannels(),1); //TODO-manage plot pane channels somehow. 
		Arrays.fill(stemScaleInfo.getPlotChannels(),1);
		Arrays.fill(rawScaleInfo.getPlotChannels(),1);

		frequencyInfo.setMaxVal(pamDataBlock.getSampleRate()/2);

		getScaleInfos().add(bearingScaleInfo);
		getScaleInfos().add(slantScaleInfo);
		getScaleInfos().add(iciScaleInfo);
		getScaleInfos().add(ampScaleInfo);
		getScaleInfos().add(stemScaleInfo);
		getScaleInfos().add(rawScaleInfo);
		getScaleInfos().add(frequencyInfo);


		//set correct min/max for frequency data axis
		frequencyInfo.setMaxVal(clickControl.getClickDataBlock().getSampleRate()/2.);

		//create the data selector
		dataSelector=this.getDataBlock().getDataSelectCreator().getDataSelector(this.getTDGraph().getUniqueName() +"_clicks", false, null);


		//create the settings pane
		clickControlPane=new ClickControlPane2(this); 

		//additonal menu items fro pop up menu
		menuLists = new ArrayList<OverlayMenuItem>(); 
		menuLists.add(new ClickReclassifyMenuItem(this)); 


		updateSettings();
	}

	@Override
	public boolean shouldDraw(int plotNumber, PamDataUnit dataUnit) {
		boolean shouldDraw=super.shouldDraw(plotNumber, dataUnit.getSequenceBitmap());
		if (!shouldDraw) return shouldDraw; //false so can't draw anyway
		else {
			//first check channels 
			if (dataUnit.getChannelBitmap()!=this.clickDisplayParams.displayChannels 
					&& this.clickDisplayParams.displayChannels!=0) {
				return false; 
			}

			double score = dataSelector.scoreData(dataUnit); 
			//System.out.println("Click detection score: " +  score);
			if (score==0) shouldDraw=false;
		}
		return shouldDraw; 
	}

	@Override
	public Double getDataValue(PamDataUnit pamDataUnit) {

		ClickDetection click = (ClickDetection) pamDataUnit;
		//first check we can generally plot the click
		if (!shouldPlot(click)) return null; 
		//click has passed the first test! Now get the correct data value; 
		Double val = null;
		switch (getCurrentScaleInfo().getDataType()) {
		case BEARING:
			val = getBearingValue(click, true);
			break;
		case ICI:
			val = getICIValue(click);
			break;
		case AMPLITUDE:
			val = getAmplitudeValue(click);
			break;
		case SLANTANGLE:
			val = getBearingValue(click, false);
			break;
		default:
			val=null; 
			break;
		}
		//System.out.println("Click value: " +  val + "  " + getCurrentScaleInfo().getDataType() + "  " + this.getTDGraph().getGraphParameters().currentDataType.getTypeString());
		lastDrawnClick = click;
		return val;
	}

	/**
	 * Get the amplitude of a click in dB re 1uPa. 
	 * @param click - the click to aquirte amplitude from
	 * @return the click's peak to peak amplitude in dB re 1uPa. 
	 */
	private Double getAmplitudeValue(ClickDetection click) {
		return click.getAmplitudeDB();
	}

	/**
	 * Get the inter-click value for a click in seconds. 
	 * @param click - the click to acquire ICI from
	 * @return the inter click value. 
	 */
	private Double getICIValue(ClickDetection click) {
		if (lastDrawnClick == null) {
			return -1.;
		}
		return (double) (click.getTimeMilliseconds() - lastDrawnClick.getTimeMilliseconds()) / 1000.;
	}


	/**
	 * Get the bearing angle in degrees for a click 
	 * @param click - the click data unit
	 * @param horz - true to get horizontal angle. False for slant angle
	 * @return the angle in degrees. 
	 */
	private double getBearingValue(ClickDetection click, boolean horz) {
		double angle = 0;
		if (click.getLocalisation() == null) return 0;

		if (horz){
			//horizontal angle: copied from the click detector BT display 
			GpsData oll;
			switch(click.getLocalisation().getSubArrayType()) {
			case ArrayManager.ARRAY_TYPE_NONE:
			case ArrayManager.ARRAY_TYPE_POINT:
				return 0;
			case ArrayManager.ARRAY_TYPE_LINE:
				double[] surfaceAngle = click.getLocalisation().getPlanarAngles();
				angle = Math.toDegrees(surfaceAngle[0]);
				break;
			case ArrayManager.ARRAY_TYPE_PLANE:
			case ArrayManager.ARRAY_TYPE_VOLUME:
				PamVector[] vecs = null;
				vecs = click.getLocalisation().getRealWorldVectors();
				//PamVector[] vecs = {new PamVector(Math.random(), Math.random(), Math.random())};
				if (vecs == null || vecs.length < 1) {
					return 0;
				}
				angle = Math.toDegrees(PamVector.vectorToSurfaceBearing(vecs[0]));
				oll = click.getOriginLatLong(false);
				if (oll != null) {
					angle -= oll.getHeading();
				}
				break;
			default:
				return 0.;
			}
			angle = PamUtils.constrainedAngle(angle, 180.00001);
		}
		else {
			//slant angle
			angle = 0;
			double[] angles = click.getLocalisation().getAngles();
			if (angles != null && angles.length>=2) {
				angle = Math.toDegrees(angles[1]);
			}
		}

		return angle; 
	}


	//	//variables for the writable image. 
	//	private WritableImage fftImage; 
	//	private int[] buffer;
	//	private final WritablePixelFormat<IntBuffer> pixelFormat = 
	//			PixelFormat.getIntArgbPreInstance();
	//	private int lastWidth=0; 
	//	private int lastHeight=0; 
	//	private int trans=colorToInt(Color.TRANSPARENT); 
	private ClickDetection detection;

	/**
	 * FFT count
	 */
	private int fftCount = 0;




	/**
	 * Convert a colour to a pixel write for a pixel buffer
	 * @param  colour
	 * @return the integer value of the colour. 
	 */
	private int colorToInt(Color c) {
		return
				((int) (c.getOpacity())   * 255  << 24) |
				((int) (c.getRed()   * 255) << 16) |
				((int) (c.getGreen() * 255) << 8)  |
				((int) (c.getBlue()  * 255));
	}

	@Override
	public void drawHighLightData(int plotNumber, GraphicsContext g, double scrollStart, TDProjectorFX tdProjector){
		fftCount=0; 
		//tdProjector.clearHoverList();
		super.drawHighLightData(plotNumber, g, scrollStart, tdProjector);
	}

	@Override
	public void drawAllDataUnits(int plotNumber, GraphicsContext g, double scrollStart, TDProjectorFX tdProjector){
		//System.out.println("ClickDataInfo:drawAllDataUnits:  draw all data"); 
		fftCount=0; 
		super.drawAllDataUnits(plotNumber, g, scrollStart, tdProjector);
	}

	@Override
	public Polygon drawDataUnit(int plotNumber, PamDataUnit pamDataUnit, GraphicsContext g,  double scrollStart, TDProjectorFX tdProjector, int type) {
		//		if (getScaleInfoIndex()==getScaleInfos().indexOf(frequencyInfo))  drawClickFFT( plotNumber,  pamDataUnit,g ,  scrollStart,  tdProjector,  type);

		//System.out.println("ClickPlotInfo: Draw data unit: " + pamDataUnit.getUID()); 
		//draw special data units. 
		Path2D path2D = null; 
		if (getScaleInfoIndex()==getScaleInfos().indexOf(frequencyInfo)) {
			//draw the FFT data unit. 
			path2D= clickFFTplotManager.drawClipFFT( plotNumber,  pamDataUnit,g ,  scrollStart,  tdProjector,  type);
		}
		else if (getScaleInfoIndex()==getScaleInfos().indexOf(stemScaleInfo)) {
			//draw on a stem plot.
			path2D = drawStemClick( plotNumber,  pamDataUnit,g ,  scrollStart,  tdProjector,  type);
		}
		else if (getScaleInfoIndex()==getScaleInfos().indexOf(rawScaleInfo)) {
			//draw the FFT data unit. 
			path2D= clickRawPlotManager.drawRawData( plotNumber,  pamDataUnit,g ,  scrollStart,  tdProjector,  type);
		}
		else return super.drawDataUnit(plotNumber, pamDataUnit, g, scrollStart, tdProjector, type);

		//add to hover list if special data units. 
		if (path2D!=null && type!=TDSymbolChooserFX.HIGHLIGHT_SYMBOL && type!=TDSymbolChooserFX.HIGHLIGHT_SYMBOL_MARKED ){
			tdProjector.addHoverData(new HoverData(path2D, pamDataUnit, 0, plotNumber));
			return null; 
		}

		return null; 
	}

	@Override
	public void lastUnitDrawn(GraphicsContext g, double scrollStart, TDProjectorFX tdProjector, int plotnumber) {
		//the fft manager needs to know when to draw the writable images stored in memory
		clickFFTplotManager.lastUnitDrawn(g, scrollStart, tdProjector, plotnumber); 
		clickRawPlotManager.lastUnitDrawn(g, scrollStart, tdProjector, plotnumber);

	}



	/**
	 * Draw a click FFT on a frequency time axis. FFT is a long line of the FFT above a certain cutoff. 
	 * @param plotNumber - the plot on which the FFT is drawing. 
	 * @param pamDataUnit - the click data unit to draw 
	 * @param g - the graphics handle. 
	 * @param windowRect - the window in which to draw in.  
	 * @param orientation - the orientation of the display
	 * @param scrollStart - the scroll start 
	 * @param tdProjector - the projector which dictates the the position of the unit on the screen. 
	 * @param type - type flag for the data unit. e.g. whether selected or not. 
	 * @return true if the data unit was successfully drawn. 
	 */
	private  Path2D drawStemClick(int plotNumber, PamDataUnit pamDataUnit, GraphicsContext g, double scrollStart,
			TDProjectorFX tdProjector, int type) {
		long timeMillis=pamDataUnit.getTimeMilliseconds();

		//System.out.println("plotNumber; "+ plotNumber+ " chan: "+PamUtils.getChannelArray(pamDataUnit.getChannelBitmap())[0]);
		//check if we can plot click on this plot pane. 
		if (!shouldDraw(plotNumber, pamDataUnit)) {
			return null; 
		}

		//get position on time axis
		double tC = tdProjector.getTimePix(timeMillis-scrollStart);

		//		System.out.println("TDDataInfoFX: tc: "+tC+"  timeMillis"+timeMillis+" scrollStart: "+scrollStart+" (timeMillis-scrollStart)/1000. "+((timeMillis-scrollStart)/1000.));
		if (tC < 0 || tC>tdProjector.getWidth()) {
			return null;
		}

		Color ffColor = this.getSymbolChooser().getPamSymbol(pamDataUnit, type).getFillColor();
		//int col=colorToInt(Color.valueOf(clickDisplayParams.fftColor));

		if (type==TDSymbolChooserFX.HIGHLIGHT_SYMBOL_MARKED ) g.setLineWidth(2);
		else if (type==TDSymbolChooserFX.HIGHLIGHT_SYMBOL ) g.setLineWidth(6);
		else g.setLineWidth(2);

		g.setStroke(ffColor);

		int[] chanClick=PamUtils.getChannelArray(pamDataUnit.getChannelBitmap());
		int[] chanPlot=PamUtils.getChannelArray(getTDGraph().getCurrentScaleInfo().getPlotChannels()[plotNumber]); 

		Path2D path2D = new Path2D.Double(0,1); 

		double x1, y1, x2, y2; 
		for (int i=0; i<chanClick.length; i++){
			//chanPlot.length is almost going to be one as generally for frequency time plot one plot pane is for one channel. 
			for (int j=0; j<chanPlot.length; j++){
				if (chanClick[i]==chanPlot[j]){
					if (tdProjector.getOrientation()==Orientation.HORIZONTAL){
						//						System.out.println("Draw amplitude: min y : " +  tdProjector.getYPix(1) + " max y: "
						//					+tdProjector.getYPix(pamDataUnit.getAmplitudeDB()) + " tC: " + tC +  " true amplitude: " + pamDataUnit.getAmplitudeDB());
						x1=tC; 
						y1=tdProjector.getYPix(1);
						x2=tC; 
						y2= tdProjector.getYPix(pamDataUnit.getAmplitudeDB()); 
						//						System.out.println("max: " + this.stemScaleInfo.getMinVal() + "  in: " +this.stemScaleInfo.getMaxVal()); 							
					}
					else {
						x1=tdProjector.getYPix(1);
						y1=tC;
						x2=tdProjector.getYPix(pamDataUnit.getAmplitudeDB());
						y2= tC;
					}
					g.strokeLine(x1,y1,x2,y2);
					path2D.moveTo(x1,y1);
					path2D.lineTo(x2,y2);

					//					if ((this.getSymbolChooser().getDrawTypes(pamDataUnit) & TDSymbolChooserFX.DRAW_LINES) != 0 && getLastDrawPoints()[chanClick[i]] != null) {
					//						g.setFill(getSymbolChooser().getPamSymbol(pamDataUnit,type).getLineColor());
					//						
					//						// only draw the line if it's moving forward, no tbackwards to work ok when scroll wrapping
					////						if (Math.abs(pt.getX() - lastPoint[firstChannel].getX()) > 100) {
					////							System.out.printf("Gap %d %3.1f to %3.1f\n", drawCalls, lastPoint[firstChannel].getX(), pt.getX());
					////						}
					//						if (x1 >= getLastDrawPoints()[chanClick[i]].getX()) {
					//							g.strokeLine(getLastDrawPoints()[chanClick[i]].getX(), getLastDrawPoints()[chanClick[i]].getY(), x1, y2);
					//						}
					//						
					//						getLastDrawPoints()[chanClick[i]] = new Point2D(x1, y2);
					//					}
					return path2D;
				}
			}
		}
		return null;
	}


	@Override
	public Point2D getPreviousDataUnit(TDProjectorFX generalProjector, PamDataUnit pamDataUnit){

		if (pamDataUnit.getSuperDetectionsCount()==0) return null;

		ListIterator<PamDataUnit> iterator = this.getPamDataBlock().getListIterator(PamDataBlock.ITERATOR_END); 
		PamDataUnit testDataUnit; 

		while (iterator.hasPrevious()) {
			//the unit to test
			testDataUnit=iterator.previous(); 

			//don't got past the start time of the super detection. 
			if (testDataUnit.getTimeMilliseconds()<pamDataUnit.getSuperDetection(0).getTimeMilliseconds()) return null; 

			//no tests untill within superdetection range. 
			//if (testDataUnit.getTimeMilliseconds()>pamDataUnit.getSuperDetection(0).getEndTimeInMilliseconds()) continue; 

			//test whether the unit is the same as the input unit, whether it has any super detections 
			//and the channel bitmap is the same. 
			if (testDataUnit==pamDataUnit || testDataUnit.getTimeMilliseconds()>pamDataUnit.getTimeMilliseconds() 
					|| testDataUnit.getSuperDetectionsCount()<=0 
					|| testDataUnit.getChannelBitmap()!=pamDataUnit.getChannelBitmap()) continue; 

			for (int i=0; i<testDataUnit.getSuperDetectionsCount(); i++) {
				if (testDataUnit.getSuperDetection(i)==pamDataUnit.getSuperDetection(0)) {
					//get the point. 
					Double val = getDataValue(testDataUnit);	
					if (val==null) return null; 
					return generalProjector.getCoord3d(testDataUnit.getTimeMilliseconds(), val, 0).getPoint2DFX();
				}
			}

		}
		return null;
	}


	//	/**
	//	 * Draw the click FFT directly to a buffer. 
	//	 * @param buffer
	//	 * @param spectrum
	//	 * @param tc
	//	 * @param maxFreq
	//	 * @param tdProjector
	//	 * @param col
	//	 */
	//	@Deprecated
	//	private void drawClickFFT(int[] buffer, double[] spectrum, double tc, double maxFreq, TDProjectorFX tdProjector, int col){
	//		//do not draw
	//		if (clickDisplayParams.fftCutOf==1) return;
	//
	//
	//		double[] minMax=PamUtils.getMinAndMax(spectrum);
	//		double freqBin=maxFreq/spectrum.length;
	//		double cutOff=clickDisplayParams.fftCutOf*minMax[1];  
	//		//		System.out.println("cutOff: "+cutOff+ " fftCutOf: " + clickDisplayParams.fftCutOf);
	//
	//	
	//		//draw a fraction of the fft
	//		int pix;
	//		double y1; 
	//		double y2;
	//		
	//	
	//		for (int i=0; i<spectrum.length-1; i++){
	//			
	//			if (spectrum[i]<cutOff) {
	//				continue; 
	//			}
	//			
	//			//work out where this fragment of the fft line should be
	//			y2=tdProjector.getYPix(i*freqBin);
	//			y1=tdProjector.getYPix((i+1)*freqBin);
	//			
	//			//draw the line in the buffer
	//			for (int y=(int) y1; y<=y2; y++){
	//				//now add pixels to buffer. 
	//				pix= (int) (tc+lastWidth*y);
	//				if (pix<buffer.length) buffer[pix]=col;
	//			}
	//			
	//		}
	//
	//	}


	/***
	 * Used to determine which clicks should be plotted on the display by using the click data selector. 
	 * @param click - the click to check. 	
	 * @return true to plot the click. 
	 */
	private synchronized boolean shouldPlot(ClickDetection click) {

		if (click == null) return false;

		dataSelector.scoreData(click); 

		if (clickDisplayParams.displayChannels > 0 && (clickDisplayParams.displayChannels & click.getChannelBitmap()) == 0) return false;

		return true;
	}

	@Override
	public TDSymbolChooserFX getSymbolChooser() {
		return clickSymbolChooser;
	}

	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#getScaleInformation(boolean)
	 */
	@Override
	public TDScaleInfo getScaleInfo(boolean autoScale) {
		clearDraw(); //29/06/2020 - why is this here?
		return super.getScaleInfo(autoScale);
	}

	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#getFixedScaleInformation()
	 */
	@Override
	public TDScaleInfo getScaleInfo() {
		//System.out.println("Click bearing Info: "+ bearingScaleInfo.getMinVal() + "  "+ bearingScaleInfo.getMaxVal());
		//need to set correct number of plots 
		setNPlotPanes(this.frequencyInfo,this.getDataBlock(), false); 
		setNPlotPanes(this.stemScaleInfo,this.getDataBlock(), false); 
		setNPlotPanes(this.rawScaleInfo,this.getDataBlock(), false); 

		//29/06/2017. This was causing weird issues with min and max text fields in the data select bar.  
		//		int iind = getScaleInfoIndex();
		//		if (iind < 0) return null;
		//		if (iind == 0) { 
		//			double min = Math.min(bearingScaleInfo.getMinVal(), bearingScaleInfo.getMaxVal());
		//			double max = Math.max(bearingScaleInfo.getMinVal(), bearingScaleInfo.getMaxVal());
		//			if (this.getTDGraph().getOrientation() == Orientation.HORIZONTAL) {
		//				bearingScaleInfo.setMaxVal(min);
		//				bearingScaleInfo.setMinVal(max);
		//			}
		//			else {
		//				bearingScaleInfo.setMaxVal(max);
		//				bearingScaleInfo.setMinVal(min);
		//			}
		//		}
		return super.getScaleInfo();
	}

	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#clearDraw()
	 */
	@Override
	public void clearDraw() {
		//System.out.println("ClickPlotInfo: CLEAR: " + this.getTDGraph().getId()); 
		super.clearDraw();
		lastDrawnClick = null;
		//this.clickFFTplotManager.clear(); 
	}

	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#hasOptions()
	 */
	@Override
	public boolean hasOptions() {
		return true;
	}

	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#editOptions(java.awt.Window)
	 */
	@Override
	public boolean editOptions() {
		//		BTDisplayParameters newParams = ClickDisplayDialog.showDialog(clickControl, frame, btDisplayParams);
		//		if (newParams != null) {
		//			btDisplayParams = newParams.clone();
		//			updateSettings();
		//			return true;
		//		}
		return false;
	}

	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#getStoredSettings()
	 */
	@Override
	public Serializable getStoredSettings() {
		return clickDisplayParams;
	}

	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#setStoredSettings(java.io.Serializable)
	 */
	@Override
	public boolean setStoredSettings(Serializable storedSettings) {
		if (ClickDisplayParams.class.isAssignableFrom(storedSettings.getClass())) {
			clickDisplayParams = (ClickDisplayParams) storedSettings;
			updateSettings();
			return true;
		}
		return false;
	}

	public int getDisplayChannels() {
		return clickDisplayParams.displayChannels;

	}

	public void setDisplayChannels(int displayChannels) {
		clickDisplayParams.displayChannels = displayChannels;
		updateSettings();
	}

	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#getHidingDialogComponent()
	 */
	@Override
	public TDSettingsPane getGraphSettingsPane() {
		return clickControlPane;
	}

	/**
	 * Called when settings have changed. 
	 */
	public void updateSettings() {
		//27/03/2017. This was stopping TDGraph from properly setting data line infos
		//		if (axIndex >= 0 && axIndex < getScaleInfos().size()) {
		//			//			String dataName = dataLines.get(axIndex).name;
		//			getTDGraph().selectDataLine(this, getScaleInfos().get(axIndex));
		//		}

		//		Debug.out.println("ClickPlotInfo: Update bearing settings: " + 
		//				clickDisplayParams.bearingRange[0] + "   " + clickDisplayParams.bearingRange[1]); 


		// set the various scales depending on whats in the bt parameters. 
		bearingScaleInfo.setMinVal(clickDisplayParams.bearingRange[0]);
		bearingScaleInfo.setMaxVal(clickDisplayParams.bearingRange[1]);

		ampScaleInfo.setMinVal(clickDisplayParams.amplitudeRange[0]);
		ampScaleInfo.setMaxVal(clickDisplayParams.amplitudeRange[1]);

		stemScaleInfo.setMinVal(clickDisplayParams.amplitudeRange[0]);
		stemScaleInfo.setMaxVal(clickDisplayParams.amplitudeRange[1]);

		iciScaleInfo.setMinVal(clickDisplayParams.iciRange[0]);
		iciScaleInfo.setMaxVal(clickDisplayParams.iciRange[1]);

		slantScaleInfo.setMinVal(clickDisplayParams.slantRange[0]);
		slantScaleInfo.setMaxVal(clickDisplayParams.slantRange[1]);


		//		iciScaleInfo.setMaxVal(btDisplayParams.maxICI);
		//		if (btDisplayParams.logICIScale) {
		//			iciScaleInfo.setMinVal(btDisplayParams.minICI);
		//		}
		//		else {
		//			iciScaleInfo.setMinVal(0);
		//		}

		//get the number of plots panes needed if showing frequency graph. 
		//		if (clickDisplayParams.displayChannels==0) frequencyInfo.setnPlots(PamUtils.getNumChannels(clickControl.getClickDataBlock().getChannelMap()));
		if (clickDisplayParams.displayChannels==0){
			frequencyInfo.setnPlots(PamUtils.getNumChannels(clickControl.getClickDataBlock().getSequenceMap()));
			stemScaleInfo.setnPlots(PamUtils.getNumChannels(clickControl.getClickDataBlock().getSequenceMap()));
		}
		else {
			frequencyInfo.setnPlots(PamUtils.getNumChannels(clickDisplayParams.displayChannels));
			stemScaleInfo.setnPlots(PamUtils.getNumChannels(clickDisplayParams.displayChannels));
		}

		//set the data selector params; 
		//create the data selector
		dataSelector=this.getDataBlock().getDataSelectCreator().getDataSelector(this.getTDGraph().getUniqueName() +"_clicks", false, null);

		//		System.out.println("nChannels: "+PamUtils.getNumChannels(btDisplayParams.displayChannels));//TODO-implement new click params here. 
		getTDGraph().checkAxis();
	}

//	/*
//	 * Colour type changed in the quick dialog panel. 
//	 */
//	public void selectColourType(int colourId) {
//		clickDisplayParams.colourScheme = colourId;
//		getTDGraph().repaint(0);
//	}


	/**
	 * Get the click control control. 
	 * @return reference to the click control.
	 */
	public ClickControl getClickControl() {
		return clickControl;
	}

	/**
	 * Get the display paramters for clicks on a FX data plots. 
	 * @return paramters for how clicks are displayed on FX data plots. 
	 */
	public ClickDisplayParams getClickDisplayParams() {
		return clickDisplayParams;
	}

	/**
	 * Get the TDScale information for amplitude axis.
	 * @return tdscaleinformation for amplitude axis
	 */
	public TDScaleInfo getAmpScaleInfo() {
		return ampScaleInfo;
	}

	/**
	 * Called when the time range spinner on the main display panel changes. 
	 * @param oldValue old value (seconds)
	 * @param newValue new value (seconds)
	 */
	@Override
	public void timeRangeSpinnerChange(double oldValue, double newValue) {	
		//		System.out.println("ClickPlotInfo: timeRangeSpinnerCahnged "+newValue+ " req. histroy "+
		//				clickControl.getClickDetector().getRequiredDataHistory(clickControl.getClickDataBlock(), null));
	}

	/**
	 * Get the settingsa pane for the clicks on the display. This holds controls to change
	 * symbols and data etc. 
	 * @return the click settings pane. 
	 */
	public ClickControlPane2 getClickControlPane() {
		// TODO Auto-generated method stub
		return this.clickControlPane;
	}

	/**
	 * Get click symbol chooser. 
	 * @return the click symbol chooser. 
	 */
	public ClickSymbolChooserFX getClickSymbolChooser() {
		return this.clickSymbolChooser;
	}

//	/**
//	 * The detection display data info provider. This allows detection to be plotted in right click panes
//	 * @return the data info for the detection type.
//	 */
//	@SuppressWarnings("unchecked")
//	@Override
//	public DDDataInfo<ClickDetection> getDDataProvider(DetectionPlotDisplay detectionDisplay){
//		if (clickDDataInfo==null || clickDDataInfo.getDetectionPlotDisplay()!=detectionDisplay){
//			clickDDataInfo= new ClickDDPlotProvider(clickControl).createDataInfo(detectionDisplay);
//		}
//		return this.clickDDataInfo; 
//	}

	/**
	 *Get the data selector for clicks. This handles plotting clicks of different species
	 * @return the data selector. 
	 */
	public DataSelector getClickDataSelector() {
		return this.dataSelector;
	}

	@Override
	public void notifyChange(int changeType){
		switch (changeType) {
		case PamController.CHANGED_PROCESS_SETTINGS:
			//System.out.println("ClickPlotInfoFX: ChangeProcess: Set freq limits: " + PamController.getInstance().getGlobalMediumManager().getDefaultdBHzScales()[0]); 
			
			this.getClickControlPane().setParams(); 
			this.clickFFTplotManager.update();
			break;
			
		case PamController.GLOBAL_MEDIUM_UPDATE:

			double[] amplitudeLims = PamController.getInstance().getGlobalMediumManager().getDefaultAmplitudeScales();

			//System.out.println("ClickPlotInfoFX: Global medium Change: Set freq limits: " + PamController.getInstance().getGlobalMediumManager().getDefaultdBHzScales()[0]); 
			this.ampScaleInfo.setMinVal(amplitudeLims[0]);
			this.ampScaleInfo.setMaxVal(amplitudeLims[1]);

			this.clickDisplayParams.freqAmplitudeLimits = PamController.getInstance().getGlobalMediumManager().getDefaultdBHzScales(); 

			this.clickControlPane.setParams(); //force the change in the click display params
			this.clickControlPane.notifyUpdate();

			this.getTDGraph().repaint(0);
			break;
			
			
		case PamController.OFFLINE_DATA_LOADED:
			//this is critical to stop the buffer containing data over long time periods which can make
			//the segmenter unstable (need to work on that. )
			this.clickFFTplotManager.clear();
			this.clickRawPlotManager.clear(); 
			break; 
		case PamController.DATA_LOAD_COMPLETE:
			//this is critical to stop the buffer containing data over long time periods which can make
			//the segmenter unstable (need to work on that. )
			this.clickFFTplotManager.clear();
			this.clickRawPlotManager.clear(); 
			break; 
		}
	}

	@Override
	public  ArrayList<OverlayMenuItem> getMenuNodes(){
		return menuLists;
	}

	@Override
	public boolean setCurrentAxisName(ParameterType dataType, ParameterUnits dataUnits) {
		boolean chnaged  = super.setCurrentAxisName(dataType, dataUnits);
		//send update to the control pane. 
		this.clickControlPane.notifyUpdate(); 
		return chnaged;
	}

	/**
	 * Get the plot manager for plotting FFT data. THis handles plotting the FFT's of clicks on 
	 * time/frequency displays...
	 * @return The FFT plot manager.
	 */
	public ClickFFTPlotManager2 getClickFFTplotManager() {
		return clickFFTplotManager;
	}

	/**
	 * get the raw scale info. This handles plotting raw waveforms. 
	 * @return the raw scale info. 
	 */
	public TDScaleInfo getRawScaleInfo() {
		return rawScaleInfo;
	}

	/**
	 * The raw click manager. Handles plotting raw waveform data on the plot. 
	 * @return thw raw click manager. 
	 */
	public ClickRawPlotManager getClickRawPlotManager() {
		return this.clickRawPlotManager;
	}



}
