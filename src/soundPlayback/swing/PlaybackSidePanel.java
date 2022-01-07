package soundPlayback.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import PamView.PamSidePanel;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamTextDisplay;
import PamView.dialog.ScrollingPamLabel;
import PamView.panel.PamPanel;
import soundPlayback.PlayDeviceState;
import soundPlayback.PlaybackControl;
import soundPlayback.PlaybackSystem;
import soundPlayback.preprocess.PlaybackPreprocess;
import soundPlayback.preprocess.PreprocessSwingComponent;

public class PlaybackSidePanel implements PamSidePanel {

	private PamPanel mainPanel;
	private PlaybackControl playbackControl;
	private ScrollingPamLabel deviceName;

	private Timer deviceTimer;
	
	public PlaybackSidePanel(PlaybackControl playbackControl) {
		this.playbackControl = playbackControl;
		mainPanel = new PlaybackPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder(playbackControl.getUnitName()));
		GridBagConstraints c = new PamGridBagContraints();
		c.insets = new Insets(0, 2, 0, 2);
		c.ipady = 0;
		deviceName = new ScrollingPamLabel(20);
		mainPanel.add(deviceName, c);
		ArrayList<PlaybackPreprocess> preProcesses = playbackControl.getPlaybackProcess().getPreProcesses();
		for (PlaybackPreprocess pp : preProcesses) {
			PreprocessSwingComponent comp = pp.getSideParComponent();
			if (comp != null) {
				c.gridy++;
				mainPanel.add(comp.getComponent(), c);
			}
		}
		deviceTimer = new Timer(2000, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				sayPlaybackDevice();
			}
		});
		deviceTimer.start();
	}


	private class PlaybackPanel extends PamPanel {

		public PlaybackPanel(GridBagLayout gridBagLayout) {
			super(gridBagLayout);
			setToolTipText("Playback controls");
		}

		@Override
		public String getToolTipText() {
			//			return super.getToolTipText();
			PlayDeviceState status = playbackControl.getDeviceState();
			if (status == null) {
				return super.getToolTipText();
			}
			else {
				return status.toString();
			}
		}

		@Override
		public String getToolTipText(MouseEvent event) {
			PlayDeviceState status = playbackControl.getDeviceState();
			if (status == null) {
				return super.getToolTipText(event);
			}
			else {
				return status.toString();
			}
		}

	}


	public void newSettings() {
		boolean isRT = playbackControl.isRealTimePlayback();
		ArrayList<PlaybackPreprocess> preProcesses = playbackControl.getPlaybackProcess().getPreProcesses();
		for (PlaybackPreprocess pp : preProcesses) {
			PreprocessSwingComponent comp = pp.getSideParComponent();
			if (comp != null) {
				comp.update();
			}
		}
		mainPanel.setVisible(playbackControl.getPlaybackParameters().getSidebarShow() > 0);
		//		speedLabel.setVisible(!isRT);
		//		speedSlider.getSlider().setVisible(!isRT);
		//		PlaybackParameters params = playbackControl.getPlaybackParameters();
		//		gainLabel.setText(String.format("Gain %d dB", params.playbackGain));
		//		gainSlider.setGain(params.playbackGain);
		//		sayPlaySpeed(speedSlider.getSpeed());
		//		speedSlider.setSpeed(params.getPlaybackSpeed());
	}


	@Override
	public JComponent getPanel() {
		return mainPanel;
	}

	@Override
	public void rename(String newName) {
		// TODO Auto-generated method stub

	}


	protected void sayPlaybackDevice() {
		deviceName.setText(getDeviceName());		
	}
	
	private String getDeviceName() {
		if (playbackControl == null) {
			return "";
		}
		PlaybackSystem playbackSystem = playbackControl.getPlaybackSystem();
		if (playbackSystem == null) {
			return "No playback";
		}
		return playbackSystem.getDeviceName();
	}

}
