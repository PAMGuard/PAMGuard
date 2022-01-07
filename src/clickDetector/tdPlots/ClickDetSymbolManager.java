package clickDetector.tdPlots;

import PamView.GeneralProjector;
import PamView.PamSymbol;
import PamView.PamSymbolDialog;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.StandardSymbolChooser;
import PamView.symbol.StandardSymbolManager;
import PamView.symbol.StandardSymbolOptions;
import PamView.symbol.SymbolData;
import PamView.symbol.modifier.PeakFreqModifier;
import PamView.symbol.modifier.SuperDetSymbolModifier;
import PamView.symbol.modifier.SymbolModType;
import PamView.symbol.modifier.SymbolModifier;
import PamguardMVC.PamDataBlock;
import clickDetector.ClickControl;
import pamViewFX.fxNodes.utilsFX.ColourArray;

public class ClickDetSymbolManager extends StandardSymbolManager {

	/**
	 * Reference ot the click control. 
	 */
	private ClickControl clickControl;
	
//	/**
//	 * Flag to colour clicks by their frequency. It has to be one higher than the other options. 
//	 * 
//	 */
//	public static final int COLOUR_BY_FREQ= 6;

	public ClickDetSymbolManager(ClickControl clickControl, PamDataBlock pamDataBlock) {
		super(pamDataBlock, new SymbolData());
		this.clickControl = clickControl;
		addSymbolOption(HAS_CHANNEL_OPTIONS);
		addSymbolOption(HAS_SPECIAL_COLOUR);
		addSymbolOption(HAS_SYMBOL);
		addSymbolOption(HAS_LINE | HAS_LINE_LENGTH);
		super.setSpecialColourName("by click type");
	}
	

	@Override
	protected StandardSymbolChooser createSymbolChooser(String displayName, GeneralProjector projector) {
		return new ClickDetSymbolChooser(this, clickControl, getPamDataBlock(), displayName, getDefaultSymbol(), projector);
	}
	
//	@Override
//	public  String colourChoiceName(int iChoice) {
//		System.out.println("Select colour choice: " + iChoice);
//		if (iChoice==COLOUR_BY_FREQ) return "Colour by peak freq";
//		else return super.colourChoiceName(iChoice);
//	}
	
	@Override
	public int getNColourChoices() {
		return super.getNColourChoices()+1;
	}


	@Override
	public void addSymbolModifiers(PamSymbolChooser psc) {
		super.addSymbolModifiers(psc);
		
		//add symbol modifier that allows the clicks to be coloured by species ID. 
		psc.addSymbolModifier(new ClickClassSymbolModifier(clickControl, psc), 1);
		
		//add the peak frequency modifier that allows clicks to be coloured by peak frequency. 
		psc.addSymbolModifier(new PeakFreqModifier(psc));
		
		// we can also add some default behaviour here to match the old behaviour
		// these will get overridden once user options are set, but it's good to give defaults. 
//		SymbolModifier eventMod = psc.hasSymbolModifier(SuperDetSymbolModifier.class);
//		if (eventMod != null) {
//			eventMod.getSymbolModifierParams().modBitMap = (SymbolModType.FILLCOLOUR | SymbolModType.LINECOLOUR);
//		}
		
	}

}
