package generalDatabase.backup;

import backupmanager.action.ActionMaker;
import backupmanager.action.BackupAction;
import backupmanager.stream.BackupStream;
import generalDatabase.DBControlUnit;

public class SQLCloneMaker extends ActionMaker {

	private DBControlUnit dbControlUnit;

	public SQLCloneMaker() {
		super("Clone database", SQLCloneDatabase.class);
	}

	@Override
	public BackupAction createAction(BackupStream backupStream) {
		return new SQLCloneDatabase(this, backupStream);
	}

	@Override
	public String getToolTip() {
		return "<html>Copy the database file into a different folder using database queries<br>(a date will be appended to the database name)" +
	"<br>This is safer than the file copy option!</html>";
	}
}
