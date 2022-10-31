package PamController.command;

import PamController.PamController;

public class ExitNoSaveCommand extends ExtCommand {

	public ExitNoSaveCommand() {
		super("ExitNoSave", false);
	}

	@Override
	public String execute(String command) {
		PamController.getInstance().pamStop();
		System.exit(0);
		return getName();
	}

	@Override
	public String getHint() {
		return "Exit PAMGuard, stopping detectors but not saving configuration";
	}


}
