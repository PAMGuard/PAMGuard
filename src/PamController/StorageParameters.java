package PamController;

import java.io.Serializable;
import java.util.ArrayList;

import PamguardMVC.PamDataBlock;

public class StorageParameters implements Cloneable, Serializable {


	public static final long serialVersionUID = 1L;
	
	private ArrayList<StoreChoice> storeChoices = new ArrayList<StoreChoice>();

	@Override
	public StorageParameters clone() {
		try {
			return (StorageParameters) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	public void setStorageOptions(PamDataBlock pamDataBlock, boolean storeDatabase, boolean storeBinary) {
		setStorageOptions(pamDataBlock.getLongDataName(), storeDatabase, storeBinary);
	}
	
	private void setStorageOptions(String dataName, boolean storeDatabase, boolean storeBinary) {
		StoreChoice storeChoice = findStoreChoice(dataName);
		if (storeChoice == null) {
			storeChoice = new StoreChoice(dataName);
			storeChoices.add(storeChoice);
		}
		storeChoice.binaryStore = storeBinary;
		storeChoice.database = storeDatabase;
	}

	
	public boolean isStoreDatabase(PamDataBlock pamDataBlock, boolean def) {
		return isStoreDatabase(pamDataBlock.getLongDataName(), def);
		
	}
	private boolean isStoreDatabase(String dataName, boolean def) {
		StoreChoice storeChoice = findStoreChoice(dataName);
		if (storeChoice == null) {
			return def;
		}
		return storeChoice.database;
	}

	public boolean isStoreBinary(PamDataBlock pamDataBlock, boolean def) {
		return isStoreBinary(pamDataBlock.getLongDataName(), def);
	}
	
	private boolean isStoreBinary(String dataName, boolean def) {
		StoreChoice storeChoice = findStoreChoice(dataName);
		if (storeChoice == null) {
			return def;
		}
		return storeChoice.binaryStore;
	}
	
	public StoreChoice findStoreChoice(PamDataBlock pamDataBlock) {
		return findStoreChoice(pamDataBlock.getLongDataName());
	}
	
	private StoreChoice findStoreChoice(String dataName) {
		for (int i = 0; i < storeChoices.size(); i++) {
			if (storeChoices.get(i).dataName.equals(dataName)) {
				return storeChoices.get(i);
			}
		}
		return null;
	}

	public class StoreChoice implements Cloneable, Serializable {
		private static final long serialVersionUID = 1L;
		public StoreChoice(String dataName) {
			this.dataName = dataName;
		}
		private String dataName;
		private boolean database;
		private boolean binaryStore;
		/**
		 * @return the dataName
		 */
		public String getDataName() {
			return dataName;
		}
		/**
		 * @param dataName the dataName to set
		 */
		public void setDataName(String dataName) {
			this.dataName = dataName;
		}
		/**
		 * @return the database
		 */
		public boolean isDatabase() {
			return database;
		}
		/**
		 * @param database the database to set
		 */
		public void setDatabase(boolean database) {
			this.database = database;
		}
		/**
		 * @return the binaryStore
		 */
		public boolean isBinaryStore() {
			return binaryStore;
		}
	}
}
