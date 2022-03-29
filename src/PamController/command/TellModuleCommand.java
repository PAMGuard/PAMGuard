package PamController.command;

import java.util.ArrayList;

import PamController.PamControlledUnit;
import PamController.PamSettingManager;

public class TellModuleCommand extends ExtCommand {

	public TellModuleCommand() {
		super("tellmodule", true);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String execute(String command) {		
		String[] commandWords = CommandManager.splitCommandLine(command);
		if (commandWords.length < 3) {
			return "Unspecified modules for tellmodule. Command must be followed by module name and type";
		}
		PamSettingManager settingsManager = PamSettingManager.getInstance();
		String unitType = commandWords[1];
		String unitName = commandWords[2];
		ArrayList<PamControlledUnit> pamControlledUnits = findControlledUnits(unitType, unitName);
		if (pamControlledUnits.size() == 0) {
			return "tellmodule: no modules found for command " + command;
		}
		String commandPart = CommandManager.getCommandFromIndex(command, 3);
		if (commandPart == null) {
			return "tellmodule: No command. Must have a module type, module name and the thing to send to modules";
		}
		String retStr = "";
		for (int i = 0; i < pamControlledUnits.size(); i++) {
			if (i > 0) {
				retStr += ";";
			}
			retStr += pamControlledUnits.get(i).tellModule(commandPart);
		}
		return retStr;
	}

	@Override
	public String getHint() {
		return "Send a command to a module.";
	}
}
