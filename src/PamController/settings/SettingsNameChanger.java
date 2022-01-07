package PamController.settings;

import generalDatabase.MySQLSystem;
import hfDaqCard.SmruDaqSystem;

import java.io.FileInputStream;
import java.io.Serializable;
import java.util.ArrayList;

import simulatedAcquisition.SimProcess;
import nidaqdev.NIDAQProcess;
import Acquisition.FolderInputSystem;
import Acquisition.RonaInputSystem;
import Acquisition.SoundCardParameters;
import Acquisition.SoundCardSystem;
import PamController.PamSettings;

/**
 * 
 * To improve complex module loading where settings may be saved by multiple sub-modules, in 
 * July 2015 many modules which had fixed settings had their settings names and types changed !
 * Therefore these modules won't have found their settings on the first go, so need to also check
 * against the alternate names defined for each class. 
 * <p>
 * It won't be possible to work out from the settingsUser.Class what changes may have been made 
 * since the class type has only recently been added to the store. Will primarily have to search on 
 * the old moduletype and perhaps run a check on type conversion and also the module name but not if
 * it was null - indicates it would have been varying.  
 * <p>
 * Mostly, the old settings had fixed types and names so can be recognised from both. 
 * 
 * @author Doug Gillespie
 *
 */
public class SettingsNameChanger {
	
	private static SettingsNameChanger singleInstance;
	
	private ArrayList<SettingsNameChange> nameChanges = new ArrayList<>();

	private SettingsNameChanger() {
		nameChanges.add(new SettingsNameChange(SoundCardSystem.class, "Sound Card System", null, "Acquisition System", "Sound Card System"));
		nameChanges.add(new SettingsNameChange(FileInputStream.class, "File Input System", null, "Acquisition System", "File Input System"));
		nameChanges.add(new SettingsNameChange(FolderInputSystem.class, "File Folder Analysis", null, "File Folder Acquisition System", "File Folder Acquisition System"));
		nameChanges.add(new SettingsNameChange(NIDAQProcess.class, "NI-DAQ Card System", null, "Acquisition System", "NI-DAQ Card System"));
		nameChanges.add(new SettingsNameChange(SmruDaqSystem.class, null, null, "SAIL DAQ Card", "SAIL DAQ Card"));
		nameChanges.add(new SettingsNameChange(SimProcess.class, "Simulated Data Sources", null, "Simulated Data DAQ", "Simulated Data DAQ"));
		nameChanges.add(new SettingsNameChange(RonaInputSystem.class, "Rona File Folder Analysis", null, "Rona File Folders", "Rona File Folders"));
		nameChanges.add(new SettingsNameChange(MySQLSystem.class, "MySQL Database System", null, "MySQL Database System", "MySQL Database System"));
	}

	public static synchronized SettingsNameChanger getInstance() {
		if (singleInstance == null) {
			singleInstance = new SettingsNameChanger();
		}
		return singleInstance;
	}
	
	/**
	 * Find a name change. The pamSettings object will have the new type and name in it.
	 * Once this is found, then we'll be able to search for settings based on the old name.  
	 * @param pamSettings
	 * @return
	 */
	public SettingsNameChange findNameChange(PamSettings pamSettings) {
//		Serializable settings = pamSettings.getSettingsReference();
//		if (settings != null && SoundCardParameters.class.isInstance(settings)) {
//			System.out.println("Sound card params !)");
//		}
		for (SettingsNameChange nC:nameChanges) {
			if (nC.seemsSame(pamSettings)) {
				return nC;
			}
		}
		return null;
	}
	
	
}
