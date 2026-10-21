package PamView.symbol.modifier;

import java.awt.Color;

import PamUtils.PamUtils;
import PamView.GeneralProjector;
import PamView.PamColors;
import PamView.PamSymbolType;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.SymbolData;
import PamView.symbol.legend.LegendSymbolData;
import PamView.symbol.legend.SymbolLegend;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import pamViewFX.symbol.StandardSymbolModifierPane;
import pamViewFX.symbol.SymbolModifierPane;

public class HydrophoneSymbolModifier extends SymbolModifier {

	private SymbolData symbolData = new SymbolData(PamSymbolType.SYMBOL_CIRCLE, 5, 5, true, Color.BLACK, Color.BLACK);
	
	
	public HydrophoneSymbolModifier(PamSymbolChooser symbolChooser) {
		super("Hydrophone", symbolChooser, SymbolModType.FILLCOLOUR | SymbolModType.LINECOLOUR);
		setToolTipText("Colour by the number of the first used channel");
	}

	@Override
	public SymbolData getSymbolData(GeneralProjector projector, PamDataUnit dataUnit) {
		int firstPhone = PamUtils.getLowestChannel(dataUnit.getChannelBitmap());
		if (firstPhone < 0) {
			return null;
		}
		Color col = PamColors.getInstance().getChannelColor(firstPhone);
		symbolData.setFillColor(col);
		symbolData.setLineColor(col);
		return symbolData;
	}
	


	@Override
	public SymbolLegend getSymbolLegend() {
		SymbolLegend legend = new SymbolLegend(getName());
		PamDataBlock dataBlock = getSymbolChooser() == null ? null : getSymbolChooser().getPamDataBlock();
		if (dataBlock == null) {
			return legend;
		}
		int chanMap = dataBlock.getChannelMap();
		for (int i = 0; i < 32; i++) {
			if ((chanMap & 1<<i) == 0) {
				continue;
			}
			Color col = PamColors.getInstance().getChannelColor(i);
			legend.addSymbol(new LegendSymbolData(symbolData.symbol, symbolData.width, symbolData.height,
					symbolData.lineThickness, true, col, col, "Channel " + i));
		}
		return legend;
	}

	@Override
	public boolean isModifyAll() {
		return true;
	}

}
