package effortmonitor.swing;

import Layout.DisplayPanel;
import Layout.DisplayPanelContainer;
import Layout.DisplayPanelProvider;
import effortmonitor.EffortControl;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;
import userDisplay.UserDisplayProvider;

public class EffortDisplayProvider implements UserDisplayProvider {

	private EffortControl effortControl;

	public EffortDisplayProvider(EffortControl effortControl) {
		this.effortControl = effortControl;
	}

	@Override
	public String getName() {
		return effortControl.getUnitName() + " table";
	}

	@Override
	public UserDisplayComponent getComponent(UserDisplayControl userDisplayControl, String uniqueDisplayName) {
		return new EffortDisplayPanel(this, effortControl, uniqueDisplayName);
	}

	@Override
	public Class getComponentClass() {
		return EffortDisplayPanel.class;
	}

	@Override
	public int getMaxDisplays() {
		return 0;
	}

	@Override
	public boolean canCreate() {
		return true;
	}

	@Override
	public void removeDisplay(UserDisplayComponent component) {
		// TODO Auto-generated method stub
		
	}

}
