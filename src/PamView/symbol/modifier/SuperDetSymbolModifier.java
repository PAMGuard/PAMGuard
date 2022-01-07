package PamView.symbol.modifier;

import java.awt.Color;

import PamView.GeneralProjector;
import PamView.PamSymbolType;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.PamSymbolManager;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

/**
 * Super detection symbol modifier. Wow this is confusing. This is actually a symbolModifier
 * in the sub detection datablock. It will call into the appropriate super detection, the class of 
 * which is quite possibly unknown in advance, and the symbol chooser of the superdetection will 
 * eventually call it's own modifiers, likely to be something like a RotateColourModifier, but could
 * easily go on into annotations, to colour and shape by all sorts of stuff. 
 * @author dg50
 *
 */
@Deprecated
public class SuperDetSymbolModifier extends SymbolModifier {

	private SymbolData symbolData = new SymbolData(PamSymbolType.SYMBOL_CIRCLE, 5, 5, true, Color.BLACK, Color.BLACK);

	public SuperDetSymbolModifier(PamSymbolChooser symbolChooser) {
		super("Super Detection", symbolChooser, SymbolModType.EVERYTHING);
		
	}

	@Override
	public SymbolData getSymbolData(GeneralProjector projector, PamDataUnit dataUnit) {
		PamDataUnit superDet = dataUnit.getSuperDetection(0);
		if (superDet == null) {
//			System.out.println("Super 1 - no det " + dataUnit.getSuperDetectionsCount());
			return null;
		}
		PamDataBlock superBlock = superDet.getParentDataBlock();
		if (superBlock == null) {
//			System.out.println("Super 2 - no super det block");
			return null;
		}
		PamSymbolManager symMan = superBlock.getPamSymbolManager();
		if (symMan == null) {
//			System.out.println("Super 3 - no super det symbol manager");
			return null;
		}
		PamSymbolChooser superChooser = symMan.getSymbolChooser(getSymbolChooser().getDisplayName(), projector);
		if (superChooser == null) {
//			System.out.println("Super 4 - - no super det symbol chooser");
			return null;
		}
//		System.out.println("Super OK:  " + dataUnit.getSuperDetectionsCount() +  "  superDet " + superDet 
//				+ " Colour: " + superChooser.getSymbolChoice(projector, superDet));
		
		//this needs to take the super detection as an input - the data unit. 
		SymbolData superSymbol = superChooser.getSymbolChoice(projector, superDet); 
//		if (superSymbol !=null) {
//			symbolData.setFillColor(superSymbol.getFillColor());
//			symbolData.setLineColor(superSymbol.getLineColor());
//		}
		
		return superSymbol;
	}
	
}
