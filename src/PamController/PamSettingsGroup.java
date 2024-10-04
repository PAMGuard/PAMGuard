package PamController;

import java.util.ArrayList;

import binaryFileStorage.BinaryStore;
import generalDatabase.DBControlUnit;

//import sun.jdbc.odbc.OdbcDef;


/**
 * Stores a group of PAMGUARD settings read back from the database
 * or some other store (e.g. binary storage). 
 * <p>
 * The group of settings contains three things:
 * 1) A time
 * 2) A list of installed units
 * 3) A list of settings (this is included as part of one of the unitSettings from Pam Controller)
 * @author Doug Gillespie
 * @see DBControlUnit
 * @see BinaryStore
 */
public class PamSettingsGroup implements Comparable<PamSettingsGroup> {

	private long settingsTime;
	
	private ArrayList<PamControlledUnitSettings> unitSettings;
	
	/**
	 * Create a new settings unit, which will contain all the settings
	 * of a configuration at a particular time
	 * @param settingsTime settings time in milliseconds. 
	 */
	public PamSettingsGroup(long settingsTime) {
		this(settingsTime, new ArrayList<PamControlledUnitSettings>());
	}
	
	/**
	 * Constructor to use when a whole list of settings is already available. 
	 * @param settingsTime
	 * @param settings
	 */
	public PamSettingsGroup(long settingsTime, ArrayList<PamControlledUnitSettings> settings) {
		super();
		this.settingsTime = settingsTime;
		this.unitSettings = settings;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(PamSettingsGroup o) {
		if (o.settingsTime - this.settingsTime > 0) return 1;
		if (o.settingsTime - this.settingsTime < 0) return -1;
		return 0;
	}

	/**
	 * 
	 * @return the millisecond timestamp of the unit settings
	 */
	public long getSettingsTime() {
		return settingsTime;
	}
	
	/**
	 * @param settingsTime the settingsTime to set
	 */
	public void setSettingsTime(long settingsTime) {
		this.settingsTime = settingsTime;
	}

	/**
	 * Add settings to the list of different unit settings
	 * @param pamControlledUnitSettings  new settings
	 */
	public void addSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		unitSettings.add(pamControlledUnitSettings);
	}

	/**
	 *
	 * @return the complete array list of different unit settings
	 */
	public ArrayList<PamControlledUnitSettings> getUnitSettings() {
		return unitSettings;
	}

	/**
	 * Get a particular set of settings. 
	 * @param settingsNo Settings number
	 * @return unit settings
	 */
	public PamControlledUnitSettings getUnitSettings(int settingsNo) {
		if (settingsNo >= getNumSettings()) return null;
		return unitSettings.get(settingsNo);
	}
	
	/**
	 * 
	 * @return the number of different unit settings
	 */
	int getNumSettings() {
		return unitSettings.size();
	}
	
	/**
	 * find the unit settings for a module of a given name and type. 
	 * @param unitType unit type
	 * @param unitName unit name
	 * @return unit settings, or null if none found
	 */
	public PamControlledUnitSettings findUnitSettings(String unitType, String unitName) {
		PamControlledUnitSettings pcu;
		for (int i = 0; i < unitSettings.size(); i++) {
			pcu = unitSettings.get(i);
			if (pcu.isSettingsOf(unitType, unitName)) {
				return pcu;
			}
		}
		return null;
	}
	
	/**
	 * Return the list of used modules. These were / are settings returned
	 * by the PamController. 
	 * @return list of used modules. 
	 */
	public ArrayList<UsedModuleInfo> getUsedModuleInfo() {
		// search for the PamController unit settings which is
		// basically a list of unit settings. 
		PamController pamController = PamController.getInstance();
		if (pamController == null) {
			return null;
		}
		PamControlledUnitSettings pcu = findUnitSettings(pamController.getUnitType(), 
				pamController.getUnitName());
		if (pcu != null) {
			return (ArrayList<UsedModuleInfo>) pcu.getSettings();
		}
		return null;
	}
}
