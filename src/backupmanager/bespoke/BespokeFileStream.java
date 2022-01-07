package backupmanager.bespoke;

import PamController.SettingsNameProvider;
import backupmanager.FileLocation;
import backupmanager.settings.BackupSettings;
import backupmanager.settings.FileBackupSettings;
import backupmanager.stream.FileBackupStream;

public class BespokeFileStream extends FileBackupStream {

	
	public BespokeFileStream(SettingsNameProvider settingsName, String name) {
		super(settingsName, name);
	}

	@Override
	public FileLocation getSourceLocation() {
		FileLocation sourceLocation = getBackupSettings().sourceLocation;
		if (sourceLocation == null) {
			sourceLocation = new FileLocation();
		}
		sourceLocation.canEditMask = true;
		sourceLocation.canEditPath = true;
		return sourceLocation;
	}

	@Override
	public void setSourceLocation(FileLocation fileLocation) {
		getBackupSettings().sourceLocation = fileLocation;
	}

}
