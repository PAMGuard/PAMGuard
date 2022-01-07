package detectiongrouplocaliser;

import java.awt.Color;

import PamView.PamDetectionOverlayGraphics;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.symbol.SymbolData;

public class DetectionGroupGraphics extends PamDetectionOverlayGraphics {
	
	public static final SymbolData defaultSymbol = new SymbolData(PamSymbolType.SYMBOL_DIAMOND, 10, 10, true, Color.GREEN, Color.CYAN);
	
	public static final SymbolData starSymbol = new SymbolData(PamSymbolType.SYMBOL_STAR, 10, 10, true, Color.GREEN, Color.CYAN);


	public DetectionGroupGraphics(DetectionGroupProcess detectionGroupProcess) {
		super(detectionGroupProcess.getDetectionGroupDataBlock(), new PamSymbol(defaultSymbol));
	}

}
