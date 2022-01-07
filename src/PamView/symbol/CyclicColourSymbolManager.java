package PamView.symbol;

import PamView.GeneralProjector;
import PamguardMVC.PamDataBlock;

public class CyclicColourSymbolManager extends PamSymbolManager<CyclicColourSymbolChooser> {

	private SymbolData baseSymbol;

	public CyclicColourSymbolManager(PamDataBlock pamDataBlock, SymbolData baseSymbol) {
		super(pamDataBlock);
		this.baseSymbol = baseSymbol;
	}

	@Override
	protected CyclicColourSymbolChooser createSymbolChooser(String displayName, GeneralProjector projector) {
		return new CyclicColourSymbolChooser(this, getPamDataBlock(), displayName, projector, baseSymbol);
	}

}
