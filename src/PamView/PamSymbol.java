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
package PamView;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.io.Serializable;

import javax.swing.Icon;
import javax.swing.JPanel;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamView.PamColors.PamColor;
import PamView.symbol.SymbolData;

/**
 * 
 * Standard symbols for Pamguard graphics. A number of shapes
 * are available, most of which can have a fill colour and a line
 * colour. They may be created anywhere in Pamguard code using
 * new PamSymbol(...) and can also be created and configured using
 * a PamSymbolDialog to chose the symbol type, colours, line thicknesses, etc.
 * A selection of drawing routines allow you to draw them on plots with
 * varying sizes at any location.
 * 
 * @author Doug Gillespie
 * @see PamSymbolDialog
 * @see PanelOverlayDraw
 *
 */
public class PamSymbol extends PamSymbolBase implements Serializable, Icon, Cloneable, ManagedParameters {

	static public final long serialVersionUID = -5212611766032085395L;

	static public final int SYMBOL_NONE = 0;

	static public final int SYMBOL_REGIONSTART = 1;

	static public final int SYMBOL_LINESTART = 2;

	static public final int ICON_STYLE_SYMBOL = 0x1;

	static public final int ICON_STYLE_LINE = 0x2;

	private int iconStyle = ICON_STYLE_SYMBOL;

	
	
	/**
	 * Simplest constructor creates a PamSymbol with 
	 * default attributes. You will probably only use
	 * this constructor if you plan to subsequently
	 * modify it with PamSymbolDialog
	 * @see PamSymbolDialog
	 *
	 */
	public PamSymbol() {
		super(new SymbolData());
	}
	

	public PamSymbol(SymbolData symbolData) {
		super(symbolData);
	}


	/**
	 * Creates a PamSymbol with a given shape, size, colour, etc.
	 * @param symbol Symbol type
	 * @param width  Width of symbol in pixels
	 * @param height  Height of symbol in pixels
	 * @param fill  true if the symbol is to be filled, false if the shape should be hollow
	 * @param fillColor fill colour (required fill to be true)
	 * @param lineColor line colour
	 */
	public PamSymbol(PamSymbolType symbol, int width, int height, boolean fill,
			Color fillColor, Color lineColor) {
		super(new SymbolData(symbol, width, height, fill, fillColor, lineColor));
	}

	/**
	 * Draw the shape with additional options. 
	 * @param g graphics handle
	 * @param pt point to centre shape at. 
	 * @param drawingOptions drawing options
	 * @return bounding rectangle of draw
	 */
	public Rectangle draw(Graphics g, Point pt, ProjectorDrawingOptions drawingOptions) {
//		if (drawingOptions == null) {
//			return draw(g, pt);
//		}
//		int intAlpha = 255;
//		if (drawingOptions.getShapeOpacity() != null) {
//			intAlpha = (int) (drawingOptions.getShapeOpacity() * 255);
//		}
//		Color fillCol = getSymbolData().getFillColor();
//		Color newFill = new Color(fillCol.getRed(), fillCol.getGreen(), fillCol.getBlue(), intAlpha);
//		if (drawingOptions.getLineOpacity() != null) {
//			intAlpha = (int) (drawingOptions.getLineOpacity() * 255);
//		}
//		else {
//			intAlpha = 255;
//		}
//		Color lineCol = getSymbolData().getLineColor();
//		Color newLine = new Color(lineCol.getRed(), lineCol.getGreen(), lineCol.getBlue(),  intAlpha);

		return draw(g, pt, getSymbolData().width, getSymbolData().height, getSymbolData().fill, 
				getSymbolData().lineThickness, getSymbolData().getFillColor(), getSymbolData().getLineColor(), drawingOptions);
	}

	/**
	 * Draw the symbol at a given point using it's preset size.
	 * @param g graphics component to draw on
	 * @param pt x,y coordinate to draw centre of symbol at
	 */
	public Rectangle draw(Graphics g, Point pt) {
		return draw(g, pt, getSymbolData().width, getSymbolData().height, getSymbolData().fill, getSymbolData().lineThickness, 
				getSymbolData().getFillColor(), getSymbolData().getLineColor());
	}

