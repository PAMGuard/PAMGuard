package loggerForms.formdesign.propertypanels;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import loggerForms.ItemInformation;
import loggerForms.PropertyDescription;
import loggerForms.PropertyTypes;
import loggerForms.UDColName;
import loggerForms.PropertyDescription.hotkeys;
import loggerForms.formdesign.FormEditor;

public class HotkeyPanel extends PropertyPanel {

	private JComboBox<String> hotKeys;

	public HotkeyPanel(FormEditor formEditor, PropertyTypes propertyType) {
		super(formEditor, propertyType);
		addItem(hotKeys = new JComboBox<String>());
		addItem(new JLabel("Will automatically open the form"));
		addItem(new JLabel(" or create a new subtab when pressed."));

		for (PropertyDescription.hotkeys key:hotkeys.values()) {
			this.hotKeys.addItem(key.toString());
		}
	}

	@Override
	public void propertyEnable(boolean enabled) {
		hotKeys.setEnabled(enabled);
	}

	@Override
	public void pushProperty(ItemInformation itemInformation) {
		super.pushProperty(itemInformation);
		if (itemInformation == null) {
			return;
		}
		hotKeys.setSelectedItem(itemInformation.getStringProperty(UDColName.Title.toString()));
	}

	@Override
	public ItemInformation fetchProperty(ItemInformation itemInformation) {	 
		itemInformation = super.fetchProperty(itemInformation);
		if (itemInformation == null) {
			return null;
		}
		itemInformation.setProperty(UDColName.Title.toString(), hotKeys.getSelectedItem());
		return itemInformation;
	}

}
