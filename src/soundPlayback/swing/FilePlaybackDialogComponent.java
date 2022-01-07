package soundPlayback.swing;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamRawDataBlock;
import soundPlayback.FilePlayback;
import soundPlayback.FilePlaybackDevice;
import soundPlayback.PlaybackControl;
import soundPlayback.PlaybackParameters;

public class FilePlaybackDialogComponent extends PlaybackDialogComponent {

	private static final int FSLEN = 9;

	private JPanel panel;
	
	private PlaybackControl playbackControl;
	
	private JComboBox<String> deviceTypes;
	
	private JComboBox<String> soundCards;
		
	private JTextField inputSampleRate, outputSampleRate, playbackSpeed;
	
	private JLabel decimateInfo;

	private FilePlayback filePlayback;

	private PlaybackParameters playbackParameters;

	private JButton defButton;
	
	private PlaySpeedSlider playSpeedSlider;
	
	private boolean isRT;

	private PamDataBlock dataSource;
	
	/**
	 * Dialog component for sound playback when input is from a file. 
	 * <p>
	 * Have now implemented a system whereby playback can be over 
	 * a number of device types. For now this will be sound cards and NI 
	 * cards so that we can generate real audio data at V high frequency
	 * for some real time testing. 
	 * <p>
	 * Playback from file is easy since there is no need to synchronise sound input with 
	 * sound output. 
	 * @param playbackControl
	 */
	public FilePlaybackDialogComponent(FilePlayback filePlayback) {
		this.filePlayback = filePlayback;
		this.playbackControl = filePlayback.getPlaybackControl();
		
		panel = new JPanel();
		panel.setBorder(new TitledBorder("Options"));
		deviceTypes = new JComboBox<String>();
		deviceTypes.addActionListener(new NewDeviceType());
		soundCards = new JComboBox<String>();
		soundCards.addActionListener(new NewSoundCard());

		GridBagLayout layout = new GridBagLayout();
		panel.setLayout(layout);
		GridBagConstraints c = new PamGridBagContraints();
		c.gridwidth = 4;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = c.gridy = 0;
		PamDialog.addComponent(panel, new JLabel("Output device type ..."), c);
		c.gridy++;
		PamDialog.addComponent(panel, deviceTypes, c);
		c.gridy++;
		PamDialog.addComponent(panel, new JLabel("Output device name ..."), c);
		c.gridy++;
		PamDialog.addComponent(panel, soundCards, c);

		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		panel.add(new JLabel("Source sample rate ", JLabel.RIGHT), c);
		c.gridx++;
		panel.add(inputSampleRate = new JTextField(FSLEN), c);
		c.gridx++;
		panel.add(new JLabel(" Hz"), c);
		c.gridy++;
		c.gridx = 0;
		panel.add(new JLabel("Output sample rate ", JLabel.RIGHT), c);
		c.gridx++;
		panel.add(outputSampleRate = new JTextField(FSLEN), c);
		c.gridx++;
		panel.add(new JLabel(" Hz"), c);
		c.gridx=1;
		c.gridy++;
		c.gridwidth = 3;
		panel.add(decimateInfo = new JLabel(), c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		panel.add(new JLabel("Playback Speed"), c);
		c.gridx +=1;
		panel.add(playbackSpeed = new JTextField(5), c);
		c.gridx++;
		panel.add(defButton = new JButton("Default"), c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 4;
		playSpeedSlider = new PlaySpeedSlider();
		panel.add(playSpeedSlider.getSlider(), c);
		
		defButton.addActionListener(new DefSampleRateAction());
		playbackSpeed.setEditable(false);
		inputSampleRate.setEditable(false);
		playSpeedSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				playSpeedChange();
			}
		});
		outputSampleRate.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				sayDecimateInfo();
			}
		});
		deviceTypes.setToolTipText("Select type of output device");
		soundCards.setToolTipText("Select output device");
		inputSampleRate.setToolTipText("This is the sample rate of the incoming data.");
		outputSampleRate.setToolTipText("<html>This is the sample rate data will be played back at.<br>"+
				"Data will automatically be decimated or upsampled to convert from input sample rate to the output sample rate."+
				"<br>It must be set to a rate that is possible with the selected output device.");
		playSpeedSlider.getSlider().setToolTipText("This is the playback speed relative to the true data speed." );
		
	}
	
	private void playSpeedChange() {
		playbackSpeed.setText(playSpeedSlider.getRatioString());
		sayDecimateInfo();
	}
	
	/**
	 * Work out what the decimation rate is and say it. 
	 */
	private void sayDecimateInfo() {
		double inFS =  playbackControl.getPlaybackProcess().getSampleRate();
		double outFS = inFS;
		try {
			outFS = Double.valueOf(outputSampleRate.getText());
		}
		catch (NumberFormatException e) {
		}
		double deciFac = inFS/outFS*playSpeedSlider.getDataValue();
		if (deciFac == 1) {
			decimateInfo.setText("No Decimator");
		}
		else if (deciFac > 1.) {
			decimateInfo.setText(String.format("Decimate x%3.1f", deciFac));
		}
		else {
			decimateInfo.setText(String.format("Upsample x%3.1f", 1./deciFac));
		}
		
	}

	class DefSampleRateAction implements ActionListener {

		public void actionPerformed(ActionEvent e) {

			playSpeedSlider.setDataValue(1.0);
//			if (defaultRate.isSelected()) {
//				saySampleRate();
				enableControls();
//			}
			
		}
		
	}
	
	
	
	@Override
	Component getComponent() {
		return panel;
	}

	@Override
	public void dataSourceChanged(PamDataBlock dataSource) {
		if (dataSource == null) {
			return;
		}
		this.dataSource = dataSource;
		if (this.playbackParameters != null) {
			setParams(playbackParameters);
		}
	}

	private double getInputRate() {
		if (dataSource != null) {
			return dataSource.getSampleRate();
		}
		else {
			return playbackControl.getPlaybackProcess().getSampleRate();
		}
	}

	@Override
	PlaybackParameters getParams(PlaybackParameters playbackParameters) {
		playbackParameters.deviceType = deviceTypes.getSelectedIndex();
		playbackParameters.deviceNumber = soundCards.getSelectedIndex();
//		playbackParameters.defaultSampleRate = defaultRate.isSelected();
		double sourceSR = playbackControl.getSourceSampleRate();
		try {
			double playbackRate = Double.valueOf(outputSampleRate.getText());
			playbackParameters.setPlaybackRate((float) playbackRate); 
			playbackParameters.setPlaybackSpeed(playSpeedSlider.getDataValue());
		}
		catch (NumberFormatException ex) {
			return null;
		}
		return playbackParameters;
	}

	@Override
	void setParams(PlaybackParameters playbackParameters) {
		isRT = playbackControl.isRealTimePlayback();
		this.playbackParameters = playbackParameters;
		deviceTypes.removeAllItems();
		for (int i = 0; i < filePlayback.getFilePBDevices().size(); i++) {
			deviceTypes.addItem(filePlayback.getFilePBDevices().get(i).getName());
		}
		deviceTypes.setSelectedIndex(playbackParameters.deviceType);
		
		double fs = getInputRate();
		inputSampleRate.setText(String.format("%3.1f", fs));
		outputSampleRate.setText(String.format("%3.1f", playbackParameters.getPlaybackRate()));
		playSpeedSlider.setDataValue(playbackParameters.getPlaybackSpeed());
		playSpeedChange();
//		
//		soundCards.removeAllItems();
//		ArrayList<Info> mixers = SoundCardSystem.getOutputMixerList();
//		for (int i = 0; i < mixers.size(); i++) {
//			soundCards.addItem(mixers.get(i).getName());
//		}
//		soundCards.setSelectedIndex(playbackParameters.deviceNumber);
		
//		defaultRate.setSelected(playbackParameters.defaultSampleRate);
		
//		saySampleRate();
		
		fillDeviceSpecificList();
		
		enableControls();
	}
	
	private class NewDeviceType implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			fillDeviceSpecificList();
			PlaybackDialog parentDialog = getParentDialog();
			if (parentDialog != null) {
				parentDialog.pack();
			}
		}

	}

	private class NewSoundCard implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (filePlayback != null) {
				filePlayback.notifyObservers();
			}
			
		}
	}
	
	public void fillDeviceSpecificList() {
		int deviceType = deviceTypes.getSelectedIndex();
		deviceType = Math.max(0, deviceType);
		FilePlaybackDevice selectedDeviceType = filePlayback.getFilePBDevices().get(deviceType);
		soundCards.removeAllItems();
		String[] devList = selectedDeviceType.getDeviceNames();
		for (int i = 0; i < devList.length; i++) {
			soundCards.addItem(devList[i]);
		}
		if (playbackParameters.deviceNumber < devList.length) {
			soundCards.setSelectedIndex(playbackParameters.deviceNumber);
		}
	}
	
//	void saySampleRate() {
//		double playbackRate;
//		try {
//			playbackRate = Float.valueOf(sampleRate.getText());
//		}
//		catch (NumberFormatException ex) {
//			playbackRate = 0;
//		}
//		
//		playbackRate = playbackControl.getSourceSampleRate(); 
//		playbackRate = playbackControl.playbackParameters.getPlaybackSpeed(playbackRate);
//		
//		if (defaultRate.isSelected() || playbackRate == 0) {
//			playbackRate = playbackControl.playbackProcess.getSampleRate();
//		}
//		
//		sampleRate.setText(String.format("%.0f", playbackRate));
//		
//	}
	
	private void enableControls() {
//		boolean ad = autoDecimate.isSelected();
//		defaultRate.setEnabled(ad == false);
//		if (ad == true) {
//			defaultRate.setSelected(false);
//		}
//		sampleRate.setEnabled(defaultRate.isSelected() == false);
		playSpeedSlider.setEnabled(!isRT);
		defButton.setEnabled(!isRT);
		if (isRT) {
			playSpeedSlider.setDataValue(1.0);
		}
	}
	
	
	
	
}
