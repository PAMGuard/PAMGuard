package PamController.pamWizard;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingUtilities;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.PamGUIManager;
import PamController.pamWizard.configurations.BlankConfigAutoConfig;
import PamController.pamWizard.configurations.ConfigWizardData;
import PamController.pamWizard.configurations.FileConfigAutoConfig;
import PamController.pamWizard.configurations.PamConfigDescription;
import PamController.pamWizard.configurations.PamConfigRepository;
import PamController.pamWizard.layoutFX.ConfigImportWizardFX;
import PamController.pamWizard.swing.ConfigImportWizard;
import PamModel.PamModuleInfo;
import javafx.application.Platform;

/**
 * Manages the automatic creation of PAMGuard configurations when files are
 * dragged onto a <b>blank</b> configuration. Dropped files are scanned by a set
 * of {@link PamFileTypeScanner}s (sound files, SoundTrap click detections and
 * CPOD/FPOD detection files) and the user is offered a list of
 * {@link PamAutoConfig} options appropriate to the file types present and the run
 * mode.
 *
 * @author Jamie Macaulay
 */
public class PamWizardManager {

	/**
	 * Scanners which recognise file types within the dropped files. Sound files,
	 * SoundTrap click detections and CPOD/FPOD detection files are recognised; a
	 * PAMGuard binary file scanner could be added here to enable configurations that
	 * view those data too.
	 */
	private final List<PamFileTypeScanner> scanners = new ArrayList<>();

	/**
	 * Available automatic configurations. Each decides for itself whether it applies
	 * to a given set of dropped files and run mode.
	 */
	private final List<PamAutoConfig> autoConfigs = new ArrayList<>();

	/**
	 * Always-present default modules that are added automatically to every
	 * configuration (not via the data model) and so do not count towards a
	 * configuration being "non-blank". Core / hidden / essential ({@code minNumber > 0})
	 * modules are detected generically; this set covers the remaining auto-added
	 * singletons that are none of those.
	 */
	private static final Set<String> DEFAULT_MODULE_CLASSES = new HashSet<>(Arrays.asList(
			"Array.ArrayManager",
			"metadata.MetaDataContol"));

	public PamWizardManager(PamController pamController) {
		createScanners();
		createAutoConfigs();
	}

	private void createScanners() {
		scanners.add(new SoundFileScanner());
		scanners.add(new SudClickScanner());
		scanners.add(new PODFileScanner(PamImportFileType.CPOD));
		scanners.add(new PODFileScanner(PamImportFileType.FPOD));
		/* Future: register a PAMGuard binary file scanner here. */
	}

	private void createAutoConfigs() {
		autoConfigs.add(new SpectrogramViewerAutoConfig());
		autoConfigs.add(new SpectrogramRealTimeAutoConfig());
		autoConfigs.add(new PODViewerAutoConfig());
		/* Future: register combined configurations here, e.g. a config that is valid
		 * when both FPOD detection files and sound files are present and shows the
		 * detections alongside the spectrogram. */
	}

	/**
	 * The configurations which can be used with a set of imported files: the code
	 * built ones above, the file based ones from the configurations folders, and the
	 * blank configuration, which is always last and always available.
	 *
	 * @param fileImport the imported and scanned files.
	 * @param runMode    the PAMGuard run mode.
	 * @return the available configurations, never empty.
	 */
	public List<PamAutoConfig> getAvailableConfigs(PamFileImport fileImport, int runMode) {
		List<PamAutoConfig> valid = new ArrayList<>();

		for (PamAutoConfig config : autoConfigs) {
			if (config.isValid(fileImport, runMode)) {
				valid.add(config);
			}
		}

		for (PamConfigDescription description :
				PamConfigRepository.getInstance().getMatching(fileImport, runMode)) {
			valid.add(new FileConfigAutoConfig(description));
		}

		valid.add(new BlankConfigAutoConfig());

		return valid;
	}

