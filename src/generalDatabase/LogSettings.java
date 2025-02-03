package generalDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import PamController.DeserialisationWarning;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.PamSettingsGroup;
import PamUtils.Ascii6Bit;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;
import generalDatabase.pamCursor.NonScrollablePamCursor;
import generalDatabase.pamCursor.PamCursor;

/**
 * Functions for writing serialised Pamguard Settings into any database as character data
 * Runs at DAQ start, goes through the settings manager list and for each
 * set of settings, it serialises the settings data into a binary array, this
 * is then converted from binary data to 6 bit ascii data (using the character set
 * from the AIS standard, which should be compatible with any DBMS). This character
 * string is then broken up into parts < 255 characters long and written to the 
 * Pamguard_Settings table in the database. 
 * <br>
 * This will allow 1) an audit of exactly how Pamguard was configured at any particular
 * time, 2) when looking at data offline, the database will contain all information 
 * required to reconstruct the Pamguard data model and displays, the database thereby
 * becomes a self contained document of operations, there being no need to keep hold
 * of psf settings files. 
 * 
 * @author Doug Gillespie
 * @see LogModules
 *
 */
public class LogSettings extends DbSpecial {

	private PamSettingsTableDefinition tableDef;

	static private final int DATA_LENGTH = 255;
	static private final int DATA_LINE_LENGTH = DATA_LENGTH;

	private boolean deletePrevious;

	public LogSettings(DBControl dbControl, String tableName, boolean deletePrevious) {

		super(dbControl);

		this.deletePrevious = deletePrevious;

		tableDef = new PamSettingsTableDefinition(tableName, UPDATE_POLICY_OVERWRITE, DATA_LENGTH);

		setTableDefinition(tableDef);
	}

	@Override
	public void pamStart(PamConnection con) {
		saveAllSettings();
	}

	/**
	 * Saves all settings from the standard set of settings that are global to PAMGuard. 
	 * @return
	 */
	boolean saveAllSettings() {

		long now = PamCalendar.getTimeInMillis(); // log all at the same time
		now = System.currentTimeMillis();

		int errors = 0;

		PamSettingManager settingsManager = PamSettingManager.getInstance();
		for (int i = 0; i < settingsManager.getOwners().size(); i++) {
			if (!logSettings(settingsManager.getOwners().get(i), now)) {
				errors ++;
			}
		}
		return errors == 0;
	}

	/**
	 * Save a more specific list of settings. Used by the batck processor to write new configs to 
	 * the batch processing databases. 
	 * @param setingsGroup
	 * @return
	 */
	public boolean saveSettings(PamSettingsGroup settingsGroup) {
		return saveSettings(settingsGroup, settingsGroup.getSettingsTime());
	}
	
	/**
	 * Save a more specific list of settings. Used by the batck processor to write new configs to 
	 * the batch processing databases. 
	 * @param setingsGroup
	 * @return
	 */
	public boolean saveSettings(PamSettingsGroup settingsGroup, long time) {
		ArrayList<PamControlledUnitSettings> setList = settingsGroup.getUnitSettings();
		boolean ok = true;
		for (PamControlledUnitSettings aSet : setList) {
			ok &= logSettings(aSet, time);
		}
		return ok;
	}

	public boolean logSettings(PamSettings pamSettings, long logTime) {
		PamControlledUnitSettings pcuSettings = new PamControlledUnitSettings(pamSettings);
		return logSettings(pcuSettings, logTime);
	}

