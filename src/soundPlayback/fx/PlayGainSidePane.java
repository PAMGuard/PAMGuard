package soundPlayback.fx;

import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import soundPlayback.preprocess.PlaybackGain;
import soundPlayback.preprocess.PreProcessFXPane;

public class PlayGainSidePane implements PreProcessFXPane {

	private PlaybackGain playbackGain;


	private PlayGainSlider playGainSlider;

	private PamBorderPane mainPane;


	private Label label;


	private Text medVol;


	private Text lowVol;


	private Text highVol;


	private PamButton defaultGainButton;

	public PlayGainSidePane(PlaybackGain playbackGain) {
		this.playbackGain = playbackGain;
		playGainSlider = new PlayGainSlider();

		playGainSlider.getSlider().setTooltip(new Tooltip("<html>Adjust the output volume.<br>"+
				"N.B. You should also consider turning up the volume in the computers volume controls."));
		playGainSlider.addChangeListener((oldval, newVal, obsVal)->{
			gainChanged();
		});
		
		playGainSlider.getChildren().add(defaultGainButton = new PamButton("0 dB"));
		defaultGainButton.setGraphic(PamGlyphDude.createPamIcon("mdi2r-refresh", PamGuiManagerFX.iconSize-3));
		defaultGainButton.setPrefWidth(60);
		defaultGainButton.setOnAction((action)->{
			playGainSlider.setDataValue(0);
		});

		label = new Label("Gain"); 
		lowVol = PamGlyphDude.createPamIcon("mdi2v-volume-low", PamGuiManagerFX.iconSize);
		medVol = PamGlyphDude.createPamIcon("mdi2v-volume-medium", PamGuiManagerFX.iconSize);					
		highVol = PamGlyphDude.createPamIcon("mdi2v-volume-high", PamGuiManagerFX.iconSize);
		
		setLabelVolGrpahic();

		this.mainPane = new PamBorderPane();
		this.mainPane.setCenter(playGainSlider);
	}

	private void setLabelVolGrpahic() {
		//System.out.println("Playback gain: " + playbackGain.getGaindB());

		if (playbackGain.getGaindB()<PlayGainSlider.MINGAIN+20 && label.getGraphic()!=lowVol)
			label.setGraphic(lowVol);
		else if (playbackGain.getGaindB()>=PlayGainSlider.MINGAIN+20 && playbackGain.getGaindB()<PlayGainSlider.MAXGAIN-20 
				&& label.getGraphic()!=medVol) {
			label.setGraphic(medVol); 
		}
		else if ( playbackGain.getGaindB()>=PlayGainSlider.MAXGAIN-20 && label.getGraphic()!=highVol) {
			label.setGraphic(highVol);
		}


	}

	@Override
	public void update() {
		playGainSlider.setDataValue(playbackGain.getGaindB());
		setLabelVolGrpahic();
		sayGain();
	}

	private void sayGain() {
		//playGainSlider.setTextLabel(playbackGain.getTextValue());
		label.setText(playbackGain.getTextValue());
	}

	private void gainChanged() {
		playbackGain.setGaindB(playGainSlider.getDataValue());
		setLabelVolGrpahic();
		sayGain();
	}

	@Override
	public Pane getPane() {
		return mainPane;
	}

	@Override
	public Label getLabel() {
		return label;
	}

}
