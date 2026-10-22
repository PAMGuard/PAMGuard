package PamView.symbol.legend;

import java.awt.Color;

import PamView.PamSymbolType;
import PamView.symbol.SymbolData;

/**
 * A single entry in a {@link SymbolLegend}. This is a standard {@link SymbolData}
 * with a name attached, e.g. a click type symbol and the name of the click type.
 * <p>
 * Subclasses add the information needed to draw entries which represent more
 * than one colour, e.g. {@link ColourBarSymbolData} for symbols coloured on a
 * continuous scale and {@link ColourSetSymbolData} for symbols coloured from a
 * set of rotating colours.
 *
 * @author Jamie Macaulay
 */
public class LegendSymbolData extends SymbolData {

	private static final long serialVersionUID = 1L;

	/**
	 * The name of the entry.
	 */
	private String name;

	/**
	 * Create a legend entry from existing symbol data.
	 * @param symbolData - the symbol data. Values are copied, not referenced.
	 * @param name - the name of the entry.
	 */
	public LegendSymbolData(SymbolData symbolData, String name) {
		this(symbolData.symbol, symbolData.width, symbolData.height, symbolData.lineThickness,
				symbolData.fill, symbolData.getFillColor(), symbolData.getLineColor(), name);
	}

	/**
	 * Create a legend entry.
	 * @param symbol - the symbol shape
	 * @param width - the symbol width
	 * @param height - the symbol height
	 * @param lineThickness - the line thickness
	 * @param fill - true to fill the symbol
	 * @param fillColor - the fill colour
	 * @param lineColor - the line colour
	 * @param name - the name of the entry.
	 */
	public LegendSymbolData(PamSymbolType symbol, float width, float height, float lineThickness, boolean fill,
			Color fillColor, Color lineColor, String name) {
		super(symbol, width, height, lineThickness, fill, fillColor, lineColor);
		this.name = name;
	}

	/**
	 * Get the name of the entry.
	 * @return the name of the entry.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name of the entry.
	 * @param name - the name of the entry.
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public LegendSymbolData clone() {
		return (LegendSymbolData) super.clone();
	}

}
