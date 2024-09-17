package PamController;

import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JFrame;

import PamguardMVC.PamDataBlock;
import generalDatabase.DBControlUnit;

public class StorageOptions implements PamSettings {
	
	private static StorageOptions singleInstance;
	
	private StorageParameters storageParameters = new StorageParameters();
	
	private StorageOptions() {
		PamSettingManager.getInstance().registerSettings(this);
	}
	
	public static StorageOptions getInstance() {
		if (singleInstance == null) {
			singleInstance = new StorageOptions();
		}
		return singleInstance;
	}

	public boolean showDialog(JFrame parentFrame) {
		StorageParameters newParams = StorageOptionsDialog.showDialog(parentFrame, storageParameters);
		if (newParams != null) {
			storageParameters = newParams.clone();
			setBlockOptions();
			return true;
		}
		else {
			return false;
		}
	}
	
	@Override
	public Serializable getSettingsReference() {
		return storageParameters;
	}

	@Override
	public long getSettingsVersion() {
		return StorageParameters.serialVersionUID;
	}

	@Override
	public String getUnitName() {
		return "PAMGUARD Storage Options";
	}

	@Override
	public String getUnitType() {
		return "PAMGUARD Storage Options";
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		storageParameters = ((StorageParameters) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}
	
	/**
	 * Set the options in available data blocks. 
	 */
	public void setBlockOptions() {
		ArrayList<PamDataBlock> blocks = PamController.getInstance().getDataBlocks();
		boolean doLog;
		for (PamDataBlock aBlock:blocks) {
			boolean haveDatabase = (DBControlUnit.findDatabaseControl() != null && aBlock.getLogging() != null);			
//			boolean haveBinary = (PamController.getInstance().findControlledUnit(BinaryStore.unitType) != null &&
//					aBlock.getBinaryDataSource() != null);	
			/*
			 * Change the definition of haveBinary slightly so that it just looks to see if there is a 
			 * capability to write to the binary store. This will stop clicks and whistles
			 * storing to the database by default
			 */
			boolean haveBinary = (aBlock.getBinaryDataSource() != null);
			doLog = storageParameters.isStoreDatabase(aBlock, !haveBinary);
			aBlock.setShouldLog(doLog);
			doLog = storageParameters.isStoreBinary(aBlock, true);
			aBlock.setShouldBinary(doLog);
		}
	}
	
	public void setStorageParameters(StorageParameters storageParameters) {
		this.storageParameters = storageParameters;
	}

	/**
	 * Get storage parameters settings. 
	 * @return the storage paramters settings
	 */
	public StorageParameters getStorageParameters() {
		return storageParameters;
	}
}
