package Acquisition.layoutFX;

import PamController.SettingsPane;
import javafx.scene.layout.Pane;

/**
 * Settings pane for a DAQ specific nodes. 
 * @author Jamie Macaulay 
 *
 */
public abstract class DAQSettingsPane<T> extends SettingsPane<T>{

	public DAQSettingsPane() {
		super(null);
	}
	
	/**
	 * Called by the acquisition pane whenever parameters are to be set. 
	 */
	public abstract void setParams(); 
	
	/**
	 * Called by the acquisition pane whenever parameters are to be got. 
	 * 
	 */
	public abstract boolean getParams(); 
	
	/**
	 * Get a pane to be added to the shared toolbar/status bar area of the GUI.
	 * This pane should be compact (narrow height) as it sits within the control bar.
	 * <p>
	 * For example, a sound card system might return a level meter pane, 
	 * while a folder input system might return a file progress pane.
	 * 
	 * @return a Pane to display in the toolbar status area, or null if none.
	 */
	public Pane getStatusBarPane() {
		return null;
	}
	
	/**
	 * Returns a pane factory to be added to the status bar of the acquisition pane. 
	 * @return the factory, or null.
	 * @deprecated Use {@link #getStatusBarPane()} instead. The toolbar is now shared
	 * across all tabs so a factory pattern for creating multiple pane instances is no longer needed.
	 */
	@Deprecated
	public DAQStatusPaneFactory getStatusBarFactory() {
		return null;
	}
		
}
