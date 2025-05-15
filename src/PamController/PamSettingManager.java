/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation
 * of marine mammals (cetaceans).
 *
 * Copyright (C) 2006
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package PamController;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import PamController.settings.SettingsNameChange;
import PamController.settings.SettingsNameChanger;
import PamUtils.PamCalendar;
import PamUtils.PamFileChooser;
import PamUtils.PamFileFilter;
import PamView.dialog.warn.WarnOnce;
import generalDatabase.DBControl;
import generalDatabase.DBControlSettings;
import javafx.scene.control.Alert.AlertType;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;
import pamViewFX.fxSettingsPanes.SettingsFileDialogFX;
import pamguard.GlobalArguments;
import pamguard.LogFileUtils;
import pamguard.Pamguard;

//XMLSettings
//import org.jdom.Document;
//import org.jdom.Element;
//import org.jdom.JDOMException;
//import org.jdom.input.SAXBuilder;
//import org.jdom.output.XMLOutputter;
//import org.w3c.dom.Node;
//import com.thoughtworks.xstream.XStream;

//import javax.xml.transform.OutputKeys;
//import javax.xml.transform.Transformer;
//import javax.xml.transform.TransformerException;
//import javax.xml.transform.TransformerFactory;
//import javax.xml.transform.dom.DOMSource;
//import javax.xml.transform.stream.StreamResult;


//import sun.jdbc.odbc.OdbcDef;
import tipOfTheDay.TipOfTheDayManager;
//import javax.swing.filechooser.FileFilter;
//import javax.swing.filechooser.FileNameExtensionFilter;

//import PamUtils.PamFileFilter;


/**
 * @author Doug Gillespie
 *
 * Singleton class for managing Pam settings - where and how they are stored in
 * a persistent way between runs.
 *
 * Any class that wants is settings saved should register with the
 * PamSettingsManager.
 * <p>
 * When the GUI closes, SaveSettings is called, SaveSettings goes through the
 * list of registered objects and asks each one to give it a reference to an
 * Object containing the settings (this MUST implement serialisable). This can
 * be the object itself, but will more likely be a reference to another object
 * just containing settings parameters. The class implementing PamSettings must
 * also provide functions getUnitType, getUnitName and getSettingsVersion. These
 * four pieces of information are then bundled into a PamControlledUnitSettings
 * which is added to an array list which is then stored in a serialised file.
 * <p>
 * When PAMGUARD starts, after all detectors have been created, the serialised
 * file is reopened. Each PamControlledUnitSettings is taken in turn and
 * compared with the list of registered objects to find one with the same name,
 * type and settings version. Once one is found, it is given the reference to
 * the settings data which t is responsible for casting into whatever class it
 * requires.
 *
 *
 */
public class PamSettingManager {

	static public final int LOAD_SETTINGS_OK = 0;
	static public final int LOAD_SETTINGS_CANCEL = 1;
	static public final int LOAD_SETTINGS_NEW = 2; // new settings

	private static PamSettingManager pamSettingManager;

	/**
	 * List of modules that have / want PAMGUARD Settings
	 * which get stored in the psf file and / or the database store.
	 */
//	private ArrayList<PamSettings> owners;

	/**
	 * List of modules that specifically use settings from the database
	 * storage.
	 */
	private ArrayList<PamSettings> databaseOwners;

	/**
	 * List of modules that are stored globally on the PC
	 * with a single common psf type file.
	 */
	private ArrayList<PamSettings> globalOwners;

	private ArrayList<PamControlledUnitSettings> globalSettings;

	/**
	 * List of settings used by 'normal' modules.
	 */
	private ArrayList<PamControlledUnitSettings> initialSettingsList;

	/**
	 * List of settings used specifically by databases.
	 * This list never get's stored anywhere, but is just held
	 * in memory so that the database identified at startup in
	 * viewer and mixed modes gets reloaded later on .
	 */
	private ArrayList<PamControlledUnitSettings> databaseSettingsList;

	//	static public final String oldFileEnd = "PamSettingsFiles.ser";

	static public final String fileEnd = "psf";
	static public final String fileEndx = "psfx";
	static public final String fileEndXML = "psfxml";
	
	private static boolean saveAsPSFX = true;
	
	/**
	 * A secondary configuration to use when loading configs into 
	 * batch mode for viewing and extracting offline tasks. This is a 
	 * real bodge and bad style, but can't do much about it at this stage. 
	 * USe very sparingly and make sure it's set null once the external batch
	 * configuration is loaded. 
	 */
	private PamConfiguration secondaryConfiguration;

	static public String getCurrentSettingsFileEnd() {
		if (saveAsPSFX) {
			return fileEndx;
		}
		else {
			return fileEnd;
		}
	}

	/**
	 * Name of the file that contains a list of recent psf files.
	 */
	transient private final String settingsListFileName = "PamSettingsFilesUID";

	/**
	 * End of the name - will be joine to the name, but may be changed a bit for funny versions
	 */
	transient private final String settingsListFileEnd = ".psg";

	transient private final String gloablListfileName = "PamguardGlobals";

	/**
	 * Name of a list of recent database informations (probably just the last one)
	 */
	transient private final String databaseListFile = "recentDatabasesUID.psg";

	/**
	 * Identifier for modules that go in the 'normal' list
	 * (everything apart from database modules)
	 */
	public static final int LIST_UNITS = 0x1;

	/**
	 * Identifier for modules which are part of the database system.
	 */
	public static final int LIST_DATABASESTUFF = 0x2;

	/**
	 * Stuff which is global to the computer system (at the user level).
	 * Invented for colour settings, might extend to other things too.
	 */
	public static final int LIST_SYSTEMGLOBAL = 0x4;

	/**
	 * Save settings to a psf file
	 */
	static public final int SAVE_PSF = 0x1;
	
	/**
	 * Save settings to database tables (if available).
	 */
	static public final int SAVE_DATABASE = 0x2;

	/**
	 * running in remote mode, default normal
	 */
	static public boolean RUN_REMOTE = false;
	static public String  remote_psf = null;
	static public String  external_wav = null;

	private boolean loadingLocalSettings;

	//	File currentFile; // always use firstfile from the settingsFileData

	private boolean[] settingsUsed;
	//	private boolean userNotifiedAbsentSettingsFile = false;
	//	private boolean userNotifiedAbsentDefaultSettingsFile = false;

	private boolean programStart = true;

	private SettingsFileData settingsFileData;


	private PamSettingManager() {
//		owners = new ArrayList<PamSettings>();
		databaseOwners = new ArrayList<PamSettings>();
		globalOwners = new ArrayList<PamSettings>();
		//		setCurrentFile(new File(defaultFile));
	}

	public static PamSettingManager getInstance() {
		if (pamSettingManager == null) {
			pamSettingManager = new PamSettingManager();
		}
		return pamSettingManager;
	}

	/**
	 * Clear all settings from the manager
	 */
	public void reset() {
		initialSettingsList = null;
		databaseSettingsList = null;
//		owners = new ArrayList<PamSettings>();
		getOwners().clear();
		databaseOwners = new ArrayList<PamSettings>();

	}

	/*
	 * Flag to indicate that initialisation of PAMGUARD has completed.
	 */
	private boolean initializationComplete = false;

	/**
	 * Called everytime anything in the model changes.
	 * @param changeType type of change
	 */
	public void notifyModelChanged(int changeType) {
		if (changeType == PamControllerInterface.INITIALIZATION_COMPLETE) {
			initializationComplete = true;
		}
	}

	/**
	 * Register a PAMGAURD module that wants to store settings in a
	 * serialised file (.psf file) and / or have those settings stored
	 * in the database settings table.
	 * <p>Normally, all modules will
	 * call this for at least one set of settings. Often the PamSettings
	 * is implemented by the class that extends PamControlledunit, but
	 * it's also possible to have multiple sub modules, processes or displays
	 * implement PamSettings so that different settings for different bits of
	 * a PamControlledUnit are stored separately.
	 * @see PamSettings
	 * @see PamControlledUnit
	 * @param pamUnit Reference to a PamSettings module
	 * @return True if settings correctly restored. This is either the return of the restoreSettings() function
	 * in the calling pamUnit, or will be false if there was a ClassCastException in the call to restoreSettings()
	 */
	public boolean registerSettings(PamSettings pamUnit) {
		return registerSettings(pamUnit, LIST_UNITS);
	}

	/**
	 * Deregister a settings. 
	 * @param pamUnit
	 * @return
	 */
	public boolean unRegisterSettings(PamSettings pamUnit) {
		boolean found = getOwners().remove(pamUnit);
		found |= databaseOwners.remove(pamUnit);
		found |= globalOwners.remove(pamUnit);
		return found;
	}
	/**
	 * Register modules that have settings information that
	 * should be stored in serialised form in
	 * psf files and database Pamguard_Settings tables.
	 * @param pamUnit Unit containing the settings
	 * @param whichLists which lists to store the settings in. <p>
	 * N.B. These are internal lists and not the external storage. Basically
	 * any database modules connected with settings should to in LIST_DATABASESTUFF
	 * everything else (including the normal database) should go to LISTS_UNITS
	 * @return true if settings registered sucessfully.
	 */
	public boolean registerSettings(PamSettings pamUnit, int whichLists) {

		if ((whichLists & LIST_UNITS) != 0) {
			getOwners().add(pamUnit);
		}
		if ((whichLists & LIST_DATABASESTUFF) != 0) {
			databaseOwners.add(pamUnit);
		}
		if ((whichLists & LIST_SYSTEMGLOBAL) != 0) {
			globalOwners.add(pamUnit);
		}

		PamControlledUnitSettings settings = findSettings(pamUnit, whichLists);
		if (settings != null && settings.getSettings() != null) {
			try {
				return pamUnit.restoreSettings(settings);
			}
			catch (ClassCastException e) {
				System.out.printf("Error restoring settings to module %s,%s: %s\n", pamUnit.getUnitType(),
						pamUnit.getUnitName(), e.getMessage());
			}
		}
		return false;
	}

