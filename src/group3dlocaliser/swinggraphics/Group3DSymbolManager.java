package group3dlocaliser.swinggraphics;

import PamView.symbol.StandardSymbolManager;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataBlock;
import group3dlocaliser.Group3DLocaliserControl;

public class Group3DSymbolManager extends StandardSymbolManager {

	private Group3DLocaliserControl group3DControl;
	
	public Group3DSymbolManager(Group3DLocaliserControl group3DControl, PamDataBlock pamDataBlock, SymbolData defaultSymbol) {
		super(pamDataBlock, defaultSymbol);
		this.group3DControl = group3DControl;
	}


}
