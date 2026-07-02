package loggerForms.formdesign.propertypanels;

import loggerForms.ItemInformation;
import loggerForms.PropertyTypes;
import loggerForms.UDColName;
import loggerForms.formdesign.FormEditor;

public class UDPInputPanel extends TextPanel {

	public UDPInputPanel(FormEditor formEditor, PropertyTypes propertyType) {
		super(formEditor, propertyType, UDColName.Topic, "UDP Topic", "identifier");
	}

	@Override
	public void propertyEnable(boolean enabled) {
		super.propertyEnable(enabled);
	}

	@Override
	public void pushProperty(ItemInformation itemInformation) {
		if (itemInformation == null) {
			itemInformation = new ItemInformation(formEditor.getFormDescription());
		}
		if (itemInformation != null) {
			String str = itemInformation.getStringProperty(UDColName.Topic.toString());
			if (str == null) {
				itemInformation.setProperty(UDColName.Topic.toString(), "LoggerButton");	
			}
		}
		// TODO Auto-generated method stub
		super.pushProperty(itemInformation);
	}

}
