package backupmanager.stream;

import java.awt.Window;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOCase;

import PamController.SettingsNameProvider;
import PamUtils.FileList;
import PamUtils.PamFileFilter;
import PamUtils.PamWildCardFileFilter;
import PamView.dialog.PamDialogPanel;
import PamguardMVC.debug.Debug;
import backupmanager.FileLocation;
import backupmanager.action.ActionMaker;
import backupmanager.action.BackupAction;
import backupmanager.action.CopyActionMaker;
import backupmanager.action.DeleteActionMaker;
import backupmanager.action.MoveActionMaker;
import backupmanager.database.BackupCatalog;
import backupmanager.database.DatabaseCatalog;
import backupmanager.settings.BackupSettings;
import backupmanager.settings.FileBackupSettings;
import backupmanager.swing.FileStreamDialogPanel;

public abstract class FileBackupStream extends BackupStream {

	private List<ActionMaker> availableActions;

//	private StreamItemTable streamTable;
	private BackupCatalog backupCatalog;
	
	private long processDelaySeconds = 60;

	/**
	 * Don't initialise this, because this is called within the constructor
	 * of this, but settings will have been loaded from super() and will be
	 * overwitten since this line gets called after. 
	 */
	private FileBackupSettings fileBackupSettings;
	
	public FileBackupStream(SettingsNameProvider settingsName, String name) {
		super(settingsName, name);
	}
	
	
	@Override
	public void backupComplete() {
		if (backupCatalog != null) {
			backupCatalog.backupComplete();
		}
	}


	/**
	 * Source location - For most things, like the sound recorder or binary 
	 * files this will have to be taken from the other settings. For some  modules
	 * like the gemini, which are recording elsewhere, it can be anything. 
	 * @return the source location. 
	 */
	public abstract FileLocation getSourceLocation();
	
	/**
	 * As with the getter, this may be ignored by many modules which will 
	 * perfectly well know their source location
	 * @param fileLocation
	 */
	public abstract void setSourceLocation(FileLocation fileLocation);
	
//	/**
//	 * Get the destination location folder
//	 * @return
//	 */
//	public abstract FileLocation getDestLocation();
//	
//	/**
//	 * Set the destination location folder
//	 * @param fileLocation
//	 */
//	public abstract void setDestLocation(FileLocation fileLocation);

	/**
	 * Get new file items that haven't been recently modified  and convert to
	 * StreamItems.  
	 * @param maximumTimeMillis last allowed modified time. 
	 * @return list of StreamItems in the root folders. 
	 */
	public List<StreamItem> getAllSourceItems(long maximumTimeMillis) {
		long t1 = System.currentTimeMillis();
		List<File> fileList = getAllFiles();
		if (fileList == null) {
			return null;
		}
		long t2 = System.currentTimeMillis();
		ArrayList<StreamItem> newItems = new ArrayList<StreamItem>();
		for (File aFile : fileList) {
			Long lastMod = getModifedTime(aFile);
			if (lastMod != null && lastMod > maximumTimeMillis) {
				continue;
			}
			newItems.add(new FileStreamItem(aFile));
		}
		long t3 = System.currentTimeMillis();
		Debug.out.printf("Time to catalog %d files was %dms and select %d was %dms\n", fileList.size(), t2-t1, newItems.size(), t3-t2);
		return newItems;
	}
	
	private Long getModifedTime(File aFile) {
		try {
			BasicFileAttributes attr = Files.readAttributes(aFile.toPath(), BasicFileAttributes.class);
			return attr.lastModifiedTime().toMillis();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get all files in the source folder system. 
	 * @return big list of files. 
	 */
	public List<File> getAllFiles() {
		FileLocation sourceLocation = getSourceLocation();
		FileList fileList = new FileList();
		FileFilter fileFilter;
		String mask = sourceLocation.mask;
		String[] maskBits = null;
		if (mask != null && mask.length() > 0) {
			maskBits = mask.split(";");
			PamWildCardFileFilter pamFF = new PamWildCardFileFilter(maskBits, IOCase.INSENSITIVE);
			pamFF.setAcceptFolders(true);
			fileFilter = pamFF;
		}
		else {
			fileFilter = new PamFileFilter("Everything", null);
		}

		return fileList.getFileList(sourceLocation.path, fileFilter, true);
	}


	@Override
	public FileBackupSettings getBackupSettings() {
		if (fileBackupSettings == null) {
			fileBackupSettings = new FileBackupSettings();
		}
		return fileBackupSettings;
	}


	@Override
	public boolean setBackupSettings(BackupSettings restoredSettings) {
		if (restoredSettings instanceof FileBackupSettings) {
			fileBackupSettings = (FileBackupSettings) restoredSettings;
			return true;
		}
		return false;
	}


	@Override
	public List<ActionMaker> getAvailableActions() {
		if (availableActions == null) {
			availableActions = new ArrayList<ActionMaker>();
			availableActions.add(new CopyActionMaker());
			availableActions.add(new MoveActionMaker());
			availableActions.add(new DeleteActionMaker());
		}
		return availableActions;
	}

	@Override
	public List<StreamItem> catalogData() {
//		streamTable = new StreamItemTable(this); // make fresh every time

		backupCatalog = new DatabaseCatalog(this, getSourceLocation().path);
		
		long t1 = System.currentTimeMillis();
		
		long maximumTimeMillis = System.currentTimeMillis() - getMinBackupDelay();
		List<StreamItem> allSourceItems = (List<StreamItem>) getAllSourceItems(maximumTimeMillis);
		if (allSourceItems == null) {
			return null;
		}
		
		long t2 = System.currentTimeMillis();
		System.out.printf("List %d files %dms", allSourceItems.size(), t2-t1);

		List<StreamItem> newList = backupCatalog.catalogNewItems(allSourceItems);
		
		long t4 = System.currentTimeMillis();
		System.out.printf("; Write %d new files %dms\n\n", newList.size(), t4-t2);
		
		return newList;
	}
	/**
	 * Get the minimum delay from the time when a file was last modified or created to when it can be backed up
	 * @return time in milliseconds
	 */
	public long getMinBackupDelay() {
		return processDelaySeconds * 1000;
	}
//
//	@Override
//	public List<StreamItem> getToDoList(int iAction, BackupAction action) {
//		List<StreamItem> toDoList = backupCatalog.getUnactedItems(, action);
//		return toDoList;
//	}

//
//	@Override
//	public void updateActedItem(int actionIndex, BackupAction action, StreamItem streamItem) {
//		PamConnection dbConnection = DBControlUnit.findConnection();
//		if (dbConnection == null) {
//			return;
//		}
//		backupCatalog.updateActedItem(dbConnection, actionIndex, action, streamItem);		
//	}


	@Override
	public PamDialogPanel getDialogPanel(Window owner) {
		return new FileStreamDialogPanel(this, owner);
	}

	@Override
	public List<StreamItem> getToDoList(List<StreamItem> sourceItems, BackupAction action) {
		return backupCatalog.getUnactedItems(sourceItems, action);
	}

	@Override
	public void updateActedItem(BackupAction action, StreamItem streamItem) {
		backupCatalog.updateItem(streamItem, action);
	}


	@Override
	public Long getAvailableSpace() {
		FileLocation sourceLoc = getSourceLocation();
		if (sourceLoc == null) {
			return null;
		}
		return sourceLoc.getFreeSpace();
	}
	
}
