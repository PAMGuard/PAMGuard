package loggerForms.symbol;

import PamView.GeneralProjector;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.SymbolData;
import PamView.symbol.modifier.SymbolModifier;
import PamguardMVC.PamDataUnit;
import loggerForms.FormDescription;
import loggerForms.FormsDataUnit;
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
