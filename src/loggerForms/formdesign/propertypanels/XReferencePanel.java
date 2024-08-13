package loggerForms.formdesign.propertypanels;

import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import loggerForms.ItemInformation;
import loggerForms.PropertyTypes;
import loggerForms.UDColName;
import loggerForms.controlDescriptions.ControlTypes;
import loggerForms.formdesign.ControlTitle;
import loggerForms.formdesign.FormEditor;
import loggerForms.formdesign.FormEditor.EditNotify;

/**
 * Property form for a cross reference sontrol. 
 * @author dg50
 *
 */
public class XReferencePanel extends PropertyPanel {

	private ControlTypes controlType;

	private JComboBox<String> fieldNames;
	
	public XReferencePanel(FormEditor formEditor, PropertyTypes propertyType, ControlTypes controlType) {
		super(formEditor, propertyType);
		this.controlType = controlType;
		addItem(new JLabel(" Use "));
		addItem(fieldNames = new JComboBox<String>());
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
			if (title.getType() == controlType) {
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
	}
	
	@Override
	public ItemInformation fetchProperty(ItemInformation itemInformation) {
		itemInformation =  super.fetchProperty(itemInformation);
		if (itemInformation == null) {
			return null;
		}
		
		itemInformation.setProperty(UDColName.Title.toString(), fieldNames.getSelectedItem());
				
		return itemInformation;
	}


}
