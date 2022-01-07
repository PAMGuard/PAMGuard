package generalDatabase.backup;

import java.io.File;

import PamUtils.PamCalendar;
import backupmanager.action.ActionMaker;
import backupmanager.action.BackupException;
import backupmanager.action.CopyFile;
import backupmanager.stream.BackupStream;
import backupmanager.stream.StreamItem;
import generalDatabase.DBControlUnit;

public class CopyDatabaseFile extends CopyFile {

	public CopyDatabaseFile(ActionMaker actionMaker, BackupStream backupStream) {
		super(actionMaker, backupStream);
		// reset filter to null since it's not relevant to this. 
		setBackupFilter(null);
	}

	public String getNewDatabaseName(String destination) {
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		if (dbControl == null) {
			return null;
		}
		String srcName = dbControl.getLongDatabaseName();
		// see if it's a file and if so, modify the name before the . bit, otherwise
		// it might be something like MySQL in which case there will be no . Further more
		// if it's not a file, then we'll need to ignore all the path information for 
		// the destination AND the source. 
		File srcFile = new File(srcName);
		if (srcFile.exists()) {
			return modifyNameAndPath(destination, srcName);
		}
		else {
			return appendDate(srcName);
		}
	}
	
	private String modifyNameAndPath(String destination, String srcName) {
		File srcFile = new File(srcName);
		srcName = srcFile.getName(); //just the name bit
		int lastDot = srcName.lastIndexOf('.');
		String name = srcName.substring(0, lastDot);
		String end = srcName.substring(lastDot);
		String dateStr = getDateString();
		String newName = String.format("%s_%s%s", name, dateStr, end);
		File file = new File(destination, newName);
		return file.getAbsolutePath();
	}

	private String appendDate(String srcName) {
		return srcName + "_" + getDateString();
	}
	
	private String getDateString() {
		return PamCalendar.formatFileDateTime(System.currentTimeMillis(), false);
	}

	@Override
	public File createDestinationFile(BackupStream backupStream, String destination, StreamItem streamItem)
			throws BackupException {
		String newName = modifyNameAndPath(destination, streamItem.getName());
		File destFile = new File(newName);
		return destFile;
	}



}
