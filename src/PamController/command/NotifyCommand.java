package PamController.command;

import javax.swing.SwingUtilities;

import PamController.PamController;

public class NotifyCommand extends ExtCommand {

	public NotifyCommand() {
		super("notify", false);
	}



	@Override
	public String getHint() {
		return "Send an integer notification method to PamController";
	}



	@Override
	public String execute(String command) {
		String[] commandWords = CommandManager.splitCommandLine(command);
		if (commandWords.length < 2) {
			return "Notify command requires one parameter";
		}
		String pStr = commandWords[1];
		int pInt = 0;
		try {
			pInt = Integer.valueOf(pStr);
		}
		catch (NumberFormatException e) {
			return String.format("Command \"%s\" is not a valid parameter for the notiry command", pStr);
		}
		final int pInt2 = pInt;
		System.out.println(command);
		PamController.getInstance().notifyModelChanged(pInt2);
//		SwingUtilities.invokeLater(new Runnable() {
//			
//			@Override
//			public void run() {
////				PamController.getInstance().notifyModelChanged(pInt2);
//			}
//		});
		return null;
	}

}