	/**
	 * Draw the symbol at a given point using a new width and height.
	 * @param g graphics component to draw on
	 * @param pt x,y coordinate to draw centre of symbol at
	 * @param width width for drawing symbol (overrides preset width)
	 * @param height height for drawing symbol (overrides prest height)
	 */
	public Rectangle draw(Graphics g, Point pt, int width, int height) {
		return draw(g, pt, width, height, getSymbolData().fill, getSymbolData().lineThickness, 
				getSymbolData().getFillColor(), getSymbolData().getLineColor());
	}
	
	/**
	 * Draw the symbol at a given point using a new width and height.
	 * @param g graphics component to draw on
	 * @param pt x,y coordinate to draw centre of symbol at
	 * @param width width for drawing symbol (overrides preset width)
	 * @param height height for drawing symbol (overrides preset height)
	 * @param drawingOptions additional drawing options.
	 * @return 
	 */
	public Rectangle draw(Graphics g, Point pt, int width, int height, ProjectorDrawingOptions drawingOptions) {
		return draw(g, pt, width, height, getSymbolData().fill, getSymbolData().lineThickness, 
				getSymbolData().getFillColor(), getSymbolData().getLineColor(), drawingOptions);
	}

	public Rectangle draw(Graphics g, Point pt, double w,
			double h, boolean fill, float lineThickness, int fillColorRGB,
			int lineColorRGB) {
		return draw(g, pt, w, h, fill, lineThickness, new Color(fillColorRGB), new Color(lineColorRGB));
	}

