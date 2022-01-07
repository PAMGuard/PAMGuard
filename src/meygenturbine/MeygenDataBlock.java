package meygenturbine;

import java.awt.Color;

import PamView.PamSymbolType;
import PamView.symbol.StandardSymbolManager;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

public class MeygenDataBlock extends PamDataBlock<MeygenDataUnit> {

	public MeygenDataBlock(PamProcess parentProcess) {
		super(MeygenDataUnit.class, "Meygen Tide Data", parentProcess, 0);
		setPamSymbolManager(new StandardSymbolManager(this, new SymbolData(PamSymbolType.SYMBOL_CIRCLE, 1, 1, false, Color.LIGHT_GRAY, Color.GRAY)));
	}
	
	@Override
	public int getNumRequiredBeforeLoadTime() {
		return 1;
	}

}
