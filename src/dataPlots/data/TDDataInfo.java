package dataPlots.data;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import dataPlots.layout.TDGraph;
import dataPlotsFX.data.TDScaleInfo;
import pamScrollSystem.PamScroller;
import Layout.PamAxis;
import PamController.PamController;
import PamUtils.LatLong;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.hidingpanel.HidingDialogComponent;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

/**
 * Information about a type of data that can be displayed on any of the plots. 
 * <br>Life is complicated since many data types can display many different things, 
 * e.g. amplitude / bearing / ici, etc. <br>
 * Therefore getDataUnits can return multiple types of units <br>
 * Life is even more complicated since data cannot only be identified by their units, 
 * e.g. three channels of accelerometer data in dtag data. 
 * 
 * @author Doug Gillespie
 *
 */
public abstract class TDDataInfo {

	/*
	 * Some standard names that might get reused - list here
	 * to save having to try to spell them accurately elsewhere. 
	 */
	public static final String UNITS_ANGLE = LatLong.deg;
	public static final String UNITS_TEMPERATURE = LatLong.deg+".C";
	
	private PamDataBlock pamDataBlock;
	private int currentDataLineIndex; // index into the dataLineInfos list. 
	private ArrayList<DataLineInfo> dataLineInfos = new ArrayList<>();
	private TDScaleInfo fixedScaleInformation = new TDScaleInfo(-1, 1, null, null);
	private TDDataProvider tdDataProvider;
	private TDGraph tdGraph;
	private boolean isViewer;
	
	public TDDataInfo(TDDataProvider tdDataProvider, TDGraph tdGraph, PamDataBlock pamDataBlock) {
		super();
		this.tdDataProvider = tdDataProvider;
		this.tdGraph = tdGraph;
		this.pamDataBlock = pamDataBlock;
		this.isViewer = (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW);
	}

	/**
	 * Get the datablock for this information
	 * @return
	 */
	public PamDataBlock getDataBlock() {
		return pamDataBlock;
	}
	
	/**
	 * Gets a value for a specific data unit which should be in the
	 * same units as the scale information. This will then be 
	 * converted into a plot position by the TDGraph. 
	 * @param pamDataUnit
	 * @return data value or null if this data poit should not be plotted. 
	 */
	abstract public Double getDataValue(PamDataUnit pamDataUnit);
	
	/**
	 * Short string names of the dimensions of the data. This will be 
	 * something like Bearing, Angle, Amplitude, ICI, etc. Each graph
	 * will only be able to contain data which have the same units. 
	 * @return name of the units for the data. 
	 */
	public ArrayList<DataLineInfo> getDataLineInfos() {
		return dataLineInfos;
	}
	