	public Rectangle draw(Graphics g, Point pt, double w,
			double h, boolean fill, float lineThickness, Color fillColor,
			Color lineColor, ProjectorDrawingOptions drawingOptions) {
		if (drawingOptions == null) {
			return draw(g, pt, w, h, fill, lineThickness, fillColor, lineColor);
		}
		else {
			int intAlpha = 255;
			if (drawingOptions.getShapeOpacity() != null) {
				intAlpha = (int) (drawingOptions.getShapeOpacity() * 255);
			}
			Color fillCol = getSymbolData().getFillColor();
			Color newFill = new Color(fillCol.getRed(), fillCol.getGreen(), fillCol.getBlue(), intAlpha);
			if (drawingOptions.getLineOpacity() != null) {
				intAlpha = (int) (drawingOptions.getLineOpacity() * 255);
			}
			else {
				intAlpha = 255;
			}
			Color lineCol = getSymbolData().getLineColor();
			Color newLine = new Color(lineCol.getRed(), lineCol.getGreen(), lineCol.getBlue(),  intAlpha);

			return draw(g, pt, w, h, fill, lineThickness, newFill, newLine);
		}
	}
	/**
	 * 
	 * Draw the symbol using a complete new set of parameters.
	 * @param g graphics component to draw on
	 * @param pt x,y coordinate to draw centre of symbol at
	 * @param w width for drawing symbol (overrides preset width)
	 * @param h height for drawing symbol (overrides prest height)
	 * @param fill true if the symbol is to be filled, false for hollow
	 * @param lineThickness outer line thickness
	 * @param fillColor fill colour
	 * @param lineColor line colour
	 * @return a rectangle giving an outer boud of the shape (can be used to invaldiate a 
	 * graphic for redrawing).
	 */
	public Rectangle draw(Graphics g, Point pt, double w,
			double h, boolean fill, float lineThickness, Color fillColor,
			Color lineColor) {

		Graphics2D g2d = (Graphics2D) g;

		// g2d.setPaint(lineColor);
		// g2d.setColor(lineColor);
		Stroke oldStroke = g2d.getStroke();
		g2d.setStroke(new BasicStroke(lineThickness, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_MITER));

		int i;
		double halfWidth = Math.max(1., w / 2.);
		double halfHeight = Math.max(1., h / 2.);
		// if (!Fill) DC.RestoreBrush();
		// LOGPEN lp;
		if (getSymbolData().symbol == PamSymbolType.SYMBOL_POINT) {
			// HPEN CurrPen = (HPEN) DC.GetCurrentObject(OBJ_PEN);
			// OWL::TPen tp(CurrPen);
			// tp.GetObject(lp);
			// g2d.fil
			//			draw(g, pt, SYMBOL_CIRCLE, 2, 2, true, 1, lineColor, lineColor);
		}
		if (iconStyle == ICON_STYLE_LINE) {
			g2d.setPaint(lineColor);
			g.drawLine((int) (pt.x - halfWidth), pt.y,
					(int) (pt.x + halfWidth), pt.y);
			setSquareDrawnPolygon(pt.x, (int) (pt.y - halfHeight), pt.x,
					(int) (pt.y + halfHeight));
		}
		else switch (getSymbolData().symbol) {
		case SYMBOL_POINT:
			halfWidth = 1;
			halfHeight = 1;
			w = h = 2;
			fill = true;
			fillColor = lineColor;
			// DC.SetPixel(Pt, NS_CLASSLIB::TColor(lp.lopnColor));
			Ellipse2D j = new Ellipse2D.Double(pt.x - halfWidth, pt.y
					- halfHeight, w, h);
			if (fill) {
				g2d.setPaint(fillColor);
				g2d.fill(j);
			}
			// else {
			g2d.setPaint(lineColor);
			g2d.draw(j);
			setSquareDrawnPolygon(pt.x, pt.y, pt.x, pt.y);
			break;
		case SYMBOL_CROSS:
			g2d.setPaint(lineColor);
			g.drawLine((int) (pt.x - halfWidth), pt.y,
					(int) (pt.x + halfWidth), pt.y);
			g.drawLine(pt.x, (int) (pt.y - halfHeight), pt.x,
					(int) (pt.y + halfHeight));
			setSquareDrawnPolygon(pt.x, (int) (pt.y - halfHeight), pt.x,
					(int) (pt.y + halfHeight));
			break;
		case SYMBOL_CROSS2:
			g2d.setPaint(lineColor);
			g.drawLine((int) (pt.x - halfWidth), (int) (pt.y - halfHeight),
					(int) (pt.x + halfWidth), (int) (pt.y + halfHeight));
			g.drawLine((int) (pt.x + halfWidth), (int) (pt.y - halfHeight),
					(int) (pt.x - halfWidth), (int) (pt.y + halfHeight));
			setSquareDrawnPolygon(pt.x, (int) (pt.y - halfHeight), pt.x,
					(int) (pt.y + halfHeight));
			break;
		case SYMBOL_SQUARE:
			drawScaledPolygon(g2d, pt, 3, halfWidth, halfHeight, sqx, sqy,
					fill, fillColor, lineColor);
			break;
		case SYMBOL_TRIANGLEU:
			drawScaledPolygon(g2d, pt, 3, halfWidth, halfHeight, trux, truy,
					fill, fillColor, lineColor);
			break;
		case SYMBOL_TRIANGLED:
			drawScaledPolygon(g2d, pt, 3, halfWidth, halfHeight, trdx, trdy,
					fill, fillColor, lineColor);
			break;
		case SYMBOL_TRIANGLER:
			drawScaledPolygon(g2d, pt, 3, halfWidth, halfHeight, trrx, trry,
					fill, fillColor, lineColor);
			break;
		case SYMBOL_TRIANGLEL:
			drawScaledPolygon(g2d, pt, 3, halfWidth, halfHeight, trlx, trly,
					fill, fillColor, lineColor);
			break;
		case SYMBOL_DOUBLETRIANGLER:
			pt.x -= (halfWidth/2+1);
			drawScaledPolygon(g2d, pt, 3, halfWidth, halfHeight, trrx, trry,
					fill, fillColor, lineColor);
			pt.x += halfWidth+2;
			drawScaledPolygon(g2d, pt, 3, halfWidth, halfHeight, trrx, trry,
					fill, fillColor, lineColor);
			break;
		case SYMBOL_DOUBLETRIANGLEL:
			pt.x -= (halfWidth/2+1);
			drawScaledPolygon(g2d, pt, 3, halfWidth, halfHeight, trlx, trly,
					fill, fillColor, lineColor);
			pt.x += halfWidth+2;
			drawScaledPolygon(g2d, pt, 3, halfWidth, halfHeight, trlx, trly,
					fill, fillColor, lineColor);
			break;
		case SYMBOL_PENTAGRAM:
			drawScaledPolygon(g2d, pt, 5, halfWidth, halfHeight, pentx, penty,
					fill, fillColor, lineColor);
			break;
		case SYMBOL_HEXAGRAM:
			drawScaledPolygon(g2d, pt, 6, halfWidth, halfHeight, hexx, hexy,
					fill, fillColor, lineColor);
			break;
		case SYMBOL_DIAMOND:
			drawScaledPolygon(g2d, pt, 6, halfWidth, halfHeight, diax, diay,
					fill, fillColor, lineColor);
			break;
		case SYMBOL_CIRCLE:
			Ellipse2D o = new Ellipse2D.Double(pt.x - halfWidth+1, pt.y
					- halfHeight+1, w-2, h-2);
			if (fill) {
				g2d.setPaint(fillColor);
				g2d.fill(o);
			}
			// else {
			g2d.setPaint(lineColor);
			g2d.draw(o);
			setSquareDrawnPolygon(pt.x, (int) (pt.y - halfHeight), pt.x,
					(int) (pt.y + halfHeight));
			// }
			break;
		case SYMBOL_STAR:
			for (i = 0; i < 6; i++) {
				g2d.setPaint(lineColor);
				g2d.drawLine(pt.x, pt.y, (int) (pt.x + Math.ceil(hexx[i]
				                                                      * halfWidth)), (int) (pt.y - Math.ceil(hexy[i]
				                                                                                                  * halfHeight)));
				// DC.MoveTo(pt.x + ceil(hexx[i] * HalfSize), pt.y -
				// ceil(hexy[i]*HalfSize));
				// DC.LineTo(pt.x + ceil(hexx[i+3] * HalfSize), pt.y -
				// ceil(hexy[i+3]*HalfSize));
			}
			setSquareDrawnPolygon(pt.x, (int) (pt.y - halfHeight), pt.x,
					(int) (pt.y + halfHeight));
			break;
		case SYMBOL_CUSTOMPOLYGON:
			drawScaledPolygon(g2d, pt, 6, halfWidth, halfHeight, getXPoints(), getYPoints(),
					fill, fillColor, lineColor);
			break;
		default:
			g2d.setPaint(lineColor);
			g2d.fillOval((int) (pt.x - halfWidth), (int) (pt.y - halfHeight),
					(int) (2* halfWidth), (int) (2* halfHeight));
			setSquareDrawnPolygon(pt.x, (int) (pt.y - halfHeight), pt.x,
					(int) (pt.y + halfHeight));
			break;
		}
		g2d.setStroke(oldStroke);
		return new Rectangle((int) Math.floor(pt.x - halfWidth), (int) Math.floor(pt.y - halfHeight),
				(int) w+1, (int) h+1);
	}

