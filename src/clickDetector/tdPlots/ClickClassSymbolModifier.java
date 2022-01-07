package clickDetector.tdPlots;

import PamView.GeneralProjector;
import PamView.PamSymbol;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.SymbolData;
import PamView.symbol.modifier.SymbolModType;
import PamView.symbol.modifier.SymbolModifier;
import PamguardMVC.PamDataUnit;
import clickDetector.ClickControl;
import clickDetector.ClickDetection;

public class ClickClassSymbolModifier extends SymbolModifier {

	private ClickControl clickControl;

	public ClickClassSymbolModifier(ClickControl clickControl, PamSymbolChooser symbolChooser) {
		super("Click Classification", symbolChooser, SymbolModType.EVERYTHING);
		this.clickControl = clickControl;
		getSymbolModifierParams().modBitMap = SymbolModType.SHAPE;
//		setModifyableBits(SymbolModType.SHAPE);
	}

	@Override
	public SymbolData getSymbolData(GeneralProjector projector, PamDataUnit dataUnit) {
		PamSymbol symbol=clickControl.getClickIdentifier().getSymbol((ClickDetection) dataUnit);
		if (symbol != null) {
			return symbol.getSymbolData();
		}
		else {
			return null;
		}
	}

}
