package SoundRecorder.backup;

import java.io.Serializable;

import backupmanager.FileLocation;

public class RecorderBackupParams implements Serializable, Cloneable {
	
	public static final long serialVersionUID = 1L;
		
	public FileLocation destLocation;

	@Override
	protected RecorderBackupParams clone() {
		try {
			return (RecorderBackupParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

}
