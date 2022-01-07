package backupmanager.action;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import backupmanager.stream.BackupStream;

/**
 * Almost identical to copy file. Will always return true, but throws 
 * an exception if the move fails. 
 * @author dg50
 *
 */
public class MoveFile extends CopyFile {

	public MoveFile(ActionMaker actionMaker, BackupStream backupStream) {
		super(actionMaker, backupStream);
	}

	@Override
	protected boolean fileAction(File source, File dest) throws BackupException {
		try {
			Files.move(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new BackupException(e.getMessage());
		}
		return true;
	}
	
	@Override
	public String getName() {
		return "Move to " + getSettings();
	}

}
