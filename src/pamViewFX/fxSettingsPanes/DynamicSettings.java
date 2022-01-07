package pamViewFX.fxSettingsPanes;


/**
 * Interface for implementing dynamic settings panes. These settings panes
 * show instant changes whenever a controller is changed. 
 * @author Jamie Macaulay
 *
 */
public interface DynamicSettings {
	
	
	/**
	 * Add a settings listener to the pane. This will register any change in controls which changes settings. 
	 * @param settingsListener - the settings listener to add
	 */
	public void addSettingsListener(SettingsListener settingsListener);

	/**
	 * Remove a settings listener
	 * @param settingsListener - the settings to remove. 
	 */
	public void removeSettingsListener(SettingsListener settingsListener);
	/**
	 * Notify all settings listeners of a settings change. 
	 */
	public void notifySettingsListeners();

	/**
	 * Check whether the pane can notify settings listeners
	 * @return true if settings listeners are notified on calling notifySettingsListeners()
	 */
	public boolean isAllowNotify() ;

	/**
	 * Set settings listeners to be notified. Set to false to disable all listeners. 
	 * @param allowNotify - true to allow notification of settings listeners. 
	 */
	public void setAllowNotify(boolean allowNotify) ;

}
