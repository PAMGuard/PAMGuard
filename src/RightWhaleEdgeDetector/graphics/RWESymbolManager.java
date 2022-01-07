package RightWhaleEdgeDetector.graphics;

import PamView.symbol.PamSymbolChooser;
import PamView.symbol.StandardSymbolManager;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataBlock;

public class RWESymbolManager extends StandardSymbolManager {


	public RWESymbolManager(PamDataBlock pamDataBlock, SymbolData defaultSymbol, boolean hasChannelOption) {
		super(pamDataBlock, defaultSymbol, hasChannelOption);
	}

	@Override
	public void addSymbolModifiers(PamSymbolChooser psc) {
		super.addSymbolModifiers(psc);
		psc.addSymbolModifier(new RWESymbolModifier(psc));
	}


}
