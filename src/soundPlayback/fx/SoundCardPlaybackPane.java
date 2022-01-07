package soundPlayback.fx;

import java.util.ArrayList;

import javax.sound.sampled.Mixer.Info;
import Acquisition.SoundCardSystem;
import PamguardMVC.PamDataBlock;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import soundPlayback.PlaybackParameters;
import soundPlayback.SoundCardPlayback;

/**e
 * Settings pane for sound cards. 
 * 
 * @author Jamie Macaulay
 *
 */
public class SoundCardPlaybackPane extends PlaybackSettingsPane {
	
	/**
	 * The main panel. 
	 */
	private PamBorderPane panel;

	/**
	 * Sound cards. 
	 */
	private ComboBox<String> soundCards; 
	
	public SoundCardPlaybackPane(SoundCardPlayback soundCardPlayback) {
		//mainPane = new PamBorderPane(new Label("Make the sound pane Jamie")); 
		
		panel = new PamBorderPane();
		panel.setPadding(new Insets(10,0,0,0));
		
		//panel.setBorder(new TitledBorder("Options"));
		soundCards = new ComboBox<String>();
		soundCards.prefWidthProperty().bind(panel.widthProperty());
		
		Label outputLabel = new Label("Output device ...");
		PamGuiManagerFX.titleFont2style(outputLabel);
		panel.setTop(outputLabel);
		panel.setCenter(soundCards);
		
		
		Label warning = new Label();
		warning.setWrapText(true);
		warning.setText("You must select output channels on the same device that " +
				"you are using for sound input. Otherwise you will run into synchronisation " +
				"problems due to the clocks in the different devices running at different speeds");
//		warning.setLineWrap(true);
//		warning.setWrapStyleWord(true);
		//warning.setBackground(panel.getBackground());
		//warning.setBorder(new EmptyBorder(3,3,3,3));
		
		panel.setBottom(warning);
		
		panel.setPrefWidth(PlayBackPane.PREF_WIDTH);
		
	}

	
	public void dataSourceChanged(PamDataBlock<?> source) {
		// TODO Auto-generated method stub
	}


	public void setParentDialog(PlayBackPane playBackPane) {
		// TODO Auto-generated method stub	
	}

	Pane getPane() {
		return panel;
	}

	@Override
	PlaybackParameters getParams(PlaybackParameters playbackParameters) {
		playbackParameters.deviceNumber = soundCards.getSelectionModel().getSelectedIndex();
		return playbackParameters;
	}

	@Override
	public void setParams(PlaybackParameters playbackParameters) {
		soundCards.getItems().clear();
		ArrayList<Info> mixers = SoundCardSystem.getOutputMixerList();
		for (int i = 0; i < mixers.size(); i++) {
			soundCards.getItems().add(mixers.get(i).getName());
		}
		soundCards.getSelectionModel().select(playbackParameters.deviceNumber);
	}

	
	
}
