package nidaqdev.networkdaq;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import Acquisition.AcquisitionDialog;
import PamView.dialog.PamGridBagContraints;

/*
 * Dialog panel for CRio daq system. 
 */
public class CRioDialogPanel {

	private JPanel dialogPanel;

	private NINetworkDaq niNetworkDaq;

	private JComboBox<String> sampleRate;

	private JComboBox<String> nChannels;
	
	private JComboBox<ChassisConfig> chassisType;

	private JTextField udpPort, tcpPort, verboseLevel, exeName;

	private JComboBox<String> niAddress;

	private AcquisitionDialog acquisitionDialog;

	public CRioDialogPanel(AcquisitionDialog acquisitionDialog, NINetworkDaq niNetworkDaq) {
		super();
		this.acquisitionDialog = acquisitionDialog;
		this.niNetworkDaq = niNetworkDaq;

		dialogPanel = new JPanel();

		dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
		JPanel samplePanel = new JPanel(new GridBagLayout());
		samplePanel.setBorder(new TitledBorder("Sampling"));
		GridBagConstraints c = new PamGridBagContraints();
		samplePanel.add(new JLabel("Sample Rate ", SwingConstants.RIGHT));
		c.gridx++;
		samplePanel.add(sampleRate = new JComboBox<>(), c);
		c.gridx = 0;
		c.gridy++;
		samplePanel.add(new JLabel("N Channels ", SwingConstants.RIGHT), c);
		c.gridx++;
		samplePanel.add(nChannels = new JComboBox<>(), c);

		JPanel controlPanel = new JPanel(new GridBagLayout());
		controlPanel.setBorder(new TitledBorder("Control"));
		c = new PamGridBagContraints();
		controlPanel.add(new JLabel("cRIO ip addr ", SwingConstants.RIGHT), c);
		c.gridx++;
		controlPanel.add(niAddress = new JComboBox<>(), c);
		c.gridx = 0;
		c.gridy++;
		controlPanel.add(new JLabel("NI Chassis type ", SwingConstants.RIGHT), c);
		c.gridx++;
		controlPanel.add(chassisType = new JComboBox<>(), c);
		c.gridx = 0;
		c.gridy++;
		controlPanel.add(new JLabel("Crio Executable "), c);
		c.gridx++;
		controlPanel.add(exeName = new JTextField(20), c);
		c.gridx = 0;
		c.gridy++;
		controlPanel.add(new JLabel("UDP Control Port ", SwingConstants.RIGHT), c);
		c.gridx++;
		controlPanel.add(udpPort = new JTextField(5), c);
		c.gridx = 0;
		c.gridy++;
		controlPanel.add(new JLabel("TCP Data Port ", SwingConstants.RIGHT), c);
		c.gridx++;
		controlPanel.add(tcpPort = new JTextField(5), c);
		c.gridx = 0;
		c.gridy++;
		controlPanel.add(new JLabel("Verbose level ", SwingConstants.RIGHT), c);
		c.gridx++;
		controlPanel.add(verboseLevel = new JTextField(2), c);

		dialogPanel.add(samplePanel);
		dialogPanel.add(controlPanel);

		sampleRate.addActionListener(new SampleRate());
		nChannels.addActionListener(new nChan());
		
		ChassisConfig[] configs = ChassisConfig.values();
		for (int i = 0; i < configs.length; i++) {
			chassisType.addItem(configs[i]);
		}

		niAddress.setEditable(true);
		sampleRate.setToolTipText("Select the sample rate from the drop down list. Currently only 500kHz sampling is available for this system");
		nChannels.setToolTipText("Select the number of channels. This must can only be 8 or 12 depending on the number of NI9222 modules available");
		niAddress.setToolTipText("Set the ip address of the NI cRIO chassis. This should be visible and can be set from NI-MAX");
		udpPort.setToolTipText("The network port for control of the cRIO software. Unless the cRIO software has been started with a different default, this will always be 8000");;
		tcpPort.setToolTipText("The TCP Port for data relay from the cRIO to the PC");
		chassisType.setToolTipText("The type of NI chassis (9067 or 9068). This is needed to load the correct FPGA software version on the cRIO");
		verboseLevel.setToolTipText("Verbose level for cRio 0 = almost no terminal output 3 means lots!");
		exeName.setToolTipText("Name of Linux executable on cRio");
		
	}

