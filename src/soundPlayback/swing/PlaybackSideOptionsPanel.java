package soundPlayback.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamNorthPanel;
import soundPlayback.PlaybackControl;
import soundPlayback.PlaybackParameters;

public class PlaybackSideOptionsPanel {

	private JCheckBox showSpeed, showEnvelope, showFilter, showGain;
	
	private JPanel mainPanel;

	private PlaybackControl playbackControl;
	
	public PlaybackSideOptionsPanel(PlaybackControl playbackControl) {
		this.playbackControl = playbackControl;
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		panel.add(showFilter = new JCheckBox("Show filter control"),c);
		c.gridy++;
		panel.add(showEnvelope = new JCheckBox("Show envelope mix"),c);
		c.gridy++;
		panel.add(showSpeed = new JCheckBox("Show speed control"),c);
		c.gridy++;
		panel.add(showGain = new JCheckBox("Show gain control"),c);
		c.gridy++;
		mainPanel = new PamNorthPanel(panel);
		mainPanel.setBorder(new TitledBorder("Side panel options"));
	}

	public JComponent getDialogComponent() {
		return mainPanel;
	}

	public void setParams(PlaybackParameters playbackParameters) {
		int showSel = playbackParameters.getSidebarShow();
		showFilter.setSelected((showSel & PlaybackParameters.SIDEBAR_SHOW_FILTER) != 0);
		showEnvelope.setSelected((showSel & PlaybackParameters.SIDEBAR_SHOW_ENVMIX) != 0);
		showGain.setSelected((showSel & PlaybackParameters.SIDEBAR_SHOW_GAIN) != 0);
		if (playbackControl.isRealTimePlayback()) {
			showSpeed.setEnabled(false);
//			showSpeed.setSelected(false);
		}
		else {
			showSpeed.setEnabled(true);
			showSpeed.setSelected((showSel & PlaybackParameters.SIDEBAR_SHOW_SPEED) != 0);
		}
	}

	public boolean getParams(PlaybackParameters playbackParameters) {
		int showSel = 0;
		if (showFilter.isSelected()) showSel |= PlaybackParameters.SIDEBAR_SHOW_FILTER;
		if (showEnvelope.isSelected()) showSel |= PlaybackParameters.SIDEBAR_SHOW_ENVMIX;
		if (showSpeed.isSelected()) showSel |= PlaybackParameters.SIDEBAR_SHOW_SPEED;
		if (showGain.isSelected()) showSel |= PlaybackParameters.SIDEBAR_SHOW_GAIN;
		
		playbackParameters.setSidebarShow(showSel);
		
		return true;
	}

}
