package loggerForms.formdesign.itempanels;

import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import loggerForms.ItemInformation;
import loggerForms.UDColName;
import loggerForms.formdesign.ControlTitle;

public class IntegerCtlrColPanel extends CtrlColPanel {

	private JTextField intValue;
	
	private JPanel intPanel;
	
	public IntegerCtlrColPanel(ControlTitle controlTitle,
			UDColName propertyName, String hint) {
		super(controlTitle, propertyName);
		intPanel = new JPanel(new FlowLayout());
		intPanel.add(intValue = new JTextField(5));
		if (hint != null) {
			intPanel.add(new JLabel(hint));
		}
	}

	@Override
	public Component getPanel() {
		return intPanel;
	}

	@Override
	public void pushProperty(ItemInformation itemDescription) {
		Integer intVal = itemDescription.getIntegerProperty(this.propertyName.toString());
		if (intVal != null) {
			intValue.setText(intVal.toString());
		}
		else {
			intValue.setText("");
		}
	}

	@Override
	public boolean fetchProperty(ItemInformation itemDescription) {
		try {
			int iVal = Integer.valueOf(intValue.getText());
			itemDescription.setProperty(this.propertyName.toString(), new Integer(iVal));
		}
		catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

}
