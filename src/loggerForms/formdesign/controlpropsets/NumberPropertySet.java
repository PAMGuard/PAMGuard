package loggerForms.formdesign.controlpropsets;

import loggerForms.FormDescription;
import loggerForms.UDColName;
import loggerForms.formdesign.ControlTitle;
import loggerForms.formdesign.itempanels.CtrlColPanel;
import loggerForms.formdesign.itempanels.FloatCtrlColPanel;

public class NumberPropertySet extends BasePropertySet {

	public NumberPropertySet(FormDescription formDescription,
			ControlTitle controlTitle) {
		super(formDescription, controlTitle);
	}

	@Override
	public CtrlColPanel getItemPropertyPanel(ControlTitle selTitle,
			UDColName propertyName) {
		switch (propertyName) {
		case MaxValue:
		case MinValue:
			return new FloatCtrlColPanel(selTitle, propertyName);
		default:
			return super.getItemPropertyPanel(selTitle, propertyName);
		}
	}

}
