package soundPlayback.swing;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import PamView.PamColors.PamColor;
import soundPlayback.preprocess.PlaybackDecimator;
import soundPlayback.preprocess.PreprocessSwingComponent;

public class DecimatorSideBar implements PreprocessSwingComponent {

	private static final double DEFAULT_PLAY_SPEED = 1;

	private PlaybackDecimator playbackDecimator;

	private PlaySpeedSlider playSpeedSlider;

	private BasicSidebarLayout basicSidebarLayout;

	private JButton defaultSpeedButton;

	public DecimatorSideBar(PlaybackDecimator playbackDecimator) {
		this.playbackDecimator = playbackDecimator;
		playSpeedSlider = new PlaySpeedSlider(PamColor.BORDER);
		defaultSpeedButton = new JButton("x1");
		defaultSpeedButton.setMargin(new Insets(0, 6, 0, 6));
		BasicSidebarLayout.setActionButtonWidth(defaultSpeedButton);
		basicSidebarLayout = BasicSidebarLayout.makeBasicLayout(playSpeedSlider, defaultSpeedButton);
		basicSidebarLayout.setToolTipText("<html>Control the playback speed.<br>Note that the data's sample rate "+
		"will automatically adjust<br>based on the input rate, output rate and speed factor");
		defaultSpeedButton.setToolTipText("Reset the playback speed to x1");
		playSpeedSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				speedChanged();
			}
		});
		defaultSpeedButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				playSpeedSlider.setDataValue(DEFAULT_PLAY_SPEED);
			}
		});
	}

	protected void speedChanged() {
		playbackDecimator.setPlaySpeed(playSpeedSlider.getDataValue());
		saySpeed();
	}

	@Override
	public JComponent getComponent() {
		return basicSidebarLayout.getComponent();
	}

	@Override
	public void update() {
		basicSidebarLayout.getComponent().setVisible(playbackDecimator.makeVisible());
		playSpeedSlider.setDataValue(playbackDecimator.getPlaySpeed());
		saySpeed();
	}

	private void saySpeed() {
		basicSidebarLayout.setTextLabel("Speed " + playSpeedSlider.getRatioString());
		defaultSpeedButton.setEnabled(playSpeedSlider.getDataValue() != DEFAULT_PLAY_SPEED);
	}

}
