package mel.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SourcePanel;
import PamView.panel.WestAlignedPanel;
import PamguardMVC.PamDataBlock;
import fftManager.FFTDataUnit;
import mel.MelControl;
import mel.MelParameters;

public class MelDialog extends PamDialog {
	
	private static MelDialog singleInstance;
	private MelControl melControl;
	private MelParameters melParams;
	
	private SourcePanel sourcePanel;
	
	private JTextField n_Mel, minFrequency, maxFrequency, power;

	private MelDialog(MelControl melControl) {
		super(melControl.getGuiFrame(), melControl.getUnitName() + " settings", true);
		this.melControl = melControl;
		sourcePanel = new SourcePanel(singleInstance, "FFT Source", FFTDataUnit.class, true, true);
		n_Mel = new JTextField(6);
		minFrequency = new JTextField(6);
		maxFrequency = new JTextField(6);
		power = new JTextField(6);
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(BorderLayout.NORTH, sourcePanel.getPanel());
		JPanel cPanel = new WestAlignedPanel(new GridBagLayout());
		cPanel.setBorder(new TitledBorder("Mel Settings"));
		mainPanel.add(BorderLayout.CENTER, cPanel);
		GridBagConstraints c = new PamGridBagContraints();
		cPanel.add(new JLabel("Min Frequency ", JLabel.RIGHT), c);
		c.gridx++;
		cPanel.add(minFrequency, c);
		c.gridx = 0;
		c.gridy++;
		cPanel.add(new JLabel("Max Frequency ", JLabel.RIGHT), c);
		c.gridx++;
		cPanel.add(maxFrequency, c);
		c.gridx = 0;
		c.gridy++;
		cPanel.add(new JLabel("Number of Mels ", JLabel.RIGHT), c);
		c.gridx++;
		cPanel.add(n_Mel, c);
		c.gridx = 0;
		c.gridy++;
		cPanel.add(new JLabel("Power ", JLabel.RIGHT), c);
		c.gridx++;
		cPanel.add(power, c);
		
		setDialogComponent(mainPanel);
	}

	public static MelParameters showDialog(MelControl melControl) {
		if (singleInstance == null || singleInstance.melControl != melControl) {
			singleInstance = new MelDialog(melControl);
		}
		singleInstance.setParams(melControl.getMelParameters());
		singleInstance.setVisible(true);
		return singleInstance.melParams;
	}
	
	private void setParams(MelParameters melParams) {
		this.melParams = melParams;
		sourcePanel.setSource(melParams.dataSource);
		sourcePanel.setChannelList(melParams.chanelMap);
		minFrequency.setText(String.format("%3.1f", melParams.minFrequency));
		maxFrequency.setText(String.format("%3.1f", melParams.maxFrequency));
		power.setText(String.format("%3.1f", melParams.power));
		n_Mel.setText(String.format("%d", melParams.nMel));
	}

	@Override
	public boolean getParams() {
		PamDataBlock source = sourcePanel.getSource();
		if (source == null) {
			return showWarning("You must select a source of FFT data as input");
		}
		melParams.dataSource = source.getLongDataName();
		melParams.chanelMap = sourcePanel.getChannelList();
		if (melParams.chanelMap == 0) {
			return showWarning("You must select at least one inpur channel");
		}
		try {
			melParams.minFrequency = Double.valueOf(minFrequency.getText());
			melParams.maxFrequency = Double.valueOf(maxFrequency.getText());
		}
		catch (NumberFormatException ex) {
			return showWarning("Invalid frequency value");
		}
		if (melParams.maxFrequency <= melParams.minFrequency) {
			return showWarning("The maximum frequency must be greater than the minimum frequency");
		}
		try {
			melParams.nMel = Integer.valueOf(n_Mel.getText());
		}
		catch (NumberFormatException ex) {
			return showWarning("The number of Mels must be a positive integer value");
		}
		if (melParams.nMel <= 0) {
			return showWarning("The number of Mels must be a positive integer value");
		}
		try {
			melParams.power = Double.valueOf(power.getText());
		}
		catch (NumberFormatException ex) {
			return showWarning("Invalid power value");
		}
		if (melParams.power <= 0) {
			return showWarning("The power value must be greater than 0 (default is 2)");
		}
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		melParams = null;
	}

	@Override
	public void restoreDefaultSettings() {
		setParams(new MelParameters());
	}

}
