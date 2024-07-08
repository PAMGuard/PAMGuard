package Acquisition.pamAudio;

import PamView.panel.PamPanel;
import javafx.scene.layout.Pane;

public interface PamAudioSettingsPane {
	
	/**
	 * Get the JavaFX pane for loading audio. 
	 * @return
	 */
	public Pane getAudioLoaderPane();
	
	/**
	 * Get the swing audio loader pane. 
	 * @return
	 */
	public PamPanel getAudioLoaderPanel();


}
