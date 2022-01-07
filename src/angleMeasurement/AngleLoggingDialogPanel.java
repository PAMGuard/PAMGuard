package angleMeasurement;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class AngleLoggingDialogPanel {

	private JPanel panel;
	
	private JRadioButton logNone, logAll, logTimed;
	private JRadioButton logHeld;
	private JRadioButton timedEven, timedRandom;
	private JTextField logTime;
	
	private AngleLoggingParameters angleLoggingParameters;
	
	
	public AngleLoggingDialogPanel() {
		super();
		panel = new JPanel();
		panel.setBorder(new TitledBorder("Angle logging"));
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.gridwidth = 3;
		PamDialog.addComponent(panel, logNone = new JRadioButton("Log Nothing"), c);
		c.gridy++;
		PamDialog.addComponent(panel, logAll = new JRadioButton("Log All"), c);
		c.gridy++;
		PamDialog.addComponent(panel, logHeld = new JRadioButton("Log Held Angles"), c);
		c.gridy++;
		PamDialog.addComponent(panel, logTimed = new JRadioButton("Log Held and on timer"), c);
		c.gridy++;

		c.gridwidth = 1;
		PamDialog.addComponent(panel, new JLabel("Log interval "), c);
		c.gridx++;
		PamDialog.addComponent(panel, logTime = new JTextField(4), c);
		c.gridx++;
		PamDialog.addComponent(panel, new JLabel(" s"), c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 3;
		PamDialog.addComponent(panel, timedEven = new JRadioButton("Uniform log times"), c);
		c.gridy++;
		PamDialog.addComponent(panel, timedRandom = new JRadioButton("Random log times"), c);
		
		ButtonGroup mainGroup = new ButtonGroup();
		mainGroup.add(logNone);
		mainGroup.add(logAll);
		mainGroup.add(logHeld);
		mainGroup.add(logTimed);
		
		ButtonGroup timedGroup = new ButtonGroup();
		timedGroup.add(timedEven);
		timedGroup.add(timedRandom);
		
		ButtonListeer bl = new ButtonListeer();
		logNone.addActionListener(bl);
		logAll.addActionListener(bl);
		logHeld.addActionListener(bl);
		logTimed.addActionListener(bl);
	}

	public Component getPanel() {
		return panel;
	}
	
	public void setParams(AngleLoggingParameters angleLoggingParameters) {
		if (angleLoggingParameters != null) {
			this.angleLoggingParameters = angleLoggingParameters.clone();
		}
		else {
			this.angleLoggingParameters = new AngleLoggingParameters();
		}
		
		logNone.setSelected(this.angleLoggingParameters.logAngles == AngleLoggingParameters.LOG_NONE);
		logAll.setSelected(this.angleLoggingParameters.logAngles == AngleLoggingParameters.LOG_ALL);
		logHeld.setSelected(this.angleLoggingParameters.logAngles == AngleLoggingParameters.LOG_HELD);
		logTimed.setSelected(this.angleLoggingParameters.logAngles == AngleLoggingParameters.LOG_TIMED);
		logTime.setText(String.format("%.1f", this.angleLoggingParameters.logInterval));
		timedEven.setSelected(this.angleLoggingParameters.timedRandom == false);
		timedRandom.setSelected(this.angleLoggingParameters.timedRandom == true);
		
		enableControls();
	}
	
	public AngleLoggingParameters getParams() {
		if (angleLoggingParameters == null) {
			angleLoggingParameters = new AngleLoggingParameters();
		}
		angleLoggingParameters.logAngles = getLogType();
		angleLoggingParameters.timedRandom = timedRandom.isSelected();
		try {
			angleLoggingParameters.logInterval = Double.valueOf(logTime.getText());
		}
		catch (NumberFormatException e) {
			return null;
		}
		return angleLoggingParameters;
	}
	
	private int getLogType() {
		if (logNone.isSelected()) {
			return AngleLoggingParameters.LOG_NONE;
		}
		if (logAll.isSelected()) {
			return AngleLoggingParameters.LOG_ALL;
		}
		if (logHeld.isSelected()) {
			return AngleLoggingParameters.LOG_HELD;
		}
		if (logTimed.isSelected()) {
			return AngleLoggingParameters.LOG_TIMED;
		}
		else {
			return AngleLoggingParameters.LOG_NONE;
		}
	}
	
	private void enableControls() {
		int t = getLogType();
		logTime.setEnabled(t == AngleLoggingParameters.LOG_TIMED);
		timedEven.setEnabled(t == AngleLoggingParameters.LOG_TIMED);
		timedRandom.setEnabled(t == AngleLoggingParameters.LOG_TIMED);
	}
	
	private class ButtonListeer implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			enableControls();
		}
		
	}
}
