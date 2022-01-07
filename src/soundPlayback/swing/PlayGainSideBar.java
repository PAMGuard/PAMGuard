package soundPlayback.swing;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import soundPlayback.preprocess.PlaybackGain;
import soundPlayback.preprocess.PreprocessSwingComponent;

public class PlayGainSideBar implements PreprocessSwingComponent {

	private PlaybackGain playbackGain;
	
	private BasicSidebarLayout basicSidebarLayout;
	
	private PlayGainSlider playGainSlider;

	public PlayGainSideBar(PlaybackGain playbackGain) {
		this.playbackGain = playbackGain;
		basicSidebarLayout = BasicSidebarLayout.makeBasicLayout(playGainSlider = new PlayGainSlider());
		basicSidebarLayout.setToolTipText("<html>Adjust the output volume.<br>"+
		"N.B. You should also consider turning up the volume in the computers volume controls.");
		playGainSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				gainChanged();
			}

		});
	}

	@Override
	public JComponent getComponent() {
		return basicSidebarLayout.getComponent();
	}

	@Override
	public void update() {
		playGainSlider.setDataValue(playbackGain.getGaindB());
		sayGain();
	}
	
	private void sayGain() {
		basicSidebarLayout.setTextLabel(playbackGain.getTextValue());
	}
	
	private void gainChanged() {
		playbackGain.setGaindB(playGainSlider.getDataValue());
		sayGain();
	}

}
