package dataPlotsFX.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import pamViewFX.fxNodes.pamAxis.PamAxisFX;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Polygon;
import PamController.PamController;
import PamUtils.Coordinate3d;
import PamUtils.PamUtils;
import PamView.HoverData;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamguardMVC.LoadObserver;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamObserverAdapter;
import dataPlotsFX.TDSymbolChooserFX;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.layout.TDSettingsPane;
import dataPlotsFX.overlaymark.menuOptions.OverlayMenuItem;
import dataPlotsFX.projector.TDProjectorFX;
import detectionPlotFX.data.DDDataInfo;
import detectionPlotFX.layout.DetectionPlotDisplay;
import detectiongrouplocaliser.DetectionGroupSummary;

/**
 * Information about a type of data that can be displayed on any of the plots. 
 * <br>Life is complicated since many data types can display many different things, 
 * e.g. amplitude / bearing / ICI, etc. <br>
 * Therefore getDataUnits() can return multiple types of units <br>
 * Life is even more complicated since data cannot only be identified by their units, 
 * e.g. three channels of accelerometer data in Dtag data. 
 * 
 * @author Doug Gillespie, Jamie Macaulay
 *
 */
@SuppressWarnings("rawtypes")
public abstract class TDDataInfoFX {

	/**
	 * The data plugins
	 */
	public ArrayList<TDDataInfoPlugin> dataPlugIns = new ArrayList<TDDataInfoPlugin>();

	/**
	 * The data block which is being plotted. 
	 */
	private PamDataBlock pamDataBlock;


	public int scaleInfoIndex=-1; // index into the dataLineInfos list. 

	/**
	 * Holds all information on the axis which can be plotted. . 
	 */
	private ArrayList<TDScaleInfo> scaleInfos = new ArrayList<TDScaleInfo>(); 


	private TDDataProviderFX tdDataProvider;


	private TDGraphFX tdGraph;


	protected boolean isViewer;

	
	//private TDScaleInfo currentScaleInfo;


	public TDDataInfoFX(TDDataProviderFX tdDataProvider, TDGraphFX tdGraph, PamDataBlock pamDataBlock) {
		super();
		this.tdDataProvider = tdDataProvider;
		this.tdGraph = tdGraph;
		this.pamDataBlock = pamDataBlock;
		this.isViewer = (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW);
		selfObserver = new TDDataObserver();
	}


	/**
	 * Get the data block for this information
	 * @return the data block. 
	 */
	public PamDataBlock getDataBlock() {
		return pamDataBlock;
	}

	/**
	 * The detection display data info. This allows detection to be plotted in right click panes
	 * @param - the detection display for the provider. 
	 * @return the data info for the detection type.
	 *
	 */
	@Deprecated
	public DDDataInfo getDDataProvider(DetectionPlotDisplay detectionPlotDisplay){
		return null; 
	}

	/**
	 * Gets a value for a specific data unit which should be in the
	 * same units as the scale information. This will then be 
	 * converted into a plot position by the TDGraph. 
	 * @param pamDataUnit
	 * @return data value or null if this data point should not be plotted. 
	 */
	abstract public Double getDataValue(PamDataUnit pamDataUnit);


