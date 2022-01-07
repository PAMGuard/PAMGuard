package backupmanager.schedule;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import PamController.PamController;
import PamView.dialog.PamDialogPanel;
import backupmanager.BackupManager;

public class SmallHoursSchedule extends BackupSchedule {

	private long minBackupTime = System.currentTimeMillis();

	static long oneHour = 3600L*1000L;
	static long oneDay = oneHour*24L;

	public SmallHoursSchedule(BackupManager backupManager) {
		super(backupManager);
		updateBackupTime();
		if (PamController.getInstance().getRunMode() != PamController.RUN_PAMVIEW) {
			startTimer();
		}
	}
	
	private void startTimer() {
		Timer timer = new Timer(60000, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				runBackups();
			}
		});
		timer.start();
	}

	private void runBackups() {
		if (getBackupManager().getBackupParams().getScheduleState() == ScheduleState.PAUSED) {
			return;
		}
		if (System.currentTimeMillis() > getNextBackupTime()) {
			startBackup();
			updateBackupTime();
		}
	}


	@Override
	public String getName() {
		return "Small Hours";
	}

	@Override
	public PamDialogPanel getDialogPanel() {
		return null;
	}


	private void updateBackupTime() {
		minBackupTime = System.currentTimeMillis() + 1;
	}
	
	@Override
	public long getNextBackupTime() {
		/*
		 *  just round it up for now and see how we do.
		 *  minbackup time is only set AFTER a backup, so hopefully won't wrap 
		 *  up to the next day easily. only feature is that if the software starts
		 *  after midnight, it won't back up until the next 1am.  
		 */
		long nextmidnight = ((minBackupTime / oneDay)+1)*oneDay;
		long nextBackup = nextmidnight + 3600L*1000L;// one hour after midnight 
		return nextBackup;
	}

}
