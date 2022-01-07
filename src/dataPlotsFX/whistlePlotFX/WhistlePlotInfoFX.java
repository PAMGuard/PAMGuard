package dataPlotsFX.whistlePlotFX;

import java.awt.geom.Path2D;
import java.util.ListIterator;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.HoverData;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import whistlesAndMoans.ConnectedRegion;
import whistlesAndMoans.ConnectedRegionDataBlock;
import whistlesAndMoans.ConnectedRegionDataUnit;
import whistlesAndMoans.SliceData;
import whistlesAndMoans.WhistleMoanControl;
import whistlesAndMoans.WhistleToneParameters;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import clickDetector.ClickDetection;
import dataPlots.data.TDSymbolChooser;
import dataPlotsFX.TDSymbolChooserFX;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.data.TDScaleInfo;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.layout.TDSettingsPane;
import dataPlotsFX.projector.TDProjectorFX;
import dataPlotsFX.projector.TimeProjectorFX;
import detectionPlotFX.data.DDDataInfo;
import detectionPlotFX.layout.DetectionPlotDisplay;
import detectionPlotFX.rawDDPlot.ClickDDPlotProvider;
import detectionPlotFX.whistleDDPlot.WhistleDDPlotProvider;

/**
 * Whistles/Moans are long (relatively) tonal sounds which can be plotted on a bearing/amplitude display and/or spectrogram. 
 * @author Jamie Macaulay.
 */
public class WhistlePlotInfoFX extends TDDataInfoFX {

	/**
	 * Reference to whistle and moan control
	 */
	private WhistleMoanControl wmControl;

	//	/**
	//	 * Need a simple symbol chooser here. Deals with whistles on bearing time display
	//	 */
	//	private TDSymbolChooserFX symbolChooser = new SimpleSymbolChooserFX();

	/**
	 * Scale information for whistles on bearing time display
	 */
	private TDScaleInfo bearingScaleInfo;

	/**
	 * Scale information for whistles on frequency time display
	 */
	private TDScaleInfo frequencyInfo;

	/**
	 * Reference to connected region datablock. 
	 */
	private ConnectedRegionDataBlock dataBlock;

	/**
	 * Colours to use for plotting whisltes. 
	 */
	private Color[] whistleColours = {Color.RED, Color.LIGHTGREEN, Color.CYAN,
			Color.ORANGE, Color.PINK, Color.MAGENTA };

	private WhistleTDSymbolChooser whistleSymbolChooser;

	/**
	 * Controls appearance of whistles. 
	 */
	private WhislteControlPane whislteControlPane;


	/**
	 * The click detection display info. This is used to preview data units e.g. by showing waveforms, frequency plots and wigner plots
	 */
	private DDDataInfo whistleDDataInfo; 

	public WhistlePlotInfoFX(TDDataProviderFX tdDataProvider, WhistleMoanControl wmControl, TDGraphFX tdGraph, 
			ConnectedRegionDataBlock pamDataBlock) {
		super(tdDataProvider, tdGraph, pamDataBlock);

		this.wmControl = wmControl;
		this.dataBlock=pamDataBlock;

		//create the whistle symbol chooser. 
		whistleSymbolChooser = new WhistleTDSymbolChooser(this); 

		//create the whsitle control pane
		whislteControlPane = new WhislteControlPane(this); 

		//add types of data that can be displayed by this data unit
		//create data axis scale information for each type. 
		bearingScaleInfo = new TDScaleInfo(0,180, ParameterType.BEARING, ParameterUnits.DEGREES);
		bearingScaleInfo.setReverseAxis(true); //set the axis to be reverse so 0 is at top of graph
		frequencyInfo = new TDScaleInfo(0, 1, ParameterType.FREQUENCY, ParameterUnits.HZ);
		this.getScaleInfos().add(bearingScaleInfo);
		this.getScaleInfos().add(frequencyInfo);

		//set correct frequency range based on nyquist. 
		frequencyInfo.setMaxVal(pamDataBlock.getSampleRate()/2.);

	}

