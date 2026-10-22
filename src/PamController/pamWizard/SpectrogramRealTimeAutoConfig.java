package PamController.pamWizard;

import java.util.Set;

import PamController.PamController;
import PamController.pamWizard.configurations.ConfigSpeciesGroup;
import PamController.soundMedium.GlobalMedium.SoundMedium;

/**
 * Auto-configuration (real-time / normal mode) that shows a live spectrogram of
 * the dropped sound files. Builds Sound Acquisition + FFT + FX Time Display
 * (spectrogram), without the viewer-mode datamap / annotation setup.
 *
 * @author Jamie Macaulay
 */
public class SpectrogramRealTimeAutoConfig implements PamAutoConfig {

	@Override
	public boolean isValid(PamFileImport importHandler, int runMode) {
		return runMode == PamController.RUN_NORMAL && importHandler.hasType(PamImportFileType.SOUND);
	}

	@Override
	public void createConfiguration(PamFileImport importHandler) {
		new SpectrogramConfigBuilder().build(importHandler, false);
	}

	@Override
	public String getConfigName() {
		return "Real-time spectrogram";
	}

	@Override
	public String getConfigDescription() {
		return "Show a live spectrogram of the sound files as they are processed.\n\n"
				+ "Sets up sound acquisition, an FFT (spectrogram) engine and a time-display "
				+ "spectrogram. Press the run button to start processing.";
	}

	@Override
	public String[] getSpeciesList() {
		return null;
	}

	/**
	 * A spectrogram is not written for any species - it shows whatever was
	 * recorded - so the groups picked out are simply the ones which live in the
	 * medium the user chose and whose sounds fall below the Nyquist frequency of
	 * the files that were imported. That says nothing about what is actually there,
	 * only about what these recordings are capable of showing.
	 */
	@Override
	public Set<ConfigSpeciesGroup> getSpeciesGroups(double sampleRate, SoundMedium medium) {
		return ConfigSpeciesGroup.getVisibleGroups(sampleRate, medium);
	}

	@Override
	public SoundMedium getGlobalMediumSettings() {
		return null;
	}
}
