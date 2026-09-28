package PamController.pamWizard;

import PamController.PamController;
import PamController.soundMedium.GlobalMedium.SoundMedium;

/**
 * Auto-configuration (viewer mode) that lets a user view and annotate a
 * spectrogram of dropped sound files. Builds Sound Acquisition + FFT + FX Time
 * Display (spectrogram) + Spectrogram Annotation module, with a 1 hour loaded
 * window, 20 s visible range, auto-loaded at the first file.
 *
 * @author Jamie Macaulay
 */
public class SpectrogramViewerAutoConfig implements PamAutoConfig {

	@Override
	public boolean isValid(PamFileImport importHandler, int runMode) {
		return runMode == PamController.RUN_PAMVIEW && importHandler.hasType(PamImportFileType.SOUND);
	}

	@Override
	public void createConfiguration(PamFileImport importHandler) {
		new SpectrogramConfigBuilder().build(importHandler, true);
	}

	@Override
	public String getConfigName() {
		return "View a spectrogram";
	}

	@Override
	public String getConfigDescription() {
		return "View and annotate a spectrogram of the sound files.\n\n"
				+ "Sets up sound acquisition, an FFT (spectrogram) engine, a time-display "
				+ "spectrogram and a spectrogram annotation module for manual annotation. The "
				+ "display loads one hour of data at a time, showing 20 seconds, and opens at "
				+ "the start of the first file.";
	}

	@Override
	public String[] getSpeciesList() {
		return null;
	}

	@Override
	public SoundMedium getGlobalMediumSettings() {
		// spectrograms don't care whether air or water.
		return null;
	}
}
