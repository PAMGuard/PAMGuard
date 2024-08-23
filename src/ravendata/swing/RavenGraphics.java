package ravendata.swing;

import java.awt.Color;

import PamView.PamDetectionOverlayGraphics;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamguardMVC.PamDataBlock;

public class RavenGraphics extends PamDetectionOverlayGraphics {
	
	public static final PamSymbol defaultSymbol = new PamSymbol(PamSymbolType.SYMBOL_SQUARE,12, 12, false, Color.white, Color.red);

	public RavenGraphics(PamDataBlock parentDataBlock) {
		super(parentDataBlock, defaultSymbol);
	}

}
