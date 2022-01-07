package clickDetector.tdPlots;

import PamView.symbol.StandardSymbolOptions;
import PamView.symbol.SymbolData;
import PamView.symbol.modifier.PeakFreqModifier;
import PamView.symbol.modifier.PeakFreqSymbolOptions;
import pamViewFX.fxNodes.utilsFX.ColourArray.ColourArrayType;

/**
 * Clikc symbol options which contain a few more settings tghan standard symbol options. 
 * @author Jamie Macaulay
 *
 */
public class ClickSymbolOptions extends StandardSymbolOptions {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ClickSymbolOptions(SymbolData defaultSymbol) {
		super(defaultSymbol);
		
//		//System.out.println("ADD FREQ PARAMS: "); 
//		//always add a set of frequency params because these are not default. 
//		//Not really sure this is the best place for this.
//		this.setModifierParams(PeakFreqModifier.PEAK_FREQ_MODIFIER_NAME, new PeakFreqSymbolOptions());
	}

	/**
	 * Used if saved standard symbol options are loaded. Now that click detector uses click specific options, 
	 * need to convert sometimes. 
	 * @param symbolOptions - standard symbol options
	 * @param defaultSymbol - default symbol; 
	 */
	public ClickSymbolOptions(StandardSymbolOptions symbolOptions) {
		super(symbolOptions.symbolData);
		
		//System.out.println("ADD FREQ PARAMS: "); 

		//does this need to be here still?
		this.colourChoice=symbolOptions.colourChoice;
		this.hideLinesWithLatLong=symbolOptions.hideLinesWithLatLong;
		this.mapLineLength=symbolOptions.mapLineLength;
		this.symbolData=symbolOptions.symbolData;
		
//		//always add a set of frequency params because these are not default. 
//		//Not really sure this is the best place for this.
//		this.setModifierParams(PeakFreqModifier.PEAK_FREQ_MODIFIER_NAME, new PeakFreqSymbolOptions());
	}
	
	
}
