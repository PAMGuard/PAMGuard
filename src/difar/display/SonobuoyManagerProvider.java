package difar.display;

import difar.DifarControl;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;
import userDisplay.UserDisplayProvider;

public class SonobuoyManagerProvider implements UserDisplayProvider {


	private DifarControl difarControl;
	
	private SonobuoyManagerContainer currentContainer;
	
	public SonobuoyManagerProvider(DifarControl difarControl) {
		super();
		this.difarControl = difarControl;
	}

	@Override
	public String getName() {
		return difarControl.getUnitName() + " Sonobuoy Manager";
	}


	@Override
	public UserDisplayComponent getComponent(UserDisplayControl userDisplayControl, String uniqueDisplayName) {
		if (currentContainer != null) {
			return null; // return null if the display already exists. 
		}
		currentContainer = difarControl.getSonobuoyManagerContainer();
		return currentContainer;
	}

	@Override
	public Class getComponentClass() {
		return SonobuoyManagerContainer.class;
	}

	@Override
	public int getMaxDisplays() {
		return 0;
	}

	@Override
	public boolean canCreate() {
		return currentContainer == null;
	}

	@Override
	public void removeDisplay(UserDisplayComponent component) {
		currentContainer = null;
	}

}
