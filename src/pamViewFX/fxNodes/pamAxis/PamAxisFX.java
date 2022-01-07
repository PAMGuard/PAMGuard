package pamViewFX.fxNodes.pamAxis;

import java.text.DecimalFormat;

/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */


//import java.awt.Graphics;
//import java.awt.Graphics2D;

import java.util.ArrayList;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import Layout.PamAxisPanel;
import com.sun.javafx.geom.Dimension2D;
//import com.sun.javafx.tk.FontMetrics; replaced with internal FontMetrics class

/**

 * Draws an axis on a JavaFX Node. 
 * <p> 
 * PamAxis contains all the information needed to draw an axis, the 
 * actual drawing is generally called from the paintComponent function 
 * in the containing window. 
 * 
 * <p>
 * For a good example (in swing but similar to JavaFX), see PamAxisPanel which will automatically draw
 * four axis around a central plot. 
 *  
 * @author Doug Gillespie
 * @see PamView.PamLabel
 * @see PamAxisPanel
 *
 */
public class PamAxisFX {

	/*
	 * Try out some static functions for drawing axis.
	 */
	static final int TICKS_ENDSONLY = 0;

	static final int TICKS_AUTO = 1;

	static private final double[] defaultIntervals = { 1, 2, 2.5, 5, 10 }; // and multiples of
															// 10 thereof !
	private double[] intervals = defaultIntervals;
	
	static private final double[] defaultScaleEnds = { 1, 2, 4, 5, 8, 10 };
	
	private double[] scaleEnds = defaultScaleEnds;
	
	private boolean allowScaleMultiples = true;

	static final int minPixels = 100;

	static public final int INTERVAL_AUTO = Integer.MIN_VALUE;

	static public final int ABOVE_LEFT = 0;

	static public final int BELOW_RIGHT = 1;

	static public final int BOTH_SIDES = 2;

	static public final int LABEL_NEAR_MAX = 0;

	static public final int LABEL_NEAR_MIN = 1;

	static public final int LABEL_NEAR_CENTRE = 2;

	/**
	 * The start x pixel.
	 */
	private DoubleProperty x1 =  new AxisDoubleProperty(0);  
	
	/**
	 * The end x pixel 
	 */
	private DoubleProperty x2 =  new AxisDoubleProperty(1);  
	
	/**
	 * The start y pixel
	 */
	private DoubleProperty y1 =  new AxisDoubleProperty(0);  
	
	/**
	 * The end y pixel
	 */
	private DoubleProperty y2 =  new AxisDoubleProperty(2);  
		
	/**
	 * The stroke color, i.e. colour of text and labels. 
	 */
	private Color axisStrokeColor=Color.BLACK; 

	
	/**
	 * Minimum axis value property. 
	 */
	private DoubleProperty minValProperty=new AxisDoubleProperty(0); 
	
	/**
	 * Maximum value property. 
	 */
	private DoubleProperty maxValProperty=new AxisDoubleProperty(1);
	
	
	/**
	 * Maximum value property. 
	 */
	private BooleanProperty reverseAxis=new SimpleBooleanProperty(false);
	
	
	/**
	 * Format for labels. 2 decimal places and gets rid of trailing zeros. Note: # means optional zeros (gets rid of 
	 * trailing zeros).
	 */
	DecimalFormat df = new DecimalFormat("#.####"); 
	
	
	private boolean forceFirstVal = false;
	
	private double forcedFirstVal = 0;

	private int tickPosition = LABEL_NEAR_CENTRE;

//	private int tickMarks, scaleType;

	private double interval;
	
	private boolean logTenthsScale = false;

	private boolean drawLine = false;

	private boolean intervalStart;

	private int tickLength;

	/**
	 * Extra label information to format into the 
	 * start of the axis at same height as label
	 */
	private String extraAxStartLabel;
	/**
	 * Extra label information to format into the 
	 * end of the axis at same height as label
	 */
	private String extraAxisEndLabel;
	
	private String format;
	
	
	private boolean integerFormat = false;

	private boolean logScale;
	
	/**
	 * The axis label
	 */
	private StringProperty label = new SimpleStringProperty("");

	private int labelPos = LABEL_NEAR_CENTRE;
	
	/**
	 * Always set the scale interval to be 1/2, 1/4, 1/8, etc of the maximum
	 */
	private boolean fractionalScale = false;

	private boolean crampLabels = false;
//	private String longestString;

	/**
	 * 
	 * @param x1 first x coordinate of axis
	 * @param y1 first y coordinate of axis
	 * @param x2 second x coordinate of axis
	 * @param y2 second  coordinate of axis
	 * @param minVal minimum axis value
	 * @param maxVal maximum axis value
	 * @param tickPosition tick position (ABOVE_LEFT or BELOW_RIGHT)
	 * @param label text for label (or null if no label)
	 * @param labelPos Position of axis label (LABEL_NEAR_MAX, LABEL_NEAR_MIN
	 * or LABEL_NEAR_CENTRE)
	 * @param format format for numbers printed on the display. This must be a standard 
	 * format String such as "%d", "%f", "%3.1f", "%.2 seconds", etc. 
	 */
	public PamAxisFX(double x1, double y1, double x2, double y2, double minVal,
			double maxVal, int tickPosition, String label, int labelPos,
			String format) {
		this.x1 .setValue(x1);
		this.x2 .setValue(x2);
		this.y1 .setValue(y1);
		this.y2 .setValue(y2);
		setRange(minVal, maxVal);
		this.tickPosition = tickPosition;
		this.label.setValue(label);
		this.labelPos = labelPos;
		setFormat(format);
		// this.tickMarks = tickMarks;
		// this.scaleType = scaleType;
		interval = INTERVAL_AUTO;
		drawLine = false;
		tickLength = 3;
		logScale = false;
	}

