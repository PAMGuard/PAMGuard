package backupmanager.stream;

import java.awt.Window;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.SettingsNameProvider;
import PamView.dialog.PamDialogPanel;
import PamguardMVC.debug.Debug;
import backupmanager.BackupFunction;
import backupmanager.BackupManager;
import backupmanager.BackupProgress;
import backupmanager.BackupProgress.STATE;
import backupmanager.action.ActionMaker;
import backupmanager.action.BackupAction;
import backupmanager.action.BackupException;
import backupmanager.filter.BackupFilter;
import backupmanager.filter.PassAllBackupFilter;
import backupmanager.settings.ActionSettings;
import backupmanager.settings.BackupSettings;

/**
 * This is the central management class for each datastream that's going to get automatically backed up. 
 * It will have links to the stream information, database info, etc. and manage the calls on a schedule to 
 * process and backup data. 
 * @author dg50
 *
 */
public abstract class BackupStream implements PamSettings, BackupFunction {
	
	
	private ArrayList<BackupAction> actions = new ArrayList<BackupAction>();
	
	private String name;

	private SettingsNameProvider settingsName;
	
	public BackupStream(SettingsNameProvider settingsName, String name) {
		super();
		this.settingsName = settingsName;
		this.name = name;
		PamSettingManager.getInstance().registerSettings(this);
	}
	
	/**
	 * Run all backups associated with this stream. 
	 * There is a single SwingWorker running ALL backups in turn, 
	 * so this will already be running in a separate thread and it should 
	 * send updates back to the manager which can forward them onto observers
	 * in the AWT thread (I think). 
	 * These will have 
	 * @param backupManager 
	 * @return
	 */
	public boolean runBackup(BackupManager backupManager) {
		backupManager.updateProgress(new BackupProgress(this, null, STATE.CATALOGING));
		/*
		 * sourceItems is all items in list, many of which may already have been backed up, 
		 * but it's a useful list to have anyway. 
		 */
		List<StreamItem> sourceItems = catalogData();
		if (sourceItems == null) {
			return false;
		}
		boolean ok = true; 
		for (int i = 0; i < actions.size(); i++) {
			BackupAction action = actions.get(i);
			long t1 = System.currentTimeMillis();
			if (ok || action.runIfPreviousActionError()) {
				/*
				 * Only proceed if no errors or this action flagged to proceed anyway.  
				 */
				boolean actOk = runAction(backupManager, sourceItems, action);
				if (!actOk) {
					ok = false;
//					String errMsg ="Problem in " + action.getName();
				}
				action.getSettings().setLastBackupTime(System.currentTimeMillis());
				action.getSettings().setLastBackupDuration(System.currentTimeMillis()-t1);
			}
		}
		backupManager.updateProgress(new BackupProgress(this, null, STATE.STREAMDONE));
		
		backupComplete();
		
		return ok;
	}
	
	private boolean runAction(BackupManager backupManager, List<StreamItem> sourceItems, BackupAction action) {
		// need to find database entries that have a null or value for this action

		List<StreamItem> toDoList = getToDoList(sourceItems, action);
		
		BackupFilter backupFilter = action.getBackupFilter();
		if (backupFilter == null) {
			backupFilter = new PassAllBackupFilter(action);
		}
		backupFilter.runFilter(backupManager, toDoList);

		if (toDoList == null) {
			backupManager.updateProgress(new BackupProgress(this, action, STATE.STREAMDONE));
			return true;
		}
		backupManager.updateProgress(new BackupProgress(this, action, STATE.RUNNING, toDoList.size(), 0, ""));
		int iDone = 0;
		boolean ok = true;
		for (StreamItem item : toDoList) {
			++iDone;
			if (item.isProcessIt() == false) {
				Debug.out.printf("Skipping backup item %s on action %s: %s\n", item.getName(), action.getName(), item.getFilterMessage());
				ok = true;
			}
			else {
				try {
					ok = doAction(backupManager, action, item);
				} catch (BackupException e) {
					System.out.println("Error in BackupStream.runAction:" + e.getMessage());
					backupManager.updateProgress(new BackupProgress(this, action, STATE.PROBLEM, toDoList.size(), 
							iDone, "Error " + e.getMessage()));
					ok = false; //otherwise, OK stays true! This will leave a true null entry in the database, so file will be selected next time around
				}
			}
			if (ok) {
				updateActedItem(action, item);
				backupManager.updateProgress(new BackupProgress(this, action, STATE.RUNNING, toDoList.size(), 
						iDone, item.getActionMessage()));
			}
		}
		backupManager.updateProgress(new BackupProgress(this, action, STATE.STREAMDONE));
		
		return ok;
	}

