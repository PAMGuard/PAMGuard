package hfDaqCard;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import Acquisition.AcquisitionDialog;
import Array.ArrayManager;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamguardMVC.PamConstants;

public class SmruDaqDialogPanel {

	private JPanel dialogPanel;

	private SmruDaqSystem smruDaqSystem;

	JComboBox sampleRate;
	JCheckBox[] channelEnable = new JCheckBox[PamConstants.MAX_CHANNELS];
	JComboBox[] gains = new JComboBox[PamConstants.MAX_CHANNELS];
	JComboBox[] filters = new JComboBox[PamConstants.MAX_CHANNELS];
	
	JLabel libVersion;	

	private SmruDaqParameters smruDaqParameters;

	private AcquisitionDialog acquisitionDialog;

	private JButton[] toggles = new JButton[2];

	private int nSmruDaqBoards;

	private int nShownBoards;

	/**
	 * @param smruDaqSystem
	 */
	public SmruDaqDialogPanel(SmruDaqSystem smruDaqSystem) {
		super();

		this.smruDaqSystem = smruDaqSystem;
		dialogPanel = new JPanel(new GridBagLayout());
		dialogPanel.setBorder(new TitledBorder("Daq card configuration"));
		GridBagConstraints c = new PamGridBagContraints();
		c.gridwidth = 3;
		dialogPanel.add(new JLabel("Library version: ", SwingConstants.RIGHT), c);
		c.gridx += c.gridwidth;
		dialogPanel.add(libVersion = new JLabel("Unknown"));
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 3;
		PamDialog.addComponent(dialogPanel,new JLabel("Sample Rate ", SwingConstants.RIGHT),c);
		c.gridx += c.gridwidth;
		c.gridwidth = 3;
		PamDialog.addComponent(dialogPanel, sampleRate = new JComboBox(), c);
		sampleRate.addActionListener(new SampleRate());
		c.gridx += c.gridwidth;
		c.gridwidth = 1;
		//		PamDialog.addComponent(dialogPanel,new JLabel(" Hz ", JLabel.LEFT),c);
		for (int i = 0; i < SmruDaqParameters.sampleRates.length; i++) {
			sampleRate.addItem(String.format("%3.1f kHz", SmruDaqParameters.sampleRates[i]/1000.));
		}

		nSmruDaqBoards = smruDaqSystem.getSmruDaqJNI().getnDevices();
		if (nSmruDaqBoards <= 0) {
			c.gridwidth = 7;
			c.gridx = 0;
			c.gridy++;
			JLabel l;
			PamDialog.addComponent(dialogPanel, l= new JLabel("**** WARNING ****", SwingConstants.CENTER), c);
			l.setForeground(Color.RED);
			c.gridy++;
			PamDialog.addComponent(dialogPanel, l =new JLabel("No SAIL Daq cards are present on your system ", SwingConstants.CENTER), c);
			l.setForeground(Color.RED);
		}
		int iBoard = 0;		
		String snStr;
		nShownBoards = nSmruDaqBoards;
		int nPhones = ArrayManager.getArrayManager().getCurrentArray().getHydrophoneCount();
		while(nShownBoards * 4 < nPhones) nShownBoards++;
		
		for (int b = 0; b < nShownBoards; b++) {
			c.gridx = 0;
			c.gridy++;
			c.gridwidth = 5;

			JLabel snrLabel;
			PamDialog.addComponent(dialogPanel, snrLabel = new JLabel("", SwingConstants.CENTER), c);			
			if (b < nSmruDaqBoards) {
				snStr = String.format("Board %d s/n %X", b, smruDaqSystem.getSmruDaqJNI().getSerialNumber(b));
				snrLabel.setText(snStr);
			}
			else {
				snStr = "*** No board installed ***";
				snrLabel.setText(snStr);
				snrLabel.setForeground(Color.RED);
			}
			c.gridx += c.gridwidth;
			c.gridwidth = 3;
			JButton ledButton = new JButton("Flash LED's");
			PamDialog.addComponent(dialogPanel, ledButton, c);
			ledButton.addActionListener(new FlashLED(b));
//			for (int i = 0; i < 2; i++) {
//				PamDialog.addComponent(dialogPanel, toggles[i] = new JButton("Toggle LED "+i), c);
//				toggles[i].addActionListener(new Toggle(b, i));
//				c.gridx += c.gridwidth;
//			}
			
			for (int i = 0; i < 4; i++) {
				c.gridx = 0;
				c.gridy++;
				c.gridwidth = 1;
				PamDialog.addComponent(dialogPanel,new JLabel("Enable ", SwingConstants.RIGHT),c);
				c.gridx += 1;
				PamDialog.addComponent(dialogPanel, channelEnable[iBoard] = new JCheckBox(), c);
				c.gridx ++;
				PamDialog.addComponent(dialogPanel,new JLabel(", gain ", SwingConstants.RIGHT),c);
				c.gridx ++;
				PamDialog.addComponent(dialogPanel, gains[iBoard] = new JComboBox(), c);
				c.gridx ++;
				PamDialog.addComponent(dialogPanel,new JLabel(" dB, HP filter ", SwingConstants.LEFT),c);
				c.gridx ++;
				PamDialog.addComponent(dialogPanel, filters[iBoard] = new JComboBox(), c);
				c.gridx ++;
				PamDialog.addComponent(dialogPanel,new JLabel(" Hz ", SwingConstants.LEFT),c);

				double[] daqGains = SmruDaqParameters.getGains();
				channelEnable[iBoard].addActionListener(new ChannelEnable());
				for (int j = 0; j < daqGains.length; j++) {
					if (Double.isInfinite(daqGains[j])) {
						gains[iBoard].addItem("Off");
					}
					else {
						gains[iBoard].addItem(String.format("%3.1f", daqGains[j]));
					}
				}
				for (int j = 0; j < SmruDaqParameters.filters.length; j++) {
					filters[iBoard].addItem(String.format("%3.1f", SmruDaqParameters.filters[j]));
				}
				iBoard++;
			}
		}
//		c.gridy++;
//		c.gridx = 0;
//		c.gridwidth = 4;
//		for (int i = 0; i < 2; i++) {
//			PamDialog.addComponent(dialogPanel, toggles[i] = new JButton("Toggle LED "+i), c);
//			toggles[i].addActionListener(new Toggle(0, i));
//			c.gridx += c.gridwidth;
//		}
	}

