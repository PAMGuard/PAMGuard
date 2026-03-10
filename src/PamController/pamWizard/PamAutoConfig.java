package PamController.pamWizard;

import PamController.soundMedium.GlobalMedium.SoundMedium;

/**
 * Interface for creating automatic configurations for PAMGuard
 * 
 * "author Jamie Macaulay
 */
public interface PamAutoConfig {
	
	/**
	 * Check whether a configuration is valid. Note that this will also check things like whether
	 * PAMGuard is in viewer mode or not, the sample rate of the files etc.
	 * @param importHandler - the imported data. 
	 * @return check for a valid configuration.
	 */
	public boolean isConfigValid(PamFileImport importHandler);
	
	/**
	 * Get a description of the configuration
	 * @return description string
	 */
	public String getConfigDescription();
	
	/**
	 * Get a list of species that this configuration is valid for.
	 * @return
	 */
	public String[] getSpeciesList(); 
	
	/**
	 * Get the name of the configuration
	 * @return the name of the configuration
	 */
	public String getConfigName();
	
	
	/**
	 * Get the global medium settings for this configuration - i.e. whether
	 * the configuration is for air or water. Returning null means that the configuration 
	 * is suitable for both. 
	 * @return the sound medium settings.
	 */
	public SoundMedium getGlobalMediumSettings();


}
