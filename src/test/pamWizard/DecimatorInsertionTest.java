package test.pamWizard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingsGroup;
import PamController.UsedModuleInfo;
import PamController.pamWizard.PamFileImport;
import PamController.pamWizard.PamFileTypeResult;
import PamController.pamWizard.PamImportFileType;
import PamController.pamWizard.SoundFileSummary;
import PamController.pamWizard.configurations.ConfigApplyContext;
import PamController.pamWizard.configurations.DefaultConfigLoader;
import PamController.pamWizard.configurations.PamConfigDescription;
import PamController.pamWizard.configurations.PamConfigInspection;
import PamController.pamWizard.configurations.PamConfigRepository;
import decimator.DecimatorParams;

/**
 * Tests the insertion of a decimator into a configuration whose detectors run at
 * a lower sample rate than the imported data.
 * <p>
 * This is the part of loading a configuration most likely to go quietly wrong,
 * because repointing a module's input means rewriting a string buried in its
 * settings. The tests run against the configurations which actually ship with
 * PAMGuard, and check both that the right modules were moved onto the decimated
 * data and - just as important - that the wrong ones were not.
 */
public class DecimatorInsertionTest {

	private static final String ACQ_LONG = "Sound Acquisition, Raw input data from Sound Acquisition";
	private static final String ACQ_SHORT = "Raw input data from Sound Acquisition";
	private static final String DEC_LONG = "Sample Rate Decimator, Sample Rate Decimator Data";
	private static final String DEC_SHORT = "Sample Rate Decimator Data";

	static boolean configurationsAvailable() {
		return new File(System.getProperty("user.dir"), PamConfigRepository.CONFIG_FOLDER_NAME).isDirectory();
	}

	/**
	 * Reaches the protected planning and rewriting methods.
	 */
	private static class TestLoader extends DefaultConfigLoader {

		boolean decimate(PamConfigInspection inspection, PamFileImport files, ConfigApplyContext context) {
			DecimatorPlan plan = planDecimation(inspection, files);
			if (plan == null) {
				return false;
			}
			applyDecimatorPlan(plan, inspection, context);
			return true;
		}
	}

	private PamConfigDescription readShipped(String baseName) {
		File jsonFile = new File(new File(System.getProperty("user.dir"),
				PamConfigRepository.CONFIG_FOLDER_NAME), baseName + ".json");
		PamConfigDescription config = PamConfigRepository.getInstance().readConfiguration(jsonFile);
		assertNotNull(config, "Could not read " + jsonFile.getAbsolutePath());
		return config;
	}

	private PamFileImport anImportAt(float sampleRate) {
		List<File> dropped = new ArrayList<>();
		dropped.add(new File("data"));
		PamFileImport fileImport = new PamFileImport(dropped);
		fileImport.addResult(new PamFileTypeResult(PamImportFileType.SOUND, 10, null,
				new SoundFileSummary(10, 10, sampleRate, sampleRate, 1, 1, true)));
		fileImport.addResult(new PamFileTypeResult(PamImportFileType.SUD_CLICKS, 10, null));
		return fileImport;
	}

	/**
	 * The data source strings held in a module's settings.
	 *
	 * @param inspection the inspected configuration.
	 * @param unitName   the module's unit name.
	 * @return every string in its settings which names a data block.
	 */
	private List<String> sourcesOf(PamConfigInspection inspection, String unitName) {
		List<String> sources = new ArrayList<>();
		PamSettingsGroup psg = inspection.getSettingsGroup();
		for (PamControlledUnitSettings settings : psg.findSettingsForName(unitName)) {
			Object params;
			try {
				params = settings.getSettings();
			}
			catch (Throwable e) {
				continue;
			}
			collectSources(params, sources, 0, new IdentityHashMap<>());
		}
		return sources;
	}

