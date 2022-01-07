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
	public boolean execute() {
		System.exit(0);
		return true;
	}

}