	/**
	 * @return the dialogPanel
	 */
	public JPanel getDialogPanel() {
		return dialogPanel;
	}

	protected void setDialogParams(NINetworkDaqParams niNetParams) {
		int selIndex = -1;
		niAddress.removeAllItems();
		int ind = 0;
		for (String addr:niNetParams.recentAddresses) {
			niAddress.addItem(addr);
			if (addr.equals(niNetParams.niAddress)) {
				selIndex = ind;
			}
			ind++;
		}
		if (selIndex >= 0) {
			niAddress.setSelectedIndex(selIndex);
		}
		else {
			niAddress.insertItemAt(niNetParams.niAddress, 0);
			niAddress.setSelectedIndex(0);
		}
		
		exeName.setText(niNetParams.getExeName());
		if (niNetParams.chassisConfig != null) {
			chassisType.setSelectedItem(niNetParams.chassisConfig);
		}
		udpPort.setText(Integer.toString(niNetParams.niUDPPort));
		tcpPort.setText(Integer.toString(niNetParams.niTCPPort));

		sampleRate.removeAllItems();
		nChannels.removeAllItems();
		for (int i = 0; i < NINetworkDaqParams.SampleRates.length; i++) {
			sampleRate.addItem(Integer.toString(NINetworkDaqParams.SampleRates[i]));
		}
		for (int i = 0; i < NINetworkDaqParams.NChannels.length; i++) {
			nChannels.addItem(Integer.toString(NINetworkDaqParams.NChannels[i]));
		}
		sampleRate.setSelectedIndex(niNetParams.sampleRateIndex);
		nChannels.setSelectedIndex(niNetParams.nChannelsIndex);
		verboseLevel.setText(String.format("%d", niNetParams.verboseLevel));
	}

	protected boolean getDialogParams(NINetworkDaqParams niNetworkDaqParams) {
		Object niAdd = niAddress.getSelectedItem();
		if (niAdd == null) {
			return acquisitionDialog.showWarning("Invalid network address");
		}
		niNetworkDaqParams.niAddress = niAddress.getSelectedItem().toString();
		niNetworkDaqParams.newAddress(niNetworkDaqParams.niAddress);
		niNetworkDaqParams.chassisConfig = (ChassisConfig) chassisType.getSelectedItem();
		niNetworkDaqParams.setExeName(exeName.getText());
		try{
			niNetworkDaqParams.niUDPPort = Integer.valueOf(udpPort.getText());
			niNetworkDaqParams.niTCPPort = Integer.valueOf(tcpPort.getText());
		}
		catch (NumberFormatException e) {
			return acquisitionDialog.showWarning("Invalid TCP or UDP port address");
		}
		niNetworkDaqParams.nChannelsIndex = nChannels.getSelectedIndex();		
		niNetworkDaqParams.sampleRateIndex = sampleRate.getSelectedIndex();		
		try {
			niNetworkDaqParams.verboseLevel = Integer.valueOf(verboseLevel.getText());
		}
		catch (NumberFormatException e) {
			return acquisitionDialog.showWarning("Verbose level must be an integer");
		}
		return true;
	}
	class SampleRate implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			saySampleRate();
		}
	}
	class nChan implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			sayNChan();
		}
	}
	public void saySampleRate() {
		if (acquisitionDialog == null) return;
		if (sampleRate.getSelectedIndex() < 0) return;
		float sr = NINetworkDaqParams.SampleRates[sampleRate.getSelectedIndex()];
		acquisitionDialog.setSampleRate(sr);
		acquisitionDialog.setVPeak2Peak(20);
	}

	public void sayNChan() {
		if (acquisitionDialog == null) return;
		if (nChannels.getSelectedIndex() < 0) return;
		int n = NINetworkDaqParams.NChannels[nChannels.getSelectedIndex()];
		acquisitionDialog.setChannels(n);
	}

}