	public SmruDaqParameters getParams() {
		smruDaqParameters.setSampleRateIndex(sampleRate.getSelectedIndex());
		smruDaqParameters.channelMask = 0;
		for (int i = 0; i < SmruDaqParameters.NCHANNELS * nShownBoards; i++) {
			if (channelEnable[i].isSelected()) {
				smruDaqParameters.channelMask += 1<<i;
			}
			smruDaqParameters.setGainIndex(i, gains[i].getSelectedIndex());
			smruDaqParameters.setFilterIndex(i, filters[i].getSelectedIndex());
		}
		return smruDaqParameters;
	}

	public void setParams(SmruDaqParameters smruDaqParameters) {
		
		libVersion.setText(smruDaqSystem.getJNILibInfo());
		
		this.smruDaqParameters = smruDaqParameters.clone();
		sampleRate.setSelectedIndex(smruDaqParameters.getSampleRateIndex());
		for (int i = 0; i < SmruDaqParameters.NCHANNELS * nShownBoards; i++) {
			channelEnable[i].setSelected((smruDaqParameters.channelMask & 1<<i) != 0);
			gains[i].setSelectedIndex(smruDaqParameters.getGainIndex(i));
			filters[i].setSelectedIndex(smruDaqParameters.getFilterIndex(i));
		}

		enableControls();
	}

	class ChannelEnable implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			enableControls();
		}
	}

	public void enableControls() {
		boolean b;
		int enabledChannels = 0;
		for (int i = 0; i < SmruDaqParameters.NCHANNELS * nShownBoards; i++) {
			if (i/4 < nSmruDaqBoards) {
//				channelEnable[i].setsel
			}
			b = channelEnable[i].isSelected();
			gains[i].setEnabled(b);
			filters[i].setEnabled(b);
			if (b) {
				enabledChannels++;
			}
		}
		if (acquisitionDialog != null) {
			acquisitionDialog.setChannels(enabledChannels);
		}
	}

	class SampleRate implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			saySampleRate();
		}
	}
	
	class FlashLED implements ActionListener {
		private int board;
		private boolean flashing = false;

		public FlashLED(int board) {
			super();
			this.board = board;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			SmruDaqJNI smruJNI = smruDaqSystem.getSmruDaqJNI();
			boolean ok = smruJNI.flashLEDs(board, 6);
			if (!ok) {
				String warning = "Unable to communicate with SAIL DAQ card. \n"
						+ "It may be in use by another program. \n"
						+ "Otherwise check connections and power.";
				PamDialog.showWarning(acquisitionDialog, "DAQ Card Error", warning);
			}
		}
		
	}

	class Toggle implements ActionListener {

		int led;
		private int board;
		public Toggle(int board, int led) {
			super();
			this.board = board;
			this.led = led;
			setText();
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			smruDaqSystem.toggleLED(board, led);
			setText();
		}
		public void setText() {
			int status = smruDaqSystem.getLED(board, led);
			String text = String.format("Toggle LED %d", led);
			switch(status) {
			case 0:
				text = String.format("Turn LED %d ON", led);
				break;
			case 1:
				text = String.format("Turn LED %d OFF", led);
				break;
			}
			toggles[led].setText(text);
		}
	}


	public void saySampleRate() {
		if (acquisitionDialog != null) {
			int sr = SmruDaqParameters.sampleRates[sampleRate.getSelectedIndex()];
			acquisitionDialog.setSampleRate(sr);

			// while here, set the V p-p
			acquisitionDialog.setVPeak2Peak(SmruDaqParameters.VPEAKTOPEAK);
		}
	}

	/**
	 * @return the dialogPanel
	 */
	public JPanel getDialogPanel() {
		return dialogPanel;
	}

	public void setDaqDialog(AcquisitionDialog acquisitionDialog) {
		this.acquisitionDialog = acquisitionDialog;
	}


}
