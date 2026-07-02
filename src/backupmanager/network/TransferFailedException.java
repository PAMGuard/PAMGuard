package backupmanager.network;

import backupmanager.action.BackupException;

public class TransferFailedException extends BackupException{
	
	public TransferFailedException(Exception e) {
		super(e);
	}
	
	public TransferFailedException(String mesage) {
		super(mesage);
	}
	
	public TransferFailedException(String message, Exception e) {
		super(message, e);
	}

}
