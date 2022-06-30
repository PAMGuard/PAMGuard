package soundPlayback.fx;

import java.util.ArrayList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import pamViewFX.PamGuiFX;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.sliders.PamSlider;
import soundPlayback.PlaybackControl;
import soundPlayback.preprocess.PlaybackPreprocess;
import soundPlayback.preprocess.PreProcessFXPane;
import soundPlayback.preprocess.PreprocessSwingComponent;

/**
 * Th eplay back side pane. 
 * @author Jamie Macaulay
 *
 */
public class PlayBackSidePane extends BorderPane {

	/**
	 * The play back control.
	 */
	private PlaybackControl playBackControl;

	/**
	 * Label which shows which sound ouptut device is being used. 
	 */
	private Label deviceLabel;

	public PlayBackSidePane(PlaybackControl playBackControl) {
		this.playBackControl=playBackControl; 
		this.setCenter(createSidePane());
	}

	/**
	 * Create side pane.
	 * @return the side pane. 
	 */
	private Pane createSidePane() {

		PamBorderPane borderPane = new PamBorderPane(); 

		borderPane.setPrefWidth(PamGuiFX.SIDE_PANE_PREF_WIDTH-10);
		borderPane.setMaxWidth(Double.MAX_VALUE);


		Label titlelabel = new Label("Playback"); 
		PamGuiManagerFX.titleFont2style(titlelabel);
		//		titlelabel.setGraphic(PamGlyphDude.createPamIcon("mdi2p-play", PamGuiManagerFX.iconSize));

		borderPane.setTop(titlelabel);


		PamHBox hBox = new PamHBox();
		hBox.setSpacing(5);
		hBox.getChildren().add(deviceLabel= new Label(""));

		borderPane.setCenter(hBox);



		PamVBox vBox = new PamVBox();
		vBox.setSpacing(0);
		vBox.setPadding(new Insets(5,0,0,0));

		ArrayList<PlaybackPreprocess> preProcesses = playBackControl.getPlaybackProcess().getPreProcesses();

		PreProcessFXPane preProcessFXPane;
		for (int i=0; i<preProcesses.size() ; i++) {
			//System.out.println("PLAYBACKSIDEPANE: ADDING : " + preProcesses.get(i).toString());
			preProcessFXPane = preProcesses.get(i).getSideParPane();
			if (preProcessFXPane!=null) {

				PamBorderPane labelHBox = new PamBorderPane();
//				labelHBox.setAlignment(Pos.CENTER_LEFT);
//				labelHBox.setSpacing(5);
				labelHBox.setLeft(preProcessFXPane.getLabel());
				labelHBox.setRight(preProcessFXPane.getDefaultButton() == null ? new Label() : preProcessFXPane.getDefaultButton());

				vBox.getChildren().add(labelHBox);
				vBox.getChildren().add(preProcessFXPane.getPane());
				vBox.getChildren().add(new Label(""));


//					if (preProcessFXPane.getDefaultButton()!=null) {
//												
//					//only show the default button when the user is inside the slider to reduce clutter. 
//					final Node defaultButton = preProcessFXPane.getDefaultButton();
//					labelHBox.setOnMouseEntered((e)->{
//						defaultButton.setVisible(true);
//					});
//	
//					labelHBox.setOnMouseExited((e)->{
//						defaultButton.setVisible(false);
//					});
//					
//					preProcessFXPane.getPane().setOnMouseEntered((e)->{
//						defaultButton.setVisible(true);
//					});
//	
//					preProcessFXPane.getPane().setOnMouseExited((e)->{
//						defaultButton.setVisible(false);
//					});
//				
//				}
			}
		}

		borderPane.setBottom(vBox);

		return borderPane;

	}



	public void newSettings() {
		boolean isRT = playBackControl.isRealTimePlayback();
		ArrayList<PlaybackPreprocess> preProcesses = playBackControl.getPlaybackProcess().getPreProcesses();
		for (PlaybackPreprocess pp : preProcesses) {
			PreProcessFXPane comp = pp.getSideParPane();
			if (comp != null) {
				comp.update();
			}
		}
		this.setVisible(playBackControl.getPlaybackParameters().getSidebarShow() > 0);
		//		speedLabel.setVisible(!isRT);
		//		speedSlider.getSlider().setVisible(!isRT);
		//		PlaybackParameters params = playbackControl.getPlaybackParameters();
		//		gainLabel.setText(String.format("Gain %d dB", params.playbackGain));
		//		gainSlider.setGain(params.playbackGain);
		//		sayPlaySpeed(speedSlider.getSpeed());
		//		speedSlider.setSpeed(params.getPlaybackSpeed());
	}

}
