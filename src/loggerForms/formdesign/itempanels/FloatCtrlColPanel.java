package loggerForms.formdesign.itempanels;

import java.awt.Component;

import javax.swing.JTextField;

import loggerForms.ItemInformation;
import loggerForms.UDColName;
import loggerForms.formdesign.ControlTitle;

public class FloatCtrlColPanel extends CtrlColPanel {

	private JTextField floatField;
	
	public FloatCtrlColPanel(ControlTitle controlTitle, UDColName propertyName) {
		super(controlTitle, propertyName);
		floatField = new JTextField(5);
	}


	@Override
	public Component getPanel() {
		return floatField;
	}

	@Override
	public void pushProperty(ItemInformation itemDescription) {
		Float floatVal = itemDescription.getFloatProperty(this.propertyName.toString());
		if (floatVal != null) {
			floatField.setText(floatVal.toString());
		}
		else {
			floatField.setText("");
		}
	}

	@Override
	public boolean fetchProperty(ItemInformation itemDescription) {
		try {
			float iVal = Float.valueOf(floatField.getText());
			itemDescription.setProperty(this.propertyName.toString(), new Float(iVal));
		}
		catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

}
