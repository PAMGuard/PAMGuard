package generalDatabase;

import java.util.ArrayList;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettingsGroup;
import PamController.UsedModuleInfo;

/**
 * Load all of the settings from a database and store in a single class.
 * <p>
 * Each database will possibly hold lots of different settings, the only way
 * of grouping them together is by time. 
 * <p>
 * Settings are stored in a somewhat dubious way in that any part of Pamguard,
 * be it a PamControlledUnit or not can store settings, so some of the
 * settings read back are associated with modules, others are not. 
 * <p>
 * The actual list of modules is an ArrayList<UsedModuleInfo> with unitNType and unitName 
 * set to PamController and Pam Controller respectively. 
 * @author Doug
 * @see UsedModuleInfo
 * @see PamSettingManager
 * @see PamControlledUnitSettings
 *
 */
public class DBSettingsStore {

	private ArrayList<PamSettingsGroup> settingsGroups;
	
	public DBSettingsStore() {
		super();
		settingsGroups = new ArrayList<PamSettingsGroup>();
	}

	public ArrayList<PamSettingsGroup> getSettingsGroups() {
		return settingsGroups;
	}

	public PamSettingsGroup getSettingsGroup(int groupNumber) {
		return settingsGroups.get(groupNumber);
	}

	public void addSettingsGroup(PamSettingsGroup dbSettingsGroup) {
		settingsGroups.add(dbSettingsGroup);
	}
	
	public int getNumGroups() {
		return settingsGroups.size();
	}

	public PamSettingsGroup getLastSettingsGroup() {
		if (settingsGroups.size() < 1) {
			return null;
		}
		return settingsGroups.get(getNumGroups() - 1);
	}

	
}
