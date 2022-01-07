package rawDeepLearningClassifier.layoutFX.dlTransfroms;

import java.util.ArrayList;

import org.jamdev.jdl4pam.transforms.DLTransform;

import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxSettingsPanes.DynamicSettings;
import pamViewFX.fxSettingsPanes.SettingsListener;

/**
 * Settings pane for all DLTransforms
 * 
 * @author Jamie macaulay
 *
 */
public abstract class DLTransformPane extends PamBorderPane implements DynamicSettings {
	
	ArrayList<SettingsListener> settingsListeners = new ArrayList<SettingsListener>();
	
	
	private boolean allowNotify = true; 
	
	public DLTransformPane() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Get the DL transform
	 * @return the DL transform. 
	 */
	public abstract DLTransform getDLTransform(); 

	/**
	 * Get the parameters form the controls in the pane. 
	 * @param dlTransform - the DLTransform to apply parameters to. 
	 * @return the DLTransform with new parameters. 
	 */
	
	public abstract DLTransform getParams(DLTransform dlTransform); 
	
	/**
	 * Set the parameters on the pane. 
	 * @param dlTransform - the DLTransform containing the parameters. 
	 */
	
	public abstract void setParams(DLTransform dlTransform); 
	

	/**
	 * Add a settings listener to the pane. This will register any change in controls which changes settings. 
	 * @param settingsListener - the settings listener to add
	 */
	public void addSettingsListener(SettingsListener settingsListener){
		settingsListeners.add(settingsListener);
	} 

	/**
	 * Remove a settings listener
	 * @param settingsListener - the settings to remove. 
	 */
	public void removeSettingsListener(SettingsListener settingsListener){
		settingsListeners.add(settingsListener);
	}

	/**
	 * Notify all settings listeners of a settings change. 
	 * @param obsVal - the observable value
	 * @param oldVal - the old value	
	 * @param newVal	- the new value. 
	 */
	public void notifySettingsListeners(){
		if (allowNotify) {
			for (int i=0; i<settingsListeners.size(); i++){
				settingsListeners.get(i).settingsChanged();;
			}
		}
	}

	/**
	 * Check whether the pane can notify settings listeners
	 * @return true if settings listeners are notified on calling notifySettingsListeners()
	 */
	public boolean isAllowNotify() {
		return allowNotify;
	}

	/**
	 * Set settings listeners to be notified. Set to false to disable all listeners. 
	 * @param allowNotify - true to allow notifucation of settings listeners. 
	 */
	public void setAllowNotify(boolean allowNotify) {
		this.allowNotify = allowNotify;
	}
	
	


}
