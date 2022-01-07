package PamView.symbol.modifier;

import pamViewFX.fxNodes.utilsFX.ColourArray.ColourArrayType;


/**
 * Options for peak frequency.  
 * @author Jamie Macaulay
 *
 */
public class PeakFreqSymbolOptions extends SymbolModifierParams {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;

	/**
	 * Colour array for colouring by frequency. 
	 */
	public ColourArrayType freqColourArray = ColourArrayType.HOT; 
	
	/**
	 * Frequency limits. 
	 */
	public double[] freqLimts = new double[]{0, 250000}; 

}