	@Override
	public Double getDataValue(PamDataUnit pamDataUnit) {
		if (pamDataUnit.getLocalisation() == null) {
			return null;
		}
		double[] angles = pamDataUnit.getLocalisation().getAngles();
		if (angles != null && angles.length > 0) {
			return Math.toDegrees(angles[0]);
		}
		return null;
	}

	@Override
	public TDScaleInfo getScaleInfo() {

		setNPlotPanes(frequencyInfo, this.getDataBlock(), false); 

		double min = Math.min(bearingScaleInfo.getMinVal(), bearingScaleInfo.getMaxVal());
		double max = Math.max(bearingScaleInfo.getMinVal(), bearingScaleInfo.getMaxVal());

		bearingScaleInfo.setMaxVal(max);
		bearingScaleInfo.setMinVal(min);

		return super.getScaleInfo();
	}

	@Override
	public Polygon drawDataUnit(int plotNumber, PamDataUnit pamDataUnit, GraphicsContext g, double scrollStart, TDProjectorFX tdProjector,int type) {
		// if drawing FFT then need to use slight more complex drawing functions.
		if (getScaleInfoIndex()==1) drawWhistleFFT(plotNumber,pamDataUnit, g, tdProjector, scrollStart,type);
		//if not then use the standard drawing stuff; 
		else super.drawDataUnit(plotNumber, pamDataUnit, g, scrollStart, tdProjector ,type);
		return null;
	}

	/**
	 * Get the color for a whistle/moan fragment. 
	 * @param iCol
	 * @return the color to paint the fragment. 
	 */
	int iCol=0; 
	private static Color getFragmentColour(ConnectedRegionDataUnit dataUnit, WhistleTDSymbolChooser whistleSymbolChooser){
		whistleSymbolChooser.getPamSymbol(dataUnit, TDSymbolChooserFX.NORMAL_SYMBOL); 
		//		iCol = dataUnit.getAbsBlockIndex()%whistleColours.length;
		//		Color col=whistleColours[iCol];
		return whistleSymbolChooser.getPamSymbol(dataUnit, TDSymbolChooserFX.NORMAL_SYMBOL).getFillColor(); 
	}

	/**
	 * Draw a whistle fragment on the spectrogram. 
	 * @param plotNumber - the plot number 
	 * @param pamDataUnit - the PAM data unit	
	 * @param g - the graphics handle
	 * @param windowRect - window describing window pixel dimensions to draw on
	 * @param orientation - orientation
	 * @param tdprojector - projector which converts pixels to time, frequency and vice versa. 
	 * @param scrollStart - the scroll start 
	 * @param type - type flag for plotting
	 */
	private void drawWhistleFFT(int plotNumber, PamDataUnit pamDataUnit,
			GraphicsContext g, TDProjectorFX tdprojector, double scrollStart, int type) {
		if (!shouldDraw(plotNumber, pamDataUnit)){
			//System.out.println("Cannot plot whistle");
			iCol++;
			return; 
		}
		else {

			Path2D awtPath=drawWhistleFFT( pamDataUnit,
					g,  tdprojector,  scrollStart,  type);
			if (awtPath!=null && type!=TDSymbolChooserFX.HIGHLIGHT_SYMBOL && type!=TDSymbolChooserFX.HIGHLIGHT_SYMBOL_MARKED ){
				tdprojector.addHoverData(new HoverData(awtPath, pamDataUnit, 0, plotNumber));
			}
		}
	}

