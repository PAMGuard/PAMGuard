package pamguard;

/**
 * Hold command line information permanently for the PAMGuard execution
 * so that other modules, including plugins, can check for command line parameters
 * as and when they load, without having to add to the increasingly stupidly 
 * large list of possible command line arguments in the calls to Pamguard.main()
 */
public class CommandLine {

	private String[] args;
	
	private static CommandLine commandLine;

	private CommandLine(String args[]) {
		this.args = args;
	}
	
	/**
	 * Called as the first line of PAMGuard, so that other 
	 * modules can find command line information as and when they need it. 
	 * @param args The argument list passed to PAMGuard from the system.
	 */
	public static void create(String[] args) {
		commandLine = new CommandLine(args);
	}
	
	/**
	 * Get a static reference to the CommandLine singleton object. 
	 * Since the create function was called as the first line of Pamguard.main()
	 * this should always return a valid class and never null
	 * @return The CommandLine class
	 */
	public static CommandLine getCommandLine() {
		return commandLine;
	}
	
	/**
	 * Get the index of a command in the command line
	 * @param command
	 * @return index of a command line argument, or -1 if it's not found. 
	 */
	public int findCommand(String command) {
		if (args == null) {
			return -1;
		}
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals(command)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Does a command exist - basically just checking that 
	 * the findCommand function returns >= 0. 
	 * @param command
	 * @return true if the command exists. 
	 */
	public boolean hasCommand(String command) {
		return findCommand(command) >= 0;
	}
	
	/**
	 * Get the argument that comes after command. This is probably 
	 * the most useful one stop function call for commands that have a 
	 * single parameter. 
	 * @param command
	 * @return command argument (argument after command) or null if command not set. 
	 */
	public String getCommandParameter(String command) {
		int ind = findCommand(command);
		if (ind < 0 || ind+1 >= args.length) {
			return null;
		}
		return getArgument(ind+1);
	}
	
	/**
	 * Get the argument for given index in the list. 
	 * @param argIndex
	 * @return argument at that index, or null
	 */
	public String getArgument(int argIndex) {
		if (args == null || argIndex < 0 || argIndex >= args.length) {
			return null;
		}
		return args[argIndex];
	}

	/**
	 * Get the total number of arguments. 
	 * @return argument count
	 */
	public int getNumArgs() {
		if (args == null) {
			return 0;
		}
		return args.length;
	}
}
