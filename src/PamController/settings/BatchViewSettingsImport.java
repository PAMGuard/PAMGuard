package PamController.settings;

import java.util.ArrayList;

import PamController.DataInputStore;
import PamController.OfflineDataStore;
import PamController.OfflineFileDataStore;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.PamSettingsGroup;
import PamController.UsedModuleInfo;
import PamModel.PamModuleInfo;

/**
 * A set of functions to handle importing and overriding settings imported from a psfx during 
 * batch mode processing in viewer mode. Some of this is quite similar to code in SettingsImport
 * but different enough that it's easier to have in a separate set of functions. 
 * @author dg50
 *
 */
public class BatchViewSettingsImport {

	private PamController pamController;
	private PamSettingsGroup settingsGroup;
	private SettingsImport settingsImport;

	public BatchViewSettingsImport(PamController pamController, PamSettingsGroup settingsGroup) {
		this.pamController = pamController;
		this.settingsGroup = settingsGroup;
		settingsImport = new SettingsImport(pamController);
	}
	
	public boolean importSettings() {
		
		// first organise by controlled unit
		ArrayList<SettingsImportGroup> moduleGroups = settingsImport.organiseSettingsGroups(settingsGroup.getUnitSettings());
		// can now go through those modules and see which exist and which need to be added. Not asking questions, just doing it!
		for (SettingsImportGroup moduleGroup : moduleGroups) {
			UsedModuleInfo moduleInfo = moduleGroup.getUsedModuleInfo();
			PamControlledUnit exModule = pamController.findControlledUnit(moduleInfo.getUnitType(), moduleInfo.getUnitName());
			boolean existingStore = false;
			if (exModule != null) {
				existingStore = isDataStore(exModule);
				System.out.printf("Module %s:%s already exists in model (data store: %s)\n", moduleInfo.getUnitType(), moduleInfo.getUnitName(), Boolean.valueOf(existingStore));
			}
			else {
				System.out.printf("Module %s:%s will be added to model\n", moduleInfo.getUnitType(), moduleInfo.getUnitName());
				// add the module. No questions asked. 
				PamModuleInfo pamModuleInfo = moduleGroup.getPamModuleInfo();
				if (pamModuleInfo == null) {
					System.out.printf("Module %s:%s is not available to this PAMGuard installation\n", moduleInfo.getUnitType(), moduleInfo.getUnitName());
					continue;
				}
				exModule = pamController.addModule(pamModuleInfo, moduleInfo.getUnitName());
			}
			// set the settings for that module, but only if it's NOT a data storage module. 
			if (exModule == null) {
				continue;
			}
//			if (exModule.getUnitName().contains("Noise")) {
//				System.out.printf("restoring settings for %s, %s\n", exModule.getUnitType(), exModule.getUnitName());
//				
//			}
			if (isDataStore(exModule)) {
				System.out.printf("Skip restoring settings for %s, %s\n", exModule.getUnitType(), exModule.getUnitName());
				continue;
			}
			restoreSettings(moduleGroup.getMainSettings());
			ArrayList<PamControlledUnitSettings> subSets = moduleGroup.getSubSettings();
			if (subSets != null) {
				for (PamControlledUnitSettings aSet : subSets) {
					restoreSettings(aSet);
				}
			}
//			exModule.notifyModelChanged(PamController.INITIALIZATION_COMPLETE);
//			if (exModule.getTabPanel() != null) {
////				exModule.getTabPanel()
//			}
		}
		/*
		 * Don't need to call this here since it get's called shortly 
		 * from PAMController once all modules are in place. 
		 */
//		pamController.notifyModelChanged(PamController.INITIALIZATION_COMPLETE);
		
		// send out an initialisation complete to help restore settings
//		ArrayList<PamControlledUnitSettings> allSettings = settingsGroup.getUnitSettings();
//		for (PamControlledUnitSettings aSet : allSettings) {
//			PamSettings owner = PamSettingManager.getInstance().findSettingsOwner(aSet.getUnitType(), aSet.getUnitName(), null);
//			if (owner == null) {
//				System.out.printf("Cannot find owner for %s, %s, %s\n", aSet.getUnitType(), aSet.getUnitName(), aSet.getOwnerClassName());
//			}
//			else {
//				owner.restoreSettings(aSet);
//			}
//		}
		
		return true;		
	}
	
	/**
	 * Find the owner of these settings and send it it's new settings. 
	 * @param aSet
	 * @return true if found and set sucessfully. 
	 */
	private boolean restoreSettings(PamControlledUnitSettings aSet) {
		PamSettings owner = PamSettingManager.getInstance().findSettingsOwner(aSet.getUnitType(), aSet.getUnitName(), null);
		if (owner == null) {
			System.out.printf("Cannot find owner for %s, %s, %s\n", aSet.getUnitType(), aSet.getUnitName(), aSet.getOwnerClassName());
			return false;
		}
		else {
			try {
				owner.restoreSettings(aSet);
			}
			catch (Exception e) {
				System.out.printf("Exception restoring settings %s, %s, %s\n", aSet.getUnitType(), aSet.getUnitName(), aSet.getOwnerClassName());
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
	

	/**
	 * Is the module a data store. If it is, we probably won't want to copy over it's settings. 
	 * @param controlledUnit PAMGuard module
	 * @return true if it's data output or input. 
	 */
	private boolean isDataStore(PamControlledUnit controlledUnit) {
		return isDataStore(controlledUnit.getClass());
	}

	/**
	 * Is the class a data store. If it is, we probably won't want to copy over it's settings. 
	 * @param moduleClass module class
	 * @return true if it's data output or input. 
	 */
	private boolean isDataStore(Class<? extends PamControlledUnit> moduleClass) {
		//OfflineFileDataStore, DataInputStore
		if (OfflineDataStore.class.isAssignableFrom(moduleClass)) {
			return true;
		}
		if (DataInputStore.class.isAssignableFrom(moduleClass)) {
			return true;
		}
		return false;
	}

}
