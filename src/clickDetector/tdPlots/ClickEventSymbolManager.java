package clickDetector.tdPlots;

import PamView.symbol.SymbolData;
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