	/**
	 * Called whenever files are dropped into PAMGuard. Scans the files and, if the
	 * configuration is blank, offers the user a list of matching auto-configurations.
	 *
	 * @param files dropped files or folders.
	 */
	public void newImportedFiles(List<File> files) {
		// Importing is only allowed into a blank configuration; once any module has
		// been added the user should use the normal module-setup tools.
		if (!isBlankConfiguration()) {
			return;
		}
		if (files == null || files.isEmpty()) {
			return;
		}

		final PamFileImport fileImport = new PamFileImport(files);

		if (scanners.isEmpty()) {
			presentOptions(fileImport);
			return;
		}

		// run all scanners (scanning may be asynchronous) and present options once
		// every scanner has reported back.
		final AtomicInteger remaining = new AtomicInteger(scanners.size());
		for (PamFileTypeScanner scanner : scanners) {
			scanner.scan(files, result -> {
				synchronized (fileImport) {
					fileImport.addResult(result);
				}
				if (remaining.decrementAndGet() == 0) {
					presentOptions(fileImport);
				}
			});
		}
	}

	/**
	 * Work out which configurations apply and present them to the user.
	 */
	private void presentOptions(PamFileImport fileImport) {
		// the scan may have been asynchronous - re-check the config is still blank.
		if (!isBlankConfiguration()) {
			return;
		}

		int runMode = PamController.getInstance().getRunMode();
		List<PamAutoConfig> validConfigs = getAvailableConfigs(fileImport, runMode);
		if (validConfigs.isEmpty()) {
			return;
		}

		showWizard(new ConfigWizardData(fileImport, runMode, validConfigs));
	}

	/**
	 * Show the import wizard (FX or Swing depending on the active GUI) and build the
	 * configuration the user chooses.
	 */
	private void showWizard(ConfigWizardData wizardData) {
		if (PamGUIManager.isFX()) {
			Platform.runLater(() -> {
				PamAutoConfig selected = ConfigImportWizardFX.showWizard(wizardData);
				if (selected != null) {
					buildConfiguration(wizardData);
				}
			});
		}
		else {
			SwingUtilities.invokeLater(() -> {
				if (isBlankConfiguration()) {
					// the Swing wizard builds the configuration itself when it finishes.
					ConfigImportWizard.showWizard(PamController.getMainFrame(), wizardData);
				}
			});
		}
	}

	/**
	 * Build the selected configuration on the Swing event thread (module management
	 * runs through the Swing-based {@link PamController}), then report any warnings
	 * back on the FX thread.
	 */
	private void buildConfiguration(ConfigWizardData wizardData) {
		SwingUtilities.invokeLater(() -> {
			if (!isBlankConfiguration()) {
				return;
			}
			wizardData.applySelected();
			Platform.runLater(() -> ConfigImportWizardFX.showWarnings(wizardData));
		});
	}

	/**
	 * Whether the current configuration is blank, i.e. has no user-added modules.
	 * Essential / always-present modules (those with a minimum count greater than
	 * zero) are ignored - this matches the logic that shows/removes the import tab,
	 * which only removes it when a user module ({@code minNumber <= 0}) is added.
	 *
	 * @return true if no user modules have been added.
	 */
	public boolean isBlankConfiguration() {
		PamController pamController = PamController.getInstance();
		int n = pamController.getNumControlledUnits();
		for (int i = 0; i < n; i++) {
			if (isUserModule(pamController.getControlledUnit(i))) {
				return false; // a user-added module is present
			}
		}
		return true;
	}

	/**
	 * Whether a controlled unit is a user-added module (as opposed to always-present
	 * infrastructure: core, hidden, essential, or a default singleton).
	 */
	private boolean isUserModule(PamControlledUnit unit) {
		if (unit == null) {
			return false;
		}
		PamModuleInfo moduleInfo = unit.getPamModuleInfo();
		if (moduleInfo == null) {
			return false;
		}
		if (moduleInfo.isCoreModule() || moduleInfo.isHidden() || moduleInfo.getMinNumber() > 0) {
			return false;
		}
		return !DEFAULT_MODULE_CLASSES.contains(unit.getClass().getName());
	}

	/**
	 * Convert a list of File objects to a String array of absolute paths. Null
	 * entries are skipped. If the input list is null or contains no valid files this
	 * returns an empty array.
	 *
	 * @param files list of File objects
	 * @return array of absolute path strings
	 */
	public static String[] filesToPathArray(List<File> files) {
		if (files == null || files.isEmpty()) {
			return new String[0];
		}
		List<String> paths = new ArrayList<>();
		for (File f : files) {
			if (f == null) {
				continue;
			}
			paths.add(f.getAbsolutePath());
		}
		return paths.toArray(new String[0]);
	}
}
