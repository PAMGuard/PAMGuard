package pamViewFX.fxSettingsPanes;

import java.util.ArrayList;

import PamController.SettingsPane;
import javafx.beans.Observable;

/**
 * A dynamic settings is a settings pane which allows users to change settings instantly. i.e. when a control
 * is pressed the setting is instantly changed rather than, for example, an OK button being pressed and all settings then saving. 
 * <p>
 * This is often is slider panes. 
 * @param <T> - the settings file. 
 * 
 * @author Jamie Macaulay
 */
public abstract class DynamicSettingsPane<T> extends SettingsPane<T> implements DynamicSettings {

	boolean allowNotify = true; 

	public DynamicSettingsPane(Object ownerWindow) {
		super(ownerWindow);
	}

	ArrayList<SettingsListener> settingsListeners = new ArrayList<SettingsListener>();

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
