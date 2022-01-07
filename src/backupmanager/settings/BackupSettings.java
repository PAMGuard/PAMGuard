package backupmanager.settings;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

public abstract class BackupSettings implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	private ArrayList<ActionSettings> actionSettings = new ArrayList<ActionSettings>();

	/**
	 * 
	 * @return a list of action settings. 
	 */
	public List<ActionSettings> getActionSettings() {
		return actionSettings;
	}
	
	/**
	 * Add some action settings
	 * @param actionSettings
	 */
	public void addActionSettings(ActionSettings actionSettings) {
		this.actionSettings.add(actionSettings);
	}
	
	/**
	 * Remove some action settings
	 * @param actionSettings
	 * @return true if it was in the list, false if it didn't exist. 
	 */
	public boolean removeActionSettings(ActionSettings actionSettings) {
		return this.actionSettings.remove(actionSettings);
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
