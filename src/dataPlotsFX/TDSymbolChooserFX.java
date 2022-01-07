package dataPlotsFX;

import pamViewFX.fxNodes.PamSymbolFX;
import PamguardMVC.PamDataUnit;
import javafx.scene.paint.Color;

/**
 * Interface allowing things getting drawn on the plots to chose their won
 * symbol
 * 
 * @author Doug Gillespie
 *
 */
public interface TDSymbolChooserFX {

	public static final int DRAW_SYMBOLS = 0x1;

	public static final int DRAW_LINES = 0x2;

	/**
	 * Flag to get a standard symbol;
	 */
	public static final int NORMAL_SYMBOL = 0x3;

	/**
	 * Flag to get a highlighted symbol. Generally this is for data units which have
	 * been individually selected e.g. by a mouse or finger tap
	 */
	public static final int HIGHLIGHT_SYMBOL = 0x4;

	/**
	 * Flag for a highlighted symbol. Generally this particular highlight is used if
	 * a data unit has been sleected as a part of a group of data units, e.g. by a
	 * zoomer. In ths case applying individual highlights may clutter the display
	 * and so a more light weight highlight or no highlight at all may be
	 * appropriate,
	 */
	public static final int HIGHLIGHT_SYMBOL_MARKED = 0x5;

	/**
	 * Get the draw type for a particular data unit. This allows the data units to
	 * be drawn in multiple ways depending on their properties. For example click
	 * trains can have lines between data units whislt unclassified clicks have
	 * nothing
	 * 
	 * @param dataUnit
	 *            - the data unit
	 * @return
	 */
	public int getDrawTypes(PamDataUnit dataUnit);

	public PamSymbolFX getPamSymbol(PamDataUnit dataUnit, int type);

	// public Color getLineColor(PamDataUnit pamDataUnit, int type);

}
