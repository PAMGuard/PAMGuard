package generalDatabase.external;

import generalDatabase.DBControlSettings;
import generalDatabase.ucanAccess.UCanAccessSystem;

public class ExternalDatabaseControl extends DBControlSettings {

	public ExternalDatabaseControl(String unitName) {
//		super(unitName, 0, false);
		super(unitName);
		addDatabaseSystem(new UCanAccessSystem(this, 0));
	}

	@Override
	public String getUnitType() {
		return "External Database";
	}

}