	/**
	 * 
	 * @param x1 x1
	 * @param y1 y1 
	 * @param x2 x2 
	 * @param y2 y2
	 * @param minVal min axis value
	 * @param maxVal max axis value
	 * @param aboveLeft above and / or to the left
	 * @param label axis label
	 * @param format format of numbers
	 */
	public PamAxisFX(double x1, double y1, double x2, double y2, double minVal,
			double maxVal, boolean aboveLeft, String label, String format) {
		this.x1 .setValue(x1);
		this.x2 .setValue(x2);
		this.y1 .setValue(y1);
		this.y2 .setValue(y2);
		setRange(minVal, maxVal);
		if (aboveLeft) {
			this.tickPosition = ABOVE_LEFT;
		} else {
			this.tickPosition = BELOW_RIGHT;
		}
		this.label.setValue(label);
		setFormat(format);
		// this.tickMarks = tickMarks;
		// this.scaleType = scaleType;
		interval = INTERVAL_AUTO;
		drawLine = false;
		tickLength = 3; //defualt tick lsize
		logScale = false;
	}

	/**
	 * Set the axis default coordinates
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	public void setPosition(double x1, double y1, double x2, double y2) {
		this.x1 .setValue(x1);
		this.x2 .setValue(x2);
		this.y1 .setValue(y1);
		this.y2 .setValue(y2);
		axisAngle = Math.atan2(-(y2 - y1), x2 - x1);
	}

	/**
	 * Set the interval between ticks. This is by
	 * default set to INTERVAL_AUTO whereby an interval is
	 * chosen so that there is a tick mark approximately every 
	 * 100 pixels. 
	 * @param interval the interval or INTERVAL_AUTO
	 */
	public void setInterval(double interval) {
		this.interval = interval;
	}

	/**
	 * Set the minimum and maximum values for the axis. 
	 * @param minVal - the minimum value of the axis
	 * @param maxVal - the maximu  value of the axis. 
	 */
	public void setRange(double minVal, double maxVal) {
		this.minValProperty.setValue(minVal);
		this.maxValProperty.setValue(maxVal);
		setFractionalScale(fractionalScale);
	}

	/**
	 * Draw the axis in the graphics context g at the given position
	 * @param g
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	public void drawAxis(GraphicsContext g, double x1, double y1, double x2, double y2) {
		setPosition(x1, y1, x2, y2);
		drawAxis(g);
	}
	
	private void updateAxisInfo(){
		totalPixs = Math.sqrt(Math.pow(x2.get() - x1.get(), 2) + Math.pow(y2.get() - y1.get(), 2));
		axisScale = totalPixs / (maxValProperty.getValue() - minValProperty.getValue() );
	}

	FontMetrics fontMetrics;

	double axisValue, axisScale;

	int[] tickLengthX = new int[2], tickLengthY = new int[2];

	double labelOffsetX, labelOffsetY;

	double tickStepX, tickStepY;

	double axisAngle;

	double theInterval;

	int maxLabelWidth, maxLabelHeight;

	/**
	 * The size of the axis in pixels. 
	 */
	double totalPixs;
	
	double tickAngle;

	/**
	 * Draw the axis in the Graphics Context g
	 * @param g graphics context to draw on
	 */
	public void drawAxis(GraphicsContext g) {

		 g.setFill(axisStrokeColor);

//		fontMetrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(g.getFont());
		fontMetrics = new FontMetrics(g.getFont());

		theInterval = interval;
		totalPixs = Math.sqrt(Math.pow(x2.get() - x1.get(), 2) + Math.pow(y2.get() - y1.get(), 2));

		axisScale = totalPixs / (maxValProperty.getValue() - minValProperty.getValue() );

		if (interval == INTERVAL_AUTO) {
			theInterval = getInterval((maxValProperty.getValue() - minValProperty.getValue()), (int) totalPixs, 80);
		}
		if (drawLine)
			g.strokeLine(x1.get(), y1.get(), x2.get(), y2.get());
		if (forceFirstVal) {
			axisValue = forcedFirstVal;
		} else if (intervalStart) {
			axisValue = ((int) (minValProperty.getValue() / theInterval)) * theInterval;
		} else {
			axisValue = minValProperty.getValue();
		}
		while (axisValue < minValProperty.getValue())
			axisValue += theInterval;

		axisAngle = Math.atan2(-(y2.get() - y1.get()), x2.get() - x1.get());
		if (tickPosition == ABOVE_LEFT) {
			tickAngle = axisAngle + Math.PI / 2;
			tickLengthX[0] = tickLengthY[0] = 0;
			tickLengthX[1] = (int) (tickLength * Math.cos(tickAngle));
			tickLengthY[1] = (int) -(tickLength * Math.sin(tickAngle));
		} else if (tickPosition == BELOW_RIGHT) {
			tickAngle = axisAngle - Math.PI / 2;
			tickLengthX[0] = tickLengthY[0] = 0;
			tickLengthX[1] = (int) (tickLength * Math.cos(tickAngle));
			tickLengthY[1] = (int) -(tickLength * Math.sin(tickAngle));
		} else {
			tickAngle = axisAngle - Math.PI / 2;
			tickLengthX[0] = (int) (tickLength * Math.cos(tickAngle));
			tickLengthY[0] = (int) -(tickLength * Math.sin(tickAngle));
			tickAngle = axisAngle + Math.PI / 2;
			tickLengthX[1] = (int) (tickLength * Math.cos(tickAngle));
			tickLengthY[1] = (int) -(tickLength * Math.sin(tickAngle));
		}
		/*
		 * Now work out which quadrant it's in and set appropriate offsets for
		 * the labels Start by getting it in the range
		 */
		double cosT = Math.cos(tickAngle);
		double sinT = Math.sin(tickAngle);
		if (Math.abs(cosT) > Math.abs(sinT)) {
			/*
			 * it's a vertical(ish) axis !
			 */
			if (cosT > 0) {
				labelOffsetX = 0;
				labelOffsetY = fontMetrics.getAscent() / 2;
			} else {
				labelOffsetX = 1;
				labelOffsetY = fontMetrics.getAscent() / 2;
			}
		} else {
			/*
			 * It's a horixontal(ish) axis
			 */
			if (sinT > 0) {
				labelOffsetX = 0.5;
				labelOffsetY = -fontMetrics.getDescent();
			} else {
				labelOffsetX = 0.5;
				labelOffsetY = fontMetrics.getAscent();
			}
		}
		maxLabelHeight = (int) fontMetrics.getAscent();
		maxLabelWidth = 0;

		if (logScale) {
			drawLogAxis(g);
		} else {
			drawLinearAxis(g);
		}
		drawAxisTitle(g);
	}
	
