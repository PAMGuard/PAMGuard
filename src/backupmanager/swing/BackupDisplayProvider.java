package backupmanager.swing;

import backupmanager.BackupManager;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;
import userDisplay.UserDisplayProvider;

public class BackupDisplayProvider implements UserDisplayProvider {

	private BackupManager backupManager;
	
	private BackupDisplayComponent backupDisplayComponent;

	public BackupDisplayProvider(BackupManager backupManager) {
		super();
		this.backupManager = backupManager;
	}
	
	@Override
	public String getName() {
		return backupManager.getUnitName() + " display";
	}


	@Override
	public UserDisplayComponent getComponent(UserDisplayControl userDisplayControl, String uniqueDisplayName) {
		if (backupDisplayComponent == null) {
			backupDisplayComponent = new BackupDisplayComponent(backupManager);
		}
		return backupDisplayComponent;
	}

	@Override
	public Class getComponentClass() {
		return BackupDisplayComponent.class;
	}

	@Override
	public int getMaxDisplays() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public boolean canCreate() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void removeDisplay(UserDisplayComponent component) {
		// TODO Auto-generated method stub
		
	}

}
