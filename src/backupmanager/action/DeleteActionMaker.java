package backupmanager.action;

import backupmanager.stream.BackupStream;

public class DeleteActionMaker extends ActionMaker {

	public DeleteActionMaker() {
		super("Delete File", DeleteFile.class);
	}

	@Override
	public BackupAction createAction(BackupStream backupStream) {
		return new DeleteFile(this, backupStream);
	}


	@Override
	public String getToolTip() {
		return "Delete a file. No if's, no but's. Delete it !";
	}


}
