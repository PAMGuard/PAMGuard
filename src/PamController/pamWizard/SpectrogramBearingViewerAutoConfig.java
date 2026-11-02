package PamController.pamWizard;

import java.util.Set;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.pamWizard.configurations.ConfigApplyContext;
import PamController.pamWizard.configurations.ConfigSpeciesGroup;
import PamController.soundMedium.GlobalMedium.SoundMedium;
import PamguardMVC.PamConstants;
import PamView.GroupedSourceParameters;
import PamView.dialog.GroupedSourcePanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamRawDataBlock;
import bearinglocaliser.BearingLocaliserControl;
import bearinglocaliser.BearingLocaliserParams;
import bearinglocaliser.algorithms.BearingAlgorithmProvider;

/**
 * Auto-configuration (viewer mode) for multi-channel sound files: the spectrogram
 * and annotation setup of {@link SpectrogramViewerAutoConfig} plus a Bearing
 * Localiser which works out a bearing for every annotation the user draws, and
 * updates it whenever the annotation is moved or resized.
 * <p>
 * Only offered when every imported file has at least two channels, since a
 * bearing can't be calculated from one.
 *
 * @author Jamie Macaulay
 */
public class SpectrogramBearingViewerAutoConfig implements PamAutoConfig {

	public static final String BEARING_LOCALISER_CLASS = "bearinglocaliser.BearingLocaliserControl";

	/** Bearing algorithm used for each channel group (time delay based). */
	public static final String BEARING_ALGORITHM = "TDOA Localiser";

	private SpectrogramOptions spectrogramOptions;

	@Override
	public boolean isValid(PamFileImport importHandler, int runMode) {
		if (runMode != PamController.RUN_PAMVIEW || !importHandler.hasType(PamImportFileType.SOUND)) {
			return false;
		}
		SoundFileSummary summary = importHandler.getSoundSummary();
		return summary != null && summary.isValid() && summary.getMinChannels() >= 2;
	}

	@Override
	public void createConfiguration(PamFileImport importHandler) {
		SpectrogramConfigBuilder builder = new SpectrogramConfigBuilder();
		builder.build(importHandler, true, spectrogramOptions);
		addBearingLocaliser(builder);
	}

	/**
	 * Add a Bearing Localiser using the annotations as its detections, and the raw
	 * audio from Sound Acquisition (all channels, as one group) to localise them.
	 * The raw audio is used rather than the spectrogram FFT data since it's at the
	 * full sample rate even if the spectrogram is decimated, which matters for time
	 * delays, and it can be read back from file for any annotation.
	 */
	private void addBearingLocaliser(SpectrogramConfigBuilder builder) {
		PamDataBlock annotationBlock = PamAutoConfigUtils.getOutputDataBlock(builder.getAnnotationControl());
		if (annotationBlock == null || builder.getAcquisitionControl() == null) {
			System.err.println("SpectrogramBearingViewerAutoConfig: annotation or acquisition module missing - no bearing localiser added");
			return;
		}
		PamControlledUnit unit = PamAutoConfigUtils.addModule(BEARING_LOCALISER_CLASS, "Bearing Localiser");
		if (!(unit instanceof BearingLocaliserControl)) {
			System.err.println("SpectrogramBearingViewerAutoConfig: bearing localiser module could not be added");
			return;
		}
		BearingLocaliserControl bearingControl = (BearingLocaliserControl) unit;
		PamRawDataBlock rawData = builder.getAcquisitionControl().getAcquisitionProcess().getRawDataBlock();

		BearingLocaliserParams params = bearingControl.getBearingLocaliserParams();
		params.detectionSource = annotationBlock.getLongDataName();
		int channelMap = rawData.getChannelMap();
		// every channel in group 0.
		int[] channelGroups = new int[PamConstants.MAX_CHANNELS];
		params.setRawOrFFTSourceParameters(new GroupedSourceParameters(rawData.getLongDataName(), channelMap,
				channelGroups, GroupedSourcePanel.GROUP_ALL));
		params.clearAlgorithmNames();
		params.addAlgorithmName(BEARING_ALGORITHM);
		BearingAlgorithmProvider provider = bearingControl.findAlgorithmByName(BEARING_ALGORITHM);
		if (provider != null) {
			params.setAlgorithmParams(BEARING_ALGORITHM, 0, channelMap, provider.createNewParams(0, channelMap));
		}
		bearingControl.setBearingLocaliserParams(params);

		PamController.getInstance().notifyModelChanged(PamControllerInterface.CHANGED_PROCESS_SETTINGS);
	}

	@Override
	public String getConfigName() {
		return "View a spectrogram and measure bearings";
	}

	@Override
	public String getConfigDescription() {
		return "View and annotate a spectrogram of multi-channel sound files, with a bearing "
				+ "calculated for each annotation.\n\n"
				+ "Sets up the same spectrogram and annotation modules as \"View a spectrogram\", "
				+ "plus a Bearing Localiser. Every annotation drawn on the spectrogram gets a "
				+ "bearing from the time delays between channels, which is updated whenever the "
				+ "annotation is moved or resized. Check the hydrophone positions in the array "
				+ "manager, since the bearings depend on them.";
	}

	@Override
	public String[] getSpeciesList() {
		return null;
	}

	@Override
	public Set<ConfigSpeciesGroup> getSpeciesGroups(double sampleRate, SoundMedium medium) {
		return ConfigSpeciesGroup.getVisibleGroups(sampleRate, medium);
	}

	@Override
	public SoundMedium getGlobalMediumSettings() {
		return null;
	}

	@Override
	public boolean hasSpectrogramOptions() {
		return true;
	}

	@Override
	public void setApplyContext(ConfigApplyContext applyContext) {
		spectrogramOptions = applyContext.getSpectrogramOptions();
	}
}
