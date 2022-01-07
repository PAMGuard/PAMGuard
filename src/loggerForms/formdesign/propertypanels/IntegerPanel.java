package loggerForms.formdesign.propertypanels;

import javax.swing.JLabel;
import javax.swing.JTextField;

import loggerForms.ItemInformation;
import loggerForms.PropertyTypes;
import loggerForms.UDColName;
import loggerForms.formdesign.FormEditor;

public class IntegerPanel extends PropertyPanel {

	private JTextField textField;
	private UDColName colName;
	
	public IntegerPanel(FormEditor formEditor, PropertyTypes propertyType,
			UDColName colName, String preText, String postText) {
		super(formEditor, propertyType);
		this.colName = colName;
		if (preText != null) {
			addItem(new JLabel(preText));
		}
		textField = new JTextField(5);
		addItem(textField);
		if (postText != null) {
			addItem(new JLabel(postText));
		}
	}

	/* (non-Javadoc)
	 * @see loggerForms.formdesign.propertypanels.PropertyPanel#pushProperty(loggerForms.ItemInformation)
	 */
	@Override
	public void pushProperty(ItemInformation itemInformation) {
		if (itemInformation != null) {
			Integer iVal = itemInformation.getIntegerProperty(colName.toString());
			if (iVal != null) {
				textField.setText(iVal.toString());
			}
		}
		super.pushProperty(itemInformation);
	}

	/* (non-Javadoc)
	 * @see loggerForms.formdesign.propertypanels.PropertyPanel#fetchProperty(loggerForms.ItemInformation)
	 */
	@Override
	public ItemInformation fetchProperty(ItemInformation itemInformation) {
		itemInformation = super.fetchProperty(itemInformation);
		if (itemInformation == null) return null;
		String str = textField.getText();
		Integer iVal = null;
		if (str != null) {
			try {
				iVal = Integer.valueOf(str);
			}
			catch (NumberFormatException e) {
				iVal = null;
			}
		}
		itemInformation.setProperty(colName.toString(), iVal);
		return itemInformation;
	}

	@Override
	public void propertyEnable(boolean enabled) {
		textField.setEnabled(enabled);
	}


}
