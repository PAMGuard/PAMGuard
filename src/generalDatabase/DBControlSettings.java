package generalDatabase;

import java.util.ArrayList;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettingsGroup;

/**
 * Version of DBControl for loading settings at program startup.
 * @author Doug Gillespie
 * @see DBControl
 *
 */
public class DBControlSettings extends DBControl {

	//	LogSettings logSettings;

	public DBControlSettings(String unitName) {

		super(null, unitName, PamSettingManager.LIST_DATABASESTUFF, false);

		//		logSettings = new LogSettings(this);

	}
	
	public DBControlSettings() {

		this("Settings database");

		//		logSettings = new LogSettings(this);

	}

	public ArrayList<PamControlledUnitSettings> loadSettingsFromDB(PamConnection pamConnection) {

		if (pamConnection == null) {
			return null;
		}
		DBSettingsStore dbSettingsStore = null;
		/**
		 * First try the viewersettings table, then if that's empty 
		 * try the last settings, if that fails, go for the main list 
		 * of all settings. 
		 */
		if (isViewer) {
			dbSettingsStore = getDbProcess().getLogViewerSettings().loadSettings(pamConnection);
		}
		if (dbSettingsStore == null || dbSettingsStore.getNumGroups() == 0) {
			dbSettingsStore = getDbProcess().getLogLastSettings().loadSettings(pamConnection);
		}		
		if (dbSettingsStore == null || dbSettingsStore.getNumGroups() == 0) {
			dbSettingsStore = getDbProcess().getLogSettings().loadSettings(pamConnection);
		}
		if (dbSettingsStore != null) {
			PamSettingsGroup lastGroup = dbSettingsStore.getLastSettingsGroup();
			if (lastGroup != null) {
				return lastGroup.getUnitSettings();
			}
		}
		/*
		 * by default return an empty array list which 
		 * will let it know that we do at least have a database
		 * we want. 
		 */
		return new ArrayList<PamControlledUnitSettings>();

	}

	public ArrayList<PamControlledUnitSettings> loadSettingsFromDB(PamControlledUnitSettings pamControlledUnitSettings) {

		if (pamControlledUnitSettings != null) {
			DBParameters np = (DBParameters) pamControlledUnitSettings.getSettings();
			dbParameters = np.clone();
		}

		DBParameters newParams = DBDialog.showDialog(this, null, dbParameters, null);
		if (newParams != null) {
			dbParameters = newParams.clone();
			selectSystem(dbParameters.getDatabaseSystem(), true);
			return loadSettingsFromDB(2);
		}
		else {
			return null;
		}
	}

	/**
	 * Try to load the serialised settings from the database.
	 * <p> First go for the last settings which are in a separate
	 * table, then if that's empty (which it will in many cases since
	 * the last settings table (Pamguard_Settings_Last) only appeared
	 * in February 2009), get the last entry in the cumulative settings 
	 * stored in the Pamguard_Settings table.  
	 * @param showDatabaseDialog Show a dialog to ask for a database. 0 = never, 1 = if no database open 2 = always. 
	 * @return Array list of PAMGUARD settings. 
	 */
	public ArrayList<PamControlledUnitSettings> loadSettingsFromDB(int showDatabaseDialog) {

		/*
		 * The database should have been opened when the dialog closed
		 * 
		 */
		if (showDatabaseDialog == 1) {
			if (getConnection() == null) {
				showDatabaseDialog = 2;
			}
		}
		if (showDatabaseDialog == 2) {
			// open the database dialog.
			if (!selectDatabase(null, null)) {
				return null;
			}
		}
		else {
			selectSystem(dbParameters.getDatabaseSystem(), true);
		}

		PamConnection con = getConnection();

		if (con == null) return null;

		ArrayList<PamControlledUnitSettings> settings = loadSettingsFromDB(con);
		closeConnection();

		return settings;

	}

	@Override
	public void notifyModelChanged(int changeType) {
		// TODO Auto-generated method stub
		//		super.notifyModelChanged(changeType);
	}
	
	

}
