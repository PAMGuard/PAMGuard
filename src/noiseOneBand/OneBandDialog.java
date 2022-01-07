package noiseOneBand;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import Filters.FilterDialogPanel;
import PamDetection.RawDataUnit;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SourcePanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamRawDataBlock;

public class OneBandDialog extends PamDialog {

	private OneBandControl oneBandControl;
	
	private static OneBandDialog singleInstance;
	
	private OneBandParameters params;
	
	private FilterDialogPanel filterDialogPanel;
	
	private SourcePanel sourcePanel;
	
	private JTextField measurementInterval, integrationTime;
	
	private JCheckBox detectPulses;
	
	private JTextField singlePulseThreshold, maxPulseLength;

	public OneBandDialog(OneBandControl oneBandControl, Window parentFrame) {
		super(parentFrame, oneBandControl.getUnitName(), false);
		this.oneBandControl = oneBandControl;
		filterDialogPanel = new FilterDialogPanel(parentFrame, 1);
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(BorderLayout.CENTER, filterDialogPanel.getMainPanel());
		
		JPanel northPanel = new JPanel();
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.X_AXIS));
		mainPanel.add(BorderLayout.NORTH, northPanel);
		sourcePanel = new SourcePanel(null, "Raw data source", RawDataUnit.class, true, true);
		sourcePanel.excludeDataBlock(oneBandControl.getOneBandProcess().getWaveOutDataBlock(), true);
		northPanel.add(sourcePanel.getPanel());
		sourcePanel.addSelectionListener(new SourceListener());
		
		JPanel timePanel = new JPanel();
		timePanel.setBorder(new TitledBorder("Measurement Time"));
		timePanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		timePanel.add(new JLabel("Measurement Interval ", JLabel.RIGHT), c);
		c.gridx++;
		timePanel.add(measurementInterval = new JTextField(3), c);
		c.gridx++;
		timePanel.add(new JLabel(" s", JLabel.LEFT), c);
		c.gridx = 0;
		c.gridy++;
		timePanel.add(new JLabel("SEL Integration Time ", JLabel.RIGHT), c);
		c.gridx++;
		timePanel.add(integrationTime = new JTextField(3), c);
		c.gridx++;
		timePanel.add(new JLabel(" s", JLabel.LEFT), c);
		northPanel.add(timePanel);

		JPanel pulsePanel = new JPanel();
		pulsePanel.setBorder(new TitledBorder("Pulse Measurement"));
		pulsePanel.setLayout(new GridBagLayout());
		c = new PamGridBagContraints();
		c.gridwidth = 3;
		pulsePanel.add(detectPulses = new JCheckBox("Detect Pulses"), c);
		detectPulses.addActionListener(new DetectPulses());
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy++;
		pulsePanel.add(new JLabel("Det' Threshold ", JLabel.RIGHT), c);
		c.gridx++;
		pulsePanel.add(singlePulseThreshold = new JTextField(3), c);
		c.gridx++;
		pulsePanel.add(new JLabel(" dB", JLabel.LEFT), c);
		c.gridx = 0;
		c.gridy++;
		pulsePanel.add(new JLabel("Max Length ", JLabel.RIGHT), c);
		c.gridx++;
		pulsePanel.add(maxPulseLength = new JTextField(3), c);
		c.gridx++;
		pulsePanel.add(new JLabel(" S", JLabel.LEFT), c);
		
		
		northPanel.add(pulsePanel);
		
		setHelpPoint("sound_processing.NoiseOneBand.Docs.NoiseOneBand");
		
		setDialogComponent(mainPanel);
		setResizable(true);
	}

	public static OneBandParameters showDialog(OneBandControl oneBandControl, Window parentFrame) {
		if (singleInstance == null || 
				singleInstance.oneBandControl != oneBandControl || 
				singleInstance.getOwner() != parentFrame) {
			singleInstance = new OneBandDialog(oneBandControl, parentFrame);
		}
		singleInstance.params = oneBandControl.oneBandParameters.clone();
		singleInstance.setParams(oneBandControl.getOneBandProcess().getSampleRate());
		singleInstance.setVisible(true);
		return singleInstance.params;
	}

	private void setParams(float sampleRate) {
		filterDialogPanel.setSampleRate(sampleRate);
		filterDialogPanel.setParams(params.getFilterParams());

		sourcePanel.setSource(params.dataSource);
		sourcePanel.setChannelList(params.channelMap);
		
		measurementInterval.setText(String.format("%d", params.measurementInterval));
		integrationTime.setText(String.format("%d", params.selIntegrationTime));
		
		detectPulses.setSelected(params.detectPulses);
		singlePulseThreshold.setText(String.format("%3.1f", params.singlePulseThreshold));
		maxPulseLength.setText(String.format("%3.1f", params.maxPulseLength));

		enableControls();
	}

	@Override
	public boolean getParams() {
		PamDataBlock dataBlock = sourcePanel.getSource();
		if (dataBlock == null) {		
			return showWarning("You must select a data source");
		}
		params.dataSource = dataBlock.getDataName();
		params.channelMap = sourcePanel.getChannelList();
		filterDialogPanel.setSampleRate(dataBlock.getSampleRate());
		
		if (filterDialogPanel.getParams() == false) {
			return false;
		}
		params.setFilterParams(filterDialogPanel.getFilterParams());

		
		try {
			params.measurementInterval = Integer.valueOf(measurementInterval.getText());
			params.selIntegrationTime = Integer.valueOf(integrationTime.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid measurement interval or SEL integration time");
		}

		params.detectPulses = detectPulses.isSelected();
		if (params.detectPulses) {
			try {
				params.singlePulseThreshold = Double.valueOf(singlePulseThreshold.getText());
				params.maxPulseLength = Double.valueOf(maxPulseLength.getText());
			}
			catch (NumberFormatException e) {
				return showWarning("Invalid pulse measurement parameter");
			}
		}
		
		return true;
	}
	class SourceListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			checkSource();
		}
	}
	class DetectPulses implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			enableControls();
		}
	}
	
	private void enableControls() {
		singlePulseThreshold.setEnabled(detectPulses.isSelected());
		maxPulseLength.setEnabled(detectPulses.isSelected());
	}

	public void checkSource() {
		PamDataBlock sourceBlock = sourcePanel.getSource();
		if (sourceBlock == null) {
			filterDialogPanel.setSampleRate(1);
		}
		else {
			filterDialogPanel.setSampleRate(sourceBlock.getSampleRate());
		}
	}

	@Override
	public void cancelButtonPressed() {
		params = null;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
