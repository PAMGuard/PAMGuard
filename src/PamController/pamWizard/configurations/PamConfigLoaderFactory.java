package PamController.pamWizard.configurations;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Decides which {@link PamConfigLoader} is used for a configuration, based on
 * the {@code configType} field of its JSON descriptor.
 * <p>
 * This is the extension point for organisations which need something more than
 * the standard behaviour when their configurations are loaded. A configuration
 * with no {@code configType}, or with {@code "default"}, gets
 * {@link DefaultConfigLoader}. To add another, subclass
 * {@code DefaultConfigLoader} and register it:
 *
 * <pre>
 * PamConfigLoaderFactory.getInstance().register("noaaserver", NoaaServerConfigLoader::new);
 * </pre>
 *
 * and configurations declaring {@code "configType": "noaaserver"} will then be
 * loaded by that class - which might, for example, contact a server and download
 * models before letting the default behaviour build the configuration.
 * <p>
 * A configuration whose type has no registered loader is <b>not</b> silently
 * dropped. {@link #findLoader} returns null and the wizard shows the
 * configuration as unavailable, with a reason, so that the user can see why a
 * configuration they were expecting is not offered.
 *
 * @author Jamie Macaulay
 */
public class PamConfigLoaderFactory {

	private static PamConfigLoaderFactory singleInstance;

	/**
	 * Registered loaders, keyed on lower case configuration type.
	 */
	private final Map<String, Supplier<PamConfigLoader>> loaders = new LinkedHashMap<>();

	private PamConfigLoaderFactory() {
		register(PamConfigDescription.DEFAULT_CONFIG_TYPE, DefaultConfigLoader::new);
	}

	/**
	 * Get the single instance of the loader factory.
	 * @return the factory.
	 */
	public static synchronized PamConfigLoaderFactory getInstance() {
		if (singleInstance == null) {
			singleInstance = new PamConfigLoaderFactory();
		}
		return singleInstance;
	}

	/**
	 * Register a loader for a configuration type.
	 *
	 * @param configType the value of {@code configType} in the JSON descriptor.
	 * @param supplier   creates a loader; called once per configuration loaded, so
	 *                   a loader need not be reusable.
	 */
	public void register(String configType, Supplier<PamConfigLoader> supplier) {
		if (configType == null || supplier == null) {
			return;
		}
		loaders.put(configType.toLowerCase().trim(), supplier);
	}

	/**
	 * Whether a loader is registered for a configuration type.
	 *
	 * @param configType the configuration type.
	 * @return true if the type can be loaded.
	 */
	public boolean isRegistered(String configType) {
		return configType != null && loaders.containsKey(configType.toLowerCase().trim());
	}

	/**
	 * Find a loader which can handle a configuration.
	 *
	 * @param config the configuration.
	 * @return a new loader, or null if the configuration's type is not registered or
	 *         its loader refuses it.
	 */
	public PamConfigLoader findLoader(PamConfigDescription config) {
		if (config == null) {
			return null;
		}
		Supplier<PamConfigLoader> supplier = loaders.get(config.getConfigType().toLowerCase());
		if (supplier == null) {
			return null;
		}
		PamConfigLoader loader = supplier.get();
		if (loader == null || !loader.canLoad(config)) {
			return null;
		}
		return loader;
	}

	/**
	 * Why a configuration cannot be loaded, for showing to the user.
	 *
	 * @param config the configuration.
	 * @return the reason, or null if the configuration can be loaded.
	 */
	public String getUnavailableReason(PamConfigDescription config) {
		if (config == null) {
			return "No configuration";
		}
		String configType = config.getConfigType();
		if (!isRegistered(configType)) {
			return String.format("This configuration is of type \"%s\", which this version of PAMGuard "
					+ "does not know how to load.", configType);
		}
		if (findLoader(config) == null) {
			return String.format("The loader for configurations of type \"%s\" is not available.", configType);
		}
		return null;
	}
}
