package loggerForms.formdesign.itempanels;

import java.awt.Component;

import javax.swing.JCheckBox;

import loggerForms.ItemInformation;
import loggerForms.UDColName;
import loggerForms.formdesign.ControlTitle;

public class BooleanCtrlColPanel extends CtrlColPanel {

	private JCheckBox checkBox;
	
	public BooleanCtrlColPanel(ControlTitle controlTitle,
			UDColName propertyName) {
		super(controlTitle, propertyName);
		checkBox = new JCheckBox();
	}

	@Override
	public Component getPanel() {
		return checkBox;
	}

	@Override
	public void pushProperty(ItemInformation itemDescription) {
		Boolean prop = itemDescription.getBooleanProperty(propertyName.toString());
		if (prop == null) {
			prop = false;
		}
		checkBox.setSelected(prop);
	}

	@Override
	public boolean fetchProperty(ItemInformation itemDescription) {
		itemDescription.setProperty(propertyName.toString(), checkBox.isSelected());
		return true;
	}

}
