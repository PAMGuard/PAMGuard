package PamView.symbol.modifier;

import java.awt.Color;

import PamUtils.PamUtils;
import PamView.GeneralProjector;
import PamView.PamColors;
import PamView.PamSymbolType;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.SymbolData;
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
	


}
