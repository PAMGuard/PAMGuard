package soundPlayback.swing;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.kordamp.ikonli.materialdesign2.MaterialDesignV;

import PamView.component.PamFontIcon;
import soundPlayback.preprocess.PlaybackGain;
import soundPlayback.preprocess.PreprocessSwingComponent;

public class PlayGainSideBar implements PreprocessSwingComponent {

	private PlaybackGain playbackGain;
	
	private BasicSidebarLayout basicSidebarLayout;
	
	private PlayGainSlider playGainSlider;

	private JToggleButton muteButton;

	public PlayGainSideBar(PlaybackGain playbackGain) {
		this.playbackGain = playbackGain;
		muteButton = new JToggleButton(PamFontIcon.of(MaterialDesignV.VOLUME_HIGH, PamFontIcon.MENU_SIZE));
		muteButton.setSelectedIcon(PamFontIcon.of(MaterialDesignV.VOLUME_OFF, PamFontIcon.MENU_SIZE));
		muteButton.setMargin(new Insets(0, 6, 0, 6));
		BasicSidebarLayout.setActionButtonWidth(muteButton);
		basicSidebarLayout = BasicSidebarLayout.makeBasicLayout(playGainSlider = new PlayGainSlider(), muteButton);
		basicSidebarLayout.setToolTipText("<html>Adjust the output volume.<br>"+
		"N.B. You should also consider turning up the volume in the computers volume controls.");
		muteButton.setToolTipText("Mute the output");
		playGainSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				gainChanged();
			}

		});
		muteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				muteChanged();
			}
		});
	}

	private void muteChanged() {
		boolean muted = muteButton.isSelected();
		playbackGain.setMuted(muted);
		playGainSlider.getSlider().setEnabled(!muted);
		muteButton.setToolTipText(muted ? "Unmute the output" : "Mute the output");
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
