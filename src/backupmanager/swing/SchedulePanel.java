package backupmanager.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamUtils.PamCalendar;
import PamView.dialog.PamGridBagContraints;
import backupmanager.BackupManager;
import backupmanager.BackupObserver;
import backupmanager.BackupParams;
import backupmanager.BackupProgress;
import backupmanager.BackupProgress.STATE;
import backupmanager.schedule.BackupResult;
import backupmanager.schedule.BackupSchedule;
import backupmanager.schedule.ScheduleState;

public class SchedulePanel implements BackupObserver {

	private BackupManager backupManager;
	
	private JPanel westPanel;

	private BackupSchedule schedule;
	
	private JLabel lastBackup, lastSuccess, nextBackup;
	
	private JButton backupNow;
	
	private JComboBox<ScheduleState> scheduleState;

	public SchedulePanel(BackupManager backupManager) {
		super();
		this.backupManager = backupManager;
		schedule = backupManager.getBackupSchedule();
		westPanel = new JPanel(new BorderLayout());
		JPanel mainPanel = new JPanel();
		westPanel.setBorder(new TitledBorder("Schedule - " + schedule.getName()));
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.gridwidth = 5;
		mainPanel.add(lastBackup = new JLabel(" "), c);
		c.gridy++;
		mainPanel.add(lastSuccess = new JLabel(" "), c);
		c.gridy++;
		mainPanel.add(nextBackup = new JLabel(" "), c);
		c.gridy++;
		c.gridwidth = 1;
		mainPanel.add(new JLabel("  State: ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(scheduleState = new JComboBox<ScheduleState>(), c);
		ScheduleState[] states = ScheduleState.values();
		for (int i = 0; i < states.length; i++) {
			scheduleState.addItem(states[i]);
		}
		scheduleState.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				newScheduleState();
			}
		});
		c.gridx++;
		mainPanel.add(backupNow = new JButton("Backup now"), c);
		backupNow.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				backupNow();
			}
		});
		c.gridx++;
		
		fillData(null);
		westPanel.add(BorderLayout.WEST, mainPanel);
		backupManager.addObserver(this);
	}
	
	protected void newScheduleState() {
		BackupParams params = backupManager.getBackupParams();
		params.setScheduleState((ScheduleState) scheduleState.getSelectedItem());
		backupManager.updateConfiguration();
	}

	protected void backupNow() {
		schedule.startBackup();
	}

	public Component getComponent() {
		return westPanel;
	}

	@Override
	public boolean update(BackupProgress backupProgress) {
		this.backupNow.setEnabled(backupProgress.getState() == STATE.ALLDONE);
		fillData(backupProgress);
		return true;
	}

	@Override
	public void configurationChange() {
		BackupParams params = backupManager.getBackupParams();
		if (params.getScheduleState() != scheduleState.getSelectedItem()) {
			scheduleState.setSelectedItem(params.getScheduleState());
		}
	}
	
	private void fillData(BackupProgress backupProgress) {
		STATE currState = null;
		if (backupProgress != null) {
			currState = backupProgress.getState();
		}
		if (currState == null || currState == STATE.ALLDONE) {
			BackupResult lastB = schedule.getLastBackup();
			BackupResult lastS = schedule.getLastSuccess();
			if (lastB != null) {
				lastBackup.setText(lastB.toString());
			}
			else {
				lastBackup.setText("No attempted backups");
			}
			if (lastS != null) {
				lastSuccess.setText(lastS.toString());
			}
			else {
				lastSuccess.setText("No successful backups");
			}
			String nxt = String.format("Next backup due at %s", PamCalendar.formatDateTime(schedule.getNextBackupTime()));
			nextBackup.setText(nxt);
		}
		else {
			nextBackup.setText("Backup running ...");
		}
		BackupParams params = backupManager.getBackupParams();
		scheduleState.setSelectedItem(params.getScheduleState());
		
	}
	
}
