package PamView.symbol.legend;

import java.util.ArrayList;
import java.util.List;

import PamView.symbol.modifier.SymbolModType;

/**
 * The legend for a single symbol modifier, e.g. the click types for a click
 * classification modifier or a colour bar for a peak frequency modifier.
 * <p>
 * A legend has a title, a bitmap of which symbol properties (fill colour, line
 * colour, shape) it actually sets on the display and a list of entries. A legend
 * can have no entries, in which case a display will just show the title, i.e.
 * that symbols are coloured / shaped by whatever the title says.
 * <p>
 * Displays get an ordered list of legends from
 * {@link PamView.symbol.PamSymbolManager#getSymbolLegends(String, PamView.GeneralProjector)}
 * and turn them into a graphical component using either the Swing or the FX
 * symbol legend factory.
 *
 * @author Jamie Macaulay
 */
public class SymbolLegend {

	/**
	 * The title of the legend - usually the name of the symbol modifier.
	 */
	private String title;

	/**
	 * Bitmap of the symbol properties this legend sets. See {@link SymbolModType}
	 */
	private int modBits;

	/**
	 * The entries in the legend.
	 */
	private List<LegendSymbolData> symbols;

	/**
	 * Create a legend with no entries.
	 * @param title - the legend title.
	 */
	public SymbolLegend(String title) {
		this(title, (List<LegendSymbolData>) null);
	}

	/**
	 * Create a legend.
	 * @param title - the legend title.
	 * @param symbols - the legend entries. Can be null.
	 */
	public SymbolLegend(String title, List<LegendSymbolData> symbols) {
		this.title = title;
		this.symbols = symbols == null ? new ArrayList<>() : symbols;
	}

	/**
	 * Create a legend with a single entry.
	 * @param title - the legend title.
	 * @param symbol - the legend entry.
	 */
	public SymbolLegend(String title, LegendSymbolData symbol) {
		this(title);
		addSymbol(symbol);
	}

	/**
	 * Add an entry to the legend.
	 * @param symbol - the entry to add.
	 */
	public void addSymbol(LegendSymbolData symbol) {
		if (symbol != null) {
			symbols.add(symbol);
		}
	}

	/**
	 * @return the legend title.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title - the legend title.
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Get the bitmap of symbol properties which this legend sets on the display.
	 * This is set by the symbol chooser and accounts for properties which are
	 * overwritten by modifiers lower down the list.
	 * @return bitmap of {@link SymbolModType} flags.
	 */
	public int getModBits() {
		return modBits;
	}

	/**
	 * Set the bitmap of symbol properties which this legend sets on the display.
	 * @param modBits - bitmap of {@link SymbolModType} flags.
	 */
	public void setModBits(int modBits) {
		this.modBits = modBits;
	}

	/**
	 * @return true if the legend sets the fill colour.
	 */
	public boolean isFillColour() {
		return (modBits & SymbolModType.FILLCOLOUR) != 0;
	}

	/**
	 * @return true if the legend sets the line colour.
	 */
	public boolean isLineColour() {
		return (modBits & SymbolModType.LINECOLOUR) != 0;
	}

	/**
	 * @return true if the legend sets the symbol shape.
	 */
	public boolean isShape() {
		return (modBits & SymbolModType.SHAPE) != 0;
	}

	/**
	 * @return the legend entries. Never null.
	 */
	public List<LegendSymbolData> getSymbols() {
		return symbols;
	}

}
