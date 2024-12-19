package rawDeepLearningClassifier.dataPlotFX;

import PamView.GeneralProjector;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.PamSymbolManager;
import PamView.symbol.StandardSymbolManager;
import PamguardMVC.PamDataBlock;
import clickTrainDetector.layout.ClickTrainSymbolModifer;
import detectiongrouplocaliser.DetectionGroupGraphics;

public class DLGroupSymbolManager extends StandardSymbolManager {

	public DLGroupSymbolManager(PamDataBlock pamDataBlock2) {
		super(pamDataBlock2, DetectionGroupGraphics.defaultSymbol);
		setSpecialColourName("DL Detection Group");

	}
	
	

	@Override
	public void addSymbolModifiers(PamSymbolChooser psc) {		
		super.addSymbolModifiers(psc);
		
		//add the peak frequency modifier that allows clicks to be coloured by peak frequency. 
		psc.addSymbolModifier(new ClickTrainSymbolModifer(psc));
		
		// we can also add some default behaviour here to match the old behaviour
		// these will get overridden once user options are set, but it's good to give defaults. 
//		SymbolModifier eventMod = psc.hasSymbolModifier(SuperDetSymbolModifier.class);
//		if (eventMod != null) {
//			eventMod.getSymbolModifierParams().modBitMap = (SymbolModType.FILLCOLOUR | SymbolModType.LINECOLOUR);
//		}
		
	}

}
