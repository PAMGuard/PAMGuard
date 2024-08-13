package loggerForms.symbol;

import PamView.GeneralProjector;
import PamView.PamSymbol;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataUnit;
import generalDatabase.lookupTables.LookupItem;
import generalDatabase.lookupTables.LookupList;
import loggerForms.FormDescription;
import loggerForms.FormsDataUnit;
import loggerForms.controlDescriptions.CdLookup;
import loggerForms.controlDescriptions.ControlDescription;

public class LookupSymbolModifier extends LoggerSymbolModifier {

	public LookupSymbolModifier(FormDescription formDescription, ControlDescription controlDescription,
			PamSymbolChooser symbolChooser, int modifyableBits) {
		super(formDescription, controlDescription, symbolChooser, modifyableBits);
	}

	@Override
	public SymbolData getSymbolData(GeneralProjector projector, PamDataUnit dataUnit) {
		Object data = getControlData(dataUnit);
		if (data == null) {
			return null;
		}

		LookupList lutList = ((CdLookup)controlDescription).getLookupList();
		int lutIndex = lutList.indexOfCode(data.toString());
		LookupItem lutItem = lutList.getLookupItem(lutIndex);
		if (lutItem == null) {
			return null;
		}
		PamSymbol symbol = lutItem.getSymbol();
		if (symbol == null) {
			return null;
		}
		return symbol.getSymbolData();
	}

}
