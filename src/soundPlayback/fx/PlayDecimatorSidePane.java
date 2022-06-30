package soundPlayback.fx;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;
import soundPlayback.preprocess.PlaybackDecimator;
import soundPlayback.preprocess.PlaybackGain;
import soundPlayback.preprocess.PreProcessFXPane;

public class PlayDecimatorSidePane implements PreProcessFXPane {

	private static final double DEFAULT_PLAY_SPEED = 1;


	private PlaybackDecimator playbackDecimator;


	private PlaySpeedSlider playSpeedSlider;

	private PamBorderPane mainPane;


	private Label label;


	private PamButton defaultSpeedButton;

	public PlayDecimatorSidePane(PlaybackDecimator playbackDecimator) {
		this.playbackDecimator = playbackDecimator;
		playSpeedSlider = new PlaySpeedSlider();

		playSpeedSlider.getSlider().setTooltip(new Tooltip("<html>Adjust the output volume.<br>"+
				"N.B. You should also consider turning up the volume in the computers volume controls."));
		
		playSpeedSlider.addChangeListener((oldval, newVal, obsVal)->{
			speedChanged();
		});

		playSpeedSlider.getChildren().add(defaultSpeedButton = new PamButton("x 1"));
		defaultSpeedButton.setGraphic(PamGlyphDude.createPamIcon("mdi2r-refresh", PamGuiManagerFX.iconSize-3));
		defaultSpeedButton.setPrefWidth(70);
		defaultSpeedButton.setOnAction((action)->{
			playSpeedSlider.setDataValue(DEFAULT_PLAY_SPEED);
		});
		
		label = new Label("Speed"); 
		
		PamHBox hBox = new PamHBox();
		hBox.setAlignment(Pos.CENTER_LEFT);
		hBox.setSpacing(5);
		hBox.getChildren().addAll(PamGlyphDude.createPamIcon("mdi2p-play", PamGuiManagerFX.iconSize), playSpeedSlider);
		playSpeedSlider.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(playSpeedSlider, Priority.ALWAYS);
	
		this.mainPane = new PamBorderPane();
		this.mainPane.setCenter(hBox);
	}

	@Override
	public void update() {
		playSpeedSlider.setVisible(playbackDecimator.makeVisible());
		playSpeedSlider.setDataValue(playbackDecimator.getPlaySpeed());
		saySpeed();
	}

	private void saySpeed() {
		//playSpeedSlider.setTextLabel("Speed " + playSpeedSlider.getRatioString());
		label.setText("Speed " + playSpeedSlider.getRatioString());

	}
	
	protected void speedChanged() {
		defaultSpeedButton.setDisable(false);
		playbackDecimator.setPlaySpeed(playSpeedSlider.getDataValue());
		if (playSpeedSlider.getDataValue()== DEFAULT_PLAY_SPEED) {
			defaultSpeedButton.setDisable(true);
		}
		saySpeed();
	}

	@Override
	public Pane getPane() {
		return mainPane;
	}

	@Override
	public Label getLabel() {
		return label;
	}

	@Override
	public Node getDefaultButton() {
		return defaultSpeedButton;
	}

}