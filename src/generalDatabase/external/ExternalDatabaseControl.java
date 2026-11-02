package generalDatabase.external;

import generalDatabase.DBControlSettings;

public class ExternalDatabaseControl extends DBControlSettings {

	public ExternalDatabaseControl(String unitName) {
//		super(unitName, 0, false);
		super(unitName);
		/*
		 *  I don't think this is needed anymore. In previous releases, ucanaccess was
		 *  not included in the main database list, but was made available here so that
		 *  users can import from older Access databases. Now that UcanAccess is moved to 
		 *  being a plugin it will appear in the external list if people have installed
		 *  the plugin, otherwise, it won't be available. 
		 */
		
//		addDatabaseSystem(new UCanAccessSystem(this, 0));
	}

	@Override
	public String getUnitType() {
		return "External Database";
	}

}
