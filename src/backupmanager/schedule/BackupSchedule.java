package backupmanager.schedule;

import java.util.ArrayList;

import PamView.dialog.PamDialogPanel;
import backupmanager.BackupManager;
import backupmanager.BackupObserver;
import backupmanager.BackupProgress;
import backupmanager.BackupProgress.STATE;
import backupmanager.schedule.BackupResult.RESULT;

public abstract class BackupSchedule implements BackupObserver {

	private BackupManager backupManager;
	
	private BackupResult currentBackup;
	
	private ScheduleDatabase scheduleDatabase;
	
	private ArrayList<BackupProgress> errors;

	public BackupSchedule(BackupManager backupManager) {
		super();
		this.backupManager = backupManager;
		scheduleDatabase = new ScheduleDatabase(this);
		backupManager.addObserver(this);
		errors = new ArrayList<BackupProgress>();
	};
	
	@Override
	public boolean update(BackupProgress backupProgress) {
		if (backupProgress.getState() == STATE.PROBLEM) {
			synchronized(errors) {
				errors.add(backupProgress);
			}
		}
		if (backupProgress.getState() == STATE.ALLDONE) {
			backupDone();
		}
		return false;
	}

	@Override
	public void configurationChange() {
		// TODO Auto-generated method stub
		
	}

	private void backupDone() {
		String errDetail = null;
		RESULT backupResult = null;
		synchronized(errors) {
			errDetail = getErrorSummary();
			int nProblems = countProblems();
			backupResult = nProblems > 0 ? RESULT.Fail : RESULT.Success;
		}
		if (currentBackup == null) {
			return;
		}
		currentBackup.setEndTime(System.currentTimeMillis());
		currentBackup.setResult(backupResult);
		currentBackup.setDetail(errDetail);
		scheduleDatabase.updateResult(currentBackup);
	}

	private int countProblems() {
		int nProb = 0;
		for (BackupProgress res : errors) {
			if (res.getState() == STATE.PROBLEM) {
				nProb++;
			}
		}
		return nProb;
	}

	private String getErrorSummary() {
		String str = null;
		for (BackupProgress res : errors) {
			if (res.getState() == STATE.PROBLEM) {
				if (str == null) {
					str = res.getMsg();
				}
				else {
					str += ";" + res.getMsg();
				}
			}
		}
		return str;
	}

	public abstract String getName();
	
	public abstract PamDialogPanel getDialogPanel();
	
	public BackupResult startBackup() {
		currentBackup = new BackupResult(getName(), System.currentTimeMillis());
		scheduleDatabase.writeResult(currentBackup);
		backupManager.runBackups(backupManager.getGuiFrame());
		return currentBackup;
	}

	public BackupManager getBackupManager() {
		return backupManager;
	}
	
	public BackupResult getLastBackup() {
		return scheduleDatabase.getLastResult();
	}
	
	public BackupResult getLastSuccess() {
		return scheduleDatabase.getLastSuccess();
	}
	
	public abstract long getNextBackupTime();
	
}
