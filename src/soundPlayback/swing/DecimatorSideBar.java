package soundPlayback.swing;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import PamView.PamColors.PamColor;
import soundPlayback.preprocess.PlaybackDecimator;
import soundPlayback.preprocess.PreprocessSwingComponent;

public class DecimatorSideBar implements PreprocessSwingComponent {

	private PlaybackDecimator playbackDecimator;
	
	private PlaySpeedSlider playSpeedSlider;
	
	private BasicSidebarLayout basicSidebarLayout;

	public DecimatorSideBar(PlaybackDecimator playbackDecimator) {
		this.playbackDecimator = playbackDecimator;
		playSpeedSlider = new PlaySpeedSlider(PamColor.BORDER);
		basicSidebarLayout = BasicSidebarLayout.makeBasicLayout(playSpeedSlider);
		basicSidebarLayout.setToolTipText("<html>Control the playback speed.<br>Note that the data's sample rate "+
		"will automatically adjust<br>based on the input rate, output rate and speed factor");
		playSpeedSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				speedChanged();
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
	}

}
