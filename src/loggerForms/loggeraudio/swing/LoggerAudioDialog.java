package loggerForms.loggeraudio.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.util.ArrayList;

import javax.sound.sampled.Mixer.Info;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import Acquisition.SoundCardSystem;
import PamUtils.SelectFolder;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import loggerForms.loggeraudio.LoggerAudioSettings;

public class LoggerAudioDialog extends PamDialog {

	private static LoggerAudioDialog singleInstance;
	private LoggerAudioSettings audioSettings;
	
	private JComboBox<String> cardList;
	private ArrayList<Info> mixers;
	
	private SelectFolder outputFolder;
	
	private JTextField bufferSeconds;
	private JTextField recordSeconds;
	
	private LoggerAudioDialog(Window parentFrame) {
		super(parentFrame, "Logger app audio", false);
		cardList = new JComboBox<>();
		cardList.setToolTipText("Sound card for audio output");
		outputFolder = new SelectFolder("Output folder", 40, true);
		bufferSeconds = new JTextField(3);
		recordSeconds = new JTextField(3);
		bufferSeconds.setToolTipText("Time to record before recording is initialised");
		recordSeconds.setToolTipText("Time to record after recording is initialised");
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		JPanel cardPanel = new JPanel(new BorderLayout());
		mainPanel.add(cardPanel);
		cardPanel.setBorder(new TitledBorder("Output device"));
		cardPanel.add(BorderLayout.CENTER, cardList);
		
		JPanel pp = new JPanel(new BorderLayout());
		pp.add(outputFolder.getFolderPanel(), BorderLayout.CENTER);
		mainPanel.add(pp);
		pp.setBorder(new TitledBorder("Output folder"));
		
		JPanel dataPanel = new JPanel(new GridBagLayout());
		mainPanel.add(dataPanel);
		dataPanel.setBorder(new TitledBorder("Data options"));
		GridBagConstraints c = new PamGridBagContraints();
		dataPanel.add(new JLabel("Buffer length ", JLabel.RIGHT), c);
		c.gridx++;
		dataPanel.add(bufferSeconds, c);
		c.gridx++;
		dataPanel.add(new JLabel(" seconds ", JLabel.LEFT), c);
		c.gridx = 0;
		c.gridy++;
		dataPanel.add(new JLabel("Record duration ", JLabel.RIGHT), c);
		c.gridx++;
		dataPanel.add(recordSeconds, c);
		c.gridx++;
		dataPanel.add(new JLabel(" seconds ", JLabel.LEFT), c);
		
		fillCardList();
		
		setDialogComponent(mainPanel);
	}
	
	public static LoggerAudioSettings showDialog(Window parentFrame, LoggerAudioSettings audioSettings) {
		if (singleInstance == null || singleInstance.getParent() != parentFrame) {
			singleInstance = new LoggerAudioDialog(parentFrame);
		}
		singleInstance.setParams(audioSettings);
		singleInstance.setVisible(true);
		return singleInstance.audioSettings;
	}

	private void fillCardList() {
		cardList.removeAllItems();
		mixers = SoundCardSystem.getOutputMixerList();
		for (int i = 0; i < mixers.size(); i++) {
			cardList.addItem(mixers.get(i).getName());
		}
	}
	
	private void setParams(LoggerAudioSettings audioSettings) {
		this.audioSettings =audioSettings;
		Info currMix = audioSettings.findMixer();
		for (int i = 0; i < mixers.size(); i++) {
			if (mixers.get(i).getName().equals(currMix.getName())) {
				cardList.setSelectedIndex(i);
				break;
			}
		}
		outputFolder.setFolderName(audioSettings.outputFolder);
		outputFolder.setIncludeSubFolders(audioSettings.outputSubFolders);
		
		bufferSeconds.setText(Integer.valueOf(audioSettings.bufferSeconds).toString());
		recordSeconds.setText(Integer.valueOf(audioSettings.recordSeconds).toString());
	}

	@Override
	public boolean getParams() {
		int ind = cardList.getSelectedIndex();
		if (ind < 0) {
			return showWarning("No output sound device selected");
		}
		audioSettings.outputDeviceName = mixers.get(ind).getName();
		audioSettings.outputFolder = outputFolder.getFolderName(true);
		audioSettings.outputSubFolders = outputFolder.isIncludeSubFolders();
		try {
			audioSettings.bufferSeconds = Integer.valueOf(bufferSeconds.getText());
			audioSettings.recordSeconds = Integer.valueOf(recordSeconds.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid recording or buffer seconds (must be integer");
		}
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		audioSettings = null;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
