package whistlesAndMoans.alarm;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class WMAlarmDialog extends PamDialog {

	private static WMAlarmDialog singleInstance;
	private WMAlarmParameters wmAlarmParameters;
	
	private JTextField minFreq, maxFreq, minLength, minAmplitude;
	
	private WMAlarmDialog(Window parentFrame) {
		super(parentFrame, "Whistle moan alarm options", true);
		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(new TitledBorder("Select whistles"));
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		addComponent(mainPanel, new JLabel("Min frequency ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(mainPanel, minFreq = new JTextField(6), c);
		c.gridx++;
		addComponent(mainPanel, new JLabel(" Hz", JLabel.LEFT), c);
		c.gridx = 0;
		c.gridy++;
		addComponent(mainPanel, new JLabel("Max frequency ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(mainPanel, maxFreq = new JTextField(6), c);
		c.gridx++;
		addComponent(mainPanel, new JLabel(" Hz", JLabel.LEFT), c);
		c.gridx = 0;
		c.gridy++;
		addComponent(mainPanel, new JLabel("Min amplitude ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(mainPanel, minAmplitude = new JTextField(6), c);
		c.gridx++;
		addComponent(mainPanel, new JLabel(" dB", JLabel.LEFT), c);
		c.gridx = 0;
		c.gridy++;
		addComponent(mainPanel, new JLabel("Min	length ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(mainPanel, minLength = new JTextField(6), c);
		c.gridx++;
		addComponent(mainPanel, new JLabel(" milliseconds", JLabel.LEFT), c);
		
		
		setDialogComponent(mainPanel);
	}
	
	public static WMAlarmParameters showDialog(Window frame, WMAlarmParameters wmAlarmParameters) {
		if (singleInstance == null || singleInstance.getOwner() != frame) {
			singleInstance = new WMAlarmDialog(frame);
		}
		singleInstance.wmAlarmParameters = wmAlarmParameters.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.wmAlarmParameters;
	}

	private void setParams() {
		minFreq.setText(String.format("%3.1f", wmAlarmParameters.minFreq));
		maxFreq.setText(String.format("%3.1f", wmAlarmParameters.maxFreq));
		minAmplitude.setText(String.format("%3.1f", wmAlarmParameters.minAmplitude));
		minLength.setText(String.format("%3.1f", wmAlarmParameters.minLengthMillis));
	}

	@Override
	public boolean getParams() {
		try {
			wmAlarmParameters.minFreq = Double.valueOf(minFreq.getText());
			wmAlarmParameters.maxFreq = Double.valueOf(maxFreq.getText());
			wmAlarmParameters.minAmplitude = Double.valueOf(minAmplitude.getText());
			wmAlarmParameters.minLengthMillis = Double.valueOf(minLength.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid parameter");
		}
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		wmAlarmParameters = null;
	}

	@Override
	public void restoreDefaultSettings() {
		wmAlarmParameters = new WMAlarmParameters();
		setParams();
	}

}