	/** 
	 * Work out the coordinates of all tick marks. 
	 * @param extraOne
	 * @return list of axis Point2Ds.
	 */
	public ArrayList<Point2D> getAxisPoint2Ds(boolean extraOne) {

		if (logScale) {
			return getLogPoint2Ds(extraOne);
		} else {
			return getLinearPoint2Ds(extraOne);
		}
	}
	
	private ArrayList<Double> axisValues = new ArrayList<Double>();

	/**
	 * Label scale can be used to change the scaling of axis labels without
	 * changing the underlying axis data that get used for display calculations
	 * and projections between screen coordinates and data values. For instance this 
	 * may be used on a time axis which wants to keep with SI units of seconds, but 
	 * finds it 'nicer' to display the scale in millisecs, in which case labelScale
	 * should be set to 1000. 
	 */
	private double labelScale = 1.;

	
	/**
	 * work out the coordinates of all tick marks using a linear scale
	 * @param extraOne
	 * @return
	 */
	private ArrayList<Point2D> getLinearPoint2Ds(boolean extraOne) {
		// separate this from the drawing bit so that it can be called by
		// the inner panel for grid drawing.
		ArrayList<Point2D> axisPoint2Ds = new ArrayList<Point2D>();
		axisValues.clear();

		// if ((x2 - x1) * (maxVal - minVal) < 0) tickStepX *= -1;
		tickStepX = Math.abs(theInterval) * Math.cos(axisAngle) * axisScale;
		tickStepY = -Math.abs(theInterval) * Math.sin(axisAngle) * axisScale;
		if ((maxValProperty.getValue() - minValProperty.getValue()) < 0) {
			tickStepY *= -1;
			tickStepX *= -1;
		}
		int ticks = 0; // count these to make sure it doesn't get out of hand !
		/*
		 * Need to start at x1,y1 and go towards x2,y2 and stop when you pass
		 * x2,y2. Trouble is you don't know if x2 and y2 are greater or less
		 * than x1 and y1, so can't use a simple test.
		 */
		int xDirection = 1;
		int yDirection = 1;
		if (x2.get() < x1.get())
			xDirection = -1;
		else if (x2 == x1)
			xDirection = 0;
		if (y2.get() < y1.get())
			yDirection = -1;
		else if (y2 == y1)
			yDirection = 0;
		double x = x1.get();
		double y = y1.get();
		if (forceFirstVal) {
			axisValue = forcedFirstVal;
			x = x1.get() + axisScale * (axisValue-minValProperty.getValue()) * xDirection;
			y = y1.get() + axisScale * (axisValue-minValProperty.getValue()) * yDirection;
		}
		while (((x2.get() - x) * xDirection >= -1 && (y2.get() - y) * yDirection >= -1)
				|| ticks < 2) {
			axisPoint2Ds.add(new Point2D((int)x,(int)y));
			axisValues.add(axisValue);
//			DrawTickAndLabel(g2d, x, y, axisValue);
			x += tickStepX;
			y += tickStepY;
			axisValue += theInterval;
			if (ticks++ > 100)
				break;
		}
		if (extraOne) { // needed for minor tick marks on axis
			axisPoint2Ds.add(new Point2D((int)x,(int)y));
			axisValues.add(axisValue);
		}
		return axisPoint2Ds;
	}

	/**
	 * Draw a linear axis on the graphics context
	 * @param g2d graphics context to draw on
	 */
	protected void drawLinearAxis(GraphicsContext g2d) {
		ArrayList<Point2D> axisPoint2Ds = getLinearPoint2Ds(false);
		// axis values are set in the same function and held 
		for (int i = 0; i < axisPoint2Ds.size(); i++) {
			drawTickAndLabel(g2d, axisPoint2Ds.get(i), axisValues.get(i));
		}
	}

	/**
	 * Draw a logarithmic axis on the graphics context
	 * @param g2d graphics context to draw on
	 */
	protected void drawLogAxis(GraphicsContext g2d) {
		ArrayList<Point2D> axisPoint2Ds = getLogPoint2Ds(false);
		// axis values are set in the same function and held 
	    
		for (int i = 0; i < axisPoint2Ds.size(); i++) {
			drawTickAndLabel(g2d, axisPoint2Ds.get(i), axisValues.get(i));
		}
	}
	
