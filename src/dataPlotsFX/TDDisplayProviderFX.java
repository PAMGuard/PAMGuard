package dataPlotsFX;

import PamController.PamController;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;
import userDisplay.UserDisplayProvider;

public class TDDisplayProviderFX implements UserDisplayProvider {

	static {
		if (PamController.getInstance().getJCompliance()>=1.8) {
			UserDisplayControl.addUserDisplayProvider(new TDDisplayProviderFX());
		}
		else {
			System.err.println("Cannot load a JavaFX display on Java version: "+  PamController.getInstance().getJCompliance());
		}
	}
	
	public TDDisplayProviderFX() {
		super();
	}

	@Override
	public String getName() {
		return "Time base data display fx";
	}

	@Override
	public UserDisplayComponent getComponent(UserDisplayControl userDisplayControl, String uniqueDisplayName) {
		return new TDControlAWT(this, userDisplayControl, uniqueDisplayName);
	}

	@Override
	public Class getComponentClass() {
		return TDControlAWT.class;
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
