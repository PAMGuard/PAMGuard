package PamController.command;

import PamController.PamController;

public class StopCommand extends ExtCommand {

	public StopCommand() {
		super("stop", false);
	}

	@Override
	public boolean execute() {
		PamController.getInstance().pamStop();
		return true;
	}


	@Override
	public String getHint() {
		return "Stop PAMGuard processing";
	}
}
