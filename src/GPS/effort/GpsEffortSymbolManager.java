package GPS.effort;

import java.awt.Color;

import PamView.PamColors;
import PamView.PamSymbolType;
import PamView.PamColors.PamColor;
import PamView.symbol.StandardSymbolManager;
import PamView.symbol.SymbolData;
import PamView.symbol.SymbolOnlyManager;
import PamguardMVC.PamDataBlock;

public class GpsEffortSymbolManager extends SymbolOnlyManager {
	
	private static Color defColour = PamColors.getInstance().getColor(PamColor.GPSTRACK);
	private static SymbolData defaultSymbol = new SymbolData(PamSymbolType.SYMBOL_LINESEGMENT, 10, 10, false, defColour, defColour);

	public GpsEffortSymbolManager(PamDataBlock pamDataBlock) {
		super(pamDataBlock, defaultSymbol);
	}

}
