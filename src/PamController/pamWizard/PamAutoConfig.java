package PamController.pamWizard;

import PamController.pamWizard.configurations.ConfigApplyContext;
import PamController.soundMedium.GlobalMedium.SoundMedium;

/**
 * Interface for an automatic PAMGuard configuration that can be built from a set
 * of dropped files. Implementations decide whether they apply to a given set of
 * files and run mode ({@link #isValid}), and build the configuration when chosen
 * by the user ({@link #createConfiguration}).
 *
 * @author Jamie Macaulay
 */
public interface PamAutoConfig {

	/**
	 * Check whether this configuration is applicable to the given dropped files and
	 * run mode. Implementations should inspect which file types are present (via
	 * {@link PamFileImport#hasType}) and the run mode (e.g.
	 * {@code PamController.RUN_NORMAL} vs {@code PamController.RUN_PAMVIEW}).
	 *
	 * @param importHandler the imported/scanned files.
	 * @param runMode       the current PAMGuard run mode.
	 * @return true if this configuration can be offered for the given files.
	 */
	public boolean isValid(PamFileImport importHandler, int runMode);

	/**
	 * Build the configuration for the given dropped files. Called when the user
	 * selects this option. Implementations add and wire the required modules and
	 * apply any display / load settings.
	 *
	 * @param importHandler the imported/scanned files.
	 */
	public void createConfiguration(PamFileImport importHandler);

	/**
	 * Get a description of the configuration.
	 * @return description string.
	 */
	public String getConfigDescription();

	/**
	 * Get a list of species that this configuration is valid for.
	 * @return the species list, or null if not species-specific.
	 */
	public String[] getSpeciesList();

	/**
	 * Get the name of the configuration.
	 * @return the name of the configuration.
	 */
	public String getConfigName();

	/**
	 * Get the global medium settings for this configuration - i.e. whether the
	 * configuration is for air or water. Returning null means that the
	 * configuration is suitable for both.
	 * @return the sound medium settings.
	 */
	public SoundMedium getGlobalMediumSettings();

	/**
	 * Whether this configuration writes binary data, and so needs the user to say
	 * where the binary store should go. Configurations which answer true (or
	 * {@link #needsDatabase}) are shown the wizard's storage page, and are handed
	 * the paths the user chose through {@link #setApplyContext}.
	 * <p>
	 * Most code built configurations store nothing of their own, so this defaults
	 * to false.
	 *
	 * @return true if a binary store folder is needed.
	 */
	default boolean needsBinaryStore() {
		return false;
	}

	/**
	 * Whether this configuration needs a database file, and so needs the user to
	 * say where it should go. Note that in viewer mode a database is already open
	 * before any files are dropped, so a viewer configuration should normally leave
	 * this false rather than repointing it.
	 *
	 * @return true if a database file is needed.
	 */
	default boolean needsDatabase() {
		return false;
	}

	/**
	 * Give the configuration the storage locations and other choices the user made
	 * in the wizard, immediately before {@link #createConfiguration} is called.
	 * Configurations which store nothing can ignore this.
	 *
	 * @param applyContext the choices made in the wizard, never null.
	 */
	default void setApplyContext(ConfigApplyContext applyContext) {
	}
}
