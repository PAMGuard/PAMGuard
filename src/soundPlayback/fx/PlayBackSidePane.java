package soundPlayback.fx;

import java.util.ArrayList;

import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.sliders.PamSlider;
import soundPlayback.PlaybackControl;
import soundPlayback.preprocess.PlaybackPreprocess;
import soundPlayback.preprocess.PreProcessFXPane;

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


		PamHBox hBox = new PamHBox();
		hBox.setSpacing(5);

		PamVBox vBox = new PamVBox();
		vBox.setSpacing(5);

		borderPane.setCenter(hBox);
		borderPane.setBottom(vBox);


		ArrayList<PlaybackPreprocess> preProcesses = playBackControl.getPlaybackProcess().getPreProcesses();

		PreProcessFXPane preProcessFXPane;
		for (int i=0; i<preProcesses.size() ; i++) {
			preProcessFXPane = preProcesses.get(i).getSideParPane();
			if (preProcessFXPane!=null) {
				hBox.getChildren().add(preProcessFXPane.getPane());
			}
		}

		return borderPane;

	}

}
