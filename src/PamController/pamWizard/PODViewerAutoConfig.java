package PamController.pamWizard;

import PamController.PamController;
import PamController.pamWizard.configurations.ConfigApplyContext;
import PamController.pamWizard.configurations.ConfigSpeciesGroup;
import PamController.soundMedium.GlobalMedium.SoundMedium;

/**
 * Auto-configuration (viewer mode) that imports dropped CPOD/FPOD detection files
 * and shows the clicks on a time display, coloured by peak frequency. Builds
 * Binary Storage + CPOD Detector Import + an FX Time Display, and runs the
 * importer on the dropped files.
 *
 * @author Jamie Macaulay
 */
public class PODViewerAutoConfig implements PamAutoConfig {

	/**
	 * Where the wizard said the binary data should go. Null until the wizard hands
	 * it over, in which case the builder picks a folder itself.
	 */
	private ConfigApplyContext applyContext;

	@Override
	public boolean isValid(PamFileImport importHandler, int runMode) {
		// the CPOD module - and the binary conversion it does - is viewer only.
		if (runMode != PamController.RUN_PAMVIEW) {
			return false;
		}
		return importHandler.hasType(PamImportFileType.CPOD) || importHandler.hasType(PamImportFileType.FPOD);
	}

	/**
	 * The POD data are converted into binary files, so the user is asked where the
	 * binary store should go.
	 */
	@Override
	public boolean needsBinaryStore() {
		return true;
	}

	/**
	 * The click train classifications go to the database, but in viewer mode a
	 * database is already open before any files can be dropped, so there is nothing
	 * to ask about.
	 */
	@Override
	public boolean needsDatabase() {
		return false;
	}

	@Override
	public void setApplyContext(ConfigApplyContext applyContext) {
		this.applyContext = applyContext;
	}

	@Override
	public void createConfiguration(PamFileImport importHandler) {
		new PODConfigBuilder().build(importHandler, applyContext);
	}

	@Override
	public String getConfigName() {
		return "View CPOD/FPOD detections";
	}

	@Override
	public String getConfigDescription() {
		return "Import CPOD/FPOD click detections and view them on a time display.\n\n"
				+ "Sets up binary storage, the CPOD/FPOD importer and a time display. The CP1, "
				+ "CP3, FP1 and FP3 files are converted into PAMGuard binary files straight away, "
				+ "which only has to happen once - the next page asks where those files should go."
				+ "\n\n"
				+ "The clicks are coloured by their peak frequency, so that narrow band high "
				+ "frequency clicks (e.g. harbour porpoise) and lower frequency dolphin clicks are "
				+ "easy to tell apart. The display loads six hours of data at a time, showing 30 "
				+ "minutes, and opens at the first detection.";
	}

	@Override
	public String[] getSpeciesList() {
		/*
		 * PODs classify their click trains as NBHF or dolphin, so name the same groups
		 * the file based configurations use. Note that only file based configurations
		 * take part in the wizard's species group filter, so this is shown as text
		 * rather than driving the filter.
		 */
		return new String[] { ConfigSpeciesGroup.NBHF.getGroupName(), ConfigSpeciesGroup.DOLPHIN.getGroupName() };
	}

	@Override
	public SoundMedium getGlobalMediumSettings() {
		// CPODs and FPODs are deployed underwater.
		return SoundMedium.Water;
	}
}