	private void collectSources(Object object, List<String> out, int depth, IdentityHashMap<Object, Object> seen) {
		if (object == null || depth > 6 || seen.put(object, object) != null) {
			return;
		}
		Class<?> objectClass = object.getClass();
		if (objectClass.getName().startsWith("java.") || objectClass.isPrimitive() || objectClass.isEnum()) {
			return;
		}
		for (Class<?> c = objectClass; c != null && c != Object.class; c = c.getSuperclass()) {
			for (Field field : c.getDeclaredFields()) {
				try {
					field.setAccessible(true);
					Object value = field.get(object);
					if (value instanceof String) {
						String text = (String) value;
						if (text.contains("Raw input data") || text.contains("Decimator")) {
							out.add(text);
						}
					}
					else if (value != null && !(value instanceof Number) && !(value instanceof Boolean)) {
						collectSources(value, out, depth + 1, seen);
					}
				}
				catch (Throwable ignored) {
					// an inaccessible field tells us nothing; carry on.
				}
			}
		}
	}

	@Test
	@EnabledIf("configurationsAvailable")
	public void rightWhaleModulesMoveOntoTheDecimatedData() {
		PamConfigInspection inspection = new DefaultConfigLoader().inspect(readShipped("right_whale_dl"));
		assertTrue(inspection.isValid(), inspection.getError());

		// before: the FFT engine and the classifier both read the acquisition data.
		assertTrue(sourcesOf(inspection, "FFT (Spectrogram) Engine").contains(ACQ_LONG));
		assertTrue(sourcesOf(inspection, "Deep Learning Classifier").contains(ACQ_LONG));

		ConfigApplyContext context = new ConfigApplyContext();
		assertTrue(new TestLoader().decimate(inspection, anImportAt(48000), context));

		// after: both read the decimator instead.
		assertTrue(sourcesOf(inspection, "FFT (Spectrogram) Engine").contains(DEC_LONG));
		assertTrue(sourcesOf(inspection, "Deep Learning Classifier").contains(DEC_LONG));
		assertFalse(sourcesOf(inspection, "Deep Learning Classifier").contains(ACQ_LONG));

		assertFalse(context.hasWarnings(), String.valueOf(context.getWarnings()));
	}

	@Test
	@EnabledIf("configurationsAvailable")
	public void theInsertedDecimatorIsCorrectlyConfigured() {
		PamConfigInspection inspection = new DefaultConfigLoader().inspect(readShipped("right_whale_dl"));
		assertTrue(new TestLoader().decimate(inspection, anImportAt(48000), new ConfigApplyContext()));

		PamControlledUnitSettings settings =
				inspection.getSettingsGroup().findUnitSettings("Decimator", "Sample Rate Decimator");
		assertNotNull(settings, "The decimator settings were not added");

		DecimatorParams params = (DecimatorParams) settings.getSettings();
		assertEquals(2000f, params.newSampleRate);
		// it must read the full rate acquisition data, by its long name.
		assertEquals(ACQ_LONG, params.rawDataSource);
		// anti aliasing filter at half the new rate.
		assertNotNull(params.filterParams);
		assertEquals(1000f, params.filterParams.lowPassFreq);
		assertEquals(1, params.channelMap);
		// 48 kHz to 2 kHz is a whole number ratio, so no interpolation is needed.
		assertEquals(0, params.interpolation);
	}

	@Test
	@EnabledIf("configurationsAvailable")
	public void theDecimatorIsAddedRightAfterSoundAcquisition() {
		PamConfigInspection inspection = new DefaultConfigLoader().inspect(readShipped("right_whale_dl"));
		assertTrue(new TestLoader().decimate(inspection, anImportAt(48000), new ConfigApplyContext()));

		List<UsedModuleInfo> modules = inspection.getModules();
		int acquisition = indexOf(modules, "Acquisition.AcquisitionControl");
		int decimator = indexOf(modules, "decimator.DecimatorControl");
		assertTrue(acquisition >= 0);
		assertEquals(acquisition + 1, decimator, "The decimator should follow sound acquisition");
	}

