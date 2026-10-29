package generalDatabase.plugins;

import PamModel.CommonPluginInterface;
import generalDatabase.DBControl;
import generalDatabase.DBSystem;

public interface DBPluginInterface extends CommonPluginInterface {

	/**
	 * Generate a database system. 
	 * @param dbControl
	 * @param settingsStore
	 * @return database system
	 */
	public DBSystem getDatabaseSystem(DBControl dbControl, int settingsStore);
	
}
