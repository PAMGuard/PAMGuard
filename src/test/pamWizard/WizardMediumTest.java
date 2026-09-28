package test.pamWizard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import PamController.PamController;
import PamController.pamWizard.PamAutoConfig;
import PamController.pamWizard.PamFileImport;
import PamController.pamWizard.SpectrogramRealTimeAutoConfig;
import PamController.pamWizard.configurations.BlankConfigAutoConfig;
import PamController.pamWizard.configurations.ConfigWizardData;
import PamController.pamWizard.configurations.FileConfigAutoConfig;
import PamController.pamWizard.configurations.PamConfigDescription;
import PamController.pamWizard.configurations.PamConfigRepository;
import PamController.soundMedium.GlobalMedium.SoundMedium;

/**
 * Tests which configurations let the user choose between air and water.
 * <p>
 * A configuration written for a particular medium fixes it - a right whale
 * detector is of no use in air - while a general purpose one such as the
 * spectrogram leaves the choice to the user. The wizard enables or disables its
 * medium chooser on exactly that distinction, so it is worth pinning down.
 */
public class WizardMediumTest {

	static boolean configurationsAvailable() {
		return new File(System.getProperty("user.dir"), PamConfigRepository.CONFIG_FOLDER_NAME).isDirectory();
	}

	private ConfigWizardData wizardData() {
		return new ConfigWizardData(new PamFileImport(new ArrayList<>()),
				PamController.RUN_NORMAL, new ArrayList<PamAutoConfig>());
	}

	@Test
	public void generalPurposeConfigurationsLeaveTheMediumOpen() {
		// a null medium is what the wizard reads as "let the user decide".
		assertNull(new SpectrogramRealTimeAutoConfig().getGlobalMediumSettings());
		assertNull(new BlankConfigAutoConfig().getGlobalMediumSettings());
	}

	@Test
	@EnabledIf("configurationsAvailable")
	public void aWaterConfigurationFixesTheMedium() {
		File jsonFile = new File(new File(System.getProperty("user.dir"),
				PamConfigRepository.CONFIG_FOLDER_NAME), "right_whale_dl.json");
		PamConfigDescription config = PamConfigRepository.getInstance().readConfiguration(jsonFile);
		assertNotNull(config);

		assertEquals(SoundMedium.Water, config.getMedium());
		assertEquals(SoundMedium.Water, new FileConfigAutoConfig(config).getGlobalMediumSettings());
	}

	@Test
	public void theWizardStartsFromTheCurrentMediumAndRemembersAChoice() {
		ConfigWizardData data = wizardData();

		// with no controller running this falls back to water rather than to null.
		assertNotNull(data.getMedium());

		data.setMedium(SoundMedium.Air);
		assertEquals(SoundMedium.Air, data.getMedium());

		// a null choice must never wipe out a good one.
		data.setMedium(null);
		assertEquals(SoundMedium.Air, data.getMedium());
	}

	@Test
	public void bothMediaAreOfferedToTheUser() {
		List<SoundMedium> media = List.of(SoundMedium.values());
		assertEquals(2, media.size(), "The chooser offers every SoundMedium, so there should be just air and water");
		assertEquals(true, media.contains(SoundMedium.Air));
		assertEquals(true, media.contains(SoundMedium.Water));
	}
}
