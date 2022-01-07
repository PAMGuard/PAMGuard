package loggerForms.formdesign.propertypanels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import loggerForms.HeadingTypes;
import loggerForms.ItemInformation;
import loggerForms.PropertyTypes;
import loggerForms.RangeTypes;
import loggerForms.RangeUnitTypes;
import loggerForms.UDColName;
import loggerForms.formdesign.ControlTitle;
import loggerForms.formdesign.FormEditor;
import loggerForms.formdesign.FormEditor.EditNotify;

public class RangePanel extends PropertyPanel {

	private JComboBox<String> fieldNames;

	private JComboBox<RangeTypes> rangeType;

	private JComboBox<RangeUnitTypes> rangeUnits;

	private JTextField fixedValue;

	public RangePanel(FormEditor formEditor, PropertyTypes propertyType) {
		super(formEditor, propertyType);
		// TODO Auto-generated constructor stub
		addItem(new JLabel(" Use "));
		addItem(rangeType = new JComboBox<RangeTypes>());
		addItem(new JLabel(" range of "));
		addItem(fieldNames = new JComboBox<String>());
		addItem(fixedValue = new JTextField(5));
		addItem(rangeUnits = new JComboBox<RangeUnitTypes>());

		for (RangeUnitTypes rU:RangeUnitTypes.values()) {
			rangeUnits.addItem(rU);
		}
		for (RangeTypes rT:RangeTypes.values()) {
			rangeType.addItem(rT);
		}
		fillComboBox();

		rangeType.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {	
				propertyEnable(getUseProperty().isSelected());
			}
		});
		rangeUnits.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {	
				propertyEnable(getUseProperty().isSelected());
			}
		});

		propertyEnable(false);
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
		String currentItem = (String) fieldNames.getSelectedItem();
		fieldNames.removeAllItems();
		ArrayList<ControlTitle> ctrlTitles = formEditor.getControlTitles();
		for (ControlTitle title:ctrlTitles) {
			if (title.getType() == null) {
				continue;
			}
			if (title.getType().isNumeric()) {
				String tit = title.getItemInformation().getStringProperty(UDColName.Title.toString());
				if (tit != null) {
					fieldNames.addItem(tit.trim());
				}
				else {
					
				}
			}
		}
		if (currentItem != null) {
			fieldNames.setSelectedItem(currentItem);
		}
	}

	/* (non-Javadoc)
	 * @see loggerForms.formdesign.propertypanels.PropertyPanel#propertyEnable(boolean)
	 */
	@Override
	public void propertyEnable(boolean enabled) {
		RangeTypes type = (RangeTypes) rangeType.getSelectedItem();
		boolean fixed = (type == RangeTypes.FIXED);
		fieldNames.setVisible(fixed == false);
		fixedValue.setVisible(fixed == true);

		fieldNames.setEnabled(enabled);
		rangeType.setEnabled(enabled);
		rangeUnits.setEnabled(enabled);
		fixedValue.setEnabled(enabled);
	}

	@Override
	public void pushProperty(ItemInformation itemInformation) {
		super.pushProperty(itemInformation);
		if (itemInformation == null) {
			return;
		}
		fillComboBox();
		String title = itemInformation.getStringProperty(UDColName.Title.toString()).trim();
		if (title != null) {
			fieldNames.setSelectedItem(title);
		}
		String topic = itemInformation.getStringProperty(UDColName.Topic.toString());
		RangeTypes selRangeType = RangeTypes.VARIABLE;
		if (topic != null) {
			try {
				selRangeType = RangeTypes.valueOf(topic);
			}
			catch (Exception e) {
				selRangeType = RangeTypes.VARIABLE;
			}
		}
		if (selRangeType != null) {
			this.rangeType.setSelectedItem(selRangeType);
		}
		String unit = itemInformation.getStringProperty(UDColName.PostTitle.toString());
		if (unit != null) {
			try {
				RangeUnitTypes rangeType = RangeUnitTypes.valueOf(unit);
				if (rangeType != null) {
					this.rangeUnits.setSelectedItem(rangeType);
				}
			}
			catch (Exception e) {

			}
		}
		Integer fixedLen = itemInformation.getIntegerProperty(UDColName.Length.toString());
		if (fixedLen != null) {
			fixedValue.setText(fixedLen.toString());
		}
		propertyEnable(true);
	}

	@Override
	public ItemInformation fetchProperty(ItemInformation itemInformation) {
		itemInformation = super.fetchProperty(itemInformation);
		if (itemInformation == null) {
			return null;
		}
		itemInformation.setProperty(UDColName.Title.toString(), fieldNames.getSelectedItem());
		boolean isFixed = rangeType.getSelectedItem() == RangeTypes.FIXED;
		itemInformation.setProperty(UDColName.Topic.toString(), 
				isFixed ? RangeTypes.FIXED.toString() : RangeTypes.VARIABLE.toString());
		itemInformation.setProperty(UDColName.PostTitle.toString(), rangeUnits.getSelectedItem().toString());
		itemInformation.setProperty(UDColName.Length.toString(), fieldNames.getSelectedItem());
		return itemInformation;
	}



}
