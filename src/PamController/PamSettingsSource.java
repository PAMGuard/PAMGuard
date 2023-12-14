package PamController;

/**
 * Added to a PamControlledUnit which can save and reload PamSettings
 * information from files, databases, etc. 
 * <p>
 * Any PamControlledUnit implementing this class will be asked to 
 * save settings each time PAMGUARD starts and will be able
 * to read back lists of timestamped settings from files. 
 * 
 * @author Doug Gillespie. 
 *
 */
public interface PamSettingsSource {

	/**
	 * Save the settings in some way or another. 
	 * @param timeNow current time.
	 * @return true if settings saved successfully 
	 */
	public boolean saveStartSettings(long timeNow);
	
	/**
	 * Save settings when processing ends.
	 * This may just be an update of the settings saves with saveStartSettings, e.g. an end time.  
	 * @param timeNow
	 * @return true if saved correctly. 
	 */
	public boolean saveEndSettings(long timeNow);
	
	/**
	 * Get the number of different settings
	 * within the settings source. 
	 * @return the number of PamSettingsGroups.
	 */
	public int getNumSettings();
	
	/**
	 * Get a specific PamSettingsGroup
	 * @param settingsIndex index of group
	 * @return a settings group
	 */
	public PamSettingsGroup getSettings(int settingsIndex);
	
	/**
	 * Get a name for the settings source
	 * @return a name
	 */
	public String getSettingsSourceName();
	
}