	public boolean logSettings(PamControlledUnitSettings pamSettings, long logTime) {
		/*
		 * need to serialise the pamSettings data into a byte array, then convert 
		 * that byte array to 6 bit ascii, then chom it up into bits that aren't
		 * too large, then add these to PDU's and send them off to the database. 
		 */

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try {
			ObjectOutputStream out = new ObjectOutputStream(buffer);
			Object settings = pamSettings.getSettings();
			out.writeObject(settings);
			out.close();
		}
		catch (IOException ex) {
			System.out.println("Error saving settings " + pamSettings.getUnitName());
			ex.printStackTrace();
			return false;
		}
		Ascii6Bit charData = new Ascii6Bit(buffer.toByteArray());
		String dataString = charData.getStringData();
		//		System.out.println(charData.getStringData() + " spare bits " + charData.getSpareBits());
		/*
		 * now need to cut that up and turn it into sensible length chunks of ascii
		 * data that will fit into the database. 
		 */  
		try {
			//		if (pamSettings.getUnitName().equals("User TDDisplay")) {
			//			System.out.println("Saving user display data");
			//		}
		}
		catch (NullPointerException e) {
			e.printStackTrace();
		}
		int nLines = 1;
		while (nLines * DATA_LINE_LENGTH < dataString.length()) nLines++;
		int firstChar = 0;
		int lastChar;
		String subString;
		SettingsDataUnit su;
		int iString = 0;
		int spareBits = 0;
		//		while (firstChar < dataString.length()) {
		for (int i = 0; i < nLines; i++) {
			lastChar = Math.min(dataString.length(), firstChar + DATA_LINE_LENGTH);
			if (lastChar == dataString.length()) {
				spareBits = charData.getSpareBits();
			}
			subString = dataString.substring(firstChar, lastChar);
			su = new SettingsDataUnit(logTime, pamSettings.getUnitType(), pamSettings.getUnitName(),
					(int) pamSettings.getVersionNo(), nLines, iString, subString, spareBits);

			this.logData(su);

			firstChar = lastChar;
			iString++;
		}



		return true;
	}
	public DBSettingsStore loadSettings(PamConnection con) {

		//		System.out.println(stmt);

		DBSettingsStore dbSettingsStore = new DBSettingsStore();

		PamCursor pamCursor = new NonScrollablePamCursor(tableDef);

		// work through and read in all records to build up the store

		ResultSet result = pamCursor.openReadOnlyCursor(con, "ORDER BY Id");

		String settingString = null;

		String partString;

		String unitType, unitName;

		int spares;

		int nStrings, iString;

		long version;

		long vLo, vHi;

		PamTableItem tableItem;

		Timestamp timestamp;

		long timeMillis;

		long lastTimeMillis = 0;

		PamSettingsGroup dbSettingsGroup = null;

		PamControlledUnitSettings pamControlledUnitSettings;

		DeserialisationWarning dsWarning = new DeserialisationWarning(getDbControl().getDatabaseName());

		SQLTypes sqlTypes = con.getSqlTypes();

		boolean haveData;
		if (result != null) try {
			haveData = result.next();
			while (haveData) {

				// transfer data back into the tableItems store.
				transferDataFromResult(sqlTypes, result);

				tableItem = tableDef.getTimeStampItem();
				//				timestamp = (Timestamp) tableItem.getTimestampValue();
				timeMillis = SQLTypes.millisFromTimeStamp(tableItem.getValue());

				nStrings = (Integer) tableDef.getGroupTotal().getValue();
				iString = (Integer) tableDef.getGroupIndex().getValue();
				unitType = ((String) tableDef.getType().getValue()).trim();
				unitName = ((String) tableDef.getName().getValue());
				if (unitName != null) {
					unitName = unitName.trim();
				}
				//				this.versionLo = (int) (version & 0xFFFFFFFF);
				//				this.versionHi = (int) (version>>32 & 0xFFFFFFFF);
				//				if (unitName.equals("User TDDisplay")) {
				//					System.out.println("Reading " + unitType);
				//				}
				vLo = (Integer) tableDef.getVersionLo().getValue();
				vHi = (Integer) tableDef.getVersionHi().getValue();
				version = (vHi << 32) | vLo;
				spares = (Integer) tableDef.getSpareBits().getValue();
				partString = ((String) tableDef.getData().getValue()).trim();				

				//				System.out.println(PamCalendar.formatDateTime(PamCalendar.millisFromTimeStamp(timestamp)));
				// have to allow a bit of slack here, since in some early databases
				// not all settings were written at same time - so can be spread over > 1s.
				if (Math.abs(lastTimeMillis - timeMillis) > 2000) {
					// new group
					dbSettingsGroup = new PamSettingsGroup(timeMillis);
					dbSettingsStore.addSettingsGroup(dbSettingsGroup);
				}
				lastTimeMillis = timeMillis;
				if (iString == 0 || settingString == null) {
					settingString = new String(partString);
				}
				else {
					settingString += partString;
				}
				if (iString == nStrings - 1) {
					Ascii6Bit newData = new Ascii6Bit(settingString, spares);
					byte[] byteData = newData.getByteData(); 
					boolean deserialisationError = false;
					//					ByteArrayInputStream inputBuffer = new ByteArrayInputStream(byteData);
					//					Object readObject = null;
					//					try {
					//						ObjectInputStream in = new ObjectInputStream(inputBuffer);
					//						readObject = in.readObject();
					//						in.close();
					//					}
					//					catch (IOException ex) {
					////						System.out.println("Database deserialisation IOException: " + ex.getMessage());
					//						dsWarning.addMissingClass(ex.getMessage());
					//						deserialisationError = true;
					//						//						continue;
					//					}
					//					catch (ClassNotFoundException cx) {
					//						dsWarning.addMissingClass(cx.getMessage());
					////						cx.printStackTrace();
					//					}
					if (!deserialisationError) {
						pamControlledUnitSettings = new PamControlledUnitSettings(unitType, unitName, 
								null, 
								version, byteData);

						//					System.out.println("Read settings from database " + unitType);
						dbSettingsGroup.addSettings(pamControlledUnitSettings);
					}
				}


				haveData = result.next();
			}

			result.close();
		}
		catch (SQLException ex) {

		}
		dsWarning.showWarning(PamController.getMainFrame());

		pamCursor.close();

		return dbSettingsStore;
	}

