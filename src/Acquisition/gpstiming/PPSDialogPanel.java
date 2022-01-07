package Acquisition.gpstiming;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionDialog;
import Acquisition.AcquisitionParameters;
import NMEA.NMEADataUnit;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SourcePanel;

public class PPSDialogPanel {
	
	private JPanel ppsPanel;
	
	private JCheckBox enablePPS;
	private JTextField ppsChannel;
	private JTextField ppsThreshold;
	private SourcePanel ppsNMEA;
	private JTextField storageInterval;

	private JPanel mainPanel;

	private AcquisitionControl acquisitionControl;

	private AcquisitionDialog acquisitionDialog;

	public PPSDialogPanel(AcquisitionControl acquisitionControl, AcquisitionDialog acquisitionDialog) {
		this.acquisitionControl = acquisitionControl;
		this.acquisitionDialog = acquisitionDialog;
		ppsPanel = new JPanel();
		ppsPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.gridwidth = 3;
		ppsPanel.add(enablePPS = new JCheckBox("Enable GPS Pulse Per Second timing"), c);
		c.gridy++;
		c.gridwidth = 1;
		c.gridx = 0;
		ppsPanel.add(new JLabel(" Channel number ",JLabel.RIGHT), c);
		c.gridx++;
		ppsPanel.add(ppsChannel = new JTextField(3), c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		ppsPanel.add(new JLabel(" Detection threshold ",JLabel.RIGHT), c);
		c.gridx++;
		ppsPanel.add(ppsThreshold = new JTextField(3), c);
		c.gridx++;
		ppsPanel.add(new JLabel(" (range -1 to 1)", JLabel.LEFT), c);
		c.gridx = 0;
		c.gridy++;
		ppsPanel.add(new JLabel(" NMEA GPS Source ",JLabel.RIGHT), c);
		c.gridx++;
		c.gridwidth = 2;
		ppsNMEA = new SourcePanel(null, NMEADataUnit.class, false, false);
		ppsPanel.add(ppsNMEA.getPanel(), c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		ppsPanel.add(new JLabel(" Storage Interval ",JLabel.RIGHT), c);
		c.gridx++;
		ppsPanel.add(storageInterval = new JTextField(4), c);
		c.gridx++;
		ppsPanel.add(new JLabel(" (seconds)", JLabel.LEFT), c);
		
		enablePPS.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				enableControls();
			}
		});
		
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(BorderLayout.NORTH, ppsPanel);
	}

	public JComponent getDialogComponent() {
		return mainPanel;
	}

	public void setParams(PPSParameters ppsParams) {
		ppsNMEA.setSourceList();
		enablePPS.setSelected(ppsParams.useGpsPPS);
		ppsChannel.setText(String.format("%d", ppsParams.gpsPPSChannel));
		ppsThreshold.setText(String.format("%3.3f", ppsParams.gpsPPSThreshold));
		ppsNMEA.setSource(ppsParams.gpsPPSNmeaSource);
		storageInterval.setText(String.format("%d", ppsParams.storageInterval));
		enableControls();
	}


	public boolean getParams(AcquisitionParameters daqParams) {
		PPSParameters ppsParams = daqParams.getPpsParameters();
		ppsParams.useGpsPPS = enablePPS.isSelected();
		if (ppsParams.useGpsPPS == false) {
			return true;
		}
		
		try {
			ppsParams.gpsPPSChannel = Integer.valueOf(ppsChannel.getText());
		}
		catch (NumberFormatException e) {
			return acquisitionDialog.showWarning("Invalid PPS channel number");
		}
		if (ppsParams.gpsPPSChannel < 0 || ppsParams.gpsPPSChannel >= daqParams.nChannels) {
			return acquisitionDialog.showWarning("Invalid PPS channel number");
		}
		
		try {
			ppsParams.gpsPPSThreshold = Double.valueOf(ppsThreshold.getText());
		}
		catch (NumberFormatException e) {
			return acquisitionDialog.showWarning("PPS Threshold must be > -1 and < 1");
		}
		if (ppsParams.gpsPPSThreshold <= -1 || ppsParams.gpsPPSThreshold >= 1) {
			return acquisitionDialog.showWarning("PPS Threshold must be > -1 and < 1");
		}
		ppsParams.gpsPPSNmeaSource = ppsNMEA.getSourceName();
		if (ppsParams.gpsPPSNmeaSource  == null) {
			return acquisitionDialog.showWarning("You must select a valid NMEA data source");
		}
		try {
			ppsParams.storageInterval = Integer.valueOf(storageInterval.getText());
		}
		catch (NumberFormatException e) {
			return acquisitionDialog.showWarning("Invalid storage interval");
		}
		return true;
	}
	
	private void enableControls() {
		boolean haveNMEA = ppsNMEA.getSourceCount() > 0;
		enablePPS.setEnabled(haveNMEA);
		ppsChannel.setEnabled(enablePPS.isSelected() & haveNMEA);
		ppsThreshold.setEnabled(enablePPS.isSelected() & haveNMEA);
		ppsNMEA.setEnabled(enablePPS.isSelected() & haveNMEA);
	}

}
