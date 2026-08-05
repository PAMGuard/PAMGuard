package clipgenerator.clipDisplay;

import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;
import userDisplay.UserDisplayProvider;

public class ClipDisplayProvider implements UserDisplayProvider {
	
	private ClipDisplayParent clipDisplayParent;
	private String name;

	public ClipDisplayProvider(ClipDisplayParent clipDisplayParent, String name) {
		this.clipDisplayParent = clipDisplayParent;
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public UserDisplayComponent getComponent(UserDisplayControl userDisplayControl, String uniqueDisplayName) {
		return new ClipDisplayPanel(clipDisplayParent);
	}

	@Override
	public Class getComponentClass() {
		return ClipDisplayPanel.class;
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
