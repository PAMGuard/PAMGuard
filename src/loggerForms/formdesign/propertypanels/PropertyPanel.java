package loggerForms.formdesign.propertypanels;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import loggerForms.ItemInformation;
import loggerForms.PropertyTypes;
import loggerForms.UDColName;
import loggerForms.formdesign.FormEditor;
import loggerForms.formdesign.FormEditor.EditNotify;

/**
 * Base class for strips of information to go with each 
 * property of a form
 * @author Doug
 *
 */
public abstract class PropertyPanel {

	private PropertyTypes propertyType;
	
	private JPanel propPanel;
	
	private JCheckBox useProperty;

	protected FormEditor formEditor;

	public PropertyPanel(FormEditor formEditor, PropertyTypes propertyType) {
		this.formEditor = formEditor;
		this.propertyType = propertyType;
		propPanel = new JPanel(new FlowLayout());
		propPanel.add(useProperty = new JCheckBox());
		useProperty.setToolTipText(propertyType.getDescription());
		useProperty.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				propertyEnable(useProperty.isSelected());
			}

		});
	}

	public Component getPanel() {
		return propPanel;
	}
	
	protected void addItem(Component component) {
		propPanel.add(component);
	}

	public abstract void propertyEnable(boolean enabled);
	
	/**
	 * Set the control content on the display
	 * @param itemInformation
	 */
	public void pushProperty(ItemInformation itemInformation) {
		useProperty.setSelected(itemInformation != null);
	}

	/**
	 * Fetch properties and put them back into the Hash table in the 
	 * form editor. 
	 * @param itemInformation
	 * @return true if successful, false if inconsistent or missing data. 
	 */
	public ItemInformation fetchProperty(ItemInformation itemInformation) {
		boolean sel = useProperty.isSelected();
		if (sel == false) {
			formEditor.setFormProperty(propertyType, null);
			return null;
		}
		if (itemInformation == null) {
			itemInformation = new ItemInformation(formEditor.getFormDescription());
			itemInformation.setProperty(UDColName.Type.toString(), propertyType.toString());
		}
		formEditor.setFormProperty(propertyType, itemInformation);
		
		return itemInformation;
	}

	/**
	 * @return the useProperty
	 */
	public JCheckBox getUseProperty() {
		return useProperty;
	}

	/**
	 * @return the propertyType
	 */
	public PropertyTypes getPropertyType() {
		return propertyType;
	}

	public void notifyChanges(EditNotify notifyType) {	}
	
	
}
