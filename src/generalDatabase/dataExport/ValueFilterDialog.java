package generalDatabase.dataExport;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class ValueFilterDialog extends PamDialog implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	private JCheckBox useMin, useMax;
	private JTextField minValue, maxValue;
	private ValueFilterParams valueFilterParams;

	private ValueFilterDialog(ValueFilter valueFilter) {
		super(null, valueFilter.getColumnName(), false);
		valueFilterParams = valueFilter.valueFilterParams.clone();
		
		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(new TitledBorder("Value Range"));
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		addComponent(mainPanel, new JLabel("Minimum Value ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(mainPanel, useMin = new JCheckBox(), c);
		c.gridx++;
		addComponent(mainPanel, minValue = new JTextField(valueFilterParams.getTextFieldLength()), c);
		c.gridy++;
		c.gridx = 0;
		addComponent(mainPanel, new JLabel("Maximum Value ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(mainPanel, useMax = new JCheckBox(), c);
		c.gridx++;
		addComponent(mainPanel, maxValue = new JTextField(valueFilterParams.getTextFieldLength()), c);
		
		useMin.addActionListener(this);
		useMax.addActionListener(this);
		
		setParams();
		
		setDialogComponent(mainPanel);
	}
	
	public static ValueFilterParams showDialog(Window parentWindow, ValueFilter valueFilter, Point point) {
		ValueFilterDialog valueFilterDialog = new ValueFilterDialog(valueFilter);
		valueFilterDialog.setLocation(point);
		valueFilterDialog.setVisible(true);
		return valueFilterDialog.valueFilterParams;
	}

	private void setParams() {
		useMin.setSelected(valueFilterParams.isUseMin());
		useMax.setSelected(valueFilterParams.isUseMax());
		minValue.setText(valueFilterParams.getMinValue());
		maxValue.setText(valueFilterParams.getMaxValue());
		enableControls();
	}

	@Override
	public void cancelButtonPressed() {
		valueFilterParams = null;
	}

	@Override
	public boolean getParams() {
		valueFilterParams.setUseMin(useMin.isSelected());
		valueFilterParams.setUseMax(useMax.isSelected());
			if (valueFilterParams.isUseMin()) {
				if (valueFilterParams.setMinValue(minValue.getText()) == false) {
					return showWarning("Error in minimum value");
				}
			}
			if (valueFilterParams.isUseMax()) {
				if (valueFilterParams.setMaxValue(maxValue.getText()) == false) {
					return showWarning("Error in maximum value");
				}
			}
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		enableControls();
	}

	private void enableControls() {
		minValue.setEnabled(useMin.isSelected());
		maxValue.setEnabled(useMax.isSelected());
	}
	

}
