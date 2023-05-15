package PamController.command;

import networkTransfer.send.NetworkSender;
import pamguard.GlobalArguments;

public class BatchCommand extends ExtCommand {
	
	public static final String commandId = "batchcommand";
	private CommandManager commandManager;

	public BatchCommand(CommandManager commandManager) {
		super(commandId, true);
		this.commandManager = commandManager;
	}

	@Override
	public String execute(String command) {
		/**
		 * this should have to identifiers. If they match the identifiers of
		 * this pamguard instance, then the command part is passed back to the manager
		 * otherwise it is ignored. 
		 */
		if (command == null) {
			return null;
		}
		String[] bits = command.split(" ");
		if (bits.length < 4) {
			return null;
		}
		int id1 = 0;
		int id2 = 0;
		int expId1 = 0, expId2 = 0;
		try {
			id1 = Integer.valueOf(bits[1]);
			id2 = Integer.valueOf(bits[2]);
		}
		catch (NumberFormatException e) {
			System.out.println("Invalid BatchCommand: " + command);
			return null;
		}
		String nid1 = GlobalArguments.getParam(NetworkSender.ID1);
		String nid2 = GlobalArguments.getParam(NetworkSender.ID2);
		if (bits[1].trim().equals(nid1) == false) {
			return null;
		}
		
		// now trim the string to the end of the third comma and send on the rest
		int comPos = -1;
		for (int i = 0; i < 3; i++) {
			comPos = command.indexOf(" ", comPos+1);
			if (comPos < 0) { 
				return null;
			}
		}
		String trueCommand = command.substring(comPos);
		trueCommand = trueCommand.trim();
//		System.out.printf(">>>>>>>>>>>>>>>>>>>  Batchcommand execute \"%s\" in command manager %s\n", trueCommand, commandManager.getClass().getName());
		if (commandManager.interpretCommand(trueCommand)) {
//			return commandManager.get
		}
		return null;
	}

}
