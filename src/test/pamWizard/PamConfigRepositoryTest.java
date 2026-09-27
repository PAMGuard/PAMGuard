package test.pamWizard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import PamController.pamWizard.configurations.ConfigSpeciesGroup;
import PamController.pamWizard.configurations.PamConfigDescription;
import PamController.pamWizard.configurations.PamConfigRepository;
import PamController.pamWizard.PamImportFileType;

/**
 * Tests for reading configuration descriptors from disk.
 */
public class PamConfigRepositoryTest {

	/**
	 * A minimal but complete descriptor.
	 */
	private static final String FULL_JSON = "{\n"
			+ "  \"configType\": \"default\",\n"
			+ "  \"name\": \"Test right whale\",\n"
			+ "  \"description\": \"A test configuration.\",\n"
			+ "  \"minSampleRate\": 2000,\n"
			+ "  \"targetSampleRate\": 2000,\n"
			+ "  \"minChannels\": 1,\n"
			+ "  \"medium\": \"water\",\n"
			+ "  \"requiredFileTypes\": [\"SOUND\"],\n"
			+ "  \"taxa\": [ { \"group\": \"BALEEN_WHALE\", \"species\": [\"North Atlantic right whale\"] } ]\n"
			+ "}";

	private File writeConfig(Path folder, String baseName, String json, boolean withPsfx) throws Exception {
		File jsonFile = folder.resolve(baseName + ".json").toFile();
		Files.writeString(jsonFile.toPath(), json);
		if (withPsfx) {
			Files.write(folder.resolve(baseName + ".psfx"), new byte[] { 1, 2, 3 });
		}
		return jsonFile;
	}

	@Test
	public void readsAFullDescriptor(@TempDir Path folder) throws Exception {
		File jsonFile = writeConfig(folder, "right_whale", FULL_JSON, true);

		PamConfigDescription config = PamConfigRepository.getInstance().readConfiguration(jsonFile);

		assertNotNull(config);
		assertEquals("Test right whale", config.getName());
		assertEquals("default", config.getConfigType());
		assertEquals(2000, config.getMinSampleRate());
		assertEquals(2000., config.getTargetSampleRate());
		assertEquals(1, config.getMinChannels());
		assertNull(config.getMaxSampleRate());
		assertEquals(ConfigSpeciesGroup.BALEEN_WHALE, config.getPrimaryGroup());
		assertEquals(1, config.getSpeciesList().length);
		assertEquals("North Atlantic right whale", config.getSpeciesList()[0]);
		// the psfx is found from the json base name.
		assertEquals("right_whale.psfx", config.getPsfxFile().getName());
	}

	@Test
	public void appliesDefaultsToASparseDescriptor(@TempDir Path folder) throws Exception {
		File jsonFile = writeConfig(folder, "sparse", "{ \"name\": \"Sparse\" }", true);

		PamConfigDescription config = PamConfigRepository.getInstance().readConfiguration(jsonFile);

		assertNotNull(config);
		// no configType means the default loader.
		assertEquals("default", config.getConfigType());
		assertEquals(0, config.getMinSampleRate());
		assertEquals(1, config.getMinChannels());
		assertNull(config.getTargetSampleRate());
		// medium unset means suitable for both air and water.
		assertNull(config.getMedium());
		assertEquals(1, config.getRequiredFileTypes().size());
		assertTrue(config.getRequiredFileTypes().contains(PamImportFileType.SOUND));
		// no taxa means it is not tied to any species group.
		assertEquals(ConfigSpeciesGroup.OTHER, config.getPrimaryGroup());
		// and the SoundTrap click detector is protected from decimation by default.
		assertTrue(config.getKeepRawSourceModules().contains("soundtrap.STClickControl"));
	}

	@Test
	public void ignoresUnknownFields(@TempDir Path folder) throws Exception {
		String json = "{ \"name\": \"Future\", \"somethingFromANewerVersion\": { \"a\": 1 } }";
		File jsonFile = writeConfig(folder, "future", json, true);

		PamConfigDescription config = PamConfigRepository.getInstance().readConfiguration(jsonFile);

		assertNotNull(config);
		assertEquals("Future", config.getName());
	}

	@Test
	public void rejectsADescriptorWithNoSettingsFile(@TempDir Path folder) throws Exception {
		File jsonFile = writeConfig(folder, "no_psfx", FULL_JSON, false);

		// there is nothing to load, so the configuration must not be offered.
		assertNull(PamConfigRepository.getInstance().readConfiguration(jsonFile));
	}

	@Test
	public void rejectsMalformedJson(@TempDir Path folder) throws Exception {
		File jsonFile = writeConfig(folder, "broken", "{ this is not json", true);

		assertNull(PamConfigRepository.getInstance().readConfiguration(jsonFile));
	}

	@Test
	public void unrecognisedSpeciesGroupFallsBackToOther() {
		assertEquals(ConfigSpeciesGroup.OTHER, ConfigSpeciesGroup.fromString("Giant Squid"));
		assertEquals(ConfigSpeciesGroup.OTHER, ConfigSpeciesGroup.fromString(null));
	}

	@Test
	public void speciesGroupNamesAreFlexible() {
		// the same group written the various ways a descriptor might spell it.
		assertEquals(ConfigSpeciesGroup.BALEEN_WHALE, ConfigSpeciesGroup.fromString("BALEEN_WHALE"));
		assertEquals(ConfigSpeciesGroup.BALEEN_WHALE, ConfigSpeciesGroup.fromString("baleen whale"));
		assertEquals(ConfigSpeciesGroup.BALEEN_WHALE, ConfigSpeciesGroup.fromString("Baleen-Whale"));
		assertEquals(ConfigSpeciesGroup.NBHF, ConfigSpeciesGroup.fromString("nbhf"));
	}

	@Test
	public void everySpeciesGroupHasAnIcon() {
		for (ConfigSpeciesGroup group : ConfigSpeciesGroup.values()) {
			assertNotNull(getClass().getResourceAsStream(group.getIconResource()),
					"Missing icon resource for " + group + ": " + group.getIconResource());
		}
	}
}
