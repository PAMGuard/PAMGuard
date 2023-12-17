package PamController.settings;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import PamController.PSFXReadWriter;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.PamSettingsGroup;
import PamController.UsedModuleInfo;
import PamModel.PamModel;
import PamModel.PamModuleInfo;
import PamModel.SMRUEnable;
import PamView.dialog.PamFileBrowser;
import PamView.dialog.warn.WarnOnce;

/**
 * Class to handle the import of settings from other psf files. 
 * @author Doug Gillespie
 *
 */
public class SettingsImport {

	public SettingsImport(PamController pamController) {
	}

	/**
	 * Allow user to select a psf file and then import settings from it
	 * @return true if settings loaded. 
	 */
	public boolean loadSettingsFile() {
		//		First select a psf file
		String[] exts = {".psfx"};
		String settingsFile = PamFileBrowser.fileBrowser(PamController.getMainFrame(), null, PamFileBrowser.OPEN_FILE, exts, "PAMGuard Settings Files");
		if (settingsFile == null) {
			return false;
		}
		return selectSettings(settingsFile);
	}

	/**
	 * Select and load settings from a file. Generally loading settings for 
	 * complete modules, but there are a few other things worth importing, i.e. 
	 * Storage Options-PAMGUARD Storage Options (probably not since may get poor match with different modules)
	 * PamSymbolManager-PamSymbolManager - again, may get poor match with different modules so may screw up. 
	 * Pam Color Manager-Pam Color Manager - currently only contains day/night flag, so useless until we add ability to edit colours
	 * 
	 * @param settingsFile psf file name
	 * @return true fi settings loaded. 
	 */
	private boolean selectSettings(String settingsFile) {
		/*
		 * Load a list of settings objects from the file. 
		 */
		ArrayList<PamControlledUnitSettings> settings = readSettingsFile(settingsFile);
		if (settings == null) {
			return false;
		}

		File f = new File(settingsFile);
		ArrayList<SettingsImportGroup> groupedSettings = organiseSettingsGroups(settings);

		boolean ans = SettingsImportDialog.showDialog(PamController.getMainFrame(), f.getName(), groupedSettings);
		if (ans == false) {
			return false;
		}
		/**
		 * OK - so we get here and need to actually import some stuff. Wow !
		 */
		PamControlledUnit newUnit;
		ArrayList<PamControlledUnit> newUnits = new ArrayList<>();
		for (SettingsImportGroup aGroup:groupedSettings) {
			ImportChoice importChoice = aGroup.getImportChoice();
			if (importChoice == null){
				continue;
			}
			newUnit = null;
			switch(importChoice.getImportChoice()) {
			case ImportChoice.DONT_IMPORT:
				break;
			case ImportChoice.IMPORT_NEW:
				newUnit = importNew(aGroup);
				break;
			case ImportChoice.IMPORT_REPLACE:
				newUnit = importReplace(aGroup, importChoice.getReplaceModule());
				break;
			}
			if (newUnit != null) {
				newUnits.add(newUnit);
			}
		}
		
		/*
		 * Send round an initialisation complete notification - since this gets sent after normal 
		 * config loading. May create havoc in which case will have to do a new one and implement where necessary. 
		 */
		for (PamControlledUnit aUnit:newUnits) {
			aUnit.notifyModelChanged(PamController.INITIALIZATION_COMPLETE);
		}

		return true;
	}

	/**
	 * Replace the settings in an existing module. 
	 * @param importGroup Import group information
	 * @param replaceModule Existing module to replace. 
	 */
	private PamControlledUnit importReplace(SettingsImportGroup importGroup, String replaceModule) {
		PamControlledUnitSettings mainSet = importGroup.getMainSettings();
		PamControlledUnit unit = PamController.getInstance().findControlledUnit(mainSet.getUnitType(), replaceModule);
		if (unit == null) {
			System.out.println("Unable to find " + mainSet.getUnitType() + " " + mainSet.getUnitName() + " for settings replacement");
			return null; 
		}
		// check we can cast it to PamSettings
		if (PamSettings.class.isAssignableFrom(unit.getClass())) {
			try {
				((PamSettings) unit).restoreSettings(mainSet);
			}
			catch (Exception e) {
				String str = "Error updating settings in " + mainSet.getUnitType() + " " + mainSet.getUnitName() + " during import";
				System.err.println(str);
				System.err.println(e.getMessage());
			}
		}
		loadSubUnitSettings(importGroup, mainSet.getUnitName());
		return unit;
	}

