package gpl.graphfx;

import java.awt.Color;

import PamView.PamSymbolType;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataUnit;
import dataPlotsFX.TDSymbolChooserFX;
import gpl.GPLStateDataUnit;
import pamViewFX.fxNodes.PamSymbolFX;

public class GPLStateSymbolChooser implements TDSymbolChooserFX {

	private SymbolData symbolData = new SymbolData(PamSymbolType.SYMBOL_CIRCLE, 4.f, 4.f, true, Color.BLACK, Color.BLACK);
	private PamSymbolFX fxSymbol = new PamSymbolFX(symbolData);
	
	@Override
	public int getDrawTypes(PamDataUnit pamDataUnit) {
		return TDSymbolChooserFX.DRAW_LINES;
	}

	@Override
	public PamSymbolFX getPamSymbol(PamDataUnit dataUnit, int type) {
		GPLStateDataUnit sdu = (GPLStateDataUnit) dataUnit;
		symbolData.setLineColor(sdu.getPeakState() > 1 ? Color.RED : Color.BLACK);
		symbolData.setFillColor(sdu.getPeakState() > 1 ? Color.RED : Color.BLACK);
		fxSymbol.setSymbolData(symbolData);
		return fxSymbol;
	}

}
