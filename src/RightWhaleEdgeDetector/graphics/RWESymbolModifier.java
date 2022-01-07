package RightWhaleEdgeDetector.graphics;

import java.awt.Color;

import PamView.ColourArray;
import PamView.GeneralProjector;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.SymbolData;
import PamView.symbol.modifier.SymbolModType;
import PamView.symbol.modifier.SymbolModifier;
import PamguardMVC.PamDataUnit;
import RightWhaleEdgeDetector.RWEDataUnit;

public class RWESymbolModifier extends SymbolModifier {
	
	private ColourArray rwColours;
	
	private SymbolData symbolData = new SymbolData();

	public RWESymbolModifier(PamSymbolChooser symbolChooser) {
		super("Right Whale Score", symbolChooser, SymbolModType.FILLCOLOUR | SymbolModType.LINECOLOUR);
		rwColours = ColourArray.createHotArray(13);
	}

	@Override
	public SymbolData getSymbolData(GeneralProjector projector, PamDataUnit dataUnit) {
		RWEDataUnit rweDataUnit = (RWEDataUnit) dataUnit;
		int type = rweDataUnit.rweSound.soundType;
		Color col = rwColours.checkColour(type);
		symbolData.setLineColor(col);
		symbolData.setFillColor(col);
		return symbolData;
	}

}