	/**
	 * Load settings into any other PamSEttings classes that have the same unitName association. 
	 * @param importGroup group of imported settings data. 
	 * @param unitName Unit Name - all in group hav ehte same name, but if overwriting an existing module then
	 * this may not be the default from the importGroup. 
	 */
	private void loadSubUnitSettings(SettingsImportGroup importGroup, String unitName) {
		ArrayList<PamControlledUnitSettings> subSets = importGroup.getSubSettings();
		if (subSets == null) {
			return;
		}
		PamSettingManager setManager = PamSettingManager.getInstance();
		for (PamControlledUnitSettings pamSettings:subSets) {
			PamSettings owner = setManager.findSettingsOwner(pamSettings.getUnitType(), unitName, pamSettings.getOwnerClassName());
			if (owner == null) {
				System.out.println(String.format("Cannot find settings owner for %s %s in current model", pamSettings.getUnitType(), unitName));
				continue;
			}
			try {
				pamSettings.setUnitName(unitName);
				owner.restoreSettings(pamSettings);
			}
			catch (Exception e) {
				String str = "Error updating settings in " + pamSettings.getUnitType() + " " + pamSettings.getUnitName() + " during import";
				System.err.println(str);
				System.err.println(e.getMessage());
			}
		}
	}

	private PamControlledUnit importNew(SettingsImportGroup importGroup) {
		PamControlledUnitSettings mainSet = importGroup.getMainSettings();
		String moduleName = mainSet.getUnitName();

		// check we've got a name that doesnt' exist and replace it if if does. 
		//		int startChar = 0;
		//		while (true) {
		//			PamControlledUnit unit = PamController.getInstance().findControlledUnit(mainSet.getUnitType(), moduleName);
		//			if (unit == null) {
		//				break;
		//			}
		//			moduleName = String.format("%s_%d", mainSet.getUnitName(), ++startChar);
		//		}
		/**
		 * Now make a new Pamcontrolled unit with the given name ...
		 */
		// need to find the module information in the PamModel
//		PamModel pamModel = PamModel.getPamModel();
//		pamModel.
		PamModuleInfo moduleInfo = PamModuleInfo.findModuleInfo(importGroup.getUsedModuleInfo().className);
		// find the module info for this one
//		PamModuleInfo moduleInfo = importGroup.getUsedModuleInfo();
		if (moduleInfo == null) {
			String msg = String.format("Unable to find module information for type %s main class %s in model",
					importGroup.getUsedModuleInfo().getUnitType(), importGroup.getUsedModuleInfo().className);
			WarnOnce.showWarning("Module creating error!", msg, WarnOnce.WARNING_MESSAGE);
			return null;
		}

		moduleInfo.setDefaultName(moduleName);
		
		PamControlledUnit unit = PamController.getInstance().addModule(PamController.getMainFrame(), moduleInfo);
		if (unit == null) {
			System.out.println("Unable to find " + mainSet.getUnitType() + " " + mainSet.getUnitName() + " for settings replacement");
			return null; 
		}
		// check we can cast it to PamSettings
		if (PamSettings.class.isAssignableFrom(unit.getClass())) {
			try {
				mainSet.setUnitName(unit.getUnitName()); // need to force the unit name for some modules. 
				((PamSettings) unit).restoreSettings(mainSet);
			}
			catch (Exception e) {
				String str = "Error updating settings in " + mainSet.getUnitType() + " " + unit.getUnitName() + " during import";
				System.err.println(str);
				System.err.println(e.getMessage());
			}
		}
		loadSubUnitSettings(importGroup, unit.getUnitName());
		unit.setupControlledUnit();
		return unit;
	}

	/**
	 * Arrange settings into groups by module. 
	 * @param settings
	 * @return
	 */
	ArrayList<SettingsImportGroup> organiseSettingsGroups(ArrayList<PamControlledUnitSettings> settings) {
		/**
		 * this needs rewriting for psfx files which are organised differently. first we need to find 
		 * a list of PAMGuard modules by finding the settings group of the PAMController. 
		 */	
		
		ArrayList<SettingsImportGroup> groupedSettings = new ArrayList<>();
		ArrayList<UsedModuleInfo> usedModules = findPamControllerSettings(settings);
		// make the group list based on the list of modules. 
		for (UsedModuleInfo usedModule : usedModules) {
			groupedSettings.add(new SettingsImportGroup(usedModule));
		}
			
		
//		// first pull out the settings for PamControlledNnits. 
		boolean[] used = new boolean[settings.size()];
//		for (int i = 0; i < settings.size(); i++) {
//			PamControlledUnitSettings aSet = settings.get(i);
//			if (aSet.getOwnerClassName() == null) {
//				continue;
//			}
//			Class ownerClass = null;
//			try {
//				ownerClass = Class.forName(aSet.getOwnerClassName());
//			} catch (ClassNotFoundException e) {
//				// TODO Auto-generated catch block
////				e.printStackTrace();
//				// this is happening since the ownerclassname is not set correctly in psfx files
//				// so we have to deserialise the data to find the class. 
////				ownerClass = getClassFromData(aSet.getSerialisedByteArray());
////				ownerClass = PamModuleInfo.findModuleClass(aSet.getUnitType());
//			}
//			if (ownerClass == null) {
//				continue;
//			}
//			if (PamControlledUnit.class.isAssignableFrom(ownerClass)) {
//				PamModuleInfo moduleInfo = PamModuleInfo.findModuleInfo(aSet.getOwnerClassName());
//				groupedSettings.add(new SettingsImportGroup(moduleInfo));
//				used[i] = true;
//			}
//		}

		// now match all the remaining settings into the first set based on ModuleName. 
		for (int i = 0; i < settings.size(); i++) {
			PamControlledUnitSettings aSet = settings.get(i);
//			if (used[i]) continue;
			SettingsImportGroup mainGroup = findGroup(groupedSettings, aSet.getUnitName());
			if (mainGroup != null) {
				// main settings will have same type as well as same name. 
				boolean mainType = isMainType(mainGroup, aSet);
				if (mainType) {
					mainGroup.setMainSettings(aSet);
				}
				else {
					mainGroup.addSubSettings(aSet);
				}
				used[i] = true;
//				System.out.println(String.format("Adding %s-%s to %s-%s group", aSet.getUnitType(), aSet.getUnitName(), 
//						mainGroup.getMainSettings().getUnitType(), mainGroup.getMainSettings().getUnitName()));
			}
		}
		
		/*
		 * Now go through a final time and see what's not used. 
		 */
		for (int i = 0; i < settings.size(); i++) {
			PamControlledUnitSettings aSet = settings.get(i);
			if (used[i]) continue;
			if (SMRUEnable.isEnable()) {
				System.out.println(String.format("Ungrouped settings %s-%s ", aSet.getUnitType(), aSet.getUnitName()));			
			}
		}
		

		return groupedSettings;
	}
	
