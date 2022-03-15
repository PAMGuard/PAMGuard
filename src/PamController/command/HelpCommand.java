package PamController.command;

import java.util.ArrayList;

public class HelpCommand extends ExtCommand {

	private CommandManager commandManager;

	public HelpCommand(CommandManager commandManager) {
		super("help", true);
		this.commandManager = commandManager;
	}

	@Override
	public String execute(String commandString) {
		ArrayList<ExtCommand> commands = commandManager.getCommandsList();
		String out = "Available commands are:\n";
		for (ExtCommand command : commands) {
			out += command.getName();
			String hint = command.getHint();
			if (hint != null) {
				out += String.format(": %s\n", hint);
			}
			else {
				out += "\n";
			}
		}
		commandManager.sendData(out);
		return getName();
	}


	@Override
	public String getHint() {
		return "Get a list of available commands";
	}

}
