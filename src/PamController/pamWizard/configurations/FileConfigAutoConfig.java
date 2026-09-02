package PamController.pamWizard.configurations;

import PamController.soundMedium.GlobalMedium.SoundMedium;
import PamController.pamWizard.PamAutoConfig;
import PamController.pamWizard.PamFileImport;

/**
 * Presents a configuration read from a {@code .psfx} / {@code .json} file pair as
 * a {@link PamAutoConfig}, so that file based configurations sit alongside the
 * few configurations which are built in code (the spectrogram viewers) and are
 * offered to the user in exactly the same way.
 * <p>
 * The psfx is inspected lazily, the first time something asks what is in it, and
 * the result is kept - the wizard asks more than once as the user moves between
 * pages.
 *
 * @author Jamie Macaulay
 */
public class FileConfigAutoConfig implements PamAutoConfig {

	private final PamConfigDescription config;

	private PamConfigLoader loader;

	private PamConfigInspection inspection;

	/**
	 * The storage paths and other choices the wizard collected. If nothing is set,
	 * whatever paths the psfx file contained are left alone.
	 */
	private ConfigApplyContext applyContext = new ConfigApplyContext();

	public FileConfigAutoConfig(PamConfigDescription config) {
		this.config = config;
	}

	/**
	 * The configuration description this was built from.
	 * @return the description.
	 */
	public PamConfigDescription getDescription() {
		return config;
	}

	/**
	 * The loader which will build this configuration.
	 * @return the loader, or null if none is registered for its type.
	 */
	public PamConfigLoader getLoader() {
		if (loader == null) {
			loader = PamConfigLoaderFactory.getInstance().findLoader(config);
		}
		return loader;
	}

	/**
	 * Read the psfx file and report what is in it. The result is cached, so this is
	 * cheap to call repeatedly.
	 *
	 * @return what the configuration contains, or null if it cannot be loaded at all.
	 */
	public PamConfigInspection getInspection() {
		if (inspection == null) {
			PamConfigLoader configLoader = getLoader();
			if (configLoader != null) {
				inspection = configLoader.inspect(config);
			}
		}
		return inspection;
	}

	/**
	 * The storage paths and other choices to apply with this configuration.
	 * @return the apply context, never null.
	 */
	public ConfigApplyContext getApplyContext() {
		return applyContext;
	}

	/**
	 * @param applyContext the storage paths and other choices to apply.
	 */
	@Override
	public void setApplyContext(ConfigApplyContext applyContext) {
		this.applyContext = (applyContext == null) ? new ConfigApplyContext() : applyContext;
	}

	/**
	 * Whether the psfx file contains a binary store which will need repointing at a
	 * folder on this machine.
	 */
	@Override
	public boolean needsBinaryStore() {
		PamConfigInspection configInspection = getInspection();
		return configInspection != null && configInspection.isValid() && configInspection.hasBinaryStore();
	}

	/**
	 * Whether the psfx file contains a database which will need repointing at a file
	 * on this machine.
	 */
	@Override
	public boolean needsDatabase() {
		PamConfigInspection configInspection = getInspection();
		return configInspection != null && configInspection.isValid() && configInspection.hasDatabase();
	}

	@Override
	public boolean isValid(PamFileImport importHandler, int runMode) {
		return PamConfigRepository.getInstance().matches(config, importHandler, runMode);
	}

	@Override
	public void createConfiguration(PamFileImport importHandler) {
		PamConfigLoader configLoader = getLoader();
		if (configLoader == null) {
			System.out.println("No loader available for configuration " + config.getName());
			return;
		}
		PamConfigInspection configInspection = getInspection();
		if (configInspection == null || !configInspection.isValid()) {
			String reason = (configInspection == null) ? "unknown error" : configInspection.getError();
			System.out.println("Unable to load configuration " + config.getName() + ": " + reason);
			return;
		}
		configLoader.apply(configInspection, importHandler, applyContext);
	}

	@Override
	public String getConfigDescription() {
		return config.getDescription();
	}

	@Override
	public String[] getSpeciesList() {
		String[] species = config.getSpeciesList();
		return (species.length == 0) ? null : species;
	}

	@Override
	public String getConfigName() {
		return config.getName();
	}

	@Override
	public SoundMedium getGlobalMediumSettings() {
		return config.getMedium();
	}

	@Override
	public String toString() {
		return getConfigName();
	}
}
