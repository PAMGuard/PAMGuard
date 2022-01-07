package loggerForms.formdesign;

import loggerForms.FormDescription;
import loggerForms.FormsControl;
import loggerForms.UDColName;
import loggerForms.formdesign.controlpropsets.BasePropertySet;
import loggerForms.formdesign.itempanels.CtrlColPanel;
import loggerForms.formdesign.itempanels.SubformTopicPanel;

public class SubformPropertySet extends BasePropertySet {

	public SubformPropertySet(FormDescription formDescription,
			ControlTitle controlTitle) {
		super(formDescription, controlTitle);
	}

	@Override
	public CtrlColPanel getItemPropertyPanel(ControlTitle selTitle,
			UDColName propertyName) {
		switch (propertyName) {
		case Topic:
			return new SubformTopicPanel(formDescription, selTitle, propertyName);
		case Autoclear:
		case ReadOnly:
			return null;
//		case Send_Control_Name:
//			return new SubFormCrossRefPanel(formDescription, selTitle, propertyNa)
		}
		return super.getItemPropertyPanel(selTitle, propertyName);
	}

}
