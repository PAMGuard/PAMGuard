package rawDeepLearningClassifier.swing;

import clipgenerator.clipDisplay.ClipDisplayPanel;
import rawDeepLearningClassifier.DLControl;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;
import userDisplay.UserDisplayProvider;

public class DLClipDisplayProvider   implements UserDisplayProvider {
	
	private DLControl dlControl;

	public DLClipDisplayProvider(DLControl dlControl) {
		super();
		this.dlControl = dlControl;
	}

	@Override
	public String getName() {
		return dlControl.getUnitName() + " clips";
	}

	@Override
	public UserDisplayComponent getComponent(UserDisplayControl userDisplayControl, String uniqueDisplayName) {
		return new ClipDisplayPanel(dlControl);
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
