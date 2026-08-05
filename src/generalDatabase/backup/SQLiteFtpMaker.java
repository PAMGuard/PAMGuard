package generalDatabase.backup;

import backupmanager.action.ActionMaker;
import backupmanager.action.BackupAction;
import backupmanager.stream.BackupStream;
import generalDatabase.DBControlUnit;

public class SQLiteFtpMaker extends ActionMaker{
	
	private DBControlUnit dbControlUnit;

	public SQLiteFtpMaker() {
		super("FTP Backup database", SQLiteSafeFTPBackup.class);
	}

	@Override
	public BackupAction createAction(BackupStream backupStream) {
		return new SQLiteSafeFTPBackup(this, backupStream);
	}

	@Override
	public String getToolTip() {
		return "<html>Copy the database file into a different folder using database queries"
				+ "<br>(a date will be appended to the database name)"
				+ "<br>then transmit to cloud storage using FTP."
				+ "<br>the clone of the local database will be deleted from the local harddrive if clone and transfer success" +
	"<br>This is safer than the file copy option!</html>";
	}

}
