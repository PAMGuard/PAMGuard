package group3dlocaliser.swinggraphics;

import java.awt.Color;

import PamView.PamDetectionOverlayGraphics;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamguardMVC.PamDataBlock;

public class Group3DOverlayDraw extends PamDetectionOverlayGraphics {


	public static final PamSymbol defaultSymbol = new PamSymbol(PamSymbolType.SYMBOL_DIAMOND, 6, 6, true, Color.GREEN, Color.BLACK);

	public Group3DOverlayDraw(PamDataBlock parentDataBlock) {
		super(parentDataBlock, defaultSymbol);
		// TODO Auto-generated constructor stub
	}

}
