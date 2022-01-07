package PamView.symbol.modifier;

import PamView.GeneralProjector;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataUnit;


/**
 * Symbol modifier which cannot modify anything....
 * @author Doug Gillespie
 *
 */
public class FixedSymbolModifier extends SymbolModifier {

	public FixedSymbolModifier(PamSymbolChooser symbolChooser) {
		super("Fixed", symbolChooser, 0);
	}

	@Override
	public boolean canModify(int modType) {
		return false;
	}

	@Override
	public SymbolData modifySymbol(SymbolData symbolData, GeneralProjector projector, PamDataUnit dataUnit) {
		return symbolData;
	}

	@Override
	public SymbolData getSymbolData(GeneralProjector projector, PamDataUnit dataUnit) {
		return null;
	}

}
