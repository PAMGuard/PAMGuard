package PamView.symbol;

import PamguardMVC.PamDataBlock;

/**
 * Version of the standard symbol manager that only has the optoin on the main
 * symbol type and no modifiers whatsoever. 
 * @author dg50
 *
 */
public class SymbolOnlyManager extends StandardSymbolManager {

	public SymbolOnlyManager(PamDataBlock pamDataBlock, SymbolData defaultSymbol) {
		super(pamDataBlock, defaultSymbol);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void addSymbolModifiers(PamSymbolChooser psc) {
	}
}
