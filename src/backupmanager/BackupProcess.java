package backupmanager;

import PamguardMVC.PamProcess;

public class BackupProcess extends PamProcess {

	public BackupProcess(BackupManager backupManager) {
		super(backupManager, null);
	}

	@Override
	public void pamStart() {
	}

	@Override
	public void pamStop() {
	}

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
	}

}
