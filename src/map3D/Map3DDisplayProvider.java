package map3D;

import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;
import userDisplay.UserDisplayProvider;

public class Map3DDisplayProvider implements UserDisplayProvider {

	private Map3DControl map3DControl;
	
	public Map3DDisplayProvider(Map3DControl map3DControl) {
		super();
		this.map3DControl = map3DControl;
	}

	@Override
	public String getName() {
		return map3DControl.getUnitName();
	}

	@Override
	public UserDisplayComponent getComponent(UserDisplayControl userDisplayControl, String uniqueDisplayName) {
		return new Map3DDisplayComponent(this, map3DControl, userDisplayControl, uniqueDisplayName);
	}

	@Override
	public Class getComponentClass() {
		return Map3DDisplayComponent.class;
		
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
