package backupmanager.swing;

import java.awt.Component;

import backupmanager.BackupManager;
import userDisplay.UserDisplayComponent;

public class BackupDisplayComponent implements UserDisplayComponent {

	private BackupManager backupManager;
	
	private BackupControlPanel backupControlPanel;
	
	private String uniqueName;
	
	public BackupDisplayComponent(backupmanager.BackupManager backupManager) {
		super();
		this.backupManager = backupManager;
		backupControlPanel = new BackupControlPanel(null, backupManager);
	}

	@Override
	public Component getComponent() {
		return backupControlPanel.getComponent();
	}

	@Override
	public void openComponent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeComponent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyModelChanged(int changeType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getUniqueName() {
		return uniqueName;
	}

	@Override
	public void setUniqueName(String uniqueName) {
		this.uniqueName =uniqueName;
	}

	@Override
	public String getFrameTitle() {
		return backupManager.getUnitName();
	}

}
