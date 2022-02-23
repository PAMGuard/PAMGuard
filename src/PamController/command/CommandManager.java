package PamController.command;

import java.util.ArrayList;

import PamController.PamControlledUnit;
import PamController.PamController;

/**
 * Interpret commands which may have come either from a terminal or from a Network 
 * (e.g. UDP) interface. 
 * @author dg50
 *
 */
public abstract class CommandManager extends PamControlledUnit {

	private PamController pamController;

	private static String unitType = "Command Manager";

	private ArrayList<ExtCommand> commandsList = new ArrayList<ExtCommand>();
	
	public CommandManager(PamController pamController, String unitName) {
		super(unitType, unitName);
		this.pamController = pamController;

		commandsList.add(new StartCommand());
		commandsList.add(new StopCommand());
		commandsList.add(new PingCommand());
		commandsList.add(new StatusCommand());
		commandsList.add(new SummaryCommand());
		commandsList.add(new ExitCommand());
		commandsList.add(new KillCommand());
		commandsList.add(new HelpCommand(this));
		
	}

	protected ArrayList<ExtCommand> getCommandsList() {
		return commandsList;
	}

	public ExtCommand findCommand(String command) {
		for (ExtCommand aCommand:commandsList) {
			if (aCommand.getName().equals(command)) {
				return aCommand;
			}
		}
		return null;
	}

	/**
	 * Interpret and act on a udp command string. 
	 * @param command command string
	 * @return false if the command was to exit
	 * the program (in which case this thread will
	 * exit and close the port). True otherwise. 
	 */
	public boolean interpretCommand(String command) {
		//System.out.println(String.format("New UDP Command %s", command));
		
		command = command.toLowerCase();
		// strip of the first two letters if they begin pg ...
		if (command.substring(0,2).equals("pg")) {
			command = command.substring(2); 
		}
		ExtCommand extCommand = findCommand(command);
		if (extCommand == null) {
			sendData("Cmd \"" + command + "\" Not Recognised.");
			return false;
		}
		if (extCommand.canExecute() == false) {
			sendData("Cmd \"" + command + "\" Cannot Execute.");
			sendData("   Cmd return string = " + extCommand.getReturnString());
			return false;
		}
		extCommand.execute();
		sendData(extCommand.getReturnString());
		
//		
//		if (command.equals("pgstart")) {
//			sendData("PgAck " + "pgstart");
//			pamController.pamStart();
//		}
//		else if (command.equals("pgstop")) {
//			sendData("PgAck " + "pgstop");
//			pamController.pamStop();
//		}
//		else if (command.equals("pgping")) {
//			sendData("PgAck " + "pgping");
//		}
//		else if (command.equals("pgstatus")) {
//			sendData("PgAck Status " + pamController.getPamStatus());
//		}
//		else if (command.equals("pgsetrec")) {
//			sendData("PgAck pgsetrec");
//			
//			//triggerRecording(String name, int seconds);
//		}
//		else if (command.equals("pgexit")) {
//			sendData("Exiting PAMGUARD");
//			System.exit(0);
//			return false;
//		}
//		else{
//			sendData("PgAck " + "Cmd Not Recognised.");
//		}

		
		return true;
	}

	/**
	 * Reply to data called from InterpredData
	 * @param dataString
	 * @return true if replay successful
	 */
	abstract public boolean sendData(String dataString); 

}
