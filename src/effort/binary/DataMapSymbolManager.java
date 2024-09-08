package effort.binary;

import PamView.symbol.PamSymbolChooser;
import PamView.symbol.StandardSymbolManager;
import PamView.symbol.SymbolData;
import PamView.symbol.SymbolOnlyManager;
import PamguardMVC.PamDataBlock;

public class DataMapSymbolManager extends SymbolOnlyManager {

	public static SymbolData defaultSymbol = new SymbolData();
	
	public DataMapSymbolManager(PamDataBlock pamDataBlock) {
		super(pamDataBlock, defaultSymbol);
	}


}
