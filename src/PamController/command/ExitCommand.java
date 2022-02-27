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
	public boolean execute() {
		PamController.getInstance().pamStop();
		PamSettingManager.getInstance().saveFinalSettings();
		System.exit(0);
		return true;
	}

	@Override
	public String getHint() {
		return "Exit PAMGuard, stopping detectors prior to exiting";
	}


}
