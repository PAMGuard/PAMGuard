package backupmanager.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import com.sun.prism.paint.Color;

import PamController.PamController;
import PamUtils.DiskSpaceFormat;
import PamView.component.FixedLabel;
import PamView.dialog.PamGridBagContraints;
import backupmanager.BackupProgress;
import backupmanager.FileLocation;
import backupmanager.action.ActionMaker;
import backupmanager.action.BackupAction;
import backupmanager.stream.BackupStream;
import backupmanager.stream.FileBackupStream;

public class BackupStreamPanel {

	private JPanel streamPanel, mainPanel;
	
	private BackupStream backupStream;

//	private ArrayList<BackupAction> actions;
	
	private ArrayList<BackupActionPanel> actionPanels = new ArrayList<BackupActionPanel>();
	
	private JButton addButton;
	
	private Window owner;

	private BackupControlPanel backupControlPanel;

	private TitledBorder titleBorder;
	
	private FixedLabel actionLabel;

	private BackupProgress latestProgress;

	private Timer spaceTimer;
	
	public BackupStreamPanel(Window owner, BackupControlPanel backupControlPanel, BackupStream backupStream) {
		super();
		this.owner = owner;
		this.backupControlPanel = backupControlPanel;
		this.backupStream = backupStream;
		mainPanel = new JPanel(new BorderLayout());
		streamPanel = new JPanel();
		actionLabel = new FixedLabel(" ");
		streamPanel.setLayout(new GridBagLayout());
		mainPanel.setBorder(titleBorder = new TitledBorder(backupStream.getName()));
		mainPanel.add(BorderLayout.CENTER, streamPanel);
		JPanel botPanel = new JPanel(new BorderLayout());
		addButton = new JButton("Add Action...");
		botPanel.add(BorderLayout.CENTER, actionLabel);
		botPanel.add(BorderLayout.EAST, addButton);
		mainPanel.add(BorderLayout.SOUTH, botPanel);
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showAddMenu(e);
			}
		});
		fillStreamPanel();
		makeTitle(backupStream);
		sayInitialSpace();
		if (PamController.getInstance().getRunMode() == PamController.RUN_NORMAL) {
			spaceTimer = new Timer(6000, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					updateSpace();					
				}
			});
			spaceTimer.start();
		}
	}
	
	private String makeTitle(BackupStream backupStream) {
		String tit = backupStream.getName();
		if (backupStream instanceof FileBackupStream) {
			FileBackupStream fileBackupStream = (FileBackupStream) backupStream;
			FileLocation source = fileBackupStream.getSourceLocation();
			if (source != null) {
				if (source.path != null) {
					tit += " - " + source.path;
				}
				if (source.mask != null && source.mask.length() > 0) {
					tit += ", files " + source.mask;
				}
			}
		}
		titleBorder.setTitle(tit);
		return tit;
	}
	
	private void fillStreamPanel() {
		streamPanel.removeAll();
		ArrayList<BackupAction> actions = backupStream.getActions();
		GridBagConstraints c = new PamGridBagContraints();
		for (BackupAction action : actions) {
			BackupActionPanel actionPanel = new BackupActionPanel(backupControlPanel, backupStream, action, streamPanel, c);
//			streamPanel.add(actionPanel.getComponent());
			actionPanels.add(actionPanel);
		}
	}

	protected void showAddMenu(ActionEvent e) {
		JPopupMenu popMenu = new JPopupMenu();
		List<ActionMaker> availActions = backupStream.getAvailableActions();
		for (ActionMaker am : availActions) {
			JMenuItem menuItem = new JMenuItem(am.getName());
			menuItem.setToolTipText(am.getToolTip());
			menuItem.addActionListener(new AddAction(am));
			popMenu.add(menuItem);
		}
		popMenu.show(addButton, addButton.getWidth()/2, addButton.getHeight()/2);
	}

	private class AddAction implements ActionListener {
		ActionMaker actionMaker;

		public AddAction(ActionMaker actionMaker) {
			super();
			this.actionMaker = actionMaker;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			BackupAction newAction = actionMaker.createAction(backupStream);
			backupStream.addAction(newAction);
			fillStreamPanel();
			if (owner != null) {
				owner.pack();
			}
		}
		
	}
	public JComponent getComponent() {
		return mainPanel;
	}

	/**
	 * @return the backupStream
	 */
	public BackupStream getBackupStream() {
		return backupStream;
	}
	
	/**
	 * Called on a timer every minute in normal mode so that the disk space data
	 * on the data source gets updated. 
	 */
	protected synchronized void updateSpace() {
		if (latestProgress == null) {
			sayInitialSpace();
		}
		else {
			progressUpdate(latestProgress);
		}
	}

	private void sayInitialSpace() {
		if (backupStream == null) {
			return;
		}
		Long space = backupStream.getAvailableSpace();
		if (space != null) {
			actionLabel.setText(DiskSpaceFormat.formatSpace(space) + " remaining at source location");
		}
	}
	
	int errors = 0;

	public synchronized boolean progressUpdate(BackupProgress backupProgress) {
		latestProgress = backupProgress;
		for (BackupActionPanel ap : actionPanels) {
			if (backupProgress.getBackupAction() == null || backupProgress.getBackupAction() == ap.getBackupAction()) {
				ap.progressUpdate(backupProgress);
			}
		}
		String runMsg = null;
		switch(backupProgress.getState()) {
		case CATALOGING:
			actionLabel.setText(" Searching root folder");
			errors = 0;
			break;
		case STREAMDONE:
			if (errors == 0) {
				actionLabel.setText(getDoneText());
			}
			break;
		case RUNNING:
			runMsg = String.format(" Processing %s, %d of %d", backupProgress.getMsg(), backupProgress.getN2(), backupProgress.getN1());
			break;
		case PROBLEM:
			runMsg = "Error";
			errors++;
		default:
			break;
		
		}
		String msg = backupProgress.getMsg();
		if (msg == null) {
			msg = runMsg;
		}
		if (msg != null) {
			actionLabel.setText(msg);
		}
		
		return false;
	}
	
	private String getDoneText() {
		String str = " Backup complete";
		Long space = backupStream.getAvailableSpace();
		if (space != null) {
			str += "; " + DiskSpaceFormat.formatSpace(space) + " remaining";
		}
		return str;
	}
	
	
	
}
