package PamController.command;

import PamController.PamController;
import PamController.PamSettingManager;

/**
 * Exits PAMGuard. Attempts to issue a stop command before exiting. 
 * @author Doug Gillespie
 *
 */
public class ExitCommand extends ExtCommand {

	public ExitCommand() {
		super("Exit", false);
	}

	@Override
	public String execute(String command) {
		PamController.getInstance().pamStop();
		PamSettingManager.getInstance().saveFinalSettings();
		System.exit(0);
		return getName();
	}

	@Override
	public String getHint() {
		return "Exit PAMGuard, stopping detectors and saving configuration prior to exiting";
	}


}
