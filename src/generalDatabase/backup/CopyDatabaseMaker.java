package generalDatabase.backup;

import backupmanager.action.ActionMaker;
import backupmanager.action.BackupAction;
import backupmanager.stream.BackupStream;

public class CopyDatabaseMaker extends ActionMaker {

	public CopyDatabaseMaker() {
		super("Copy database", CopyDatabaseFile.class);
	}

	@Override
	public BackupAction createAction(BackupStream backupStream) {
		return new CopyDatabaseFile(this, backupStream);
	}

	@Override
	public String getToolTip() {
		return "<html>Copy the database file into a different folder<br>(a date will be appended to the database name)" +
	"<br>Note that if the databsase updated while copying, it can become corrupted</html>";
	}
}
