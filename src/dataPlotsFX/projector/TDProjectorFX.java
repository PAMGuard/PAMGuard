package dataPlotsFX.projector;

import java.util.List;

import PamUtils.Coordinate3d;
import PamUtils.LatLong;
import PamUtils.PamCoordinate;
import PamUtils.PamUtils;
import PamView.HoverData;
import dataPlotsFX.layout.TDGraphFX;
import javafx.geometry.Orientation;
import javafx.scene.shape.Rectangle;
import pamViewFX.fxNodes.pamAxis.PamAxisFX;

/**
 * Projector which handles all time to pixel conversions for the display. 
 * <p>
 * This is essentially a wrapper for a time axis and a y axis. 
 * 
 * @author Jamie Macaulay
 *
 */
public class TDProjectorFX extends TimeProjectorFX {
	
	
	/*
	 * Some standard names that might get reused - list here
	 * to save having to try to spell them accurately elsewhere. 
	 */
	public static final String UNITS_ANGLE = LatLong.deg;
	
	
	public static final String UNITS_TEMPERATURE = LatLong.deg+".C";
	
	/**
	 * Frequency units
	 */
	public static final String UNITS_FREQ = "Hz";
	
	/**
	 * Reference to the tdGraph. 
	 */
	private TDGraphFX tdGraph;
	
	private double timePix; 
	
	private double yPix;
	
	/**
	 * Holds the pixel dimensions of the graph. 
	 */
	private Rectangle windowRect; 

	public TDProjectorFX(TDGraphFX tdGraph){
		super(); 
		this.tdGraph=tdGraph; 
		windowRect=new Rectangle(); 
		windowRect.widthProperty().bind(tdGraph.getPlotWidthProperty());
		windowRect.heightProperty().bind(tdGraph.getPlotHeightProperty().divide(tdGraph.nPanelsProperty()));
	}
	
	
	public TDProjectorFX(){
		
	}

	/**
	 * Get the time axis. 
	 * @return the time axis. 
	 */
	@Override
	public PamAxisFX getTimeAxis(){
		return tdGraph.getTDDisplay().getTimeAxis(); 
	}
	

	/**
	 * Get the pixel value for the y Co-Ordinate. 
	 * @param yVal
	 * @return
	 */
	public double getYPix(double yVal){
		if (tdGraph!=null && tdGraph.getCurrentScaleInfo()!=null) {
			double div = tdGraph.getCurrentScaleInfo().getUnitDivisor();
			return yPix = getYAxis().getPosition(yVal/div);
		}
		else return yPix = getYAxis().getPosition(yVal); 
	}

	
	/**
	 * Get the time axis. 
	 * @return the time axis. 
	 */
	public PamAxisFX getYAxis(){
		return tdGraph.getGraphAxis();
	}
	
	/**
	 * Get the dimensions of the TDGaph();
	 * :
	 * @return the dimensions of the TDGraph. 
	 */
	public Rectangle getWindow(int iPlot){
		return windowRect;
	}
	

	/**
	 * Get time range in milliseconds. 
	 * @return time range (max-min) in millis. 
	 */
	public double getVisibleTime() {
//		return tdGraph.getTDDisplay().getTimeScroller().getVisibleMillis();
				
		return (getTimeAxis().getMaxVal()-getTimeAxis().getMinVal())*1000.;
	}
	
	/**
	 * Get the minimum time in milliseconds
	 * @return the minimjum time in milliseconds. 
	 */
	public long getMinTime() {
		return (long) (getTimeAxis().getMinVal()*1000);
	}
	
//	/**
//	 * Get the location of the wrap in milliseconds. 
//	 * @param scrollStart - the scroll start
//	 * @return the location of the wrap in milliseconds. 
//	 */
//	public long getWrapMillis(long scrollStart) {
//		return this.tdGraph.getTDDisplay().getWrapMillis(scrollStart);
//	}
	
	/**
	 * Get the length of the time axis in pixels. 
	 * @return the length of the axis in pixel. 
	 */
	public double getGraphTimePixels(){
		return getTimeAxis().getPosition(getTimeAxis().getMaxVal()); 
	}


