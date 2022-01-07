package loggerForms.formdesign.propertypanels;

import javax.swing.JLabel;
import javax.swing.JTextField;

import loggerForms.ItemInformation;
import loggerForms.PropertyTypes;
import loggerForms.UDColName;
import loggerForms.formdesign.FormEditor;

public class AutoAlertPanel extends PropertyPanel {

	private JTextField alertTime;

	public AutoAlertPanel(FormEditor formEditor, PropertyTypes propertyType) {
		super(formEditor, propertyType);
		addItem(new JLabel("Alert interval ", JLabel.RIGHT));
		addItem(alertTime = new JTextField(4));
		addItem(new JLabel(" (seconds) ", JLabel.LEFT));
	}

	/* (non-Javadoc)
	 * @see loggerForms.formdesign.propertypanels.PropertyPanel#pushProperty(loggerForms.ItemInformation)
	 */
	@Override
	public void pushProperty(ItemInformation itemInformation) {
		// TODO Auto-generated method stub
		super.pushProperty(itemInformation);
	}

	/* (non-Javadoc)
	 * @see loggerForms.formdesign.propertypanels.PropertyPanel#fetchProperty(loggerForms.ItemInformation)
	 */
	@Override
	public ItemInformation fetchProperty(ItemInformation itemInformation) {
		itemInformation = super.fetchProperty(itemInformation);
		if (itemInformation == null) return null;
		int length = 0;
		String str = alertTime.getText();
		if (str != null) {
			try {
				length = Integer.valueOf(str);
			}
			catch (NumberFormatException e) {
			}
		}

		itemInformation.setProperty(UDColName.Length.toString(), length);
		return itemInformation;
	}

	@Override
	public void propertyEnable(boolean enabled) {
		alertTime.setEnabled(enabled);
	}

}
