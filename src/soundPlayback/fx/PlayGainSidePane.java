package soundPlayback.fx;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javafx.scene.layout.Pane;
import soundPlayback.preprocess.PlaybackGain;
import soundPlayback.preprocess.PreProcessFXPane;
import soundPlayback.swing.BasicSidebarLayout;
import soundPlayback.fx.PlayGainSlider;

public class PlayGainSidePane implements PreProcessFXPane {

	private PlaybackGain playbackGain;
	
	private BasicSidebarLayout basicSidebarLayout;
	
	private PlayGainSlider playGainSlider;

	public PlayGainSidePane(PlaybackGain playbackGain) {
		this.playbackGain = playbackGain;
		playGainSlider = new PlayGainSlider();
		
		basicSidebarLayout.setToolTipText("<html>Adjust the output volume.<br>"+
		"N.B. You should also consider turning up the volume in the computers volume controls.");
		playGainSlider.addChangeListener((oldval, newVal, obsVal)->{
				gainChanged();
			});
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

	@Override
	public Pane getPane() {
		return null;
	}

}
