package pamViewFX.fxSettingsPanes;

/**
 * Listener used with a SettingsFilePane to check for settings that have changed. This 
 * is used in dynamic settigns panes where we want to change something
 * as soon as a control is changed, rather than dialogs where we wait for the dialog to 
 * close before applying settings. 
 * 
 * @author Jamie Macaulay
 *
 */
public interface SettingsListener {
	
	public void settingsChanged(); 

}
