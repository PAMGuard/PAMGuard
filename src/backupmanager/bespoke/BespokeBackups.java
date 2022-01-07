package backupmanager.bespoke;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import backupmanager.BackupManager;

/**
 * Can back up anything, PAMGuard or not. Just needs a folder, a file mask, etc. 
 * @author dg50
 *
 */
public class BespokeBackups implements PamSettings {

	private BackupManager backupManager;
	
	private BespokeSettings bespokeSettings = new BespokeSettings();
	
	private ArrayList<BespokeFileStream> fileStreams = new ArrayList<BespokeFileStream>();

	public BespokeBackups(BackupManager backupManager) {
		super();
		this.backupManager = backupManager;
		PamSettingManager.getInstance().registerSettings(this);
		makeBespokeStreams();
	}
	
	private void makeBespokeStreams() {
		for (BespokeIdentity bi : bespokeSettings.identities) {
			addBespokeStream(bi);			
		}
	}

	private void addBespokeStream(BespokeIdentity bi) {
		BespokeFileStream bfs = new BespokeFileStream(bi, bi.getName());
		fileStreams.add(bfs);
	}

	public int addMenuItems(JMenuItem menu) {
		JMenuItem add = new JMenuItem("Add stream ...");
		add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createStream(e);
			}
		});
		menu.add(add);
		
		JMenu remove = new JMenu("Remove stream");
		for (BespokeIdentity bi : bespokeSettings.identities) {
			JMenuItem mi = new JMenuItem(bi.getUnitName());
			mi.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					removeStream(bi);
				}
			});
			remove.add(mi);
		}
		menu.add(remove);
		return 1;
	}

	protected void removeStream(BespokeIdentity bi) {
		// TODO Auto-generated method stub
		
	}

	protected void createStream(ActionEvent e) {
		String streamName = BespokeNameDialog.showDialog(backupManager.getGuiFrame(), this);
		if (streamName == null) {
			return;
		}
		BespokeIdentity newId = new BespokeIdentity(streamName);
		bespokeSettings.identities.add(newId);
		addBespokeStream(newId);
		backupManager.updateConfiguration();
	}

	public BespokeIdentity findByName(String streamName) {
		for (BespokeIdentity bi : bespokeSettings.identities) {
			if (bi.getName().equals(streamName)) {
				return bi;
			}
		}
		return null;
	}

	@Override
	public String getUnitName() {
		return backupManager.getUnitName();
	}

	@Override
	public String getUnitType() {
		return "Bespoke Backups";
	}

	@Override
	public Serializable getSettingsReference() {
		return bespokeSettings;
	}

	@Override
	public long getSettingsVersion() {
		return BespokeSettings.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		bespokeSettings = (BespokeSettings) pamControlledUnitSettings.getSettings();
		return true;
	}

	/**
	 * @return the fileStreams
	 */
	public ArrayList<BespokeFileStream> getFileStreams() {
		return fileStreams;
	}
	
}
