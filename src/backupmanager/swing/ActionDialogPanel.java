package backupmanager.swing;

import PamView.dialog.PamDialogPanel;
import backupmanager.action.BackupAction;

public abstract class ActionDialogPanel implements PamDialogPanel {

	private BackupAction backupAction;

	public ActionDialogPanel(BackupAction backupAction) {
		super();
		this.backupAction = backupAction;
	}

	/**
	 * @return the backupAction
	 */
	public BackupAction getBackupAction() {
		return backupAction;
	};

}