	/**
	 * Get the coordinates of tick Point2Ds for a logarithmic axis
	 * @param extraOne
	 * @return Array of Point2Ds
	 */
	protected ArrayList<Point2D> getLogPoint2Ds(boolean extraOne) {
		/*
		 * Start value must be on a power of 10. Assume that MaxValue MUST be
		 * greater than Min Value.
		 */
		ArrayList<Point2D> axisPoint2Ds = new ArrayList<Point2D>();
		axisValues.clear();
		double vx;
		double x = 0, y = 0;
		// String str;
		axisValue = minValProperty.getValue();
		axisValue = Math.floor(Math.log10(axisValue));
		// // axisValue = Math.pow(10, axisValue);
		if (maxValProperty.getValue() / minValProperty.getValue() <= 0.0) {
			return axisPoint2Ds;
		}
		axisScale = totalPixs / Math.log10(maxValProperty.getValue() / minValProperty.getValue());
		tickStepX = Math.cos(axisAngle);
		tickStepY = Math.sin(axisAngle);
		if (minValProperty.getValue() <= 0) {
			return axisPoint2Ds;
//			minVal = maxVal / 1000;
//			if (minVal <= 0) return axisPoint2Ds;
//			axisValue = Math.floor(Math.log10(minVal));
		}
		double logMinVal = Math.log10(minValProperty.getValue());
		while (axisValue < logMinVal) {
			axisValue += 1;
		}
		while (true) {
			vx = (axisValue - logMinVal) * axisScale;
			if (vx < 0 || vx > totalPixs) {
				break;
			}
			x = (x1.get() + vx * tickStepX);
			y = (y1.get() - vx * tickStepY);
			axisPoint2Ds.add(new Point2D((int) x, (int) y));
			axisValues.add(Math.pow(10, axisValue));
//			DrawTickAndLabel(g2d, x, y, Math.pow(10, axisValue));
			axisValue += 1;
			if (axisPoint2Ds.size() > totalPixs) {
				break;
			}
		}
		if (extraOne) { // needed for minor tick marks on axis
			x = (x1.get() + vx * tickStepX);
			y = (y1.get() - vx * tickStepY);
			axisPoint2Ds.add(new Point2D((int)x,(int)y));
			axisValues.add(axisValue);
		}
		return axisPoint2Ds;
	}

	/**
	 * Draw a tick and a label at the given Point2D. 
	 * @param g2d
	 * @param xy
	 * @param value
	 */
	protected void drawTickAndLabel(GraphicsContext g2d, Point2D xy, double value) {
		drawTickAndLabel(g2d, xy.getX(), xy.getY(), value);
	}
	
	/**
	 * Draw a tick and a label at the given Point2D
	 * @param g2d
	 * @param x
	 * @param y
	 * @param value
	 */
	private void drawTickAndLabel(GraphicsContext g2d, double x, double y,
			double value) {
		double xt, yt;
		String str;
		int labelWidth, labelHeight;

		str = formatValue(value);
		labelWidth = (int) fontMetrics.computeStringWidth(str);
		labelHeight = (int) fontMetrics.getAscent();
		
		xt = (int) x + tickLengthX[1];
		yt = (int) y + tickLengthY[1];
		if (isCrampLabels()) {
			if (tickLengthY[1] == 0) {
				yt = Math.max(Math.min(y1.get(), y2.get())+labelHeight/2, yt);
				yt = Math.min(Math.max(y1.get(), y2.get())-labelHeight/2, yt);
			}
			if (tickLengthX[1] == 0) {
				xt = Math.max(Math.min(x1.get(), x2.get())+labelWidth/2, xt);
				xt = Math.min(Math.max(x1.get(), x2.get())-labelWidth/2, xt);
			}
		}
		
		g2d.strokeLine( x + tickLengthX[0],  y + tickLengthY[0], xt, yt);
		// print the label
		maxLabelWidth = Math.max(labelWidth, maxLabelWidth); // need this for drawing axistitles
		g2d.fillText(str,  (xt - labelOffsetX * labelWidth),
				(yt + labelOffsetY));
	}
	
	/**
	 * Format the text for the label. 
	 * @param val - value. 
	 * @return formatted string.
	 */
	protected String formatValue(double val) {
		val *= labelScale ;
		if (format == null) {
			if (val==0) return "0";
			return 	df.format(val); ///PamCalendar.formatDuration((long) (val*1000.));  //TODO
		}
		if (integerFormat) {
			return String.format(format, (int) val);
		}
		else {
			return String.format(format, val);
		}
	}
	
	/**
	 * Draw a grid to go with the axis. Generally, the graphics handle 
	 * for grid drawing will be some inner window sitting inside the 
	 * window containing the axis. 
	 * @param g Graphics context to draw on 
	 * @param plotSize size of the graphic
	 * @param minorGrid draw a minor grid as well as lines at the main tick marks.
	 */
	public void drawGrid(GraphicsContext g, Dimension2D plotSize, int minorGrid) {
		drawGrid(g, plotSize, null, minorGrid);
	}
	public void drawGrid(GraphicsContext g, Dimension2D plotSize, Insets insets, int minorGrid) {	
		// need to first work out which way the lines are going and how long they are:
		// basically they go in the same direction as the tickmarks - given in tickAngle

		g.setFill(Color.GRAY);
		
		int xExtent = (int) (Math.cos(tickAngle) * plotSize.width);
		int yExtent = (int) (Math.sin(tickAngle) * plotSize.height);
		ArrayList<Point2D> axisPoint2Ds = getAxisPoint2Ds(true);
		Point2D pt;
		int gx1, gx2, gy1, gy2;
		if (insets == null) {
			insets = new Insets(0, 0, 0, 0);
		}
		
		float[] dashes = {2, 2};
		Paint s = g.getStroke();
		if (minorGrid == 0) {
//			// if there is a monir grid, draw the main one solid.
//			// if no minor grid, thendo the main one dashed.
//			g.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, dashes, 0));
		}
		
		for (int i = 0; i < axisPoint2Ds.size(); i++) {
			pt = axisPoint2Ds.get(i);
			gx1 = (int) (pt.getX() - xExtent - x1.get() + insets.getLeft());
			gy1 = (int) (pt.getY() - yExtent - y2.get() + insets.getTop()); 
			gx2 = (int) (pt.getX() + xExtent - x1.get() + insets.getLeft());
			gy2 = (int) (pt.getY() + yExtent - y2.get() + insets.getTop());

			if (gx1 <= insets.getLeft() && gx2 <= insets.getLeft()) continue;
			if (gy1 <= insets.getTop() && gy2 <= insets.getTop()) continue;
			g.strokeLine(gx1, gy1, gx2, gy2);
		}
		
		if (minorGrid > 0) {
			float[] minordashes = {2, 2};
			//TODO stroke in javafx
//			g.setStroke(new Paint(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, minordashes, 0));
			if (logScale) {
				drawLogMinorGrid(g, axisPoint2Ds, xExtent, yExtent, minorGrid);
			} else {
				drawLinearMinorGrid(g, axisPoint2Ds, xExtent, yExtent, minorGrid);
			}
		}
		g.setStroke(s);
	}
	
