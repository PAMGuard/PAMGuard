package backupmanager;

import backupmanager.action.BackupAction;
import backupmanager.stream.BackupStream;

/**
 * Class to send progress data to backup observers. 
 * The observer is primarily the dialog window, which 
 * will have a single observer for all streams and will 
 * have to farm out any notifications to it's appropriate
 * sub components.  
 * @author dg50
 *
 */
public class BackupProgress {

	public enum STATE {CATALOGING, RUNNING, STREAMDONE, ALLDONE, PROBLEM};
	/**
	 * Backup stream can never be null except in the one case of
	 * things being complete. 
	 */
	private BackupStream backupStream;
	
	/**
	 * Backup action might be null when cataloguing all data in a stream 
	 */
	private BackupAction backupAction;

	private STATE state;

	private int n1;

	private int n2;

	private String msg;

	/**
	 * Bckup progress. 
	 * @param backupStream
	 * @param backupAction
	 * @param state
	 */
	public BackupProgress(BackupStream backupStream, BackupAction backupAction, STATE state) {
		this(backupStream, backupAction, state, 0, 0, null);
	}
	
	/**
	 * Bckup progress. 
	 * @param backupStream
	 * @param backupAction
	 * @param state
	 * @param n1 generally the total number of units to backup
	 * @param n2 generally the id of the unit currently working on 
	 * @param msg some sort of text, e.g. name of file. 
	 */
	public BackupProgress(BackupStream backupStream, BackupAction backupAction, STATE state, int n1, int n2, String msg) {
		super();
		this.backupStream = backupStream;
		this.backupAction = backupAction;
		this.state = state;
		this.n1 = n1;
		this.n2 = n2;
		this.msg = msg;
	}

	/**
	 * @return the backupStream
	 */
	public BackupStream getBackupStream() {
		return backupStream;
	}

	/**
	 * @return the backupAction
	 */
	public BackupAction getBackupAction() {
		return backupAction;
	}

	/**
	 * @return the state
	 */
	public STATE getState() {
		return state;
	}

	/**
	 * @return the n1
	 */
	public int getN1() {
		return n1;
	}

	/**
	 * @return the n2
	 */
	public int getN2() {
		return n2;
	}

	/**
	 * @return the msg
	 */
	public String getMsg() {
		return msg;
	}
	 
	/**
	 * Get n1/n2 as a percentage. 
	 * @return
	 */
	public int getPercent() {
		if (n1 == 0) {
			return 100;
		}
		return (100* n2) / n1;
	}
	
}