	@Test
	@EnabledIf("configurationsAvailable")
	public void nothingHappensWhenTheDataAreAlreadyAtTheTargetRate() {
		PamConfigInspection inspection = new DefaultConfigLoader().inspect(readShipped("right_whale_dl"));

		// the configuration runs at 2 kHz, so 2 kHz data need no decimation.
		assertFalse(new TestLoader().decimate(inspection, anImportAt(2000), new ConfigApplyContext()));
		assertNull(inspection.getSettingsGroup().findUnitSettings("Decimator", "Sample Rate Decimator"));
		assertTrue(sourcesOf(inspection, "Deep Learning Classifier").contains(ACQ_LONG));
	}

	@Test
	@EnabledIf("configurationsAvailable")
	public void theSoundTrapClickDetectorStaysOnFullRateData() {
		PamConfigInspection inspection = new DefaultConfigLoader().inspect(readShipped("static_monitoring_soundtrap"));
		assertTrue(inspection.isValid(), inspection.getError());

		List<String> before = sourcesOf(inspection, "SoundTrap Click Detector");

		ConfigApplyContext context = new ConfigApplyContext();
		assertTrue(new TestLoader().decimate(inspection, anImportAt(576000), context));

		/*
		 * The click detector extracts clicks from the raw SoundTrap stream. Whatever it
		 * was reading before, it must still be reading afterwards - decimating its input
		 * would destroy the clicks it exists to find.
		 */
		assertEquals(before, sourcesOf(inspection, "SoundTrap Click Detector"));
	}

	@Test
	@EnabledIf("configurationsAvailable")
	public void theLowFrequencyBranchIsChainedBehindTheNewDecimator() {
		PamConfigInspection inspection = new DefaultConfigLoader().inspect(readShipped("static_monitoring_soundtrap"));
		assertTrue(new TestLoader().decimate(inspection, anImportAt(576000), new ConfigApplyContext()));

		/*
		 * The configuration's own "Decimator" produces the low frequency data a whistle
		 * detector runs on. It was reading the acquisition module, so it now reads the
		 * new decimator - it is chained behind it rather than being replaced by it.
		 */
		List<String> lowFreqDecimator = sourcesOf(inspection, "Decimator");
		assertTrue(lowFreqDecimator.contains(DEC_SHORT) || lowFreqDecimator.contains(DEC_LONG),
				"Expected the low frequency decimator to read the new one, found " + lowFreqDecimator);
		assertFalse(lowFreqDecimator.contains(ACQ_SHORT));

		// and the low frequency FFT still reads that decimator, not the new one.
		assertTrue(sourcesOf(inspection, "FFT (Spectrogram) Engine Low Freq").contains("Decimator, Decimator Data"));
	}

	@Test
	@EnabledIf("configurationsAvailable")
	public void theInsertedDecimatorDoesNotClashWithAnExistingOne() {
		PamConfigInspection inspection = new DefaultConfigLoader().inspect(readShipped("static_monitoring_soundtrap"));
		assertTrue(new TestLoader().decimate(inspection, anImportAt(576000), new ConfigApplyContext()));

		// the configuration already has a module called "Decimator", so the new one
		// must have been given a different name.
		int decimators = 0;
		for (UsedModuleInfo module : inspection.getModules()) {
			if ("decimator.DecimatorControl".equals(module.className)) {
				decimators++;
			}
		}
		assertEquals(2, decimators);
		assertNotNull(inspection.getSettingsGroup().findUnitSettings("Decimator", "Decimator"));
		assertNotNull(inspection.getSettingsGroup().findUnitSettings("Decimator", "Sample Rate Decimator"));
	}

	private int indexOf(List<UsedModuleInfo> modules, String className) {
		for (int i = 0; i < modules.size(); i++) {
			if (className.equals(modules.get(i).className)) {
				return i;
			}
		}
		return -1;
	}
}
