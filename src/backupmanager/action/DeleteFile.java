package backupmanager.action;

import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import PamController.PamController;
import PamUtils.PamCalendar;
import PamView.dialog.PamDialogPanel;
import backupmanager.BackupManager;
import backupmanager.filter.alarm.AlarmBackupFilter;
import backupmanager.settings.ActionSettings;
import backupmanager.stream.BackupStream;
import backupmanager.stream.StreamItem;
import backupmanager.swing.CopyFileDialogPanel;
import backupmanager.swing.GenericBackupDialog;

public class DeleteFile extends BackupAction {

	private ActionSettings actionSettings;
	
	public DeleteFile(ActionMaker actionMaker, BackupStream backupStream) {
		super(actionMaker, backupStream);
		
		setBackupFilter(new AlarmBackupFilter(this, "file selection options"));
	}

	@Override
	public String getName() {
		return "Delete File";
	}

	@Override
	public boolean doAction(BackupManager backupManager, BackupStream backupStream, StreamItem streamItem)
			throws BackupException {
		File srcFile = new File(streamItem.getName());
		if (srcFile.exists() == false) {
			throw new BackupException("Source file " + streamItem.getName() + " doesn't exist");
		}
		try {
			Files.delete(srcFile.toPath());
		} catch (IOException e) {
			throw new BackupException(e.getMessage());
		}
		streamItem.setActionMessage("Deleted " + PamCalendar.formatDBDateTime(System.currentTimeMillis()));
		return true;
	}

	@Override
	public PamDialogPanel getDialogPanel(Window owner) {
		return null;
	}


	@Override
	public ActionSettings getSettings() {
		if (actionSettings == null) {
			actionSettings = new ActionSettings(getClass());
		}
		return actionSettings;
	}

	@Override
	public boolean setSettings(ActionSettings settings) {
		this.actionSettings = settings;
		return true;
	}

	@Override
	public boolean runIfPreviousActionError() {
		// don't delete if  a previous action failed. 
		return false;
	}

}
