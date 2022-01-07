package pamViewFX.fxNodes;


import java.io.Serializable;

import PamView.PamSymbol;
import PamView.PamSymbolBase;
import PamView.PamSymbolType;
import PamView.symbol.SymbolData;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;

public class PamSymbolFX  extends PamSymbolBase implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
//	/**
//	 * Width in pixels of symbol
//	 */
//	private double width;
//
//	/**
//	 * Height in pixels of symbol
//	 */
//	private double height;
//
//	/**
//	 * Fill symbol or has transparent background
//	 */
//	private boolean fill;
//
//	/**
//	 * Fill colour
//	 */
//	private Color fillColor;
//
//	/**
//	 * Line colour. 
//	 */
//	private Color lineColor;
//
//
//	/**
//	 * The current symbol
//	 */
//	private PamSymbolType symbol = PamSymbolType.SYMBOL_CIRCLE;
//
//	/**
//	 * thickness of line. 
//	 */
//	private float lineThickness;

//
//	public enum PamSymbolType {
//		SYMBOL_CIRCLE, SYMBOL_CROSS,
//		SYMBOL_CROSS2, SYMBOL_SQUARE, SYMBOL_TRIANGLEU, SYMBOL_DIAMOND, SYMBOL_POINT, SYMBOL_STAR,
//		SYMBOL_TRIANGLED, SYMBOL_TRIANGLEL,SYMBOL_TRIANGLER, SYMBOL_PENTAGRAM,
//		SYMBOL_HEXAGRAM, SYMBOL_CUSTOMPOLYGON, SYMBOL_DOUBLETRIANGLEL, SYMBOL_DOUBLETRIANGLER
//	}



	/**
	 * Creates a PamSymbolFX with a given shape, size, colour, etc.
	 * @param symbol Symbol type
	 * @param width  Width of symbol in pixels
	 * @param height  Height of symbol in pixels
	 * @param fill  true if the symbol is to be filled, false if the shape should be hollow
	 * @param fillColor fill colour (required fill to be true)
	 * @param lineColor line colour
	 */
	public PamSymbolFX(PamSymbolType symbol, int width, int height, boolean fill,
			Color fillColor, Color lineColor) {
		super(new SymbolData(symbol, width, height, fill, PamUtilsFX.fxToAWTColor(fillColor), PamUtilsFX.fxToAWTColor(lineColor)));
		// pamViewFX.fxNodex.utilsFX.PamUtilsFX
	}
	
	/**
	 * Get a colour RGB integer value from an FX color.
	 * @param colour
	 * @return
	 */
	public static int getIntRGB(Color colour) {
		int r = (int) (colour.getRed()*255);
		int g = (int) (colour.getGreen()*255);
		int b = (int) (colour.getBlue()*255);
		return r<<16 & g<<8 & b;
	}
	
	/**
	 * Make an FX colour from a standard integer RGB
	 * @param rgb
	 * @return
	 */
	public static Color makeRGBColor(int rgb) {
		int r = rgb>>16;
		int g = (rgb>>8)&0xFF;
		int b = rgb&0xFF;
		return Color.rgb(r, g, b);
	}


	/**
	 * Create a default black PamSymbolFX. 
	 */
	public PamSymbolFX() {
		super(new SymbolData());
	}

	public PamSymbolFX(SymbolData symbolData) {
		super(symbolData);
	}

	/**
	 * Construct a PamSymbolFX from a PamSymbol.
	 * @param symbol2 - the AWT PamSymbol. 
	 */
	public PamSymbolFX(PamSymbol pamSymbol) {
		super(pamSymbol.getSymbolData().clone());
	}


	/**
	 * Draw the symbol onto a canvas. 
	 * @param g- graphics context from canvas to draw symbol on. 
	 * @param pt- point on canvas to draw symbol
	 * @return 
	 */
	public Rectangle draw(GraphicsContext g, Point2D pt) {
		return draw(g, pt, getSymbolData().width, getSymbolData().height, getSymbolData().fill, getSymbolData().lineThickness,
				getFillColor(), getLineColor());
	}
	
	public Rectangle draw(GraphicsContext g, Point2D pt, double width, double height) {
		return draw(g, pt, width, height, getSymbolData().fill, getSymbolData().lineThickness,
				getFillColor(), getLineColor());
	}

	public Rectangle draw(GraphicsContext g2d , Point2D pt, double w,
			double h, boolean fill, float lineThickness, Color fillColor,
			Color lineColor) {
		return draw(getSymbolData().symbol, g2d ,  pt,  w,
				h,  fill,lineThickness  ,  fillColor,
				lineColor);
	}

	public static Rectangle draw(PamSymbolType symbol, GraphicsContext g2d , Point2D pt, double w,
			double h, boolean fill, float lineThickness, Color fillColor,
			Color lineColor) {

		int i;
		double halfWidth = Math.max(1., w / 2.);
		double halfHeight = Math.max(1., h / 2.);

		g2d.setLineWidth(lineThickness);
		switch (symbol) {
		case SYMBOL_POINT:
			halfWidth = 1;
			halfHeight = 1;
			w = h = 2;
			fill = true;
			fillColor = lineColor;
			if (fill) {
				g2d.setFill(fillColor);
				g2d.fillOval(pt.getX() - halfWidth, pt.getY()
						- halfHeight, w, h);
			}
			// else {
			g2d.setStroke(lineColor);
			g2d.strokeOval(pt.getX() - halfWidth, pt.getY()
					- halfHeight, w, h);
			setSquareDrawnPolygon(pt.getX(), pt.getY(), pt.getX(), pt.getY());
			break;
		case SYMBOL_CROSS:
			g2d.setStroke(lineColor);
			g2d.strokeLine((int) (pt.getX() - halfWidth), pt.getY(),
					(int) (pt.getX() + halfWidth), pt.getY());
			g2d.strokeLine(pt.getX(), (int) (pt.getY() - halfHeight), pt.getX(),
					(int) (pt.getY() + halfHeight));
			setSquareDrawnPolygon(pt.getX(), (pt.getY() - halfHeight), pt.getX(),
					(pt.getY() + halfHeight));
			break;
		case SYMBOL_CROSS2:
			g2d.setStroke(lineColor);
			g2d.strokeLine((int) (pt.getX() - halfWidth), (int) (pt.getY()- halfHeight),
					(int) (pt.getX() + halfWidth), (int) (pt.getY() + halfHeight));
			g2d.strokeLine((int) (pt.getX() + halfWidth), (int) (pt.getY() - halfHeight),
					(int) (pt.getX() - halfWidth), (int) (pt.getY() + halfHeight));
			setSquareDrawnPolygon(pt.getX(), (int) (pt.getY() - halfHeight), pt.getX(),
					(int) (pt.getY() + halfHeight));
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
			Point2D pt1R=new Point2D(pt.getX()- (halfWidth/2.+1),pt.getY());
			drawScaledPolygon(g2d, pt1R, 3, halfWidth, halfHeight, trrx, trry,
					fill, fillColor, lineColor);
			pt1R=new Point2D(pt1R.getX()+ ( halfWidth+2.),pt1R.getY());
			drawScaledPolygon(g2d, pt1R, 3, halfWidth, halfHeight, trrx, trry,
					fill, fillColor, lineColor);
			break;
		case SYMBOL_DOUBLETRIANGLEL:
			Point2D pt1=new Point2D(pt.getX()- (halfWidth/2.+1),pt.getY());
			drawScaledPolygon(g2d, pt1, 3, halfWidth, halfHeight, trlx, trly,
					fill, fillColor, lineColor);
			pt1=new Point2D(pt1.getX()+ ( halfWidth+2.),pt1.getY());
			drawScaledPolygon(g2d, pt1, 3, halfWidth, halfHeight, trlx, trly,
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
			if (fill) {
				g2d.setFill(fillColor);
				g2d.fillOval(pt.getX() - halfWidth+1, pt.getY()
						- halfHeight+1, w-2, h-2);
			}
			// else {
			g2d.setStroke(lineColor);
			g2d.strokeOval(pt.getX() - halfWidth+1, pt.getY()
					- halfHeight+1, w-2, h-2);

			setSquareDrawnPolygon(pt.getX(), (int) (pt.getY() - halfHeight), pt.getX(),
					(int) (pt.getY() + halfHeight));
			// }
			break;
		case SYMBOL_STAR:
			for (i = 0; i < 6; i++) {
				g2d.setStroke(lineColor);
				g2d.strokeLine(pt.getX(), pt.getY(), (int) (pt.getX() + Math.ceil(hexx[i]
						* halfWidth)), (int) (pt.getY() - Math.ceil(hexy[i]
								* halfHeight)));
				// DC.MoveTo(pt.x + ceil(hexx[i] * HalfSize), pt.y -
						// ceil(hexy[i]*HalfSize));
				// DC.LineTo(pt.x + ceil(hexx[i+3] * HalfSize), pt.y -
				// ceil(hexy[i+3]*HalfSize));
			}
			setSquareDrawnPolygon(pt.getX(), (pt.getY() - halfHeight), pt.getX(),
					pt.getY() + halfHeight);
			break;
		case SYMBOL_CUSTOMPOLYGON:
			drawScaledPolygon(g2d, pt, 6, halfWidth, halfHeight, null, null,
					fill, fillColor, lineColor);
			break;
		default:
			g2d.setFill(lineColor);
			g2d.fillOval((int) (pt.getX() - halfWidth), (int) (pt.getY() - halfHeight),
					(int) (pt.getX() + halfWidth), (int) (pt.getY() + halfHeight));
			setSquareDrawnPolygon(pt.getX(), (int) (pt.getY() - halfHeight), pt.getX(),
					(int) (pt.getY() + halfHeight));
			break;
		}
		return new Rectangle((int) Math.floor(pt.getX() - halfWidth), (int) Math.floor(pt.getY() - halfHeight),
				(int) w+1, (int) h+1);
	}


//	/**
//	 * Get a PamSymbolType from a PamSymbol flag. This helps convert between PamSymbol and PamSymbolFX
//	 * @param symbolflag - the flag of a PamSymbol. 
//	 * @return the PamSymbolType corresponding to the flag. 
//	 */
//	public PamSymbolType getSymbolfromFlag(int symbolflag){
//		
//		//TODO- a few more to add here. 
//		switch (symbolflag){
//		case PamSymbol.SYMBOL_CIRCLE:
//			return PamSymbolType.SYMBOL_CIRCLE; 
//		case PamSymbol.SYMBOL_CROSS:
//			return PamSymbolType.SYMBOL_CROSS; 		
//		case PamSymbol.SYMBOL_CROSS2:
//			return PamSymbolType.SYMBOL_CROSS2; 		
//		case PamSymbol.SYMBOL_CUSTOMPOLYGON:
//			return PamSymbolType.SYMBOL_CUSTOMPOLYGON; 		
//		case PamSymbol.SYMBOL_DIAMOND:
//			return PamSymbolType.SYMBOL_DIAMOND; 		
//		case PamSymbol.SYMBOL_DOUBLETRIANGLEL:
//			return PamSymbolType.SYMBOL_DOUBLETRIANGLEL; 		
//		case PamSymbol.SYMBOL_DOUBLETRIANGLER:
//			return PamSymbolType.SYMBOL_DOUBLETRIANGLER; 		
//		case PamSymbol.SYMBOL_HEXAGRAM:
//			return PamSymbolType.SYMBOL_HEXAGRAM; 		
//		case PamSymbol.SYMBOL_TRIANGLED:
//			return PamSymbolType.SYMBOL_TRIANGLED; 		
//		case PamSymbol.SYMBOL_TRIANGLEL:
//			return PamSymbolType.SYMBOL_TRIANGLEL; 		
//		case PamSymbol.SYMBOL_TRIANGLEU:
//			return PamSymbolType.SYMBOL_TRIANGLEU; 		
//		case PamSymbol.SYMBOL_STAR:
//			return PamSymbolType.SYMBOL_STAR; 
//		case PamSymbol.SYMBOL_SQUARE:
//			return PamSymbolType.SYMBOL_SQUARE; 		
//		}
//		
//		//default
//		return PamSymbolType.SYMBOL_CIRCLE; 
//	}

	/**
	 * Does the actual drawing work.
	 */
	private static void drawScaledPolygon(GraphicsContext g2d, Point2D pt, int np,
			double halfWidth, double halfHeight, double[] px, double[] py,
			boolean fill, Color fillColor, Color lineColor) {

		if (px == null || py == null) return;

		double[] xpoints = new double[px.length + 1];
		double[] ypoints = new double[py.length + 1];
		for (int i = 0; i < px.length; i++) {
			xpoints[i] = (int) (pt.getX() + px[i] * halfWidth);
			ypoints[i] = (int) (pt.getY() + py[i] * halfHeight);
		}
		xpoints[px.length] = xpoints[0];
		ypoints[px.length] = ypoints[0];

		if (fill) {
			g2d.setFill(fillColor);
			g2d.fillPolygon(xpoints, ypoints, xpoints.length);
		}
		g2d.setStroke(lineColor);
		g2d.strokePolygon(xpoints, ypoints, xpoints.length);

	}

	/**
	 * Create a canvas with a transparent background showing the symbol set by the symbol flag. This can be added various 
	 * JavaFX buttons and controls. 
	 * @param symbolStroke- line colour for the symbol
	 * @param symbolFill- fill colour for the symbol
	 * @param symbol- flag for symbol to draw
	 * @param h- height of the canvas
	 * @param w- width of the canvas. 
	 */
	public static Canvas createCanvas(PamSymbolType symbol, Color symbolStroke, Color symbolFill,  double h, double w){
		Canvas canvas=new Canvas(w,h);
		canvas.getGraphicsContext2D();
		canvas.getGraphicsContext2D().setFill(Color.TRANSPARENT);
		canvas.getGraphicsContext2D().fillRect(0, 0,w, h);
		draw(symbol, canvas.getGraphicsContext2D(),new Point2D(h/2.,w/2.),  w-2,
				h-2, true, 1f, symbolFill,
				symbolStroke);
		return canvas;
	}

	/**
	 * Get the flag for the type of symbol the pamsymbol is set to draw. 
	 * @return
	 */
	public PamSymbolType getSymbol() {
		return getSymbolData().symbol;
	}

	/**
	 * Set the flag for the type of symbol the pamsymbol is set to draw
	 * @param symbol
	 */
	public void setSymbol(PamSymbolType symbol) {
		getSymbolData().symbol = symbol;
	}

	/**
	 * Called to set a square drawn polygon when 
	 * drawing shapes which didn't actually use the polygon draw function. 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	private static void setSquareDrawnPolygon(double x1, double y1, double x2, double y2) {
		//TODO
		//		double[] xp = {x1, x1, x2, x2};
		//		double[] yp = {y1, y2, y2, y1};
		//	
		//		drawnPolygon = new Polygon(xp, yp, 4);
	}	



	private transient java.awt.Color lastAWTFillColor = null;
	private transient Color lastFXFillColor = null;
	public Color getFillColor() {
		if (getSymbolData().getFillColor() == lastAWTFillColor) {
			return lastFXFillColor;
		}
		else {
			return (lastFXFillColor = PamUtilsFX.awtToFXColor(getSymbolData().getFillColor()));
		}
	}

	public void setFillColor(Color fillColor) {
		getSymbolData().setFillColor(PamUtilsFX.fxToAWTColor(fillColor));
	}

	private transient java.awt.Color lastAWTLineColor = null;
	private transient Color lastFXLineColor = null;
	public Color getLineColor() {
		if (getSymbolData().getLineColor() == lastAWTLineColor) {
			return lastFXLineColor;
		}
		else {
			return (lastFXLineColor = PamUtilsFX.awtToFXColor(getSymbolData().getLineColor()));
		}
	}

	public void setLineColor(Color lineColor) {
		getSymbolData().setLineColor(PamUtilsFX.fxToAWTColor(lineColor));
	}

	/**
	 * Get the symbol width as a double
	 * @return symbol width in pixels
	 */
	public double getWidth() {
		return getSymbolData().width;
	}

	/**
	 * Get the symbol height as an double
	 * @return symbol height in pixels
	 */
	public double getHeight() {
		return getSymbolData().height;
	}

	@Override
	public PamSymbolFX clone() {
		// TODO Auto-generated method stub
		try {
			PamSymbolFX newSymbol = (PamSymbolFX) super.clone();
			if (newSymbol.getSymbolData() == null) {
				newSymbol.setSymbolData(new SymbolData());
			}
			return newSymbol;
		} catch (CloneNotSupportedException Ex) {
			Ex.printStackTrace();
		}
		return null;
	}

	/**
	 * Get a node version of the icon
	 * @param iconSize - the icon size in pixels. 
	 * @return a node containing the icon. 
	 */
	public Node getNode(int iconSize) {
		Canvas canvas=new Canvas();
		draw(canvas.getGraphicsContext2D(), new Point2D(iconSize/2,iconSize/2));
		return canvas;
	}

}
