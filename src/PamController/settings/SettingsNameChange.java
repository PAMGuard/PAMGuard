package PamController.settings;

import PamController.PamSettings;

public class SettingsNameChange {
	
	private String oldName, newName, oldType, newType;
	private Class moduleClass;

	public SettingsNameChange(Class moduleClass, String oldName, String newName, String oldType, String newType) {
		super();
		this.moduleClass = moduleClass;
		this.oldName = oldName;
		this.newName = newName;
		this.oldType = oldType;
		this.newType = newType;
		
	}

	/**
	 * 
	 * @param pamSettings
	 * @return true if the pamSettings seem compatible with this 
	 * Comparison is mostly just done on module type with the unit names being null.  
	 */
	public boolean seemsSame(PamSettings pamSettings) {
		if (moduleClass != null && moduleClass != pamSettings.getClass()) {
			return false;
		}
		if (newType != null && !newType.equals(pamSettings.getUnitType())) {
			return false;
		}
		
		return true;
	}

	/**
	 * @return the oldName
	 */
	public String getOldName() {
		return oldName;
	}

	/**
	 * @return the newName
	 */
	public String getNewName() {
		return newName;
	}

	/**
	 * @return the oldType
	 */
	public String getOldType() {
		return oldType;
	}

	/**
	 * @return the newType
	 */
	public String getNewType() {
		return newType;
	}

	/**
	 * @return the moduleClass
	 */
	public Class getModuleClass() {
		return moduleClass;
	}
	
}
