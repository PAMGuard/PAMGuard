package PamController.pamWizard.configurations;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import PamController.PamController;
import PamController.pamWizard.PamAutoConfig;
import PamController.pamWizard.PamFileImport;
import PamController.pamWizard.SoundFileSummary;
import PamController.soundMedium.GlobalMedium.SoundMedium;

/**
 * The state shared between the pages of the import wizard: what was imported,
 * what can be built from it, what the user chose, and where the output should
 * go.
 * <p>
 * Kept separate from the pages themselves so that the Swing and JavaFX wizards
 * can present the same information and produce the same result.
 *
 * @author Jamie Macaulay
 */
public class ConfigWizardData {

	private final PamFileImport fileImport;

	private final int runMode;

	private final List<PamAutoConfig> availableConfigs;

	private PamAutoConfig selectedConfig;

	private ConfigApplyContext applyContext;

	/**
	 * Whether the recordings were made in air or in water. Configurations which only
	 * make sense in one medium fix this; the rest leave it to the user.
	 */
	private SoundMedium medium = currentGlobalMedium();

	public ConfigWizardData(PamFileImport fileImport, int runMode, List<PamAutoConfig> availableConfigs) {
		this.fileImport = fileImport;
		this.runMode = runMode;
		/*
		 * Written out rather than as conditional expressions: the Eclipse compiler this
		 * project builds with mis-generates the stack map frame where a conditional
		 * merges two reference types, and the class then fails verification at load time.
		 * See the note in DefaultConfigLoader.planDecimation.
		 */
		if (availableConfigs == null) {
			this.availableConfigs = new ArrayList<>();
		}
		else {
			this.availableConfigs = availableConfigs;
		}
		this.applyContext = new ConfigApplyContext(ConfigApplyContext.getDefaultProjectFolder(), "pamguard");
	}

	/**
	 * The files the user imported.
	 * @return the imported files.
	 */
	public PamFileImport getFileImport() {
		return fileImport;
	}

	/**
	 * A summary of the audio format of the imported files.
	 * @return the summary, or null if no sound files were scanned.
	 */
	public SoundFileSummary getSoundSummary() {
		if (fileImport == null) {
			return null;
		}
		return fileImport.getSoundSummary();
	}

	/**
	 * The PAMGuard run mode the wizard is running in.
	 * @return the run mode.
	 */
	public int getRunMode() {
		return runMode;
	}

	/**
	 * Every configuration which can be used with the imported files.
	 * @return the available configurations, never null.
	 */
	public List<PamAutoConfig> getAvailableConfigs() {
		return availableConfigs;
	}

	/**
	 * The configuration the user chose.
	 * @return the selected configuration, or null if none has been chosen yet.
	 */
	public PamAutoConfig getSelectedConfig() {
		return selectedConfig;
	}

	/**
	 * Choose a configuration. The default database name follows the configuration
	 * name, so that a project folder holding several runs is not a jumble of
	 * identically named databases.
	 *
	 * @param selectedConfig the chosen configuration.
	 */
	public void setSelectedConfig(PamAutoConfig selectedConfig) {
		this.selectedConfig = selectedConfig;
		if (selectedConfig != null) {
			applyContext.setProjectFolder(applyContext.getProjectFolder(), selectedConfig.getConfigName());
		}
	}

	/**
	 * What was found inside the selected configuration's settings file.
	 *
	 * @return the inspection, or null if the selected configuration is not one that
	 *         comes from a file.
	 */
	public PamConfigInspection getSelectedInspection() {
		if (selectedConfig instanceof FileConfigAutoConfig) {
			return ((FileConfigAutoConfig) selectedConfig).getInspection();
		}
		return null;
	}

	/**
	 * The description of the selected configuration.
	 *
	 * @return the description, or null if the selected configuration is not one that
	 *         comes from a file.
	 */
	public PamConfigDescription getSelectedDescription() {
		if (selectedConfig instanceof FileConfigAutoConfig) {
			return ((FileConfigAutoConfig) selectedConfig).getDescription();
		}
		return null;
	}