	/**
	 * Draw a whistle fragment. 
	 * @param pamDataUnit - the PAM data unit	
	 * @param g - the graphics handle
	 * @param windowRect - window describing window pixel dimensions to draw on
	 * @param orientation - orientation
	 * @param tdprojector - projector which converts pixels to time, frequency and vice versa. 
	 * @param scrollStart - the scroll start 
	 * @param type - type flag for plotting
	 * @param wmControl
	 * @param fftLength - the FFT length in samples
	 * @param fftHop - the FFT hop in samples
	 * @param sampleRate  - the sample rate in samples per second
	 * @param fillCol - the fill colour
	 * @param linCol - the line colour.
	 * @return a 2D path in pixels of the fragment. 
	 */
	public static Path2D drawWhistleFragement(PamDataUnit pamDataUnit, WhistleMoanControl wmControl, int fftLength, int fftHop, float sampleRate,
			GraphicsContext g, TimeProjectorFX tdprojector, double scrollStart, int type,  Color fillCol, Color linCol, Orientation orientation) {

		//get position on time axis
		long timeMillis=pamDataUnit.getTimeMilliseconds();
		double tC = tdprojector.getTimePix(timeMillis-scrollStart); 

		//		timeAxis.getPosition((timeMillis-scrollStart)/1000.);

		if (tC < 0 || tC >  tdprojector.getGraphTimePixels()) {
			return null;
		}

		ConnectedRegionDataUnit dataUnit=((ConnectedRegionDataUnit) pamDataUnit);
		ConnectedRegion cr = dataUnit.getConnectedRegion();
		WhistleToneParameters wmParams = wmControl.getWhistleToneParameters();


		g.setFill(fillCol);
		g.setStroke(linCol);


		//		int lastYStep = 0;
		//		int maxPeaks = cr.getMaxPeaks();
		int[] lastPeak = null, thisPeak;
		int lastPeakNum;
		int slicePeaks;
		double minX = Double.MAX_VALUE;
		double minY = minX;
		double maxX = Double.MIN_VALUE;
		double maxY = maxX;

		SliceData sliceData, prevSlice = null;
		if (wmParams.shortShowPolicy == WhistleToneParameters.SHORT_HIDEALL && 
				cr.getNumSlices() < wmParams.shortLength) {
			return null;
		}

		ListIterator<SliceData> sliceIterator = cr.getSliceData().listIterator();
		Point2D pt1 = null, pt2 = null;
		double f1, f2;

		boolean fullOutline = wmParams.showContourOutline;

		//		if (wmParams.shortShowPolicy == WhistleToneParameters.SHORT_SHOWGREY && 
		//			cr.getNumSlices() < wmParams.shortLength) {
		//			g.setFill(Color.GRAY);
		//			g.setStroke(Color.GRAY);
		//		}
		//		else {
		//		}
		if (type==TDSymbolChooser.HIGHLIGHT_SYMBOL){
			g.setLineWidth(5);
		}
		else g.setLineWidth(2);


		double binStepMillis = fftHop / sampleRate * 1000;
		double sliceMillis, prevtC = 0;
		SliceData firstSlice = null;
		int sliceX, prevSliceX = -100;
		//iterate through all fft bins which contain fragment slices. 
		Path2D awtPath = new Path2D.Double(0, cr.getNumSlices());
		int pathCount = 0;
		while (sliceIterator.hasNext()) {
			sliceData = sliceIterator.next();
			if (firstSlice == null) {
				firstSlice = sliceData;
			}
			sliceMillis = (sliceData.getSliceNumber()-firstSlice.getSliceNumber()) * binStepMillis;
			sliceMillis += timeMillis;
			slicePeaks = sliceData.getnPeaks();

			tC=tdprojector.getTimePix((long) (sliceMillis-scrollStart)); 
			if (tC < 0 || tC >  tdprojector.getGraphTimePixels()) {
				return null;
			}

			//			if (wrap){
			//				tC= PamUtils.constrainedNumber(this.getTDGraph().getWrapPix() + 
			//						timeAxis.getPosition((sliceMillis-scrollStart)/1000.), windowRect.getWidth());
			//			}
			//			else {
			//				tC = timeAxis.getPosition((sliceMillis-scrollStart)/1000.);
			//			}


			sliceX = (int) tC;
			if (wmControl.getWhistleToneParameters().stretchContours && sliceX <= prevSliceX) {
				sliceX = prevSliceX + 1;
			}

			for (int iP = 0; iP < slicePeaks; iP++) {
				thisPeak = sliceData.getPeakInfo()[iP];
				lastPeakNum = thisPeak[3];
				if (lastPeakNum < 0 || prevSlice == null) {
					continue;
				}
				if (lastPeakNum >= prevSlice.getPeakInfo().length) {
					lastPeak = prevSlice.getPeakInfo()[lastPeakNum];		
				}
				lastPeak = prevSlice.getPeakInfo()[lastPeakNum];
				f2 = thisPeak[1] * sampleRate / fftLength;

				pt2 = new Point2D(tC, tdprojector.getCoord3d(0,f2,0).getCoordinate(1));

				if (pathCount == 0) {
					awtPath.moveTo(pt2.getX(), pt2.getY());
				}
				else {
					awtPath.lineTo(pt2.getX(), pt2.getY());
				}
				pathCount++;
				//				System.out.println("yAxis: "+ tdprojector.getCoord3d(0,f2,0).getCoordinate(1)+ " f2: " +f2 + " max val: "+yAxis.getMaxVal()+" "+frequencyInfo.getUnitDivisor());

				f1=lastPeak[1] * sampleRate / fftLength;

				pt1 = new Point2D(prevtC,tdprojector.getCoord3d(0,f1,0).getCoordinate(1));

				if (pt1 != null) {
//										System.out.println("Draw Whistle: tC " + prevSliceX + "  " + pt1.getY()+ " f1:  " + f1 + "  "+ sliceX + " " + pt2.getY()); 

					drawWhistleSegment( g,  orientation, prevSliceX, pt1.getY(),  sliceX, pt2.getY());
				}

				if (fullOutline) {
					f2=thisPeak[0] * sampleRate / fftLength;

					pt2 = new Point2D(tC, tdprojector.getCoord3d(0,f2,0).getCoordinate(1));

					f1=lastPeak[0] * sampleRate / fftLength;

					pt1 = new Point2D(prevtC, tdprojector.getCoord3d(0,f1,0).getCoordinate(1));

					drawWhistleSegment( g,  orientation, prevSliceX, pt1.getY(),  sliceX, pt2.getY());

					minX = Math.min(minX, pt1.getX());
					minY = Math.min(minY, pt1.getY());
					maxX = Math.max(maxX, pt1.getX());
					maxY = Math.max(maxY, pt1.getY());
					minX = Math.min(minX, pt2.getX());
					minY = Math.min(minY, pt2.getY());
					maxX = Math.max(maxX, pt2.getX());
					maxY = Math.max(maxY, pt2.getY());

					f2=thisPeak[2] * sampleRate / fftLength;

					pt2 =  new Point2D(tC, tdprojector.getCoord3d(0,f2,0).getCoordinate(1));

					f1=lastPeak[2] * sampleRate / fftLength;
					pt1 =  new Point2D(prevtC, tdprojector.getCoord3d(0,f1,0).getCoordinate(1));

					drawWhistleSegment( g,   orientation, prevSliceX, pt1.getY(),  sliceX, pt2.getY());

				}

				minX = Math.min(minX, pt1.getX());
				minY = Math.min(minY, pt1.getY());
				maxX = Math.max(maxX, pt1.getX());
				maxY = Math.max(maxY, pt1.getY());
				minX = Math.min(minX, pt2.getX());
				minY = Math.min(minY, pt2.getY());
				maxX = Math.max(maxX, pt2.getX());
				maxY = Math.max(maxY, pt2.getY());
			}

			prevSlice = sliceData;
			prevtC = tC;
			prevSliceX = sliceX;
		}
		return awtPath;	
	}

