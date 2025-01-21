package PamController.command;

/**
 * Kills PAMGuard, not attempting to stop it first. 
 * @author Doug Gillespie
 *
 */
public class KillCommand extends ExtCommand {

	public KillCommand() {
		super("kill", true);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String execute(String command) {
		System.exit(0);
		return getName();
	}
	
	@Override
	public String getHint() {
		return "kill PAMGuard, don't necessarily stop detectors or clean anything up";
	}

}