	/**
	 * Called to set a square drawn polygon when 
	 * drawing shapes which didn't actually use the polygon draw function. 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	private void setSquareDrawnPolygon(int x1, int y1, int x2, int y2) {
		int[] xp = {x1, x1, x2, x2};
		int[] yp = {y1, y2, y2, y1};
		drawnPolygon = new Polygon(xp, yp, 4);
	}

	/**
	 * The last polygon used for drawing. 
	 */
	private transient Polygon drawnPolygon;

	public static final int ICON_HORIZONTAL_LEFT = 0;
	public static final int ICON_HORIZONTAL_CENTRE = 1;
	public static final int ICON_HORIZONTAL_RIGHT = 2;
	public static final int ICON_HORIZONTAL_FILL = 3;
	private int iconHorizontalAlignment = ICON_HORIZONTAL_LEFT;
	public static final int ICON_VERTICAL_TOP = 0;
	public static final int ICON_VERTICAL_MIDDLE = 1;
	public static final int ICON_VERTICAL_BOTTOM = 2;
	public static final int ICON_VERTICAL_FILL = 3;
	private int iconVerticalAlignment = ICON_VERTICAL_MIDDLE;

	/**
	 * 
	 * @return the last drawn polygon
	 */
	public Polygon getDrawnPolygon() {
		return drawnPolygon;
	}

