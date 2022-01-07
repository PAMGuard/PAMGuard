package PamView.symbol;

import java.awt.Color;

import PamView.GeneralProjector;
import PamView.PamColors;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

public class CyclicColourSymbolChooser extends PamSymbolChooser {
	
	private SymbolData symbolData;;


	public CyclicColourSymbolChooser(PamSymbolManager pamSymbolManager, PamDataBlock pamDataBlock, String displayName,
			GeneralProjector projector) {
		this(pamSymbolManager, pamDataBlock, displayName, projector, null);
	}
	
	public CyclicColourSymbolChooser(PamSymbolManager pamSymbolManager, PamDataBlock pamDataBlock, String displayName,
			GeneralProjector projector, SymbolData baseSymbol) {
		super(pamSymbolManager, pamDataBlock, displayName, projector);
		if (baseSymbol != null) {
			symbolData = baseSymbol;
		}
		else {
			symbolData = new SymbolData();
		}
	}

	@Override
	public SymbolData getSymbolChoice(GeneralProjector projector, PamDataUnit dataUnit) {
		Color col = PamColors.getInstance().getWhaleColor((int) dataUnit.getUID());
		symbolData.setFillColor(col);
		symbolData.setLineColor(col);
		return symbolData;
	}

	@Override
	public void setSymbolOptions(PamSymbolOptions symbolOptions) {
		// TODO Auto-generated method stub

	}

	@Override
	public PamSymbolOptions getSymbolOptions() {
		// TODO Auto-generated method stub
		return null;
	}

}
