package whistleDetector;

import java.awt.Color;

import PamView.PamDetectionOverlayGraphics;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataBlock;

public class WhistleLocalisationGraphics extends PamDetectionOverlayGraphics {

	public static final SymbolData defaultSymbol = new SymbolData(PamSymbolType.SYMBOL_DIAMOND, 10, 10, true,
			Color.BLUE, Color.GREEN);
	private PamDataBlock<WhistleGroupDetection> whistleLocations;
	public WhistleLocalisationGraphics(PamDataBlock<WhistleGroupDetection> whistleLocations) {
		super(whistleLocations, new PamSymbol(defaultSymbol));
		this.whistleLocations = whistleLocations;
//		if (getPamSymbol() == null) {
//			setPamSymbol(new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 12, 12, false, Color.BLACK, Color.BLUE));
//		}
	}

}
