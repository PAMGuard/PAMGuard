package backupmanager.action;

/**
 * Exception thrown when a backup operation fails. 
 * @author dg50
 *
 */
public class BackupException extends Exception {

	private static final long serialVersionUID = 1L;

	public BackupException(String message) {
		super(message);
	}
	
	public BackupException(String message, Throwable cause) {
		super(message,cause);
	}

	public BackupException(Exception e) {
		super(e);
	}

}