	/**
	 * Get the location in pixels for a time in millis.
	 * @param timeMillis - the time along the axis in millis (Note: the axis start at timeMillis=0).
	 * @return - the pixel location along the x axis (if horizontal)
	 */
	@Override
	public double getTimePix(double timeMillis){
		double timePix = getTimeAxis().getPosition((timeMillis)/1000.);
		if (isWrap()){	
			//stops a small portion of the spectrogram showing at the end of the display
			if (timePix<0) {
				return timePix - 30;
			}
			else if (timePix>getWidth()){
				return timePix + 30; //this will never be painted. 
			}
			else return PamUtils.constrainedNumber(this.tdGraph.getWrapPix()+timePix, getWidth());
		}
		else {
			return timePix; 		
	}
		
//		System.out.println("timeMillis: " + timeMillis + " timePix: "+timePix);	
		
//		if (isWrap()){
//			long tmillisVal=timeMillis-tdGraph.getLastWrapMillis();
//			
//			System.out.println("TDDataInfo: " +tmillisVal + " timeAxis.getMaxVal()*-1000 "+(getTimeAxis().getMaxVal()*-1000));
//			if (tmillisVal<0){
//				tmillisVal=(long) (getTimeAxis().getMaxVal()*1000+(timeMillis-tdGraph.getLastWrapMillis()));
//			}
//			timePix=getTimeAxis().getPosition(tmillisVal/1000.); 
//		}
//		else{
//			timePix = getTimeAxis().getPosition((timeMillis-scrollStart)/1000.);
//		}
	}

	@Override
	public Coordinate3d getDataPosition(PamCoordinate screenPosition) {
		PamAxisFX dataAxis = tdGraph.getGraphAxis();
		double y;
		double x;
		try {
			int xAx = tdGraph.getTDDisplay().getTimeAxisIndex();
			y = dataAxis.getDataValue(screenPosition.getCoordinate(1-xAx))*tdGraph.getCurrentScaleInfo().getUnitDivisor();
			double xPix = screenPosition.getCoordinate(xAx);
			if (isWrap()){		
				xPix -= tdGraph.getWrapPix();
//			xPix += getWidth();
				if (xPix < 0) {
					xPix += getWidth();
				}
				x = getTimeAxis().getDataValue(xPix) * 1000 + tdGraph.getScrollStart();
//			System.out.printf(" x coord data point = %s, scroll Start %s\n", PamCalendar.formatDateTime((long) x),
//					PamCalendar.formatTime(tdGraph.getScrollStart()));
			}
			else {
				 x = getTimeAxis().getDataValue(screenPosition.getCoordinate(xAx)) * 1000; // time in seconds from display start.
				 x += tdGraph.getScrollStart(); 
			}
		} catch (Exception e) {
//			System.out.println("Warning - cannot determine mouse position");
			return null;
		}
		return new Coordinate3d(x, y);
	}

	@Override
	public Coordinate3d getCoord3d(PamCoordinate dataObject) {
//		PamAxisFX dataAxis = tdGraph.getGraphAxis();
//		int xAx = tdGraph.getTDDisplay().getTimeAxisIndex();
//		double y = dataAxis.getPosition(dataObject.getCoordinate(1-xAx) / tdGraph.getCurrentScaleInfo().getUnitDivisor());
//		double x = dataObject.getCoordinate(xAx) - tdGraph.getScrollStart();
//		x = getTimeAxis().getPosition(x/1000.);
//		return new Coordinate3d(x, y);
		return getCoord3d(dataObject.getCoordinate(0), dataObject.getCoordinate(1), 
				dataObject.getNumCoordinates()<=2 ? dataObject.getCoordinate(2) : 0);
	}
	
