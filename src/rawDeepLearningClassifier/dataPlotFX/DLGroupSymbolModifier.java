package rawDeepLearningClassifier.dataPlotFX;

import java.awt.Color;

import PamView.GeneralProjector;
import PamView.PamColors;
import PamView.PamSymbolType;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.SymbolData;
import PamView.symbol.legend.ColourSetSymbolData;
import PamView.symbol.legend.SymbolLegend;
import PamView.symbol.modifier.SymbolModType;
import PamView.symbol.modifier.SymbolModifier;
import PamguardMVC.PamDataUnit;

/**
 * Symbol modifier for DL groups
 */
public class DLGroupSymbolModifier extends SymbolModifier {
	
	public final static String CLICK_TRAIN_MODIFIER_NAME = "Colour by DL Group";


	public DLGroupSymbolModifier(PamSymbolChooser symbolChooser) {
		super(CLICK_TRAIN_MODIFIER_NAME, symbolChooser, SymbolModType.FILLCOLOUR |  SymbolModType.LINECOLOUR );
		// TODO Auto-generated constructor stub
	}

	@Override
	public SymbolData getSymbolData(GeneralProjector projector, PamDataUnit dataUnit) {
	
		SymbolData symbolData=new SymbolData();
		symbolData.symbol=PamSymbolType.SYMBOL_CIRCLE;
		
		Color col = PamColors.getInstance().getWhaleColor(dataUnit.getColourIndex());
		symbolData.setFillColor(col);
		symbolData.setLineColor(col);

		return symbolData;
	}

	@Override
	public SymbolLegend getSymbolLegend() {
		SymbolData symbolData = new SymbolData();
		symbolData.symbol = PamSymbolType.SYMBOL_CIRCLE;
		return new SymbolLegend(getName(), ColourSetSymbolData.createWhaleColourSet(symbolData, "DL group"));
	}

	@Override
	public boolean isModifyAll() {
		return true;
	}

}
