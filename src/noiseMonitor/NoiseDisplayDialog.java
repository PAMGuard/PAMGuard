package noiseMonitor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class NoiseDisplayDialog extends PamDialog {

	private static NoiseDisplayDialog singleInstance;
	
	private NoiseDisplaySettings noiseSettings;
	
	private JTextField maxLevel, minLevel;
	
	private JCheckBox autoLevel, showGrid, showKey;
	
	private NoiseDisplayDialog(Window parentFrame) {
		super(parentFrame, "Noise Display Options", true);
		
		JPanel levPanel = new JPanel(new GridBagLayout());
		levPanel.setBorder(new TitledBorder("Scale"));
		
		GridBagConstraints c = new PamGridBagContraints();
		addComponent(levPanel, new JLabel("Maximum "), c);
		c.gridx++;
		addComponent(levPanel, maxLevel = new JTextField(5), c);
		c.gridx++;
		addComponent(levPanel, new JLabel(" dB"), c);
		c.gridx = 0;
		c.gridy++;
		addComponent(levPanel, new JLabel("Minimum "), c);
		c.gridx++;
		addComponent(levPanel, minLevel = new JTextField(5), c);
		c.gridx++;
		addComponent(levPanel, new JLabel(" dB"), c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		addComponent(levPanel, autoLevel = new JCheckBox("Automatic"), c);
		autoLevel.addActionListener(new AutoScale());
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		addComponent(levPanel, showGrid = new JCheckBox("Show Grid"), c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		addComponent(levPanel, showKey = new JCheckBox("Show Key"), c);
//		autoLevel.addActionListener(new ShowGrid());
		
		setDialogComponent(levPanel);
		
	}
	
	public static NoiseDisplaySettings showDialog(Window parentFrame, NoiseDisplaySettings noiseDisplaySettings) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame) {
			singleInstance = new NoiseDisplayDialog(parentFrame);
		}
		singleInstance.noiseSettings = noiseDisplaySettings;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.noiseSettings;
	}

	@Override
	public void cancelButtonPressed() {
		noiseSettings = null;
	}
	
	private void setParams() {
		maxLevel.setText(formatDouble(noiseSettings.levelMax));
		minLevel.setText(formatDouble(noiseSettings.levelMin));
		autoLevel.setSelected(noiseSettings.autoScale);
		showGrid.setSelected(noiseSettings.showGrid);
		showKey.setSelected(noiseSettings.showKey);
		enableControls();
	}

	public void enableControls() {
		boolean e = (autoLevel.isSelected() == false);
		maxLevel.setEnabled(e);
		minLevel.setEnabled(e);
	}

	@Override
	public boolean getParams() {
		try {
			noiseSettings.levelMax = Double.valueOf(maxLevel.getText());
			noiseSettings.levelMin = Double.valueOf(minLevel.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid number format");
		}
		noiseSettings.autoScale = autoLevel.isSelected();
		noiseSettings.showGrid = showGrid.isSelected();
		noiseSettings.showKey = showKey.isSelected();
		return true;
	}
	
	private class AutoScale implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			enableControls();
		}
	}

	@Override
	public void restoreDefaultSettings() {
		NoiseDisplaySettings defSettings = new NoiseDisplaySettings();
		noiseSettings.levelMin = defSettings.levelMin;
		noiseSettings.levelMax = defSettings.levelMax;
		setParams();
	}

}
