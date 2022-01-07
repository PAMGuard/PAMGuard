package PamguardMVC.superdet.swing;

import PamView.symbol.PamSymbolChooser;
import PamView.symbol.StandardSymbolManager;
import PamView.symbol.SymbolData;
import PamView.symbol.modifier.RotateColoursModifier;
import PamView.symbol.modifier.SymbolModType;
import PamguardMVC.superdet.SuperDetDataBlock;

public class SuperDetectionSymbolManager extends StandardSymbolManager {

	private SuperDetDataBlock superDetBlock;

	public SuperDetectionSymbolManager(SuperDetDataBlock pamDataBlock, SymbolData defaultSymbol) {
		super(pamDataBlock, defaultSymbol);
		superDetBlock = pamDataBlock;
	}

	@Override
	public void addSymbolModifiers(PamSymbolChooser psc) {
		super.addSymbolModifiers(psc);
		// the default will be just to modify by rotating colours. 
		RotateColoursModifier rotCols = new RotateColoursModifier(psc);
		rotCols.setName("Event Colour");
		rotCols.getSymbolModifierParams().modBitMap = SymbolModType.FILLCOLOUR | SymbolModType.LINECOLOUR;
		psc.addSymbolModifier(rotCols, 0);
//		addAnnotationModifiers(psc);
	}

}
