package detectionview.swing;

import clipgenerator.clipDisplay.ClipDisplayProvider;
import detectionview.DVControl;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;

public class DVClipDisplayProvider extends ClipDisplayProvider {

	private DVControl dvControl;

	public DVClipDisplayProvider(DVControl dvControl, String name) {
		super(dvControl, name);
		this.dvControl = dvControl;
	}

	@Override
	public UserDisplayComponent getComponent(UserDisplayControl userDisplayControl, String uniqueDisplayName) {
		return new DVClipDisplayPanel(dvControl);
	}

	@Override
	public Class getComponentClass() {
		return DVClipDisplayPanel.class;
	}

}
