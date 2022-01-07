package binaryFileStorage.backup;

import java.io.Serializable;

import backupmanager.FileLocation;

public class BinaryBackupParams implements Serializable, Cloneable {
	
	public static final long serialVersionUID = 1L;
		
	public FileLocation destLocation;

	@Override
	protected BinaryBackupParams clone() {
		try {
			return (BinaryBackupParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
