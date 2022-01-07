package detectiongrouplocaliser.dialogs;

import detectiongrouplocaliser.DetectionGroupControl;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;
import userDisplay.UserDisplayProvider;

public class DetectionGroupTableProvider implements UserDisplayProvider {

	private  DetectionGroupControl detectionGroupControl;
	
	public DetectionGroupTableProvider(DetectionGroupControl detectionGroupControl) {
		super();
		this.detectionGroupControl = detectionGroupControl;
	}

	@Override
	public String getName() {
		return detectionGroupControl.getUnitName() + " data display";
	}

	@Override
	public UserDisplayComponent getComponent(UserDisplayControl userDisplayControl, String uniqueDisplayName) {
		return new DetectionGroupTable(detectionGroupControl.getDetectionGroupProcess());
	}

	@Override
	public Class getComponentClass() {
		return DetectionGroupTable.class;
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
		
	}

}
