package test.pamWizard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import PamController.PamController;
import PamController.pamWizard.PamFileImport;
import PamController.pamWizard.PamFileTypeResult;
import PamController.pamWizard.PamImportFileType;
import PamController.pamWizard.SoundFileSummary;
import PamController.pamWizard.configurations.ConfigSpeciesGroup;
import PamController.pamWizard.configurations.DefaultConfigLoader;
import PamController.pamWizard.configurations.PamConfigDescription;
import PamController.pamWizard.configurations.PamConfigInspection;
import PamController.pamWizard.configurations.PamConfigRepository;

/**
 * Tests the configurations which ship with PAMGuard: that their descriptors and
 * settings files are consistent with each other, and that they are offered for
 * the right imports and withheld for the wrong ones.
 * <p>
 * Reading a psfx does not need a running PAM controller, so these run headless.
 */
public class ShippedConfigurationsTest {

	private static final String RIGHT_WHALE = "right_whale_dl";
	private static final String STATIC_MONITORING = "static_monitoring_soundtrap";

	private static File configFolder;

	@BeforeAll
	public static void findConfigFolder() {
		configFolder = new File(System.getProperty("user.dir"), PamConfigRepository.CONFIG_FOLDER_NAME);
	}

	static boolean configurationsAvailable() {
		File folder = new File(System.getProperty("user.dir"), PamConfigRepository.CONFIG_FOLDER_NAME);
		return folder.isDirectory();
	}

	private PamConfigDescription read(String baseName) {
		File jsonFile = new File(configFolder, baseName + ".json");
		assertTrue(jsonFile.exists(), "Missing shipped configuration " + jsonFile.getAbsolutePath());
		PamConfigDescription config = PamConfigRepository.getInstance().readConfiguration(jsonFile);
		assertNotNull(config, "Could not read " + jsonFile.getAbsolutePath());
		return config;
	}

	/**
	 * Build an import as if the user had dropped in files of the given format.
	 *
	 * @param sampleRate sample rate of every file, in Hz.
	 * @param channels   channel count of every file.
	 * @param sudClicks  whether the files are sud files holding click detections.
	 */
	private PamFileImport anImportOf(float sampleRate, int channels, boolean sudClicks) {
		List<File> dropped = new ArrayList<>();
		dropped.add(new File("somewhere"));
		PamFileImport fileImport = new PamFileImport(dropped);

		SoundFileSummary summary =
				new SoundFileSummary(10, 10, sampleRate, sampleRate, channels, channels, sudClicks);
		fileImport.addResult(new PamFileTypeResult(PamImportFileType.SOUND, 10, null, summary));
		if (sudClicks) {
			fileImport.addResult(new PamFileTypeResult(PamImportFileType.SUD_CLICKS, 10, null));
		}
		return fileImport;
	}

	private String reasonFor(PamConfigDescription config, PamFileImport fileImport) {
		return PamConfigRepository.getInstance().getMismatchReason(config, fileImport, PamController.RUN_NORMAL);
	}

	@Test
	@EnabledIf("configurationsAvailable")
	public void rightWhaleDescriptorIsAsExpected() {
		PamConfigDescription config = read(RIGHT_WHALE);

		assertEquals(2000, config.getMinSampleRate());
		assertEquals(2000., config.getTargetSampleRate());
		assertEquals(1, config.getMinChannels());
		assertEquals(ConfigSpeciesGroup.BALEEN_WHALE, config.getPrimaryGroup());
		assertTrue(config.getRequiredFileTypes().contains(PamImportFileType.SOUND));
		assertFalse(config.getRequiredFileTypes().contains(PamImportFileType.SUD_CLICKS));
	}

	@Test
	@EnabledIf("configurationsAvailable")
	public void staticMonitoringDescriptorIsAsExpected() {
		PamConfigDescription config = read(STATIC_MONITORING);

		assertEquals(96000, config.getMinSampleRate());
		assertEquals(96000., config.getTargetSampleRate());
		assertTrue(config.getRequiredFileTypes().contains(PamImportFileType.SUD_CLICKS));
		assertTrue(config.getGroups().contains(ConfigSpeciesGroup.NBHF));
		// its click detector must never be moved onto decimated data.
		assertTrue(config.getKeepRawSourceModules().contains("soundtrap.STClickControl"));
		/*
		 * Its Decimator feeds a low frequency whistle detector - it is part of what the
		 * configuration does, not a sample rate adapter - so it must not be named as one
		 * to retune.
		 */
		assertNull(config.getDecimatorUnitName());
	}

