package clickDetector.tdPlots;

import java.awt.Color;

import PamView.GeneralProjector;
import PamView.PamColors;
import PamView.symbol.StandardSymbolChooser;
import PamView.symbol.StandardSymbolManager;
import PamView.symbol.StandardSymbolOptions;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import clickDetector.ClickTrainDetection;
import clickDetector.offlineFuncs.OfflineEventDataUnit;

public class ClickEventSymbolChooser extends StandardSymbolChooser {

	public ClickEventSymbolChooser(StandardSymbolManager standardSymbolManager, PamDataBlock pamDataBlock,
			String displayName, SymbolData defaultSymbol, GeneralProjector projector) {
		super(standardSymbolManager, pamDataBlock, displayName, defaultSymbol, projector);
		this.getSymbolOptions().colourChoice = StandardSymbolOptions.COLOUR_SPECIAL;
	}

//	@Override
//	public SymbolData getSymbolChoice(GeneralProjector projector, PamDataUnit dataUnit) {
////		if (dataUnit.getClass() != OfflineEventDataUnit.class && dataUnit.getClass() != ClickTrainDetection.class) {
////			dataUnit = dataUnit.getSuperDetection(OfflineEventDataUnit.class);
////			if (dataUnit == null) {
////				return null; 
////			}
////		}
////		OfflineEventDataUnit oedu = (OfflineEventDataUnit) dataUnit;
//		int colId = dataUnit.getColourIndex();
//		Color col = PamColors.getInstance().getWhaleColor(colId);
//		//SymbolData sd = super.colourBySpecial(symbolData, projector, dataUnit);
//		getDefaultSymbol().setFillColor(col);
//		getDefaultSymbol().setLineColor(col);
//		return getDefaultSymbol();
//	}


}