	class SettingsDataUnit extends PamDataUnit {

		//		private PamTableItem type, name, version, groupTotal, groupIndex, data;
		String type, name, data;
		int versionLo, versionHi, groupTotal, groupIndex, spareBits;
		public SettingsDataUnit(long timeMilliseconds, String type, String name, long version, 
				int groupTotal, int groupIndex, String data, int spareBits) {
			super(timeMilliseconds);
			this.type = type;
			this.name = name;
			this.data = data;
			this.versionLo = (int) (version & 0xFFFFFFFF);
			this.versionHi = (int) (version>>32 & 0xFFFFFFFF);
			this.groupTotal = groupTotal;
			this.groupIndex = groupIndex;
			this.spareBits = spareBits;
		}

	}


	/* not a functional routine yet - just somewhere to put this demo code. 
	 * 
	 */
	// now try to turn that back into a java object
	//		Ascii6Bit newData = new Ascii6Bit(charData.getStringData(), charData.getSpareBits());
	//		byte[] byteData = newData.getByteData(); 
	//		ByteArrayInputStream inputBuffer = new ByteArrayInputStream(byteData);
	//		Object readObject = null;
	//		try {
	//			ObjectInputStream in = new ObjectInputStream(inputBuffer);
	//			readObject = in.readObject();
	//			in.close();
	//		}
	//		catch (IOException ex) {
	//			return false;
	//		}
	//		catch (ClassNotFoundException cx) {
	//			cx.printStackTrace();
	//		}
	//		System.out.println(readObject.toString());
	//	}

	@Override
	public void pamStop(PamConnection con) {
		// TODO Auto-generated method stub

	}

	//	@Override
	//	public PamTableDefinition getTableDefinition() {
	//		return tableDef;
	//	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {

		//		private PamTableItem type, name, versionLo, versionHi, groupTotal, groupIndex, data, spareBits;
		SettingsDataUnit su = (SettingsDataUnit) pamDataUnit;
		tableDef.getType().setValue(su.type);
		tableDef.getName().setValue(su.name);
		tableDef.getVersionLo().setValue(su.versionLo);
		tableDef.getVersionHi().setValue(su.versionHi);
		tableDef.getGroupTotal().setValue(su.groupTotal);
		tableDef.getGroupIndex().setValue(su.groupIndex);
		tableDef.getData().setValue(su.data);
		tableDef.getSpareBits().setValue(su.spareBits);

	}
	@Override
	public final boolean transferDataFromResult(SQLTypes sqlTypes, ResultSet resultSet) {
		return super.transferDataFromResult(sqlTypes, resultSet);
	}

}
