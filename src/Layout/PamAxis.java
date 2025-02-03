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
package Layout;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Stroke;
import java.util.ArrayList;

import PamView.PamColors;
import PamView.PamColors.PamColor;

/**
 * 
 * <p>
 * Draws an axis on a Java Component. 
 * <p> 
 * PamAxis contains all the information needed to draw an axis, the 
 * actual drawing is generally called from the paintComponent function 
 * in the containing window. 
 * 
 * <p>
 * for a good example, see PamAxisPanel which will automaticall draw
 * four axis around a central plot. 
 *  
 * @author Doug Gillespie
 * @see PamView.dialog.PamLabel
 * @see PamAxisPanel
 *
 */
public class PamAxis {

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

	private int x1, x2, y1, y2;

	private double minVal, maxVal;

	private boolean forceFirstVal = false;

	private double forcedFirstVal = 0;

	private int tickPosition = LABEL_NEAR_CENTRE;

	//	private int tickMarks, scaleType;

	private double interval;

	private boolean logTenthsScale = false;

	private boolean drawLine = false;

	private boolean intervalStart;

	private int tickLength;

	private String label;

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
	public PamAxis(int x1, int y1, int x2, int y2, double minVal,
			double maxVal, int tickPosition, String label, int labelPos,
			String format) {
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
		setRange(minVal, maxVal);
		this.tickPosition = tickPosition;
		this.label = label;
		this.labelPos = labelPos;
		setFormat(format);
		// this.tickMarks = tickMarks;
		// this.scaleType = scaleType;
		interval = INTERVAL_AUTO;
		drawLine = false;
		tickLength = 6;
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
	public PamAxis(int x1, int y1, int x2, int y2, double minVal,
			double maxVal, boolean aboveLeft, String label, String format) {
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
		setRange(minVal, maxVal);
		if (aboveLeft) {
			this.tickPosition = ABOVE_LEFT;
		} else {
			this.tickPosition = BELOW_RIGHT;
		}
		this.label = label;
		setFormat(format);
		// this.tickMarks = tickMarks;
		// this.scaleType = scaleType;
		interval = INTERVAL_AUTO;
		drawLine = false;
		tickLength = 6;
		logScale = false;
	}

	/**
	 * Set the axis default coordinates
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	public void setPosition(int x1, int y1, int x2, int y2) {
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
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
	 * @param minVal
	 * @param maxVal
	 */
	public void setRange(double minVal, double maxVal) {
		this.minVal = minVal;
		this.maxVal = maxVal;
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
	public void drawAxis(Graphics g, int x1, int y1, int x2, int y2) {
		setPosition(x1, y1, x2, y2);
		drawAxis(g);
	}

	FontMetrics fontMetrics;

	double axisValue, axisScale;

	int[] tickLengthX = new int[2], tickLengthY = new int[2];

	double labelOffsetX, labelOffsetY;

	double tickStepX, tickStepY;

	double axisAngle;

	double theInterval;

	int maxLabelWidth, maxLabelHeight;

	double totalPixs;

	double tickAngle;

	/**
	 * Draw the axis in the Graphics Context g
	 * @param g graphics context to draw on
	 */
	public void drawAxis(Graphics g) {

		if (overrideAxisColor==null) g.setColor(PamColors.getInstance().getColor(PamColor.AXIS));
		else g.setColor(overrideAxisColor);

		Graphics2D g2d = (Graphics2D) g;

		fontMetrics = g2d.getFontMetrics();

		theInterval = interval;
		totalPixs = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));

		axisScale = totalPixs / (maxVal - minVal);

		if (interval == INTERVAL_AUTO) {
			int minGap = Math.min(80, Math.max(Math.abs(x2-x1), Math.abs(y2-y1)));
			theInterval = getInterval((maxVal - minVal), (int) totalPixs, minGap);
		}
		if (drawLine)
			g.drawLine(x1, y1, x2, y2);
		if (forceFirstVal) {
			axisValue = forcedFirstVal;
		} else if (intervalStart) {
			axisValue = ((int) (minVal / theInterval)) * theInterval;
		} else {
			axisValue = minVal;
		}
		while (axisValue < minVal)
			axisValue += theInterval;

		axisAngle = Math.atan2(-(y2 - y1), x2 - x1);
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
		maxLabelHeight = fontMetrics.getAscent();
		maxLabelWidth = 0;

		if (logScale) {
			drawLogAxis(g2d);
		} else {
			drawLinearAxis(g2d);
		}
		drawAxisTitle(g2d);
	}

