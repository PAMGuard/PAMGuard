package loggerForms.formdesign.propertypanels;

import javax.swing.JLabel;
import javax.swing.JTextField;

import loggerForms.ItemInformation;
import loggerForms.PropertyTypes;
import loggerForms.UDColName;
import loggerForms.formdesign.FormEditor;

public class TextPanel extends PropertyPanel {

	private JTextField textField;
	private UDColName colName;
	
	public TextPanel(FormEditor formEditor, PropertyTypes propertyType, UDColName colName, String preText, String postText) {
		super(formEditor, propertyType);
		this.colName = colName;
		if (preText != null) {
			addItem(new JLabel(preText));
		}
		textField = new JTextField(colName.getStringLength()/2);
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
			String str = itemInformation.getStringProperty(colName.toString());
			textField.setText(str);
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
		itemInformation.setProperty(colName.toString(), str);
		return itemInformation;
	}

	@Override
	public void propertyEnable(boolean enabled) {
		textField.setEnabled(enabled);
	}

}
