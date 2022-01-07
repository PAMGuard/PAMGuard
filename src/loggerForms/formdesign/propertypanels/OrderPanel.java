package loggerForms.formdesign.propertypanels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import loggerForms.FormDescription;
import loggerForms.ItemInformation;
import loggerForms.PropertyTypes;
import loggerForms.UDColName;
import loggerForms.formdesign.FormEditor;

public class OrderPanel extends PropertyPanel {

	private JComboBox<Integer> orderList;
	
	public OrderPanel(FormEditor formEditor) {
		super(formEditor, PropertyTypes.ORDER);
		addItem(new JLabel(" Form Order "));
		addItem(orderList = new JComboBox<Integer>());
		orderList.setToolTipText("Order position of form on the display");
		getUseProperty().setEnabled(false);
		getUseProperty().setSelected(true);
		fillList();
		orderList.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				orderChanged();
			}
		});
	}

	/**
	 * The order numbers for the forms. In principle, these
	 * are just 1 - n, so can fill easily. 
	 */
	private void fillList() {
		int n = formEditor.getFormsControl().getNumFormDescriptions();
		for (int i = 0; i < n; i++) {
			orderList.addItem(new Integer(i+1));
		}
	}

	protected void orderChanged() {
		// find the form with the new order and set it's order to current order. 
		Integer newOrder = (Integer) orderList.getSelectedItem();
		if (newOrder == null) {
			return;
		}
		// find the form that currently has that order. 
		FormDescription otherForm = formEditor.getFormsControl().findFormByOrder(newOrder);
		FormDescription thisForm = formEditor.getFormDescription();
		if (otherForm == thisForm) {
			return; // nothing to do !
		}
		// work out which order this form currently holds. 
		Integer thisOrder = formEditor.getFormDescription().getFormOrderProperty();
		if (otherForm != null && thisOrder != null) {
			otherForm.setFormOrderProperty(thisOrder);
			otherForm.setNeedsUDFSave(true);
		}
		thisForm.setFormOrderProperty(newOrder);
	}
	
	

	@Override
	public void propertyEnable(boolean enabled) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see loggerForms.formdesign.propertypanels.PropertyPanel#pushProperty(loggerForms.ItemInformation)
	 */
	@Override
	public void pushProperty(ItemInformation itemInformation) {
		// get the order number of this form. It should be in the list, 
		// but check and add it if necessary. 
		int formOrder = 0;
		if (itemInformation != null) {
			Integer lVal = itemInformation.getIntegerProperty(UDColName.Length.toString());
			if (lVal != null) {
				formOrder = lVal;
			}
		}
		else {
			// work out the order of the form from the forms list. 
			int formInd = formEditor.getFormsControl().getFormIndex(formEditor.getFormDescription());
			if (formInd >= 0) {
				formOrder = formInd+1;
			}
		}
		if (formOrder == 0) {
			return;
		}
		orderList.setSelectedItem(formOrder);
	}

	/* (non-Javadoc)
	 * @see loggerForms.formdesign.propertypanels.PropertyPanel#fetchProperty(loggerForms.ItemInformation)
	 */
	@Override
	public ItemInformation fetchProperty(ItemInformation itemInformation) {
		ItemInformation itemInfo = super.fetchProperty(itemInformation);
		Integer selItem = (Integer) orderList.getSelectedItem();
//		if (selItem != null) {
			itemInfo.setProperty(UDColName.Length.toString(), selItem);
//		}
		return itemInfo;
	}

}
