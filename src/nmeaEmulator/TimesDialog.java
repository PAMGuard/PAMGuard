package nmeaEmulator;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class TimesDialog extends PamDialog {

	private NMEAEmulatorParams params;

	private static TimesDialog singleInstance;

	private JCheckBox useAll, repeat;

	private JTextField startTime, endTime;

	private TimesDialog(Window parentFrame) {
		super(parentFrame, "Simulation Times", false);
		JPanel p = new JPanel(new GridBagLayout());
		p.setBorder(new TitledBorder("Simulation period"));
		GridBagConstraints c = new PamGridBagContraints();
		c.gridwidth = 1;
		c.gridx = 1;
		addComponent(p, useAll = new JCheckBox("Use all data in database"), c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		PamDialog.addComponent(p, new JLabel("Start Time ", SwingConstants.RIGHT), c);
		c.gridx++;
		PamDialog.addComponent(p, startTime = new JTextField(20), c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		PamDialog.addComponent(p, new JLabel("End Time ", SwingConstants.RIGHT), c);
		c.gridx++;
		PamDialog.addComponent(p, endTime = new JTextField(20), c);
		c.gridx = 1;
		c.gridy++;
		addComponent(p, repeat = new JCheckBox("repeat"), c);

		setDialogComponent(p);
	}

	public static NMEAEmulatorParams showDialog(Window parentFrame,
			NMEAEmulatorParams params) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame) {
			singleInstance = new TimesDialog(parentFrame);
		}
		singleInstance.params = params;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.params;
	}

	private void setParams() {
//		useAll.setSelected(params.useAll);
//		startTime.setText(PamCalendar.formatDBDateTime(params.startTime));
//		endTime.setText(PamCalendar.formatDBDateTime(params.endTime));
		repeat.setSelected(params.repeat);
	}

	@Override
	public void cancelButtonPressed() {
		params = null;
	}

	@Override
	public boolean getParams() {
//		params.useAll = useAll.isSelected();
//		params.startTime = PamCalendar.msFromDateString(startTime.getText());
//		if (params.startTime < 0) {
//			return showWarning("Invalid end time string");
//		}
//		params.endTime = PamCalendar.msFromDateString(endTime.getText());
//		if (params.endTime < 0) {
//			return showWarning("Invalid start time string");
//		}
//		if (params.endTime <= params.startTime) {
//			return showWarning("The end time is before the start time");
//		}
		params.repeat = repeat.isSelected();
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
