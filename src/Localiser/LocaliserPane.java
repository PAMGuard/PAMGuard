package Localiser;

import java.awt.Window;

import PamController.SettingsPane;
import group3dlocaliser.algorithm.LocaliserAlgorithmParams;

public abstract class LocaliserPane<T> {

	public LocaliserPane() {
		// TODO Auto-generated constructor stub
	}


	/**
	 * Get the FX pane with settings. 
	 * @return the settings pane for the localiser. 
	 */
	public abstract SettingsPane<T> getSettingsPane(); 
	

	/**
	 * Get the dialog for the localiser. This is usually used
	 * for Swing settings panes
	 * @return the settings dialog. 
	 */
	public abstract LocaliserAlgorithmParams showAlgorithmDialog(Window awtWindow,
			LocaliserAlgorithmParams algorithmPaams); 

}
