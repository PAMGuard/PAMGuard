package loggerForms.formdesign.itempanels;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JPanel;

import loggerForms.ItemInformation;
import loggerForms.UDColName;
import loggerForms.formdesign.ControlTitle;

public abstract class CtrlColPanel {
		
	protected ControlTitle controlTitle;
	
	protected UDColName propertyName;

	public CtrlColPanel(ControlTitle controlTitle, UDColName propertyName) {
		super();
		this.controlTitle = controlTitle;
		this.propertyName = propertyName;
	}
	
	public String getTitle() {
		return propertyName.toString();
	}

	/**
	 * Get panel to display in larger ControlPropertySheet
	 * @return  AWT component
	 */
	public abstract Component getPanel();
	
	/**
	 * Push properties from the item description onto the 
	 * visible control
	 * @param itemDescription
	 */
	public abstract void pushProperty(ItemInformation itemDescription);
	
	/**
	 * Fetch properties from the visible control back into the item description. 
	 * @param itemDescription
	 * @return true if sucessful, false if a problem. 
	 */
	public abstract boolean fetchProperty(ItemInformation itemDescription);

	/**
	 * @return the controlTitle
	 */
	public ControlTitle getControlTitle() {
		return controlTitle;
	}
}
