package backupmanager.database;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import backupmanager.action.BackupAction;
import backupmanager.stream.BackupStream;
import backupmanager.stream.StreamItem;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamTableItem;

/**
 * Catalogue data that are already backed up. Can do this by scanning 
 * the backup directory, but preferred option is to use the database since
 * this can hold more information about what is already backup up and what 
 * actions have been performed and why.  <p>
 * Order of operations for all the backing up using the database is
 * <br>1) List all files in source folder (in DataStream)
 * <br>2) Check table (can do in constructor)
 * <br>3) Get all files already in output database and cross check to make list of new files. 
 * This doesn't mean that the files have been backed up, just that they are catalogued 
 * <br>4) For each action, re-query the database to see which items have null in that action column
 * <br>5) run action on that item, and update that row of the database
 * <br>6) Repeat 4 & 5 for next action. 
 * <p>
 * If this was done without the database, it's harder !
 * <br> 1) List all files in source (step 1 above)
 * <br> 2) For each action , list all files in destination and make list of source files not in dest
 * <br> 3) Run action on each item
 * <br> 4) Repeat 2 and 3 for next action
 * <p>
 * 
 * @author dg50
 *
 */
public abstract class BackupCatalog {

	private BackupStream backupStream;

	private String source;
	

	public BackupCatalog(BackupStream backupStream, String source) {
		super();
		this.backupStream = backupStream;
		this.source = source;
	}

	/**
	 * If possible, save new items in the backup catalog. Doesn't look at output
	 * of actions yet. Used by the database to add to the backuptable. 
	 * @param allSourceItems all source items
	 * @return items added to the catalog (hopefully a subset). 
	 */
	public abstract List<StreamItem> catalogNewItems(List<StreamItem> allSourceItems);
	
	/**
	 * Get a list of items that have not been operated by the given action (may vary by action, 
	 * particularly if new actions are added). 
	 * @param sourceItems all potentially new items. 
	 * @param action 
	 * @return list of items that need acting on. 
	 */
	public abstract List<StreamItem> getUnactedItems(List<StreamItem> sourceItems, BackupAction action);
	
	/**
	 * Once an action is complete, store it's result (if possible)
	 * @param streamItem
	 * @param action
	 * @return
	 */
	public abstract boolean updateItem(StreamItem streamItem, BackupAction action);

	/**
	 * @return the backupStream
	 */
	public BackupStream getBackupStream() {
		return backupStream;
	}

	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}
	
	/**
	 * Called when backup of a stream is complete so that catalogue
	 * can be closed. 
	 */
	public abstract void backupComplete();
	
}
