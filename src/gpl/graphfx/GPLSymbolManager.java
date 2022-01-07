package gpl.graphfx;


import java.awt.Color;

import PamView.PamSymbolType;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.StandardSymbolManager;
import PamView.symbol.SymbolData;
import PamView.symbol.modifier.RotateColoursModifier;
import gpl.GPLDetectionBlock;

public class GPLSymbolManager extends StandardSymbolManager {


	private static SymbolData defaultSymbol = new SymbolData(PamSymbolType.SYMBOL_DIAMOND, 10, 10, true, Color.RED, Color.RED);
	private GPLDetectionBlock gplDetectionBlock;

	public GPLSymbolManager(GPLDetectionBlock gplDetectionBlock) {
		super(gplDetectionBlock, defaultSymbol);
		defaultSymbol.lineThickness = 3;
		this.gplDetectionBlock = gplDetectionBlock;
		addSymbolOption(HAS_SYMBOL);
		addSymbolOption(HAS_CHANNEL_OPTIONS);
		addSymbolOption(HAS_LINE | HAS_LINE_LENGTH);
	}
	
	@Override
	public void addSymbolModifiers(PamSymbolChooser psc) {
		super.addSymbolModifiers(psc);
		psc.addSymbolModifier(new RotateColoursModifier(psc));
	}

}
