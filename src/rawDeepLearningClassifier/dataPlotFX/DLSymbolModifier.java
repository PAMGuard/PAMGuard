package rawDeepLearningClassifier.dataPlotFX;

import java.awt.Color;

import PamView.GeneralProjector;
import PamView.PamSymbolType;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.SymbolData;
import PamView.symbol.modifier.SymbolModType;
import PamView.symbol.modifier.SymbolModifier;
import PamView.symbol.modifier.SymbolModifierParams;
import PamguardMVC.PamDataUnit;
import pamViewFX.fxNodes.utilsFX.ColourArray;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;
import pamViewFX.fxNodes.utilsFX.ColourArray.ColourArrayType;
import pamViewFX.symbol.SymbolModifierPane;
import rawDeepLearningClassifier.dlClassification.PredictionResult;
import rawDeepLearningClassifier.logging.DLAnnotation;
import rawDeepLearningClassifier.logging.DLAnnotationType;


/**
 * The DL symbol modifier. Colours symbols by prediction.
 * 
 * @author Jamie Macaulay. 
 *
 */
public class DLSymbolModifier extends SymbolModifier {

	public static String DL_MODIFIER_NAME = "DL Prediction";

	/**
	 * The default symbol data for data annotated with a deep elanring classifier. 
	 */
	private SymbolData symbolData = new SymbolData(PamSymbolType.SYMBOL_CIRCLE, 5, 5, true, Color.BLACK, Color.BLACK);


	/**
	 * The symbol options pane.
	 */
	private DLSymbolOptionPane optionsPane;

	/**
	 * Rge DL annotation type. 
	 */
	private DLAnnotationType dlAnnotType;
	

	/**
	 * The DL symbol options. 
	 */
	private DLSymbolModifierParams dlSymbolOptions = new DLSymbolModifierParams(); 

	private ColourArrayType colourArrayType;

	private ColourArray colourArray;



	public DLSymbolModifier(PamSymbolChooser symbolChooser, DLAnnotationType dlAnnotType) {
		 super(DL_MODIFIER_NAME, symbolChooser, SymbolModType.FILLCOLOUR | SymbolModType.LINECOLOUR);
		 this.dlAnnotType=dlAnnotType; 
//		//if there is not a  module
//		if (symbolChooser.getClass().isAssignableFrom(StandardSymbolChooser.class)) {
//			System.out.println("HERE: "); 
//			((StandardSymbolChooser) symbolChooser).getSymbolOptions().setModifierParams(DL_MODIFIER_NAME + dlAnnotType.getShortIdCode(), new DLSymbolModifierParams());
//		}

	}
	
//	protected SymbolModifierParams checkParams(SymbolModifierParams standardSymbolOptions) {
////		System.out.println("---"); 
//		if (!(standardSymbolOptions instanceof DLSymbolModifierParams)) {
////			System.err.println("DLSymbolModifier: warning: the correct parameters class was not set -> re-setting new DLSymbolModifierParams: " + standardSymbolOptions);
//			SymbolModifierParams standardSymbolOptions2 = new DLSymbolModifierParams(); 
//			((StandardSymbolChooser) this.getSymbolChooser()).getSymbolOptions().setModifierParams(DL_MODIFIER_NAME, standardSymbolOptions2);
//			standardSymbolOptions = ((StandardSymbolChooser) this.getSymbolChooser()).getSymbolOptions().getModifierParams(DL_MODIFIER_NAME); 			
////			System.out.println("New symbol options: " + standardSymbolOptions + " Options class: " + ((StandardSymbolChooser) this.getSymbolChooser()).getSymbolOptions()); 
////			System.out.println("---------"); 
//			return standardSymbolOptions2;
//		}
//		return standardSymbolOptions; 
//	}
	
	

	@Override
	public SymbolData getSymbolData(GeneralProjector projector, PamDataUnit dataUnit) {

		//OK need to extract the annotation. 
		DLAnnotation annotation  = (DLAnnotation) dataUnit.findDataAnnotation(DLAnnotation.class);

		if (annotation==null || annotation.getModelResults()==null || annotation.getModelResults().size()<=0) {
			return null; 
		}	
		
		//System.out.println("Class index: " + dlSymbolOptions.classIndex); 
		
		if (dlSymbolOptions.classIndex<0) {
			dlSymbolOptions.classIndex=0;
		}

		boolean passed = false; 
		double maxValue = -1; 
		for (PredictionResult modelResult: annotation.getModelResults()) {
			if (modelResult.isBinaryClassification()) passed = true; 
			
			if (modelResult.getPrediction()[dlSymbolOptions.classIndex]>maxValue) {
				maxValue = modelResult.getPrediction()[dlSymbolOptions.classIndex]; 
			}; 
		}
		
		if (passed || !dlSymbolOptions.showOnlyBinary) {
			//work out probability colour...
			
			checkColourArray(); 
			
			maxValue=(maxValue - dlSymbolOptions.clims[0])/(dlSymbolOptions.clims[1]-dlSymbolOptions.clims[0]);

			//System.out.println("Get max index: maxIndex" + maxIndex + " freq: " + freq + " pwr len: " + powerSpectrum.length);
			Color freqCol = PamUtilsFX.fxToAWTColor(this.colourArray.getColour(maxValue));
			
			symbolData.setFillColor(freqCol);
			symbolData.setLineColor(freqCol);

			return symbolData; 
		}
		else {
			//has data but we have only show binary option selected. 
			return null; 
		}
	}
	
	/**
	 * Check the correct colour array is being used and, if not, change it. 
	 */
	public void checkColourArray() {
		if (this.colourArrayType!=dlSymbolOptions.colArray || colourArrayType==null) {
			colourArray = ColourArray.createStandardColourArray(128, dlSymbolOptions.colArray); 
			this.colourArrayType=dlSymbolOptions.colArray; 
		}
	}

	@Override
	public DLSymbolModifierParams getSymbolModifierParams() {
		if (dlSymbolOptions==null) dlSymbolOptions = new DLSymbolModifierParams(); 
		return dlSymbolOptions;
	}

	@Override
	public void setSymbolModifierParams(SymbolModifierParams symbolModifierParams) {
		if (!(symbolModifierParams instanceof DLSymbolModifierParams)) {
			System.err.println("DLModifier: warning: the saved parameters were not an instance of DLModifier: " + symbolModifierParams); 
			dlSymbolOptions = new DLSymbolModifierParams(); 
			return; 
		}
		else this.dlSymbolOptions = (DLSymbolModifierParams) symbolModifierParams;
	}

	/**
	 * Get the JavaFX symbol options pane that has options for the symbol pane.
	 * @return the symbol options pane. 
	 */
	public SymbolModifierPane getOptionsPane() {
		if (optionsPane == null) {
			optionsPane = new DLSymbolOptionPane(this); 
		}
		return optionsPane; 
	}
	
	
	public DLAnnotationType getDLAnnotType() {
		return dlAnnotType;
	}

}
