package rawDeepLearningClassifier.dataPlotFX;


import PamView.symbol.modifier.SymbolModifierParams;
import javafx.scene.paint.Color;
import pamViewFX.fxNodes.utilsFX.ColourArray.ColourArrayType;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;

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
	 * Set the default colours. 
	 * @param num - the number of colours to set. 
	 */
	public void setDefaultClassColors(int num) {
		if (classColors==null || classColors.length<num) {
			classColors = new int[32];
		}
		
		//run through default colours
		for (int i=0; i<classColors.length; i++) {
			switch (i%8) {
			case 0:
				classColors[i]=PamUtilsFX.colorToInt(Color.RED);
				break;
			case 1:
				classColors[i]=PamUtilsFX.colorToInt(Color.GREEN);
				break;
			case 2:
				classColors[i]=PamUtilsFX.colorToInt(Color.BLUE);
				break;
			case 3:
				classColors[i]=PamUtilsFX.colorToInt(Color.CYAN);
				break;
			case 4:
				classColors[i]=PamUtilsFX.colorToInt(Color.MAGENTA);
				break;
			case 5:
				classColors[i]=PamUtilsFX.colorToInt(Color.YELLOW);
				break;
			case 6:
				classColors[i]=PamUtilsFX.colorToInt(Color.ORANGE);
				break;
			case 7:
				classColors[i]=PamUtilsFX.colorToInt(Color.PURPLE);
				break;

			}
		}
	}

	@Override
	protected DLSymbolModifierParams clone()  {
		return (DLSymbolModifierParams) super.clone();
	}


}
