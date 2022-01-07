package loggerForms.controlDescriptions;

import loggerForms.FormDescription;
import loggerForms.ItemInformation;
import loggerForms.LoggerForm;
import loggerForms.controls.LoggerControl;

public abstract class NumberControlDescription extends InputControlDescription {

	protected NumberControlDescription(FormDescription formDescription,
			ItemInformation itemInformation) {
		super(formDescription, itemInformation);
	}

	@Override
	public String getHint() {
		String hint = super.getHint();
		if (hint == null) {
			hint = "Enter a number";
		}
		return hint;
	}


}
