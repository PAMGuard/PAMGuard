package PamController.command;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		commandsList.add(new SummaryPeekCommand());
		commandsList.add(new TellModuleCommand());
		commandsList.add(new ExitCommand());
		commandsList.add(new KillCommand());
		commandsList.add(new HelpCommand(this));
		commandsList.add(new GetXMLSettings());
		commandsList.add(new SetXMLSettings());
		
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
	public boolean interpretCommand(String commandString) {
		//System.out.println(String.format("New UDP Command %s", command));
		if (commandString == null) {
			return false;
		}
		String[] commandWords = commandString.split(" "); //splitCommandLine(commandString);
		if (commandWords == null || commandWords.length < 1) {
			return false;
		}
		String first = commandWords[0].toLowerCase();
		
		String command = commandWords[0].toLowerCase();
		// strip of the first two letters if they begin pg ...
		if (command.length() > 2 && command.substring(0,2).equals("pg")) {
			command = command.substring(2); 
		}
		ExtCommand extCommand = findCommand(command);
		if (extCommand == null) {
			sendData("Cmd \"" + commandString + "\" Not Recognised.");
			return false;
		}
		if (extCommand.canExecute() == false) {
			sendData("Cmd \"" + command + "\" Cannot Execute.");
//			sendData("   Cmd return string = " + extCommand.getReturnString());
			return false;
		}
		String output = extCommand.executeCommand(commandString);
		sendData(output);
		
		
		return true;
	}
	
	/**
	 * Slightly nasty split since some module names will be in quotes if they are names with spaces
	 * so the split needs to a) take out multiple words surrounded by "" and then split what's left 
	 * by word. Or split by work and rejoin anything with "" ? 
	 * @param command
	 * @return
	 */
	public static String[] splitCommandLine(String command) {
		// https://stackoverflow.com/questions/7804335/split-string-on-spaces-in-java-except-if-between-quotes-i-e-treat-hello-wor
		if (command == null) {
			return null;
		}
		Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(command);
		List<String> bits = new ArrayList<>();
		while (m.find()) {
			String bit = m.group(1);
			bit = bit.trim();
			bit = bit.replace("\"", "");
			bits.add(bit);
		}
		int n = bits.size();
		String[] sbits = new String[n];
		for (int i = 0; i < bits.size(); i++) {
			sbits[i] = (String) bits.get(i);
		}
		return sbits;
	}
	
	/**
	 * Get the command string left from the given index. This can be used to get 
	 * everything left in the string that follows the command name, module type and module name
	 * fields for example. 
	 * @param command full command string
	 * @param index how many items to skip
	 * @return remainder of the string, warts n all. 
	 */
	public static String getCommandFromIndex(String command, int index) {
		if (command == null) {
			return null;
		}
		int startCharIndex = 0;
		int foundBits = 0;
		Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(command);
		List<String> bits = new ArrayList<>();
		while (m.find() && foundBits++ < index) {
			String bit = m.group(1);
			startCharIndex = command.indexOf(bit, startCharIndex) + bit.length();
			if (startCharIndex >= command.length()) {
				return null;
			}
		}
		String wanted = command.substring(startCharIndex+1).trim();
		return wanted;
	}
	

	/**
	 * Reply to data called from InterpredData
	 * @param dataString
	 * @return true if replay successful
	 */
	abstract public boolean sendData(String dataString); 

}
