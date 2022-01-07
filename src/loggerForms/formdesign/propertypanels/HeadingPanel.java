package loggerForms.formdesign.propertypanels;

import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import loggerForms.BearingTypes;
import loggerForms.HeadingTypes;
import loggerForms.ItemInformation;
import loggerForms.PropertyTypes;
import loggerForms.UDColName;
import loggerForms.formdesign.ControlTitle;
import loggerForms.formdesign.FormEditor;
import loggerForms.formdesign.FormEditor.EditNotify;

public class HeadingPanel extends PropertyPanel {

	private JComboBox<String> fieldNames;
	
	private JComboBox<String> headingType;
	
	private JTextField arrowLength;
	
	public HeadingPanel(FormEditor formEditor, PropertyTypes propertyType) {
		super(formEditor, propertyType);
		addItem(new JLabel(" Use "));
		addItem(fieldNames = new JComboBox<String>());
		addItem(new JLabel(" as "));
		addItem(headingType = new JComboBox<String>());
		addItem(new JLabel(" heading; arrow length "));
		addItem(arrowLength = new JTextField(3)); 
		addItem(new JLabel(" pixs"));
		
		for (HeadingTypes hT:HeadingTypes.values()) {
			headingType.addItem(hT.toString());
		}
		fillComboBox();
	}
	/* (non-Javadoc)
	 * @see loggerForms.formdesign.propertypanels.PropertyPanel#notifyChanges(loggerForms.formdesign.FormEditor.EditNotify)
	 */
	@Override
	public void notifyChanges(EditNotify notifyType) {
		fillComboBox();
	}

	/**
	 * Fill the combo box with a list of all control names that have 
	 * numeric data which might be used for bearing information. 
	 */
	private void fillComboBox() {
		String currentValue = (String) fieldNames.getSelectedItem();
		fieldNames.removeAllItems();
		ArrayList<ControlTitle> ctrlTitles = formEditor.getControlTitles();
		for (ControlTitle title:ctrlTitles) {
			if (title.getType() == null) {
				continue;
			}
			if (title.getType().isNumeric()) {
				fieldNames.addItem(title.getItemInformation().getStringProperty(UDColName.Title.toString()));
			}
		}
		if (currentValue != null) {
			fieldNames.setSelectedItem(currentValue);
		}
		else {
//			pushProperty();
		}
	}
	@Override
	public void propertyEnable(boolean enabled) {
		fieldNames.setEnabled(enabled);
		headingType.setEnabled(enabled);
	}
	@Override
	public void pushProperty(ItemInformation itemInformation) {
		super.pushProperty(itemInformation);
		if (itemInformation == null) {
			return;
		}
		try {
			fieldNames.setSelectedItem(itemInformation.getStringProperty(UDColName.Title.toString()));
		}
		catch (Exception e) {};
		try {
			headingType.setSelectedItem(itemInformation.getStringProperty(UDColName.PostTitle.toString()));
		}
		catch (Exception e) {};
		Integer len = itemInformation.getIntegerProperty(UDColName.Length.toString());
		if (len != null) {
			arrowLength.setText(len.toString());
		}
	}
	@Override
	public ItemInformation fetchProperty(ItemInformation itemInformation) {
		itemInformation =  super.fetchProperty(itemInformation);
		if (itemInformation == null) {
			return null;
		}
		
		Integer len = null;
		try {
			len = Integer.valueOf(arrowLength.getText());
		}
		catch (NumberFormatException e) {};
		itemInformation.setProperty(UDColName.Length.toString(), len);
		itemInformation.setProperty(UDColName.Title.toString(), fieldNames.getSelectedItem());
		itemInformation.setProperty(UDColName.PostTitle.toString(), headingType.getSelectedItem());
				
		return itemInformation;
	}


}
