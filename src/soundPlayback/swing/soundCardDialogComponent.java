package soundPlayback.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;

import javax.sound.sampled.Mixer.Info;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import Acquisition.SoundCardSystem;
import soundPlayback.PlaybackParameters;

public class soundCardDialogComponent extends PlaybackDialogComponent {

	private JPanel panel;

	private JComboBox soundCards;
	
	private SoundCardSystem soundCardSystem;
	
//	private PlaybackControl playbackControl;
	
	public soundCardDialogComponent(SoundCardSystem soundCardSystem) {
		super();
		this.soundCardSystem = soundCardSystem;
//		this.playbackControl = playbackControl;
		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(new TitledBorder("Options"));
		soundCards = new JComboBox();
		panel.add(BorderLayout.NORTH, new JLabel("Output device ..."));
		panel.add(BorderLayout.CENTER, soundCards);
		JTextArea warning = new JTextArea();
		warning.setText("You must select output channels on the same device that " +
				"you are using for sound input. Otherwise you will run into synchronisation " +
				"problems due to the clocks in the different devices running at different speeds");
		warning.setLineWrap(true);
		warning.setWrapStyleWord(true);
		warning.setEditable(false);
		warning.setBackground(panel.getBackground());
		warning.setBorder(new EmptyBorder(3,3,3,3));
		
		panel.add(BorderLayout.SOUTH, warning);
//
//		JPanel p = new JPanel();
//		GridBagLayout layout = new GridBagLayout();
//		p.setLayout(layout);
//		GridBagConstraints c = new PamGridBagContraints();
//		c.gridwidth = 3;
//		c.fill = GridBagConstraints.HORIZONTAL;
//		c.anchor = GridBagConstraints.WEST;
//		c.gridx = c.gridy = 0;
//		PamDialog.addComponent(p, new JLabel("Output device ..."), c);
//		c.gridy++;
//		PamDialog.addComponent(p, soundCards, c);
//		panel.add(BorderLayout.CENTER, p);
	}

	@Override
	Component getComponent() {
		return panel;
	}

	@Override
	PlaybackParameters getParams(PlaybackParameters playbackParameters) {
		playbackParameters.deviceNumber = soundCards.getSelectedIndex();
		return playbackParameters;
	}

	@Override
	void setParams(PlaybackParameters playbackParameters) {
		soundCards.removeAllItems();
		ArrayList<Info> mixers = SoundCardSystem.getOutputMixerList();
		for (int i = 0; i < mixers.size(); i++) {
			soundCards.addItem(mixers.get(i).getName());
		}
		soundCards.setSelectedIndex(playbackParameters.deviceNumber);
	}

}
