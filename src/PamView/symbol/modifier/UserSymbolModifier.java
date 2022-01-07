package PamView.symbol.modifier;

import java.awt.Color;

import PamView.GeneralProjector;
import PamView.PamSymbolType;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataUnit;

/**
 * 	A symbol modifier which allows the user to manually change settings. 
 * @author Jamie Macualay 
 *
 */
public class UserSymbolModifier extends SymbolModifier {
	
	
	private SymbolData symbolData = new SymbolData(PamSymbolType.SYMBOL_CIRCLE, 5, 5, true, Color.BLACK, Color.BLACK);


	public SymbolData getSymbolData() {
		return symbolData;
	}

	public void setSymbolData(SymbolData symbolData) {
		this.symbolData = symbolData;
	}

	public UserSymbolModifier(PamSymbolChooser symbolChooser) {
		super("User", symbolChooser,  SymbolModType.FILLCOLOUR | SymbolModType.LINECOLOUR | SymbolModType.SHAPE);
		// TODO Auto-generated constructor stub
	}

	@Override
	public SymbolData getSymbolData(GeneralProjector projector, PamDataUnit dataUnit) {
		return symbolData;
	}

}
