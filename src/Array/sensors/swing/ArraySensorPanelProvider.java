package Array.sensors.swing;

import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;
import userDisplay.UserDisplayProvider;

public class ArraySensorPanelProvider implements UserDisplayProvider {

	@Override
	public String getName() {
		return "Array sensor graphic";
	}

	@Override
	public UserDisplayComponent getComponent(UserDisplayControl userDisplayControl, String uniqueDisplayName) {
		// TODO Auto-generated method stub
		return new ArraySensorComponent(this, userDisplayControl, uniqueDisplayName);
	}

	@Override
	public Class getComponentClass() {
		return ArraySensorComponent.class;
	}

	@Override
	public int getMaxDisplays() {
		// TODO Auto-generated method stub
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
