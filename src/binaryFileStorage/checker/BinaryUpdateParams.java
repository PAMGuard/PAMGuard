package binaryFileStorage.checker;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

import PamguardMVC.PamDataBlock;

/**
 * Parameters controlling updates. 
 * @author dg50
 *
 */
public class BinaryUpdateParams implements Serializable {

	public static final long serialVersionUID = 1L;

	private HashMap<String, BinaryUpdateSet> updateSets = new HashMap<>();
	
	private boolean useSameFolder;
	
	private String newFolderName;
	
	public void setUpdateSet(PamDataBlock dataBlock, BinaryUpdateSet updateSet) {
		updateSets.put(dataBlock.getLongDataName(), updateSet);
	}
	
	public BinaryUpdateSet getUpdateSet(PamDataBlock dataBlock) {
		BinaryUpdateSet us = updateSets.get(dataBlock.getLongDataName());
		if (us == null) {
			us = new BinaryUpdateSet();
			updateSets.put(dataBlock.getLongDataName(), us);
		}
		return us;
	}
	
	public int getSelBlockCount() {
		Set<String> keys = updateSets.keySet();
		int n = 0;
		for (String aKey : keys) {
			BinaryUpdateSet us = updateSets.get(aKey);
			if (us.update) {
				n++;
			}
		}
		return n;
	}

	/**
	 * @return the useSameFolder
	 */
	public boolean isUseSameFolder() {
		return useSameFolder;
	}

	/**
	 * @param useSameFolder the useSameFolder to set
	 */
	public void setUseSameFolder(boolean useSameFolder) {
		this.useSameFolder = useSameFolder;
	}

	/**
	 * @return the newFolderName
	 */
	public String getNewFolderName() {
		return newFolderName;
	}

	/**
	 * @param newFolderName the newFolderName to set
	 */
	public void setNewFolderName(String newFolderName) {
		this.newFolderName = newFolderName;
	}
}
