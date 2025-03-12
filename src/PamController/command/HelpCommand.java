package PamController.command;

import java.awt.Component;
import java.util.ArrayList;

import javax.swing.JFrame;

import PamController.PamController;
import PamView.GuiFrameManager;

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

		// check the focus
		GuiFrameManager gui = PamController.getInstance().getGuiFrameManager();
		System.out.println("Check GUI focus " + gui);
		if (gui != null) {
			JFrame frame = gui.getFrame(0);
			Component focussed = frame.getMostRecentFocusOwner();
			
			if (focussed != null) {
				System.out.printf("Focussed is %s: %s\n", focussed.toString(), focussed.getName());
			}
			else {
				System.out.println("No focus available: " + focussed); 
			}
		}

		commandManager.sendData(this, out);
		return getName();
	}


	@Override
	public String getHint() {
		return "Get a list of available commands";
	}

}
