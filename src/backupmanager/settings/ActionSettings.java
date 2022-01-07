package backupmanager.settings;

import java.io.Serializable;

import backupmanager.filter.BackupFilterParams;

/**
 * Base class for action settings. Every action has to return action settings, even if it has 
 * no settings of it's own, since they are needed for some bookkeeping and more importantly to 
 * identify actions when settingss are reloaded. 
 * @author dg50
 *
 */
public class ActionSettings implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;
	
	/**
	 * Store the name as a string, sinc the class is not serializable. 
	 */
	private String className;
	
	/**
	 * Time of the last backup in milliseconds
	 */
	private long lastBackupTime;
	
	/**
	 * How long the last backup took. 
	 */
	private long lastBackupDuration;
	
	/**
	 * Parameters for a backup filter 
	 */
	private BackupFilterParams backupFilterParams;
	
	/**
	 * constructor for the base ActinSettings. A class is 
	 * needed since this is used when config is reloaded to 
	 * recreate the actions. 
	 * @param actionClass
	 */
	public ActionSettings(Class actionClass) {
		this(actionClass.getName());
	}
	/**
	 * classname must be the full name of the class that uses these settings. 
	 * @param className
	 */
	public ActionSettings(String className) {
		super();
		this.className = className;
	}
	/**
	 * 
	 * @return a class name for an action. 
	 */
	public String getActionClass() {
		return className;
	}
	
	/**
	 * Time of the last backup in milliseconds
	 * @return the lastBackupTime
	 */
	public long getLastBackupTime() {
		return lastBackupTime;
	}
	/**
	 * Time of the last backup in milliseconds
	 * @param lastBackupTime the lastBackupTime to set
	 */
	public void setLastBackupTime(long lastBackupTime) {
		this.lastBackupTime = lastBackupTime;
	}
	/**
	 * @return the lastBackupDuration
	 */
	public long getLastBackupDuration() {
		return lastBackupDuration;
	}
	/**
	 * @param lastBackupDuration the lastBackupDuration to set
	 */
	public void setLastBackupDuration(long lastBackupDuration) {
		this.lastBackupDuration = lastBackupDuration;
	}
	public BackupFilterParams getBackupFilterParams() {
		return backupFilterParams;
	}
	public void setBackupFilterParams(BackupFilterParams backupFilterParams) {
		this.backupFilterParams = backupFilterParams;
	}
}
