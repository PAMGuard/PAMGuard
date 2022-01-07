package difar.display;

import java.awt.Component;

import difar.DifarControl;

import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;
import userDisplay.UserDisplayProvider;

public class DifarDisplayProvider implements UserDisplayProvider {

	private DifarControl difarControl;
	
	private DifarDisplayContainer currentContainer;
	
	public DifarDisplayProvider(DifarControl difarControl) {
		super();
		this.difarControl = difarControl;
	}

	@Override
	public String getName() {
		return difarControl.getUnitName() + " Displays";
	}

	@Override
	public UserDisplayComponent getComponent(UserDisplayControl userDisplayControl, String uniqueDisplayName) {
		if (currentContainer != null) {
			return null;
		}
		currentContainer = difarControl.getDifarDisplayContainer();
		return currentContainer;
	}

	@Override
	public Class getComponentClass() {
		return DifarDisplayContainer.class;
	}

	@Override
	public int getMaxDisplays() {
		return 1;
	}

	@Override
	public boolean canCreate() {
		return (currentContainer == null);
	}

	@Override
	public void removeDisplay(UserDisplayComponent component) {
		currentContainer = null;
	}

}
