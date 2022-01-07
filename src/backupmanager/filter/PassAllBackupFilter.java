package backupmanager.filter;

import java.awt.Window;
import java.util.List;

import PamView.dialog.PamDialogPanel;
import backupmanager.BackupManager;
import backupmanager.action.BackupAction;
import backupmanager.stream.StreamItem;

public class PassAllBackupFilter extends BackupFilter {

	public PassAllBackupFilter(BackupAction backupAction) {
		super(backupAction, "Select Everything");
	}

	@Override
	public PamDialogPanel getDialogPanel(Window owner) {
		return null;
	}

	@Override
	public boolean runFilter(BackupManager backupManager, List<StreamItem> streamItems) {
		this.passEverything(streamItems);
		return true;
	}

	@Override
	public void setFilterParams(BackupFilterParams backupFilterParams) {

	}

	@Override
	public BackupFilterParams getFilterParams() {
		return null;
	}

}
