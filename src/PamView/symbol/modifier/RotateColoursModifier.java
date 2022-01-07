package PamView.symbol.modifier;

import java.awt.Color;

import PamView.GeneralProjector;
import PamView.PamColors;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataUnit;

/**
 * Symbol modifier that rotates through standard colours, usually based on the 
 * data's UID. 
 * @author dg50
 *
 */
public class RotateColoursModifier extends SymbolModifier {
	
	private SymbolData symbolData = new SymbolData();
	
	public RotateColoursModifier(PamSymbolChooser symbolChooser) {
		super("Rotate colours", symbolChooser, SymbolModType.FILLCOLOUR | SymbolModType.LINECOLOUR);
		setToolTipText("Colour by a series of rotating colours");
	}

	public RotateColoursModifier(String name, PamSymbolChooser symbolChooser, int modifyableBits) {
		super(name, symbolChooser, modifyableBits);
	}

	@Override
	public SymbolData getSymbolData(GeneralProjector projector, PamDataUnit dataUnit) {
		
		int modifyBits = getSymbolModifierParams().modBitMap; 
		
		if (modifyBits == 0) {
			return symbolData;
		}
		Color col = PamColors.getInstance().getWhaleColor(dataUnit.getColourIndex());
		if ((modifyBits & SymbolModType.LINECOLOUR) != 0) {
			symbolData.setLineColor(col);
		}
		if ((modifyBits & SymbolModType.FILLCOLOUR) != 0) {
			symbolData.setFillColor(col);
			symbolData.fill = true;
		}
		return symbolData;
	}


}
