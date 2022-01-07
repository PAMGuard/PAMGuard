package rawDeepLearningClassifier.dataPlotFX;

import PamView.symbol.modifier.SymbolModifierParams;
import pamViewFX.fxNodes.utilsFX.ColourArray.ColourArrayType;

/**
 * Parameters for colouring symbols by deep learning probability. 
 * 
 * @author Jamie Macaulay
 *
 */
public class DLSymbolModifierParams extends SymbolModifierParams {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The colour limits.
	 */
	public double[] clims = new double[] {0.,1.}; 
	
	
	/**
	 * The class index to show probability for. 
	 */
	public int classIndex = 0; 
	
	/**
	 * Show only binary. 
	 */
	public boolean showOnlyBinary = false;

	/**
	 * The colour array to show. 
	 */
	public ColourArrayType colArray  = ColourArrayType.FIRE; 
	

	@Override
	protected DLSymbolModifierParams clone()  {
		return (DLSymbolModifierParams) super.clone();
	}


}
