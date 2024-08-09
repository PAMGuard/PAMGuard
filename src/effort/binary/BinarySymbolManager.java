package effort.binary;

import PamView.symbol.StandardSymbolManager;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataBlock;

public class BinarySymbolManager extends StandardSymbolManager {

	public static SymbolData defaultSymbol = new SymbolData();
	
	public BinarySymbolManager(PamDataBlock pamDataBlock) {
		super(pamDataBlock, defaultSymbol);
	}

}
