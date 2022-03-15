package PamController.command;

import PamController.PamController;

public class StopCommand extends ExtCommand {

	public StopCommand() {
		super("stop", false);
	}

	@Override
	public String execute(String command) {
		PamController.getInstance().pamStop();
		return getName();
	}


	@Override
	public String getHint() {
		return "Stop PAMGuard processing";
	}
}
