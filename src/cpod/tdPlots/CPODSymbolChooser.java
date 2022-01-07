package cpod.tdPlots;

import java.awt.Color;

import cpod.CPODClick;
import PamView.ColourArray;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamguardMVC.PamDataUnit;
import dataPlots.data.TDSymbolChooser;

public class CPODSymbolChooser implements TDSymbolChooser {

	private PamSymbol symbol = new PamSymbol(PamSymbolType.SYMBOL_TRIANGLEU, 5, 5, true, Color.RED, Color.RED);
	private CPODPlotinfo cpodPlotinfo;
	private ColourArray colArray = ColourArray.createHotArray(256);
	public CPODSymbolChooser(CPODPlotinfo cpodPlotinfo) {
		this.cpodPlotinfo = cpodPlotinfo;
	}

	@Override
	public int getDrawTypes() {
		return 1;
	}

	@Override
	public PamSymbol getPamSymbol(PamDataUnit dataUnit, int type) {
		CPODClick click = (CPODClick) dataUnit;
		Color col = colArray.getColour(click.getkHz());
		symbol.setFillColor(col);
		symbol.setLineColor(col);
		symbol.setSymbol(click.getkHz()>=100 && click.getkHz()<=150 ? PamSymbolType.SYMBOL_TRIANGLEU : PamSymbolType.SYMBOL_CIRCLE);
		return symbol;
	}

}
