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

}
