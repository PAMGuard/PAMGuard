package backupmanager.action;

import java.awt.Window;

import PamController.PamController;
import PamView.dialog.PamDialogPanel;
import backupmanager.BackupFunction;
import backupmanager.BackupManager;
import backupmanager.filter.BackupFilter;
import backupmanager.settings.ActionSettings;
import backupmanager.stream.BackupStream;
import backupmanager.stream.StreamItem;
import backupmanager.swing.GenericBackupDialog;

public abstract class BackupAction implements BackupFunction {
	
	private BackupStream backupStream;
	private ActionMaker actionMaker;
	
	private BackupFilter backupFilter;
	
	public BackupAction(ActionMaker actionMaker, BackupStream backupStream) {
		super();
		this.backupStream = backupStream;
		this.actionMaker = actionMaker;
	}

	/**
	 * Perform a backup action. Return false or throw an exception if the action fails. 
	 * @param backupManager 
	 * @param streamInformation
	 * @param streamItem
	 * @return
	 * @throws BackupException
	 */
	public abstract boolean doAction(BackupManager backupManager, BackupStream backupStream, StreamItem streamItem) throws BackupException;
	
	/**
	 * Get settings associated with this action. <br> All actions must return
	 * settings even if they have nothing in them, since the settings are needed
	 * to recreate the action based on it's class name. 
	 * @return action settings
	 */
	public abstract ActionSettings getSettings();
	
	/**
	 * Set settings. Will return false if they were the wrong type for
	 * this action - though that should not be possible. 
	 * @param settings
	 * @return true if settings are OK. 
	 */
	public abstract boolean setSettings(ActionSettings settings);
	
	/**
	 * @return a dialog panel for controlling any of the settings. 
	 */
	public boolean showDialog(Window owner) {
		if (owner == null) {
			owner = PamController.getMainFrame();
		}
		return GenericBackupDialog.showDialog(owner, getName(), getBackupStream(), this, getBackupFilter());
	}
	
	/**
	 * flag to say whether or not to run this action if any previous 
	 * action threw an error. Actions such as a file copy would probably 
	 * want to run anyway, since may be copying to different disks and if one
	 * is full, you'd want to copy to the other. But if you'd failed to 
	 * copy a file to a backup drive, you probably wouldn't want to delete it!
	 * @return true if it's OK to run this action when a previous action failed. 
	 */
	public abstract boolean runIfPreviousActionError();
	
	/**
	 * Get a dialog panel for this action to incorporate into 
	 * a larger dialog. 
	 * @param owner
	 * @return dialog panel (assuming most actions have one - can return null).
	 */
	public abstract PamDialogPanel getDialogPanel(Window owner);
	
	/**
	 * @return a name for the action, to display in dialogs
	 */
	public abstract String getName();

	/**
	 * @return the backupStream
	 */
	public BackupStream getBackupStream() {
		return backupStream;
	}

	/**
	 * @return the actionMaker
	 */
	public ActionMaker getActionMaker() {
		return actionMaker;
	}

	public BackupFilter getBackupFilter() {
		return backupFilter;
	}

	public void setBackupFilter(BackupFilter backupFilter) {
		this.backupFilter = backupFilter;
	}
	
	public String getSpace() {
		return null;
	}
}
