package loggerForms.actions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import loggerForms.LoggerForm;
import loggerForms.controls.LoggerControl;

/**
 * Class to handle actions that can be activated by a logger form in response to 
 * a form control such as ONOPEN, etc. 
 * Modules, including plugins, that can provide actions should register their action 
 * which will then be available in the Logger form design dialogs
 */
public class LoggerActions {

	private static LoggerActions singleInstance;

	private HashMap<String, LoggerAction> actionsMap;

	private LoggerActions() {
		super();
		actionsMap = new HashMap<>();
	}

	public static LoggerActions getInstance() {
		if (singleInstance == null) {
			singleInstance = new LoggerActions();
		}
		return singleInstance;
	}

	/**
	 * Find and run the action. 
	 * @param actionName Name of action to run
	 * @param loggerForm form calling the action
	 * @param loggerControl control running the action (e.g. an ONOPEN control)
	 * @return
	 * @throws ActionException
	 */
	public boolean runAction(String actionName, LoggerForm loggerForm, LoggerControl loggerControl) throws ActionException {
		LoggerAction action = actionsMap.get(actionName);
		if (action == null) {
			throw(new ActionException("Unable to find logger action " + actionName));
		}
		boolean ok = false;
		try {
			ok = action.runAction(loggerForm, loggerControl);
		}
		catch (Exception e) {
			throw new ActionException(e);
		}
		return ok;
	}
	
	/**
	 * Get the set of possible actions. 
	 * @return possible actions set
	 */
	public Set<String> getActionKeys() {
		return actionsMap.keySet();
	}

	/**
	 * Register an action
	 * @param loggerAction
	 */
	public void registerAction(LoggerAction loggerAction) {
		// check it doesn't already exist, and throw a warning
		LoggerAction existing = actionsMap.get(loggerAction.getName());
		if (existing != null) {
			System.out.println("Logger action already exists and is being replaced: "  + existing.toString());
		}
		actionsMap.put(loggerAction.getName(), loggerAction);
	}

	/**
	 * Remove an action
	 * @param loggerAction
	 * @return true if it existed, false if it wasn't even there. 
	 */
	public boolean removeAction(LoggerAction loggerAction) {
		return actionsMap.remove(loggerAction.getName()) != null;
	}

	/**
	 * Remove all actions belonging to a given owner. 
	 * @param owner
	 * @return number of actions removes. 
	 */
	public int removeAllOwnersActions(ActionOwner owner) {
		Collection<Entry<String, LoggerAction>> ents = actionsMap.entrySet();
		int nr = 0;
		Iterator<Entry<String, LoggerAction>> it = ents.iterator();
		while (it.hasNext()) {
			Entry<String, LoggerAction> act = it.next();
			if (act.getValue().getActionOwner() == owner) {
				it.remove();
				nr ++;
			}
		}
		return nr;
	}
}
