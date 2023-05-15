package PamController.command;

import PamController.PamController;
import PamController.PamSettingManager;

/**
 * Exits PAMGuard. Attempts to issue a stop command before exiting. 
 * @author Doug Gillespie
 *
 */
public class ExitCommand extends ExtCommand {

	public static final String commandId = "Exit";
	
	public ExitCommand() {
		super(commandId, false);
	}

	@Override
	public String execute(String command) {
		PamController.getInstance().pamStop();
		PamSettingManager.getInstance().saveFinalSettings();
		PamController pamController = PamController.getInstance();
		pamController.pamClose();
		// shut down the JavaFX thread and the JVM
		pamController.shutDownPamguard();
		return getName();
	}

	@Override
	public String getHint() {
		return "Exit PAMGuard, stopping detectors and saving configuration prior to exiting";
	}


}
