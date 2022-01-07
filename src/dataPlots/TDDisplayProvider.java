package dataPlots;

import PamModel.SMRUEnable;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;
import userDisplay.UserDisplayProvider;

public class TDDisplayProvider implements UserDisplayProvider {

	static {
		if (SMRUEnable.isEnable()) {
			UserDisplayControl.addUserDisplayProvider(new TDDisplayProvider());
		}
	}
	
	public TDDisplayProvider() {
		super();
	}

	@Override
	public String getName() {
		return "Time base data display";
	}

	@Override
	public UserDisplayComponent getComponent(UserDisplayControl userDisplayControl, String uniqueDisplayName) {
		return new TDControl();
	}

	@Override
	public Class getComponentClass() {
		return TDControl.class;
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

	/**
	 * Gets called from user display - does nothing except force
	 * instantiation of methods - and registration !
	 */
	public static void register() {}


}
