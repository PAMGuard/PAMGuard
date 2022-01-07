package SoundRecorder.backup;

import PamController.PamController;
import SoundRecorder.RecorderControl;
import backupmanager.FileLocation;
import backupmanager.stream.FileBackupStream;

public class RecorderBackupStream extends FileBackupStream {

	private RecorderControl recorderControl;

	public RecorderBackupStream(RecorderControl recorderControl) {
		super(recorderControl, recorderControl.getUnitName());
		this.recorderControl = recorderControl;
	}

	@Override
	public FileLocation getSourceLocation() {
		FileLocation fileLocation = new FileLocation();
		String path = recorderControl.getRecorderSettings().outputFolder;
		fileLocation.path = path;
//		fileLocation.fileOrFolders = FileLocation.SEL_FILES;
		fileLocation.mask = null;
		fileLocation.canEditMask = false;
		fileLocation.canEditPath = false;
		return fileLocation;
	}

	@Override
	public void setSourceLocation(FileLocation fileLocation) {
		// TODO Auto-generated method stub

	}

	@Override
	public long getMinBackupDelay() {
		if (PamController.getInstance().getPamStatus() == PamController.PAM_IDLE) {
			return 0;
		}
		else {
			/*
			 *  you'd hope that any wav file that is being written will have been very recently modified
			 *  bin any case, so this isn't really needed. 
			 */
			return recorderControl.getRecorderSettings().maxLengthSeconds*1100; // 1.1 file lengths
		}
	}
}
