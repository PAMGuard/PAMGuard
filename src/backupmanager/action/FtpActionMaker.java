package backupmanager.action;

import backupmanager.stream.BackupStream;

public class FtpActionMaker extends ActionMaker{
	
	public FtpActionMaker() {
		super("FTP File", FTPFile.class);
	}

	@Override
	public BackupAction createAction(BackupStream backupStream) {
		return new FTPFile(this, backupStream);
	}


	@Override
	public String getToolTip() {
		return "FTP File to Server. File will stay in place after transfer.";
	}

}