	/**
	 * Does the actual drawing work.
	 */
	private void drawScaledPolygon(Graphics2D g2d, Point pt, int np,
			double halfWidth, double halfHeight, double[] px, double[] py,
			boolean fill, Color fillColor, Color lineColor) {

		if (px == null || py == null) return;

		int[] xpoints = new int[px.length + 1];
		int[] ypoints = new int[py.length + 1];
		for (int i = 0; i < px.length; i++) {
			xpoints[i] = (int) (pt.x + px[i] * halfWidth);
			ypoints[i] = (int) (pt.y + py[i] * halfHeight);
		}
		xpoints[px.length] = xpoints[0];
		ypoints[px.length] = ypoints[0];

		drawnPolygon = new Polygon(xpoints, ypoints, xpoints.length);
		if (fill) {
			g2d.setPaint(fillColor);
			g2d.fill(drawnPolygon);
		}
		g2d.setPaint(lineColor);
		g2d.draw(drawnPolygon);

	}


	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		//		g.drawLine(0, 0, 10, 10);
		Graphics2D g2d = (Graphics2D) g;
		if ((iconStyle & ICON_STYLE_SYMBOL) != 0) {
			//			draw(g, new Point(c.getWidth() + x - getIconHeight() / 2, y + getIconHeight() / 2),
			//					getIconHeight()-2, getIconHeight()-2, fill, lineThickness,
			//					fillColor, lineColor);
			if (c == null) {
				draw(g, new Point(x + getIconWidth() / 2, getIconHeight() / 2),
						getIconWidth(), getIconHeight(), getSymbolData().fill, getSymbolData().lineThickness,
						getSymbolData().getFillColor(), getSymbolData().getLineColor());
			}
			else {
				int w = (int) getWidth();
				int h = (int) getHeight();
				if (w == 0) {
					w = getIconWidth();
				}
				if (h == 0) {
					h = getIconHeight();
				}
				//				x = c.getWidth()/2;
				switch (iconHorizontalAlignment) {
				case ICON_HORIZONTAL_LEFT:
					x = getIconWidth()/2+1;
					break;
				case ICON_HORIZONTAL_RIGHT:
					x = c.getWidth()-getIconWidth()/2-1;
					break;
				case ICON_HORIZONTAL_CENTRE:
					x = c.getWidth()/2;
					break;
				case ICON_HORIZONTAL_FILL:
					x = c.getWidth()/2;
					w = c.getWidth()-2;
					break;
				default:
					x = (int) Math.max(x, getWidth() / 2);						
				}
				switch (iconVerticalAlignment) {
				case ICON_VERTICAL_TOP:
					y = h/2+1;
					break;
				case ICON_VERTICAL_BOTTOM:
					y = c.getHeight() - h/2 - 1;
					break;
				case ICON_VERTICAL_MIDDLE:
					y = c.getHeight()/2;
					break;
				case ICON_VERTICAL_FILL:
					y = c.getHeight()/2;
					h = c.getHeight()-2;
				default:
					y = c.getHeight() / 2;						
				}
				draw(g, new Point(x, y),
						w, h, getSymbolData().fill, getSymbolData().lineThickness,
						getSymbolData().getFillColor(), getSymbolData().getLineColor());
			}
		}
		if ((iconStyle & ICON_STYLE_LINE) != 0) {
			g.setColor(getSymbolData().getLineColor());
			g2d.setStroke(new BasicStroke(getSymbolData().lineThickness, BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_MITER));
		
			g.drawLine(x, c.getHeight() / 2, getIconWidth(), c.getHeight() / 2);
		}
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub

