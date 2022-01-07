package backupmanager;

/**
 * Observers of backup progress. 
 * @author dg50
 *
 */
public interface BackupObserver {

	/**
	 * Progress update. 
	 * @param backupProgress
	 * @return true to continue, false to cancel the backup. 
	 */
	public boolean update(BackupProgress backupProgress);
	
	/**
	 * configuration has changed. Will need to lay out displays. 
	 */
	public void configurationChange();
	
}
