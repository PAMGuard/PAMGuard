package PamController.pamWizard.configurations;

import Acquisition.AcquisitionControl;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.pamWizard.AcquisitionConfigurer;
import PamController.pamWizard.PamAutoConfig;
import PamController.pamWizard.PamFileImport;
import PamController.soundMedium.GlobalMedium.SoundMedium;
import PamModel.PamModuleInfo;

/**
 * The blank configuration, which is always offered whatever files the user
 * imported. It creates nothing but a Sound Acquisition module, already set up
 * with the imported files, leaving the user to add whatever detectors and
 * displays they want.
 * <p>
 * This is what makes the wizard safe to show for any import: there is always at
 * least one thing the user can choose, so a set of files which matches no
 * configuration is still a useful starting point rather than a dead end.
 *
 * @author Jamie Macaulay
 */
public class BlankConfigAutoConfig implements PamAutoConfig {

	public static final String CONFIG_NAME = "Blank configuration";

	public static final String ACQUISITION_CLASS = "Acquisition.AcquisitionControl";

	public static final String ACQUISITION_NAME = "Sound Acquisition";

	@Override
	public boolean isValid(PamFileImport importHandler, int runMode) {
		// always available, whatever was imported.
		return true;
	}

	@Override
	public void createConfiguration(PamFileImport importHandler) {
		PamModuleInfo moduleInfo = PamModuleInfo.findModuleInfo(ACQUISITION_CLASS);
		if (moduleInfo == null) {
			System.err.println("BlankConfigAutoConfig: could not find module " + ACQUISITION_CLASS);
			return;
		}
		PamControlledUnit unit = PamController.getInstance().addModule(moduleInfo, ACQUISITION_NAME);
		if (unit instanceof AcquisitionControl) {
			AcquisitionConfigurer.configure((AcquisitionControl) unit, importHandler, false);
		}
		PamController.getInstance().notifyModelChanged(PamControllerInterface.CHANGED_PROCESS_SETTINGS);
	}

	@Override
	public String getConfigDescription() {
		return "Start with an empty configuration. A Sound Acquisition module is added and set up to read "
				+ "the files you imported; everything else is up to you.";
	}

	@Override
	public String[] getSpeciesList() {
		return null;
	}

	@Override
	public String getConfigName() {
		return CONFIG_NAME;
	}

	@Override
	public SoundMedium getGlobalMediumSettings() {
		// suitable for both air and water.
		return null;
	}

	@Override
	public String toString() {
		return CONFIG_NAME;
	}
}
