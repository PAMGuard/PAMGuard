package PamController.command;

import PamController.PamController;

public class StopCommand extends ExtCommand {
	
	public static final String commandId = "stop";

	public StopCommand() {
		super(commandId, false);
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
