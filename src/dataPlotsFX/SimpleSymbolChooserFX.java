package dataPlotsFX;

import PamView.PamSymbolType;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataUnit;
import pamViewFX.fxNodes.PamSymbolFX;

public class SimpleSymbolChooserFX implements TDSymbolChooserFX {
	
	private SymbolData symbolData = new SymbolData(PamSymbolType.SYMBOL_CIRCLE, 8, 8, true, java.awt.Color.BLACK, java.awt.Color.BLACK);
	
	private int drawTypes = DRAW_SYMBOLS;
	

//	private LineData line= new LineData(java.awt.Color.black);

	@Override
	public PamSymbolFX getPamSymbol(PamDataUnit dataUnit, int type) {
		// black is invisible on the dark colour schemes - see PamSymbolChooser.
		return new PamSymbolFX(PamSymbolChooser.adaptToColourScheme(symbolData));
	}

	/**
	 * Get the symbol these data are drawn with, so that sub classes can change its
	 * shape or size. Change it here rather than through the symbol handed back by
	 * {@link #getPamSymbol(PamDataUnit, int)}, which may be a copy adapted to the
	 * colour scheme.
	 *
	 * @return the symbol data.
	 */
	public SymbolData getSymbolData() {
		return symbolData;
	}

	@Override
	public int getDrawTypes(PamDataUnit pamDataUnit) {
		return drawTypes ;
	}

	/**
	 * @param drawTypes the drawTypes to set, can be a combination of DRAW_LINES and DRAW_SYMBOLS
	 */
	public void setDrawTypes(int drawTypes) {
		this.drawTypes = drawTypes;
	}


}