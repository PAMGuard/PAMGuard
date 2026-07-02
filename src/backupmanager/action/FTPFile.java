package backupmanager.action;

import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import PamUtils.DiskSpaceFormat;
import PamUtils.FileFunctions;
import PamView.dialog.PamDialogPanel;
import backupmanager.BackupManager;
import backupmanager.FileLocation;
import backupmanager.filter.BackupFilterParams;
import backupmanager.filter.alarm.AlarmBackupFilter;
import backupmanager.network.TransferFailedException;
import backupmanager.settings.ActionSettings;
import backupmanager.stream.BackupStream;
import backupmanager.stream.FileBackupStream;
import backupmanager.stream.StreamItem;
import backupmanager.swing.CopyFileDialogPanel;
import backupmanager.swing.FtpFileDialogPanel;

public class FTPFile extends BackupAction{

	private FtpSettings ftpSettings;
	
	public FTPFile(ActionMaker actionMaker, BackupStream backupStream) {
		super(actionMaker, backupStream);
		setBackupFilter(new AlarmBackupFilter(this, "file selection options"));
	}

	@Override
	public boolean doAction(BackupManager backupManager, BackupStream backupStream, StreamItem streamItem) throws BackupException {
		/**
		 * Want to maintain any sub folder structure that was in the root file system (folders by date)
		 * so will need to work out how much of the name in streamItem comes after the root folder 
		 * in streamItem;
		 */
		String destination = ftpSettings.destLocation.path;


		File srcFile = new File(streamItem.getName());
		if (srcFile.exists() == false) {
			throw new BackupException("Source file " + streamItem.getName() + " doesn't exist");
		}
		
		String destinationDir = destination+"/"+srcFile.getParentFile().getName();
		
		
		fileAction(srcFile, destinationDir);
		
		return true;
	}
	
	public boolean requiresFTPConnection() {
		return true;
	}
	
	protected boolean fileAction(File source, String destDir) throws BackupException {
		try {
			BackupManager.getBackupManager().getFtpClient().mkdir(destDir);
		}catch(TransferFailedException e) {
			throw e;
		}
		try {
			BackupManager.getBackupManager().getFtpClient().copyLocalToRemote(source.getParent(), destDir, source.getName());
		} catch (TransferFailedException e) {
			throw e;
		}
		return true;
	}

	@Override
	public String getName() {
		return "FTP to " + ftpSettings;
	}

	@Override
	public ActionSettings getSettings() {
		if (ftpSettings == null) {
			ftpSettings = new FtpSettings(this.getClass().getName());
		}
		return ftpSettings;
	}

	@Override
	public boolean setSettings(ActionSettings settings) {
		if (settings instanceof FtpSettings) {
			this.ftpSettings = (FtpSettings) settings;
		}
		else {
			return false;
		}
		BackupFilterParams backupFilterParams = settings.getBackupFilterParams();
		if (backupFilterParams != null && getBackupFilter() != null) {
			getBackupFilter().setFilterParams(backupFilterParams);
		}
		return true;
	}

	@Override
	public PamDialogPanel getDialogPanel(Window owner) {
		return new FtpFileDialogPanel(this, owner);
	}

	/**
	 * @return the copySettings
	 */
	public FtpSettings getFtpSettings() {
		if (getBackupFilter() != null & ftpSettings != null) {
			ftpSettings.setBackupFilterParams(getBackupFilter().getFilterParams());
		}
		return ftpSettings;
	}

	/**
	 * @param copySettings the copySettings to set
	 */
	public void setFtpSettings(FtpSettings ftpSettings) {
		this.ftpSettings = ftpSettings;
	}

	@Override
	public boolean runIfPreviousActionError() {
		return true;
	}

	@Override
	public String getSpace() {
		return "Cloud storage";
	}

}