	/**
	 * Add a type of data unit to the list. 
	 * @param unitType String name of the data unit. 
	 */
	public void addDataUnits(DataLineInfo dataLineInfo) {
		dataLineInfos.add(dataLineInfo);
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
	 * Set the current data units - this must be a String which 
	 * matches one of the strings in dataLineInfos() or all will 
	 * go horribly wrong. 
	 * @param AxisName
	 */
	public boolean setCurrentAxisName(String axisName) {
		/*
		 *  see if the currently selected line is OK and 
		 * if it is, leave it alone. Otherwise set to the first 
		 * available line with those units.  
		 */
		DataLineInfo currentLine = getCurrentDataLine();
		if (currentLine != null && currentLine.name.equals(axisName)) {
			return true; // nothing to do !
		}
		currentDataLineIndex = -1;
		for (int i = 0; i < dataLineInfos.size(); i++) {
			if (dataLineInfos.get(i).name.equals(axisName)) {
				selectDataLine(dataLineInfos.get(i));
				currentDataLineIndex = i;
				return true;
			}
		}
		return (currentDataLineIndex >= 0);
	}
	
	/**
	 * 
	 * @param axisName
	 * @return true if the data have a datalineinfo with the given name
	 */
	public boolean hasAxisName(String axisName) {
		for (int i = 0; i < dataLineInfos.size(); i++) {
			if (dataLineInfos.get(i).name.equals(axisName)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @return the name of the currently set data units. 
	 */
	public DataLineInfo getCurrentDataLine() {
		if (currentDataLineIndex < 0 || currentDataLineIndex >= dataLineInfos.size()) {
			return null;
		}
		return dataLineInfos.get(currentDataLineIndex);
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
	 * on standard datablock names. 
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
	public final TDDataProvider getDataProvider() {
		return tdDataProvider;
	}

	/**
	 * 
	 * @param orientation 
	 * @return Information about the scales of the data. 
	 */
	public TDScaleInfo getScaleInformation(int orientation, boolean autoScale) {
		if (autoScale == false && getFixedScaleInformation(orientation) != null) {
			return getFixedScaleInformation(orientation);
		}
		int n = 0;
		double minVal = Double.MAX_VALUE;
		double maxVal = Double.MIN_VALUE;
		synchronized (pamDataBlock.getSynchLock()) {
			if (pamDataBlock.getUnitsCount() == 0) {
				return getFixedScaleInformation(orientation);
			}			
			
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
			return getFixedScaleInformation(orientation);
		}
		minVal = PamAxis.getDefaultScaleEnd(minVal, maxVal);
		maxVal = PamAxis.getDefaultScaleEnd(maxVal, minVal);
		return new TDScaleInfo(minVal, maxVal, null, null);
	}
	
	/**
	 * @return the fixedScaleInformation
	 */
	public TDScaleInfo getFixedScaleInformation(int orientation) {
		return fixedScaleInformation;
	}

	/**
	 * @param fixedScaleInformation the fixedScaleInformation to set
	 */
	public void setFixedScaleInformation(TDScaleInfo fixedScaleInformation) {
		this.fixedScaleInformation = fixedScaleInformation;
	}

	/**
	 * 
	 * @return 1 or 2 for the data (lines are 1, things that will make
	 * a 3D plot, such as spectrograms, get 2. 
	 */
	public int getDataDimensions() {
		return 1;
	}
	
	/**
	 * 
	 * @return a class containing functions to chose a symbol to plot. 
	 */
	abstract public TDSymbolChooser getSymbolChooser();
	
	/**
	 * 
	 * @param pamDataUnit data unit
	 * @return text to display in tooltip if mouse hovered over symbol
	 */
	public String getToolTipText(PamDataUnit pamDataUnit) {
		return pamDataUnit.getSummaryString();
	}
	
	private Point lastPoint;
	
	private boolean showing = true;
	
	/**
	 * Clear any residual drawing objects that get held between calls, 
	 * e.g. the point of the previous unit plotted which may have been 
	 * held so that lines can be drawn between points. 
	 */
	public void clearDraw() {
		lastPoint = null;
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
	 */
	public void drawData(int plotNumber, Graphics g, Rectangle windowRect, int orientation,
			PamAxis timeAxis, long scrollStart, PamAxis graphAxis) {
//		System.out.println("TDDataInfo: print all data"); 
		PamDataUnit dataUnit;
		//			Point pt;
		//			PamSymbol symbol;
		
		synchronized (pamDataBlock.getSynchLock()) {
			ListIterator<PamDataUnit> it = pamDataBlock.getListIterator(0);
			clearDraw();
			while (it.hasNext()) {
				dataUnit = it.next();
				drawDataUnit(dataUnit, g, windowRect, orientation, timeAxis, scrollStart, graphAxis, TDSymbolChooser.NORMAL_SYMBOL);
				//					pt = getDataUnitPoint(dataInfo, dataUnit);
				//					if (pt == null) continue;
				//					symbol = dataInfo.getSymbolChooser().getPamSymbol(dataUnit);
				//					if (symbol == null) continue;
				//					symbol.draw(g, pt);
			}
		}
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
	public void drawHighLightData(int plotNumber, Graphics g, Rectangle windowRect, int orientation,
			PamAxis timeAxis, long scrollStart, PamAxis graphAxis){
		//draw all the highlighted units; 
		synchronized (tdGraph.getSelectedDataUnits()){
			FoundDataUnit foundDataUnit; 
			for (int i=0; i<tdGraph.getSelectedDataUnits().size(); i++){
				foundDataUnit=tdGraph.getSelectedDataUnits().get(i);
				//we don't want to draw highlighted data if it's been marked within an area- this will just clutter things up.
				if (foundDataUnit.dataInfo==this && foundDataUnit.selectionType==FoundDataUnit.SINGLE_SELECTION) drawDataUnit(foundDataUnit.dataUnit, g, windowRect, orientation, timeAxis, scrollStart, graphAxis, TDSymbolChooser.HIGHLIGHT_SYMBOL);
				if (foundDataUnit.dataInfo==this && foundDataUnit.selectionType==FoundDataUnit.MARKED_AREA_DETECTION)  drawDataUnit(foundDataUnit.dataUnit, g, windowRect, orientation, timeAxis, scrollStart, graphAxis, TDSymbolChooser.HIGHLIGHT_SYMBOL_MARKED);
			}
		}
	}

	/**
	 * Draw a data unit. 
	 * @param pamDataUnit data unit to draw
	 * @param g graphics handle to draw on
	 * @param windowRect 
	 * @param orientation orientation of the display
	 * @param timeAxis start time of the display
	 * @param timeScale timescale in pixels per millisecond
	 * @param yAxis yAxis (used for scale information for the data point)
	 * @param type flag for which type of symbol to draw. e.g normal or highlighted. 
	 * @return polygon of area drawn on. 
	 */
	public Polygon drawDataUnit(PamDataUnit pamDataUnit, Graphics g, Rectangle windowRect, int orientation, PamAxis timeAxis, long scrollStart, PamAxis yAxis, int type) {
		
		Double val = getDataValue(pamDataUnit);
		if (val == null) {
			return null;
		}
		double tC = timeAxis.getPosition((pamDataUnit.getTimeMilliseconds()-scrollStart)/1000.);
		if (tC < 0) {
			return null;
		}
		double dataPixel = yAxis.getPosition(val);
		Point pt;
		if (orientation == PamScroller.HORIZONTAL) {
			pt = new Point((int) tC, (int) dataPixel);
		}
		else {
			pt = new Point((int) dataPixel, (int) tC);
		}
		if (pt.x < -20 || pt.x > windowRect.width + 20) return null;
		if ((getSymbolChooser().getDrawTypes() & TDSymbolChooser.DRAW_SYMBOLS) != 0) {
			getSymbolChooser().getPamSymbol(pamDataUnit,type).draw(g, pt);
		}
		if ((getSymbolChooser().getDrawTypes() & TDSymbolChooser.DRAW_LINES) != 0 && lastPoint != null) {
			g.setColor(getSymbolChooser().getPamSymbol(pamDataUnit,type).getLineColor());
			g.drawLine(lastPoint.x, lastPoint.y, pt.x, pt.y);
		}
		lastPoint = pt;
		
		return null;
	}

	/**
	 * Called when the user selects a specific data line
	 * @param dataLine
	 */
	public void selectDataLine(DataLineInfo dataLine) {
		currentDataLineIndex = dataLineInfos.indexOf(dataLine);
	}

	/**
	 * @return the currentDataLineIndex
	 */
	public int getCurrentDataLineIndex() {
		return currentDataLineIndex;
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
	public boolean editOptions(Window frame) {
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
	protected TDDataProvider getTdDataProvider() {
		return tdDataProvider;
	}

	/**
	 * Return a hiding dialog component which will get incorporated into 
	 * a larger tabbed sliding dialog. 
	 * @return sliding dialog component. 
	 */
	public HidingDialogComponent getHidingDialogComponent() {
		return null;
	}
	
	/**
	 * @return the tdGraph
	 */
	public TDGraph getTdGraph() {
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
	 * any datablocks it might be observing.
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
	public void timeScrollValueChanged(long valueMillis) {}
	
	/**
	 * Called in viewer mode when the time scroll range moves. <p>
	 * Most data won't need to do anything here since they are already
	 * subscribed to the scroller and will get their data loaded 
	 * from psf automatically. 
	 * @param minimumMillis new minimum in millis
	 * @param maximumMillis new maximum in millis. 
	 */
	public void timeScrollRangeChanged(long minimumMillis, long maximumMillis) {}

	/**
	 * Set whether or not it's showing in current graph window. 
	 * @param isShowing
	 */
	public void setShowing(boolean isShowing) {
		this.showing = isShowing;
	}

	/**
	 * @return the showing
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
	 * @return the isViewer
	 */
	protected boolean isViewer() {
		return isViewer;
	}
	
	/**
	 * Get notifications from the main controller.
	 * @param changeType
	 */
	public void notifyModelChanged(int changeType) {
		
	}
}