	/**
	 * Draw a whistle fragment on the spectrogram. 
	 * @param pamDataUnit - the PAM data unit	
	 * @param g - the graphics handle
	 * @param windowRect - window describing window pixel dimensions to draw on
	 * @param orientation - orientation
	 * @param tdprojector - projector which converts pixels to time, frequency and vice versa. 
	 * @param scrollStart - the scroll start 
	 * @param type - type flag for plotting
	 */
	private Path2D drawWhistleFFT(PamDataUnit pamDataUnit,
			GraphicsContext g, TDProjectorFX tdprojector, double scrollStart, int type) {

		ConnectedRegionDataUnit dataUnit=((ConnectedRegionDataUnit) pamDataUnit);
		ConnectedRegion cr = dataUnit.getConnectedRegion();
		WhistleToneParameters wmParams = wmControl.getWhistleToneParameters();

		int fftLength = dataBlock.getFftLength();
		int fftHop = dataBlock.getFftHop();
		float sampleRate = dataBlock.getSampleRate();

		Color fillCol=getFragmentColour(dataUnit, this.whistleSymbolChooser);
		Color linCol=getFragmentColour(dataUnit, this.whistleSymbolChooser);

		return drawWhistleFragement( pamDataUnit,  wmControl,  fftLength,  fftHop,  sampleRate,
				g,  tdprojector,  scrollStart,  type,   fillCol,  linCol, tdprojector.getOrientation()) ;
	}

