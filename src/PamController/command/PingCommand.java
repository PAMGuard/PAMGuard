package PamController.command;

import PamController.PamController;

public class PingCommand extends ExtCommand {
	
	public PingCommand() {
		super("ping", true);
	}

	@Override
	public boolean execute() {
		return true;
	}

	@Override
	public String getHint() {
		return "ping PAMGuard to see if it's alive";
	}
}