	@Override
	public Coordinate3d getCoord3d(double timeMillis, double dataValue, double d3) {
	
//		timePix=getTimePix((long) timeMillis); 
//		double x = timeMillis - tdGraph.getScrollStart();
//		timePix = getTimeAxis().getPosition(x/1000.);
		timePix = getTimePix(timeMillis - tdGraph.getScrollStart());
		
		yPix= getYPix(dataValue); 
		switch(tdGraph.getTDDisplay().getTimeAxisIndex()) {
		case 0:
			return new Coordinate3d(timePix, yPix); 
		case 1:
			return new Coordinate3d(yPix, timePix); 
		}
		return null; // should never happen. 
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String getYAxisName() {
		return String.format("%s (%s)", getAxisName(getParmeterType(2)), getUnitName(getParmeterUnits(2)));
	}
	
	public boolean isWrap(){
		return (tdGraph.getTDDisplay().getTDControl().isStopped() || tdGraph.getTDDisplay().isViewer()) ? false :  tdGraph.getTDDisplay().getTDParams().wrap;
	}
	
	/**
	 * Get the string name for axis. 
	 * @param axisType - the axis type 
	 * @return the axis type string. 
	 */
	public static String getUnitName(ParameterUnits axisType){
		if (axisType == null) {
			return null;
		}
		return axisType.toString(); 
		
//		String axisUnits=""; 
//		switch (axisType){
//		case DB:
//			//axisUnits="dB re 1" + "\u00B5" +"Pa"; 
//			axisUnits=PamController.PamController.getInstance().getGlobalMediumManager().getdBRefString(); 
//			break;
//		case DECIMALDEGREES:
//			axisUnits=UNITS_ANGLE; 
//			break;
//		case DEGREES:
//			axisUnits=UNITS_ANGLE; 
//			break;
//		case HZ:
//			axisUnits="Hz"; 
//			break;
//		case METERS:
//			axisUnits="m"; 
//			break;
//		case NMILES:
//			axisUnits="NM"; 
//			break;
//		case RADIANS:
//			axisUnits="rad"; ;
//		case RAW:
//			axisUnits="u"; ;
//			break;
//		case SECONDS:
//			axisUnits="s"; ;
//			break;
//		default:
//			break;
//		}
//		return axisUnits; 
	}
	
	
	/**
	 * Get the string name for axis. 
	 * @param axisType - the axis type
	 * @return the axis type string. 
	 */
	public static String getAxisName(ParameterType axisType){
		if (axisType == null) {
			return null;
		}
		return axisType.toString(); 
		
//		String axisName=""; 
//		switch (axisType){
//		case AMPLITUDE:
//			 axisName="Amplitude"; 
//			break;
//		case BEARING:
//			 axisName="Bearing"; 
//			break;
//		case FREQUENCY:
//			 axisName="Frequency"; 
//			break;
//		case LATITUDE:
//			 axisName="Latitude"; 
//			break;
//		case LONGITUDE:
//			 axisName="Longitude"; 
//			break;
//		case RANGE:
//			axisName="Range"; 
//			break;
//		case SLANTANGLE:
//			axisName="Slant Angle"; 
//			break;
//		case TIME:
//			axisName="Time"; 
//		case ICI:
//			axisName="Inter-click-interval"; 
//			break;
//		case SLANTBEARING:
//			axisName="Slant Angle"; 
//			break;
//		case AMPLITUDE_STEM:
//			axisName="Amplitude (stem)"; 
//			break;
//		default:
//			break;
//		}
//		return axisName;
	}

	public Orientation getOrientation() {
		return tdGraph.getOrientation();
	}

	/**
	 * Get the width of the display. 
	 * @return the width of the display in pixels.
	 */
	public double getWidth() {
		return windowRect.getWidth();
	}
	
	/**
	 * Get the height of the display. 
	 * @return the height of the display in pixels.
	 */
	public double getHeight() {
		return windowRect.getHeight();
	}
	
	/**
	 * The window rectangle in pixels.
	 * @return the window rectangle in pixels. 
	 */
	public Rectangle getWindowRect() {
		return this.windowRect;
	}

	
	public synchronized List<HoverData> getHoverDataList() {
		//System.out.println("TDProjectorFX: Hover list: " + super.getHoverDataList().size()); 
		return super.getHoverDataList();
	}
	
	/* (non-Javadoc)
	 * @see PamView.GeneralProjector#clearHoverList()
	 */
	@Override
	public synchronized void clearHoverList() {
		super.clearHoverList();
	}
	
	public void setWindowRect(Rectangle windowRect) {
		this.windowRect=windowRect; 
	}

}
