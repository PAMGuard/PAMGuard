package backupmanager.network;

public class TransferLoginException extends Exception{
	
	public TransferLoginException(Exception e) {
		super(e);
	}
	
	public TransferLoginException(String mesage) {
		super(mesage);
	}
	
	public TransferLoginException(String message, Exception e) {
		super(message, e);
	}

}
