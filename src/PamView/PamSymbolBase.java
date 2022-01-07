package PamView;

import java.io.Serializable;

import PamView.symbol.SymbolData;

/**
 * Base class for both FX and Swing PamSymbols
 * @author dg50
 *
 */
public abstract class PamSymbolBase implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;

	/*
	 * drawing dimensions for the various shapes.
	 */
	//	static private final double[] sqx = { -.5, .5, .5, -.5 };
	//
	//	static private final double[] sqy = { -.5, -.5, .5, .5 };

	static protected final double[] sqx = { -1, 1, 1, -1 };

	static protected final double[] sqy = { -1, -1, 1, 1 };

	//	static private final double[] diax = { 0, 0.5, 0, -0.5 };
	//
	//	static private final double[] diay = { -.5, 0, .5, 0 };

	static protected final double[] diax = { 0, 1, 0, -1 };

	static protected final double[] diay = { -1, 0, 1, 0 };

	//	static private final double[] trux = { 0, 0.866, -0.866 };
	//
	//	static private final double[] truy = { -0.866, 0.866, 0.866 };
	static protected final double[] trux = { 0, 1, -1 };

	static protected final double[] truy = { -0.5, 0.5, 0.5 };

	//	static private final double[] trdx = { 0, 0.866, -0.866 };
	//
	//	static private final double[] trdy = { 0.866, -0.866, -0.866 };
	static protected final double[] trdx = { 0, 1, -1 };

	static protected final double[] trdy = { 0.5, -0.5, -0.5 };

	//	static private final double[] trrx = { 1, -0.5, -0.5 };
	//
	//	static private final double[] trry = { 0, 0.866, -0.866 };
	static protected final double[] trrx = { 0.5, -0.5, -0.5 };

	static protected final double[] trry = { 0, 1, -1 };

	//	static private final double[] trlx = { -1, 0.5, 0.5 };
	//
	//	static private final double[] trly = { 0, 0.866, -0.866 };

	static protected final double[] trlx = { -.5, .5, .5 };

	static protected final double[] trly = { 0, 1, -1 };

	static protected final double[] pentx = { 0, 0.951, 0.588, -0.588, -0.951 };

	static protected final double[] penty = { -1, -0.309, 0.809, 0.809, -0.309 };

	static protected final double[] hexx = { 1, .5, -.5, -1, -.5, .5 };

	static protected final double[] hexy = { 0, 0.866, 0.866, 0, -0.866, -0.866 };
	
	/**
	 * fx
	 * 
	static private final double[] sqx = { -1, 1, 1, -1 };

	static private final double[] sqy = { -1, -1, 1, 1 };

	static private final double[] diax = { 0, 1, 0, -1 };

	static private final double[] diay = { -1, 0, 1, 0 };

	static private final double[] trux = { 0, 1, -1 };

	static private final double[] truy = { -0.5, 0.5, 0.5 };

	static private final double[] trdx = { 0, 1, -1 };

	static private final double[] trdy = { 0.5, -0.5, -0.5 };

	static private final double[] trrx = { 0.5, -0.5, -0.5 };

	static private final double[] trry = { 0, 1, -1 };


	static private final double[] trlx = { -.5, .5, .5 };

	static private final double[] trly = { 0, 1, -1 };

	static private final double[] pentx = { 0, 0.951, 0.588, -0.588, -0.951 };

	static private final double[] penty = { -1, -0.309, 0.809, 0.809, -0.309 };

	static private final double[] hexx = { 1, .5, -.5, -1, -.5, .5 };

	static private final double[] hexy = { 0, 0.866, 0.866, 0, -0.866, -0.866 };

	 */

	// static private boolean first = true;
	private SymbolData symbolData;
	
	/**
	 * Construct a symbol with shape information. 
	 * @param symbolData
	 */
	public PamSymbolBase(SymbolData symbolData) {
		super();
		this.symbolData = symbolData;
		if (this.symbolData == null) {
			this.symbolData = new SymbolData();
		}
	}


	/**
	 * Convert a single character text code into 
	 * a symbol type more or less following the Matlab symbol 
	 * definitions. 
	 * @param textCode text code
	 * @return symbol type
	 */
	static public PamSymbolType interpretTextCode(String textCode) {
		if (textCode == null) {
			return null;
		}
		if (textCode.equalsIgnoreCase("x")) {
			return PamSymbolType.SYMBOL_CROSS;
		} else if (textCode.equalsIgnoreCase("+")) {
			return PamSymbolType.SYMBOL_CROSS2;
		} else if (textCode.equalsIgnoreCase("s")) {
			return PamSymbolType.SYMBOL_SQUARE;
		} else if (textCode.equalsIgnoreCase("^")) {
			return PamSymbolType.SYMBOL_TRIANGLEU;
		} else if (textCode.equalsIgnoreCase("o")) {
			return PamSymbolType.SYMBOL_CIRCLE;
		} else if (textCode.equalsIgnoreCase("d")) {
			return PamSymbolType.SYMBOL_DIAMOND;
		} else if (textCode.equalsIgnoreCase(".")) {
			return PamSymbolType.SYMBOL_POINT;
		} else if (textCode.equalsIgnoreCase("*")) {
			return PamSymbolType.SYMBOL_STAR;
		} else if (textCode.equalsIgnoreCase("v")) {
			return PamSymbolType.SYMBOL_TRIANGLED;
		} else if (textCode.equalsIgnoreCase("<")) {
			return PamSymbolType.SYMBOL_TRIANGLEL;
		} else if (textCode.equalsIgnoreCase(">")) {
			return PamSymbolType.SYMBOL_TRIANGLER;
		} else if (textCode.equalsIgnoreCase("p")) {
			return PamSymbolType.SYMBOL_PENTAGRAM;
		} else if (textCode.equalsIgnoreCase("h")) {
			return PamSymbolType.SYMBOL_HEXAGRAM;
		} else if (textCode.equalsIgnoreCase("Cross")) {
			return PamSymbolType.SYMBOL_CROSS;
		} else if (textCode.equalsIgnoreCase("Cross2")) {
			return PamSymbolType.SYMBOL_CROSS2;
		} else if (textCode.equalsIgnoreCase("Square")) {
			return PamSymbolType.SYMBOL_SQUARE;
		} else if (textCode.equalsIgnoreCase("Traingle")) {
			return PamSymbolType.SYMBOL_TRIANGLEU;
		} else if (textCode.equalsIgnoreCase("Circle")) {
			return PamSymbolType.SYMBOL_CIRCLE;
		} else if (textCode.equalsIgnoreCase("Diamond")) {
			return PamSymbolType.SYMBOL_DIAMOND;
		} else
			return null;
	}
	

	/**
	 * 
	 * @return the text code for this symbol
	 */
	public char getTextCode() {
		return getTextCode(getSymbolData().symbol);
	}

	/**
	 * Get the symbol data - all the information needed to describe the symbol, shape, colour, etc. 
	 * @return
	 */
	public SymbolData getSymbolData() {
		if (symbolData == null) {
			symbolData = new SymbolData();
		}
		return symbolData;
	}
	
	/**
	 * Set the symbol data. 
	 * @param symbolData
	 */
	public void setSymbolData(SymbolData symbolData) {
		this.symbolData = symbolData;
	}
	/**
	 * Get a text code for a symbol
	 * @param symbol symbol type
	 * @return text code
	 */
	static public char getTextCode(PamSymbolType symbol) {
		switch(symbol) {
		case SYMBOL_CROSS:
			return '+';
		case SYMBOL_CROSS2:
			return 'x';
		case SYMBOL_SQUARE:
			return 's';
		case SYMBOL_TRIANGLEU:
			return '^';
		case SYMBOL_CIRCLE:
			return 'o';
		case SYMBOL_DIAMOND:
			return 'd';
		case SYMBOL_POINT:
			return '.';
		case SYMBOL_STAR:
			return '*';
		case SYMBOL_TRIANGLED:
			return 'v';
		case SYMBOL_TRIANGLEL:
			return '<';
		case SYMBOL_TRIANGLER:
			return '>';
		case SYMBOL_PENTAGRAM:
			return 'p';
		case SYMBOL_HEXAGRAM:
			return 'h';
		}
		return 0;
	}

	/**
	 * Set the symbol width
	 * @param d symbol width in pixels. 
	 */
	public void setWidth(double width) {
		getSymbolData().width = (float) width;
	}

	/**
	 * Set the symbol height
	 * @param height symbol height in pixels
	 */
	public void setHeight(double height) {
		getSymbolData().height = (float) height;
	}
	
	/**
	 * Get the symbol width as a double
	 * @return symbol width in pixels
	 * @see getWidth() Symbol width as an int
	 */
	public double getDWidth() {
		return getSymbolData().width;
	}
	
	/**
	 * Get the symbol height as an double
	 * @return symbol height in pixels
	 * @see getWidth() Symbol height as an int
	 */
	public double getDHeight() {
		return getSymbolData().height;
	}

	/**
	 * Is the symbol filled
	 * @return true if filled
	 */
	public boolean isFill() {
		return getSymbolData().fill;
	}


	/**
	 * Set the symbol fill
	 * @param fill
	 */
	public void setFill(boolean fill) {
		getSymbolData().fill = fill;
	}


	/**
	 * Get the symbol line thickness
	 * @return line thickness
	 */
	public float getLineThickness() {
		return getSymbolData().lineThickness;
	}

	/**
	 * Set the symbol line thickness
	 * @param lineThickness line thickness
	 */
	public void setLineThickness(float lineThickness) {
		getSymbolData().lineThickness = lineThickness;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public PamSymbolBase clone() throws CloneNotSupportedException {
		return (PamSymbolBase) super.clone();
	}
}
