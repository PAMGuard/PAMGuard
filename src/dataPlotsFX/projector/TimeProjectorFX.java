package dataPlotsFX.projector;

import PamUtils.PamCoordinate;
import PamView.GeneralProjector;
import pamViewFX.fxNodes.pamAxis.PamAxisFX;

/*
 * Projector for a display with a time axis. 
 */
public abstract class TimeProjectorFX extends GeneralProjector<PamCoordinate> {
	
	
	private PamAxisFX timeAxis;


	public TimeProjectorFX(PamAxisFX timeAxis){
		this.timeAxis=timeAxis; 
	} 
	
	public TimeProjectorFX() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Get the time axis 
	 */
	public PamAxisFX getTimeAxis(){
		return timeAxis; 
	}
	
	/**
	 * Set the time axis. 
	 * @param axis - the axis to set. 
	 */
	public void setTimeAxis(PamAxisFX axis) {
		this.timeAxis=axis;
	}
	
	/**
	 * Get time range in milliseconds. 
	 * @return time range (max-min) in millis. 
	 */
	public double getVisibleTime() {
//		return tdGraph.getTDDisplay().getTimeScroller().getVisibleMillis();
				
		return (getTimeAxis().getMaxVal()-getTimeAxis().getMinVal())*1000.;
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
	public double getTimePix(double timeMillis){
		return getTimeAxis().getPosition((timeMillis)/1000.);
	}
	
}
