package loggerForms.formdesign.controlpropsets;

import loggerForms.FormDescription;
import loggerForms.UDColName;
import loggerForms.controlDescriptions.ControlTypes;
import loggerForms.formdesign.ControlTitle;
import loggerForms.formdesign.itempanels.BooleanCtrlColPanel;
import loggerForms.formdesign.itempanels.CtrlColPanel;

public class CounterPropertySet extends BasePropertySet {

	public CounterPropertySet(FormDescription formDescription,
			ControlTitle controlTitle) {
		super(formDescription, controlTitle);
	}

	@Override
	public CtrlColPanel getItemPropertyPanel(ControlTitle selTitle,
			UDColName propertyName) {
//		if (selTitle.getType() == ControlTypes.COUNTER) {
//			System.out.println("counter");
//		}
		switch (propertyName) {
		case Plot:
		case Autoclear:
		case ReadOnly:
		case AutoUpdate:
			return null;
//		case Topic:
//			return new BooleanCtrlColPanel(selTitle, new );
		}
		return super.getItemPropertyPanel(selTitle, propertyName);
	}

}
