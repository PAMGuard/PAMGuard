package dataPlotsFX;

import PamView.PamSymbolType;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataUnit;
import pamViewFX.fxNodes.PamSymbolFX;

public class SimpleSymbolChooserFX implements TDSymbolChooserFX {
	
	private SymbolData symbolData = new SymbolData(PamSymbolType.SYMBOL_CIRCLE, 8, 8, true, java.awt.Color.BLACK, java.awt.Color.BLACK);
	
	private int drawTypes = DRAW_SYMBOLS;
	

//	private LineData line= new LineData(java.awt.Color.black);

	@Override
	public PamSymbolFX getPamSymbol(PamDataUnit dataUnit, int type) {
		return new PamSymbolFX(symbolData);
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