	private void drawLinearMinorGrid(GraphicsContext g, ArrayList<Point2D> axisPoint2Ds, int xExtent, int yExtent, int minorGrid) {
		
		if (axisPoint2Ds.size() < 2) return;
		
		Point2D p1, p2;
		double x, y;
		double stepX, stepY;
		int gx1, gx2, gy1, gy2;
		for (int i = 1; i < axisPoint2Ds.size(); i++) {
			p1 = axisPoint2Ds.get(i-1);
			p2 = axisPoint2Ds.get(i);
			stepX = (double) (p2.getX()-p1.getX()) / (minorGrid + 1);
			stepY = (double) (p2.getY()-p1.getY()) / (minorGrid + 1);
			x = p1.getX();
			y = p1.getY();
			for (int v = 0; v < minorGrid; v++) {
				x += stepX; 
				y += stepY; 
				gx1 = (int) (x - xExtent - x1.get());
				gy1 = (int) (y - yExtent - y2.get());
				gx2 = (int) (x + xExtent - x1.get());
				gy2 = (int) (y + yExtent - y2.get());
				if (gx1 <= 0 && gx2 <= 0) continue;
				if (gy1 <= 0 && gy2 <= 0) continue;
				g.strokeLine(gx1, gy1, gx2, gy2);
			}
		}
	}
	private void drawLogMinorGrid(GraphicsContext g, ArrayList<Point2D> axisPoint2Ds, int xExtent, int yExtent, int minorGrid) {
		// don't worry about value of minorGris - do all intermediate Point2Ds.
		if (axisPoint2Ds.size() < 2) return;
		Point2D p1, p2;
		int x, y;
		double sepX, sepY;
		double gx1, gx2, gy1, gy2;
		for (int i = 1; i < axisPoint2Ds.size(); i++) {
			p1 = axisPoint2Ds.get(i-1);
			p2 = axisPoint2Ds.get(i);
			sepX = p2.getX()-p1.getX();
			sepY = p2.getY()-p1.getY();
			for (int v = 2; v < 10; v++) {
				x = (int) (p1.getX() + sepX * Math.log10(v)); 
				y = (int) (p1.getY() + sepY * Math.log10(v)); 
				gx1 = x - xExtent - x1.get();
				gy1 = y - yExtent - y2.get();
				gx2 = x + xExtent - x1.get();
				gy2 = y + yExtent - y2.get();
				if (gx1 <= 0 && gx2 <= 0) continue;
				if (gy1 <= 0 && gy2 <= 0) continue;
				g.strokeLine(gx1, gy1, gx2, gy2);
			}
		}
	}

	private void drawAxisTitle(GraphicsContext g2d) {
		if (label != null) {
			drawAxisTextItem(g2d, label.get(), labelPos);
		}
		if (extraAxStartLabel != null) {
			drawAxisTextItem(g2d, extraAxStartLabel, LABEL_NEAR_MIN);
		}
		if (extraAxisEndLabel != null) {
			drawAxisTextItem(g2d, extraAxisEndLabel, LABEL_NEAR_MAX);
		}
	}
	
	private void drawAxisTextItem(GraphicsContext g2d, String text, int position) {
		double x = x1.get(), y = y1.get();
		// calculate for a simple x axis
		switch (position) {
		case LABEL_NEAR_MAX:
			x =  (totalPixs
					- fontMetrics.computeStringWidth(text));
			break;
		case LABEL_NEAR_MIN:
			x = 0;
			break;
		case LABEL_NEAR_CENTRE:
			x = (int) (totalPixs - fontMetrics.computeStringWidth(text
					)) / 2;
			break;
		}
		// switch ()
		if (tickPosition == BELOW_RIGHT) {
			//needs to be 2 x because starts at bottom of font...FIXME- why 1.5?
			y = (int) (tickLength + 1.5*maxLabelHeight + 1);

		} 
		else {
			y = (int) -(tickLength + maxLabelHeight + 1);// + fontMetrics.getLineHeight());
		}
		if (x1==x2) {
			if (this.tickPosition == ABOVE_LEFT) {
				y -= fontMetrics.getDescent();
			}
			else {
				y += fontMetrics.getAscent();
			}
		}
		g2d.translate(x1.get(), y1.get());
		g2d.rotate(Math.toDegrees(-axisAngle));
		g2d.fillText(text, x, y);
		// y = -y;
		// g2d.drawString(label, x, y);
		g2d.rotate(Math.toDegrees(axisAngle));
		g2d.translate(-x1.get(), -y1.get());
	}


	/**
	 * 
	 * Gets the dimension of the axis perpendicular to the direction of the 
	 * tickmark - i.e. for a vertical axis, it's half the text height, for 
	 * a horizontal axis it's half the typical string width. 
	 * @param g graphics handle for component the axis is to be draw on 
	 * @return axis extent in pixels
	 */
	public int getExtent2(GraphicsContext g) {
		return getExtent2(g, formatValue(Math.max(this.minValProperty.getValue(),
				this.maxValProperty.getValue())));
	}
	
	/**
	 * 
	 * Gets the dimension of the axis perpendicular to the direction of the 
	 * tickmark - i.e. for a vertical axis, it's half the text height, for 
	 * a horizontal axis it's half the typical string width. 
	 * @param g graphics handle for component the axis is to be draw on
	 * @param typicalString typical text string. 
	 * @return Axis extent
	 */
	public int getExtent2(GraphicsContext g, String typicalString) {
//		FontMetrics font=Toolkit.getToolkit().getFontLoader().getFontMetrics(g.getFont());
		FontMetrics font=new FontMetrics(g.getFont());
		if (x1.get() == x2.get()) { // vertical axis
			// return half the height of typical text.
			return (int) (font.getLineHeight() / 2);
		}
		if (y1.get() == y2.get()) {
			// return half the width of a typical string
			return (int) (font.computeStringWidth(typicalString)/2);
		}
		return 0;
	}
	/**
	 * 
	 * Gets the dimension of the axis parallel to the direction of the 
	 * tickmark - i.e. the ticklength + the text height + the label height. 
	 * @param g graphics handle for component the axis is to be draw on 
	 * @return axis extent in pixels
	 */
	public int getExtent(GraphicsContext g) {
		return getExtent(g, formatValue(Math.max(this.minValProperty.getValue(),
				this.maxValProperty.getValue())));
	}

