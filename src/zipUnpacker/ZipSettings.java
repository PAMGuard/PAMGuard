package zipUnpacker;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamUtils.FileParts;
import PamUtils.PamCalendar;
import binaryFileStorage.BinaryStore;
import binaryFileStorage.BinaryStoreSettings;

public class ZipSettings implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;

	private boolean enable = true;

	private String archiveFolder;

	private String dataFolder;
	
	private ArrayList<String> doneFiles;

	@Override
	protected ZipSettings clone()  {
		try {
			return (ZipSettings) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @return the enable
	 */
	public boolean isEnable() {
		return enable;
	}

	/**
	 * @param enable the enable to set
	 */
	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	/**
	 * @return the archiveFolder
	 */
	public String getArchiveFolder() {
		// enable this when there is a dialog !
//		if (archiveFolder == null) {
			archiveFolder = getDataFolder();
			if (archiveFolder != null) {
				archiveFolder += FileParts.getFileSeparator() + "archive";
			}
//		}
		return archiveFolder;
	}

	/**
	 * @param archiveFolder the archiveFolder to set
	 */
	public void setArchiveFolder(String archiveFolder) {
		this.archiveFolder = archiveFolder;
	}
	
	public boolean isDoneFile(File aFile) {
		if (aFile == null || doneFiles == null) {
			return false;
		}
		FileParts fp = new FileParts(aFile);
		String name = fp.getFileNameAndEnd();
		for (String aName:doneFiles) {
			if (aName.equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	

	/**
	 * @return the dataFolder
	 */
	public String getDataFolder() {
		// enable this when there is a dialog !
//		if (dataFolder == null) {
			dataFolder = findStorageFolder();
//		}
		return dataFolder;
	}

	/**
	 * @param dataFolder the dataFolder to set
	 */
	public void setDataFolder(String dataFolder) {
		this.dataFolder = dataFolder;
	}

	/**
	 * Find the binary data store and use it's root as a default location. 
	 * @return binary store location minus it's last sub folder. 
	 */
	private String findStorageFolder() {
		// no point in looking for the binary store since it won't be there yet. 
		// Need to find it's settings. 
		BinaryStoreSettings bsSettings = findBinaryStoreSettings();
		if (bsSettings == null) {
			return null;
		}
		String bsFolder = bsSettings.getStoreLocation();
		if (bsFolder == null) {
			return null;
		}
		int lastSplit = bsFolder.lastIndexOf(FileParts.getFileSeparator());
		if (lastSplit > 0) {
			return bsFolder.substring(0, lastSplit);
		}
		else {
			return bsFolder;
		}
	}

	/**
	 * Try to find the binary store settings. First look in the binary store. Chances are
	 * that this won't be there yet, so will have to go to the settings manager to 
	 * try to find the correct settings. 
	 * @return Binary store settings. 
	 */
	private BinaryStoreSettings findBinaryStoreSettings() {
		try {
			BinaryStore binaryStore = (BinaryStore) PamController.getInstance().findControlledUnit(BinaryStore.defUnitType);
			if (binaryStore != null) {
				return binaryStore.getBinaryStoreSettings();
			}
			PamControlledUnitSettings binarySettings = PamSettingManager.getInstance().findSettingsForType(BinaryStore.defUnitType);
			if (binarySettings == null) {
				return null;
			}
			else return (BinaryStoreSettings) binarySettings.getSettings();
		}
		catch (Exception e) {
			return null;
		}
	}

	/**
	 * @return the doneFiles
	 */
	protected ArrayList<String> getDoneFiles() {
		return doneFiles;
	}

	/**
	 * @param doneFiles the doneFiles to set
	 */
	protected void setDoneFiles(ArrayList<String> doneFiles) {
		this.doneFiles = doneFiles;
	}


}
