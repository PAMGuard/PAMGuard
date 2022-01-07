package backupmanager.swing;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import PamUtils.PamCalendar;
import PamView.component.FixedLabel;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamTextDisplay;
import PamView.dialog.RemoveButton;
import PamView.dialog.ScrollingPamLabel;
import PamView.dialog.SettingsButton;
import PamView.dialog.warn.WarnOnce;
import backupmanager.BackupProgress;
import backupmanager.action.BackupAction;
import backupmanager.settings.ActionSettings;
import backupmanager.stream.BackupStream;

public class BackupActionPanel {
	
	private BackupAction backupAction;

	private BackupStream backupStream;
	
	private JProgressBar progressBar;
	
	private FixedLabel lastAction;
	
	private JButton removeButton;
	
	private SettingsButton settingsButton;

	private FixedLabel backupName;

	private FixedLabel diskSpace;

	private BackupControlPanel backupControlPanel;

	private Color okColor = Color.GREEN;
	private Color errColor = Color.RED;

	public BackupActionPanel(BackupControlPanel backupControlPanel, BackupStream backupStream, BackupAction backupAction, JPanel mainPanel, GridBagConstraints c) {
		super();
		this.backupControlPanel = backupControlPanel;
		this.backupStream = backupStream;
		this.backupAction = backupAction;
		c.gridy++;
		c.insets.top = 3;
		c.gridx = 0;
		c.gridwidth = 3;
		mainPanel.add(backupName = new FixedLabel(backupAction.getName(), JLabel.LEFT), c);
		backupName.setToolTipText(backupAction.getActionMaker().getToolTip());
		c.gridx += c.gridwidth;
		c.gridwidth = 2;
		mainPanel.add(diskSpace = new FixedLabel("diskSpace", JLabel.RIGHT), c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		c.insets.top = c.insets.bottom = 0;
//		c.gridx++;
		mainPanel.add(new JLabel("Last: ", JLabel.CENTER), c);
		c.gridx++;
		mainPanel.add(lastAction = new FixedLabel(), c);
		c.gridx++;
		mainPanel.add(progressBar = new JProgressBar(), c);
		okColor = progressBar.getForeground();
		progressBar.setStringPainted(true);
		c.gridx++;
		mainPanel.add(settingsButton = new SettingsButton(), c);
		c.gridx++;
		mainPanel.add(removeButton = new RemoveButton(), c);
		
		settingsButton.setToolTipText("Configure backup");
		settingsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				settingsAction(e);
			}
		});
		
		removeButton.setToolTipText("Remove backup");
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeAction(e);
			}
		});
		
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		fillPanel();
	}

	protected void settingsAction(ActionEvent e) {
		if (backupAction.showDialog(null)) {
			backupControlPanel.getBackupManager().updateConfiguration();
		};
	}

	protected void removeAction(ActionEvent e) {
		String msg = String.format("Are you sure you want to remove the %s backup action?", backupAction.getName());
		int ans = WarnOnce.showWarning(backupStream.getName(), msg, WarnOnce.OK_CANCEL_OPTION, null);
		if (ans == WarnOnce.CANCEL_OPTION) {
			return;
		}
		backupStream.removeAction(backupAction);
		backupControlPanel.getBackupManager().updateConfiguration();
	}

	private void fillPanel() {
		backupName.setText(backupAction.getName());
		diskSpace.setText(backupAction.getSpace());
		tellTime();
	}

	private void tellTime() {
		long lastBackup = 0;
		long lastDuration = 0;
		ActionSettings backupSettings = backupAction.getSettings();
		if (backupSettings != null) {
			lastBackup = backupSettings.getLastBackupTime();
			lastDuration = backupSettings.getLastBackupDuration();
		}
		if (lastBackup == 0) {
			lastAction.setText("unknown");
		}
		else {
			lastAction.setText(PamCalendar.formatDateTime(lastBackup));
		}
		if (lastDuration > 0) {
			lastAction.setToolTipText(String.format("Last backup took %s", PamCalendar.formatDuration(lastDuration)));
		}
		else {
			lastAction.setToolTipText(" ");
		}
	}

	/**
	 * @return the backupAction
	 */
	public BackupAction getBackupAction() {
		return backupAction;
	}

	int errors = 0;
	public void progressUpdate(BackupProgress backupProgress) {
		int perc;
		switch(backupProgress.getState()) {
		case CATALOGING:
			lastAction.setText("Cataloging");
			progressBar.setIndeterminate(true);
			errors = 0;
			progressBar.setForeground(okColor);
			break;
		case STREAMDONE:
			if (errors == 0) {
				progressBar.setIndeterminate(false);
			}
			tellTime();
			break;
		case RUNNING:
			progressBar.setIndeterminate(false);
			lastAction.setText("Running");
			perc = backupProgress.getPercent();
			progressBar.setValue(perc);
			progressBar.setString(String.format("%d of %d", backupProgress.getN2(), backupProgress.getN1()));
			break;
		case PROBLEM:
			progressBar.setIndeterminate(false);
			lastAction.setText("Error");
			perc = backupProgress.getPercent();
			progressBar.setValue(perc);
			progressBar.setString(String.format("%d Errors", backupProgress.getN2()));
			progressBar.setForeground(errColor);
			errors++;
			break;
		default:
			break;
		
		}
	}

}
