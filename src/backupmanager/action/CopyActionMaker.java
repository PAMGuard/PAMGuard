package backupmanager.action;


import backupmanager.stream.BackupStream;

public class CopyActionMaker extends ActionMaker {


	public CopyActionMaker() {
		super("Copy Files", CopyFile.class);
	}

	@Override
	public BackupAction createAction(BackupStream backupStream) {
		return new CopyFile(this, backupStream);
	}

	@Override
	public String getToolTip() {
		return "Copy folders of files to a different drive or folder (original files left in place)";
	}

}
