package backupmanager.swing;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import backupmanager.BackupManager;
import backupmanager.BackupProgress;
import backupmanager.stream.BackupStream;

/**
 * Main panel for handling, configuring and displaying backup progress. 
 * @author dg50
 *
 */
public class BackupControlPanel {
	
	private JPanel mainPanel;
	
	private JPanel modulePanel;
	
	private BackupManager backupManager;

	private ArrayList<BackupStream> backupStreams;
	
	private ArrayList<BackupStreamPanel> backupStreamPanels = new ArrayList<BackupStreamPanel>();

	private Window owner;

	public BackupControlPanel(Window owner, BackupManager backupManager) {
		super();
		this.owner = owner;
		this.backupManager = backupManager;
		mainPanel = new JPanel(new BorderLayout());
		
		mainPanel.add(new SchedulePanel(backupManager).getComponent(), BorderLayout.NORTH);
		
		modulePanel = new JPanel();
		modulePanel.setLayout(new BoxLayout(modulePanel, BoxLayout.Y_AXIS));
		
		JPanel nAlPanel = new JPanel(new BorderLayout());
		nAlPanel.add(modulePanel, BorderLayout.NORTH);
		JPanel wAlPanel = new JPanel(new BorderLayout());
		wAlPanel.add(nAlPanel, BorderLayout.WEST);
		
		JScrollPane scrollPane = new JScrollPane(wAlPanel);
		mainPanel.add(scrollPane, BorderLayout.CENTER);
		
		fillModulePanel();
		
		backupManager.addObserver(new BackupObserver());
	}
	
	public JComponent getComponent() {
		return mainPanel;
	}
	
	public void fillModulePanel() {
		modulePanel.removeAll();
		backupStreams = backupManager.findBackupStreams();
		backupStreamPanels.clear();
		for (BackupStream bs : backupStreams) {
			BackupStreamPanel bsp = new BackupStreamPanel(owner, this, bs);
			modulePanel.add(bsp.getComponent());
			backupStreamPanels.add(bsp);
		}
		if (owner != null) {
			owner.pack();
		}
	}

	class BackupObserver implements backupmanager.BackupObserver {

		@Override
		public boolean update(BackupProgress backupProgress) {
			return progressUpdate(backupProgress);
		}

		@Override
		public void configurationChange() {
			fillModulePanel();
		}
		
	}

	public boolean progressUpdate(BackupProgress backupProgress) {
		// TODO Auto-generated method stub
		for (BackupStreamPanel bsp : backupStreamPanels) {
			if (backupProgress.getBackupStream()  == null || backupProgress.getBackupStream() == bsp.getBackupStream()) {
				boolean ok = bsp.progressUpdate(backupProgress);
			}
		}
		return true;
	}

	/**
	 * @return the backupManager
	 */
	public BackupManager getBackupManager() {
		return backupManager;
	}

}