	/**
	 * Whether the user needs to be asked where data should be stored. Only
	 * configurations which write a binary store or a database of their own do -
	 * each configuration says so for itself, since a code built configuration has no
	 * psfx file to inspect.
	 *
	 * @return true if the storage page is needed.
	 */
	public boolean needsStoragePaths() {
		if (selectedConfig == null) {
			return false;
		}
		return selectedConfig.needsBinaryStore() || selectedConfig.needsDatabase();
	}

	/**
	 * Whether a decimator will be added or retuned when this configuration is
	 * applied, because the imported data are at a higher sample rate than its
	 * detectors expect.
	 *
	 * @return true if the sample rate will be changed.
	 */
	public boolean willDecimate() {
		PamConfigInspection inspection = getSelectedInspection();
		SoundFileSummary summary = getSoundSummary();
		if (inspection == null || summary == null || !summary.isValid()) {
			return false;
		}
		return inspection.needsDecimation(summary.getMinSampleRate());
	}

	/**
	 * The storage locations and other choices to apply.
	 * @return the apply context, never null.
	 */
	public ConfigApplyContext getApplyContext() {
		return applyContext;
	}

	/**
	 * @param applyContext the storage locations and other choices to apply.
	 */
	public void setApplyContext(ConfigApplyContext applyContext) {
		if (applyContext == null) {
			this.applyContext = new ConfigApplyContext();
		}
		else {
			this.applyContext = applyContext;
		}
	}

	/**
	 * Set the folder everything should be written to, deriving the binary store and
	 * database paths from it.
	 *
	 * @param projectFolder the project folder.
	 */
	public void setProjectFolder(File projectFolder) {
		String name = (selectedConfig == null) ? "pamguard" : selectedConfig.getConfigName();
		applyContext.setProjectFolder(projectFolder, name);
	}

	/**
	 * Whether the recordings are from air or water.
	 * @return the sound medium, never null.
	 */
	public SoundMedium getMedium() {
		return medium;
	}

	/**
	 * @param medium the sound medium the recordings were made in.
	 */
	public void setMedium(SoundMedium medium) {
		if (medium != null) {
			this.medium = medium;
		}
	}

	/**
	 * The medium PAMGuard is currently set to, used as the starting point for the
	 * user's choice.
	 *
	 * @return the current global medium, defaulting to water.
	 */
	private static SoundMedium currentGlobalMedium() {
		PamController pamController = PamController.getInstance();
		if (pamController != null && pamController.getGlobalMediumManager() != null) {
			SoundMedium current = pamController.getGlobalMediumManager().getCurrentMedium();
			if (current != null) {
				return current;
			}
		}
		return SoundMedium.Water;
	}

	/**
	 * Build the selected configuration. Must be called on the Swing event dispatch
	 * thread, since module management runs through the Swing based PAM controller.
	 *
	 * @return true if a configuration was built.
	 */
	public boolean applySelected() {
		if (selectedConfig == null) {
			return false;
		}
		selectedConfig.setApplyContext(applyContext);
		selectedConfig.createConfiguration(fileImport);
		applyMedium();
		return true;
	}

	/**
	 * Set PAMGuard's global medium to whatever the wizard settled on.
	 * <p>
	 * This has to happen after the configuration has been built, not before: loading
	 * a psfx file restores the medium its author was using along with everything else,
	 * so setting it first would simply be overwritten.
	 */
	private void applyMedium() {
		PamController pamController = PamController.getInstance();
		if (pamController == null || pamController.getGlobalMediumManager() == null || medium == null) {
			return;
		}
		if (pamController.getGlobalMediumManager().getCurrentMedium() != medium) {
			pamController.getGlobalMediumManager().setCurrentMedium(medium);
		}
	}
}
