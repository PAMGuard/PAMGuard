package difar.display;

import difar.DifarControl;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;
import userDisplay.UserDisplayProvider;

/**
 * Offers the match candidate table as a user display component.
 */
public class DifarMatchProvider implements UserDisplayProvider {

	private final DifarControl difarControl;

	private DifarMatchContainer currentContainer;

	public DifarMatchProvider(DifarControl difarControl) {
		super();
		this.difarControl = difarControl;
	}

	@Override
	public String getName() {
		return difarControl.getUnitName() + " Match Candidates";
	}

	@Override
	public UserDisplayComponent getComponent(UserDisplayControl userDisplayControl, String uniqueDisplayName) {
		if (currentContainer != null) {
			return null; // return null if the display already exists.
		}
		currentContainer = difarControl.getMatchContainer();
		return currentContainer;
	}

	@Override
	public Class getComponentClass() {
		return DifarMatchContainer.class;
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
