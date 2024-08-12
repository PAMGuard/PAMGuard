package PamView.symbol;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.Serializable;

import PamView.PamSymbolType;

/**
 * Class to hold symbol data for both FX and Swing types 
 * of PAMSymbols. 
 * @author Doug Gillespie
 *
 */
public class SymbolData implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;
	
	/*
	 * Parameters used to describe a shape - all other are statics.
	 */
	public PamSymbolType symbol = PamSymbolType.SYMBOL_CIRCLE;


	public float width = 5;

	public float height = 5;

	public float lineThickness = 2;

	public boolean fill = true;

	private Color fillColor;

	private Color lineColor;
	
	/**
	 * Constructor for a simple empty data (black circle) 
	 */
	public SymbolData() {
		fillColor = Color.BLACK;
		lineColor = Color.BLACK;
	}
	
	/**
	 * Construct a standard symbol data object. 
	 * @param symbol
	 * @param width
	 * @param height
	 * @param fill
	 * @param fillColorRGB
	 * @param lineColorRGB
	 */
	public SymbolData(PamSymbolType symbol, float width, float height, boolean fill,
			Color fillColor, Color lineColor) {
		super();
		this.symbol = symbol;
		this.width = width;
		this.height = height;
		this.fill = fill;
		this.fillColor = fillColor;
		this.lineColor = lineColor;
	}
	
	/**
	 * Construct a standard symbol data object. 
	 * @param symbol
	 * @param width
	 * @param height
	 * @param lineThickness
	 * @param fill
	 * @param fillColorRGB
	 * @param lineColorRGB
	 */
	public SymbolData(PamSymbolType symbol, float width, float height, float lineThickness, boolean fill,
			Color fillColor, Color lineColor) {
		super();
		this.symbol = symbol;
		this.width = width;
		this.height = height;
		this.lineThickness = lineThickness;
		this.fill = fill;
		this.fillColor = fillColor;
		this.lineColor = lineColor;
	}

//	public int getFillRed() {
//		return (fillColorRGB>>16)&0xFF;
//	}
//	public int getFillGreen() {
//		return (fillColorRGB>>8)&0xFF;
//	}
//	public int getFillBlue() {
//		return (fillColorRGB)&0xFF;
//	}
//	public int getLineRed() {
//		return (lineColorRGB>>16)&0xFF;
//	}
//	public int getLineGreen() {
//		return (lineColorRGB>>8)&0xFF;
//	}
//	public int getLineBlue() {
//		return (lineColorRGB)&0xFF;
//	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public SymbolData clone() {
		try {
			return (SymbolData) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @return the fillColor
	 */
	public Color getFillColor() {
		if (fillColor == null) {
			fillColor = Color.BLACK;
		}
		return fillColor;
	}

	/**
	 * @param fillColor the fillColor to set
	 */
	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
	}

	/**
	 * @return the lineColor
	 */
	public Color getLineColor() {
		if (lineColor == null) {
			lineColor = Color.BLACK;
		}
		return lineColor;
	}

	/**
	 * @param lineColor the lineColor to set
	 */
	public void setLineColor(Color lineColor) {
		this.lineColor = lineColor;
	}
	
	/**
	 * Get the type of symbol
	 * @return the type of symbol
	 */
	public PamSymbolType getSymbol() {
		return symbol;
	}

	/**
	 * Set the symbol type
	 * @param symbol - the symbol type
	 */
	public void setSymbol(PamSymbolType symbol) {
		this.symbol = symbol;
	}

	/**
	 * Set the line properties for the given graphics handle. 
	 * @param g
	 */
	public void setGraphicsProperties(Graphics g) {
		if (lineColor != null) {
			g.setColor(lineColor);
		}
		if (lineThickness != 1) {
			((Graphics2D) g).setStroke(new BasicStroke(lineThickness));
		}
	}

}
