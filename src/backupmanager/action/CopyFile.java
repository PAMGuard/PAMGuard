package backupmanager.action;

import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import PamController.PamController;
import PamUtils.DiskSpaceFormat;
import PamUtils.FileFunctions;
import PamUtils.PamUtils;
import PamView.dialog.PamDialogPanel;
import backupmanager.BackupManager;
import backupmanager.FileLocation;
import backupmanager.FileLocationParams;
import backupmanager.filter.BackupFilter;
import backupmanager.filter.BackupFilterParams;
import backupmanager.filter.alarm.AlarmBackupFilter;
import backupmanager.settings.ActionSettings;
import backupmanager.settings.FileBackupSettings;
import backupmanager.stream.BackupStream;
import backupmanager.stream.FileBackupStream;
import backupmanager.stream.FileStreamItem;
import backupmanager.stream.StreamItem;
import backupmanager.swing.ActionDialogPanel;
import backupmanager.swing.CopyFileDialogPanel;
import backupmanager.swing.FileBackupDialog;
import backupmanager.swing.GenericBackupDialog;

/**
 * Copy a file from one location to another, trying to preserve any sub folder stucture 
 * from the source to the destination directories. 
 * @author dg50
 *
 */
public class CopyFile extends BackupAction {

	private CopySettings copySettings;
	
	public CopyFile(ActionMaker actionMaker, BackupStream backupStream) {
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
		String destination = copySettings.destLocation.path;
		/**
		 * Call the checkDestinationPath will throw an exception if the path no
		 * longer exists (e.g a drive is removed) so although there is no return
		 * statement, this function may return here with an exception
		 */
		checkDestinationPath(destination);


		File srcFile = new File(streamItem.getName());
		if (srcFile.exists() == false) {
			throw new BackupException("Source file " + streamItem.getName() + " doesn't exist");
		}
		
		File destFile = createDestinationFile(backupStream, destination, streamItem);
		
		/*
		 *  now go up one step to get the destination folder so we can check it exists
		 *  since this may now be in a sub folder of 'destination' 
		 */
		
		File destFolder = destFile.getParentFile();
		if (checkDestFolder(destFolder) == false) {
			throw new BackupException("Unable to create destination folder " + destFolder.getAbsolutePath());
		}
		fileAction(srcFile, destFile);
		boolean ok = destFile.exists();
		if (ok) {
			// take off the destFolder from the returned value. 
			String dest = String.format("%s %s", getActionMaker().getName(), destFile.getName());
			streamItem.setActionMessage(dest);
		}
		return ok;
	}
	
	/**
	 * Create the destination File object (not an actual file, the Java File object that identifies is). 
	 * @param backupStream
	 * @param destination
	 * @param streamItem
	 * @return
	 * @throws BackupException
	 */
	public File createDestinationFile(BackupStream backupStream, String destination, StreamItem streamItem) throws BackupException {
		//		FileStreamInformation<FileStreamItem> fileStreamInfo = (FileStreamInformation<FileStreamItem>) streamInformation; 
		FileBackupStream fileStream = (FileBackupStream) backupStream;
		FileLocation sourceLocation = fileStream.getSourceLocation();
		String streamRoot = sourceLocation.path;
		String file = streamItem.getName();
		// first check that streamRoot is indeed a subset of file
		int rPos = file.indexOf(streamRoot);
		if (rPos != 0) {
			// ?? WTF ? these file should have been found in the root folder. 
			throw new BackupException(String.format("File %s is not in root system %s", file, streamRoot));
		}
		String fileBit = file.substring(streamRoot.length());
		File destFile = new File(destination, fileBit);
		return destFile;
	}

	public boolean checkDestinationPath(String destination) throws BackupException {	
		if (copySettings == null || copySettings.destLocation == null || copySettings.destLocation.path == null) {
			throw new BackupException("No destination folder has been set");
		}
		File destF = new File(destination);
		if (destF.exists() == false) {
			if (!destF.mkdirs()) {
				throw new BackupException("Destination folder " + destination + " does not exist");
			}
			FileFunctions.setNonIndexingBit(destF);
		}
		return true;
	}
	protected boolean fileAction(File source, File dest) throws BackupException {
		try {
			Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new BackupException(e.getMessage());
		}
		return true;
	}

	private boolean checkDestFolder(File destFolder) {
		if (destFolder.exists()) {
			return true;
		}
		boolean success = destFolder.mkdirs(); // should do it in one.
		if (!success) return false;
		FileFunctions.setNonIndexingBit(destFolder);
		return true;
	}

	@Override
	public String getName() {
		return "Copy to " + copySettings;
	}

	@Override
	public ActionSettings getSettings() {
		if (copySettings == null) {
			copySettings = new CopySettings(this.getClass().getName());
		}
		return copySettings;
	}

	@Override
	public boolean setSettings(ActionSettings settings) {
		if (settings instanceof CopySettings) {
			this.copySettings = (CopySettings) settings;
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
		return new CopyFileDialogPanel(this, owner);
	}

	/**
	 * @return the copySettings
	 */
	public CopySettings getCopySettings() {
		if (getBackupFilter() != null & copySettings != null) {
			copySettings.setBackupFilterParams(getBackupFilter().getFilterParams());
		}
		return copySettings;
	}

	/**
	 * @param copySettings the copySettings to set
	 */
	public void setCopySettings(CopySettings copySettings) {
		this.copySettings = copySettings;
	}

	@Override
	public boolean runIfPreviousActionError() {
		return true;
	}

	@Override
	public String getSpace() {
		if (copySettings == null) {
			return null;
		}
		FileLocation destLoc = copySettings.destLocation;
		if (destLoc == null) {
			return null;
		}
		Long space = destLoc.getFreeSpace();
		if (space == null) {
			return null;
		}
		return DiskSpaceFormat.formatSpace(space) + " available";
	}
	

}
