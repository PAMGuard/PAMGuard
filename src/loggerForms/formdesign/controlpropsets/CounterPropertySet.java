package loggerForms.formdesign.controlpropsets;

import loggerForms.FormDescription;
import loggerForms.UDColName;
import loggerForms.formdesign.ControlTitle;
import loggerForms.formdesign.itempanels.CtrlColPanel;

public class CounterPropertySet extends BasePropertySet {

	public CounterPropertySet(FormDescription formDescription,
			ControlTitle controlTitle) {
		super(formDescription, controlTitle);
	}

	@Override
	public CtrlColPanel getItemPropertyPanel(ControlTitle selTitle,
			UDColName propertyName) {
		switch (propertyName) {
		case Plot:
		case Autoclear:
		case ReadOnly:
		case AutoUpdate:
			return null;
		}
		return super.getItemPropertyPanel(selTitle, propertyName);
	}

}
