package detectionPlotFX.projector;

import PamUtils.Coordinate3d;
import PamUtils.PamCoordinate;
import PamView.GeneralProjector;
import detectionPlotFX.layout.DDPlotPane;
import javafx.geometry.Side;
import pamViewFX.fxNodes.pamAxis.PamAxisFX;

/**
 * The detection plot projector deals with all co-ordinate conversion from screen to the pixels
 * and vice versa. This is a generic system throughout PAMGuard displays. 
 * <p>
 * The detection plot projector holds four axis which can be accessed through the projector. Only 
 * the bottom and left axis are used by the super class  GeneralProjector functions for co-ordinate 
 * conversion although function exist for the right and upper left axis too. 
 * @author Jamie Macaulay
 *
 */
public class DetectionPlotProjector extends GeneralProjector<Coordinate3d> {


	/**
	 * The minimum limit of the scroll bar. In millis
	 */
	public double minScrollLimit = 0; 
	

	/**
	 * The maximum limit of the scroll bar. i.e. the maximum value that can be scrolled to (e.g. the length of a waveform) In millis
	 */
	public double maxScrollLimit = 1; 
	
	
	/**
	 * True to enable the scroll bar. 
	 */
	public boolean enableScrollBar = true; 

	/**
	 * Projector for the ddPlotPane. 	
	 */
	private DDPlotPane dDPlotPane;

	/**
	 * The scroll axis. 
	 */
	private Side scrollAxis = Side.BOTTOM; 

	/**
	 * Constructor for the projector.
	 */
	public DetectionPlotProjector(DDPlotPane dDPlotPane){
		this.dDPlotPane=dDPlotPane; 
	}

	/**
	 * Get all the axis of the plot pane. 
	 * @return a list of axis in the order: TOP, RIGHT, BOTTOM, LEFT. 
	 */
	public PamAxisFX[] getAxis() {
		return dDPlotPane.getAllAxis();
	}

	/**
	 * Get a plot pane axis. 
	 * @param the axis side. TOP, BOTTOM, LEFT or RIGHT. 
	 * @return the associated PamAxisFX. 
	 */
	public PamAxisFX getAxis(Side side) {
		return dDPlotPane.getAxis(side);
	}

	/**
	 * Set the minimum and maximum value fo an axis
	 * @param min - the minimum value in axis units.
	 * @param max - the maximum value in axis units. 
	 * @param axis - the axis 
	 */
	public void setAxisMinMax(double minVal, double maxVal, Side axis) {
		//System.out.println("Projector: Change axis limits: " + minVal + "  "+ maxVal +"  "+ axis);
		PamAxisFX pamAxis = dDPlotPane.getAxis(axis); 
		pamAxis.setMinVal(minVal);
		pamAxis.setMaxVal(maxVal);
	}

	/**
	 * Set the minimum and maximum values of the axis and labels. 
	 * @param minVal - the minimum value in axis units. 
	 * @param maxVal - the maximum value in axis units. 
	 */
	public void setAxisMinMax(double minVal, double maxVal, Side axis, String labels) {
		setAxisMinMax( minVal,  maxVal,  axis); 
		dDPlotPane.getAxis(axis).setLabel(labels);
	}

	/**
	 * Set the axis interval
	 * @param minVal - the minimum value in axis units. 
	 */
	public void setAxisInterval(double interval, Side axis) {
		dDPlotPane.getAxis(axis).setInterval(interval);
	}


	/**
	 * Set the axis label. 
	 * @param string - the label
	 * @param axis - the axis
	 */
	public void setAxisLabel(String string, Side axis) {
		dDPlotPane.getAxis(axis).setLabel(string);
	}

	/**
	 * Sets the number of plots on the y axis. For example waveforms have multiple plots 
	 * whilst a spectrum is usually plotted on the same plot. 
	 * @param numberplots - the number of plots to set. 
	 */
	public void setNumberPlots(int numberplots) {
		dDPlotPane.getAxisPane(Side.LEFT).setnPlots(numberplots);
	}

	/////Get pixel position///////

	/**
	 * Get the pixel co-ordinates for two data points. 
	 * @param d1 - the data point pn the x axis. 
	 * @param d2 - the data point on the y axis
	 * @param topbottom - which axis to use for the x axis, the top or the bottom. 
	 * @param rightLeft - which axis to use for the y axis, the left or right. 
	 * @return a Coordinate3d of the pixel locat5ion of d1 and d2. 
	 */
	public Coordinate3d getCoord3d(double d1, double d2, Side topbottom, Side rightLeft) {
		return getCoord3d(d1, d2, 0, rightLeft, topbottom);
	}
	
