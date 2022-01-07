package backupmanager.settings;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import backupmanager.FileLocation;

public class FileBackupSettings extends BackupSettings implements ManagedParameters {

	public static final long serialVersionUID = 1L;

	public FileLocation sourceLocation;
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		return ps;
	}

	
}