		switch (getSymbolData().symbol) {
//		case SYMBOL_NONE:
//		case SYMBOL_REGIONSTART:
//		case SYMBOL_LINESTART:
//		case SYMBOL_LINESEGMENT:
//			break;
		case SYMBOL_CROSS:
			return "+ Cross";
		case SYMBOL_CROSS2:
			return "X Cross";
		case SYMBOL_SQUARE:
			return "Square";
		case SYMBOL_TRIANGLEU:
			return "Up Traiangle";
		case SYMBOL_CIRCLE:
			return "Circle";
		case SYMBOL_DIAMOND:
			return "Diamond";
		case SYMBOL_POINT:
			return "Point";
		case SYMBOL_STAR:
			return "Star";
		case SYMBOL_TRIANGLED:
			return "Down Triangle";
		case SYMBOL_TRIANGLEL:
			return "Left Triangle";
		case SYMBOL_TRIANGLER:
			return "Right Triangle";
		case SYMBOL_PENTAGRAM:
			return "Pentagram";
		case SYMBOL_HEXAGRAM:
			return "Hexagram";
		}

		return super.toString();
	}

	/**
	 * @return true if the symbol is a solid shape - e.g. is true
	 * for a circle, but false for a cross.
	 */
	public boolean isSolidShape() {
		// returns true for shapes than can have fill
		if (iconStyle == ICON_STYLE_LINE) {
			return false;
		}
		switch (getSymbolData().symbol) {
		case SYMBOL_SQUARE:
		case SYMBOL_TRIANGLEU:
		case SYMBOL_CIRCLE:
		case SYMBOL_DIAMOND:
		case SYMBOL_POINT:
		case SYMBOL_TRIANGLED:
		case SYMBOL_TRIANGLEL:
		case SYMBOL_TRIANGLER:
		case SYMBOL_PENTAGRAM:
		case SYMBOL_HEXAGRAM:
			return true;
		}

		return false;
	}
	/**
	 * Returns the icon's width.
	 * 
	 * @return an int specifying the fixed width of the icon.
	 */
	public int getIconWidth() {
		int iconWidth = 0;
		if ((iconStyle & ICON_STYLE_SYMBOL) != 0) {
			iconWidth += 16;
		}
		if ((iconStyle & ICON_STYLE_LINE) != 0) {
			iconWidth += 16;
		}
		return Math.max(iconWidth, 16);
	}

	/**
	 * Returns the icon's height.
	 * 
	 * @return an int specifying the fixed height of the icon.
	 */
	public int getIconHeight() {
		if ((iconStyle & ICON_STYLE_SYMBOL) != 0) {
			return 16;
		}
		else {
			return 3;
		}
		//		return 16;
	}

	public boolean isFill() {
		return getSymbolData().fill;
	}

	public void setFill(boolean fill) {
		getSymbolData().fill = fill;
	}

	public Color getFillColor() {
		return getSymbolData().getFillColor();
	}

	public void setFillColor(Color fillColor) {
		getSymbolData().setFillColor(fillColor);
	}

	public Color getLineColor() {
		return getSymbolData().getLineColor();
	}

	public void setLineColor(Color lineColor) {
		getSymbolData().setLineColor(lineColor);
	}

	public PamSymbolType getSymbol() {
		return getSymbolData().symbol;
	}

	public void setSymbol(PamSymbolType symbol) {
		getSymbolData().symbol = symbol;
	}

	/**
	 * Get the symbol width as an integer
	 * @return symbol width in pixels
	 * @see getDWidth() Symbol width as a double
	 */
	public int getWidth() {
		return (int) getSymbolData().width;
	}

	/**
	 * Get the symbol height as an integer
	 * @return symbol height in pixels
	 * @see getDHeight() Symbol height as a double
	 */
	public int getHeight() {
		return (int) getSymbolData().height;
	}

	/**
	 * Create a small JPanel to incorporate into 
	 * a key. The component will contain a small panel
	 * on the left with a symbol drawn in it and a panel
	 * on the right with the text as a JLabel.
	 * @param text
	 * @return Java component to include in a key
	 */
	public PamKeyItem makeKeyItem(String text) {
		return new SymbolKeyItem(this, text);
	}

	private class KeyPanel extends JPanel implements ColorManaged {
		static private final int size = 16;
		KeyPanel() {
			setPreferredSize(new Dimension(size,size));
			//			PamColors.getInstance().registerComponent(this, PamColor.PlOTWINDOW);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			draw(g, new Point(size/2, size/2), size-4, size-4);
		}

		private PamColor defaultColor = PamColor.PlOTWINDOW;

		public PamColor getDefaultColor() {
			return defaultColor;
		}

		public void setDefaultColor(PamColor defaultColor) {
			this.defaultColor = defaultColor;
		}

		@Override
		public PamColor getColorId() {
			return defaultColor;
		}
	}

	@Override
	public PamSymbol clone() {
		// TODO Auto-generated method stub
		try {
			PamSymbol newSymbol = (PamSymbol) super.clone();
			if (newSymbol.getSymbolData() == null) {
				newSymbol.setSymbolData(new SymbolData());
			}
			else {
				newSymbol.setSymbolData(getSymbolData().clone());
			}
			return newSymbol;
		} catch (CloneNotSupportedException Ex) {
			Ex.printStackTrace();
		}
		return null;
	}

	public static Rectangle drawArrow(Graphics g, int x1, int y1, int x2, int y2, int headSize) {
		return drawArrow(g, x1, y1, x2, y2, headSize, 45, false);
	}

	public static Rectangle drawArrow(Graphics g, double x1, double y1, double x2, double y2, double headSize, double headAngle, boolean doubleEnded) {
		Rectangle r = new Rectangle((int)Math.min(x1, x2), (int)Math.min(y1, y2), (int)Math.abs(x1-x2),(int) Math.abs(y2-y1));
		g.drawLine((int) x1, (int)y1, (int)x2, (int)y2);

		double arrowDir = Math.atan2(y2-y1, x2-x1);
		double x3, y3;
		double newDir;
		newDir = arrowDir + (180-headAngle) * Math.PI/180;
		x3 = x2 +  (headSize * Math.cos(newDir));
		y3 = y2 +  (headSize * Math.sin(newDir));
		g.drawLine((int) x2, (int) y2, (int) x3, (int) y3);
		newDir = arrowDir - (180-headAngle) * Math.PI/180;
		x3 = x2 + (headSize * Math.cos(newDir));
		y3 = y2 + (headSize * Math.sin(newDir));
		g.drawLine((int) x2, (int) y2, (int) x3, (int) y3);
		if (doubleEnded) {
			arrowDir = arrowDir + Math.PI;
			newDir = arrowDir + (180-headAngle) * Math.PI/180;
			x3 = x1 + (int) (headSize * Math.cos(newDir));
			y3 = y1 + (int) (headSize * Math.sin(newDir));
			g.drawLine((int) x1, (int) y1, (int) x3, (int) y3);
			newDir = arrowDir - (180-headAngle) * Math.PI/180;
			x3 = x1 + (int) (headSize * Math.cos(newDir));
			y3 = y1 + (int) (headSize * Math.sin(newDir));
			g.drawLine((int) x1, (int) y1, (int) x3, (int) y3);
		}

		return r;
	}

	public double[] getXPoints() {
		return null;
	}

	public double[] getYPoints() {
		return null;
	}

	/**
	 * @return the iconStyle
	 */
	public int getIconStyle() {
		return iconStyle;
	}

	/**
	 * @param iconStyle the iconStyle to set
	 */
	public void setIconStyle(int iconStyle) {
		this.iconStyle = iconStyle;
	}

	/**
	 * 
	 * @param hAlignment the icon horizontal alignment
	 */
	public void setIconHorizontalAlignment(int hAlignment) {
		this.iconHorizontalAlignment = hAlignment;
	}

	/**
	 * @return the iconVerticalAlignment
	 */
	public int getIconVerticalAlignment() {
		return iconVerticalAlignment;
	}

	/**
	 * @param iconVerticalAlignment the iconVerticalAlignment to set
	 */
	public void setIconVerticalAlignment(int iconVerticalAlignment) {
		this.iconVerticalAlignment = iconVerticalAlignment;
	}

	/**
	 * @return the iconHorizontalAlignment
	 */
	public int getIconHorizontalAlignment() {
		return iconHorizontalAlignment;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
