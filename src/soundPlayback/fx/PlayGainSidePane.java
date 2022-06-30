package soundPlayback.fx;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;
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


	private Label iconLabel;


	private static final double DEFAULT_GAIN = 0; 


//	private PamHBox labelHBox;

	public PlayGainSidePane(PlaybackGain playbackGain) {
		this.playbackGain = playbackGain;
		playGainSlider = new PlayGainSlider();

		playGainSlider.getSlider().setTooltip(new Tooltip("<html>Adjust the output volume.<br>"+
				"N.B. You should also consider turning up the volume in the computers volume controls."));
		playGainSlider.addChangeListener((oldval, newVal, obsVal)->{
			gainChanged();
		});
		
		//create the label which also has a default button
		
		defaultGainButton = new PamButton("0 dB");
		defaultGainButton.setGraphic(PamGlyphDude.createPamIcon("mdi2r-refresh", PamGuiManagerFX.iconSize-3));
		defaultGainButton.setPrefWidth(70);
		defaultGainButton.setOnAction((action)->{
			playGainSlider.setDataValue(0);
		});

		label = new Label("Gain"); 
		
//		labelHBox = new PamHBox();
//		labelHBox.setAlignment(Pos.CENTER_LEFT);
//		labelHBox.setSpacing(5);
//		labelHBox.getChildren().addAll(label, defaultGainButton);
	
		lowVol = PamGlyphDude.createPamIcon("mdi2v-volume-low", PamGuiManagerFX.iconSize);
		medVol = PamGlyphDude.createPamIcon("mdi2v-volume-medium", PamGuiManagerFX.iconSize);					
		highVol = PamGlyphDude.createPamIcon("mdi2v-volume-high", PamGuiManagerFX.iconSize);
		
		iconLabel = new Label();
		
		this.mainPane = new PamBorderPane();
		
		PamHBox hBox = new PamHBox();
		hBox.setAlignment(Pos.CENTER_LEFT);
		hBox.setSpacing(5);
		hBox.getChildren().addAll(iconLabel, playGainSlider);
		playGainSlider.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(playGainSlider, Priority.ALWAYS);
		
		this.mainPane.setCenter(hBox);
			
		setLabelVolGrpahic();

	}

	private void setLabelVolGrpahic() {
		//System.out.println("Playback gain: " + playbackGain.getGaindB());

		if (playbackGain.getGaindB()<PlayGainSlider.MINGAIN+20 && iconLabel.getGraphic()!=lowVol)
			iconLabel.setGraphic(lowVol);
		else if (playbackGain.getGaindB()>=PlayGainSlider.MINGAIN+20 && playbackGain.getGaindB()<PlayGainSlider.MAXGAIN-20 
				&& iconLabel.getGraphic()!=medVol) {
			iconLabel.setGraphic(medVol); 
		}
		else if ( playbackGain.getGaindB()>=PlayGainSlider.MAXGAIN-20 && iconLabel.getGraphic()!=highVol) {
			iconLabel.setGraphic(highVol);
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
		defaultGainButton.setDisable(false);
		playbackGain.setGaindB(playGainSlider.getDataValue());
		setLabelVolGrpahic();
		sayGain();
		if (playbackGain.getGaindB()==DEFAULT_GAIN) {
			defaultGainButton.setDisable(true);
		}
		
	}

	@Override
	public Pane getPane() {
		return mainPane;
	}

	@Override
	public Node getLabel() {
		return label;
	}

	@Override
	public Node getDefaultButton() {
		return defaultGainButton;
	}

}
