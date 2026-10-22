package RightWhaleEdgeDetector.graphics;

import java.awt.Color;

import PamView.ColourArray;
import PamView.GeneralProjector;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.SymbolData;
import PamView.symbol.legend.ColourBarSymbolData;
import PamView.symbol.legend.SymbolLegend;
import PamView.symbol.modifier.SymbolModType;
import PamView.symbol.modifier.SymbolModifier;
import PamguardMVC.PamDataUnit;
import RightWhaleEdgeDetector.RWEDataUnit;
import pamViewFX.fxNodes.utilsFX.ColourArray.ColourArrayType;

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

	/**
	 * Legend with a colour bar for the right whale sound type score. 
	 */
	@Override
	public SymbolLegend getSymbolLegend() {
		// FX FIRE is the same black-red-orange-yellow-white map as the Swing hot array.
		return new SymbolLegend(getName(), new ColourBarSymbolData(symbolData, "Score", ColourArrayType.FIRE,
				0, rwColours.getNumbColours()-1, null));
	}

	@Override
	public boolean isModifyAll() {
		return true;
	}

}
