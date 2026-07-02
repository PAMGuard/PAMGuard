package loggerForms.formdesign.propertypanels;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import loggerForms.ItemInformation;
import loggerForms.PropertyTypes;
import loggerForms.formdesign.FormEditor;

public class BooleanPanel extends PropertyPanel {

	public BooleanPanel(FormEditor formEditor, PropertyTypes propertyType, String hint) {
		super(formEditor, propertyType);
		if (hint != null) {
			addItem(new JLabel(hint));
		}
	}

	@Override
	public void propertyEnable(boolean enabled) {
		System.out.println("Propertey enabled " + enabled);
	}

	@Override
	public void pushProperty(ItemInformation itemInformation) {
		// TODO Auto-generated method stub
		super.pushProperty(itemInformation);
	}

	@Override
	public JCheckBox getUseProperty() {
//		if (this.getPropertyType() == PropertyTypes.NOCANCEL || this.getPropertyType() == PropertyTypes.NOCLEAR) {
//			JCheckBox cb = super.getUseProperty();
//			boolean isSel = cb.isSelected();
//			System.out.printf("Property %s selection is %s\n", this.getPropertyType(), isSel ? "TRUE" : "FALSE");
//		}
		return super.getUseProperty();
	}
	
	

}
