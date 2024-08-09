package loggerForms.symbol;

import PamView.GeneralProjector;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.PamSymbolManager;
import PamView.symbol.StandardSymbolChooser;
import PamView.symbol.StandardSymbolManager;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataBlock;
import loggerForms.FormsDataBlock;
import loggerForms.LoggerFormGraphics;

public class LoggerSymbolManager extends StandardSymbolManager {

	private FormsDataBlock formsDataBlock;
	
	public LoggerSymbolManager(FormsDataBlock pamDataBlock) {
		super(pamDataBlock, LoggerFormGraphics.defaultSymbol);
		this.formsDataBlock = pamDataBlock;
	}

	@Override
	protected LoggerSymbolChooser createSymbolChooser(String displayName, GeneralProjector projector) {
		return new LoggerSymbolChooser(this, formsDataBlock, displayName, getDefaultSymbol(), projector);
	}

	@Override
	public void addSymbolModifiers(PamSymbolChooser psc) {
		super.addSymbolModifiers(psc);
		/*
		 *  now add symbol modifiers for each control in the form.
		 *  This will primarily be lookups which should have defined colours. 
		 *  May be able to do other controls based on their type / null / >0 values. 
		 *  Focus on lut's for now.  
		 */
	}
}