	/**
	 * Set the current data units - this is an enum which matches an enum in
	 * dataLineInfos() or all will go horribly wrong.
	 * 
	 * @param AxisName - the type of axis.
	 */
	public boolean setCurrentAxisName(ParameterType dataType, ParameterUnits dataUnits) {
		/*
		 * see if the currently selected line is OK and if it is, leave it alone.
		 * Otherwise set to the first available line with those units.
		 */

//		TDScaleInfo currentLine = getScaleInfo();
//		
//		System.out.println("setCurrentAxisName : " +  currentLine.getDataType() + "  " + dataType); 
//
//
//		if (currentLine != null) {
//			if (currentLine.getDataType().equals(dataType) && currentLine.getDataUnit().equals(dataUnits)) {
//				scaleInfoIndex=scaleInfos.indexOf(currentLine); 
//				return true; // nothing to do !
//			}
//		}

		scaleInfoIndex = -1;
		for (int i = 0; i < scaleInfos.size(); i++) {
			if (scaleInfos.get(i).getDataType().equals(dataType) && scaleInfos.get(i).getDataUnit().equals(dataUnits)) {
				scaleInfoIndex = i;
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the current scale info. 
	 * @return the currentScaleInfo
	 */
	public TDScaleInfo getCurrentScaleInfo() {
		return scaleInfos.get(scaleInfoIndex);
	}


	/**
	 * 
	 * @param axisName
	 * @return true if the data have a datalineinfo with the given name
	 */
	public boolean hasAxisName(ParameterType dataType, ParameterUnits dataUnits) {
		for (int i = 0; i < scaleInfos.size(); i++) {
			if (scaleInfos.get(i).getDataType().equals(dataType) 
					&& scaleInfos.get(i).getDataUnit().equals(dataUnits)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * A longer data name, more descriptive than the data units. 
	 * Note that a single data block may have many different types of data 
	 * it can plot ! <br>
	 * This is now taken only from the data provider so that they can remain 
	 * matched up as settings are serialized and de-serialized. 
	 * @return a name for the data. 
	 */
	public final String getDataName() {
		return tdDataProvider.getName();
	}

	/**
	 * Get a shorter name for use in displays. This is used because
	 * a lot of the default names are quite long since they are based
	 * on standard data block names. 
	 * @return a short name for use in the display. 
	 */
	public String getShortName() {
		if (pamDataBlock == null) {
			return getDataName();
		}
		if (pamDataBlock.getParentProcess() == null) {
			return getDataName();
		}
		if (pamDataBlock.getParentProcess().getPamControlledUnit() == null) {
			return getDataName();
		}
		return pamDataBlock.getParentProcess().getPamControlledUnit().getUnitName();
	}

	/**
	 * 
	 * @return Get the data provider. References to this are 
	 * needed when settings are serialised and de-serialised.  
	 */
	public final TDDataProviderFX getDataProvider() {
		return tdDataProvider;
	}

	/**
	 * Get scale information based on the min max values of units in the data block or fixed scale information
	 * @param autoScale. Create a scale information which is based on min/max value of data units in data block. 
	 * False to use fixed scale information. 
	 * @param orientation. Orientation of the display. 
	 * @return Information about the scales of the data. 
	 */
	public TDScaleInfo getScaleInfo(boolean autoScale) {

		if (autoScale == false && getScaleInfo() != null) {
			return getScaleInfo();
		}


		int n = 0;
		double minVal = Double.MAX_VALUE;
		double maxVal = Double.MIN_VALUE;
		synchronized (pamDataBlock.getSynchLock()) {
			if (pamDataBlock.getUnitsCount() == 0) {
				return getScaleInfo();
			}			

			@SuppressWarnings("unchecked")
			ListIterator<PamDataUnit> it = pamDataBlock.getListIterator(0);
			while(it.hasNext()) {
				PamDataUnit aUnit = it.next();
				Double val = getDataValue(aUnit);
				if (val == null) {
					continue;
				}
				minVal = Math.min(minVal, val);
				maxVal = Math.max(maxVal, val);
				n++;
			}
		}

		if (n == 0) {
			return getScaleInfo();
		}

		minVal = PamAxisFX.getDefaultScaleEnd(minVal, maxVal);
		maxVal = PamAxisFX.getDefaultScaleEnd(maxVal, minVal);

		//have to be careful here when setting axis from the axis class. If reverse then 
		//max and min are swapped. 
		getScaleInfo().setMinVal(minVal);
		getScaleInfo().setMaxVal(maxVal);

		return getScaleInfo();

	}

	/**
	 * Add a scale info. ScaleInfo is used to define which y-axis the data is compatible with. e.g. a 
	 * TDDataINfo may be able to plot amplitude and frequency. 
	 * @param scaleInfo - the scale info to add. 
	 */
	public void addScaleInfo(TDScaleInfo scaleInfo) {
		scaleInfos.add(scaleInfo);
	}
	
	/**
	 * Remove a scale info. ScaleInfo objects are used to define which y-axis the data is compatible with. e.g. a 
	 * TDDataINfo may be able to plot amplitude and frequency. 
	 * @param scaleInfo - the scale info to add. 
	 */
	public void removeScaleInfo(TDScaleInfo scaleInfo) {
		scaleInfos.remove(scaleInfo);
	}
	
	
	/**
	 * @return the fixedScaleInformation
	 */
	public TDScaleInfo getScaleInfo() {
		if (scaleInfoIndex==-1 && scaleInfos.size()>0) scaleInfoIndex=0; 
		return scaleInfos.get(this.scaleInfoIndex);
	}

	/**
	 * Get the number of data dimensions being displayed. 
	 * @return 1 or 2 for the data (lines are 1, things that will make
	 * a 3D plot, such as spectrograms, get 2. 
	 */
	public int getDataDimensions() {
		return 1;
	}

	/**
	 * Get all TDScaleInfos for the TDDataInfo. 
	 * @return a list of TDScaleInfos. 
	 */
	public ArrayList<TDScaleInfo> getScaleInfos() {
		return scaleInfos;
	}

	/**
	 * 
	 * @param tdProjector 
	 * @return a class containing functions to chose a symbol to plot. 
	 */
	abstract public TDSymbolChooserFX getSymbolChooser();

	/**
	 * 
	 * @param pamDataUnit data unit
	 * @return text to display in tooltip if mouse hovered over symbol
	 */
	public String getToolTipText(PamDataUnit pamDataUnit) {
		return pamDataUnit.getSummaryString();
	}

	private Point2D[] lastPoint = new Point2D[PamConstants.MAX_CHANNELS];



	private boolean showing = true;

	/**
	 * Clear any residual drawing objects that get held between calls, 
	 * e.g. the point of the previous unit plotted which may have been 
	 * held so that lines can be drawn between points. 
	 */
	public void clearDraw() {
		lastPoint = new Point2D[PamConstants.MAX_CHANNELS];
	}

	/**
	 * Paint data into the graphics window. 
	 * @param plotNumber plot number
	 * @param g graphics
	 * @param windowRect Window rectangle to draw in
	 * @param orientation orientation
	 * @param pamAxis scroll start time in milliseconds
	 * @param tScale time scale in pixels per millisecond. 
	 * @param graphAxis graph data axis for scaling. 
	 * @param wrap. true to wrap display, false to show display scrolling. 
	 */
	public void drawData(int plotNumber, GraphicsContext g, double scrollStart, TDProjectorFX tdProjector) {
		/*
		 *  default behaviour is to draw individual data units.
		 *  Arranged so that this main draw function can be overridden, but that
		 *  it's still possible to get to the underlying draw functionality.  
		 */
		drawAllDataUnits(plotNumber, g, scrollStart, tdProjector);
	}

	/**
	 * Paint individual data units in the graphics window. 
	 * <p>
	 * Note the scroll start is a double instead of a long. The maximum time value for a double is 17 August 292278994 at 07:12:55 UTC. 
	 * If PAMGuard is going by then soomeone else can sort out timing. 
	 * @param plotNumber - plot number i.e. which subplot the data should be drawn on. 
	 * @param g - the graphics handle. 
	 * @param scrollStart - the start of the display in milliseconds. Note that this is a double value. 
	 * @param tdProjector - the projector which handles unit to pixel conversion. 
	 */
	public void drawAllDataUnits(int plotNumber, GraphicsContext g, double scrollStart, TDProjectorFX tdProjector) {
		
//		System.out.println("Max double value: " +  PamCalendar.formatDateTime((long)  Double.MAX_EXPONENT)); 
//		System.out.println("Max long value: " +  PamCalendar.formatDateTime((long)  Long.MAX_VALUE)); 

		
		PamDataUnit dataUnit = null;
		//			Point pt;
		//			PamSymbol symbol;
		int count=0; 
		drawCalls=0;
		
		synchronized (pamDataBlock.getSynchLock()) {

			//FIXME - shouldn;t have to clear every time but seems like we do? 
			pamDataBlock.clearChannelIterators();

//			scrollStart = PamCalendar.getTimeInMillis();
			//work out start and stop times
			long loopEnd = (long) (scrollStart + (tdProjector.getVisibleTime() * 1.05));
			long loopStart = (long) (scrollStart - (tdProjector.getVisibleTime() * .05));

			//find a number close to the index start, 			
			ListIterator<PamDataUnit> it = getUnitIterator( loopStart,  plotNumber);

			clearDraw();

			while (it.hasNext()) {
				dataUnit = it.next();
				count++; 
				if (dataUnit!=null && shouldDraw(plotNumber, dataUnit)) {
					if (dataUnit.getEndTimeInMilliseconds() < loopStart) {
						continue;
					}
					if (dataUnit.getTimeMilliseconds() > loopEnd) {
						break;
					}
					
					drawDataUnit(plotNumber, dataUnit, g,  scrollStart, 
							tdProjector ,TDSymbolChooserFX.NORMAL_SYMBOL);
				}
			}
			
			lastUnitDrawn(g, scrollStart, tdProjector, plotNumber); 
		}
		//System.out.println("Total data units: " + count+ " draw calls: " +drawCalls );
	}
	
	/**
	 * Indicates that the last data unit has been drawn. This can be useful in 
	 * TDDataInfo's which have more bespoke drawing mechanisms. 
	 * @param plotnumber - plot number i.e. which sub plot the data should be drawn on. 
	 */
	public void lastUnitDrawn(GraphicsContext g, double scrollStart, TDProjectorFX tdProjector, int plotnumber) {

	}

	/**
	 * Gets the list iterator starting at the correct data unit for the display.
	 * Note that this may be off the screen slightly. It is a rough start found in
	 * the fastest way possible in order to optimise repaint speed.
	 * 
	 * @param loopStart
	 *            - the start time of the loop
	 * @return the list iterator.
	 */
	private ListIterator<PamDataUnit> getUnitIterator(long loopStart, int plotNumber) {
		
		if (pamDataBlock.getFirstUnit()==null || this.pamDataBlock.getUnitsCount()<100) {
			//don't over complicate if not needed
			return pamDataBlock.getListIterator(0);
		}
		
		//the skip factor
		int skip = 20; 

		//figure out whether to go backwards or forwards. If the time is nearer the end then want to go backwards. 
		long startUnit = pamDataBlock.getFirstUnit().getTimeMilliseconds();
		long endUnit = pamDataBlock.getLastUnitMillis();

		boolean startStart = true;  //start at start
		if (Math.abs(endUnit-loopStart)<Math.abs(startUnit-loopStart)) startStart=false;// start at end

		int itstart= startStart ? 0 : -1; //-1 starts at end

		ListIterator<PamDataUnit> it; 
		if (this.getScaleInfo().getPlotChannels()[plotNumber]==0) it = pamDataBlock.getListIterator(itstart);
		// we may be plotting data with sequence numbers, so use SequenceIterator just in case
		else  it = pamDataBlock.getSequenceIterator(this.getScaleInfo().getPlotChannels()[plotNumber], itstart); 

		//now blast through the list as quickly as possible only check certian times. 
		int ncount=0; 

		if (startStart) {
			//move forwards
			while (it.hasNext()) {
				ncount++;
				if (ncount%skip==0){
					if (it.next().getTimeMilliseconds()>loopStart) {
						//might have gone over the loop start so need to go back skip units.
						for (int i=0; i<skip; i++) {
							it.previous();
						}
						return it; 
					}
				}
				else it.next();
			}
		}
		else {
			//move backwards
			while (it.hasPrevious()) {
				ncount++;
				if (ncount%skip==0){
					if (it.previous().getTimeMilliseconds()<loopStart)
						return it;
				}
				else it.previous();
			}
		}

		return it;
	}


	/**
	 * Do we want to draw this data unit on this plot ? 
	 * @param plotNumber
	 * @param dataUnit
	 * @return true if unit should be drawn. 
	 */
	public boolean shouldDraw(int plotNumber, PamDataUnit dataUnit) {
		return shouldDraw(plotNumber, dataUnit.getSequenceBitmap());
	}

	/**
	 * Do we want to draw this channel on this panel ? 
	 * @param plotNumber panel number
	 * @param sequenceMap channel map
	 * @return true if data unit should be drawn. 
	 */
	public boolean shouldDraw(int plotNumber, int sequenceMap) {
		if (sequenceMap == 0) return true;
		if (tdGraph.getNPanels() <= 1) {
			return true;
		}
		if (this.getScaleInfo().getPlotChannels()[plotNumber] == 0) {
			return true;
		}
		if ((this.getScaleInfo().getPlotChannels()[plotNumber] & sequenceMap) > 0) {
			return true;
		}
		return false;
	}

	/**
	 * Draw highlighted data.
	 * @param plotNumber
	 * @param g
	 * @param windowRect
	 * @param orientation
	 * @param timeAxis
	 * @param scrollStart
	 * @param graphAxis
	 */
	public void drawHighLightData(int plotNumber, GraphicsContext g, double scrollStart, TDProjectorFX tdProjector){

		//		System.out.println("TDDataInfoFX:  DetectionGroupSummary highlightedUnits: "  + tdGraph.getOverlayMarkerManager().getCurrentMarker().getSelectedDetectionGroup());

		//draw all the highlighted units; 
		DetectionGroupSummary highlightedUnits = tdGraph.getOverlayMarkerManager().getCurrentMarker().getSelectedDetectionGroup();
		if (highlightedUnits==null) return;
		synchronized (highlightedUnits){
			PamDataUnit foundDataUnit; 
			for (int i=0; i<highlightedUnits.getDataList().size(); i++){
				foundDataUnit=highlightedUnits.getDataList().get(i);
				//				System.out.println("TDDataInfoFX: highlighted: " + highlightedUnits.getFocusedIndex());
				//we don't want to draw highlighted data if it's been marked within an area- this will just clutter things up.
				if (this.getDataBlock()==foundDataUnit.getParentDataBlock() && i==highlightedUnits.getFocusedIndex()){
					//					System.out.println("TDDataInfoFX: Paint highlighted mark: "+ foundDataUnit);
					drawDataUnit(plotNumber, foundDataUnit, g, scrollStart, tdProjector, TDSymbolChooserFX.HIGHLIGHT_SYMBOL);
				}
				else if (this.getDataBlock()==foundDataUnit.getParentDataBlock()){
					drawDataUnit(plotNumber, foundDataUnit, g, scrollStart, tdProjector, TDSymbolChooserFX.HIGHLIGHT_SYMBOL_MARKED);
				}
			}
		}
	}

	int drawCalls = 0;


	private PamObserver selfObserver;
	

	
	/**
	 * Draw a data unit. 
	 * @param pamDataUnit data unit to draw
	 * @param g graphicsContext handle to draw on
	 * @param windowRect the rectangle describing the window.  
	 * @param orientation orientation of the display
	 * @param timeAxis start time of the display
	 * @param timeScale time scale in pixels per millisecond
	 * @param yAxis yAxis (used for scale information for the data point)
	 * @param type flag for which type of symbol to draw. e.g normal or highlighted. 
	 * @param true if it is the last data unit in the list which is being drawn. 
	 * @return polygon of area drawn on. 
	 */
	public Polygon drawDataUnit(int plotNumber,PamDataUnit pamDataUnit, GraphicsContext g, 
			double scrollStart, TDProjectorFX tdProjector, int type) {
		
		Double val = getDataValue(pamDataUnit);	
		
		if (val == null) {
			return null;
		}

		long timeMillis=pamDataUnit.getTimeMilliseconds();

		//		if (timeMillis<scrollStart){
		//			return null;
		//		}
		//		long scrollend = (long) (scrollStart+tdProjector.getVisibleTime());
		//		if (timeMillis > scrollend) {
		//			return null;
		//		}

		double tC=tdProjector.getTimePix(timeMillis-scrollStart);

//		System.out.println("TDDataInfoFX: tc: "+tC+ " dataUnitTime: "+PamCalendar.formatTime(timeMillis)+" scrollStart: "
//		+PamCalendar.formatTime((long) scrollStart)+" (timeMillis-scrollStart)/1000. "+((timeMillis-scrollStart)/1000.));
		
		if (tC < 0 || tC>tdProjector.getWidth()) {
			return null;
		}

		double dataPixel = tdProjector.getYPix(val);
		//tC=windowRect.getWidth()-tC; //TODO-wrapping- how do we manage that?
		Coordinate3d c = tdProjector.getCoord3d(timeMillis, val, 0);
		Point2D pt;
		if (tdProjector.getOrientation() == Orientation.HORIZONTAL) {
			pt = new Point2D((int) tC, (int) dataPixel);
		}
		else {
			pt = new Point2D(dataPixel, tC);
		}
		if (pt.getX() < -20 || pt.getX() > tdProjector.getWidth() + 20) return null;

		TDSymbolChooserFX symbolchoser = getSymbolChooser();

		if (symbolchoser ==null) {
			return null;
		}

		//System.out.println("TDDataInfoFX: dataPixelPoint: " + dataPixel + "  " + pt); 

		if ((symbolchoser.getDrawTypes(pamDataUnit) & TDSymbolChooserFX.DRAW_SYMBOLS) != 0) {
			symbolchoser.getPamSymbol(pamDataUnit,type).draw(g, pt);
			if (type!=TDSymbolChooserFX.HIGHLIGHT_SYMBOL && type!=TDSymbolChooserFX.HIGHLIGHT_SYMBOL_MARKED ){
				//System.out.println("Draw CPOD data: HOVER");
				tdProjector.addHoverData(new HoverData(c, pamDataUnit, 0, plotNumber));
			}
		}


		//now draw a line between data units. Here the line is simply sequential but lines can be alterred by overriding 
		//the getPreviousDataUnit() function. 
		//		int firstChannel = Math.max(PamUtils.getLowestChannel(pamDataUnit.getChannelBitmap()), 0);
		int firstChannel = Math.max(PamUtils.getLowestChannel(pamDataUnit.getSequenceBitmap()), 0);

		drawCalls++;
		if ((symbolchoser.getDrawTypes(pamDataUnit) & TDSymbolChooserFX.DRAW_LINES) != 0) {
			//WARNING<- the getPreviousDataUnit function is currently very slow and caused serious issue
			//in display speed. Onyl use draw lines with super detections if data units are very sparse. 
			// For example not suitbale for the the click detector. 
			Point2D lastDrawPoint=getPreviousDataUnit(tdProjector, pamDataUnit); 
			if (lastDrawPoint!= null)
			g.setFill(symbolchoser.getPamSymbol(pamDataUnit,type).getLineColor());
			// only draw the line if it's moving forward, no tbackwards to work ok when scroll wrapping
			//			if (Math.abs(pt.getX() - lastPoint[firstChannel].getX()) > 100) {
			//				System.out.printf("Gap %d %3.1f to %3.1f\n", drawCalls, lastPoint[firstChannel].getX(), pt.getX());
			//			}
			if (lastDrawPoint != null &&pt.getX() >= lastDrawPoint.getX()) {
				g.strokeLine(lastDrawPoint.getX(), lastDrawPoint.getY(), pt.getX(), pt.getY());
			}
		}
		//for speed and convenience just keep a record of all the last points. 
		lastPoint[firstChannel] = pt;

		return null;
	}
	
	
	


	/**
	 * Get the previous data unit for drawing a line from. This can be overriden for data units. 
	 * @return the previous point on the display to draw from. 
	 */
	public Point2D getPreviousDataUnit(TDProjectorFX generalProjector, PamDataUnit pamDataUnit){
		int firstChannel = Math.max(PamUtils.getLowestChannel(pamDataUnit.getChannelBitmap()), 0);
		return lastPoint[firstChannel]; 
	}


	//	/**
	//	 * Check whether a data unit contains channels that can be plotted on the plot pane. 
	//	 * @return true if data unit can be plotted. 
	//	 */
	//	public boolean canPlotDataUnit(int plotNumber, PamDataUnit pamDataUnit){
	//		//figure out if the click should be drawn on this plot pane. The click has to contain at least one channel the plotPane shows. 
	//		int plotChanBitMap=getTDGraph().getCurrentScaleInfo().getPlotChannels()[plotNumber]; 
	//		//perform bitwise AND operation- if result is more than zero then at least one bit in same pos overlap i.e. one of the clicks in the data unit should be plotted. 
	//		if ((pamDataUnit.getSequenceBitmap() & plotChanBitMap)==0){
	//			return false; 
	//		}	
	//		return true; 
	//	}

	/**
	 * Called when the user selects a specific data line
	 * @param dataLine
	 */
	public void selectScaleInfo(TDScaleInfo dataLine) {
		for (int i=0; i<this.scaleInfos.size(); i++){
			if (dataLine.getDataTypeInfo().equals(scaleInfos.get(i).getDataTypeInfo())){
				scaleInfoIndex=i; 
				return; 
			}
		}
		scaleInfoIndex = -1; 
	}

	/**
	 * The index of the current scale info for the TDDataInfo
	 * @return the current index of the scale info. 
	 */
	public int getScaleInfoIndex() {
		return scaleInfoIndex;
	}

	/**
	 * Has options that will result in a dialog. 
	 * @return true if there are options. 
	 */
	public boolean hasOptions() {
		return false;
	}

	/**
	 * Edit options - generally involves throwing up a 
	 * dialog of some sort. 
	 * @return true if options dialog OK button was pressed. 
	 */
	public boolean editOptions() {
		return false;
	}

	/**
	 * @return the pamDataBlock
	 */
	protected PamDataBlock getPamDataBlock() {
		return pamDataBlock;
	}

	/**
	 * @return the tdDataProvider
	 */
	protected TDDataProviderFX getTdDataProvider() {
		return tdDataProvider;
	}

	/**
	 * Return a hiding settings node which will get incorporated into 
	 * a larger tabbed sliding pane. 
	 * @return sliding dialog component. 
	 */
	public TDSettingsPane getGraphSettingsPane() {
		return null;
	}

	/**
	 * Get the TDGraphFX associated with this TDDataInfoFX
	 * @return the tdGraph
	 */
	public TDGraphFX getTDGraph() {
		return tdGraph;
	}

	/**
	 * Get an object that will be packed up with the serialised settings
	 * when information about a plot get's stored. <br>
	 * The Object MUST implement serializable and ideally also cloneable
	 * or it will go belly up !<br>
	 * This only gets called when settings are to be saved so there is no 
	 * need to keep parameters this returns up to date except right at
	 * settings save time (when this is called)
	 * @return serializable object to save. 
	 */
	public Serializable getStoredSettings() {
		return null;
	}

	/**
	 * Set settings which have been read back from storage (the psf file).
	 * Assume these are of the right type and cast into whatever is needed !
	 * @param storedSettings
	 * @return true if all OK.
	 */
	public boolean setStoredSettings(Serializable storedSettings) {
		return false;
	}

	/**
	 * Called whenever a data line is removed from a graph, or when a graph is removed from 
	 * a plot. <br>Is a good opportunity for the DataInfo to unsubscribe itself from 
	 * any data blocks it might be observing.
	 */
	public void removeData() {

	}

	/**
	 * Called in viewer mode when the time scroller moves. <p>
	 * Most data won't need to do anything here since they are already
	 * subscribed to the scroller and will get their data loaded 
	 * from psf automatically. 
	 * @param valueMillis new scroll value in millis
	 */
	public void timeScrollValueChanged(double valueMillis) {}

	/**
	 * Called in viewer mode when the time scroll range moves. <p>
	 * Most data won't need to do anything here since they are already
	 * subscribed to the scroller and will get their data loaded 
	 * from psf automatically. 
	 * @param minimumMillis new minimum in millis
	 * @param maximumMillis new maximum in millis. 
	 */
	public void timeScrollRangeChanged(double minimumMillis, double maximumMillis) {}

	/**
	 * Set whether or not data is showing in current graph window. 
	 * @param isShowing true if showing
	 */
	public void setShowing(boolean isShowing) {
		this.showing = isShowing;
	}

	/**
	 * Check whether the data is currently being displayed in the current graph window. 
	 * @return true if showing. 
	 */
	public boolean isShowing() {
		return showing;
	}

	/**
	 * Called when the time range spinner on the main display panel changes. 
	 * @param oldValue old value (seconds)
	 * @param newValue new value (seconds)
	 */
	public void timeRangeSpinnerChange(double oldValue, double newValue) {	
	}

	/**
	 * Check whether in viewer mode. 
	 * @return true if in viewer mode. 
	 */
	public boolean isViewer() {
		return isViewer;
	}

	/**
	 * Function to check whether PAMGUARD is paused. 
	 * @return true if paused - note do not use in viewer mode. 
	 */
	public boolean isPaused(){
		return getTDGraph().getTDDisplay().getTDControl().isPaused();
	}

	/**
	 * Get a colour flag for the background display. This is only ever used if the data units displayed
	 * change the overall background colour of the display e.g. a spectrogram would do this. 
	 * @return flag indicating the background colour of the display. Option are LIGHT_TD_DISPLAY or DARK_TD_DISPLAY
	 */
	public int getDisplayColType(){
		return TDGraphFX.LIGHT_TD_DISPLAY; 
	}

	/**
	 * Get the source data block. 
	 * <br>
	 * Generally the source data block is used in viewer mode to load data. e.g. FFT uses raw sound acquisition for source data block. The 
	 * source data block can cause issues e.g. the whistle and moan contours use noise free FFT data as a source data block. Thus if this function is 
	 * is not overridden in WhistlePlotInfo then raw FFT data will be saved, something which uses large amounts of memory. 
	 * @see WhistlePlotInfo
	 * @return the source data block. 
	 */
	public PamDataBlock getSourceDataBlock() {
		return getDataBlock().getSourceDataBlock();
	}

	/**
	 * Sets the scale info to tell TD graph to show the correct number of plot panes and the correct channels in those plot panes. e.g. if 'dataBlock' is a RawDataBlock
	 * and 'singlePane' is false then the TDScaleInformation will be set so that there is a single plot for each channel in the dataBlock. For data blocks with grouped channels then each
	 * group will get it's own pane if singlePane is set to false. 
	 * @param dataBlock - data block to find channels in.
	 * @param singlePane true to plot on single pane. False means every channel gets it's own pane. If channels are grouped then this may produce some weird effects.
	 */
	public static TDScaleInfo setNPlotPanes(TDScaleInfo tdScaleInfo, PamDataBlock dataBlock, boolean singlePane){
		int[] plotChannels=new int[PamConstants.MAX_CHANNELS];
		if (singlePane){
			//plot on only one pane. 
			tdScaleInfo.setnPlots(1);
			plotChannels[0]=dataBlock.getSequenceMap();
		}
		else {
			//one channel for one pane. 
			int[] chans=PamUtils.getChannelArray(dataBlock.getSequenceMap());
			if (chans==null) return tdScaleInfo;
			tdScaleInfo.setnPlots(chans.length);
			for (int i=0; i<chans.length; i++){
				int[] singleChan={chans[i]};
				plotChannels[i]=PamUtils.makeChannelMap(singleChan); 
			}
		}

		tdScaleInfo.setPlotChannels(plotChannels);
		return tdScaleInfo;
	}


	/**
	 * Notify of changes from PamController. 
	 * @param changeType - the chnage type. 
	 */
	public void notifyChange(int changeType){

	}

	private class TDDataObserver extends PamObserverAdapter {

		@Override
		public long getRequiredDataHistory(PamObservable o, Object arg) {
			PamObserver dataObserver = tdGraph.getTDDisplay().getTDControl().getDataObserver();
			long nonminalTime = dataObserver.getRequiredDataHistory(o, arg);
			return TDDataInfoFX.this.getRequiredDataHistory(o, arg, nonminalTime);
		}

		@Override
		public String getObserverName() {
			return tdGraph.getUniqueName();
		}

	}
	
	/**
	 * True if the scroller is changing
	 * @return true if the scroller is changing. 
	 */
	public boolean isScrollChanging(){
		return this.getTDGraph().getTDDisplay().getTimeScroller().isScrollerChanging();
	}

	/**
	 * Get how long data of this type needs to be held in memory during real 
	 * time operation. the third argument is the standard history length of the 
	 * display scroller so most often it can simply return this value. Occasionally, 
	 * such as in the case of spectrogram data which we don't want to save because they
	 * go into a separate store as an image, we can return 0. 
	 * @param o PamObservable - what's observing the data
	 * @param arg optional argument
	 * @param nonminalTime nominal storage time in milliseconds. 
	 * @return storage time in milliseconds. 
	 */
	public long getRequiredDataHistory(PamObservable o, Object arg, long nonminalTime) {
		return nonminalTime;
	}


	//	/**
	//	 * See if this data type has a particular data unit
	//	 * @param unitType Type of unit
	//	 * @return true or false. 
	//	 */
	//	public boolean hasDataUnits(String unitType) {
	//		String[] allUnits = getAllDataUnits();
	//		if (allUnits == null) {
	//			return false;
	//		}
	//		for (int i = 0; i < allUnits.length; i++) {
	//			if (allUnits.equals(unitType)) {
	//				return true;
	//			}
	//		}
	//		return false;
	//	}

	/**
	 * Get the last draw points. The array represents all channels (plot panes) 
	 * in sequential order
	 * @return the lastPoint
	 */
	public Point2D[] getLastDrawPoints() {
		return lastPoint;
	}

	public PamObserver getDataObserver() {
		return selfObserver;
	}
	
	/**
	 * Flag to indicate that some viewer mode is loading. 
	 */
	protected boolean isOrderring = false; 
	
	/**
	 * A data load observer. 
	 * @author Jamie Macaulay 
	 *
	 */
	public class DataLoadObserver implements LoadObserver {

		@Override
		public void setLoadStatus(int loadState) {
//			System.out.println("FFTPlotInfo: FFT Load Observer state: " + loadState);
			if (loadState==PamDataBlock.REQUEST_DATA_LOADED ||
					loadState==PamDataBlock.REQUEST_DATA_PARTIAL_LOAD ||
					loadState==PamDataBlock.REQUEST_NO_DATA){
				//repaint once all data has been loaded. Prevents a little white space at
				//the end of the spectrogram. 
				isOrderring=false; 
				Platform.runLater(()->{
					getTDGraph().repaint(0);
				});
			}
			isOrderring=false;
		}
	}

	/**
	 * Get TD specific menu items which are added to pop up menus if a data unit or
	 * data unit within a selected group of data units belongs to the data info.
	 * 
	 * @return a list of data info specific menu items to add. 
	 */
	public  ArrayList<OverlayMenuItem> getMenuNodes(){
		return null; 
	}


	/**
	 * The TDDisplayFX scroller is based on a master clock update from PAMGuard. This can be irritating because
	 * the master clock will always be slighter greater than the the processed data leaving a gap at the scroller
	 * end. Some data infos with continuous data can act as a better clock (for example the spectrgram or raw data). 
	 * Data which is discrete like the click detector cannot because they don't have contious data. 
	 */
	public long getMasterClockOverride() {
		return -1;
	}






}