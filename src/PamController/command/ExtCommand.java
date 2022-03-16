package PamController.command;

import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import PamController.PamControlledUnit;
import PamController.PamController;

public abstract class ExtCommand {

	private String name;
	
	private boolean immediate = false;

	/**
	 * @param name Name of command (must be a single word)
	 * @param immediate immediate execution. If false, then the command will be queued in the AWT
	 * thread for later execution (SwingUtilitiies.invokeLater(...)
	 */
	public ExtCommand(String name, boolean immediate) {
		super();
		this.name = name.toLowerCase();
		this.immediate = immediate;
	}
	
	/**
	 * 
	 * @return true if the command can be executed (may depend on PAMGuard status)
	 */
	public boolean canExecute() {
		return true;
	}
	
	/**
	 * Execute the command. 
	 * <p>Note that if immediate is set false, then 
	 * the command will be executed later in the AWT thread and 
	 * this function will return true. 
	 * @return true if the command executed OK, or if it was sent off
	 * to execute in a later thread. 
	 */
	public final String executeCommand(String commandString) {
		if (immediate) {
			return execute(commandString);
		}
		
		SwingUtilities.invokeLater(new ExecuteLater(commandString));		
		
		return "Command sent";
	}
	
	/**
	 * Class for setting up to execute commands in a later thread. 
	 * @author Doug Gillespie
	 *
	 */
	private class ExecuteLater implements Runnable {

		private String command;

		public ExecuteLater(String command) {
			this.command = command;
		}

		@Override
		public void run() {
			execute(command);
		}
		
	}
	
	/**
	 * Execute the command
	 * @param commandWords 
	 * @return true if command executed correctly. 
	 */
	public abstract String execute(String command);
	
//	/**
//	 * Get the return string from the command. 
//	 * <p>By default this is just the command name, but many 
//	 * commands will need to send back extensive information. 
//	 * @return command output information
//	 */
//	public String getReturnString() {
//		return name;
//	}

	/**
	 * @return the name of the command
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return true if execution is immediate (i.e. not later in the 
	 * swing thread). 
	 */
	public boolean isImmediate() {
		return immediate;
	}

	/**
	 * @param immediate: true if execution should be immediate (i.e. not later in the 
	 * swing thread). 
	 */
	public void setImmediate(boolean immediate) {
		this.immediate = immediate;
	}
	
	/**
	 * Get a hint text for the help command. 
	 * @return hint text. 
	 */
	public String getHint() {
		return null;
	};
	
	/**
	 * Find a set of controlled units, allowing for wild cards or nulls. 
	 * @param unitType unit type, can be null or * for wildcards
	 * @param unitName unit name, can be null or * for wildcards
	 * @return
	 */
	public ArrayList<PamControlledUnit> findControlledUnits(String unitType, String unitName) {
		if (unitType != null && unitType.equals("*")) {
			unitType = null;
		}
		if (unitName != null && unitName.equals("*")) {
			unitName = null;
		}
		PamController pc = PamController.getInstance();
		return pc.findControlledUnits(unitType, unitName);
	}
}
