package cpod;

import java.awt.Color;

import PamController.PamControlledUnit;
import PamView.PamSymbolType;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.StandardSymbolManager;
import PamView.symbol.SymbolData;
import PamView.symbol.modifier.PeakFreqModifier;
import PamguardMVC.PamDataBlock;

public class CPODSymbolManager extends StandardSymbolManager {

	/**
	 * Reference to the click control. 
	 */
	private PamControlledUnit cpodControl;
	
	
	private static SymbolData defaultSymbol = new SymbolData(PamSymbolType.SYMBOL_CIRCLE, 10, 10, true, Color.BLACK, Color.BLACK);
	
//	/**
//	 * Flag to colour clicks by their frequency. It has to be one higher than the other options. 
//	 * 
//	 */
//	public static final int COLOUR_BY_FREQ= 6;

	public CPODSymbolManager(PamControlledUnit cpodControl, PamDataBlock pamDataBlock) {
		super(pamDataBlock, defaultSymbol);
		this.cpodControl = cpodControl;
		addSymbolOption(HAS_SYMBOL);
	}
	

//	@Override
//	public  String colourChoiceName(int iChoice) {
//		System.out.println("Select colour choice: " + iChoice);
//		if (iChoice==COLOUR_BY_FREQ) return "Colour by peak freq";
//		else return super.colourChoiceName(iChoice);
//	}
	
//	@Override
//	public int getNColourChoices() {
//		return super.getNColourChoices()+1;
//	}


	@Override
	public void addSymbolModifiers(PamSymbolChooser psc) {
	
		super.addSymbolModifiers(psc);
		
		//add the peak frequency modifier that allows clicks to be coloured by peak frequency. 
		psc.addSymbolModifier(new PeakFreqModifier(psc));
		
		//add the peak frequency modifier that allows clicks to be coloured by peak frequency. 
		psc.addSymbolModifier(new CPODSpeciesModifier(psc));
		
		// we can also add some default behaviour here to match the old behaviours
		// these will get overridden once user options are set, but it's good to give defaults. 
//		SymbolModifier eventMod = psc.hasSymbolModifier(SuperDetSymbolModifier.class);
//		if (eventMod != null) {
//			eventMod.getSymbolModifierParams().modBitMap = (SymbolModType.FILLCOLOUR | SymbolModType.LINECOLOUR);
//		}
		
	}

}
