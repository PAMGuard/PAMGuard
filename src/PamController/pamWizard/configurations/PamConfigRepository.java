package PamController.pamWizard.configurations;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import PamController.PamController;
import PamController.PamFolders;
import PamController.pamWizard.PamFileImport;
import PamController.pamWizard.PamImportFileType;
import PamController.pamWizard.SoundFileSummary;

/**
 * Finds and holds the catalogue of PAMGuard configurations which the import
 * wizard can offer. A configuration is a pair of files sharing a base name: a
 * {@code .psfx} holding the modules and their settings, and a {@code .json}
 * holding a {@link PamConfigDescription}.
 * <p>
 * Configurations are read from several folders, which allows an organisation to
 * add its own configurations without touching the PAMGuard installation. In
 * increasing order of priority:
 * <ol>
 * <li>{@code &lt;install folder&gt;/configurations} - the configurations shipped with
 * PAMGuard.</li>
 * <li>{@code &lt;working directory&gt;/configurations} - so that configurations in the
 * source tree are found when running from an IDE, where the install folder
 * resolves to the build output directory rather than the project root.</li>
 * <li>{@code &lt;user home&gt;/Pamguard/configurations} - user or company added
 * configurations.</li>
 * </ol>
 * A configuration in a higher priority folder replaces one of the same base name
 * in a lower priority folder, so a user can override a shipped configuration by
 * copying it into their own folder and editing it.
 * <p>
 * Every folder is optional. A missing folder is simply skipped, exactly as the
 * plugins folder is (see {@code PamModel.loadPluginJars}).
 *
 * @author Jamie Macaulay
 */
public class PamConfigRepository {

	/**
	 * Name of the folder holding configuration file pairs.
	 */
	public static final String CONFIG_FOLDER_NAME = "configurations";

	private static PamConfigRepository singleInstance;

	private final ObjectMapper objectMapper;

	/**
	 * Configurations found by the last scan, keyed on
	 * {@link PamConfigDescription#getKey()} so that later folders override earlier
	 * ones.
	 */
	private Map<String, PamConfigDescription> configurations = new LinkedHashMap<>();

	/**
	 * Whether a scan has been run yet.
	 */
	private boolean scanned = false;