	/**
	 * Gets the dimension of the axis parallel to the direction of the 
	 * tickmark - i.e. the ticklength + the text height + the label height. 
	 * @param g graphics handle the axis is being draw on
	 * @param typicalString typical label string for a tick
	 * @return axisextent in pixels
	 */
	public int getExtent(GraphicsContext g, String typicalString) {
		int e = 0;
//		FontMetrics font=Toolkit.getToolkit().getFontLoader().getFontMetrics(g.getFont());
		FontMetrics font=new FontMetrics(g.getFont());
		if (x1.get() == x2.get()) {
			// vertical (y) axis
			e = (int) (tickLength
					+font.computeStringWidth(typicalString)
					+ font.computeStringWidth("1"));
			
			if (label != null && label.get().length() > 0)
				e += font.getLineHeight() * 3 / 2; // will draw it
																// on its side !
		} else if (y1.get() == y2.get()) {
			e = (int) (tickLength + font.getLineHeight() * 3 / 2);
			if (label != null && label.get().length() > 0)
				e += font.getLineHeight() * 2 / 2;
		}
		return e;
	}

	private double getInterval(double range, int totalPixs, int minPixs) {
		/*
		 * find the minimum interval that will give at least minPixs spacing
		 */
		int gap;
		double sign = 1;
		double interval = range * minPixs / totalPixs;
		if (interval < 0) {
			interval = -interval;
			sign = -1;
		}
		// get in the right ball park by rounding down to the nearset log10.
		interval = Math.log10(interval);
		int iint = (int) interval;
		double baseinterval = Math.pow(10., iint);
		if (allowScaleMultiples == false || fractionalScale) {
			baseinterval = 1;
			interval = Math.abs(intervals[intervals.length-1]);
		}
		
		for (int i = 0; i < intervals.length; i++) {
			gap = (int) (baseinterval * intervals[i] / Math.abs(range
					/ totalPixs));
			if (gap >= minPixs)
				return baseinterval * intervals[i] * sign;
		}

		return interval * sign;
	}

	public boolean isLogScale() {
		return logScale;
	}

	public void setLogScale(boolean logScale) {
		this.logScale = logScale;
	}

	public boolean isDrawLine() {
		return drawLine;
	}

	public void setDrawLine(boolean drawLine) {
		this.drawLine = drawLine;
	}

	public String getFormat() {
		return format;
	}

	/**
	 * Set the format string for writing out numbers on the axis.
	 * @param format format string
	 */
	public void setFormat(String format) {
		this.format = format;
		if (format == null) {
			integerFormat = false;
			return;
		}
		// and see if it's got a d in it !
		integerFormat = (format.indexOf('d') >= 0);
	}
	
	/**
	 * Automatically set the format of axis labels based on the types of
	 * numbers in there. 
	 */
	public void setAutoFormat(boolean isInteger) {
		setFormat(getAutoFormat(isInteger));
	}
	
	public String getAutoFormat(boolean isInteger) {
		return getAutoFormat(minValProperty.getValue(), maxValProperty.getValue(), isInteger);
	}
	
	public static String getAutoFormat(double minVal, double maxVal, boolean isInteger) {
		int ndp = Math.max(getNumDecPlaces(minVal), getNumDecPlaces(maxVal));
		String fmt = String.format("%%%d.%df", ndp+2, ndp);
		return fmt;
	}
	
	public static int getNumDecPlaces(double value) {
		if (value == 0) {
			return 0;
		}
		double scale = 1;
		value = Math.abs(value);
		for (int i = 0; i < 30; i++) {
			if (value * scale >= 10) {
				return i;
			}
			scale *= 10;
		}
		return 2; // default value. 
	}

	public boolean isIntegerFormat() {
		return integerFormat;
	}

	public String getLabel() {
		return label.get();
	}