	/**
	 * Find settings for a particular user in one or more lists.
	 * @param user PamSettings user.
	 * @param whichLists lists to search
	 * @return settings object.
	 */
	private PamControlledUnitSettings findSettings(PamSettings user, int whichLists) {
		PamControlledUnitSettings settings = null;
		if ((whichLists & LIST_SYSTEMGLOBAL) != 0) {
			if (globalSettings != null) {
				settings = findSettings(globalSettings, null, user);
				if (settings != null) {
					return settings;
				}
			}
		}
		if ((whichLists & LIST_UNITS) != 0) {
			if (initialSettingsList == null) return null;
			if (settingsUsed == null || settingsUsed.length != initialSettingsList.size()) {
				settingsUsed = new boolean[initialSettingsList.size()];
			}
			settings = findSettings(initialSettingsList, settingsUsed, user);
		}

		if (settings == null && (whichLists & LIST_DATABASESTUFF) != 0) {
			settings = findGeneralSettings(user.getUnitType());
		}

		return settings;
	}
	/**
	 * Find settings in a list of settings, ignoring settings which have
	 * already been used by a module.
	 * @param settingsList settings list
	 * @param usedSettings list of settings that have already been used.
	 * @param user module that uses the settings.
	 * @return Settings object.
	 */
	private PamControlledUnitSettings findSettings(ArrayList<PamControlledUnitSettings> settingsList,
			boolean[] usedSettings,	PamSettings user) {
		if (settingsList == null) return null;
		// go through the list and see if any match this module. Avoid repeats.
//		String unitName = user.getUnitName();
//		String unitType = user.getUnitType();
		for (int i = 0; i < settingsList.size(); i++) {
			if (usedSettings != null && usedSettings[i]) continue;
			if (isSettingsUnit(user, settingsList.get(i))) {
				if (usedSettings != null) {
					usedSettings[i] = true;
				}
				return settingsList.get(i);
			}
		}
		
		/*
		 * To improve complex module loading where settings may be saved by multiple sub-modules, in
		 * July 2015 many modules which had fixed settings had their settings names and types changed !
		 * Therefore these modules won't have found their settings on the first go, so need to also check
		 * against the alternate names defined for each class.
		 * It should be possible to work out from the settingsUser.Class what changes may have been made !
		 */
		SettingsNameChange otherName = SettingsNameChanger.getInstance().findNameChange(user);
		if (otherName == null) {
			return null;
		}
		for (int i = 0; i < settingsList.size(); i++) {
			if (usedSettings != null && usedSettings[i]) continue;
			if (isSettingsUnit(otherName, settingsList.get(i))) {
				if (usedSettings != null) {
					usedSettings[i] = true;
				}
				return settingsList.get(i);
			}
		}


		return null;
	}

	/**
	 * Searches a list of settings for settings with a
	 * specific type.
	 * @param unitType
	 * @return PamControlledUnitSettings or null if none found
	 * @see PamControlledUnitSettings
	 */
	public PamControlledUnitSettings findGeneralSettings(String unitType) {
		if (databaseSettingsList == null) {
			return null;
		}
		for (int i = 0; i < databaseSettingsList.size(); i++) {
			if (databaseSettingsList.get(i).getUnitType().equalsIgnoreCase(unitType)) {
				return databaseSettingsList.get(i);
			}
		}
		return null;
	}

	/**
	 * Find settings in a list of settings by name and by type.
	 * @param settingsList settings list to search
	 * @param unitType unit name
	 * @param unitName unit type
	 * @return settings object
	 */
	public PamControlledUnitSettings findSettings(ArrayList<PamControlledUnitSettings> settingsList,
			String unitType, String unitName) {

		if (settingsList == null) {
			return null;
		}
		PamControlledUnitSettings aSet;


		try {
			for (int i = 0; i < settingsList.size(); i++) {
				aSet = settingsList.get(i);
				if (aSet.getUnitType().equals(unitType) & (unitName == null | aSet.getUnitName().equals(unitName))) {
					return aSet;
				}
			}
		}
		catch (NullPointerException e) {
			System.out.printf("Error finding settings for %s : %s\n", unitType, unitName);
		}

		return null;
	}

	/**
	 * Find a settings owner for a type, name and class.
	 * @param unitType unit Type
	 * @param unitName unit Name
	 * @param unitClass unit Class
	 * @return Settings owner or null.
	 */
	public PamSettings findSettingsOwner(String unitType, String unitName, String unitClassName) {
		ArrayList<PamSettings> owners = getOwners();
		for (PamSettings owner:owners) {
			if (owner.getClass() != null && unitClassName != null) {
				if (!owner.getClass().getName().equals(unitClassName)) {
					continue;
				}
			}
			if (owner.getUnitName().equals(unitName) &&
					owner.getUnitType().equals(unitType)) {
				return owner;
			}
		}
		return null;
	}

	/**
	 * Call just before PAMGUARD exits to save the settings
	 * either to psf and / or database tables.
	 * @return true if settings saved successfully.
	 */
	public boolean saveFinalSettings() {
		int runMode = PamController.getInstance().getRunMode();
		switch (runMode) {
		case PamController.RUN_NORMAL:
		case PamController.RUN_NETWORKRECEIVER:
			return saveSettings(SAVE_PSF | SAVE_DATABASE);
		case PamController.RUN_PAMVIEW:
			if (GlobalArguments.getParam(GlobalArguments.BATCHVIEW) != null) {
				return saveSettings(SAVE_PSF | SAVE_DATABASE);
			}
			else {
				return saveSettings(SAVE_DATABASE);
			}
		case PamController.RUN_MIXEDMODE:
			return saveSettings(SAVE_DATABASE);
		case PamController.RUN_NOTHING:
			return saveSettings(SAVE_PSF);
		}
		return false;
	}

	/**
	 * Save settings to a psf file and / or the database tables.
	 * @param saveWhere
	 * @return true if sucessful
	 */
	public boolean saveSettings(int saveWhere) {

		if (!initializationComplete) {
			// if PAMGAURD hasn't finished loading, then don't save the settings
			// or the file will get wrecked (bug tracker 2269579)			
			String msg = "There was an error loading settings from this configuration, so the configuration"
					+ " may be incomplete. <p>Do you want to save anyway ? <p>"
					+ " If you have added new modules, the answer is probably \"Yes\"";
			int ans = WarnOnce.showWarning("Confuguration file warning", msg, WarnOnce.YES_NO_OPTION);
			if (ans == WarnOnce.CANCEL_OPTION) {
				System.out.println("Settings have not yet loaded. Don't save file");
				return false;
			}
		}

		saveGlobalSettings();
		//		saveSettingToDatabase();

		if ((saveWhere & SAVE_PSF) != 0) {
			boolean success = saveSettingsToFile();
			if (!success) {
				String title = "Error saving settings to psf file";
				String msg = "There was an error while trying to save the current settings to the psf file <p>" +
						getSettingsFileName() + "<p>" +
						"This could occur if the psf file location is in a read-only folder, or the filename is " +
						"invalid.  Please check and try again.";
				String help = null;
				int ans = WarnOnce.showWarning(PamController.getMainFrame(), title, msg, WarnOnce.WARNING_MESSAGE, help);
			}
		}
		/**
		 * Always save the settings file data (list of recent files) since it includes
		 * static information such as whether to show day tips.
		 */
		saveSettingsFileData();

		if ((saveWhere & SAVE_DATABASE) != 0) {
			saveSettingsToDatabase();
			saveDatabaseFileData();
		}

		return true;

	}

	public boolean saveSettingsToFile() {
		return saveSettingsToFile(getSettingsFileName());
	}

	/**
	 * Save configuration settings to the default (most recently used) psf file.
	 * @return true if successful.
	 */
	public boolean saveSettingsToFile(String fileName) {
		if (saveAsPSFX) {
			// check the file end is psfx.
			if (fileName.endsWith("psf")) {
				fileName += "x";
				settingsFileData.setFirstFile(new File(fileName));
				String warnTxt = "<html>To avoid backwards compatibility issues, settings are now saved in psfx files." +
				"<p>These are not backwards compatible with earlier versions of PAMGuard." +
						"<p><p>If an existing psf file was loaded into this version of PAMGuard it will not have been changed" +
				"and you will find both psf and psfx files in your configurations folder";
				WarnOnce.showWarning(PamController.getMainFrame(), "PAMGuard configuration settings", warnTxt, WarnOnce.OK_OPTION);
			}
			return PSFXReadWriter.getInstance().writePSFX(fileName);
		}
		else {
			return saveSettingsToPSFFile(fileName);
		}
	}

