package clickTrainDetector.layout;

import java.awt.Component;

import javafx.scene.layout.Pane;

/**
 * Handles GUI interactions with each click train detection <i>algorithm</i>. The click train
 * detector can choose between multiple algorithms.
 * 
 * @author Jamie Macaulay 
 *
 */
public interface CTDetectorGraphics {
	
	/**
	 * Pane with settings specific to the click train algorithm 
	 * @return - the pane for click train algorithm settings. 
	 */
	public Pane getCTSettingsPane();
	
	/**
	 * A side pane to show some basic metrics. Can be null
	 * @return - the side pane.
	 */
	public Pane getCTSidePane();

	/**
	 * Called to save the params from the settings pane. 
	 */
	public boolean getParams();
	
	/**
	 * Notify update.
	 * @param flag - the update flag. 
	 * @param - data object. Can be null. 
	 */
	public void notifyUpdate(int flag, Object object);

	/**
	 * Get the swing version of the side pane. 
	 * @return the swing side pane. 
	 */
	public Component getCTSidePaneSwing();
		

}
