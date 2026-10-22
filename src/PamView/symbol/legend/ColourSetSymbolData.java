package PamView.symbol.legend;

import java.awt.Color;

import PamView.PamColors;
import PamView.symbol.SymbolData;

/**
 * A legend entry for symbols which are coloured from a set of discrete colours
 * where the individual colours have no particular meaning, e.g. whistles or
 * events coloured randomly from the rotating whale colours. The legend draws the
 * symbol next to a swatch of the colours in the set.
 *
 * @author Jamie Macaulay
 */
public class ColourSetSymbolData extends LegendSymbolData {

	private static final long serialVersionUID = 1L;

	/**
	 * The colours in the set.
	 */
	private Color[] colours;

	/**
	 * Create a colour set legend entry. The symbol is drawn in the first colour of
	 * the set.
	 * @param symbolData - the symbol shape and size.
	 * @param name - the name of the entry.
	 * @param colours - the colours in the set.
	 */
	public ColourSetSymbolData(SymbolData symbolData, String name, Color[] colours) {
		super(symbolData, name);
		this.colours = colours;
		if (colours != null && colours.length > 0) {
			setFillColor(colours[0]);
			setLineColor(colours[0]);
		}
	}

	/**
	 * Create a colour set legend entry from the rotating whale colours of the
	 * current colour scheme.
	 * @param symbolData - the symbol shape and size.
	 * @param name - the name of the entry.
	 * @return colour set legend entry.
	 */
	public static ColourSetSymbolData createWhaleColourSet(SymbolData symbolData, String name) {
		PamColors pamColors = PamColors.getInstance();
		// colour 0 is not used when rotating through the colours.
		int n = Math.max(1, pamColors.getNWhaleColours() - 1);
		Color[] colours = new Color[n];
		for (int i = 0; i < n; i++) {
			colours[i] = pamColors.getWhaleColor(i + 1);
		}
		return new ColourSetSymbolData(symbolData, name, colours);
	}

	/**
	 * @return the colours in the set.
	 */
	public Color[] getColours() {
		return colours;
	}

	@Override
	public ColourSetSymbolData clone() {
		return (ColourSetSymbolData) super.clone();
	}

}
