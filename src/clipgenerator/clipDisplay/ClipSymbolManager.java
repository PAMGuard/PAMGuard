package clipgenerator.clipDisplay;

import PamView.GeneralProjector;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.StandardSymbolManager;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataBlock;

/**
 * Looking into making a symbol modifier that will work with the DIFAR ClipDecorations, but its
 * too complicated since the decorations are only added to the individual clips. Need some kind
 * of decorator class in the ClipDisplayPanel as well as the info in the ClipDisplayUnits for it to 
 * work. Probably very possible, but needs a fully functional DIFAR system to test, so not going to do now. 
 * @author dg50
 *
 */
public class ClipSymbolManager extends StandardSymbolManager {

	public ClipSymbolManager(PamDataBlock pamDataBlock, SymbolData defaultSymbol, boolean hasChannelOption) {
		super(pamDataBlock, defaultSymbol, hasChannelOption);
	}

	@Override
	public PamSymbolChooser getSymbolChooser(String displayName, GeneralProjector projector) {
		PamSymbolChooser symbolChooser = super.getSymbolChooser(displayName, projector);
//		checkSymbolModifiers(symbolChooser, projector);
		return symbolChooser;
	}

}
