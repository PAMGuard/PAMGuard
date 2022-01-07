package clickDetector.dialogs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import clickDetector.ClickParameters;
import clickDetector.ClickWaveform;
import fftFilter.FFTFilterDialog;
import fftFilter.FFTFilterParams;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class WaveDisplayDialog extends PamDialog {

	private static WaveDisplayDialog singleInstance;
	
	private ClickParameters clickParameters;
	
	private ClickWaveform clickWaveform;
	
	private JCheckBox showEnvelope, fixedXScale, viewFiltered;
	
	private JRadioButton separatePlots, singlePlot;
	
	private JButton filterSettings;
	
	private WaveDisplayDialog(Window parentFrame) {
		super(parentFrame, "Waveform Display", false);

		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		p.setBorder(new TitledBorder("Options"));
		c.gridx = c.gridy = 0;
		addComponent(p, separatePlots = new JRadioButton("Separate waveform plots"), c);
		c.gridy++;
		addComponent(p, singlePlot = new JRadioButton("Single waveform plot"), c);
		c.gridy++;
		addComponent(p, fixedXScale = new JCheckBox("Constant time scale"), c);
		c.gridy++;
		addComponent(p, showEnvelope = new JCheckBox("Show waveform envelope"), c);
		c.gridy++;
		addComponent(p, viewFiltered = new JCheckBox("Show Filtered Waveform"), c);
		viewFiltered.addActionListener(new ViewFiltered());
		c.gridx++;
		addComponent(p, filterSettings = new JButton("Settings"), c);
		filterSettings.addActionListener(new FilterSettings());
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(singlePlot);
		bg.add(separatePlots);
		
		setDialogComponent(p);
	}

	public static ClickParameters showDialog(Window window, ClickWaveform clickWaveform,
			ClickParameters clickParameters) {
		if (singleInstance == null || window != singleInstance.getParent()) {
			singleInstance = new WaveDisplayDialog(window);
		}
		singleInstance.clickParameters = clickParameters.clone();
		singleInstance.clickWaveform = clickWaveform;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		
		return singleInstance.clickParameters;
	}
	
	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub
		clickParameters = null;
	}
	
	private void setParams() {
		separatePlots.setSelected(clickParameters.singleWavePlot == false);
		singlePlot.setSelected(clickParameters.singleWavePlot);
		fixedXScale.setSelected(clickParameters.waveFixedXScale);
		showEnvelope.setSelected(clickParameters.waveShowEnvelope);
		viewFiltered.setSelected(clickParameters.viewFilteredWaveform);
		enableControls();
	}

	@Override
	public boolean getParams() {
		clickParameters.singleWavePlot = singlePlot.isSelected();
		clickParameters.waveFixedXScale = fixedXScale.isSelected();
		clickParameters.waveShowEnvelope = showEnvelope.isSelected();
		clickParameters.viewFilteredWaveform = viewFiltered.isSelected();
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

	private class ViewFiltered implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			enableControls();
		}
	}
	
	private class FilterSettings implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			FFTFilterParams newParams = FFTFilterDialog.showDialog(getOwner(), clickParameters.waveformFilterParams);
			if (newParams != null) {
				clickParameters.waveformFilterParams = newParams.clone();
			}
		}
	}

	public void enableControls() {
		filterSettings.setEnabled(viewFiltered.isSelected());
	}

}
