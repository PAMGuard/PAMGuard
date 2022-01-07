package difar.display;

import difar.DifarControl;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;
import userDisplay.UserDisplayProvider;

public class DifarDisplayProvider2 implements UserDisplayProvider {


	private DifarControl difarControl;
	
	private DifarDisplayContainer2 currentContainer2;
	
	public DifarDisplayProvider2(DifarControl difarControl) {
		super();
		this.difarControl = difarControl;
	}

	@Override
	public String getName() {
		return difarControl.getUnitName() + " Queue";
	}


	@Override
	public UserDisplayComponent getComponent(UserDisplayControl userDisplayControl, String uniqueDisplayName) {
		if (currentContainer2 != null) {
			return null; // return null if the display already exists. 
		}
		currentContainer2 = difarControl.getDifarDisplayContainer2();
		return currentContainer2;
	}

	@Override
	public Class getComponentClass() {
		return DifarDisplayContainer2.class;
	}

	@Override
	public int getMaxDisplays() {
		return 1;
	}

	@Override
	public boolean canCreate() {
		return currentContainer2 == null;
	}

	@Override
	public void removeDisplay(UserDisplayComponent component) {
		currentContainer2 = null;
	}

}
