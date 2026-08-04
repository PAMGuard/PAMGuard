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
	
	public DLSymbolModifierParams() {
		setDefaultClassColors(32);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 3L;
	
	
	public static final int PREDICITON_COL = 0;

	public static final int CLASS_COL = 1;
	
	/**
	 * The way to colour the predictions.
	 */
	public int colTypeSelection = PREDICITON_COL;
	
	
	/****Prediction colours****/
	
	/**
	 * The colour limits.
	 */
	public double[] clims = new double[] {0.,1.}; 
	
	
	/**
	 * The class index to show probability for. 
	 */
	public int classIndex = 0; 
	
	
	/*******Class colours******/
	
	/**
	 * The current colours for each 
	 */
	public int[] classColors = new int[32];
	
	/**
	 * The currently selected class for colour picker- just so the user sees the same selection. 
	 */
	public int classIndex2 = 0;
	
	/**
	 * The colour array to show. 
	 */
	public ColourArrayType colArray  = ColourArrayType.FIRE; 
	
	/***************************/
	
	
	/**
	 * Show only detections which have passed a decision threshold.
	 */
	public boolean showOnlyBinary = false;

	/**
	 * Set the default colours. Note that these are shared with the prediction
	 * display so that the same class has the same default colour on both.
	 * @param num - the number of colours to set.
	 */
	public void setDefaultClassColors(int num) {
		if (classColors==null || classColors.length<num) {
			classColors = new int[Math.max(32, num)];
		}

		//run through default colours
		for (int i=0; i<classColors.length; i++) {
			classColors[i]=DLClassColours.getClassColourInt(i);
		}
	}

	@Override
	protected DLSymbolModifierParams clone()  {
		return (DLSymbolModifierParams) super.clone();
	}


}
