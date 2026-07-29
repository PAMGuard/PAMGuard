package GPS.symbol;

import java.awt.Color;

import PamView.GeneralProjector;
import PamView.PamColors;
import PamView.PamSymbolType;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.StandardSymbolManager;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataBlock;

public class GPSSymbolManager extends StandardSymbolManager {

	static final Color gpsCol = PamColors.getInstance().getGPSColor();
	static final SymbolData defaultSymbol = new SymbolData(PamSymbolType.SYMBOL_NONE, 1, 1, false, gpsCol, gpsCol);
	
	public GPSSymbolManager(PamDataBlock pamDataBlock) {
		super(pamDataBlock, defaultSymbol);
		setLineOnly(true);
	}


	@Override
	public void addSymbolModifiers(PamSymbolChooser psc) {
		// do nothing. We don't want any modifiers. 
//		// TODO Auto-generated method stub
//		super.addSymbolModifiers(psc);
	}

}
