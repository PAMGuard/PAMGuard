package backupmanager;

import java.util.ArrayList;

import PamController.PamControlledUnit;
import backupmanager.stream.BackupStream;

/**
 * Class that can be returned by a PAMControlled unit which gives backup information
 * which is mostly a list of BackupStream objects (or just one of, but some modules may
 * have multiple streams which do not necessarily relate directly to the datablocks. 
 * @author dg50
 *
 */
public class BackupInformation {
	
	private ArrayList<BackupStream> backupStreams = new ArrayList<BackupStream>();
	
	public BackupInformation() {
		
	}
	
	public BackupInformation(BackupStream backupStream) {
		if (backupStream != null) {
			backupStreams.add(backupStream);
		}
	}

	/**
	 * @return the backupStreams
	 */
	public ArrayList<BackupStream> getBackupStreams() {
		return backupStreams;
	}
	
	
}
