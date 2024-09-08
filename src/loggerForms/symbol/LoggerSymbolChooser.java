package loggerForms.symbol;

import PamView.GeneralProjector;
import PamView.symbol.StandardSymbolChooser;
import PamView.symbol.StandardSymbolManager;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataBlock;
import dataPlots.data.SimpleSymbolChooser;
import loggerForms.FormsDataBlock;

public class LoggerSymbolChooser extends StandardSymbolChooser {

	private FormsDataBlock formsDataBlock;

	public LoggerSymbolChooser(StandardSymbolManager standardSymbolManager, FormsDataBlock pamDataBlock,
			String displayName, SymbolData defaultSymbol, GeneralProjector projector) {
		super(standardSymbolManager, pamDataBlock, displayName, defaultSymbol, projector);
		this.formsDataBlock = pamDataBlock;		
		
	}


}