	/**
	 * Get the pixel co-ordinates for two data points. 
	 * @param d1 - the data point on the x axis. 
	 * @param d2 - the data point on the y axis
	 * @param d3 - the data point on the z axis. (currently not implemented)
	 * @param topbottom - which axis to use for the x axis, the top or the bottom. 
	 * @param rightLeft - which axis to use for the y axis, the left or right. 
	 * @return a Coordinate3d of the pixel locat5ion of d1 and d2. 
	 */
	public Coordinate3d getCoord3d(double d1, double d2, double d3, Side topbottom, Side rightLeft) {
		return new Coordinate3d(dDPlotPane.getAxis(topbottom).getPosition(d1),
				dDPlotPane.getAxis(rightLeft).getPosition(d2), 0) ;
	}

	@Override
	public Coordinate3d getCoord3d(double d1, double d2, double d3) {
		//System.out.println("Projector: Hello! " + d1 + " " + d2 );
		return getCoord3d(d1, d2, d3, Side.BOTTOM, Side.LEFT);
	}

	@Override
	public Coordinate3d getCoord3d(Coordinate3d dataObject) {
		return getCoord3d(dataObject.x, dataObject.y, dataObject.z,Side.BOTTOM, Side.LEFT);
	}

	/////Get data position///////
	
	@Override
	public Coordinate3d getDataPosition(PamCoordinate screenPosition) {
		return getDataPosition( screenPosition, Side.BOTTOM, Side.LEFT) ;
	}
	
	/**
	 * Do the exact opposite of getCoord3d and turn a screen position back into a data coordinate 
	 * (e.g. a time / freq, a lat long, etc)/.
	 * @param screenPosition - the screen position in pixels
	 *@param topbottom - which axis to use for the x axis, the top or the bottom. 
	 * @param rightLeft - which axis to use for the y axis, the left or right. 
	 * @return a Coordinate3d with data position
	 */
	public Coordinate3d getDataPosition(PamCoordinate screenPosition, Side topbottom,  Side rightLeft) {
		return new Coordinate3d(dDPlotPane.getAxis(topbottom).getDataValue(screenPosition.getCoordinate(0)),
				dDPlotPane.getAxis(rightLeft).getDataValue(screenPosition.getCoordinate(1)), 0);
	}
	
	/**
	 * Get the current minimum and maximum axis values of the scroll axis. Note that these
	 * are not the scroll limits but the current values defined by the scroll bar. 
	 * @return the minimum and maximum values of the scroll axis. 
	 */
	public double[] getScrollAxisMinMax() {
		return new double[] {dDPlotPane.getAxis(scrollAxis).getMinVal(), dDPlotPane.getAxis(scrollAxis).getMaxVal()};
	}
	/**
	 * Get the minimum limit of the scrollbar, 
	 * @return
	 */
	public double getMinScrollLimit() {
		return minScrollLimit;
	}

	public void setMinScrollLimit(double minScrollLimit) {
		this.minScrollLimit = minScrollLimit;
	}

	public double getMaxScrollLimit() {
		return maxScrollLimit;
	}

	public void setMaxScrollLimit(double maxScrollLimit) {
		this.maxScrollLimit = maxScrollLimit;
	}

	public boolean isEnableScrollBar() {
		return enableScrollBar;
	}

	public void setEnableScrollBar(boolean enableScrollBar) {
		this.enableScrollBar = enableScrollBar;
	}

	/**
	 * Set the scroll axis. This is the axis that the scroll bar changes on the plot. 
	 * If null then the scroll bar will not notify the axis. Note that the scroll bar
	 * has units of milliseconds and thus this axis must also have millisecond units. Otherwise
	 * strange things will happen!
	 * 
	 * @param top - the scroll axis. 
	 */
	public void setScrollAxis(Side top) {
		this.scrollAxis = top;
		
	}
	
	/**
	 * Get the scroll axis. This is the axis that the scroll bar changes on the plot. 
	 * If null then the scroll bar will not notify the axis. 
	 * @return - the scroll axis. 
	 */
	public Side getScrollAxis() {
		return this.scrollAxis;
	}


}