	/**
	 * build the data catalogue from the last item in the current database. <br>
	 * Individual actions may want to look in the database to see if they have 
	 * themselves acted on the data, so will run their own query.  
	 * <p>Note that catalogData and getToDoList are separate functions, the idea being
	 * that catalog data will be listing all files in the source folder (or ones not already
	 * listed in the database table) whereas getToDoList can provide a set of files that are
	 * associated with a particular action, which may be a subset of files found in catalogData
	 * or may ignore catalogData in it's entirity. 
	 * @return
	 */
	public abstract List<StreamItem> catalogData();
	
	/**
	 * Get a list of items that this action needs to work on. this may be a subset of sourceItems
	 * @param sourceItems
	 * @param action
	 * @return
	 */
	public abstract List<StreamItem> getToDoList(List<StreamItem> sourceItems, BackupAction action);
	
	public abstract void updateActedItem(BackupAction action, StreamItem streamItem);

	public String getName() {
		return name;
	}

	/**
	 * Carry out some action or other on the stream item. 
	 * @param backupManager 
	 * @param action Backup action to perform
	 * @return true if successful, otherwise false
	 * @throws BackupException 
	 */
	public boolean doAction(BackupManager backupManager, BackupAction action, StreamItem streamItem) throws BackupException {
		return action.doAction(backupManager, this, streamItem);
	}


	/**
	 * Add an action
	 * @param action
	 */
	public void addAction(BackupAction action) {
		actions.add(action);
	}
	
	/**
	 * remove an action
	 * @param action
	 * @return
	 */
	public boolean removeAction(BackupAction action) {
		return actions.remove(action);
	}

	/**
	 * @return the actions
	 */
	public ArrayList<BackupAction> getActions() {
		return actions;
	}
	
	public PamDialogPanel getDialogPanel(Window owner) {
		return null;
	}
	
	/**
	 * Return basic settings. Cannot be null. does not need to add all 
	 * of the Action and Decision settings since that will all be handled by
	 * the calling functions getSettings from pamSettings
	 * @return
	 */
	public abstract BackupSettings getBackupSettings();
	
	/**
	 * Set settings when read back from PmSettings. Should always br
	 * the right type, but return flase if it isn't. 
	 * @param restoredSettings
	 * @return
	 */
	public abstract boolean setBackupSettings(BackupSettings restoredSettings);

	@Override
	public String getUnitName() {
		return settingsName.getUnitName();
	}

	@Override
	public String getUnitType() {
		return name;
	}

	@Override
	public Serializable getSettingsReference() {
		BackupSettings bs = getBackupSettings();
		if (bs == null) {
			return null;
		}
		List<ActionSettings> actionSettings = bs.getActionSettings();
		actionSettings.clear();
		for (BackupAction action : actions) {
			ActionSettings actSet = action.getSettings();
			if (actSet != null && action.getBackupFilter() != null) {
				actSet.setBackupFilterParams(action.getBackupFilter().getFilterParams());
			}
			actionSettings.add(action.getSettings());
		}
		return bs;
	}

	@Override
	public long getSettingsVersion() {
		return BackupSettings.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		BackupSettings restoredSettings = (BackupSettings) pamControlledUnitSettings.getSettings();
		if (setBackupSettings(restoredSettings)) {
			createActions(restoredSettings);
			return true;
		}
		return false;
	}

	/**
	 * Create actions for recently restored backup settings .
	 * @param restoredSettings
	 */
	protected void createActions(BackupSettings restoredSettings) {
		List<ActionMaker> availActions = getAvailableActions();
		List<ActionSettings> wantedActions = restoredSettings.getActionSettings();
		for (ActionSettings actionSet : wantedActions) {
			if (actionSet == null) {
				continue;
			}
			ActionMaker am = findActionMaker(availActions, actionSet.getActionClass());
			if (am != null) {
				BackupAction newAction = am.createAction(this);
				newAction.setSettings(actionSet);
				addAction(newAction);
			}
		}
	}
	
	protected ActionMaker findActionMaker(List<ActionMaker> availActions, String actionClass) {
		for (ActionMaker maker : availActions) {
			if (maker.getActionClass().getName().equals(actionClass)) {
				return maker;
			}
		}
		return null;
	}
	
	/**
	 * Get the amount of space remaining at the data source location
	 * @return space in bytes
	 */ 
	public Long getAvailableSpace() {
		return null;
	}
	
	/**
	 * Called when backup of a stream is complete so that catalogue
	 * can be closed, etc. 
	 */
	public abstract void backupComplete();

	public abstract List<ActionMaker> getAvailableActions();
	

}
