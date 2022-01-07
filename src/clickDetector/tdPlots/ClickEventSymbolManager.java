package clickDetector.tdPlots;

import PamView.GeneralProjector;
import PamView.symbol.StandardSymbolChooser;
import PamView.symbol.StandardSymbolManager;
import PamView.symbol.SymbolData;
import PamView.symbol.modifier.SuperDetSymbolModifier;
import PamguardMVC.PamDataBlock;
import PamguardMVC.superdet.SuperDetDataBlock;
import PamguardMVC.superdet.swing.SuperDetectionSymbolManager;
import clickDetector.offlineFuncs.OfflineEventDataBlock;

public class ClickEventSymbolManager extends SuperDetectionSymbolManager {

	public ClickEventSymbolManager(OfflineEventDataBlock pamDataBlock) {
		super(pamDataBlock, new SymbolData());
		super.setSpecialColourName("Event Colour");
	}
//
//	/* (non-Javadoc)
//	 * @see PamView.symbol.StandardSymbolManager#createSymbolChooser(java.lang.String, PamView.GeneralProjector)
//	 */
//	@Override
//	protected StandardSymbolChooser createSymbolChooser(String displayName, GeneralProjector projector) {
//		ClickEventSymbolChooser ces = new ClickEventSymbolChooser(this, getPamDataBlock(), displayName, getDefaultSymbol(), projector);
//	
//		return ces;
//	}

}
