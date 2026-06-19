package rawDeepLearningClassifier.layoutFX;

import PamDetection.LocContents;
import PamDetection.LocalisationInfo;
import PamView.GeneralProjector;
import PamView.symbol.StandardSymbolChooser;
import PamView.symbol.StandardSymbolManager;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataBlock;
import rawDeepLearningClassifier.DLControl;

public class DLSymbolManager extends StandardSymbolManager {

	/**
	 * Reference to the click control. 
	 */
	private DLControl dlControl;


	public DLSymbolManager(DLControl dlControl, PamDataBlock pamDataBlock) {
		super(pamDataBlock, new SymbolData());
		this.dlControl = dlControl;
		addSymbolOption(HAS_CHANNEL_OPTIONS);
		addSymbolOption(HAS_SPECIAL_COLOUR);
		addSymbolOption(HAS_SYMBOL);
		super.setSpecialColourName("by probability");
	}
	

	@Override
	protected StandardSymbolChooser createSymbolChooser(String displayName, GeneralProjector projector) {
		// if there is a localiser, then need to add appropriate additional symbol options
		PamDataBlock db = getPamDataBlock();
		LocalisationInfo locCont = db.getLocalisationContents();
		boolean hasBearing = locCont.hasLocContent(LocContents.HAS_BEARING);
		if (hasBearing) {
			addSymbolOption(HAS_LINE_LENGTH);
		}
		return new StandardSymbolChooser(this, getPamDataBlock(), displayName, getDefaultSymbol(), projector);
	}
	

	
}
