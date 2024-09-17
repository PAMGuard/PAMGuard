package backupmanager;

import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingWorker;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import backupmanager.BackupProgress.STATE;
import backupmanager.bespoke.BespokeBackups;
import backupmanager.bespoke.BespokeFileStream;
import backupmanager.schedule.BackupSchedule;
import backupmanager.schedule.SmallHoursSchedule;
import backupmanager.stream.BackupStream;
import backupmanager.swing.BackupDisplayProvider;
import userDisplay.UserDisplayControl;

/**
 * PAMGuard module to automatically back up files. Decisions on where to 
 * move files may be based on how many detections they contain,etc, so that 
 * high priority files end up in a different place to files that seem empty. 
 * @author dg50
 *
 */
public class BackupManager extends PamControlledUnit implements PamSettings {

	private static final String unitType = "Backup Manager";
	public static final String defaultName = "Backup Manager";

	private ArrayList<BackupStream> backupStreams = new ArrayList<BackupStream>();
	private BackupWorker backupWorker;

	private ArrayList<BackupObserver> progressObservers = new ArrayList<BackupObserver>();
	private volatile boolean continueBackup;
	
	private BespokeBackups bespokeBackups;
	
	private BackupSchedule backupSchedule;
	
	private BackupParams backupParams = new BackupParams();

	public BackupManager(String unitName) {
		super(unitType, unitName);
		bespokeBackups = new BespokeBackups(this);
		UserDisplayControl.addUserDisplayProvider(new BackupDisplayProvider(this));
		backupSchedule = new SmallHoursSchedule(this);
		PamSettingManager.getInstance().registerSettings(this);
	}

	public static BackupManager getBackupManager() {
		return (BackupManager) PamController.getInstance().findControlledUnit(BackupManager.class, null);
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenu menu = new JMenu(getUnitName());
		JMenuItem menuItem = new JMenuItem("Backup now ...");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (backupSchedule != null) {
					backupSchedule.startBackup();
				}
				else {
					runBackups(getGuiFrame());
				}
			}
		});
		menu.add(menuItem);
		
		bespokeBackups.addMenuItems(menu);
		
		return menu;
	}

	@Override
	public JMenuItem createFileMenu(JFrame parentFrame) {
		return createDetectionMenu(parentFrame);
	}

	/**
	 * Run all the backup. Never call this directly. Instead, call the 
	 * backupSchedule.startBackup() function so that the backup gets 
	 * correctly logged in the database. 
	 * @param parentFrame
	 */
	public void runBackups(Window parentFrame) {
		continueBackup = true;
		backupStreams = findBackupStreams();
		backupWorker = new BackupWorker(backupStreams);
		backupWorker.execute();
	}

	private class BackupWorker extends SwingWorker<Object, BackupProgress> {

		private ArrayList<BackupStream> backupStreams;

		public BackupWorker(ArrayList<BackupStream> backupStreams) {
			this.backupStreams = backupStreams;
		}

		@Override
		protected Object doInBackground() throws Exception {
			try {
				boolean ok = true;
				for (BackupStream stream : backupStreams) {
					ok |= stream.runBackup(BackupManager.this);
				}
				updateProgress(new BackupProgress(null, null, STATE.ALLDONE));
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		public void updateProgress(BackupProgress backupProgress) {
			this.publish(backupProgress);
		}


		@Override
		protected void process(List<BackupProgress> chunks) {
			for (BackupProgress backupProgress: chunks) {
				notifyObservers(backupProgress);
			}
		}

	}

	/**
	 * This will be called back from individual BackupStreams in the 
	 * Swing Worker thread. It needs to pass the message on to the 
	 * Swing worker so that the swing worker can publish it. It will 
	 * then reappear in the BackupWorker.process() function in the AWT
	 * thread from where it can be used to notify observers. 
	 * @param backupProgress
	 */
	public boolean updateProgress(BackupProgress backupProgress) {
		if (backupWorker != null) {
			backupWorker.updateProgress(backupProgress);
		}
		else {
			// no swing worker, so notify other processed directly. 
			notifyObservers(backupProgress);
		}
		return continueBackup;
	}

	public void updateConfiguration() {
		for (BackupObserver obs : progressObservers) {
			obs.configurationChange();
		}
	}

	/**
	 * Scan all PamcontrolledUnits for backup streams. |
	 * @return
	 */
	public ArrayList<BackupStream> findBackupStreams() {
		int nUnits = PamController.getInstance().getNumControlledUnits();
		ArrayList<BackupStream> streams = new ArrayList<BackupStream>();
		for (int i = 0; i < nUnits; i++) {
			PamControlledUnit pcu = PamController.getInstance().getControlledUnit(i);
			BackupInformation bi = pcu.getBackupInformation();
			if (bi == null) {
				continue;
			}
			ArrayList<BackupStream> newStreams = bi.getBackupStreams();
			if (newStreams != null) {
				streams.addAll(newStreams);
			}
		}
		ArrayList<BespokeFileStream> bespokeStreams = bespokeBackups.getFileStreams();
		if (bespokeStreams != null) {
			streams.addAll(bespokeStreams);
		}
		

		return streams;
	}

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		if (changeType == PamControllerInterface.INITIALIZATION_COMPLETE) {

		}
	}

	/**
	 * Add an observer that will get updates as backups take place. 
	 * @param progressObserver
	 */
	public void addObserver(BackupObserver progressObserver) {
		progressObservers.add(progressObserver);
	}

	/**
	 * Add an observer that will get updates as backups take place. 
	 * @param progressObserver
	 * @return true if it existed
	 */
	public boolean removeObserver(BackupObserver progressObserver) {
		return progressObservers.remove(progressObserver);
	}

	/**
	 * Notify observers of the backup process. 
	 * @param backupProgress
	 */
	private void notifyObservers(BackupProgress backupProgress) {
		for (BackupObserver obs : progressObservers) {
			boolean ans = obs.update(backupProgress);
			if (!ans) {
				continueBackup = false;
			}
		}
	}

	/**
	 * @return the backupSchedule
	 */
	public BackupSchedule getBackupSchedule() {
		return backupSchedule;
	}

	/**
	 * @param backupSchedule the backupSchedule to set
	 */
	public void setBackupSchedule(BackupSchedule backupSchedule) {
		this.backupSchedule = backupSchedule;
	}

	@Override
	public Serializable getSettingsReference() {
		return backupParams;
	}

	@Override
	public long getSettingsVersion() {
		return BackupParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		backupParams = (BackupParams) pamControlledUnitSettings.getSettings();
		return true;
	}

	/**
	 * @return the backupParams
	 */
	public BackupParams getBackupParams() {
		return backupParams;
	}
	
	
}