	/**
	 * Set the axis label text
	 * @param labelTxt
	 */
	public void setLabel(String labelTxt) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				label.set(labelTxt);
			}
		});
	}

	public int getLabelPos() {
		return labelPos;
	}

	public void setLabelPos(int labelPos) {
		this.labelPos = labelPos;
	}

	public int getTickPosition() {
		return tickPosition;
	}

	public void setTickPosition(int tickPosition) {
		this.tickPosition = tickPosition;
	}

	public double getMaxVal() {
		return maxValProperty.getValue();
	}

	public void setMaxVal(double maxVal) {
		this.maxValProperty.setValue(maxVal);
	}

	public double getMinVal() {
		return minValProperty.getValue();
	}

	public void setMinVal(double minVal) {
		this.minValProperty.setValue(minVal);
	}
	
	public DoubleProperty minValProperty() {
		return minValProperty;
	}

	public DoubleProperty maxValProperty() {
		return maxValProperty;
	}
	
	/**
	 * Work out the optimal format for the axis labels based on 
	 * analysis of previously used axis labels.  
	 * @return some kind of sensible format string !
	 */
	public String getAutoLabelFormat() {
		if (axisValues == null) {
			axisValues = new ArrayList<Double>();
		}
		if (axisValues.size() == 0) {
			axisValues.add(getMinVal());
			axisValues.add(getMaxVal());
		}
		int maxdig = 1;
		int maxdp = 0; 
		/*
		 * Method is to print each label using the Double.toString function
		 * which always makes sensible decisions. If the number is 
		 * integer, then the label will end .0 which will count as needing 
		 * zero dp's. Count up the max required digits and dps it dps == 0
		 * return a int format, if its > 0 return a floating format. 
		 */
		for (int i = 0; i < axisValues.size(); i++) {
			String defStr = axisValues.get(i).toString();
			int dpPos = defStr.indexOf('.');
			if (dpPos < 0) {
				continue;
			}
			int ndig = dpPos;
			int ndp = defStr.length()-dpPos-1;
			if (ndp == 1 && defStr.charAt(dpPos+1) == '0') {
				ndp = 0;
			}
			maxdig = Math.max(maxdig, ndig);
			maxdp = Math.max(maxdp, ndp);
		}
		if (maxdp == 0) {
			return String.format("%%%dd", maxdig);
		}
		else {
			return String.format("%%%d.%df", maxdig+maxdp+1, maxdp);
		}
	}
	
	public static double getDefaultScaleEnd(double val, double otherVal) {
		if (val == 0) {
			return 0;
		}
		int sign = 1;
		if (val < 0) {
			val = -val;
			sign = -1;
		}
		int otherSign = 1;
		if (otherVal < 0) {
			otherSign = -1;
			otherVal = -otherVal;
			
		}
		/*
		 * If value is relatively close to zero, then return zero. 
		 */
		if (val / otherVal < 0.5) {
			return 0;
		}
		double baseEnd = Math.pow(10., Math.floor(Math.log10(val)));
		double scaleEnd = baseEnd;
		for (int i = 0; i < defaultScaleEnds.length; i++) {
			scaleEnd = baseEnd * defaultScaleEnds[i];
			if (scaleEnd >= val) {
				if (val < otherVal) {
					// this is the lower of the two axis Point2Ds, so really 
					// want the one before
					if (i > 0) {
						scaleEnd = baseEnd*defaultScaleEnds[i-1];
					}
					else {
						scaleEnd /= 2;
					}
				}
				break;
			}
		}
		if (val > otherVal) {
			scaleEnd = Math.max(scaleEnd, Math.abs(otherVal)*1.1);
		}
		else {
			scaleEnd = Math.min(scaleEnd, Math.abs(otherVal)*0.9);
		}
		return scaleEnd * sign;
	}

	public void setForceFirstVal(boolean forceFirstVal, double forcedFirstVal) {
		this.forceFirstVal = forceFirstVal;
		this.forcedFirstVal = forcedFirstVal;
	}

	public boolean isLogTenthsScale() {
		return logTenthsScale;
	}

	public void setLogTenthsScale(boolean logTenthsScale) {
		this.logTenthsScale = logTenthsScale;
	}

	public ArrayList<Double> getAxisValues() {
		return axisValues;
	}

	public double getX1() {
		return x1.get();
	}

	public void setX1(double x1) {
		this.x1.setValue(x1); 
	}

	public double getX2() {
		return x2.get();
	}

	public void setX2(double x2) {
		this.x2.setValue(x2); 
	}

	public double getY1() {
		return y1.get();
	}

	public void setY1(double y1) {
		this.y1.setValue(y1); 
	}

	public double getY2() {
		return y2.get();
	}
	
	public void setY2(double y2) {
		this.y2.setValue(y2); 
	}
	
	public DoubleProperty x1Property(){
		return x1; 
	}
	
	
	public DoubleProperty x2Property(){
		return x2; 
	}
	
	
	public DoubleProperty y1Property(){
		return y1; 
	}
	
	
	public DoubleProperty y2Property(){
		return y2; 
	}

	
	/**
	 * The axis knows all about scale and can tell us the 
	 * pixel value for any given data value. 
	 * @param dataValue the value of the data.
	 * @return position in pixels along the axis for a given data value.
	 */
	public double getPosition(double dataValue) {
//		totalPixs = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
		double pixs;
		if (logScale == false) {
			pixs =  totalPixs * (dataValue - getMinVal()) / (getMaxVal() - getMinVal());
		}
		else {
			pixs =  totalPixs * (Math.log(dataValue) - Math.log(getMinVal())) / 
				(Math.log(getMaxVal()) - Math.log(getMinVal()));
		}
		if (x1.get() == x2.get() && !this.reverseAxis.get()) {
			pixs = totalPixs - pixs; // y axis, so go from other side. 
		}
		return pixs;
	}
	
	/**
	 * Converts a position on the plot into a data value 
	 * based on the axis max, min and scale. 
	 * <p>
	 * This is the exact compliment of getPosition()
	 * @param position position along the axis in pixels.
	 * @return data value. 
	 */
	public double getDataValue(double position) {
		double dataValue;
		if (totalPixs == 0) {
			return getMinVal();
		}
		if (x1.get() == x2.get() && !this.reverseAxis.get()) {
			position = totalPixs - position; // y axis, so go from other side. 
		}
		if (logScale == false) {
			dataValue = position * (getMaxVal() - getMinVal()) / totalPixs + getMinVal();
		}
		else {
			double logdataValue = position * (Math.log(getMaxVal()/getMinVal())) / totalPixs + Math.log(getMinVal());
			dataValue = Math.exp(logdataValue);
		}
		return dataValue;
	}

