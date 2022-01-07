package loggerForms.formdesign.itempanels;

import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.JPanel;
import javax.swing.JTextField;

import loggerForms.ItemInformation;
import loggerForms.UDColName;
import loggerForms.formdesign.ControlTitle;

public class TextCtrlColPanel extends CtrlColPanel {

	private int length;
	
	protected JTextField textField;

	private JPanel outerPanel;

	public TextCtrlColPanel(ControlTitle controlTitle, UDColName propertyName, int length) {
		super(controlTitle, propertyName);
		this.length = length;
		textField = new JTextField(length/2);
		outerPanel = new JPanel(new FlowLayout());
		outerPanel.add(textField);
	}

	@Override
	public Component getPanel() {
		return outerPanel;
	}

	@Override
	public void pushProperty(ItemInformation itemDescription) {
		textField.setText(itemDescription.getStringProperty(propertyName.toString()));
	}

	@Override
	public boolean fetchProperty(ItemInformation itemDescription) {
		itemDescription.setProperty(propertyName.toString(), textField.getText());
		return true;
	}

}
