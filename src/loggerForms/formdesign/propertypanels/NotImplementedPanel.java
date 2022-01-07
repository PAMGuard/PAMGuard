package loggerForms.formdesign.propertypanels;

import javax.swing.JLabel;

import loggerForms.PropertyTypes;
import loggerForms.formdesign.FormEditor;

public class NotImplementedPanel extends PropertyPanel {

	public NotImplementedPanel(FormEditor formEditor, PropertyTypes propertyType) {
		super(formEditor, propertyType);
		addItem(new JLabel("Feature not yet implemented"));
		getUseProperty().setEnabled(false);
	}

	@Override
	public void propertyEnable(boolean enabled) {
	}

}