	/**
	 * IS this the main settings group for this module ? If it is, it should have the same 
	 * type as well as the same name. 
	 * @param mainGroup
	 * @param aSet
	 * @return
	 */
	private boolean isMainType(SettingsImportGroup mainGroup, PamControlledUnitSettings aSet) {
		boolean isMain = mainGroup.getUsedModuleInfo().getUnitType().equals(aSet.getUnitType());
		return isMain;
	}

	private ArrayList<UsedModuleInfo> findPamControllerSettings(ArrayList<PamControlledUnitSettings> settings) {
		if (settings == null) {
			return null;
		}
		for (PamControlledUnitSettings aSet : settings) {
			if (aSet.getUnitName().equals(PamController.unitName) && 
					aSet.getUnitType().equals(PamController.unitType)) {
				Object sets = aSet.getSettings();
				if (sets instanceof ArrayList) {
					return (ArrayList<UsedModuleInfo>) sets;
				}
			}
		}
		return null;
	}

	private Class getClassFromData(byte[] data) {
		try {
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
			Object obj = ois.readObject();
			return obj.getClass();
		} catch (Exception e) {
			return null;
		}
		
	}

	private SettingsImportGroup findGroup(ArrayList<SettingsImportGroup> groupedSettings, String unitName) {
		for (SettingsImportGroup iG:groupedSettings) {
			if (iG.getUsedModuleInfo().unitName.equals(unitName)) {
				return iG;
			}
		}
		return null;
	}

	/**
	 * Read a psf settings file. 
	 * @param settingsFile
	 * @return
	 */
	ArrayList<PamControlledUnitSettings> readSettingsFile(String settingsFile) {
		if (settingsFile == null) {
			return null;
		}
		if (settingsFile.endsWith(PamSettingManager.fileEnd)) {
			return readPSFFile(settingsFile);
		}
		if (settingsFile.endsWith(PamSettingManager.fileEndx)) {
			return readPSFXFile(settingsFile);
		}
		return null;
	}

	/**
	 * Load settings from a psfx file 
	 * @param settingsFile file name
	 * @return Array list of settings
	 */
	private ArrayList<PamControlledUnitSettings> readPSFXFile(String settingsFile) {
		PamSettingsGroup psg = PSFXReadWriter.getInstance().loadFileSettings(new File(settingsFile));
		if (psg == null) {
			return null;
		}
		else {
			return psg.getUnitSettings();
		}
	}

	/**
	 * Load settings from a psf file 
	 * @param settingsFile file name
	 * @return Array list of settings
	 */
	private ArrayList<PamControlledUnitSettings> readPSFFile(String settingsFile) {
		File sFile = new File(settingsFile);
		if (sFile.exists() == false) {
			return null;
		}
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(sFile));
		} catch (IOException e) {
			System.err.println("Unable to open settings file " + settingsFile);
			return null;
		}
		ArrayList<PamControlledUnitSettings> settings = new ArrayList<>();
		Object j;
		PamControlledUnitSettings newSetting;
		while (true) {
			try {
				j = ois.readObject();
				newSetting = (PamControlledUnitSettings) j;
				settings.add(newSetting);
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
				// print and continue - there may be other things we can deal with.
				Ex.printStackTrace();
			}
			catch (Exception Ex) {
				Ex.printStackTrace();
			}
		}
		return settings;
	}
}
