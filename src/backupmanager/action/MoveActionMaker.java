package backupmanager.action;

import backupmanager.stream.BackupStream;

public class MoveActionMaker extends ActionMaker {

	public MoveActionMaker() {
		super("Move Files", MoveFile.class);
	}

	@Override
	public BackupAction createAction(BackupStream backupStream) {
		return new MoveFile(this, backupStream);
	}

	@Override
	public String getToolTip() {
		return "Move folders of files to a different drive or folder (original files will be deleted)";
	}
}
