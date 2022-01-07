package backupmanager.filter;

import java.util.List;

import backupmanager.BackupFunction;
import backupmanager.BackupManager;
import backupmanager.action.BackupAction;
import backupmanager.stream.StreamItem;

abstract public class BackupFilter implements BackupFunction {

	public String filterName;
	private BackupAction backupAction;
	
	public BackupFilter(BackupAction backupAction, String filterName) {
		super();
		this.backupAction = backupAction;
		this.filterName = filterName;
	}

	public BackupAction getBackupAction() {
		return backupAction;
	}
	
	/**
	 * Filter all items in the list. 
	 * @param streamItems list of stream items, will have been sorted. 
	 * @return true if preparation went OK, e.g false if a database query failed, but true if it returned no records. 
	 */
	public abstract boolean runFilter(BackupManager backupManager, List<StreamItem> streamItems);
	

	/**
	 * 
	 * @return filter name
	 */
	@Override
	public String getName() {
		return filterName;
	}

	/**
	 * Set params called from parent BackupAction when it receives its params
	 * @param backupFilterParams
	 */
	public abstract void setFilterParams(BackupFilterParams backupFilterParams);
	
	/**
	 * Get filter params to save with the BackupAction parameters. 
	 * @return
	 */
	public abstract BackupFilterParams getFilterParams();

	/**
	 * Flag all items as passed 
	 * @param streamItems
	 */
	public void passEverything(List<StreamItem> streamItems) {
		passEverything(streamItems, "No Filter");
	}
	
	/**
	 * Flag all items as passed 
	 * @param streamItems List of stream items. 
	 * @param message message to write into all stream items
	 */
	public void passEverything(List<StreamItem> streamItems, String message) {
		for (StreamItem streamItem : streamItems) {
			streamItem.setFilterMessage(message);
			streamItem.setProcessIt(true);
		}
	}
	/**
	 * Flag all items as not passed 
	 * @param streamItems
	 */
	public void unPassEverything(List<StreamItem> streamItems) {
		unPassEverything(streamItems, null);
	}
	
	/**
	 * Flag all items as passed 
	 * @param streamItems List of stream items. 
	 * @param message message to write into all stream items
	 */
	public void unPassEverything(List<StreamItem> streamItems, String message) {
		for (StreamItem streamItem : streamItems) {
			streamItem.setFilterMessage(message);
			streamItem.setProcessIt(false);
		}
	}
	
	
}
