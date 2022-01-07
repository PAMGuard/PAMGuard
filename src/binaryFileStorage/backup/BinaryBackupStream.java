package binaryFileStorage.backup;


import PamController.PamController;
import backupmanager.FileLocation;
import backupmanager.settings.BackupSettings;
import backupmanager.stream.FileBackupStream;
import binaryFileStorage.BinaryStore;
import binaryFileStorage.BinaryStoreSettings;

public class BinaryBackupStream extends FileBackupStream {
	
	private BinaryBackupParams params = new BinaryBackupParams();
	private BinaryStore binaryStore;

	public BinaryBackupStream(BinaryStore binaryStore) {
		super(binaryStore, binaryStore.getUnitName());
		this.binaryStore = binaryStore;
	}

	@Override
	public FileLocation getSourceLocation() {
		FileLocation sl = new FileLocation();
		BinaryStoreSettings binParams = binaryStore.getBinaryStoreSettings();
		sl.path = binParams.getStoreLocation();
//		sl.fileOrFolders = FileLocation.SEL_FILES;
		sl.mask = null;
		sl.canEditMask = false;
		sl.canEditPath = false;
		return sl;
	}

	@Override
	public void setSourceLocation(FileLocation fileLocation) {
		
	}

	@Override
	public long getMinBackupDelay() {
		if (PamController.getInstance().getPamStatus() == PamController.PAM_IDLE) {
			return 0;
		}
		else {
			/**
			 * binary files may not ever get modified between creation time and closing time if there
			 * are no detections, so make sure they aren't backed up until they are
			 * sure to have closed. 
			 */
			return binaryStore.getBinaryStoreSettings().fileSeconds * 1100; // 1.1 file lengths
		}
	}

}
