package PamController.command;

import PamController.statusManager.ModuleSummarizer;

public class SummaryPeekCommand extends SummaryCommand {

	public SummaryPeekCommand() {
		super();
		setName("summarypeek");
	}

	@Override
	public String execute(String command) {
		String [] splitCommand = command.split(" ");
		String format = "csv";
		if(splitCommand.length>1) format = splitCommand[1];
		return ModuleSummarizer.getModulesSummary(false,format);
	}

	@Override
	public String getHint() {
		return "Get summary information about each running process, don't clear data";
	}
}
