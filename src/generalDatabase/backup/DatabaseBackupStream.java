package generalDatabase.backup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import backupmanager.BackupManager;
import backupmanager.FileLocation;
import backupmanager.action.ActionMaker;
import backupmanager.action.BackupAction;
import backupmanager.action.BackupException;
import backupmanager.settings.BackupSettings;
import backupmanager.stream.BackupStream;
import backupmanager.stream.FileBackupStream;
import backupmanager.stream.FileStreamItem;
import backupmanager.stream.StreamItem;
import generalDatabase.DBControlUnit;
import generalDatabase.DBSystem;

public class DatabaseBackupStream extends FileBackupStream {

	private DBControlUnit dbControlUnit;

	private List<ActionMaker> availableActions;
	
//	private DatabaseBackupSettings databaseBackupSettings;

	public DatabaseBackupStream(DBControlUnit dbControlUnit) {
		super(dbControlUnit, dbControlUnit.getUnitName());
		this.dbControlUnit = dbControlUnit;
	}

	@Override
	public List<StreamItem> getToDoList(List<StreamItem> streamItems, BackupAction action) {
		/**
		 * This will have a single unit - the database file. If it's not file
		 * based, the no-can-do
		 */
		File dbFile = getDatabaesFile();
		if (dbFile == null) {
			return null;
		}
//		if (dbFile.exists() == false) {
//			System.out.printf("Database %s is not a file and cannot be backed up\n", dbFile.getAbsolutePath());
//			return null;
//		}
		StreamItem dbItem = new FileStreamItem(dbFile);
		ArrayList<StreamItem> items = new ArrayList<StreamItem>();
		items.add(dbItem);
		return items;
	}
	
	private File getDatabaesFile() {
		String dbName = dbControlUnit.getDatabaseName(); // that's just the name, not full path.
		DBSystem dbSystem = dbControlUnit.getDatabaseSystem();
		if (dbSystem == null) {
			return null;
		}
		dbName = dbSystem.getDatabaseName();
		if (dbName == null) {
			return null;
		}
		// it may not be a file, in which case it can't be backed up. 
		File dbFile = new File(dbName);
		return dbFile;
	}

	@Override
	public boolean doAction(BackupManager backupManager, BackupAction action, StreamItem streamItem) throws BackupException {
		/*
		 * commit database changes before copying it. 
		 * 
		 */
		DBControlUnit dbControlUnit = DBControlUnit.findDatabaseControl();
		if (dbControlUnit != null) { // impossible for it not to be !
			dbControlUnit.commitChanges();
		}
		return super.doAction(backupManager, action, streamItem);
	}

//	@Override
//	public BackupSettings getBackupSettings() {
//		if (databaseBackupSettings == null) {
//			databaseBackupSettings = new DatabaseBackupSettings();
//		}
//		return databaseBackupSettings;
//	}
//
//	@Override
//	public boolean setBackupSettings(BackupSettings restoredSettings) {
//		if (restoredSettings instanceof DatabaseBackupSettings) {
//			databaseBackupSettings = (DatabaseBackupSettings) restoredSettings;
//			return true;
//		}
//		return false;
//	}

	@Override
	public List<ActionMaker> getAvailableActions() {
		if (availableActions == null) {
			availableActions = new ArrayList<ActionMaker>();
			availableActions.add(new CopyDatabaseMaker());
			availableActions.add(new SQLCloneMaker());
		}
		return availableActions;
	}

	@Override
	public List<StreamItem> catalogData() {
		StreamItem si = new FileStreamItem(getDatabaesFile());
		List<StreamItem> sis = new ArrayList<StreamItem>();
		sis.add(si);
		return sis;
	}

	@Override
	public void updateActedItem(BackupAction action, StreamItem streamItem) {
		
	}

	public FileLocation getSourceLocation() {
		File dbFile = getDatabaesFile();
		if (dbFile == null) {
			return null;
		}
		FileLocation sl = new FileLocation();
		sl.path = dbFile.getAbsolutePath();
		sl.canEditMask = false;
		sl.canEditPath = false;
		return sl;
	}

	@Override
	public void setSourceLocation(FileLocation fileLocation) {
		
	}

}
