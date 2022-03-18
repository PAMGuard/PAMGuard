package PamController.command;

public class SummaryPeekCommand extends SummaryCommand {

	public SummaryPeekCommand() {
		super();
		setName("summarypeek");
	}

	@Override
	public String execute(String command) {
		return getModulesSummary(false);
	}

	@Override
	public String getHint() {
		return "Get summary information about each running process, don't clear data";
	}
}