//	public static double[] getIntervals() {
//		return intervals;
//	}
//
//	public static void setIntervals(double[] intervals) {
//		PamAxis.intervals = intervals;
//	}
//
//	public static double[] getScaleEnds() {
//		return scaleEnds;
//	}
//
//	public static void setScaleEnds(double[] scaleEnds) {
//		PamAxis.scaleEnds = scaleEnds;
//	}
	
	public boolean isAllowScaleMultiples() {
		return allowScaleMultiples;
	}

	public void setAllowScaleMultiples(boolean allowScaleMultiples) {
		this.allowScaleMultiples = allowScaleMultiples;
	}

	/**
	 * Easy way to make scales end at 45, 90, 180, etc and 
	 * step in sensible 45, 90, etc. steps. 
	 * @param angleScales
	 */
	public void setAngleScales(boolean angleScales) {
		if (angleScales) {
			intervals = new double[]{ 10, 20, 30, 45, 90, 180, 360}; // and multiples of
			// 10 thereof !
			scaleEnds = new double[]{  10, 20, 30, 45, 90, 180, 360 };
			setAllowScaleMultiples(false);
		}
		else {
			intervals = new double[]{ 1, 2, 2.5, 5, 10 }; // and multiples of
			// 10 thereof !
			scaleEnds = new double[]{ 1, 2, 4,  5, 8, 10 };
			setAllowScaleMultiples(true);
		}
	}

	public boolean isFractionalScale() {
		return fractionalScale;
	}

	public void setFractionalScale(boolean fractionalScale) {
		this.fractionalScale = fractionalScale;
		if (fractionalScale) {
			scaleEnds = new double[]{maxValProperty.getValue()};
			int nFac = 8;
			int fac = (int) Math.pow(2, nFac-1);
			intervals = new double[8];
			for (int i = 0; i < nFac; i++) { 
				intervals[i] = Math.abs(maxValProperty.getValue()-minValProperty.getValue()) / fac;
				fac /= 2;
			}
		}
		else {
			scaleEnds = defaultScaleEnds;
			intervals = defaultIntervals;
		}
	}

	/**
	 * @return the extraAxStartLabel
	 */
	public String getExtraAxisStartLabel() {
		return extraAxStartLabel;
	}

	/**
	 * @param extraAxStartLabel the extraAxStartLabel to set
	 */
	public void setExtraAxisStartLabel(String extraAxStartLabel) {
		this.extraAxStartLabel = extraAxStartLabel;
	}

	/**
	 * @return the extraAxisEndLabel
	 */
	public String getExtraAxisEndLabel() {
		return extraAxisEndLabel;
	}

	/**
	 * @param extraAxisEndLabel the extraAxisEndLabel to set
	 */
	public void setExtraAxisEndLabel(String extraAxisEndLabel) {
		this.extraAxisEndLabel = extraAxisEndLabel;
	}

	/**
	 * Cramped labels means that when drawing an axis, the end 
	 * labels will be shifted to remain within the bounds of the axis
	 * itself. 
	 * @return the crampLables
	 */
	public boolean isCrampLabels() {
		return crampLabels;
	}

	/**
	 * Cramped labels means that when drawing an axis, the end 
	 * labels will be shifted to remain within the bounds of the axis
	 * itself. 
	 * @param crampLabels the crampLables to set
	 */
	public void setCrampLabels(boolean crampLabels) {
		this.crampLabels = crampLabels;
	}
	
	/**
	 * The size of the axis in pixels. 
	 * <br>
	 * e.g. if a horizontal axis this would be the width of the display. 
	 * @return the size of the axis in pixels.
	 */
	public double getTotalPixels() {
		return totalPixs;
	}

	/**
	 * Set the stroke colour. 
	 * @param strokeColor
	 */
	public void setStrokeColor(Color strokeColor) {
		this.axisStrokeColor=strokeColor; 
		
	}

	public Color getStrokeColor() {
		// TODO Auto-generated method stub
		return axisStrokeColor;
	}
	
	class AxisDoubleProperty extends SimpleDoubleProperty {
		
		public AxisDoubleProperty(double val){
			super(val);
			this.addListener((oldVal, newVal, obsVal)->{
				updateAxisInfo(); 
			});
		}
		
	}

	public ObservableValue<? extends String> labelProperty() {
		return label;
	}

	/**
	 * Label scale can be used to change the scaling of axis labels without
	 * changing the underlying axis data that get used for display calculations
	 * and projections between screen coordinates and data values. For instance this 
	 * may be used on a time axis which wants to keep with SI units of seconds, but 
	 * finds it 'nicer' to display the scale in millisecs, in which case labelScale
	 * should be set to 1000. 
	 * @return the labelScale
	 */
	public double getLabelScale() {
		return labelScale;
	}

	/**
	 * Label scale can be used to change the scaling of axis labels without
	 * changing the underlying axis data that get used for display calculations
	 * and projections between screen coordinates and data values. For instance this 
	 * may be used on a time axis which wants to keep with SI units of seconds, but 
	 * finds it 'nicer' to display the scale in millisecs, in which case labelScale
	 * should be set to 1000. 
	 * @param labelScale the labelScale to set
	 */
	public void setLabelScale(double labelScale) {
		this.labelScale = labelScale;
	}

	/**
	 * Get the reverse property for the axis. The reverse property indicates whether
	 * the axis should be display from minimum at top of screen to maximum at bottom of the
	 * screen instead of the default vice versa.
	 * 
	 * @return the reverse boolean property.
	 */
	public BooleanProperty reverseProperty() {
		return this.reverseAxis;
	}

	/**
	 * Set whether the axis is reversed. This indicates whether
	 * the axis should be display from minimum at top of screen to maximum at bottom of the
	 * screen instead of the default vice versa.
	 * 
	 * @return the reverse boolean property.
	 */
	public void setReversed(boolean reverseAxis2) {
		reverseAxis.setValue(reverseAxis2);
	}
	
	/**
	 * Internal class to mimic the non-public JavaFx FontMetrics class, no longer available from JavaFX 9 onwards.
	 * Code is taken from here: http://werner.yellowcouch.org/log/fontmetrics-jdk9/, with fields set to
	 * private instead of public and getters added
	 * 
	 * @author mo55
	 *
	 */
	public class FontMetrics
	{
		final private Text internal;
		private float ascent, descent, lineHeight;
		
		public FontMetrics(Font fnt)
		{
			internal =new Text();
			internal.setFont(fnt);
			Bounds b= internal.getLayoutBounds();
			lineHeight= (float) b.getHeight();
			ascent= (float) -b.getMinY();
			descent=(float) b.getMaxY();
		}

		public float computeStringWidth(String txt)
		{
			internal.setText(txt);
			return (float) internal.getLayoutBounds().getWidth();
		}
		
		public float getAscent() {
			return ascent;
		}

		public float getDescent() {
			return descent;
		}

		public float getLineHeight() {
			return lineHeight;
		}

	}
}