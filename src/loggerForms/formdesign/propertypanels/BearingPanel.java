package loggerForms.formdesign.propertypanels;

import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import loggerForms.BearingTypes;
import loggerForms.ItemInformation;
import loggerForms.PropertyTypes;
import loggerForms.UDColName;
import loggerForms.formdesign.ControlTitle;
import loggerForms.formdesign.FormEditor;
import loggerForms.formdesign.FormEditor.EditNotify;

public class BearingPanel extends PropertyPanel {

	private JComboBox<String> fieldNames;
	
	private JComboBox<String> bearingType;
	
	public BearingPanel(FormEditor formEditor, PropertyTypes propertyType) {
		super(formEditor, propertyType);
		addItem(new JLabel(" Use "));
		addItem(fieldNames = new JComboBox<String>());
		addItem(new JLabel(" as "));
		addItem(bearingType = new JComboBox<String>());
		addItem(new JLabel(" bearing "));
		
		for (BearingTypes bT:BearingTypes.values()) {
			bearingType.addItem(bT.toString());
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
		Object selItem = fieldNames.getSelectedItem();
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
		if (selItem != null) {
			fieldNames.setSelectedItem(selItem);
		}
	}

	@Override
	public void propertyEnable(boolean enabled) {
		fieldNames.setEnabled(enabled);
		bearingType.setEnabled(enabled);
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
			bearingType.setSelectedItem(itemInformation.getStringProperty(UDColName.Topic.toString()));
		}
		catch (Exception e) {};

	}
	@Override
	public ItemInformation fetchProperty(ItemInformation itemInformation) {
		itemInformation =  super.fetchProperty(itemInformation);
		if (itemInformation == null) {
			return null;
		}
		
		itemInformation.setProperty(UDColName.Title.toString(), fieldNames.getSelectedItem().toString());
		itemInformation.setProperty(UDColName.Topic.toString(), bearingType.getSelectedItem().toString());
				
		return itemInformation;
	}


}
