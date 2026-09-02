package PamController.pamWizard.configurations;

import PamController.pamWizard.PamFileImport;

/**
 * Reads a configuration's psfx file and applies it to the running PAMGuard.
 * <p>
 * Which loader is used is decided by the {@code configType} field of the
 * configuration's JSON descriptor: no value, or {@code "default"}, means
 * {@link DefaultConfigLoader}. Any other value is looked up in
 * {@link PamConfigLoaderFactory}, which is the extension point for organisations
 * that need something extra to happen when their configurations are loaded - for
 * example a {@code "noaaserver"} type whose loader connects to a server and
 * downloads models or calibration data before handing over to the default
 * behaviour.
 * <p>
 * Loading happens in two stages so the wizard can tell the user what it is about
 * to do. {@link #inspect} reads the psfx and reports what is in it without
 * changing anything; {@link #apply} then builds the configuration.
 *
 * @author Jamie Macaulay
 */
public interface PamConfigLoader {

	/**
	 * Whether this loader can handle the given configuration. Normally a test of
	 * {@link PamConfigDescription#getConfigType()}, but a loader may also refuse a
	 * configuration it recognises but cannot currently use, for example because a
	 * server it needs is unreachable.
	 *
	 * @param config the configuration.
	 * @return true if this loader can load the configuration.
	 */
	public boolean canLoad(PamConfigDescription config);

	/**
	 * Read the configuration's psfx file and report what it contains, without
	 * changing anything in the running PAMGuard. Safe to call as the user browses
	 * the list of available configurations.
	 *
	 * @param config the configuration.
	 * @return what was found, never null. Check {@link PamConfigInspection#isValid()}.
	 */
	public PamConfigInspection inspect(PamConfigDescription config);

	/**
	 * Build the configuration in the running PAMGuard: create the modules, load
	 * their settings, insert a decimator if the imported data need one, point sound
	 * acquisition at the imported files and set the storage locations.
	 * <p>
	 * Must be called on the Swing event dispatch thread, since module management
	 * runs through the Swing based PAM controller.
	 *
	 * @param inspection    the result of {@link #inspect}.
	 * @param importHandler the imported files.
	 * @param context       the user's choices, and where warnings are reported.
	 * @return true if the configuration was applied.
	 */
	public boolean apply(PamConfigInspection inspection, PamFileImport importHandler, ConfigApplyContext context);
}
