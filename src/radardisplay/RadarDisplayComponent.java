package radardisplay;

import java.awt.Component;

import Layout.PamGraphLayout;
import userDisplay.UserDisplayComponentAdapter;
import userDisplay.UserDisplayControl;

public class RadarDisplayComponent extends UserDisplayComponentAdapter {

	private UserDisplayControl userDisplayControl;
	private RadarDisplay radarDisplay;
	private PamGraphLayout graphLayout;
	public RadarDisplayComponent(UserDisplayControl userDisplayControl, RadarParameters radarParameters,
			String uniqueDisplayName) {
		this.userDisplayControl = userDisplayControl;
		setUniqueName(uniqueDisplayName);
		radarDisplay = new RadarDisplay(userDisplayControl, radarParameters, this);
		graphLayout = new PamGraphLayout(radarDisplay);
	}
	@Override
	public Component getComponent() {
		return graphLayout.getMainComponent();
	}
	/* (non-Javadoc)
	 * @see userDisplay.UserDisplayComponentAdapter#notifyModelChanged(int)
	 */
	@Override
	public void notifyModelChanged(int changeType) {
		radarDisplay.notifyModelChanged(changeType);
	}

}
