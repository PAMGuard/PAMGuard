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
	 * Returns a pane to be added to the status bar of the acquisition pane. 
	 * @return
	 */
	public abstract DAQStatusPaneFactory getStatusBarPane();
		

}
