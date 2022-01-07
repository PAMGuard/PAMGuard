package bearinglocaliser.display;

import bearinglocaliser.BearingLocaliserControl;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;
import userDisplay.UserDisplayProvider;

public class BearingDisplayProvider implements UserDisplayProvider {

	private BearingLocaliserControl bearingLocaliserControl;
	
	private BearingDisplayComponent bearingDisplayComponent;

	public BearingDisplayProvider(BearingLocaliserControl bearingLocaliserControl) {
		super();
		this.bearingLocaliserControl = bearingLocaliserControl;
	}

	@Override
	public String getName() {
		return bearingLocaliserControl.getUnitName() + " display";
	}

	@Override
	public UserDisplayComponent getComponent(UserDisplayControl userDisplayControl, String uniqueDisplayName) {
		bearingDisplayComponent = new BearingDisplayComponent(this, bearingLocaliserControl, userDisplayControl, uniqueDisplayName);
		return bearingDisplayComponent;
	}

	@Override
	public Class getComponentClass() {
		return BearingDisplayComponent.class;
	}

	@Override
	public int getMaxDisplays() {
		return 1;
	}

	@Override
	public boolean canCreate() {
		return (bearingDisplayComponent == null);
	}

	@Override
	public void removeDisplay(UserDisplayComponent component) {
		if (bearingDisplayComponent != null) {
			bearingLocaliserControl.getConfigObservable().removeObserver(bearingDisplayComponent.getFxBearingDisplay());	
			bearingDisplayComponent = null;
		}
	}

}