	private PamConfigRepository() {
		objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	/**
	 * Get the single instance of the configuration repository.
	 * @return the repository.
	 */
	public static synchronized PamConfigRepository getInstance() {
		if (singleInstance == null) {
			singleInstance = new PamConfigRepository();
		}
		return singleInstance;
	}

	/**
	 * The folders searched for configurations, in increasing order of priority.
	 * Folders which do not exist are still included in the returned list - it is up
	 * to the caller to check - so that this can also be used to tell the user where
	 * configurations may be placed.
	 *
	 * @return the search folders.
	 */
	public List<File> getSearchFolders() {
		List<File> folders = new ArrayList<>();

		PamController pamController = PamController.getInstance();
		if (pamController != null && pamController.getInstallFolder() != null) {
			// note getInstallFolder() already ends in a separator.
			folders.add(new File(pamController.getInstallFolder() + CONFIG_FOLDER_NAME));
		}

		String workingDir = System.getProperty("user.dir");
		if (workingDir != null) {
			File workingFolder = new File(workingDir, CONFIG_FOLDER_NAME);
			if (!containsPath(folders, workingFolder)) {
				folders.add(workingFolder);
			}
		}

		File userFolder = new File(PamFolders.getHomeFolder(), CONFIG_FOLDER_NAME);
		if (!containsPath(folders, userFolder)) {
			folders.add(userFolder);
		}

		return folders;
	}

	/**
	 * Get all configurations, scanning the search folders if this is the first call.
	 * @return the configurations found, never null.
	 */
	public synchronized List<PamConfigDescription> getConfigurations() {
		if (!scanned) {
			rescan();
		}
		return new ArrayList<>(configurations.values());
	}

	/**
	 * Re-read every search folder, discarding anything found previously. Call this
	 * if configurations may have been added while PAMGuard was running.
	 *
	 * @return the configurations found, never null.
	 */
	public synchronized List<PamConfigDescription> rescan() {
		configurations = new LinkedHashMap<>();
		for (File folder : getSearchFolders()) {
			scanFolder(folder);
		}
		scanned = true;
		return new ArrayList<>(configurations.values());
	}

	/**
	 * Read every configuration in a single folder, adding what is found to the
	 * catalogue. Configurations replace any already found under the same key, so
	 * folders must be scanned in increasing order of priority.
	 *
	 * @param folder the folder to scan.
	 * @return the number of configurations read from this folder.
	 */
	private int scanFolder(File folder) {
		if (folder == null || !folder.exists() || !folder.isDirectory()) {
			return 0;
		}
		File[] jsonFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
		if (jsonFiles == null || jsonFiles.length == 0) {
			return 0;
		}
		int nRead = 0;
		for (File jsonFile : jsonFiles) {
			PamConfigDescription config = readConfiguration(jsonFile);
			if (config != null) {
				configurations.put(config.getKey(), config);
				nRead++;
			}
		}
		System.out.println(String.format("Found %d PAMGuard configuration(s) in %s", nRead, folder.getAbsolutePath()));
		return nRead;
	}

	/**
	 * Read a single configuration description and locate its psfx file. A
	 * description whose psfx is missing or unreadable is rejected, since there would
	 * be nothing to load if the user chose it.
	 *
	 * @param jsonFile the JSON descriptor.
	 * @return the configuration, or null if it could not be used.
	 */
	public PamConfigDescription readConfiguration(File jsonFile) {
		PamConfigDescription config;
		try {
			config = objectMapper.readValue(jsonFile, PamConfigDescription.class);
		}
		catch (Exception e) {
			System.out.println(String.format("Unable to read PAMGuard configuration %s: %s",
					jsonFile.getAbsolutePath(), e.getMessage()));
			return null;
		}
		if (config == null) {
			return null;
		}
		config.setJsonFile(jsonFile);

		String psfxName = config.getPsfxName();
		if (psfxName == null) {
			System.out.println(String.format("PAMGuard configuration %s names no psfx file", jsonFile.getAbsolutePath()));
			return null;
		}
		File psfxFile = new File(psfxName);
		if (!psfxFile.isAbsolute()) {
			psfxFile = new File(jsonFile.getParentFile(), psfxName);
		}
		if (!psfxFile.exists() || !psfxFile.canRead()) {
			System.out.println(String.format("PAMGuard configuration %s: settings file %s is missing",
					jsonFile.getAbsolutePath(), psfxFile.getAbsolutePath()));
			return null;
		}
		config.setPsfxFile(psfxFile);

		return config;
	}

	/**
	 * Get the configurations which can be used with a set of imported files in the
	 * current run mode.
	 *
	 * @param fileImport the imported and scanned files.
	 * @param runMode    the PAMGuard run mode.
	 * @return the matching configurations, never null.
	 */
	public List<PamConfigDescription> getMatching(PamFileImport fileImport, int runMode) {
		List<PamConfigDescription> matching = new ArrayList<>();
		for (PamConfigDescription config : getConfigurations()) {
			if (getMismatchReason(config, fileImport, runMode) == null) {
				matching.add(config);
			}
		}
		return matching;
	}

	/**
	 * Whether a configuration can be used with a set of imported files.
	 *
	 * @param config     the configuration.
	 * @param fileImport the imported and scanned files.
	 * @param runMode    the PAMGuard run mode.
	 * @return true if the configuration can be offered.
	 */
	public boolean matches(PamConfigDescription config, PamFileImport fileImport, int runMode) {
		return getMismatchReason(config, fileImport, runMode) == null;
	}

	/**
	 * Why a configuration cannot be used with a set of imported files.
	 * <p>
	 * Sample rate and channel count are tested against the <b>lowest</b> values found
	 * across the imported files, so that a configuration is only offered if every
	 * file meets its requirements - a configuration that could only run on some of
	 * the files would be no use.
	 * <p>
	 * There is deliberately no upper sample rate test by default: data at a higher
	 * rate than a configuration expects are decimated down to its target rate when
	 * it is applied. Only a configuration which explicitly declares a maximum is
	 * limited.
	 *
	 * @param config     the configuration.
	 * @param fileImport the imported and scanned files.
	 * @param runMode    the PAMGuard run mode.
	 * @return the reason it cannot be used, or null if it can.
	 */
	public String getMismatchReason(PamConfigDescription config, PamFileImport fileImport, int runMode) {
		if (config == null) {
			return "No configuration";
		}
		if (!config.getRunModes().contains(runMode)) {
			return "Not available in this PAMGuard run mode";
		}

		String loaderProblem = PamConfigLoaderFactory.getInstance().getUnavailableReason(config);
		if (loaderProblem != null) {
			return loaderProblem;
		}

		if (fileImport == null) {
			return "No files have been imported";
		}

		// every required file type must be present.
		for (PamImportFileType required : config.getRequiredFileTypes()) {
			if (!fileImport.hasType(required)) {
				return "Needs " + required.getName();
			}
		}

		// sample rate and channels, from the imported sound files.
		SoundFileSummary summary = fileImport.getSoundSummary();
		if (summary == null || !summary.isValid()) {
			// nothing to test against, so do not exclude the configuration on that basis.
			return null;
		}

		if (summary.getMinSampleRate() < config.getMinSampleRate()) {
			return String.format("Needs a sample rate of at least %s (files are %s)",
					SoundFileSummary.formatRate((float) config.getMinSampleRate()),
					SoundFileSummary.formatRate(summary.getMinSampleRate()));
		}
		Double maxSampleRate = config.getMaxSampleRate();
		if (maxSampleRate != null && summary.getMinSampleRate() > maxSampleRate) {
			return String.format("Needs a sample rate of no more than %s (files are %s)",
					SoundFileSummary.formatRate(maxSampleRate.floatValue()),
					SoundFileSummary.formatRate(summary.getMinSampleRate()));
		}
		if (summary.getMinChannels() < config.getMinChannels()) {
			return String.format("Needs at least %d channel%s (files have %d)",
					config.getMinChannels(), config.getMinChannels() == 1 ? "" : "s", summary.getMinChannels());
		}
		Integer maxChannels = config.getMaxChannels();
		if (maxChannels != null && summary.getMinChannels() > maxChannels) {
			return String.format("Needs no more than %d channel%s (files have %d)",
					maxChannels, maxChannels == 1 ? "" : "s", summary.getMinChannels());
		}

		return null;
	}

	/**
	 * Whether the list already holds a folder with the same absolute path.
	 */
	private boolean containsPath(List<File> folders, File folder) {
		for (File existing : folders) {
			if (existing.getAbsolutePath().equals(folder.getAbsolutePath())) {
				return true;
			}
		}
		return false;
	}
}
