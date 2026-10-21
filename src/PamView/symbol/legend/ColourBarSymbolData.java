package PamView.symbol.legend;

import PamView.symbol.SymbolData;
import pamViewFX.fxNodes.utilsFX.ColourArray;
import pamViewFX.fxNodes.utilsFX.ColourArray.ColourArrayType;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;

/**
 * A legend entry for symbols which are coloured on a continuous scale, e.g. by
 * peak frequency. As well as the symbol, the entry defines a colour bar and the
 * values at either end of it so that the legend can draw a labelled colour bar
 * next to the symbol.
 *
 * @author Jamie Macaulay
 */
public class ColourBarSymbolData extends LegendSymbolData {

	private static final long serialVersionUID = 1L;

	/**
	 * The colour map.
	 */
	private ColourArrayType colourArrayType;

	/**
	 * Value at the start of the colour bar.
	 */
	private double minValue;

	/**
	 * Value at the end of the colour bar.
	 */
	private double maxValue;

	/**
	 * Units for the colour bar labels, e.g. "kHz". Can be null.
	 */
	private String units;

	/**
	 * Cached colour array for looking up colours.
	 */
	private transient ColourArray colourArray;

	/**
	 * Create a colour bar legend entry. The symbol is drawn in the colour from
	 * the middle of the colour bar.
	 * @param symbolData - the symbol shape and size.
	 * @param name - the name of the entry, e.g. "Peak frequency"
	 * @param colourArrayType - the colour map
	 * @param minValue - the value at the start of the colour bar
	 * @param maxValue - the value at the end of the colour bar
	 * @param units - units for the labels, can be null.
	 */
	public ColourBarSymbolData(SymbolData symbolData, String name, ColourArrayType colourArrayType,
			double minValue, double maxValue, String units) {
		super(symbolData, name);
		this.colourArrayType = colourArrayType;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.units = units;
		java.awt.Color midCol = getColour(0.5);
		setFillColor(midCol);
		setLineColor(midCol);
	}

	/**
	 * Get the colour at a position along the colour bar.
	 * @param fraction - position from 0 (start) to 1 (end).
	 * @return the colour at that position.
	 */
	public java.awt.Color getColour(double fraction) {
		if (colourArray == null) {
			colourArray = getColourArray(128);
		}
		return PamUtilsFX.fxToAWTColor(colourArray.getColour(fraction));
	}

	/**
	 * Create a colour array for the colour map.
	 * @param nPoints - the number of colours in the array.
	 * @return the colour array.
	 */
	public ColourArray getColourArray(int nPoints) {
		return ColourArray.createStandardColourArray(nPoints, colourArrayType);
	}

	/**
	 * @return the colour map type.
	 */
	public ColourArrayType getColourArrayType() {
		return colourArrayType;
	}

	/**
	 * @return the value at the start of the colour bar.
	 */
	public double getMinValue() {
		return minValue;
	}

	/**
	 * @return the value at the end of the colour bar.
	 */
	public double getMaxValue() {
		return maxValue;
	}

	/**
	 * @return the units for the colour bar labels. Can be null.
	 */
	public String getUnits() {
		return units;
	}

	/**
	 * Format a value for a colour bar label using a sensible number of decimal
	 * places for the range of the colour bar.
	 * @param value - the value.
	 * @return formatted value.
	 */
	public String formatValue(double value) {
		double range = Math.abs(maxValue - minValue);
		if (range >= 10 || range == 0) {
			return String.format("%.0f", value);
		}
		else if (range >= 1) {
			return String.format("%.1f", value);
		}
		else {
			return String.format("%.3g", value);
		}
	}

	@Override
	public ColourBarSymbolData clone() {
		return (ColourBarSymbolData) super.clone();
	}

}
