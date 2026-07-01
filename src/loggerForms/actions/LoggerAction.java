package loggerForms.actions;

import loggerForms.LoggerForm;
import loggerForms.controls.LoggerControl;

/**
 * Class describing a logger action. 
 */
abstract public class LoggerAction {

	private ActionOwner actionOwner;
	private String name;
	private String description;

	public LoggerAction(ActionOwner actionOwner, String name, String description) {
		super();
		this.actionOwner = actionOwner;
		this.name = name;
		this.description = description;
	}

	/**
	 * Get the action name
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * A description suitable for use in tooltips, etc. 
	 * @return action description. 
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * A description suitable for use in tooltips, etc. 
	 * @param description action description.
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Perform logger form action
	 * @param loggerForm form that activated the action
	 * @param loggerControl control for the action (e.g. ONOPEN)
	 * @return true if executed, or seems likely to execute. 
	 */
	abstract public boolean runAction(LoggerForm loggerForm, LoggerControl loggerControl);

	@Override
	public String toString() {
		if (description != null) {
			return String.format("Logger action %s (%s)", name, description);
		}
		else {
			return String.format("Logger action %s", name);
		}
	}

	public ActionOwner getActionOwner() {
		return actionOwner;
	}
	
	
}
