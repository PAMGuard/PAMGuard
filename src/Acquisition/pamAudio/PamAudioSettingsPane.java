package Acquisition.pamAudio;

import PamView.panel.PamPanel;
import javafx.scene.layout.Pane;

/**
 * User controls to change bespoke settings for audio loaders. 
 */
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

	/**
	 * Get the parameters. This called whenever the settings dialog or pane is closed. 
	 */
	public void getParams();

	/**
	 * Set parameters. This is called when the dialog or pane is opened. 
	 */
	public void setParams();


}
