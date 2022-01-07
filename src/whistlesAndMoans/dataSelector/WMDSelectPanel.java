package whistlesAndMoans.dataSelector;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import whistlesAndMoans.alarm.WMAlarmParameters;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;

public class WMDSelectPanel implements PamDialogPanel {

	private WMDDataSelector wmdDataSelector;
	
	private JPanel mainPanel;

	private JTextField minFreq;

	private JTextField maxFreq;

	private JTextField minAmplitude;

	private JTextField minLength;
	
	private JCheckBox superDetOnly;

	public WMDSelectPanel(WMDDataSelector wmdDataSelector) {
		this.wmdDataSelector = wmdDataSelector;

		mainPanel = new JPanel();
		mainPanel.setBorder(new TitledBorder("Select whistles"));
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.add(new JLabel("Min frequency ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(minFreq = new JTextField(6), c);
		c.gridx++;
		mainPanel.add(new JLabel(" Hz", JLabel.LEFT), c);
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(new JLabel("Max frequency ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(maxFreq = new JTextField(6), c);
		c.gridx++;
		mainPanel.add(new JLabel(" Hz", JLabel.LEFT), c);
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(new JLabel("Min amplitude ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(minAmplitude = new JTextField(6), c);
		c.gridx++;
		mainPanel.add(new JLabel(" dB", JLabel.LEFT), c);
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(new JLabel("Min length ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(minLength = new JTextField(6), c);
		c.gridx++;
		mainPanel.add(new JLabel(" milliseconds", JLabel.LEFT), c);
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(new JLabel(" ", JLabel.LEFT), c);
		c.gridy++;
		c.gridwidth = 3;
		mainPanel.add(new JLabel("Only Whistles with Super-Detections ", JLabel.RIGHT), c);
		c.gridy++;
		mainPanel.add(superDetOnly = new JCheckBox(), c);
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		WMAlarmParameters wmAlarmParameters = wmdDataSelector.getWmAlarmParameters();

		minFreq.setText(String.format("%3.1f", wmAlarmParameters.minFreq));
		maxFreq.setText(String.format("%3.1f", wmAlarmParameters.maxFreq));
		minAmplitude.setText(String.format("%3.1f", wmAlarmParameters.minAmplitude));
		minLength.setText(String.format("%3.1f", wmAlarmParameters.minLengthMillis));
		superDetOnly.setSelected(wmAlarmParameters.superDetOnly);
	}

	@Override
	public boolean getParams() {
		WMAlarmParameters wmAlarmParameters = wmdDataSelector.getWmAlarmParameters().clone();
		try {
			wmAlarmParameters.minFreq = Double.valueOf(minFreq.getText());
			wmAlarmParameters.maxFreq = Double.valueOf(maxFreq.getText());
			wmAlarmParameters.minAmplitude = Double.valueOf(minAmplitude.getText());
			wmAlarmParameters.minLengthMillis = Double.valueOf(minLength.getText());
			wmAlarmParameters.superDetOnly = superDetOnly.isSelected();
		}
		catch (NumberFormatException e) {
			return false;
		}
		wmdDataSelector.setWmAlarmParameters(wmAlarmParameters);
		return true;
	}

}
