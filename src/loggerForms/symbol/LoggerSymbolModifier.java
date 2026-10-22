package loggerForms.symbol;

import PamView.GeneralProjector;
import PamView.symbol.PamSymbolChooser;
import PamView.PamSymbol;
import PamView.symbol.SymbolData;
import PamView.symbol.legend.LegendSymbolData;
import PamView.symbol.legend.SymbolLegend;
import PamView.symbol.modifier.SymbolModifier;
import PamguardMVC.PamDataUnit;
import generalDatabase.lookupTables.LookupItem;
import generalDatabase.lookupTables.LookupList;
import loggerForms.FormDescription;
import loggerForms.FormsDataUnit;
import loggerForms.controlDescriptions.CdLookup;
import loggerForms.controlDescriptions.ControlDescription;

abstract public class LoggerSymbolModifier extends SymbolModifier {

	protected FormDescription formDescription;
	protected ControlDescription controlDescription;
	protected int controlIndex;

	public LoggerSymbolModifier(FormDescription formDescription, ControlDescription controlDescription, 
			PamSymbolChooser symbolChooser, int modifyableBits) {
		super(controlDescription.getTitle(), symbolChooser, modifyableBits);
		this.formDescription = formDescription;
		this.controlDescription = controlDescription;
		controlIndex = formDescription.getControlIndex(controlDescription);
	}
	
	/**
	 * For lookup controls, a legend with the symbol for each item in the lookup
	 * list. Otherwise just the title. Only forms with data for the control are
	 * modified so this never modifies all data.
	 */
	@Override
	public SymbolLegend getSymbolLegend() {
		SymbolLegend legend = new SymbolLegend(getName());
		if (controlDescription instanceof CdLookup == false) {
			return legend;
		}
		LookupList lutList = ((CdLookup) controlDescription).getLookupList();
		if (lutList == null || lutList.getList() == null) {
			return legend;
		}
		for (LookupItem lutItem : lutList.getList()) {
			PamSymbol symbol = lutItem.getSymbol();
			if (symbol != null) {
				legend.addSymbol(new LegendSymbolData(symbol.getSymbolData(), lutItem.getText()));
			}
		}
		return legend;
	}

	/**
	 * Get the data object for this control from the data unit. 
	 * @param dataUnit
	 * @return
	 */
	public Object getControlData(PamDataUnit dataUnit) {
		if (dataUnit instanceof FormsDataUnit == false) {
			return null;
		}
		FormsDataUnit formDataUnit = (FormsDataUnit) dataUnit;
		Object[] data = formDataUnit.getFormData();
		if (data == null || data.length <= controlIndex || controlIndex < 0) {
			return null;
		}
		return data[controlIndex];
	}


	/**
	 * @return the formDescription
	 */
	protected FormDescription getFormDescription() {
		return formDescription;
	}

	/**
	 * @return the controlDescription
	 */
	protected ControlDescription getControlDescription() {
		return controlDescription;
	}

	/**
	 * @return the controlIndex
	 */
	protected int getControlIndex() {
		return controlIndex;
	}


}