	/**
	 * Draw whistle segment on the graph. A whistle segment is not a complete whistle, but usually a fragment of a whistle. 
	 * Each fragment is represented by different colours.  
	 * @param orientation - orientation of graph, horizontal or vertical. 
	 * @param prevtC - previous time position in pixels
	 * @param prevFreq - previous frequency in pixels
	 * @param tC - time position in pixels
	 * @param freq - frequency in pixels. 
	 */
	private static void drawWhistleSegment(GraphicsContext g, Orientation orientation, double prevtC, double prevFreq, double tC, double freq){
		if (prevtC<tC){
			if (orientation==Orientation.HORIZONTAL ) g.strokeLine(prevtC, prevFreq, tC, freq);
			else g.strokeLine(prevFreq, prevtC, freq, tC);
		}
		else{
			//			if (orientation==Orientation.HORIZONTAL ) g.strokeLine(tC, prevFreq, prevtC, freq);
			//			else g.strokeLine(prevFreq, tC, freq, prevtC);
		}
	}

	@Override
	public TDSymbolChooserFX getSymbolChooser() {
		return whistleSymbolChooser;
	}

	/**
	 * Get the whistle and moan control.
	 * @return the whistle and moan control. 
	 */
	public WhistleMoanControl getWmControl() {
		return wmControl;
	}

	@Override
	public PamDataBlock getSourceDataBlock() {
		return getDataBlock();
	}

	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#getHidingDialogComponent()
	 */
	@Override
	public TDSettingsPane getGraphSettingsPane() {
		return whislteControlPane;
	}

	/**
	 * Get the whistle symbol chooser. 
	 * @return the whistle symbol chooser. 
	 */
	public WhistleTDSymbolChooser getWhistleSymbolChooser() {
		return this.whistleSymbolChooser;
	}

	/**
	 * The detection display data info provider. This allows detection to be plotted in right click panes
	 * @return the data info for the detection type./
	 */
	@Override
	public DDDataInfo<ConnectedRegionDataUnit> getDDataProvider(DetectionPlotDisplay detectionDisplay){
		if (whistleDDataInfo==null || whistleDDataInfo.getDetectionPlotDisplay()!=detectionDisplay){
			whistleDDataInfo= new WhistleDDPlotProvider(wmControl).createDataInfo(detectionDisplay);
		}
		return this.whistleDDataInfo; 
	}

}