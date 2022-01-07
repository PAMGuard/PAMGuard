package dataPlots.data;

import java.awt.Color;

import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

public class SimpleSymbolChooser implements TDSymbolChooser {
	
	private PamSymbol symbol = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 8, 8, true, Color.BLACK, Color.BLACK);
	
	private int drawTypes = DRAW_SYMBOLS;

	@Override
	public PamSymbol getPamSymbol(PamDataUnit dataUnit, int type) {
		return symbol;
	}

	@Override
	public int getDrawTypes() {
		return drawTypes ;
	}

	/**
	 * @param drawTypes the drawTypes to set, can be a combination of DRAW_LINES and DRAW_SYMBOLS
	 */
	public void setDrawTypes(int drawTypes) {
		this.drawTypes = drawTypes;
	}

}
