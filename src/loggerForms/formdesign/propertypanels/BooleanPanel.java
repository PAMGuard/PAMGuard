package loggerForms.formdesign.propertypanels;

import javax.swing.JLabel;

import loggerForms.PropertyTypes;
import loggerForms.formdesign.FormEditor;

public class BooleanPanel extends PropertyPanel {

	public BooleanPanel(FormEditor formEditor, PropertyTypes propertyType, String hint) {
		super(formEditor, propertyType);
		if (hint != null) {
			addItem(new JLabel(hint));
		}
	}

	@Override
	public void propertyEnable(boolean enabled) {
	}

}
