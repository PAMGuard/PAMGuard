package PamController.command;

import PamController.PamController;

public class StartCommand extends ExtCommand {
	
	private String returnString;

	public StartCommand() {
//		super("start", true);
		super("start", false);
		returnString = this.getName();	// this is what ExtCommand defaults to
	}

	@Override
	public String execute(String command) {
		boolean ok = PamController.getInstance().pamStart();
		return getReturnString();
	}

	@Override
	public boolean canExecute() {
		if (PamController.getInstance().isInitializationComplete()) {
			returnString = this.getName();
			return true;
		}
		else {
			System.out.println("Cannot call PamStart - initialization not yet complete");
			returnString = "Initialization not yet complete";
			return false;
		}
	}

	public String getReturnString() {
		return returnString;
	}

	@Override
	public String getHint() {
		return "Start PAMGuard processing";
	}

}
