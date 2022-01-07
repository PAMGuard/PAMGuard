package whistlesAndMoans.plots;

import PamView.GeneralProjector;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.StandardSymbolChooser;
import PamView.symbol.StandardSymbolManager;
import PamView.symbol.SymbolData;
import PamView.symbol.modifier.RotateColoursModifier;
import PamView.symbol.modifier.SymbolModType;
import PamguardMVC.PamDataBlock;

public class WhistleSymbolManager extends StandardSymbolManager {

	public WhistleSymbolManager(PamDataBlock pamDataBlock, SymbolData defaultSymbol, boolean hasChannelOption) {
		super(pamDataBlock, defaultSymbol, hasChannelOption);
		
//		addSymbolOption(HAS_SYMBOL);
//		addSymbolOption(HAS_CHANNEL_OPTIONS);
//		addSymbolOption(HAS_SPECIAL_COLOUR);
		addSymbolOption(HAS_LINE_AND_LENGTH);
		this.setSpecialColourName("randomly");
	}

	/* (non-Javadoc)
	 * @see PamView.symbol.StandardSymbolManager#createSymbolChooser(java.lang.String, PamView.GeneralProjector)
	 */
	@Override
	protected StandardSymbolChooser createSymbolChooser(String displayName, GeneralProjector projector) {
		SymbolData symbolData = getDefaultSymbol().clone(); //ensure a different symbol object
		WhistleSymbolChooser wsc =  new WhistleSymbolChooser(this, getPamDataBlock(), displayName, symbolData, projector);
		return wsc;
	}
	
	public PamSymbolChooser getSymbolChooser(String displayName, GeneralProjector projector) {
		
		StandardSymbolChooser psc = (StandardSymbolChooser) super.getSymbolChooser(displayName, projector);
	
		/**
		 * There are occasions when (for some unknown reason - perhaps a bug in previous version) the loaded settings have symbolData
		 * which is the same object for different Whistle SymbolManagers. This results in all sorts of weird colour things happening 
		 * on displays. This ensures that the symbolData is a different object for each symbol manager. 
		 */
		psc.getSymbolOptions().symbolData = psc.getSymbolOptions().symbolData.clone();
		
//		addSymbolModifiers(psc);

		return psc;
	}

	@Override
	public void addSymbolModifiers(PamSymbolChooser psc) {
		super.addSymbolModifiers(psc);
		psc.addSymbolModifier(new RotateColoursModifier("Colour randomly", psc, SymbolModType.FILLCOLOUR | SymbolModType.LINECOLOUR));
	}

}