	private boolean saveGlobalSettings() {
		File setFile = getGlobalSettingsFile();
		ObjectOutputStream outStream = openOutputFile(setFile.getAbsolutePath());

		for (PamSettings gs:globalOwners) {
			PamControlledUnitSettings pus = new PamControlledUnitSettings(gs.getUnitType(),
					gs.getUnitName(), gs.getClass().getName(), gs.getSettingsVersion(), gs.getSettingsReference());
			try {
				outStream.writeObject(pus);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	private boolean loadGlobalSettings() {
		globalSettings = new ArrayList<>();
		File setFile = getGlobalSettingsFile();
		ObjectInputStream  ois = null;
		boolean ok= true;
		try {
			ois = new ObjectInputStream(new FileInputStream(setFile));
		}
		catch (IOException e) {
			ok = false;
		}
		while (ok) {
			try {
				Object o = ois.readObject();
				PamControlledUnitSettings pus = (PamControlledUnitSettings) o;
				globalSettings.add(pus);
			}
			catch (EOFException eof){
				break;
			}
			catch (IOException e) {
				ok = false;
			} catch (ClassNotFoundException e) {
				System.out.println("Global settings error " + e.getMessage());
				ok = false;
			}
		}
		try {
			if (ois != null) {
				ois.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ok;
	}

	/**
	 * Save configuration settings to the default (most recently used) psf file.
	 * @return true if successful.
	 */
	public boolean saveSettingsToPSFFile(String fileName) {

		/*
		 * Create a new list of settings in case they have changed
		 */

		ArrayList<PamControlledUnitSettings> pamSettingsList;
		pamSettingsList = new ArrayList<PamControlledUnitSettings>();
		ArrayList<PamSettings> owners = getOwners();
		for (int i = 0; i < getOwners().size(); i++) {
			pamSettingsList
			.add(new PamControlledUnitSettings(owners.get(i)
					.getUnitType(), owners.get(i).getUnitName(),
					owners.get(i).getClass().getName(),
					owners.get(i).getSettingsVersion(),
					owners.get(i).getSettingsReference()));
		}
		int nUsed = pamSettingsList.size();
		/*
		 * Then go through the initialSettings, that were read in and any that were not used
		 * add to the current settings output so that they may be used next time around incase
		 * a module reappears that was temporarily not used.
		 */
		boolean firstDuplicateFound = true;
		boolean purgeDuplicates = false;
		if (initialSettingsList != null) {
			for (int i = 0; i < initialSettingsList.size(); i++) {
				if (settingsUsed != null && settingsUsed.length > i && settingsUsed[i]) continue;

				// if this is a duplicate of something already in the list, warn the user and find out if they want to remove it
				if (thisIsADuplicate(pamSettingsList, initialSettingsList.get(i))) {
					if (firstDuplicateFound) {
						firstDuplicateFound = false;
						String msg = "<html>Duplicate settings have been found in the psf file.  Please select whether to keep them in the psf, or to" +
						" delete them.  Duplicate settings will not cause Pamguard to crash, however they will enlarge the psf file over time.</html>";
						int ans;
						if (PamGUIManager.getGUIType()==PamGUIManager.FX)  ans = WarnOnce.showWarningFX(PamController.getInstance().getGuiManagerFX().getMainScene().getOwner(),
								"Duplicate settings encountered", PamUtilsFX.htmlToNormal(msg), AlertType.WARNING, null, null, "Keep Duplicates", "Delete Duplicates");
						else  ans = WarnOnce.showWarning(null, "Duplicate settings encountered", msg, WarnOnce.OK_CANCEL_OPTION, null, null,"Keep Duplicates", "Delete Duplicates");
						if (ans == WarnOnce.CANCEL_OPTION) {
							purgeDuplicates = true;
						}
					}
					if (purgeDuplicates) continue;
					pamSettingsList.add(initialSettingsList.get(i));
				}

				// if this is not a duplicate, go ahead and add it to the list
				else {
					pamSettingsList.add(initialSettingsList.get(i));
				}
			}
		}
		/*
		 * then save it to a single serialized file
		 */
		ObjectOutputStream file = openOutputFile(fileName);
		PamControlledUnitSettings unitSettings = null;
		if (file == null) {
			System.err.println("Error opening " + fileName + " for write access.  Cannot save settings information.");
			return false;
		}
		try {
			for (int i = 0; i < pamSettingsList.size(); i++){
				PamControlledUnitSettings ps = pamSettingsList.get(i);
				//				System.out.println(String.format("Write %s %s", ps.getUnitType(), ps.getUnitName()));
				file.writeObject(unitSettings = pamSettingsList.get(i));
			}
			file.close();
		} catch (Exception Ex) {
			System.err.println("Error writing settings to file object " + unitSettings.getUnitName() + " object " + unitSettings.getSettings());
			Ex.printStackTrace();
			return false;
		}

		//		try { // experimenting with xml output.
		//			FileOutputStream fos = new FileOutputStream("pamguard.xml");
		//			XMLEncoder xe = new XMLEncoder(fos);
		//			for (int i = 0; i < nUsed; i++) {
		//				xe.writeObject(pamSettingsList.get(i).getUnitName());
		//			}
		//			xe.flush();
		//			xe.close();
		//			fos.close();
		//		} catch (FileNotFoundException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		} catch (IOException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}

		// and save the settings file list to that's file
		return true;

	}

	/**
	 * Checks if the PamControlledSettings object is already in the settings ArrayList.  Comparison is done by
	 * checking the unit type and unit name.
	 * @param pamSettingsList the ArrayList containing the PamControlledUnitSettings
	 * @param settingToCheck the PamControlledUnitSettings to check
	 * @return true if it is in the list, false if not
	 */
	public boolean thisIsADuplicate(ArrayList<PamControlledUnitSettings> pamSettingsList, PamControlledUnitSettings settingToCheck) {
		int listSize = pamSettingsList.size();
		for (int i=0; i<listSize; i++) {
			if (settingToCheck.getUnitType().equals(pamSettingsList.get(i).getUnitType()) &&
					settingToCheck.getUnitName() != null &&
					settingToCheck.getUnitName().equals(pamSettingsList.get(i).getUnitName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Save configuration settings to a PSFX file (XML).
	 * @return true if successful.
	 */
	public boolean saveSettingsToXMLFile(File file) {

		/*
		 * Create a new list of settings in case they have changed
		 */

		//XMLSettings
		ArrayList<PamControlledUnitSettings> pamSettingsList;
		pamSettingsList = new ArrayList<PamControlledUnitSettings>();
		ArrayList<PamSettings> owners = getOwners();
		for (int i = 0; i < owners.size(); i++) {
			pamSettingsList
			.add(new PamControlledUnitSettings(owners.get(i)
					.getUnitType(), owners.get(i).getUnitName(),
					owners.get(i).getClass().getName(),
					owners.get(i).getSettingsVersion(),
					owners.get(i).getSettingsReference()));
		}
		int nUsed = pamSettingsList.size();
		/*
		 * Then go through the initialSettings, that were read in and any that were not used
		 * add to the current settings output so that they may be used next time around incase
		 * a module reappears that was temporarily not used.
		 */
		if (initialSettingsList != null) {
			for (int i = 0; i < initialSettingsList.size(); i++) {
				if (settingsUsed != null && settingsUsed.length > i && settingsUsed[i]) continue;
				pamSettingsList.add(initialSettingsList.get(i));
			}
		}
		/*
		 * then save it to a single XML file
		 */
		//XML file test

		objectToXMLFile(pamSettingsList,file);
		return true;

	}

	/**
	 * An object is serializable iff .... TBC
	 */
	public void objectToXMLFile(Object serialisableObject, File file){

		//		XStream xStream = new XStream();
		//	    OutputStream outputStream = null;
		//	    Writer writer = null;
		//
		//	    try {
		//	        outputStream = new FileOutputStream(file);
		//	        writer = new OutputStreamWriter(outputStream, Charset.forName("UTF-8"));
		//	        xStream.toXML(serialisableObject, writer);
		//	    } catch (Exception exp) {
		//	    	exp.printStackTrace();
		////	        log.error(null, exp);
		////	        return false;
		//	    } finally {
		//	        try {
		//	        	writer.close();
		//	        	outputStream.close();
		//			} catch (IOException e) {
		//				e.printStackTrace();
		//			}
		//	        System.out.println("done!");
		//
		//	    }
		System.out.println("The code for objectToXMLFile(Object serialisableObject, File file) has been commented out!!");
	}

	/**
	 * Load the PAMGAURD settings either from psf file or from
	 * a database, depending on the run mode and type of settings required.
	 * @param runMode
	 * @return OK if load was successful.
	 */
	public int loadPAMSettings(int runMode) {
		int ans = LOAD_SETTINGS_OK;
		loadGlobalSettings();
		switch(runMode) {
		case PamController.RUN_NORMAL:
		case PamController.RUN_NETWORKRECEIVER:
			ans = loadNormalSettings();
			break;
		case PamController.RUN_PAMVIEW:
			if (GlobalArguments.getParam(GlobalArguments.BATCHVIEW) != null) {
				ans = loadNormalSettings();
			}
			else {
				ans = loadViewerSettings();
			}
			break;
		case PamController.RUN_MIXEDMODE:
			ans = loadMixedModeSettings();
			break;
		case PamController.RUN_REMOTE:
			PamSettingManager.RUN_REMOTE = true;
			ans = loadNormalSettings();
			break;
		case PamController.RUN_NOTHING:
			ans = loadNormalSettings();
			break;
		default:
			return LOAD_SETTINGS_CANCEL;
		}
		if (ans == LOAD_SETTINGS_OK) {
			initialiseRegisteredModules();
		}
		return ans;
	}

	/**
	 * Load settings perfectly 'normally' from a psf file.
	 * @return OK whether or not any settings were loaded.
	 */
	private int loadNormalSettings() {
		return loadPSFSettings();
	}

	/**
	 * Load settings for viewer mode. These must come from
	 * an old PAMGUARD database containing settings information.
	 * @return true if settings loaded sucessfully.
	 */
	private int loadViewerSettings() {
		return loadDBSettings();
	}

	/**
	 * Load settings for mixed mode. These must come from
	 * an old PAMGUARD database containing settings information.
	 * @return true if settings loaded sucessfully.
	 */
	private int loadMixedModeSettings() {
		return loadDBSettings();
	}

	/**
	 * Some modules may have already registered before the
	 * settings were loaded, so this function is called
	 * as soon as they are loaded which sends settings to
	 * all modules in the list.
	 */
	private void initialiseRegisteredModules() {
		ArrayList<PamSettings> owners = getOwners();
		if (owners == null) {
			return;
		}
		PamControlledUnitSettings settings = null;
		if (settingsUsed == null || settingsUsed.length != initialSettingsList.size()) {
			settingsUsed = new boolean[initialSettingsList.size()];
		}
		for (int i = 0; i < owners.size(); i++) {
			settings = findSettings(initialSettingsList, settingsUsed, owners.get(i));
			if (settings != null) {
				try {
					owners.get(i).restoreSettings(settings);
				}
				catch (ClassCastException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Open the file that contains a list of files and optionally open a dialog
	 * giving the list of recent files.
	 * <p>
	 * Unfortunately, as soon as this gets called the first time, it tries to
	 * open a database to get more settings information and different database
	 * plug ins all start trying to get more settings and it goes round and round and
	 * round. Need to ensure that these loop around only get given the general settings
	 * information.
	 * @return
	 */
	private int loadPSFSettings() {
		if (PamSettingManager.remote_psf == null) {
			if (settingsFileData == null) {
				loadLocalSettings();
			}
			if (loadingLocalSettings) return LOAD_SETTINGS_OK;
			if (
					//					settingsFileData.showFileList &&
					programStart) {
				SettingsFileData newData = showSettingsDailog(settingsFileData);
				if (newData != null) {
					settingsFileData = newData.clone();
					/*
					 * Save the settings file data immediately so that if we crash, this file
					 * is still at the top of the list next time we run.
					 */
					saveSettingsFileData();
				}
				else {
					return LOAD_SETTINGS_CANCEL;
				}
				programStart = false;
			}
			File ff = settingsFileData.getFirstFile();
		}

		// if we are running a psf remotely, add it to the SettingsFileData list
		else {
			setDefaultFile(PamSettingManager.remote_psf);
		}

		initialSettingsList = loadSettingsFromFile();
		//XMLSettings
		//		initialSettingsList = loadSettingsFromXMLFile();

		/*TODO FIXME -implement this properly (see also PamGui-line 478) to enable saving menu item
		 * so far it works for some settings- one it doesn't work for is File Folder Acquisition
		 *
		 * output from loading XML
		 * ------------------------------------
						PAMGUARD Version 1.11.02j branch SMRU
						Revision 1028
						java.version 1.7.0_07
						java.vendor Oracle Corporation
						java.vm.version 23.3-b01
						java.vm.name Java HotSpot(TM) Client VM
						os.name Windows 7
						os.arch x86
						os.version 6.1
						java.library.path lib
						For further information and bug reporting visit www.pamguard.org
						If possible, bug reports and support requests should
						contain a copy of the full text displayed in this window.
						(Windows users right click on window title bar for edit / copy options)

						System memory at 08 January 2013 19:15:31 UTC Max 1037959168, Free 13586536
						Pam Color Manager n:t Pam Color Manager
						Array Manager n:t Array Manager
						PamGUI n:t PamGUI
						Pamguard Controller n:t PamController
						MySQL Database System n:t MySQL Database System
						Database n:t MS Access Database System
						Database n:t OOo Database System
						Database n:t Pamguard Database
						Sound Card System n:t Acquisition System
						Sound Acquisition n:t ASIO Sound System
						Sound Acquisition n:t New ASIO Sound System
		 ****			File Folder Analysis n:t File Folder Acquisition System
						<PamController.PamControlledUnitSettings>
						    <versionNo>1</versionNo>
						    <unitType>File Folder Acquisition System</unitType>
						    <unitName>File Folder Analysis</unitName>
						    <settings class="Acquisition.FolderInputParameters" reference="../../PamController.PamControlledUnitSettings[13]/settings" />
						</PamController.PamControlledUnitSettings>
		 * -------------------------------------
		 * Looks like not ALL information has been stored correctly-might be best to contact XStream about resolution
		 *
		 */


		return (initialSettingsList == null ? LOAD_SETTINGS_NEW : LOAD_SETTINGS_OK);

	}

	/**
	 * Load data from settings files.
	 * <p>
	 * This is just the general data - the list of recently used
	 * psf files and recent database files.
	 */
	public boolean loadLocalSettings() {

		loadingLocalSettings = true;

		loadSettingsFileData();
		

		if (!PamSettingManager.RUN_REMOTE && !GlobalArguments.isBatch()) {
			// run the log file check and the tips of the day here. 
			
			
			if (settingsFileData != null) {
				if (settingsFileData.getCheckLogFileErrors()) {
					LogFileUtils.checkLogFileErrors(Pamguard.getSettingsFolder());
				}
				
				
				TipOfTheDayManager.getInstance().setShowAtStart(settingsFileData.showTipAtStartup);
				if (settingsFileData.showTipAtStartup) {
					if (PamGUIManager.isSwing()) {
						TipOfTheDayManager.getInstance().showTip(null, null);
					}
				}
			}
		}

		// if the scaling factor = 0 (happens on first load), set it to 1.  Then scale the display
		if (settingsFileData.getScalingFactor()==0.0) {
			settingsFileData.setScalingFactor(1.0);
		}
		scaleDisplay(settingsFileData.getScalingFactor());


		boolean ok = true; // always ok if non - database settings are used.
		//
		//		if (PamController.getInstance().getRunMode() != PamController.RUN_NORMAL) {
		//			ok = loadDBSettings();
		//		}

		loadingLocalSettings = false;

		return ok;

	}

	/**
	 * Adjust the size of the text by the scaleFactor variable.  This was added in response to the issue of PAMGuard not
	 * scaling properly on 4K monitors.  Things that aren't affected by this code: buttons and icons, the window title font,
	 * the time font, and the html in the help files.
	 *
	 * In order to finish this off, put more work into scaling the button images.
	 *
	 * @param scalingFactor
	 */
	private void scaleDisplay(double scalingFactor) {
		String[] theKeys = {
				"AbstractButton.font",
				"Button.font",
				"CheckBox.font",
				"CheckBoxMenuItem.font",
				"CheckBoxMenuItem.acceleratorFont",
				"ColorChooser.font",
				"ComboBox.font",
				"EditorPane.font",
				"FormattedTextField.font",
				"IconButton.font",
				"Label.font",
				"List.font",
				"Menu.font",
				"MenuBar.font",
				"MenuItem.font",
				"OptionPane.font",
				"Panel.font",
				"PasswordField.font",
				"PopupMenu.font",
				"ProgressBar.font",
				"RadioButton.font",
				"RadioButtonMenuItem.font",
				"ScrollPane.font",
				"Slider.font",
				"TabbedPane.font",
				"TabbedPane.smallfont",
				"Table.font",
				"TableHeader.font",
				"TextArea.font",
				"TextField.font",
				"TextPane.font",
				"TitledBorder.font",
				"ToggleButton.font",
				"ToolBar.font",
				"ToolTip.font",
				"Tree.font",
				"Viewport.font"
		};
		//TODO - does work with NIMBUS look and feel.
		for (int i=0; i<theKeys.length; i++) {
			FontUIResource f = (FontUIResource) UIManager.get(theKeys[i]);
			if (f==null) continue;
			float curSize = f.getSize2D();
			FontUIResource fNew = new FontUIResource(f.deriveFont((float) (curSize*scalingFactor)));
			UIManager.put(theKeys[i], fNew);
		}
	}

	/**
	 * Try to get settings information from a valid database. If none are
	 * loaded, then return null and Pamguard will try to get them from a psf file.
	 */
	public int loadDBSettings() {
		return loadDBSettings(2);
	}
	/**
	 * Try to get settings information from a valid database. If none are
	 * loaded, then return null and Pamguard will try to get them from a psf file.
	 * @param showDatabaseDialog Show a dialog to ask for a database. 0 = never, 1 = if no database open 2 = always.
	 */
	public int loadDBSettings(int showDatabaseDialog) {


		if (settingsFileData == null) {
			loadLocalSettings();
		}

		loadDatabaseFileData();

		// try to find the database settings...
		//		PamControlledUnitSettings dbSettings = findGeneralSettings(DBControl.getDbUnitType());

		DBControlSettings dbControlSettings = new DBControlSettings();

		/*
		 * Get settings from the database from either the Pamguard_Settings_Last
		 * or from the Pamguard_Settings table.
		 */
		initialSettingsList = dbControlSettings.loadSettingsFromDB(showDatabaseDialog);


		/**
		 *  now need to get parameters back from the listed modules in databaseOwners
		 *  so that the correct settings can be passed over to the initialSettingsList.
		 */

		if (initialSettingsList == null) {
			return LOAD_SETTINGS_CANCEL;
		}
		else {
			/* reading settings from the database was sucessful. Now the problem we have is that
			 *  this database closes, and when the 'real' database opens up later, it won't be pointing
			 *  at the same place !
			 *  Two options are 1) try to keep this version of the database alive
			 *  2) frig the generalsettings so that the 'real' database gets the same ones.
			 *
			 *  Trouble is that there are multiple settings in the settings database stuff.
			 *  Copy them all back into the generalSettings list
			 */
			PamControlledUnitSettings aSet, generalSet;
			/**
			 * Don't take these out of databaseSettingsList - go throuh
			 */
			PamSettings dbOwner;
			databaseSettingsList.clear();
			if (databaseOwners != null) {
				for (int i = 0; i < databaseOwners.size(); i++) {
					dbOwner = databaseOwners.get(i);
					aSet = new PamControlledUnitSettings(dbOwner.getUnitType(),
							dbOwner.getUnitName(), dbOwner.getClass().getName(),
							dbOwner.getSettingsVersion(), dbOwner.getSettingsReference());
					databaseSettingsList.add(aSet);
					// see if there is any settings with the same type and name
					// in the general list and copy settings object over.
					generalSet = findSettings(initialSettingsList, aSet.getUnitType(), null);
					if (generalSet != null) {
						generalSet.setSettings(aSet.getSettings());
					}
				}
			}
		}
		if (initialSettingsList == null) {
			return LOAD_SETTINGS_CANCEL;
		}
		else if (initialSettingsList.size() == 0) {
			return LOAD_SETTINGS_NEW;
		}
		else {
			return LOAD_SETTINGS_OK;
		}
	}

	/**
	 * See if there is a database module in PAMGUARD and if so, save the
	 * settings in serialised from in the Pamguard_Settings and Pamguard_Settings_Last
	 * tables.
	 * @return true if successful.
	 */
	private boolean saveSettingsToDatabase() {
		// see if there is an existing database module and if there is, then
		// it will know how to save settings.
		DBControl dbControl = (DBControl) PamController.getInstance().findControlledUnit(DBControl.getDbUnitType());
		if (dbControl == null) {
			return false;
		}
		return dbControl.saveSettingsToDB();
	}

	/**
	 * Find the owner of some PAMGUARD settings.
	 * @param ownersList which list to search
	 * @param unitType unit type
	 * @param unitName unit name
	 * @return owner of the settings.
	 */
	private PamSettings findOwner(ArrayList<PamSettings> ownersList, String unitType, String unitName) {
		PamSettings owner;
		for (int i = 0; i < ownersList.size(); i++) {
			owner = ownersList.get(i);
			if (!owner.getUnitType().equals(unitType)) continue;
			if (unitName != null && !owner.getUnitName().equals(unitName)) continue;
			return owner;
		}

		return null;
	}

	/**
	 * Load PAMGUARD settings from a psf OR a psfx file.
	 * @return Array list of settings.
	 */
	private ArrayList<PamControlledUnitSettings> loadSettingsFromFile() {
		String inputFile = getSettingsFileName();
		if (inputFile == null) {
			return null;
		}
		if (inputFile.endsWith(fileEnd)) {
			return loadSettingsFromPSFFile();
		}
		if (inputFile.endsWith(fileEndx)) {
			PamSettingsGroup psg = PSFXReadWriter.getInstance().loadFileSettings(new File(inputFile));
			if (psg == null) {
				return null;
			}
			else {
				return psg.getUnitSettings();
			}
		}
		return null;
	}
	/**
	 * Load PAMGUARD settings from a psf file.
	 * @return Array list of settings.
	 */
	private ArrayList<PamControlledUnitSettings> loadSettingsFromPSFFile() {


		ArrayList<PamControlledUnitSettings> newSettingsList =
				new ArrayList<PamControlledUnitSettings>();

		PamControlledUnitSettings newSetting;

		ObjectInputStream file = openInputFile();

		if (file == null) return null;

		Object j;
		while (true) {
			try {
				j = file.readObject();
				newSetting = (PamControlledUnitSettings) j;
				newSettingsList.add(newSetting);
			}
			catch (EOFException eof){
				break;
			}
			catch (InvalidClassException ice) {
				System.out.println(ice.getMessage());
				String title = "Error loading module";
				String msg = "<p>This psf is trying to load <em>" + ice.classname + "</em> but is having problems.</p><br>" +
						"<p>This may be because of an incompatiblility between the version of the module in the psf and " +
						"the version of the module currently in PAMGuard.  Check the console window for an error message " +
						"with the version details.</p><br>" +
						"<p>The module may or may not load, and even if it " +
						"loads it may have lost it's settings.  Please check before performing any analysis.</p>";
				String help = null;
				
				int ans = WarnOnce.showWarning(PamController.getMainFrame(), title, msg, WarnOnce.WARNING_MESSAGE, help);
				
				break;
			}
			catch (IOException io){
				System.out.println(io.getMessage());
				/**
				 * DG 10/8/2015 There is a break here, basically if I change the
				 * serialVerionUID of any class it will get stuck in an infinite loop
				 * unless I break - so don't ever change serialVersionUID's !!!!!
				 */
				break;
			}
			catch (NoClassDefFoundError Ex){
				// print and continue - there may be other things we can deal with.
				String title = "Error loading module";
				String msg = "This psf is trying to load " + Ex.getMessage() + " but is having problems.<p>" +
						"It is likely that an older version of Java is trying to load a class which is from a newer version. " +
						"This module will not be loaded, and will be removed from the psf file to prevent instabilities.";
				String help = null;
				int ans = WarnOnce.showWarning(PamController.getMainFrame(), title, msg, WarnOnce.WARNING_MESSAGE, help);
				System.err.println("Exception while loading " +	Ex.getMessage());
			}
			catch (ClassNotFoundException Ex){

				// Specific case: if a UID version of Pamguard is trying to load a psf created by an older non-UID version of Pamguard, one class
				// will not be found: PamView.SymbolData.  Do a quick check here to see if this is causing the problem.  If so, give the user a
				// specific warning so they know what's going on.
				String badModule = Ex.getMessage();
				if (badModule.startsWith("PamView.SymbolData")) {
					String title = "Warning - possible incompatibility between versions";
					String msg = "You may be trying to load a psf file created in an older (prior to version 2) version of Pamguard.  This could cause problems,  " +
							"depending on which modules are being used and if they've changed between versions.  It is recommended that you " +
							"check all module configuration parameters to verify the information is still accurate, prior to running.  ";
					String help = null;
					int ans = WarnOnce.showWarning(PamController.getMainFrame(), title, msg, WarnOnce.WARNING_MESSAGE, help, Ex);
				}

				// print and continue - there may be other things we can deal with.
				else {
					String title = "Error loading module";
					String msg = "This psf is trying to load " + Ex.getMessage() + " but is having problems.<p>" +
							"If this is a plug-in, the error may have been caused by an incompatibility between " +
							"it and this version of PAMGuard, or the plug-in jar file is not in the plugins folder.  " +
							"Check the jar file location, or the developer's website for help.<p>" +
							"If this is a core Pamguard module, please copy the error message text and email to " +
							"support@pamguard.org.<p>" +
							"This module will not be loaded, and will be removed from the psf file to prevent instabilities.";
					String help = null;
					int ans = WarnOnce.showWarning(PamController.getMainFrame(), title, msg, WarnOnce.WARNING_MESSAGE, help, Ex);
					System.err.println("Exception while loading " +	Ex.getMessage());
				}
			}
			catch (Exception Ex) {
				//				Ex.printStackTrace();
				System.out.println(Ex.getMessage());
			}
		}
		try {
			file.close();
		}
		catch (Exception Ex) {
			Ex.printStackTrace();
		}
		//		listSettings(newSettingsList);
		summarizeSettings(newSettingsList);
		return newSettingsList;
	}


	//	private String nodeToString(Node node) {
	//		StringWriter sw = new StringWriter();
	//		try {
	//			Transformer t = TransformerFactory.newInstance().newTransformer();
	//			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	//			t.transform(new DOMSource(node), new StreamResult(sw));
	//		} catch (TransformerException te) {
	//			System.out.println("nodeToString Transformer Exception");
	//		}
	//		return sw.toString();
	//	}


	//XMLSettings
	//	/**
	//	 * Load PAMGUARD settings from a psf file.
	//	 * @return Array list of settings.
	//	 */
	//	private ArrayList<PamControlledUnitSettings> loadSettingsFromXMLFile() {
	//
	//		XMLOutputter outp = new XMLOutputter();
	//		final ArrayList<PamControlledUnitSettings> newSettingsList = new ArrayList<PamControlledUnitSettings>();
	//		final XStream xStream = new XStream();
	//		SAXBuilder builder = new SAXBuilder();
	//
	//		File xmlFile = new File("C:\\Users\\gw\\Desktop\\tidy\\testPsfs\\LoggerTestpx.psfx");
	//		String str = "";
	//		try {
	//
	//			Document document = (Document) builder.build(xmlFile);
	//			Element rootNode = document.getRootElement();
	//			List list = rootNode.getChildren();
	//
	//			for (int i = 0; i < list.size(); i++) {
	//
	//				Element node = (Element) list.get(i);
	//
	//				str = outp.outputString(node);
	//				PamControlledUnitSettings pcus = (PamControlledUnitSettings) xStream.fromXML(str);
	////				System.out.println(pcus.getUnitName() + " n:t " + pcus.getUnitType());
	//				newSettingsList.add(pcus);
	//			}
	//
	//		} catch (IOException io) {
	//			System.out.println(io.getMessage());
	//		} catch (JDOMException jdomex) {
	//			System.out.println(jdomex.getMessage());
	//		} catch (Exception e) {
	//			System.out.println(str);
	//			e.printStackTrace();
	//		}
	//
	//		return newSettingsList;
	//	}


	/**
	 * Quick print list of all settings to work out wtf is going on.
	 * @param newSettingsList
	 */
	private void listSettings(
			ArrayList<PamControlledUnitSettings> newSettingsList) {
		int iSet = 0;
		for (PamControlledUnitSettings set:newSettingsList) {
			System.out.printf("%02d Type %s Name %s Class %s\n", iSet++, set.getUnitType(),
					set.getUnitName(), set.getSettings().getClass().toString());
		}

	}


	/**
	 * Similar to listSettings, but instead of listing all modules only list them once but with a note if
	 * there are duplicates
	 *
	 * @param newSettingsList
	 */
	private boolean summarizeSettings(ArrayList<PamControlledUnitSettings> newSettingsList) {
		boolean duplicatesFound = false;
		int listSize = newSettingsList.size();
		boolean[] alreadySeen = new boolean[listSize];
		for (int i=0; i<listSize; i++) {
			if (alreadySeen[i]) continue;
			PamControlledUnitSettings set = newSettingsList.get(i);
			int count = 1;
			alreadySeen[i] = true;
			for (int j=i+1; j<listSize; j++) {
				if (set.getUnitType().equals(newSettingsList.get(j).getUnitType()) &&
						set.getUnitName().equals(newSettingsList.get(j).getUnitName()) &&
						!alreadySeen[j]) {
					count++;
					alreadySeen[j] = true;
				}
			}
//			System.out.printf("Type: %s; Name: %s; Class: %s; is found in psf settings %d time(s)\n", set.getUnitType(),
//					set.getUnitName(), set.getSettings().getClass().toString(), count);
			if (count>1) duplicatesFound=true;
		}
		return duplicatesFound;
	}

	/**
	 * See if a particular PamControlledUnitSettings object is the right one
	 * for a particular module that wants some settings.
	 * @param settingsUser User of settings
	 * @param settings Settings object.
	 * @return true if matched.
	 */
	public boolean isSettingsUnit(PamSettings settingsUser, PamControlledUnitSettings settings) {
		if (settings.getUnitName() == null || settingsUser.getUnitName() == null) return false;
		if (settings.getUnitType() == null || settingsUser.getUnitType() == null) return false;

		/*
		 *  some of the settings names used in Viewer mode have become too long, notably
		 *  in some data selectors which are using a datablocks long data name. This 
		 *  screws things up, so moving to a begins with rather than equals for the name. 
		 */
		String name = settingsUser.getUnitName();
		String type = settingsUser.getUnitType();
		long version = settingsUser.getSettingsVersion();
		
		if (settings.getUnitType().equals(type)
				&& settings.versionNo == version){
			if (name.startsWith(settings.getUnitName())) {
				return true;
			}
		}

		return false;
	}


	/**
	 * Name check used when the initial setting search failed but it's been found that there has been a type
	 * name change within the settings user.
	 * @param otherName alternate name information
	 * @param settings PAm Settings.
	 * @return true if seem to be the same.
	 */
	private boolean isSettingsUnit(SettingsNameChange otherName, PamControlledUnitSettings settings) {
		if (otherName.getModuleClass().getName().equals(settings.getOwnerClassName())) {
			return true;
		}
		if (!otherName.getOldType().equals(settings.getUnitType())) {
			return false;
		}
		if (otherName.getOldName() != null && !otherName.getOldName().equals(settings.getUnitName())) {
			return false;
		}
		return true;
	}

	/**
	 * Open psf file for settings serialised output.
	 * @return stream handle.
	 */
	public ObjectOutputStream openOutputFile(String outputFile) {
		try {
			return new ObjectOutputStream(new FileOutputStream(outputFile));

		} catch (Exception Ex) {
			System.out.println(Ex);
			return null;
		}
	}

	/**
	 * Open psf file for settings input. <br> does no work with psfx files
	 * @return stream handle.
	 */
	private ObjectInputStream openInputFile() {
		//		System.out.println("Loading settings from " + getSettingsFileName());
		String fileName = getSettingsFileName();
//		File sFile = new File(fileName);
//		boolean ex = sFile.exists();
//		if (ex == false) {
//			return null;
//		}
		try {
			return new ObjectInputStream(new FileInputStream(fileName));
		} catch (Exception Ex) {
			//Ex.printStackTrace();
			//			if(!userNotifiedAbsentSettingsFile){
			//				System.out.println("Serialized settings file not found in JAR, Possibly not being run from standalone JAR file e.g. in Eclipse ?");
			//				Splash.setStartupErrors(true);
			//				JOptionPane.showMessageDialog(null,
			//	                "Cannot Load: " + getSettingsFileName() +"\nAttempting to load defaults!"
			//	                +"\nThis is expected on first use."
			//	                ,
			//	                "PamSettingManager",
			//	                JOptionPane.WARNING_MESSAGE);
			//				userNotifiedAbsentSettingsFile= true;
			//			}
			String msg = "You are opening new configuration file: " + getSettingsFileName();
			msg += "\nClick OK to continue with blank configuration or Cancel to exit PAMGuard";
			int ans = JOptionPane.showConfirmDialog(null, msg, "PAMGuard settings", JOptionPane.OK_CANCEL_OPTION);
			if (ans == JOptionPane.CANCEL_OPTION) {
				System.exit(0);
			}


			return null;
		}
	}

	//	/**
	//	 * Returns total gobbledygook - need to improve the way
	//	 * PAMGAURD creates new psf files.
	//	 * @return lies.
	//	 */
	//	private ObjectInputStream openInputFileResource() {
	//		try {
	//			return new ObjectInputStream( //new FileInputStream(
	//					ClassLoader.getSystemResourceAsStream("DefaultPamguardSettings.psf"));
	//		} catch (Exception Ex) {
	////			//Ex.printStackTrace();
	////			System.out.println("Serialized default settings file not found!");
	////			if(!userNotifiedAbsentDefaultSettingsFile){
	////			JOptionPane.showMessageDialog(null,
	////	                "No Default Settings Found",
	////	                "PamSettingManager",
	////	                JOptionPane.ERROR_MESSAGE);
	////			}
	////			userNotifiedAbsentDefaultSettingsFile= true;
	//			return null;
	//		}
	//	}

	/**
	 * The settings list file is a file containing a list of recently
	 * used psf files.
	 * @return The settings list file
	 */
	private File getSettingsListFile() {
		String setFileName = pamguard.Pamguard.getSettingsFolder() + File.separator + settingsListFileName;
		int runMode = PamController.getInstance().getRunMode();
		switch (runMode) {
		case PamController.RUN_NETWORKRECEIVER:
			setFileName += "_nr";
			break;
		case PamController.RUN_MIXEDMODE:
			setFileName += "m";
			break;
		}
		setFileName += settingsListFileEnd;
		return new File(setFileName);
	}

	/**
	 * Get a file for global settings
	 * @return File for global settings storage.
	 */
	private File getGlobalSettingsFile() {
		String fileName = pamguard.Pamguard.getSettingsFolder() + File.separator + gloablListfileName + settingsListFileEnd;
		return new File(fileName);
	}

	/**
	 * Get a list of recently used databases.
	 * @return list of recently used databases
	 */
	private File getDatabaseListFile() {
		String setFileName = pamguard.Pamguard.getSettingsFolder() + File.separator + databaseListFile;
		return new File(setFileName);
	}

	/**
	 * Get the settings folder name and if necessary,
	 * create the folder since it may not exist.
	 *
	 * 2019/10/02 mo
	 * MOVED TO pamguard.Pamguard AS A STATIC FUNCTION, SO THAT WE CAN ACCESS
	 * IT FOR THE LOG FILE WHEN PAMGUARD FIRST STARTS
	 *
	 * @return folder name string, (with no file separator on the end)
	 */
//	private String getSettingsFolder() {
//		String settingsFolder = System.getProperty("user.home");
//		settingsFolder += File.separator + "Pamguard";
//		// now check that folder exists
//		File f = new File(settingsFolder);
//		if (f.exists() == false) {
//			f.mkdirs();
//		}
//		// default folder doesn't work for psf since it saves the settings file back into the wrong place.
////		String defFolder =  PamFolders.getDefaultProjectFolder();
//		return settingsFolder;
//	}

	/**
	 * Now that the database is becoming much more fundamental to settings
	 * storage and retrieval, the latest database settings should go into
	 * the main settings file. This contains a list of recent databases. The trouble is,
	 * the settings are spread amongst several different settings object (e.g. one that
	 * tells us what type of database, another that tells us a list of recent databases
	 * for a specific database type, etc.
	 * <p>
	 * We therefore need some modules (i.e. database ones) to also store their settings
	 * in a general settings list so that they can be read in before any other settings
	 * are read in. So each unit when it registers, says whether it should be included in
	 * the general list as well as the specific data file.
	 *
	 */
	public boolean loadSettingsFileData() {
		ObjectInputStream is = null;
		settingsFileData = new SettingsFileData();
		/*
		 * First do some tests to see if the settingslistfile exists. If it doens't
		 * then create the file (and do a few other things)
		 */
		File slFile = getSettingsListFile();
		if (!slFile.exists()) {
			createSettingsListFile();
		}

		try {
			is = new ObjectInputStream(new FileInputStream(getSettingsListFile()));
			settingsFileData = (SettingsFileData) is.readObject();

		} catch (Exception Ex) {
			//			System.out.println(Ex);
			System.out.println("Unable to open " + getSettingsListFile() + " this is normal on first use");
		}
		try {
			if (is != null) {
				is.close();
			}
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		if (settingsFileData == null){
			createSettingsListFile();
			return false;
		}

		return true;
	}

	/**
	 * Create a settings list file. This should only
	 * ever get called once per user. One of the things it
	 * will do is copy all psf files from the installed directory
	 * over into the settingsFolder and then populate the list
	 * in a settings list file so that users get a reasonably
	 * coherent startup experience.
	 */
	private void createSettingsListFile() {
		/**
		 * List all psf files in the program folder.
		 * I think that we should already be working in that folder,
		 * so can just list the files.
		 */
		settingsFileData = new SettingsFileData();
		PamFileFilter psfFilter = new PamFileFilter("psf files", ".psf");

		// if we're running the beta version, also add in psfx files. To test for beta, check if
		// the version number starts with anything besides a 1
		if (!PamguardVersionInfo.version.startsWith("1")) {
			psfFilter.addFileType(".psfx");
		}
		psfFilter.setAcceptFolders(false);
		String settingsFolder = pamguard.Pamguard.getSettingsFolder() + File.separator;
		// list files in the current folder.
		String userDir = System.getProperty("user.dir");
		File folder = new File(userDir);
		File[] psfFiles = folder.listFiles(psfFilter);
		File aFile;
		if (psfFiles != null) {
			for (int i = 0; i < psfFiles.length; i++) {
				aFile = psfFiles[psfFiles.length-i-1];
				// copy that file over to the settings folder.
				File newFile = new File(settingsFolder + File.separator + aFile.getName());
				//				aFile.renameTo(newFile);
				copyFile(aFile, newFile);
				// then add it to the list.
				settingsFileData.setFirstFile(newFile);
			}
		}

		saveSettingsFileData();

	}

	private boolean copyFile(File source, File dest) {
		FileInputStream fIs;
		FileOutputStream fOs;
		final int BUFFLEN = 1024;
		byte[] buffer = new byte[BUFFLEN];
		int bytesRead;
		try {
			fIs = new FileInputStream(source);
			fOs = new FileOutputStream(dest);
			while ((bytesRead = fIs.read(buffer)) != -1) {
				fOs.write(buffer, 0, bytesRead);
			}
			fIs.close();
			fOs.close();
		} catch (FileNotFoundException e) {
			System.out.println("FileNotFound exception in PamSettingsManager: " + e.getMessage());
			return false;
		} catch (IOException e) {
			System.out.println("IO exception in PamSettingsManager: " + e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * Save the list of recently used settings files.
	 * @return true if write OK.
	 */
	private boolean saveSettingsFileData() {

		if (settingsFileData == null) {
			return false;
		}
		if (!PamSettingManager.RUN_REMOTE) {
			settingsFileData.showTipAtStartup = TipOfTheDayManager.getInstance().isShowAtStart();
		}
		settingsFileData.trimList();

		ObjectOutputStream os;
		try {
			os = new ObjectOutputStream(new FileOutputStream(getSettingsListFile()));
			os.writeObject(settingsFileData);
		} catch (Exception Ex) {
			System.out.println(Ex);
			return false;
		}
		try {
			os.close();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		return true;
	}

	/**
	 * Loads the details of the last database to be opened. This will
	 * probably be in the form of multiple serialised objects since
	 * the database information is spread amongst several plug in sub-modules.
	 * @return true if settings data loaded ok
	 */
	private boolean loadDatabaseFileData() {

		ObjectInputStream is;

		PamControlledUnitSettings newSetting;
		databaseSettingsList = new ArrayList<PamControlledUnitSettings>();
		Object j;
		try {
			is = new ObjectInputStream(new FileInputStream(getDatabaseListFile()));
		} catch (Exception Ex) {
			return false;
		}
		while (true) {
			try {
				j = is.readObject();
				newSetting = (PamControlledUnitSettings) j;
				databaseSettingsList.add(newSetting);
			}
			catch (EOFException eof){
				break;
			}
			catch (IOException io){
				break;
			}
			catch (ClassNotFoundException Ex){
				// print and continue - there may be othere things we can deal with.
				Ex.printStackTrace();
			}
			catch (Exception Ex) {
				Ex.printStackTrace();
			}
		}
		try {
			is.close();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}

		return true;
	}
	/**
	 * Save the details of the most recently used database.
	 * @return true if successful.
	 */
	private boolean saveDatabaseFileData() {

		if (databaseOwners == null || databaseOwners.size() == 0) {
			return false;
		}
		if (databaseSettingsList == null) {
			databaseSettingsList = new ArrayList<PamControlledUnitSettings>();
		}
		databaseSettingsList.clear();
		PamSettings dbOwner;
		PamControlledUnitSettings aSet;
		for (int i = 0; i < databaseOwners.size(); i++) {
			dbOwner = databaseOwners.get(i);
			aSet = new PamControlledUnitSettings(dbOwner.getUnitType(),
					dbOwner.getUnitName(), dbOwner.getClass().getName(),
					dbOwner.getSettingsVersion(), dbOwner.getSettingsReference());
			databaseSettingsList.add(aSet);
		}

		ObjectOutputStream os;
		try {
			os = new ObjectOutputStream(new FileOutputStream(getDatabaseListFile()));
		} catch (Exception Ex) {
			return false;
		}

		//		write out the settings for all units in the general owners list.
		ArrayList<PamControlledUnitSettings> generalSettingsList;
		generalSettingsList = new ArrayList<PamControlledUnitSettings>();
		for (int i = 0; i < databaseOwners.size(); i++) {
			generalSettingsList
			.add(new PamControlledUnitSettings(databaseOwners.get(i)
					.getUnitType(), databaseOwners.get(i).getUnitName(),
					databaseOwners.get(i).getClass().getName(),
					databaseOwners.get(i).getSettingsVersion(),
					databaseOwners.get(i).getSettingsReference()));
		}
		try {
			for (int i = 0; i < generalSettingsList.size(); i++){
				os.writeObject(generalSettingsList.get(i));
			}
		} catch (Exception Ex) {
			Ex.printStackTrace();
			return false;
		}


		try {
			os.close();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		return true;
	}

	/**
	 * Get the most recently used settings file name. We have added a switch in here
	 * to allow for the direct setting of the psf used from the command line. This
	 * can be used in remote on non remote deployments.
	 * @return File name string.
	 */
	public String getSettingsFileName() {
		if (PamSettingManager.remote_psf != null) {
			//			System.out.println("Automatically loading settings from " + remote_psf);
			return remote_psf;
		}
		else {
			if (settingsFileData == null || settingsFileData.getFirstFile() == null) {
				return null;
			}
			return settingsFileData.getFirstFile().getAbsolutePath();
		}
	}

	public String getDefaultFile() {
		String fn = getSettingsFileName();
		if (fn == null) {
			fn = "PamguardSettings.psf";
		}
		return fn;
	}



	/**
	 * saves settings in the current file
	 * @param frame GUI frame (needed for dialog, can be null)
	 */
	public void saveSettings(JFrame frame) {
		saveFinalSettings();
	}

	/**
	 * Save settings to a new psf file.
	 * @param frame parent frame for dialog.
	 */
	public void saveSettingsAs(JFrame frame) {
		/*
		 * get a new file name, set that as the current file
		 * then write all settings to it.
		 */
		File file = null;
		if (settingsFileData != null) {
			file = settingsFileData.getFirstFile();
		}
		JFileChooser jFileChooser = new PamFileChooser(file);
		//		jFileChooser.setFileFilter(new SettingsFileFilter());
		jFileChooser.setApproveButtonText("Select");
		PamFileFilter fileFilter = new PamFileFilter("PAMGUARD Settings files", getCurrentSettingsFileEnd());
		jFileChooser.setFileFilter(fileFilter);
		jFileChooser.setAcceptAllFileFilterUsed(false);
		//		jFileChooser.setFileFilter(new FileNameExtensionFilter("PAMGUARD Settings files", defaultFile));
		int state = jFileChooser.showSaveDialog(frame);
		if (state != JFileChooser.APPROVE_OPTION) return;
		File newFile = jFileChooser.getSelectedFile();
		if (newFile == null) return;
		//		newFile.getAbsoluteFile().
		newFile = PamFileFilter.checkFileEnd(newFile, getCurrentSettingsFileEnd(), true);

		System.out.println("Saving settings to file " + newFile.getAbsolutePath());

		// Insert the new file into the top of the recent psf file list.  Also check
		// if we are running remotely, which probably means the user double-clicked on
		// a psf to start Pamguard.  In that case, change the remotePSF pointer to
		// the new file as well
		setDefaultFile(newFile.getAbsolutePath());
		if (PamSettingManager.remote_psf != null) {
			PamSettingManager.remote_psf = newFile.getAbsolutePath();
		}

		saveSettings(SAVE_PSF);

		PamController.getInstance().getGuiFrameManager().sortFrameTitles();

	}

	//	/**
	//	 * Save settings to a new psf file.
	//	 * @param frame parent frame for dialog.
	//	 */
	//	public void saveSettingsAsXML(JFrame frame) {
	//		/*
	//		 * get a new file name, set that as the current file
	//		 * then write all settings to it.
	//		 */
	//		File file = null;
	//		if (settingsFileData != null) {
	//			file = settingsFileData.getFirstFile();
	//		}
	//		JFileChooser jFileChooser = new JFileChooser(file);
	//		//		jFileChooser.setFileFilter(new SettingsFileFilter());
	//		jFileChooser.setApproveButtonText("Select");
	//		jFileChooser.setFileFilter(new PamFileFilter("PAMGUARD Settings files (PSFX)", fileEndXML));
	//		//		jFileChooser.setFileFilter(new FileNameExtensionFilter("PAMGUARD Settings files", defaultFile));
	//		int state = jFileChooser.showSaveDialog(frame);
	//		if (state != JFileChooser.APPROVE_OPTION) return;
	//		File newFile = jFileChooser.getSelectedFile();
	//		if (newFile == null) return;
	//		//		newFile.getAbsoluteFile().
	//		newFile = PamFileFilter.checkFileEnd(newFile, fileEndXML, true);
	//
	//		System.out.println(newFile.getAbsolutePath());
	//
	//		setDefaultFile(newFile.getAbsolutePath());
	//
	//		saveSettingsToXMLFile(newFile);
	//
	//		PamController.getInstance().getGuiFrameManager().sortFrameTitles();
	//
	//	}


	/**
	 * Set the default (first) file in the settings file data.
	 * @param defaultFile File name string.
	 */
	public void setDefaultFile(String defaultFile) {

		/**
		 * If saving from viewer or mixed mode, then the
		 * settingsFileData may not have been loaded, in which case
		 * load it now so that old psf names remain in the list.
		 */
		if (settingsFileData == null) {
			//			System.out.println("Must load settings file first");
			loadSettingsFileData();
			if (settingsFileData == null) {
				settingsFileData = new SettingsFileData();
			}
		}
		settingsFileData.setFirstFile(new File(defaultFile));

	}


	/**
	 * Pop up the dialog that's shown at start up to show
	 * a list of recent settings file and give the opportunity
	 * for browsing for more. IF the new settings file is
	 * different from the current one, then send a command off
	 * to the Controller to re-do the entire Pamguard system model
	 * @param frame parent frame for dialog (can be null)
	 */
	public void loadSettingsFrom(JFrame frame) {
		/*TODO look at combining XMLversion(psfx)
		 */
		File currentFile = null;
		if (settingsFileData != null) {
			currentFile = settingsFileData.getFirstFile();
		}
		SettingsFileData newData = showSettingsDailog(settingsFileData);
		if (newData == null) {
			return;
		}
		settingsFileData = newData.clone();
		if (settingsFileData.getFirstFile() != currentFile) {
			settingsFileData.setFirstFile(currentFile);
			saveSettingsFileData();
			// rebuild the entire model.
			PamControllerInterface pamController = PamController.getInstance();
			if (pamController == null) return;
			pamController.totalModelRebuild();
		}

	}

	/**
	 * Import a configuration during viewer mode operation.
	 * @param frame
	 */
	public void importSettings(JFrame frame) {
		if (settingsFileData == null) {
			loadLocalSettings();
		}
		File currentFile = null;
		if (settingsFileData != null) {
			currentFile = settingsFileData.getFirstFile();
		}


		SettingsFileData newData = showSettingsDailog(settingsFileData);
		if (newData == null) {
			return;
		}

		ArrayList<PamControlledUnitSettings> settings = loadSettingsFromFile();
		if (settings == null) {
			String msg = "Unable to load settings from " + getSettingsFileName();
			WarnOnce.showWarning(frame, "Settings Import", msg, WarnOnce.OK_OPTION);
			return;
		}

		/*
		 * Should now have a valid settings file. Import the data from it.
		 */
		PamSettingsGroup pamSettingsGroup = new PamSettingsGroup(System.currentTimeMillis());
		for (PamControlledUnitSettings pus:settings) {
			pamSettingsGroup.addSettings(pus);
		}

		PamController.getInstance().loadOldSettings(pamSettingsGroup);

	}

	/**
	 * Load a settings file and return the contents in a settings group
	 * object. Give the time of the settings group as the time the file
	 * was modified. the settings load code is a bit of a mess - this function
	 * has been written mainly so that it can be called from Matlab and r
	 * so that those languages can load PAMGuard settings.
	 * @param psfFile psf file object.
	 * @return loaded settings from the file.
	 */
	public PamSettingsGroup loadSettings(File psfFile) {
		if (psfFile == null) {
			return null;
		}
		psfFile.lastModified();

		PamSettingsGroup psg = new PamSettingsGroup(psfFile.lastModified());
		ObjectInputStream file = null;

		try {
			file = new ObjectInputStream(new FileInputStream(psfFile));
		}
		catch (IOException eof){
			return null;
		}

		while (true) {
			try {
				Object j = file.readObject();
				PamControlledUnitSettings newSetting = (PamControlledUnitSettings) j;
				psg.addSettings(newSetting);
				//				newSettingsList.add(newSetting);
			}
			catch (EOFException eof){
				break;
			}
			catch (IOException io){
				io.printStackTrace();
				break;
			}
			catch (ClassNotFoundException Ex){
				// print and continue - there may be othere things we can deal with.
				Ex.printStackTrace();

			}
			catch (Exception Ex) {
				Ex.printStackTrace();
			}
		}


		try {
			file.close();
		}
		catch (Exception Ex) {
			//			Ex.printStackTrace();
		}


		return psg;
	}

	public void exportSettings(JFrame frame) {

	}


	/**
	 *
	 * @return everything about every set of settings currently loaded.
	 */
	public PamSettingsGroup getCurrentSettingsGroup() {
		PamSettingsGroup psg = new PamSettingsGroup(PamCalendar.getTimeInMillis());
		PamControlledUnitSettings pcus;
		PamSettings ps;
		ArrayList<PamSettings> owners = getOwners();
		for (int i = 0; i < owners.size(); i++) {
			ps = owners.get(i);
			pcus = new PamControlledUnitSettings(ps.getUnitType(), ps.getUnitName(),
					ps.getClass().getName(),
					ps.getSettingsVersion(), ps.getSettingsReference());
			psg.addSettings(pcus);
		}
		return psg;
	}

	/**
	 * Load some old settings into all modules.
	 * <p>Currently used in viewer mode to load reloaded settings
	 * from binary files and the database.
	 * @param settingsGroup settings group to load.
	 * @param send these new settings round to all existing modules.
	 */
	public void loadSettingsGroup(PamSettingsGroup settingsGroup, boolean notifyExisting) {
		ArrayList<PamControlledUnitSettings> tempSettingsList = settingsGroup.getUnitSettings();


		/////////////deleteDBsettings
		/* TODO FIXME -better way? TEMPORARY - GW
		 * delete DB settings so when old settings psf is restored over current settings
		 * the current DB will not be changed!!
		 */
		ArrayList<String> DBsettingTypes = new ArrayList<String>();

		DBsettingTypes.add("Pamguard Database");
		DBsettingTypes.add("MySQL Database System");
		DBsettingTypes.add("MS Access Database System");
		DBsettingTypes.add("OOo Database System");

		Iterator<PamControlledUnitSettings> it = tempSettingsList.iterator();
		if (it.hasNext()){
			PamControlledUnitSettings current = it.next();
			for (String dbSettingType:DBsettingTypes){
				if (current.getUnitType()==dbSettingType){
					it.remove();
				}
			}
		}

		/////////////

		initialSettingsList = tempSettingsList;
		if (notifyExisting) {
			initialiseRegisteredModules();
		}
	}

	/**
	 * Find the settings for a given unit type and any name
	 * @return the initialSettingsList
	 */
	public PamControlledUnitSettings findSettingsForType(String unitType) {
		if (unitType == null) {
			return null;
		}
		for (PamControlledUnitSettings aSet:initialSettingsList) {
			if (unitType.equals(aSet.getUnitType())) {
				return aSet;
			}
		}
		return null;
	}

	/**
	 * Find a list of unit settings by type and name. If both are specified, then it's going to
	 * (hopefully only return one setting. Otherwise, with null or wildcard names we may get many.
	 * @param unitType unit type, can be wildcard * or null
	 * @param unitName unit name, can be wildcard * or null
	 * @return Array list of settings.
	 */
	public ArrayList<PamSettings> findPamSettings(String unitType, String unitName) {
		ArrayList<PamSettings> owners = getOwners();
		if (owners == null) {
			return null;
		}
		ArrayList<PamSettings> foundSettings = new ArrayList<>();
		if (unitType != null && unitType.equals("*")) {
			unitType = null;
		}
		if (unitName != null && unitName.equals("*")) {
			unitName = null;
		}
		for (PamSettings owner:owners) {
			if (unitType != null && !unitType.equals(owner.getUnitType())) {
				continue;
			}
			if (unitName != null && !unitName.equals(owner.getUnitName())) {
				continue;
			}
			foundSettings.add(owner);
		}

		return foundSettings;
	}


	/**
	 * Show a dialog to allow the user to select a .psf file path.
	 * @param settingsFileData
	 * @return the settings file data.
	 */
	private SettingsFileData showSettingsDailog(SettingsFileData settingsFileData) {
		SettingsFileData newData;
		int flag=PamGUIManager.getGUIType();
		switch (flag) {
		case PamGUIManager.SWING:
			 newData = SettingsFileDialog.showDialog(null, settingsFileData);
			break;
		case PamGUIManager.FX:
			newData = SettingsFileDialogFX.showDialog(settingsFileData, true);
			break;
		default:
			 newData = SettingsFileDialog.showDialog(null, settingsFileData);
			 break;
		}
		return newData;
	}

	/**
	 * @return the initialSettingsList
	 */
	public ArrayList<PamControlledUnitSettings> getInitialSettingsList() {
		return initialSettingsList;
	}

	/**
	 * Get the current scaling factor
	 *
	 * @return
	 */
	public double getCurrentDisplayScaling() {
		return settingsFileData.getScalingFactor();
	}

	/**
	 * @param frame
	 */
	public void scalingFactorDialog(JFrame frame) {
		double newScalingFactor = DisplayScalingDialog.showDialog(frame, settingsFileData.getScalingFactor());
		if (newScalingFactor != 0 && newScalingFactor != settingsFileData.getScalingFactor()) {
//			scaleDisplay(newScalingFactor);
			settingsFileData.setScalingFactor(newScalingFactor);

			String message = "<html>You have changed PAMGuard's display scaling.  Please restart PAMGuard in order to update your display.</html>";
			int ans = WarnOnce.showWarning(frame, "New Display Scaling", message, WarnOnce.OK_OPTION);
		}

	}

	/**
	 * List of settings owners has moved to PAMConfiguration. This is so that when loading 
	 * a secondary config in batch mode, the 'owners' can be redirected to a different
	 * configuration. Ideally, just about everything in this entire class would move 
	 * to PAMConfiguration, but don't want to break the static registersettings function in 
	 * every module. 
	 * @return the owners
	 */
	public ArrayList<PamSettings> getOwners() {
		if (secondaryConfiguration != null) {
			return secondaryConfiguration.getSettingsOwners();
		}
		//otherwise return the main list from the main configuration held by PamController. 
		return PamController.getInstance().getPamConfiguration().getSettingsOwners();
	}

	/**
	 * @return the secondaryConfiguration
	 */
	public PamConfiguration getSecondaryConfiguration() {
		return secondaryConfiguration;
	}

	/**
	 * <b>Warning - fragile code. Use very sparingly!</b><br>
	 * A secondary configuration to use when loading configs into 
	 * batch mode for viewing and extracting offline tasks. This is a 
	 * real bodge and bad style, but can't do much about it at this stage. 
	 * USe very sparingly and make sure it's set null once the external batch
	 * configuration is loaded. 
	 * @param secondaryConfiguration the secondaryConfiguration to set
	 */
	public void setSecondaryConfiguration(PamConfiguration secondaryConfiguration) {
		this.secondaryConfiguration = secondaryConfiguration;
	}
	
	/**
	 * Check log files at PAMGuard startup ? 
	 * @return
	 */
	public boolean isCheckLogFileErrors() {
		if (settingsFileData == null) {
			return false;
		}
		return settingsFileData.getCheckLogFileErrors();
	}
	
	/**
	 * Check log files for errors at PAMGuard startup
	 * @param check
	 */
	public void setCheckLogFileErrors(boolean check) {
		if (settingsFileData != null) {
			settingsFileData.setCheckLogFileErrors(check);
		}
	}

}
