package whistlesAndMoans.plots;

import java.awt.Color;

import PamView.GeneralProjector;
import PamView.symbol.StandardSymbolChooser;
import PamView.symbol.StandardSymbolManager;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import whistlesAndMoans.CROverlayGraphics;

public class WhistleSymbolChooser extends StandardSymbolChooser {

	public WhistleSymbolChooser(StandardSymbolManager standardSymbolManager, PamDataBlock pamDataBlock,
			String displayName, SymbolData defaultSymbol, GeneralProjector projector) {
		super(standardSymbolManager, pamDataBlock, displayName, defaultSymbol, projector);
	}

//	/* (non-Javadoc)
//	 * @see PamView.symbol.StandardSymbolChooser#colourBySpecial(PamView.symbol.SymbolData, PamView.GeneralProjector, PamguardMVC.PamDataUnit)
//	 */
//	@Override
//	public SymbolData colourBySpecial(SymbolData symbolData, GeneralProjector projector, PamDataUnit dataUnit) {
//		SymbolData sd = super.colourBySpecial(symbolData, projector, dataUnit);
//		Color[] whistleColours = CROverlayGraphics.whistleColours;
//		int iCol=0;
//		if (dataUnit!=null) {	// if there is no data unit, just default to first color (will happen when drawing key)
//			iCol = dataUnit.getAbsBlockIndex()%whistleColours.length;
//		}
//		sd.setFillColor(whistleColours[iCol]);
//		sd.setLineColor(whistleColours[iCol]);
//		return sd;
//	}
	
	@Override
	public SymbolData getSymbolChoice(GeneralProjector projector, PamDataUnit dataUnit) {
//		//	for debugging		
//		SymbolData symbolData = getSymbolOptions().symbolData;
//		Debug.out.println("WhistleSymbolChooser: symbolOptions: " +  symbolData);
		return super.getSymbolChoice(projector, dataUnit); 
	}

}
