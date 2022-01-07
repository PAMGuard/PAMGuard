package backupmanager.action;

import backupmanager.stream.BackupStream;

public abstract class ActionMaker {
	
	private String name;
	
	private Class<?> actionClass;
	
	public ActionMaker(String name, Class<?> actionClass) {
		super();
		this.name = name;
		this.actionClass = actionClass;
	}

	public String getName() {
		return name;
	}
	
	public String getToolTip() {
		return getName();
	}
	
	public abstract BackupAction createAction(BackupStream backupStream);

	/**
	 * @return the actionClass
	 */
	public Class<?> getActionClass() {
		return actionClass;
	}
	
}