	/** 
	 * Work out the coordinates of all tick marks. 
	 * @param extraOne
	 * @return list of axis points.
	 */
	public ArrayList<Point> getAxisPoints(boolean extraOne) {

		if (logScale) {
			return getLogPoints(extraOne);
		} else {
			return getLinearPoints(extraOne);
		}
	}

	private ArrayList<Double> axisValues = new ArrayList<Double>();

	private Color overrideAxisColor;

	/**
	 * work out the coordinates of all tick marks using a linear scale
	 * @param extraOne
	 * @return
	 */
	private ArrayList<Point> getLinearPoints(boolean extraOne) {
		// separate this from the drawing bit so that it can be called by
		// the inner panel for grid drawing.
		ArrayList<Point> axisPoints = new ArrayList<Point>();
		axisValues.clear();

		// if ((x2 - x1) * (maxVal - minVal) < 0) tickStepX *= -1;
		tickStepX = Math.abs(theInterval) * Math.cos(axisAngle) * axisScale;
		tickStepY = -Math.abs(theInterval) * Math.sin(axisAngle) * axisScale;
		if ((maxVal - minVal) < 0) {
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
		if (x2 < x1)
			xDirection = -1;
		else if (x2 == x1)
			xDirection = 0;
		if (y2 < y1)
			yDirection = -1;
		else if (y2 == y1)
			yDirection = 0;
		double x = x1;
		double y = y1;
		if (forceFirstVal) {
			axisValue = forcedFirstVal;
			x = x1 + axisScale * (axisValue-minVal) * xDirection;
			y = y1 + axisScale * (axisValue-minVal) * yDirection;
		}
		while (((x2 - x) * xDirection >= -1 && (y2 - y) * yDirection >= -1)
				|| ticks < 2) {
			axisPoints.add(new Point((int)x,(int)y));
			axisValues.add(axisValue);
			//			DrawTickAndLabel(g2d, x, y, axisValue);
			x += tickStepX;
			y += tickStepY;
			axisValue += theInterval;
			if (ticks++ > 100)
				break;
		}
		if (extraOne) { // needed for minor tick marks on axis
			axisPoints.add(new Point((int)x,(int)y));
			axisValues.add(axisValue);
		}
		return axisPoints;
	}

	/**
	 * Draw a linear axis on the graphics context
	 * @param g2d graphics context to draw on
	 */
	protected void drawLinearAxis(Graphics2D g2d) {
		ArrayList<Point> axisPoints = getLinearPoints(false);
		// axis values are set in the same function and held 
		for (int i = 0; i < axisPoints.size(); i++) {
			drawTickAndLabel(g2d, axisPoints.get(i), axisValues.get(i));
		}
	}

	/**
	 * Draw a logarithmic axis on the graphics context
	 * @param g2d graphics context to draw on
	 */
	protected void drawLogAxis(Graphics2D g2d) {
		ArrayList<Point> axisPoints = getLogPoints(false);
		// axis values are set in the same function and held 

		for (int i = 0; i < axisPoints.size(); i++) {
			drawTickAndLabel(g2d, axisPoints.get(i), axisValues.get(i));
		}
	}

	/**
	 * Get the coordinates of tick points for a logarithmic axis
	 * @param extraOne
	 * @return Array of points
	 */
	protected ArrayList<Point> getLogPoints(boolean extraOne) {
		/*
		 * Start value must be on a power of 10. Assume that MaxValue MUST be
		 * greater than Min Value.
		 */
		ArrayList<Point> axisPoints = new ArrayList<Point>();
		axisValues.clear();
		double vx;
		double x = 0, y = 0;
		// String str;
		axisValue = minVal;
		axisValue = Math.floor(Math.log10(axisValue));
		// // axisValue = Math.pow(10, axisValue);
		if (maxVal / minVal <= 0.0) {
			return axisPoints;
		}
		axisScale = totalPixs / Math.log10(maxVal / minVal);
		tickStepX = Math.cos(axisAngle);
		tickStepY = Math.sin(axisAngle);
		if (minVal <= 0) {
			return axisPoints;
			//			minVal = maxVal / 1000;
			//			if (minVal <= 0) return axisPoints;
			//			axisValue = Math.floor(Math.log10(minVal));
		}
		double logMinVal = Math.log10(minVal);
		while (axisValue < logMinVal) {
			axisValue += 1;
		}
		while (true) {
			vx = (axisValue - logMinVal) * axisScale;
			if (vx < 0 || vx > totalPixs) {
				break;
			}
			x = (x1 + vx * tickStepX);
			y = (y1 - vx * tickStepY);
			axisPoints.add(new Point((int) x, (int) y));
			axisValues.add(Math.pow(10, axisValue));
			//			DrawTickAndLabel(g2d, x, y, Math.pow(10, axisValue));
			axisValue += 1;
			if (axisPoints.size() > totalPixs) {
				break;
			}
		}
		if (extraOne) { // needed for minor tick marks on axis
			x = (x1 + vx * tickStepX);
			y = (y1 - vx * tickStepY);
			axisPoints.add(new Point((int)x,(int)y));
			axisValues.add(axisValue);
		}
		return axisPoints;
	}

	/**
	 * Draw a tick and a label at the given point. 
	 * @param g2d
	 * @param xy
	 * @param value
	 */
	protected void drawTickAndLabel(Graphics2D g2d, Point xy, double value) {
		drawTickAndLabel(g2d, xy.x, xy.y, value);
	}

	/**
	 * Draw a tick and a label at the given point
	 * @param g2d
	 * @param x
	 * @param y
	 * @param value
	 */
	private void drawTickAndLabel(Graphics2D g2d, double x, double y,
			double value) {
		int xt, yt;
		String str;
		int labelWidth, labelHeight;

		str = formatValue(value);
		labelWidth = fontMetrics.charsWidth(str.toCharArray(), 0, str.length());
		labelHeight = fontMetrics.getAscent();

		xt = (int) x + tickLengthX[1];
		yt = (int) y + tickLengthY[1];
		if (isCrampLabels()) {
			if (tickLengthY[1] == 0) {
				yt = Math.max(Math.min(y1, y2)+labelHeight/2, yt);
				yt = Math.min(Math.max(y1, y2)-labelHeight/2, yt);
			}
			if (tickLengthX[1] == 0) {
				xt = Math.max(Math.min(x1, x2)+labelWidth/2, xt);
				xt = Math.min(Math.max(x1, x2)-labelWidth/2, xt);
			}
		}

		g2d.drawLine((int) x + tickLengthX[0], (int) y + tickLengthY[0], xt, yt);
		// print the label
		maxLabelWidth = Math.max(labelWidth, maxLabelWidth); // need this for drawing axistitles
		g2d.drawString(str, (int) (xt - labelOffsetX * labelWidth),
				(int) (yt + labelOffsetY));
	}

	/**
	 * Format the text for the label. 
	 * @param val value
	 * @return formatted String
	 */
	protected String formatValue(double val) {
		if (format == null) {
			return (new Double(val)).toString();
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
	public void drawGrid(Graphics g, Dimension plotSize, int minorGrid) {
		drawGrid(g, plotSize, null, minorGrid);
	}
	public void drawGrid(Graphics g, Dimension plotSize, Insets insets, int minorGrid) {	
		// need to first work out which way the lines are going and how long they are:
		// basically they go in the same direction as the tickmarks - given in tickAngle

		g.setColor(PamColors.getInstance().getColor(PamColor.GRID));

		int xExtent = (int) (Math.cos(tickAngle) * plotSize.getWidth());
		int yExtent = (int) (Math.sin(tickAngle) * plotSize.getHeight());
		ArrayList<Point> axisPoints = getAxisPoints(true);
		Point pt;
		Graphics2D g2 = (Graphics2D) g;
		int gx1, gx2, gy1, gy2;
		if (insets == null) {
			insets = new Insets(0, 0, 0, 0);
		}

		float[] dashes = {2, 2};
		Stroke s = g2.getStroke();
		if (minorGrid == 0) {
			// if there is a monir grid, draw the main one solid.
			// if no minor grid, thendo the main one dashed.
			g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, dashes, 0));
		}

		for (int i = 0; i < axisPoints.size(); i++) {
			pt = axisPoints.get(i);
			gx1 = pt.x - xExtent - x1 + insets.left;
			gy1 = pt.y - yExtent - y2 + insets.top; 
			gx2 = pt.x + xExtent - x1 + insets.left;
			gy2 = pt.y + yExtent - y2 + insets.top;

			if (gx1 <= insets.left && gx2 <= insets.left) continue;
			if (gy1 <= insets.top && gy2 <= insets.top) continue;
			g.drawLine(gx1, gy1, gx2, gy2);
		}

		if (minorGrid > 0) {
			float[] minordashes = {2, 2};
			g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, minordashes, 0));
			if (logScale) {
				drawLogMinorGrid(g, axisPoints, xExtent, yExtent, minorGrid);
			} else {
				drawLinearMinorGrid(g, axisPoints, xExtent, yExtent, minorGrid);
			}
		}
		g2.setStroke(s);
	}

	private void drawLinearMinorGrid(Graphics g, ArrayList<Point> axisPoints, int xExtent, int yExtent, int minorGrid) {

		if (axisPoints.size() < 2) return;

		Point p1, p2;
		double x, y;
		double stepX, stepY;
		int gx1, gx2, gy1, gy2;
		for (int i = 1; i < axisPoints.size(); i++) {
			p1 = axisPoints.get(i-1);
			p2 = axisPoints.get(i);
			stepX = (double) (p2.x-p1.x) / (minorGrid + 1);
			stepY = (double) (p2.y-p1.y) / (minorGrid + 1);
			x = p1.x;
			y = p1.y;
			for (int v = 0; v < minorGrid; v++) {
				x += stepX; 
				y += stepY; 
				gx1 = (int) (x - xExtent - x1);
				gy1 = (int) (y - yExtent - y2);
				gx2 = (int) (x + xExtent - x1);
				gy2 = (int) (y + yExtent - y2);
				if (gx1 <= 0 && gx2 <= 0) continue;
				if (gy1 <= 0 && gy2 <= 0) continue;
				g.drawLine(gx1, gy1, gx2, gy2);
			}
		}
	}
	private void drawLogMinorGrid(Graphics g, ArrayList<Point> axisPoints, int xExtent, int yExtent, int minorGrid) {
		// don't worry about value of minorGris - do all intermediate points.
		if (axisPoints.size() < 2) return;
		Point p1, p2;
		int x, y;
		double sepX, sepY;
		int gx1, gx2, gy1, gy2;
		for (int i = 1; i < axisPoints.size(); i++) {
			p1 = axisPoints.get(i-1);
			p2 = axisPoints.get(i);
			sepX = p2.x-p1.x;
			sepY = p2.y-p1.y;
			for (int v = 2; v < 10; v++) {
				x = (int) (p1.x + sepX * Math.log10(v)); 
				y = (int) (p1.y + sepY * Math.log10(v)); 
				gx1 = x - xExtent - x1;
				gy1 = y - yExtent - y2;
				gx2 = x + xExtent - x1;
				gy2 = y + yExtent - y2;
				if (gx1 <= 0 && gx2 <= 0) continue;
				if (gy1 <= 0 && gy2 <= 0) continue;
				g.drawLine(gx1, gy1, gx2, gy2);
			}
		}
	}

	private void drawAxisTitle(Graphics2D g2d) {
		if (label != null) {
			drawAxisTextItem(g2d, label, labelPos);
		}
		if (extraAxStartLabel != null) {
			drawAxisTextItem(g2d, extraAxStartLabel, LABEL_NEAR_MIN);
		}
		if (extraAxisEndLabel != null) {
			drawAxisTextItem(g2d, extraAxisEndLabel, LABEL_NEAR_MAX);
		}
	}

	private void drawAxisTextItem(Graphics2D g2d, String text, int position) {
		int x = x1, y = y1;
		// calculate for a simple x axis
		switch (position) {
		case LABEL_NEAR_MAX:
			x = (int) totalPixs
			- fontMetrics.charsWidth(text.toCharArray(), 0, text
					.length());
			break;
		case LABEL_NEAR_MIN:
			x = 0;
			break;
		case LABEL_NEAR_CENTRE:
			x = (int) (totalPixs - fontMetrics.charsWidth(text.toCharArray(),
					0, text.length())) / 2;
			break;
		}
		// switch ()
		if (tickPosition == BELOW_RIGHT) {
			y = tickLength + maxLabelHeight + fontMetrics.getAscent();
		} else {
			y = -(tickLength + maxLabelHeight + fontMetrics.getHeight());
		}
		if (x1==x2) { // vertical axis, so will rotate. 
			if (this.tickPosition == ABOVE_LEFT) {
//				y -= g2d.getFontMetrics().getDescent()*3;
			}
			else {
				y += g2d.getFontMetrics().getAscent()/2;

				//				y -= 30;
			}
			//			if (axisAngle < 0) {
			//				
			//			}
		}
		g2d.translate(x1, y1);
		g2d.rotate(-axisAngle);
		g2d.drawString(text, x, y);
		// y = -y;
		// g2d.drawString(label, x, y);
		g2d.rotate(axisAngle);
		g2d.translate(-x1, -y1);
	}


	/**
	 * 
	 * Gets the dimension of the axis perpendicular to the direction of the 
	 * tickmark - i.e. for a vertical axis, it's half the text height, for 
	 * a horizontal axis it's half the typical string width. 
	 * @param g graphics handle for component the axis is to be draw on 
	 * @return axis extent in pixels
	 */
	public int getExtent2(Graphics g) {
		return getExtent2(g, formatValue(Math.max(this.minVal,
				this.maxVal)));
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
	public int getExtent2(Graphics g, String typicalString) {
		if (x1 == x2) { // vertical axis
			// return half the height of typical text.
			return g.getFontMetrics().getHeight() / 2;
		}
		if (y1 == y2) {
			// return half the width of a typical string
			return g.getFontMetrics().charsWidth(
					typicalString.toCharArray(), 0,
					typicalString.length())/2;
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
	public int getExtent(Graphics g) {
		return getExtent(g, formatValue(Math.max(this.minVal,
				this.maxVal)));
	}

	/**
	 * Gets the dimension of the axis parallel to the direction of the 
	 * tickmark - i.e. the ticklength + the text height + the label height. 
	 * @param g graphics handle the axis is being draw on
	 * @param typicalString typical label string for a tick
	 * @return axisextent in pixels
	 */
	public int getExtent(Graphics g, String typicalString) {
		int e = 0;
		if (x1 == x2) {
			// vertical (y) axis
			e = tickLength
					+ g.getFontMetrics().charsWidth(
							typicalString.toCharArray(), 0,
							typicalString.length())
					+ g.getFontMetrics().charWidth('1');
			if (label != null && label.length() > 0)
				e += g.getFontMetrics().getHeight() * 3 / 2; // will draw it
			// on its side !
		} else if (y1 == y2) {
			e = tickLength + g.getFontMetrics().getHeight() * 3 / 2;
			if (label != null && label.length() > 0)
				e += g.getFontMetrics().getHeight() * 2 / 2;
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
		if (!allowScaleMultiples || fractionalScale) {
			baseinterval = 1;
			interval = Math.abs(intervals[intervals.length-1]);
		}

		for (int i = 0; i < intervals.length; i++) {
			gap = (int) (baseinterval * intervals[i] / Math.abs(range
					/ totalPixs));
			if (gap >= minPixs)
				return Math.min(range, baseinterval * intervals[i] * sign);
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
		return getAutoFormat(minVal, maxVal, isInteger);
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
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
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
		return maxVal;
	}

	public void setMaxVal(double maxVal) {
		this.maxVal = maxVal;
	}

	public double getMinVal() {
		return minVal;
	}

	public void setMinVal(double minVal) {
		this.minVal = minVal;
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
					// this is the lower of the two axis points, so really 
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

	public int getX1() {
		return x1;
	}

	public void setX1(int x1) {
		this.x1 = x1;
	}

	public int getX2() {
		return x2;
	}

	public void setX2(int x2) {
		this.x2 = x2;
	}

	public int getY1() {
		return y1;
	}

	public void setY1(int y1) {
		this.y1 = y1;
	}

	public int getY2() {
		return y2;
	}

	public void setY2(int y2) {
		this.y2 = y2;
	}

	/**
	 * Get a distance along the axis within the bounds of the axis, i.e. 
	 * generally within an inner panel drawn to the same bounds as the axis.<p>
	 * The axis knows all about scale and can tell us the 
	 * pixel value for any given data value. 
	 * @param dataValue
	 * @return position in pixels along the axis for a given data value.
	 */
	public double getPosition(double dataValue) {
		//		totalPixs = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
		double pixs;
		if (!logScale) {
			pixs =  totalPixs * (dataValue - getMinVal()) / (getMaxVal() - getMinVal());
		}
		else {
			if (dataValue < getMinVal()) {
				pixs = 0;
			}
			else {
				pixs =  totalPixs * (Math.log(dataValue) - Math.log(getMinVal())) / 
						(Math.log(getMaxVal()) - Math.log(getMinVal()));
			}
		}
		if (x1 == x2) {
			// for a while this was y1-pixs but that caused problems giving the coordinate on the
			// display the graph was draws on. I think that was added to draw plots for the 
			// QA module on a single plot - need to revert to what it was previously to that
			// and the QA module will have to add it's own offsets. 
			pixs = totalPixs - pixs; // y axis, so go from other side. 
		}
//		else { // also added for QA plots - get rid of !
//			pixs += x1; // no t100% sure this shouldn't be zero. 
//		}
		return pixs;
	}
	
	/**
	 * To get a position in the context that the axis is drawn in, 
	 * not the inner window relative to the axis. 
	 * The axis knows all about scale and can tell us the 
	 * pixel value for any given data value. 
	 * @param dataValue
	 * @return position in pixels along the axis for a given data value.
	 */
	public double getOuterPosition(double dataValue) {
		//		totalPixs = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
		double pixs;
		if (!logScale) {
			pixs =  totalPixs * (dataValue - getMinVal()) / (getMaxVal() - getMinVal());
		}
		else {
			pixs =  totalPixs * (Math.log(dataValue) - Math.log(getMinVal())) / 
					(Math.log(getMaxVal()) - Math.log(getMinVal()));
		}
		if (x1 == x2) {
			// for a while this was y1-pixs but that caused problems giving the coordinate on the
			// display the graph was draws on. I think that was added to draw plots for the 
			// QA module on a single plot - need to revert to what it was previously to that
			// and the QA module will have to add it's own offsets. 
			pixs = y1 - pixs; // y axis, so go from other side. 
		}
		else { // also added for QA plots - get rid of !
			pixs += x1; // no t100% sure this shouldn't be zero. 
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
		if (x1 == x2) {
			position = totalPixs - position; // y axis, so go from other side. 
		}
		if (!logScale) {
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
			scaleEnds = new double[]{maxVal};
			int nFac = 8;
			int fac = (int) Math.pow(2, nFac-1);
			intervals = new double[8];
			for (int i = 0; i < nFac; i++) { 
				intervals[i] = Math.abs(maxVal-minVal) / fac;
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
	 * Overrides the standard axis color;
	 * @param color- null if default colour. 
	 */
	public void overrideAxisColour(Color color){
		this.overrideAxisColor=color;
	}
}
