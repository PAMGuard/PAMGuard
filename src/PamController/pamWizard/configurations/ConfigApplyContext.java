package PamController.pamWizard.configurations;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import PamController.PamFolders;

/**
 * Everything the user chose in the import wizard which affects how a
 * configuration is applied, plus somewhere for the loader to report back
 * anything that did not go entirely to plan.
 * <p>
 * Storage locations are the main content: a configuration downloaded or shipped
 * with PAMGuard carries whatever paths its author happened to be using, so the
 * binary store folder and database file always have to be replaced with ones
 * that make sense on this machine.
 *
 * @author Jamie Macaulay
 */
public class ConfigApplyContext {

	/**
	 * Name of the sub folder created for binary data within the project folder. The
	 * same name binary storage uses by default.
	 */
	public static final String BINARY_FOLDER_NAME = "PAMBinary";

	private File projectFolder;

	private File binaryFolder;

	private File databaseFile;

	private final List<String> warnings = new ArrayList<>();

	/**
	 * Create a context with no paths set, so that nothing is changed.
	 */
	public ConfigApplyContext() {
	}

	/**
	 * Create a context with the binary folder and database file derived from a
	 * project folder in the usual way, i.e. a {@value #BINARY_FOLDER_NAME} sub
	 * folder and a database named after the configuration.
	 *
	 * @param projectFolder the folder to put everything in.
	 * @param configName    the configuration name, used to name the database.
	 */
	public ConfigApplyContext(File projectFolder, String configName) {
		setProjectFolder(projectFolder, configName);
	}

	/**
	 * Set the project folder and derive default storage paths from it. Either
	 * derived path can be overridden afterwards.
	 *
	 * @param projectFolder the folder to put everything in.
	 * @param configName    the configuration name, used to name the database.
	 */
	public void setProjectFolder(File projectFolder, String configName) {
		this.projectFolder = projectFolder;
		if (projectFolder == null) {
			return;
		}
		binaryFolder = new File(projectFolder, BINARY_FOLDER_NAME);
		databaseFile = new File(projectFolder, tidyFileName(configName) + ".sqlite3");
	}

	/**
	 * The folder chosen to hold the output of this configuration.
	 * @return the project folder, or null if none was chosen.
	 */
	public File getProjectFolder() {
		return projectFolder;
	}

	/**
	 * Where binary data should be written. Null leaves whatever the psfx file
	 * contained, which is almost never what is wanted.
	 *
	 * @return the binary store folder, or null to leave it unchanged.
	 */
	public File getBinaryFolder() {
		return binaryFolder;
	}

	public void setBinaryFolder(File binaryFolder) {
		this.binaryFolder = binaryFolder;
	}

	/**
	 * The database file to use. Null leaves whatever the psfx file contained.
	 * @return the database file, or null to leave it unchanged.
	 */
	public File getDatabaseFile() {
		return databaseFile;
	}

	public void setDatabaseFile(File databaseFile) {
		this.databaseFile = databaseFile;
	}

	/**
	 * Record something the user should know about after the configuration was
	 * applied - a module whose data source could not be reconnected, a folder that
	 * could not be created, and so on. These are shown on the wizard's last page.
	 *
	 * @param warning the warning message.
	 */
	public void addWarning(String warning) {
		if (warning != null && !warning.isBlank()) {
			warnings.add(warning);
			System.out.println("PamConfig: " + warning);
		}
	}

	/**
	 * Anything the user should know about after the configuration was applied.
	 * @return the warnings, never null.
	 */
	public List<String> getWarnings() {
		return warnings;
	}

	/**
	 * @return true if anything went other than perfectly.
	 */
	public boolean hasWarnings() {
		return !warnings.isEmpty();
	}

	/**
	 * A sensible default project folder to offer the user: the folder holding the
	 * current settings file or database, falling back to the PAMGuard folder in the
	 * user's home directory.
	 *
	 * @return the default project folder.
	 */
	public static File getDefaultProjectFolder() {
		String folder = PamFolders.getDefaultProjectFolder();
		return (folder == null) ? null : new File(folder);
	}

	/**
	 * Turn a configuration name into something usable as a file name.
	 *
	 * @param name the configuration name.
	 * @return a file name safe version of the name.
	 */
	private static String tidyFileName(String name) {
		if (name == null || name.isBlank()) {
			return "pamguard";
		}
		return name.trim().replaceAll("[^a-zA-Z0-9\\-_. ]", "").replaceAll("\\s+", "_");
	}
}