	@Test
	@EnabledIf("configurationsAvailable")
	public void rightWhaleIsOfferedForAnythingAboveTwoKiloHertz() {
		PamConfigDescription config = read(RIGHT_WHALE);

		assertNull(reasonFor(config, anImportOf(2000, 1, false)));
		assertNull(reasonFor(config, anImportOf(48000, 1, false)));
		assertNull(reasonFor(config, anImportOf(500000, 2, false)));
	}

	@Test
	@EnabledIf("configurationsAvailable")
	public void rightWhaleIsWithheldBelowItsMinimumSampleRate() {
		PamConfigDescription config = read(RIGHT_WHALE);

		String reason = reasonFor(config, anImportOf(1000, 1, false));
		assertNotNull(reason);
		assertTrue(reason.contains("sample rate"), reason);
	}

	@Test
	@EnabledIf("configurationsAvailable")
	public void staticMonitoringNeedsSudClickDetections() {
		PamConfigDescription config = read(STATIC_MONITORING);

		// plain audio at the right rate is not enough.
		String reason = reasonFor(config, anImportOf(96000, 1, false));
		assertNotNull(reason);
		assertTrue(reason.contains(PamImportFileType.SUD_CLICKS.getName()), reason);

		// sud files with detections are.
		assertNull(reasonFor(config, anImportOf(96000, 1, true)));
		assertNull(reasonFor(config, anImportOf(576000, 1, true)));
	}

	@Test
	@EnabledIf("configurationsAvailable")
	public void aHighSampleRateImportOffersEverything() {
		PamFileImport fileImport = anImportOf(576000, 1, true);

		assertNull(reasonFor(read(RIGHT_WHALE), fileImport));
		assertNull(reasonFor(read(STATIC_MONITORING), fileImport));
	}

	@Test
	@EnabledIf("configurationsAvailable")
	public void rightWhaleSettingsFileMatchesItsDescriptor() {
		PamConfigDescription config = read(RIGHT_WHALE);
		PamConfigInspection inspection = new DefaultConfigLoader().inspect(config);

		assertTrue(inspection.isValid(), inspection.getError());
		assertTrue(inspection.hasModule("Acquisition.AcquisitionControl"));
		assertTrue(inspection.hasModule("rawDeepLearningClassifier.DLControl"));
		assertTrue(inspection.hasBinaryStore());
		assertTrue(inspection.hasDatabase());
		assertEquals("Sound Acquisition", inspection.getAcquisitionUnitName());
		// the configuration was built at the rate its detectors expect.
		assertEquals(2000., inspection.getPsfxSampleRate());
		assertEquals(2000., inspection.getTargetSampleRate());
		// there is no decimator, so one has to be inserted for higher rate data.
		assertNull(inspection.getExistingDecimatorName());
		assertTrue(inspection.needsDecimation(48000));
		assertFalse(inspection.needsDecimation(2000));
	}

	@Test
	@EnabledIf("configurationsAvailable")
	public void staticMonitoringSettingsFileMatchesItsDescriptor() {
		PamConfigDescription config = read(STATIC_MONITORING);
		PamConfigInspection inspection = new DefaultConfigLoader().inspect(config);

		assertTrue(inspection.isValid(), inspection.getError());
		assertTrue(inspection.hasModule("Acquisition.AcquisitionControl"));
		assertTrue(inspection.hasModule("soundtrap.STClickControl"),
				"The static monitoring configuration must contain a SoundTrap Click Detector, "
						+ "otherwise gating it on sud click detections makes no sense");
		assertTrue(inspection.hasModule("decimator.DecimatorControl"));
		assertTrue(inspection.hasBinaryStore());
		assertTrue(inspection.hasDatabase());
		assertEquals(96000., inspection.getPsfxSampleRate());
		/*
		 * The psfx does contain a decimator, but it is the low frequency branch rather
		 * than a rate adapter, so inspection must not offer it up to be retuned.
		 */
		assertNull(inspection.getExistingDecimatorName());
	}

	@Test
	@EnabledIf("configurationsAvailable")
	public void bothConfigurationsAreFoundByAFolderScan() {
		List<PamConfigDescription> found = PamConfigRepository.getInstance().rescan();

		assertTrue(found.size() >= 2, "Expected to find the shipped configurations, found " + found.size());
		assertTrue(found.stream().anyMatch(c -> RIGHT_WHALE.equals(c.getKey())));
		assertTrue(found.stream().anyMatch(c -> STATIC_MONITORING.equals(c.getKey())));
	}
}
