package Acquisition.layoutFX;

import PamController.SettingsPane;

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

}
