package analoginput.swing;

import analoginput.AnalogDevicesManager;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;
import userDisplay.UserDisplayProvider;

public class AnalogDiagnosticsDisplayProvider implements UserDisplayProvider {

	private AnalogDevicesManager analogDevicesManager;

	public AnalogDiagnosticsDisplayProvider(AnalogDevicesManager analogDevicesManager) {
		this.analogDevicesManager = analogDevicesManager;
	}

	@Override
	public String getName() {
		return analogDevicesManager.getSensorUser().getUserName() + " table";
	}

	@Override
	public UserDisplayComponent getComponent(UserDisplayControl userDisplayControl, String uniqueDisplayName) {
		// TODO Auto-generated method stub
		return new AnalogDiagnosticsDisplay(analogDevicesManager, userDisplayControl, uniqueDisplayName);
	}

	@Override
	public Class getComponentClass() {
		return AnalogDiagnosticsDisplay.class;
	}

	@Override
	public int getMaxDisplays() {
		return 1;
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
